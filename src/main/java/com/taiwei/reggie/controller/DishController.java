package com.taiwei.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.dto.DishDto;
import com.taiwei.reggie.entity.Dish;
import com.taiwei.reggie.service.DishService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dish")
@Tag(name = "Dishes")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("Added");
    }

    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
        return R.success(dishService.pageWithCategory(page, pageSize, name));
    }

    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        return R.success(dishService.getByIdWithFlavor(id));
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("updated");
    }

    /** Batch start / stop selling; {@code ids} is a comma-separated list. */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable("status") int status, @RequestParam("ids") List<Long> ids) {
        dishService.updateStatus(ids, status);
        return R.success("Status updated");
    }

    /** On-sale dishes of a category with their flavours (served from the Redis-backed cache). */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        return R.success(dishService.listOnSale(dish.getCategoryId()));
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        if (dishService.removeWithFlavor(ids)) {
            return R.success("all deleted");
        }
        return R.success("deleted, except dishes that are on sale or part of a setmeal");
    }
}
