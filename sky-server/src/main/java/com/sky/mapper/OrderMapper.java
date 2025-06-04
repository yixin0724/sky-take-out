package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yixin
 * @date 2025/6/4
 * @description
 */
@Mapper
public interface OrderMapper {
    /**
     * 插入一条订单数据
     * @param orders
     */
    void insert(Orders orders);
}
