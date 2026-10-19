package com.taiwei.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taiwei.reggie.dto.DishDto;
import com.taiwei.reggie.dto.SetmealDto;
import com.taiwei.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    void saveWithDish(SetmealDto setmealDto);

    /**
     * Deletes setmeals that are off sale together with their dish links.
     *
     * @return true when every requested setmeal was deleted
     * @throws com.taiwei.reggie.common.CustomException when none could be deleted
     */
    boolean removeWithDish(List<Long> ids);

    void updateWithDish(SetmealDto setmealDto);

    void updateStatus(List<Long> ids, int status);

    SetmealDto getWithDishes(Long id);

    /** Setmeals of a category / status (cached). Both filters are optional. */
    List<Setmeal> listBy(Long categoryId, Integer status);

    /** The dishes of a setmeal as shown in the customer front's "combo details" dialog. */
    List<DishDto> dishesOf(Long setmealId);

    Page<SetmealDto> pageWithCategory(int page, int pageSize, String name);
}
