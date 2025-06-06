package com.sky.service;

import com.sky.vo.TurnoverReportVO;

import java.time.LocalDate;

/**
 * @author yixin
 * @date 2025/6/6
 * @description
 */
public interface ReportService {

    /**
     * 统计时间区间内的营业额
     * @param beginTime
     * @param endTime
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate beginTime, LocalDate endTime);
}
