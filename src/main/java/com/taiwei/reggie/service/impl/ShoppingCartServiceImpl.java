package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.entity.Dish;
import com.taiwei.reggie.entity.Setmeal;
import com.taiwei.reggie.entity.ShoppingCart;
import com.taiwei.reggie.mapper.ShoppingCartMapper;
import com.taiwei.reggie.service.DishService;
import com.taiwei.reggie.service.SetmealService;
import com.taiwei.reggie.service.ShoppingCartService;
import com.taiwei.reggie.util.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart>
        implements ShoppingCartService {

    private final DishService dishService;
    private final SetmealService setmealService;

    public ShoppingCartServiceImpl(DishService dishService, SetmealService setmealService) {
        this.dishService = dishService;
        this.setmealService = setmealService;
    }

    @Override
    @Transactional
    public ShoppingCart addItem(Long userId, ShoppingCart request) {
        requireExactlyOneProduct(request);
        String flavor = StringUtils.hasText(request.getDishFlavor()) ? request.getDishFlavor() : null;

        ShoppingCart row = findRow(userId, request.getDishId(), request.getSetmealId(), flavor);
        if (row != null) {
            // atomic increment: two quick taps on "+" must not lose an update
            this.update(new LambdaUpdateWrapper<ShoppingCart>()
                    .eq(ShoppingCart::getId, row.getId()).setSql("number = number + 1"));
        } else {
            row = priceFromCatalogue(request);
            row.setUserId(userId);
            row.setDishFlavor(request.getDishId() != null ? flavor : null);
            row.setNumber(1);
            row.setCreateTime(LocalDateTime.now());
            this.save(row);
        }
        ShoppingCart fresh = this.getById(row.getId());
        fresh.setNumber(totalFor(userId, request.getDishId(), request.getSetmealId()));
        return fresh;
    }

    @Override
    @Transactional
    public ShoppingCart subItem(Long userId, ShoppingCart request) {
        requireExactlyOneProduct(request);
        String flavor = StringUtils.hasText(request.getDishFlavor()) ? request.getDishFlavor() : null;

        LambdaQueryWrapper<ShoppingCart> query = productQuery(userId, request.getDishId(), request.getSetmealId());
        if (flavor != null) {
            query.eq(ShoppingCart::getDishFlavor, flavor);
        }
        // without an explicit flavour, take away from the most recently added row of that dish
        ShoppingCart row = this.list(query.orderByDesc(ShoppingCart::getCreateTime).last("LIMIT 1")).stream()
                .findFirst().orElse(null);
        if (row == null) {
            ShoppingCart empty = new ShoppingCart();
            empty.setDishId(request.getDishId());
            empty.setSetmealId(request.getSetmealId());
            empty.setNumber(0);
            return empty;
        }
        if (row.getNumber() > 1) {
            this.update(new LambdaUpdateWrapper<ShoppingCart>()
                    .eq(ShoppingCart::getId, row.getId()).setSql("number = number - 1"));
            row.setNumber(row.getNumber() - 1);
        } else {
            this.removeById(row.getId());
            row.setNumber(0);
        }
        row.setNumber(totalFor(userId, request.getDishId(), request.getSetmealId()));
        return row;
    }

    @Override
    public List<ShoppingCart> listFor(Long userId) {
        return this.list(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId).orderByAsc(ShoppingCart::getCreateTime));
    }

    @Override
    public void clean(Long userId) {
        this.remove(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId));
    }

    private void requireExactlyOneProduct(ShoppingCart request) {
        if ((request.getDishId() == null) == (request.getSetmealId() == null)) {
            throw new CustomException("specify either a dish or a setmeal");
        }
    }

    /** Name / image / unit price (in currency units) of an on-sale dish or setmeal. */
    private ShoppingCart priceFromCatalogue(ShoppingCart request) {
        ShoppingCart row = new ShoppingCart();
        if (request.getDishId() != null) {
            Dish dish = dishService.getById(request.getDishId());
            if (dish == null || !Integer.valueOf(1).equals(dish.getStatus())) {
                throw new CustomException("this dish is not available");
            }
            row.setDishId(dish.getId());
            row.setName(dish.getName());
            row.setImage(dish.getImage());
            row.setAmount(Money.centsToUnits(dish.getPrice()));
        } else {
            Setmeal setmeal = setmealService.getById(request.getSetmealId());
            if (setmeal == null || !Integer.valueOf(1).equals(setmeal.getStatus())) {
                throw new CustomException("this setmeal is not available");
            }
            row.setSetmealId(setmeal.getId());
            row.setName(setmeal.getName());
            row.setImage(setmeal.getImage());
            row.setAmount(Money.centsToUnits(setmeal.getPrice()));
        }
        return row;
    }

    private LambdaQueryWrapper<ShoppingCart> productQuery(Long userId, Long dishId, Long setmealId) {
        return new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .eq(dishId != null, ShoppingCart::getDishId, dishId)
                .eq(setmealId != null, ShoppingCart::getSetmealId, setmealId);
    }

    private ShoppingCart findRow(Long userId, Long dishId, Long setmealId, String flavor) {
        LambdaQueryWrapper<ShoppingCart> query = productQuery(userId, dishId, setmealId);
        if (dishId != null) {
            if (flavor == null) {
                query.isNull(ShoppingCart::getDishFlavor);
            } else {
                query.eq(ShoppingCart::getDishFlavor, flavor);
            }
        }
        return this.list(query.last("LIMIT 1")).stream().findFirst().orElse(null);
    }

    private int totalFor(Long userId, Long dishId, Long setmealId) {
        return this.list(productQuery(userId, dishId, setmealId)).stream()
                .mapToInt(ShoppingCart::getNumber).sum();
    }
}
