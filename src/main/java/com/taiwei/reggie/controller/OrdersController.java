package com.taiwei.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taiwei.reggie.common.BaseContext;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.dto.OrdersDto;
import com.taiwei.reggie.entity.Orders;
import com.taiwei.reggie.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/order")
@Tag(name = "Orders")
public class OrdersController {

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Customer: check out the current cart. */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders order) {
        orderService.submit(BaseContext.getCurrentID(), order);
        return R.success("order submitted");
    }

    /** Customer: own orders with line items, newest first. */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> userPage(int page, int pageSize) {
        return R.success(orderService.pageForUser(BaseContext.getCurrentID(), page, pageSize));
    }

    /** Customer: put the items of an earlier order back into the cart. */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders order) {
        orderService.again(BaseContext.getCurrentID(), order.getId());
        return R.success("items added to cart");
    }

    /** Admin: all orders, optionally filtered by order number and order-time window. */
    @GetMapping("/page")
    public R<Page<Orders>> page(int page, int pageSize, String number,
                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return R.success(orderService.pageForAdmin(page, pageSize, number, beginTime, endTime));
    }

    /** Admin: dispatch / complete / cancel an order ({@code {"id": ..., "status": 3}}). */
    @PutMapping
    public R<String> updateStatus(@RequestBody Orders order) {
        if (order.getStatus() == null) {
            return R.error("status is required");
        }
        orderService.changeStatus(order.getId(), order.getStatus());
        return R.success("order updated");
    }
}
