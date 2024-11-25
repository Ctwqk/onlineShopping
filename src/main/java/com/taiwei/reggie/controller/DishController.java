package com.taiwei.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.dto.DishDto;
import com.taiwei.reggie.entity.Category;
import com.taiwei.reggie.entity.Dish;
import com.taiwei.reggie.entity.DishFlavor;
import com.taiwei.reggie.mapper.DishFlavorMapper;
import com.taiwei.reggie.service.CategoryService;
import com.taiwei.reggie.service.DishFlavorService;
import com.taiwei.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("Added");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize, String name){
        Page<Dish> pageInfo  = new Page<Dish>();
        Page<DishDto> pageResult = new Page<>();
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(name!=null, Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo,queryWrapper);

        BeanUtils.copyProperties(pageInfo,pageResult, "records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((record)->{
            Long categoryId = record.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            DishDto dishDto= new DishDto();
            BeanUtils.copyProperties(record,dishDto);
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).toList();

        pageResult.setRecords(list);

        return R.success(pageResult);
    }


    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("updated");
    }

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable("status") int status, @RequestParam("ids")Long ids){

        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Dish::getId, ids).set(Dish::getStatus, status);
        dishService.update(updateWrapper);

        return R.success("Status updated");
    }

    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        queryWrapper.eq(Dish::getStatus,1);
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//    }
    public R<List<DishDto>> list(Dish dish){

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> resultList = list.stream().map((dish_)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish_,dishDto);
            Long dishID = dish_.getId();

            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId, dishID);
            dishDto.setFlavors(dishFlavorService.list(queryWrapper1));
            return dishDto;


        }).toList();


        return R.success(resultList);
    }
}
