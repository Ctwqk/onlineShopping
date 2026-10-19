package com.taiwei.reggie.controller;

import com.taiwei.reggie.common.BaseContext;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.entity.ShoppingCart;
import com.taiwei.reggie.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/shoppingCart")
@RestController
@Tag(name = "Shopping cart (customer)")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    /** Adds one unit of a dish (+ optional flavour) or setmeal. Price/name/image come from the catalogue. */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        return R.success(shoppingCartService.addItem(BaseContext.getCurrentID(), shoppingCart));
    }

    /** Removes one unit of a dish / setmeal. */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        return R.success(shoppingCartService.subItem(BaseContext.getCurrentID(), shoppingCart));
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        return R.success(shoppingCartService.listFor(BaseContext.getCurrentID()));
    }

    @DeleteMapping("/clean")
    public R<String> clean() {
        shoppingCartService.clean(BaseContext.getCurrentID());
        return R.success("cart emptied");
    }
}
