package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author yixin
 * @date 2025/5/30
 * @description
 */
@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询套餐id
     * select setmeal_id from setmeal_dish where dish_id in (?,?,?)
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量插入套餐菜品关系数据
     * 该表没有公共字段的属性因此不需要使用@AutoFill注解填充
     * insert into setmeal_dish (setmeal_id, dish_id, name, dish_flavor, price, number, amount) values (?, ?, ?, ?, ?, ?, ?)
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);
}
