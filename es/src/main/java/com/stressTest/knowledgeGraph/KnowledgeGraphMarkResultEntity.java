package com.stressTest.knowledgeGraph;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 知识图谱资源打标entity，用于推题
 */
@Getter
@Setter
public class KnowledgeGraphMarkResultEntity implements Serializable {


    /**
     * 资源ID（试题(主题)ID、绘本ID）
     */
    private String resourceId;

    /**
     * 资源类型（试题、绘本）
     */
    private String resourceType;

    /**
     * 资源子类型（试题各题型、绘本）
     */
    private String resourceSubtype;

    /**
     * 课节所属的课程docId
     */
    private String courseDocId;

    /**
     * 图谱标签list
     */
    private List<String> tagIdList;

    /**
     * 学段
     */
    private List<String> sectionTagIds;
    /**
     * 年级
     */
    private List<String> gradeTagIds;

    /**
     * 时间戳（与MySQL同步，用于做增量）
     */
    private long createTime;
    private long updateTime;

    /**
     * ES数据本身的时间戳，用于排查问题
     */
    private long createAtTime;
    private long updateAtTime;

    // MySQL主键，用于输出时确保排序稳定
    private Integer sort;
}
