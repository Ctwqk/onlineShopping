package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.dto.OrdersDto;
import com.taiwei.reggie.entity.AddressBook;
import com.taiwei.reggie.entity.Dish;
import com.taiwei.reggie.entity.OrderDetail;
import com.taiwei.reggie.entity.Orders;
import com.taiwei.reggie.entity.Setmeal;
import com.taiwei.reggie.entity.ShoppingCart;
import com.taiwei.reggie.entity.User;
import com.taiwei.reggie.mapper.OrderMapper;
import com.taiwei.reggie.service.AddressBookService;
import com.taiwei.reggie.service.DishService;
import com.taiwei.reggie.service.OrderDetailService;
import com.taiwei.reggie.service.OrderService;
import com.taiwei.reggie.service.SetmealService;
import com.taiwei.reggie.service.ShoppingCartService;
import com.taiwei.reggie.service.UserService;
import com.taiwei.reggie.util.Money;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    /** Allowed next states per current state. */
    private static final Map<Integer, Set<Integer>> TRANSITIONS = Map.of(
            AWAITING_PAYMENT, Set.of(AWAITING_DISPATCH, CANCELLED),
            AWAITING_DISPATCH, Set.of(DISPATCHED, CANCELLED),
            DISPATCHED, Set.of(COMPLETED, CANCELLED));

    private final OrderDetailService orderDetailService;
    private final ShoppingCartService shoppingCartService;
    private final UserService userService;
    private final AddressBookService addressBookService;
    private final DishService dishService;
    private final SetmealService setmealService;

    public OrderServiceImpl(OrderDetailService orderDetailService, ShoppingCartService shoppingCartService,
                            UserService userService, AddressBookService addressBookService,
                            DishService dishService, SetmealService setmealService) {
        this.orderDetailService = orderDetailService;
        this.shoppingCartService = shoppingCartService;
        this.userService = userService;
        this.addressBookService = addressBookService;
        this.dishService = dishService;
        this.setmealService = setmealService;
    }

    @Override
    @Transactional
    public Orders submit(Long userId, Orders order) {
        List<ShoppingCart> cart = shoppingCartService.listFor(userId);
        if (cart.isEmpty()) {
            throw new CustomException("cart is empty");
        }
        AddressBook address = addressBookService.findOwned(userId, order.getAddressBookId());
        if (address == null) {
            throw new CustomException("no address information found");
        }
        User user = userService.getById(userId);

        Long orderId = IdWorker.getId();
        List<OrderDetail> details = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ShoppingCart item : cart) {
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(orderId);
            detail.setNumber(item.getNumber());
            detail.setDishFlavor(item.getDishFlavor());
            detail.setDishId(item.getDishId());
            detail.setSetmealId(item.getSetmealId());
            detail.setName(item.getName());
            detail.setImage(item.getImage());
            // price at the moment of checkout, not at the moment the item was put in the cart
            detail.setAmount(currentUnitPrice(item));
            details.add(detail);
            total = total.add(detail.getAmount().multiply(BigDecimal.valueOf(item.getNumber())));
        }

        LocalDateTime now = LocalDateTime.now();
        Orders saved = new Orders();
        saved.setId(orderId);
        saved.setNumber(String.valueOf(orderId));
        saved.setStatus(AWAITING_DISPATCH);
        saved.setUserId(userId);
        saved.setAddressBookId(address.getId());
        saved.setOrderTime(now);
        saved.setCheckoutTime(now);
        saved.setPayMethod(order.getPayMethod() == null ? 1 : order.getPayMethod());
        saved.setRemark(order.getRemark());
        saved.setAmount(total.setScale(2));
        saved.setUserName(user == null ? null : user.getName());
        saved.setConsignee(address.getConsignee());
        saved.setPhone(address.getPhone());
        saved.setAddress(join(address.getProvinceName(), address.getCityName(), address.getDistrictName(),
                address.getDetail()));
        this.save(saved);
        orderDetailService.saveBatch(details);
        shoppingCartService.clean(userId);
        return saved;
    }

    @Override
    @Transactional
    public Orders changeStatus(Long orderId, int newStatus) {
        Orders order = orderId == null ? null : this.getById(orderId);
        if (order == null) {
            throw new CustomException("order not found");
        }
        Set<Integer> allowed = TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        if (!allowed.contains(newStatus)) {
            throw new CustomException("order can not move from status " + order.getStatus() + " to " + newStatus);
        }
        order.setStatus(newStatus);
        this.updateById(order);
        return order;
    }

    @Override
    public Page<Orders> pageForAdmin(int page, int pageSize, String number, LocalDateTime begin, LocalDateTime end) {
        return this.page(new Page<>(page, pageSize), new LambdaQueryWrapper<Orders>()
                .like(StringUtils.hasText(number), Orders::getNumber, number)
                .ge(begin != null, Orders::getOrderTime, begin)
                .le(end != null, Orders::getOrderTime, end)
                .orderByDesc(Orders::getOrderTime));
    }

    @Override
    public Page<OrdersDto> pageForUser(Long userId, int page, int pageSize) {
        Page<Orders> orders = this.page(new Page<>(page, pageSize), new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId).orderByDesc(Orders::getOrderTime));

        List<Long> ids = orders.getRecords().stream().map(Orders::getId).toList();
        Map<Long, List<OrderDetail>> detailsByOrder = ids.isEmpty() ? Map.of()
                : orderDetailService.list(new LambdaQueryWrapper<OrderDetail>().in(OrderDetail::getOrderId, ids))
                .stream().collect(Collectors.groupingBy(OrderDetail::getOrderId));

        Page<OrdersDto> result = new Page<>(orders.getCurrent(), orders.getSize(), orders.getTotal());
        result.setRecords(orders.getRecords().stream().map(order -> {
            OrdersDto dto = new OrdersDto();
            BeanUtils.copyProperties(order, dto);
            dto.setOrderDetails(detailsByOrder.getOrDefault(order.getId(), List.of()));
            return dto;
        }).toList());
        return result;
    }

    @Override
    // Deliberately not @Transactional: addItem() rejects withdrawn items by throwing, which would mark a shared
    // transaction rollback-only even though we skip those items on purpose.
    public void again(Long userId, Long orderId) {
        Orders order = orderId == null ? null : this.getById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new CustomException("order not found");
        }
        List<OrderDetail> details = orderDetailService.list(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId));
        int added = 0;
        for (OrderDetail detail : details) {
            for (int i = 0; i < detail.getNumber(); i++) {
                ShoppingCart request = new ShoppingCart();
                request.setDishId(detail.getDishId());
                request.setSetmealId(detail.getSetmealId());
                request.setDishFlavor(detail.getDishFlavor());
                try {
                    shoppingCartService.addItem(userId, request);
                } catch (CustomException unavailable) {
                    break; // item was withdrawn from the menu since; skip it
                }
                added++;
            }
        }
        if (added == 0) {
            throw new CustomException("none of the items of this order are available any more");
        }
    }

    private BigDecimal currentUnitPrice(ShoppingCart item) {
        if (item.getDishId() != null) {
            Dish dish = dishService.getById(item.getDishId());
            if (dish == null || !Integer.valueOf(1).equals(dish.getStatus())) {
                throw new CustomException("\"" + item.getName() + "\" is no longer available, please remove it");
            }
            return Money.centsToUnits(dish.getPrice());
        }
        Setmeal setmeal = setmealService.getById(item.getSetmealId());
        if (setmeal == null || !Integer.valueOf(1).equals(setmeal.getStatus())) {
            throw new CustomException("\"" + item.getName() + "\" is no longer available, please remove it");
        }
        return Money.centsToUnits(setmeal.getPrice());
    }

    private static String join(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part != null) {
                sb.append(part);
            }
        }
        return sb.toString();
    }
}
