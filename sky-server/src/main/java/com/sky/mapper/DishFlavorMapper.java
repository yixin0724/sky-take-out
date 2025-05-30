package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author yixin
 * @date 2025/5/30
 * @description
 */
@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入口味数据
     * 很明显是要用到动态sql。所以直接用xml
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品id删除对应的口味数据
     * 这个形参id起什么都可以，但要见名识意
     * @param dishId
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);

    /**
     * 根据菜品id集合批量删除对应的口味数据
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);
}
