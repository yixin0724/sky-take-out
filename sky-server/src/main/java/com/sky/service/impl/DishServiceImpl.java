package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author yixin
 * @date 2025/5/30
 * @description
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品对应的口味数据
     * 因为涉及多个表，要保证数据库的一致性，所以要开启事务，使用事务注解
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // dishDTO --> dish，只有属性名一致才能赋值
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //向菜品表插入1条数据，只需插需要插入的属性即可
        dishMapper.insert(dish);

        //获取insert语句生成的主键值，因为这是新增菜品，这个主键还没指，只能通过sql中去获取主键值
        Long dishId = dish.getId();
        //获取口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //因为有可能用户是没有提交口味的，所以要进行处理
        if (flavors != null && flavors.size() > 0) {
            //为dishFlavor设置dishId
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //向口味表插入n条数据，可以批量插入，不一定是一条一条插入
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
