package com.taiwei.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taiwei.reggie.common.CustomException;
import com.taiwei.reggie.dto.DishDto;
import com.taiwei.reggie.entity.Category;
import com.taiwei.reggie.entity.Dish;
import com.taiwei.reggie.entity.DishFlavor;
import com.taiwei.reggie.entity.SetmealDish;
import com.taiwei.reggie.mapper.DishMapper;
import com.taiwei.reggie.service.CategoryService;
import com.taiwei.reggie.service.DishFlavorService;
import com.taiwei.reggie.service.DishService;
import com.taiwei.reggie.service.SetmealDishService;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    public static final String CACHE = "dishCache";

    private final DishFlavorService dishFlavorService;
    private final SetmealDishService setmealDishService;
    private final CategoryService categoryService;

    public DishServiceImpl(DishFlavorService dishFlavorService, SetmealDishService setmealDishService,
                           @Lazy CategoryService categoryService) {
        this.dishFlavorService = dishFlavorService;
        this.setmealDishService = setmealDishService;
        this.categoryService = categoryService;
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE, allEntries = true)
    public void saveWithFlavor(DishDto dishDto) {
        this.save(dishDto);
        saveFlavors(dishDto.getId(), dishDto.getFlavors());
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);
        if (dish == null) {
            throw new CustomException("dish not found");
        }
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        dishDto.setFlavors(flavorsOf(List.of(id)).getOrDefault(id, new ArrayList<>()));
        return dishDto;
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE, allEntries = true)
    public void updateWithFlavor(DishDto dishDto) {
        if (dishDto.getId() == null || this.getById(dishDto.getId()) == null) {
            throw new CustomException("dish not found");
        }
        this.updateById(dishDto);
        dishFlavorService.remove(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, dishDto.getId()));
        saveFlavors(dishDto.getId(), dishDto.getFlavors());
    }

    private void saveFlavors(Long dishId, List<DishFlavor> flavors) {
        if (flavors == null || flavors.isEmpty()) {
            return;
        }
        flavors.forEach(flavor -> {
            flavor.setId(null);
            flavor.setDishId(dishId);
        });
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE, allEntries = true)
    public boolean removeWithFlavor(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException("nothing selected");
        }
        // dishes referenced by a setmeal must stay
        Set<Long> inSetmeal = setmealDishService.list(
                        new LambdaQueryWrapper<SetmealDish>().in(SetmealDish::getDishId, ids))
                .stream().map(SetmealDish::getDishId).collect(Collectors.toSet());
        List<Long> candidates = ids.stream().filter(id -> !inSetmeal.contains(id)).toList();
        if (candidates.isEmpty()) {
            throw new CustomException("dishes that belong to a setmeal can not be deleted");
        }
        // only dishes that are off sale can be deleted
        List<Long> deletable = this.list(new LambdaQueryWrapper<Dish>()
                        .eq(Dish::getStatus, 0).in(Dish::getId, candidates))
                .stream().map(Dish::getId).toList();
        if (deletable.isEmpty()) {
            throw new CustomException("dishes that are on sale can not be deleted");
        }
        dishFlavorService.remove(new LambdaQueryWrapper<DishFlavor>().in(DishFlavor::getDishId, deletable));
        this.removeByIds(deletable);
        return deletable.size() == ids.size();
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE, allEntries = true)
    public void updateStatus(List<Long> ids, int status) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException("nothing selected");
        }
        this.update(new LambdaUpdateWrapper<Dish>().in(Dish::getId, ids).set(Dish::getStatus, status));
    }

    @Override
    @Cacheable(value = CACHE, key = "'category:' + #categoryId")
    public List<DishDto> listOnSale(Long categoryId) {
        List<Dish> dishes = this.list(new LambdaQueryWrapper<Dish>()
                .eq(categoryId != null, Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus, 1)
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getUpdateTime));
        Map<Long, List<DishFlavor>> flavors = flavorsOf(dishes.stream().map(Dish::getId).toList());
        return dishes.stream().map(dish -> {
            DishDto dto = new DishDto();
            BeanUtils.copyProperties(dish, dto);
            dto.setFlavors(flavors.getOrDefault(dish.getId(), new ArrayList<>()));
            return dto;
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Page<DishDto> pageWithCategory(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        this.page(pageInfo, new LambdaQueryWrapper<Dish>()
                .like(StringUtils.hasText(name), Dish::getName, name)
                .orderByDesc(Dish::getUpdateTime));

        Collection<Long> categoryIds = pageInfo.getRecords().stream().map(Dish::getCategoryId).distinct().toList();
        Map<Long, String> categoryNames = categoryIds.isEmpty() ? Map.of()
                : categoryService.listByIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        Page<DishDto> result = new Page<>(pageInfo.getCurrent(), pageInfo.getSize(), pageInfo.getTotal());
        result.setRecords(pageInfo.getRecords().stream().map(dish -> {
            DishDto dto = new DishDto();
            BeanUtils.copyProperties(dish, dto);
            dto.setCategoryName(categoryNames.get(dish.getCategoryId()));
            return dto;
        }).toList());
        return result;
    }

    /** Flavours of several dishes in one query. */
    private Map<Long, List<DishFlavor>> flavorsOf(Collection<Long> dishIds) {
        if (dishIds.isEmpty()) {
            return Map.of();
        }
        return dishFlavorService.list(new LambdaQueryWrapper<DishFlavor>().in(DishFlavor::getDishId, dishIds))
                .stream().collect(Collectors.groupingBy(DishFlavor::getDishId));
    }
}
