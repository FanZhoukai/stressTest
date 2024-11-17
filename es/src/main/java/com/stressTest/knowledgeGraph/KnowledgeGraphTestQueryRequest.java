package com.stressTest.knowledgeGraph;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仅用于接口测试
 *
 * @author fanzhoukai
 * @des: 知识图谱推题request
 */
@Data
@NoArgsConstructor
public class KnowledgeGraphTestQueryRequest extends KnowledgeGraphQueryRequest {

    private String id;
    private String resourceId;
    private String resourceType;
    private boolean sort = true;
}