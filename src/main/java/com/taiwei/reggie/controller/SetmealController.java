package com.taiwei.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.dto.SetmealDto;
import com.taiwei.reggie.entity.Category;
import com.taiwei.reggie.entity.Setmeal;
import com.taiwei.reggie.entity.SetmealDish;
import com.taiwei.reggie.service.CategoryService;
import com.taiwei.reggie.service.SetmealDishService;
import com.taiwei.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;


    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        //log.info(setmealDto.toString());

        setmealService.saveWithDish(setmealDto);


        return R.success("saved");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        Page<Setmeal> pageInfo = new Page<>();
        Page<SetmealDto> pageResult = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);

        BeanUtils.copyProperties(pageInfo,pageResult, "records");

        List<Setmeal> list = pageInfo.getRecords();

        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();

        List<SetmealDto> listResult = list.stream().map((setmeal)->{

            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal,setmealDto);
            Category category = categoryService.getById(setmeal.getCategoryId());
            if(category !=null){
                setmealDto.setCategoryName(category.getName());
            }

            return setmealDto;
        }).toList();

        pageResult.setRecords(listResult);
        return R.success(pageResult);

    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        if(setmealService.removeWithDish(ids)){
            return R.success("all selection deleted");
        }
        else{
            return R.success("all not in sale selections deleted");
        }
    }

    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        //return R.success(setmealService.getSetmealWithFlavorById(id));
        Setmeal setmeal = setmealService.getById(id);
        SetmealDto setmealDto= new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        setmealDto.setCategoryName(categoryService.getById(setmeal.getCategoryId()).getName());
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<SetmealDish>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);
        //List<Long> dishIDs = setmealDishService.list(lambdaQueryWrapper).stream().map((SetmealDish::getDishId)).toList();
        setmealDto.setSetmealDishes(setmealDishService.list(lambdaQueryWrapper));
        return R.success(setmealDto);
    }

    @PutMapping
    public R<String> put(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("updated");
    }

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status, @RequestParam Long ids){
        Setmeal setmeal = new Setmeal();
        setmeal.setId(ids);
        setmeal.setStatus(status);
        setmealService.updateById(setmeal);
        return R.success("updated");
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list(   Setmeal setmeal){

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        return R.success(setmealService.list(queryWrapper));
    }
}
