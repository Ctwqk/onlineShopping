package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.dto.SetmealDto;
import com.taiwei.reggie.entity.Dish;
import com.taiwei.reggie.entity.Setmeal;
import com.taiwei.reggie.entity.SetmealDish;
import com.taiwei.reggie.mapper.SetmealMapper;
import com.taiwei.reggie.service.SetmealDishService;
import com.taiwei.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {


    @Autowired
    private SetmealDishService setmealDishService;


    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto){
        this.save(setmealDto);

        Long id = setmealDto.getId();
        List<SetmealDish> list = setmealDto.getSetmealDishes();

        list=list.stream().map((item)->{
            item.setSetmealId(id);
            return item;
        }).toList();

        setmealDishService.saveBatch(list);
    }

    @Transactional
    public boolean removeWithDish(List<Long> ids){
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Setmeal::getId,ids);
        lambdaQueryWrapper.eq(Setmeal::getStatus,0);


        List<Setmeal> result = this.list(lambdaQueryWrapper);
        if(result.isEmpty()){
            throw new CustomException("can not delete set meals in sale");
        }


        this.remove(lambdaQueryWrapper);
        List<Long> delIds = result.stream().map(Setmeal::getId).toList();

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.in(SetmealDish::getSetmealId,delIds);
        setmealDishService.remove(lambdaQueryWrapper1);
        return ids.size()==delIds.size();
    }
    public void updateWithDish(SetmealDto setmealDto){

        this.updateById(setmealDto);
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(lambdaQueryWrapper);

        List<SetmealDish> list = setmealDto.getSetmealDishes().stream().map((setmealDish)->{
            setmealDish.setSetmealId(setmealDto.getId());
            return setmealDish;
        }).toList();
        setmealDishService.saveBatch(list);

    }

}
