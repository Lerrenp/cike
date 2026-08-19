package com.cike.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 分页结果 VO
 */
@Data
@AllArgsConstructor
public class PageVO<T> {

    private List<T> records;
    private long total;
    private long page;
    private long size;
}
