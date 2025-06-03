package com.sky.service;

import com.sky.dto.ShoppingCartDTO;

/**
 * @author yixin
 * @date 2025/6/3
 * @description
 */
public interface ShoppingCartService {

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
