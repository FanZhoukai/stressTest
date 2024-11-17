package com.stressTest.util;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 说明:
 * <br>@author ZhangBo
 * <br>@date 23:25 2020/6/22
 *
 * <br>UpdateNote:
 * <br>UpdateTime:
 * <br>UpdateUser:
 */
public class PageResult<T> implements Serializable {

    /** 集合数据 **/

    private List<T> pageData;

    /** 总数量 **/

    private BigInteger totalSize;

    public List<T> getPageData() {
        return pageData;
    }

    public void setPageData(List<T> pageData) {
        this.pageData = pageData;
    }

    public BigInteger getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(BigInteger totalSize) {
        this.totalSize = totalSize;
    }


    public PageResult() {
        this.pageData = new ArrayList<>();
        this.totalSize = BigInteger.ZERO;
    }

    public PageResult(List<T> pageData, BigInteger totalSize) {
        this.pageData = pageData;
        this.totalSize = totalSize;
    }
}
