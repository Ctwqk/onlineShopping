package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.entity.Category;
import com.taiwei.reggie.entity.Dish;
import com.taiwei.reggie.entity.Setmeal;
import com.taiwei.reggie.mapper.CategoryMapper;
import com.taiwei.reggie.service.CategoryService;
import com.taiwei.reggie.service.DishService;
import com.taiwei.reggie.service.SetmealService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final DishService dishService;
    private final SetmealService setmealService;

    public CategoryServiceImpl(DishService dishService, SetmealService setmealService) {
        this.dishService = dishService;
        this.setmealService = setmealService;
    }

    @Override
    public void remove(Long id) {
        if (dishService.count(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id)) > 0) {
            throw new CustomException("some dishes are linked to category!");
        }
        if (setmealService.count(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getCategoryId, id)) > 0) {
            throw new CustomException("some setmeals are linked to category!");
        }
        super.removeById(id);
    }
}
