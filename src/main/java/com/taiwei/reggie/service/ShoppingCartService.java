package com.taiwei.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taiwei.reggie.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {

    /**
     * Adds one unit of a dish (optionally with a flavour) or of a setmeal to the customer's cart. Name, image and
     * price are always taken from the catalogue, never from the request.
     *
     * @return the affected cart row; its {@code number} is the customer's total for that dish / setmeal across
     *         flavours (this is what the menu badge shows)
     * @throws com.taiwei.reggie.common.CustomException when the item does not exist or is not on sale
     */
    ShoppingCart addItem(Long userId, ShoppingCart request);

    /**
     * Removes one unit of a dish / setmeal; the row is deleted when it reaches zero. A no-op when the item is not in
     * the cart.
     *
     * @return the affected cart row with {@code number} = remaining total for that dish / setmeal
     */
    ShoppingCart subItem(Long userId, ShoppingCart request);

    List<ShoppingCart> listFor(Long userId);

    void clean(Long userId);
}
