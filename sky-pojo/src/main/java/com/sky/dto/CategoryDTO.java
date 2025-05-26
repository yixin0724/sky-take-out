package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前端提交的数据和实体类中对应的属性差别比较大时，建议使用DTO来封装数据
 */
@Data
public class CategoryDTO implements Serializable {

    //主键
    private Long id;

    //类型 1 菜品分类 2 套餐分类
    private Integer type;

    //分类名称
    private String name;

    //排序
    private Integer sort;

}
