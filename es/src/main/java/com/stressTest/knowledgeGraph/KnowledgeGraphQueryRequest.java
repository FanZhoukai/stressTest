package com.stressTest.knowledgeGraph;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author fanzhoukai
 * @des: 知识图谱推题request
 */
@Data
@NoArgsConstructor
public class KnowledgeGraphQueryRequest {
    /**
     * 基础图谱ID list
     */
    private List<String> tagIdList;
    /**
     * 资源子类型list
     */
    private List<String> resourceSubtypeList;
    /**
     * 分页参数
     */
    private Integer pageNum = 1;
    private Integer pageSize = 100;

    public Integer getPageNum() {
        //pageNum从1开始
        if (pageNum == null || pageNum < 1) {
            return 1;
        }
        return pageNum;
    }

    public Integer getPageSize() {
        //pageNum上限1000
        if (pageSize == null || pageSize < 1) {
            return 100;
        }
        if (pageSize > 1000) {
            return 1000;
        }
        return pageSize;
    }
}