package com.stressTest.knowledgeGraph;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author fanzhoukai
 * @des: 知识图谱查询结果response
 */
@Data
@EqualsAndHashCode
public class KnowledgeGraphMarkResultResponse implements Serializable {


    /**
     * 资源ID（试题(主题)ID、绘本ID）
     */
    private String resourceId;

    /**
     * 图谱标签list
     */
    private List<String> tagIdList;

    /**
     * 资源类型（试题、绘本）
     */
    private String resourceType;

    /**
     * 资源子类型（试题各题型、绘本）
     */
    private String resourceSubtype;

    /**
     * 学段
     */
    private List<String> sectionTagIds;
    /**
     * 年级
     */
    private List<String> gradeTagIds;

}
