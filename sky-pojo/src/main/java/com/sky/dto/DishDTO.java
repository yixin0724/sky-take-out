package com.sky.dto;

import com.sky.entity.DishFlavor;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDTO implements Serializable {

    private Long id;
    //菜品名称
    private String name;
    //菜品分类id
    private Long categoryId;
    //菜品价格
    private BigDecimal price;
    //图片
    private String image;
    //描述信息
    private String description;
    //0 停售 1 起售
    private Integer status;
    //口味，因为菜品口味flavor里有很多菜品口味，所以用list集合保存，这里面属性又是DishFlavor实体类的属性，所以用DishFlavor接收
    private List<DishFlavor> flavors = new ArrayList<>();

}
