package com.taiwei.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taiwei.reggie.dto.SetmealDto;
import com.taiwei.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    public void saveWithDish(SetmealDto setmealDto);
    public boolean removeWithDish(List<Long> ids);

    public void updateWithDish(SetmealDto setmealDto);
}
