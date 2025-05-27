package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询参数对应的DTO类
 */
@Data
public class EmployeePageQueryDTO implements Serializable {

    //员工姓名
    private String name;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;

}
