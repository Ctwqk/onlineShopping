package com.taiwei.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taiwei.reggie.dto.OrdersDto;
import com.taiwei.reggie.entity.Orders;

import java.time.LocalDateTime;

public interface OrderService extends IService<Orders> {

    /** Order status codes as stored in {@code orders.status}. */
    int AWAITING_PAYMENT = 1;
    int AWAITING_DISPATCH = 2;
    int DISPATCHED = 3;
    int COMPLETED = 4;
    int CANCELLED = 5;

    /**
     * Turns the customer's cart into an order: re-prices every line from the catalogue, snapshots the delivery
     * address, stores order + line items and empties the cart - all in one transaction.
     *
     * @param order carries addressBookId, payMethod and remark from the client; everything else is computed
     * @return the persisted order
     * @throws com.taiwei.reggie.common.CustomException for an empty cart, an unknown address or an item that is no
     *                                                  longer on sale
     */
    Orders submit(Long userId, Orders order);

    /**
     * Moves an order along its life cycle: awaiting dispatch -> dispatched -> completed; any open order can be
     * cancelled.
     *
     * @throws com.taiwei.reggie.common.CustomException for unknown orders or illegal transitions
     */
    Orders changeStatus(Long orderId, int newStatus);

    /** Admin listing: optional order-number substring and order-time window, newest first. */
    Page<Orders> pageForAdmin(int page, int pageSize, String number, LocalDateTime begin, LocalDateTime end);

    /** A customer's own orders, newest first, each with its line items. */
    Page<OrdersDto> pageForUser(Long userId, int page, int pageSize);

    /** Copies the line items of an earlier order into the customer's cart ("order again"). */
    void again(Long userId, Long orderId);
}
