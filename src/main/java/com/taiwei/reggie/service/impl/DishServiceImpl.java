package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.dto.DishDto;
import com.taiwei.reggie.entity.Dish;
import com.taiwei.reggie.entity.DishFlavor;
import com.taiwei.reggie.entity.SetmealDish;
import com.taiwei.reggie.mapper.DishMapper;
import com.taiwei.reggie.service.DishFlavorService;
import com.taiwei.reggie.service.DishService;
import com.taiwei.reggie.service.SetmealDishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto){
        this.save(dishDto);
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((flavor)->{
            flavor.setDishId(dishId);
            return flavor;
        }).toList();
        dishFlavorService.saveBatch(flavors);


    }
    @Override
    public DishDto getByIdWithFlavor(Long id){
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper  = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);

        dishDto.setFlavors(dishFlavorService.list(queryWrapper));

        return dishDto;
    }

    @Override
    public void updateWithFlavor(DishDto dishDto){
        this.updateById(dishDto);
        Long dishId = dishDto.getId();
        LambdaQueryWrapper <DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishId);

        dishFlavorService.remove(queryWrapper);

        List<DishFlavor> list = dishDto.getFlavors();
        list = list.stream().map((flavor)->{
            flavor.setDishId(dishId);
            return flavor;
        }).toList();
        dishFlavorService.saveBatch(list);
    }



    @Override
    @Transactional
    public boolean removeWithFlavor(List<Long> ids){
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.in(SetmealDish::getDishId, ids);

        List<SetmealDish> setmealDishes = setmealDishService.list(lambdaQueryWrapper);




        Set<Long> idSet = setmealDishes.stream().map(SetmealDish::getDishId).collect(Collectors.toSet());
        List<Long> idsMid = ids.stream().filter(id->!idSet.contains(id)).toList();
        if(idsMid==null||idsMid.size()==0) {
            throw new CustomException("can not delete anything");
        }

        LambdaQueryWrapper<Dish> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.eq(Dish::getStatus,0);
        lambdaQueryWrapper1.in(Dish::getId,idsMid);
        List<Dish> dishDel = this.list(lambdaQueryWrapper1);
        List<Long> idDel = dishDel.stream().map(Dish::getId).toList();
        if(idDel==null||idDel.size()==0) {
            throw new CustomException("can not delete anything");
        }

        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper2.in(DishFlavor::getDishId, idDel);
        dishFlavorService.remove(lambdaQueryWrapper2);
        this.remove(lambdaQueryWrapper1);
        return ids.size()==idDel.size();
    }

}
