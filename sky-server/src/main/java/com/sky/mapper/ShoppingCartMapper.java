package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author yixin
 * @date 2025/6/3
 * @description
 */
@Mapper
public interface ShoppingCartMapper {

    /**
     * 动态条件查询
     * 如根据菜品id或者套餐id查询相对应的数据
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据id修改商品数量
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            " values (#{name},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{image},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户id清空用户的购物车
     * @param currentId
     */
    @Delete("delete from shopping_cart where user_id = #{currentId}")
    void delete(Long currentId);

    /**
     * 根据购车数据id删除购物车数据
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);
}
