package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据。
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在时，抛出自定义的账号不存在异常
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //对前端传过来的密码先进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误时，抛出自定义的密码错误异常
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定时，抛出自定义的账号被锁定异常
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * 这里因为是保存到数据库中，所以尽量把DTO转回对应表的实体类
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        //验证是否获取到了当前线程id
        //System.out.println("当前线程id" + Thread.currentThread().getId());

        //创建表对应实体类的对象
        Employee employee = new Employee();
        //使用对象拷贝方法，把DTO对象拷贝到实体类对象中
        BeanUtils.copyProperties(employeeDTO, employee);
        //但实体类中有些属性是DTO对象没有的，所以要寻找没有的属性，并手动赋值
        //设置账号状态，默认正常状态，1：正常，0：锁定
        //但直接设置1属于硬编码，不利于后期维护，这里使用常量类的属性
        employee.setStatus(StatusConstant.ENABLE);

        //设置默认密码，进行md5加密后存到数据库
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //设置创建时间、更新时间、创建人id、更新人id
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //使用封装后的ThreadLocal获取当前登录用户的id
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        //使用持久层mapper的insert方法，把实体类对象插入到数据库中
        employeeMapper.insert(employee);
    }

}
