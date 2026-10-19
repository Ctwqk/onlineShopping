package com.taiwei.reggie.dto;

import com.taiwei.reggie.entity.Setmeal;
import com.taiwei.reggie.entity.SetmealDish;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
