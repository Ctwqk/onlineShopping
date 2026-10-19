package com.taiwei.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.dto.DishDto;
import com.taiwei.reggie.dto.SetmealDto;
import com.taiwei.reggie.entity.Setmeal;
import com.taiwei.reggie.service.SetmealService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/setmeal")
@Tag(name = "Setmeals (combos)")
public class SetmealController {

    private final SetmealService setmealService;

    public SetmealController(SetmealService setmealService) {
        this.setmealService = setmealService;
    }

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDish(setmealDto);
        return R.success("saved");
    }

    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name) {
        return R.success(setmealService.pageWithCategory(page, pageSize, name));
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        if (setmealService.removeWithDish(ids)) {
            return R.success("all selection deleted");
        }
        return R.success("deleted, except setmeals that are on sale");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id) {
        return R.success(setmealService.getWithDishes(id));
    }

    @PutMapping
    public R<String> put(@RequestBody SetmealDto setmealDto) {
        setmealService.updateWithDish(setmealDto);
        return R.success("updated");
    }

    /** Batch start / stop selling; {@code ids} is a comma-separated list. */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status, @RequestParam List<Long> ids) {
        setmealService.updateStatus(ids, status);
        return R.success("updated");
    }

    /** Setmeals of a category (served from the Redis-backed cache). */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        return R.success(setmealService.listBy(setmeal.getCategoryId(), setmeal.getStatus()));
    }

    /** Dishes (with portion counts) contained in a setmeal - the customer front's "combo details" dialog. */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> dishes(@PathVariable Long id) {
        return R.success(setmealService.dishesOf(id));
    }
}
