package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(Dish::getCategoryId, id);
        int count = dishService.count(dishQueryWrapper);
        if(count>0){
            throw new CustomException("some dishes are linked to category!");
        }
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        count = setmealService.count(setmealLambdaQueryWrapper);
        if(count>0){
            throw new CustomException("some setmeals are linked to category!");
        }

        super.removeById(id);
    }
}
