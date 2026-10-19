package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.dto.DishDto;
import com.taiwei.reggie.dto.SetmealDto;
import com.taiwei.reggie.entity.Category;
import com.taiwei.reggie.entity.Dish;
import com.taiwei.reggie.entity.Setmeal;
import com.taiwei.reggie.entity.SetmealDish;
import com.taiwei.reggie.mapper.SetmealMapper;
import com.taiwei.reggie.service.CategoryService;
import com.taiwei.reggie.service.DishService;
import com.taiwei.reggie.service.SetmealDishService;
import com.taiwei.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    public static final String CACHE = "setmealCache";

    private final SetmealDishService setmealDishService;
    private final CategoryService categoryService;
    private final DishService dishService;

    public SetmealServiceImpl(SetmealDishService setmealDishService, @Lazy CategoryService categoryService,
                              DishService dishService) {
        this.setmealDishService = setmealDishService;
        this.categoryService = categoryService;
        this.dishService = dishService;
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE, allEntries = true)
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);
        saveDishes(setmealDto.getId(), setmealDto.getSetmealDishes());
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE, allEntries = true)
    public boolean removeWithDish(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException("nothing selected");
        }
        List<Long> deletable = this.list(new LambdaQueryWrapper<Setmeal>()
                        .in(Setmeal::getId, ids).eq(Setmeal::getStatus, 0))
                .stream().map(Setmeal::getId).toList();
        if (deletable.isEmpty()) {
            throw new CustomException("can not delete set meals in sale");
        }
        this.removeByIds(deletable);
        setmealDishService.remove(new LambdaQueryWrapper<SetmealDish>().in(SetmealDish::getSetmealId, deletable));
        return deletable.size() == ids.size();
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE, allEntries = true)
    public void updateWithDish(SetmealDto setmealDto) {
        if (setmealDto.getId() == null || this.getById(setmealDto.getId()) == null) {
            throw new CustomException("setmeal not found");
        }
        this.updateById(setmealDto);
        setmealDishService.remove(
                new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, setmealDto.getId()));
        saveDishes(setmealDto.getId(), setmealDto.getSetmealDishes());
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE, allEntries = true)
    public void updateStatus(List<Long> ids, int status) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException("nothing selected");
        }
        this.update(new LambdaUpdateWrapper<Setmeal>().in(Setmeal::getId, ids).set(Setmeal::getStatus, status));
    }

    @Override
    public SetmealDto getWithDishes(Long id) {
        Setmeal setmeal = this.getById(id);
        if (setmeal == null) {
            throw new CustomException("setmeal not found");
        }
        SetmealDto dto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, dto);
        Category category = categoryService.getById(setmeal.getCategoryId());
        dto.setCategoryName(category == null ? null : category.getName());
        dto.setSetmealDishes(setmealDishService.list(
                new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, id)
                        .orderByAsc(SetmealDish::getSort)));
        return dto;
    }

    @Override
    @Cacheable(value = CACHE, key = "'category:' + #categoryId + ':status:' + #status")
    public List<Setmeal> listBy(Long categoryId, Integer status) {
        return new ArrayList<>(this.list(new LambdaQueryWrapper<Setmeal>()
                .eq(categoryId != null, Setmeal::getCategoryId, categoryId)
                .eq(status != null, Setmeal::getStatus, status)
                .orderByDesc(Setmeal::getUpdateTime)));
    }

    @Override
    public List<DishDto> dishesOf(Long setmealId) {
        List<SetmealDish> links = setmealDishService.list(
                new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, setmealId)
                        .orderByAsc(SetmealDish::getSort));
        if (links.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Dish> dishes = dishService.listByIds(links.stream().map(SetmealDish::getDishId).toList())
                .stream().collect(Collectors.toMap(Dish::getId, Function.identity()));
        List<DishDto> result = new ArrayList<>();
        for (SetmealDish link : links) {
            Dish dish = dishes.get(link.getDishId());
            if (dish == null) {
                continue;
            }
            DishDto dto = new DishDto();
            BeanUtils.copyProperties(dish, dto);
            dto.setCopies(link.getCopies());
            result.add(dto);
        }
        return result;
    }

    @Override
    public Page<SetmealDto> pageWithCategory(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        this.page(pageInfo, new LambdaQueryWrapper<Setmeal>()
                .like(StringUtils.hasText(name), Setmeal::getName, name)
                .orderByDesc(Setmeal::getUpdateTime));

        List<Long> categoryIds = pageInfo.getRecords().stream().map(Setmeal::getCategoryId).distinct().toList();
        Map<Long, String> categoryNames = categoryIds.isEmpty() ? Map.of()
                : categoryService.listByIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        Page<SetmealDto> result = new Page<>(pageInfo.getCurrent(), pageInfo.getSize(), pageInfo.getTotal());
        result.setRecords(pageInfo.getRecords().stream().map(setmeal -> {
            SetmealDto dto = new SetmealDto();
            BeanUtils.copyProperties(setmeal, dto);
            dto.setCategoryName(categoryNames.get(setmeal.getCategoryId()));
            return dto;
        }).toList());
        return result;
    }

    private void saveDishes(Long setmealId, List<SetmealDish> dishes) {
        if (dishes == null || dishes.isEmpty()) {
            return;
        }
        dishes.forEach(item -> {
            item.setId(null);
            item.setSetmealId(setmealId);
        });
        setmealDishService.saveBatch(dishes);
    }
}
