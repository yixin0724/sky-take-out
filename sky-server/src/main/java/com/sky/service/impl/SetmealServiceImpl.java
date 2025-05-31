package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.SetmealAlreadyExistsException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yixin
 * @date 2025/5/31
 * @description 套餐业务实现
 */
@Service
public class SetmealServiceImpl implements SetmealService {

    private static final Logger log = LoggerFactory.getLogger(SetmealServiceImpl.class);
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐，同时保存套餐相关联的菜品
     *
     * @param setmealDTO
     */
    public void saveWithDish(SetmealDTO setmealDTO) {
        //将DTO转换为实体对象
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //判断套餐名称是否已经存在
        if (setmealMapper.countByName(setmeal.getName()) > 0) {
            //若套餐已经存在，则抛出自定义异常
            throw new SetmealAlreadyExistsException(MessageConstant.SETMEAL_ALREADY_EXISTS);
        } else {
            //插入套餐数据
            setmealMapper.insert(setmeal);
        }

        //获取生成的套餐id，因为插入套餐-菜品关系表的数据时，需要该字段
        Long setmealId = setmeal.getId();
        //构造要插入套餐菜品关系的数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            //设置套餐id
            setmealDish.setSetmealId(setmealId);
        });

        //保存套餐和菜品的关联关系
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        //根据前端所要的数据，这里需要设计对应的VO类，然后再把该VO类的数据返回给前端
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        //然后把分页查询的结果进行封装为PageResult，返回给前端
        return new PageResult(page.getTotal(), page.getResult());
    }
}
