package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author yixin
 * @date 2025/5/30
 * @description 菜品管理相关接口
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * 因为该接口返回数据不需要data，所以不需要写泛型
     *
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        //这里除了新增菜品，还可能新增菜品口味
        dishService.saveWithFlavor(dishDTO);
        //新增菜品后，需要删除当前菜品所属分类的缓存数据
        log.info("清理缓存：{}", dishDTO.getCategoryId());
        cleanCache("dish_" + dishDTO.getCategoryId());
        return Result.success();
    }

    /**
     * 菜品分页查询
     * url拼接的参数，不需要添加注解
     * 分页查询统一返回PageResult泛型
     *
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * 接收参数可以直接使用String ids来接收，但这样需要自己处理。
     * 使用List来接收参数，加上@RequestParam注解，springboot会自动将前端传来的ids参数，自动封装到List<Long> ids中。
     *
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品：{}", ids);
        dishService.deleteBatch(ids);
        //批量删除影响的菜品可能是多个分类，如果要精确删除，要需要去查涉及哪些分类，这里可以直接删除所有菜品缓存
        log.info("清理缓存：{}", "dish_*");
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * 因为该接口返回数据需要data，所以需要写泛型，并且data中有个其他表字段，因此用使用新的VO类
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavors(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品信息和口味信息
     * 与新增菜品基本类似，直接参考
     *
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        //这里除了修改菜品，还可能修改菜品口味
        dishService.updateWithFlavors(dishDTO);
        //修改操作可能涉及1份菜品缓存或者2份，因此直接删除所有菜品缓存
        log.info("清理缓存：{}", "dish_*");
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 启用、禁用菜品
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用、禁用菜品")
    public Result setStatus(@PathVariable Integer status, Long id) {
        log.info("设置菜品状态：{}", status);
        dishService.startOrStop(status, id);
        //如果要精确删除分类缓存的菜品，还需要去查这个菜品属于哪个分类，因此直接删除所有菜品缓存
        log.info("清理缓存：{}", "dish_*");
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据菜品分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据菜品分类id查询菜品")
    public Result<List<Dish>> getByCategoryId(Long categoryId) {
        log.info("根据菜品分类id查询菜品：{}", categoryId);
        List<Dish> dishList = dishService.getByCategoryId(categoryId);
        return Result.success(dishList);
    }

    /**
     * 清理缓存中的菜品数据
     * 只在该类中使用，直接设置为私有方法
     *
     * @param pattern
     */
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
