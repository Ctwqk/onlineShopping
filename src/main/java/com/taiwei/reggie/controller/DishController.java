package com.taiwei.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.dto.DishDto;
import com.taiwei.reggie.entity.*;
import com.taiwei.reggie.mapper.DishFlavorMapper;
import com.taiwei.reggie.service.CategoryService;
import com.taiwei.reggie.service.DishFlavorService;
import com.taiwei.reggie.service.DishService;
import com.taiwei.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SetmealDishService setmealDishService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        String queryKey = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(queryKey);
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
        String queryKey = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(queryKey);


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
        List<DishDto> resultList = null;
        String queryKey = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        resultList = (List<DishDto>)redisTemplate.opsForValue().get(queryKey);
        if(resultList != null){
            return R.success(resultList);
        }

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        resultList = list.stream().map((dish_)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish_,dishDto);
            Long dishID = dish_.getId();

            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId, dishID);
            dishDto.setFlavors(dishFlavorService.list(queryWrapper1));
            return dishDto;
        }).toList();
        redisTemplate.opsForValue().set(queryKey,resultList,60, TimeUnit.MINUTES);

        return R.success(resultList);
    }
    @DeleteMapping("/{ids}")
    public R<String> delete(@PathVariable List<Long> ids){

        if(dishService.removeWithFlavor(ids)){
            return R.success("all deleted");
        }
        else{
            return R.success("all not on sale deleted");
        }



    }
}
