package com.sky.mapper;

import com.sky.entity.DishFlavor;
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
}
