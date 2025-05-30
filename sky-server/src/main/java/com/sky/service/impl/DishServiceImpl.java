package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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
    @Autowired
    private SetmealDishMapper setmealDishMapper;

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

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        //分页查询返回的结果类是helper下的Page，泛型为了适应接口用的是DishVO，
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     * 因为删除菜品涉及的业务规则比较多，所以先分析其中的规则，再写代码
     * 涉及多个表操作，别忘了事务注解
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断菜品是否在售
        //先把id取出来，然后遍历，判断菜品是否在售，如果菜品在售，则不能删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                //如果菜品在售，则不能删除，抛出自定义异常
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //因为菜品和套餐是多对多的关系，所以要判断菜品是否关联了套餐，它们中间有个中间表，所以要查询中间表，判断菜品是否关联了套餐
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            //如果菜品关联了套餐，则不能删除，抛出自定义异常
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
//        //删除菜品表中的菜品数据，注意优化，如果删除一个菜品是一个sql语句，则sql数量多了可能会影响性能
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
//            //删除菜品关联的口味数据，不用查询是否存在，直接删
//            dishFlavorMapper.deleteByDishId(id);
//        }
        //优化后，根据菜品id集合，批量删除菜品表中的菜品数据
        //sql: delete from dish where id in (?,?,?)
        dishMapper.deleteBatch(ids);

        //优化后，根据菜品id集合，批量删除菜品关联的口味数据
        //sql: delete from dish_flavor where dish_id in (?,?,?)
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavors(Long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        //根据dishId查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        //将查询的数据封装到DishVO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 根据id修改菜品信息和口味信息
     * 涉及菜品表和菜品口味表
     * @param dishDTO
     */
    public void updateWithFlavors(DishDTO dishDTO) {
        //修改菜品表基本信息，DishDTO-->Dish
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        //口味因为可能新增，可能删除，因此直接删除原本的，插入用户新提交的
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //重新插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            //为dishFlavor设置dishId
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            //向口味表插入n条数据，可以批量插入，不一定是一条一条插入
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
