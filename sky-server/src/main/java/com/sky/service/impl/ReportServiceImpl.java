package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
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


}
