package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 统计时间区间内的营业额
     * 营业额实际上查询的是订单表中的金额字段，所以要查询的订单状态为已完成，且在指定时间范围内
     *
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
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
     *
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
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
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 提取出私有方法，提高复用性，根据时间区间统计用户数量
     *
     * @param beginTime
     * @param endTime
     * @return
     */
    private Integer getUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        Map map = new HashMap();
        map.put("begin", beginTime);
        map.put("end", endTime);
        return userMapper.countByMap(map);
    }

    /**
     * 统计订单数量，完成订单数量和订单率
     *
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //1.计算日期列表dateList，将时间区间内的每一天加进去
        List<LocalDate> dateList = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date);
        }
        //2.计算订单总数量，有效订单数量，订单完成率
        //创建每天订单总数集合和每天有效订单数集合
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //LocalDate->LocalDateTime，获取一天中的起始时间和结束时间，也就是0点0分0秒和23点59分59秒
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //查询每天的总订单数 select count(id) from orders where order_time > ? and order_time < ?
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            //查询每天的有效订单数 select count(id) from orders where order_time > ? and order_time < ? and status = ?
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }
        //时间区间内的总订单数，不用再次查数据库，遍历orderCountList集合，使用stream流的reduce合并求和即可
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //时间区间内的总有效订单数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        //订单完成率
        Double orderCompletionRate = 0.0;
        //如果总订单数不为0，则计算订单完成率
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();

    }

    /**
     * 提取出私有方法，提高复用性，根据时间区间统计订单数量
     *
     * @param begin
     * @param end
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }

    /**
     * 统计销量top10
     * 查询两张表orderDetail(查名字和份数)和order表(用于查状态)
     * select od.name,sum(od.number) number from order_detail od ,orders o
     * where od.order_id = o.id and o.order_time > ? and o.order_time < ? and o.status = 5 group by od.name order by sum(od.number) desc limit 0,10
     *
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        //LocalDate转LocalDateTime，并构造一天内的时间
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        //sql语句查出来只有name和number，需要创建一个实体类接收，这里使用GoodsSalesDTO接收
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime, endTime);
        //由出参VO类需要的数据可知，使用stream流，将goodsSalesDTOList集合中的name和number分别拼接成字符串，并使用StringUtils.join方法拼接成字符串
        List<String> names = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");
        List<Integer> numbers = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");
        //创建一个VO类，将名称列表和销售的份量列表的数据封装到VO类中
        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出近30天的运营数据报表
     * 数据包括：营业额、订单完成率、新增用户数、有效订单和平均客单价
     * @param response
     **/
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        //1.查询最近30天的概览运营数据
        //因为在workspaceService中，已经封装了根据时间区间查询的概览运营数据，所以直接调用即可
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        //创建输入流源地址Excel模板文件对象，getClass获取类对象，getClassLoader获取类加载器，getResourceAsStream从类路径下读取资源，最后这个方法会返回一个输入流
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        //2.使用POI提供的工具类，将Excel表格数据填充到Excel表格中
        try {
            //基于提供好的模板文件创建一个新的Excel表格对象
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //获得Excel文件中的一个Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //填充数据--时间，获取第2行第2个单元格，并设置值，因为模板文件行已经存在
            sheet.getRow(1).getCell(1).setCellValue(begin + "至" + end);
            //获得第4行
            XSSFRow row = sheet.getRow(3);
            //获取单元格
            row.getCell(2).setCellValue(businessData.getTurnover());    //营业额
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate()); //订单完成率
            row.getCell(6).setCellValue(businessData.getNewUsers());    //新增用户数
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount()); //有效订单数
            row.getCell(4).setCellValue(businessData.getUnitPrice());   //平均客单价
            //填充数据--明细
            for (int i = 0; i < 30; i++) {
                //计算要填充的日期
                LocalDate date = begin.plusDays(i);
                //查询date那一天的明细数据
                businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                //从模板文件可知，从第8行开始填充数据
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }
            //3.通过输出流将文件下载到客户端浏览器中
            //使用HttpServletResponse的getOutputStream方法获取输出流
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            //4.关闭资源
            out.flush();
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
