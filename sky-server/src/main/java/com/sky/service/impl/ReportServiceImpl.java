package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yixin
 * @date 2025/6/6
 * @description
 */
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 统计时间区间内的营业额
     * 营业额实际上查询的是订单表中的金额字段，所以要查询的订单状态为已完成，且在指定时间范围内
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end){
        //1.先计算日期列表dateList，将时间区间内的每一天加进去
        List<LocalDate> dateList = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date);
        }
        //2.再计算金额列表turnoverList
        //VO类中是LocalDateTime，这种带有时分秒，而LocalDate只有年月日
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //需要查询date日期对应的营业额
            //获取一天中的起始时间和结束时间，也就是0点0分0秒和23点59分59秒
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //select sum(amount) from orders where order_time >= beginTime and order_time < endTime and status = 5(已完成)
            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            //如果turnover为null，说明该日期没有订单，设置为0.0
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        //3.将VO需要的两个属性数据封装为VO类
        TurnoverReportVO reportVO = TurnoverReportVO.builder()
                //使用commons-lang3中的StringUtils工具类进行拼接
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
        return reportVO;
    }

    /**
     * 根据时间区间统计用户数量
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end){
        //1.先计算日期列表dateList，将时间区间内的每一天加进去
        List<LocalDate> dateList = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date);
        }
        //2.再计算新增用户列表newUserList，以及总用户列表totalUserList
        List<Integer> newUserList = new ArrayList<>(); //新增用户数
        List<Integer> totalUserList = new ArrayList<>(); //总用户数
        //获取指定时间区间内的用户数量
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //用一个动态sql兼容这两个sql即可
            //新增用户数量 select count(id) from user where create_time > ? and create_time < ?
            Integer newUser = getUserCount(beginTime, endTime);
            //总用户数量 select count(id) from user where  create_time < ?
            Integer totalUser = getUserCount(null, endTime);
            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }
        //3.将VO需要的两个属性数据封装为VO类
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }

    /**
     * 私有方法，将共有的内容提取为方法，根据时间区间统计用户数量
     * @param beginTime
     * @param endTime
     * @return
     */
    private Integer getUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        Map map = new HashMap();
        map.put("begin",beginTime);
        map.put("end", endTime);
        return userMapper.countByMap(map);
    }
}
