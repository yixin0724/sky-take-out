package com.sky.service;

import com.sky.dto.DishDTO;
import org.springframework.stereotype.Service;

/**
 * @author yixin
 * @date 2025/5/30
 * @description 菜品功能管理相关的服务
 */
public interface DishService {

    /**
     * 新增菜品对应的口味数据
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);
}
