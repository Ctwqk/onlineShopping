package com.taiwei.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taiwei.reggie.dto.DishDto;
import com.taiwei.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    void saveWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);

    /**
     * Deletes the given dishes with their flavours. Dishes that are still on sale or that belong to a setmeal are
     * kept.
     *
     * @return true when every requested dish was deleted
     * @throws com.taiwei.reggie.common.CustomException when nothing at all could be deleted
     */
    boolean removeWithFlavor(List<Long> ids);

    void updateStatus(List<Long> ids, int status);

    /** On-sale dishes with their flavours (cached); {@code categoryId == null} lists every category. */
    List<DishDto> listOnSale(Long categoryId);

    /** Admin listing, newest first, with the category name resolved. */
    Page<DishDto> pageWithCategory(int page, int pageSize, String name);
}
