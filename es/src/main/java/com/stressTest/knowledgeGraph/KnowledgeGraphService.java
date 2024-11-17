package com.stressTest.knowledgeGraph;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.google.common.collect.Lists;
import com.stressTest.util.EsClient;
import com.stressTest.util.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class KnowledgeGraphService {

    ElasticsearchClient client = EsClient.getClint();

    private final String index_name = "knowledge_graph_recommend_index";


    public PageResult<KnowledgeGraphMarkResultResponse> queryTerms(KnowledgeGraphTestQueryRequest request) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(index_name)
                .query(q -> q
                        .bool(bool -> {
                            // 资源子类型
                            if (!CollectionUtils.isEmpty(request.getResourceSubtypeList())) {
                                List<FieldValue> resourceSubtypeFieldValue = request.getResourceSubtypeList().stream().map(FieldValue::of).collect(Collectors.toList());
                                bool.filter(a -> a
                                        .terms(b -> b
                                                .field("resourceSubtype")
                                                .terms(c -> c.value(resourceSubtypeFieldValue))
                                        ));
                            }
                            // 打标记录
                            if (!CollectionUtils.isEmpty(request.getTagIdList())) {
                                List<FieldValue> tagIdListFieldValue = request.getTagIdList().stream().map(FieldValue::of).collect(Collectors.toList());
                                bool.filter(a -> a
                                        .terms(b -> b
                                                .field("tagIdList")
                                                .terms(c -> c.value(tagIdListFieldValue))
                                        ));
                            }
                            return bool;
                        })
                );
        // 排序字段
        if (request.isSort()) {
            builder.sort(
                    SortOptions.of(e ->
                            e.field(FieldSort.of(a ->
                                    a.field("sort").order(SortOrder.Asc)))));
        }
        // 分页参数
        builder
                .from((request.getPageNum() - 1) * request.getPageSize())
                .size(request.getPageSize());

        PageResult<KnowledgeGraphMarkResultResponse> pageResult = new PageResult<>();

        // ES默认max_result_window=10000
        if (request.getPageNum() * request.getPageSize() >= 10000) {
            return countEmptyPage(client, request);
        }

        SearchResponse<KnowledgeGraphMarkResultEntity> esResponse;
        try {
            esResponse = client.search(builder.build(), KnowledgeGraphMarkResultEntity.class);
        } catch (IOException e) {
            log.error("KnowledgeGraphMarkResultDao#queryPage error.", e);
            return pageResult;
        }
        if (!CollectionUtils.isEmpty(esResponse.hits().hits())) {
            for (Hit<KnowledgeGraphMarkResultEntity> hit : esResponse.hits().hits()) {
                if (hit.source() == null) {
                    continue;
                }
                KnowledgeGraphMarkResultResponse e;
                e = new KnowledgeGraphMarkResultResponse();
                e.setResourceId(hit.source().getResourceId());
                e.setTagIdList(hit.source().getTagIdList());
                e.setResourceType(hit.source().getResourceType());
                e.setResourceSubtype(hit.source().getResourceSubtype());
                e.setSectionTagIds(hit.source().getSectionTagIds());
                e.setGradeTagIds(hit.source().getGradeTagIds());
                pageResult.getPageData().add(e);
            }
        }
        pageResult.setTotalSize(new BigInteger(String.valueOf(esResponse.hits().total().value())));
        return pageResult;
    }


    // 返回带有count值的空页
    private PageResult<KnowledgeGraphMarkResultResponse> countEmptyPage(ElasticsearchClient client, KnowledgeGraphQueryRequest request) {
        PageResult<KnowledgeGraphMarkResultResponse> result = new PageResult<>();
        CountResponse esCountResponse;
        try {
            CountRequest.Builder countBuilder = new CountRequest.Builder();
            countBuilder.query(q -> q
                    .bool(bool -> {
                                // 资源子类型（必传）
                                bool.filter(a -> a
                                        .bool(b -> {
                                            for (String subType : request.getResourceSubtypeList()) {
                                                b.should(c -> c
                                                        .term(d -> d
                                                                .field("resourceSubtype")
                                                                .value(FieldValue.of(subType))
                                                        )
                                                );
                                            }
                                            b.minimumShouldMatch("1");
                                            return b;
                                        })
                                );
                                // 打标记录（必传）
                                bool.filter(a -> a
                                        .bool(b -> {
                                            for (String tagId : request.getTagIdList()) {
                                                b.should(c -> c
                                                        .term(d -> d
                                                                .field("tagIdList")
                                                                .value(FieldValue.of(tagId))
                                                        )
                                                );
                                            }
                                            b.minimumShouldMatch("1");
                                            return b;
                                        })
                                );
                                return bool;
                            }
                    ));

            esCountResponse = client.count(countBuilder.build());
        } catch (IOException e) {
            log.error("KnowledgeGraphMarkResultDao#queryPage count error.", e);
            return result;
        }
        result.setTotalSize(new BigInteger(String.valueOf(esCountResponse.count())));
        result.setPageData(Lists.newArrayList());
        return result;
    }

    public void batchInsert(KnowledgeGraphInsertRequest request) {
        List<String> resourceTypes = Lists.newArrayList("question", "unit", "word", "wordQuestion");
        List<String> tagIds = Lists.newArrayList("ZTYJ25_0_0_0_5", "HTSXD7_0_0", "YPLX2_0_0_4_0", "YPLX2_0_2_3", "YPLX2_0_3_1", "YPLX2_0_1_0", "YPSL18_0_0", "YPGN16_0_1", "YFZS20_0_0_2_0_4_0_1_5", "YFZS20_1_0_1", "TLWJN12_0_0", "KWJN0_0_0_1_2", "SWPZblm10_0_0_0", "SWPZkbbb11_0_1_1", "SWPZkbbb11_0_0_1", "SWPZkbbb11_0_1_0", "SWPZkbbb11_0_0_0", "RWLX9_0_0", "ZTYJ25_0_2_0_3", "YPLX2_0_2_8", "YPLX2_0_2_0", "YPGN16_0_13", "YuYongZS3_0_0_6", "YuYongZS3_1_0_2", "KYWJN6_0_3_1", "SWPZblm10_0_1_0", "RWLX9_0_1", "JHX8_0_1", "ZTYJ25_0_1_0_17", "YuYongZS3_0_0_7", "YFZS20_0_0_2_0_4_4_1_0", "KYWJN6_0_0_3", "ZTYJ25_0_0_0_0", "YPLX2_0_0_2_0_2", "YuYinZS4_0_0_0_0_1_0_6", "YuYinZS4_0_0_0_0_1_10_0", "YuYinZS4_0_0_0_0_1_19_0", "YuYinZS4_0_0_1_0_1_4", "YuYinZS4_1_0_0_0", "CHZS1_1_0_0_0", "XXCL17_0_1_12", "CHZS1_0_0_0_0", "CHZS1_0_0_1_0_3_0", "CHZS1_0_0_1_0_3_1", "CHZS1_0_0_2_0_0", "CHZS1_0_0_3_0_1", "CHZS1_0_0_4_0_2_0", "CHZS1_0_0_5_0_0_0", "CHZS1_0_0_5_0_3_0", "CHZS1_0_0_5_0_1_0", "CHZS1_0_0_5_0_2_0", "CHZS1_0_0_7_2672", "ZTYJ25_0_0_0_3", "HTSXD7_0_1", "YPLX2_0_0_7", "YPGN16_0_2", "YFZS20_0_0_2_0_4_7_1_0", "YPZS22_0_0_12", "YPZS22_1_0_2", "YPZS22_0_1_0_2", "KYWJN6_0_0_5", "ZTYJ25_0_0_0_10", "YPLX2_0_0_4_1", "YFZS20_0_0_2_0_8_3", "YFZS20_0_0_2_0_9_4_2_1", "YFZS20_0_0_2_0_4_0_1_6", "ZTYJ25_0_0_0_1", "YPLX2_0_0_2_1", "YuYinZS4_0_0_2_0_0", "CHZS1_0_0_3_0_0", "CHZS1_0_0_4_0_3_0_3", "CHZS1_0_0_5_0_0_1", "CHZS1_0_0_5_0_3_5", "CHZS1_0_0_5_0_1_1", "CHZS1_0_0_5_0_2_1", "CHZS1_0_0_7_3099", "YFZS20_0_0_3_1_76", "KWJN0_0_0_1_4", "YPLX2_0_0_2_0_6", "YuYinZS4_0_0_0_0_1_11_0", "YuYinZS4_0_0_0_0_3_3_0", "YuYinZS4_0_0_0_0_3_8_0", "YuYinZS4_0_0_0_0_1_5_0", "YuYinZS4_0_0_0_0_1_4_1", "CHZS1_0_0_3_0_5", "CHZS1_0_0_3_0_2", "CHZS1_0_0_3_0_4", "CHZS1_0_0_4_0_3_0_5", "CHZS1_0_0_5_0_3_8", "CHZS1_0_0_5_0_2_3", "CHZS1_0_0_7_3075", "ZTYJ25_0_0_0_2", "YFZS20_0_0_2_0_4_0_1_0", "ZTYJ25_0_0_0_6", "YPLX2_0_0_6_0", "YPLX2_0_2_2", "YPLX2_0_0_14_9", "YPLX2_0_1_1", "YuYinZS4_0_0_2_0_1", "YuYinZS4_0_0_5_0_0_0_1", "YuYinZS4_1_0_2", "YFZS20_0_0_2_0_0_1_1_0", "YFZS20_0_0_3_13_195", "TLWJN12_0_1_1_1", "TLWJN12_0_1_2_0", "TLWJN12_0_1_0_4", "KYWJN6_0_1_13", "KYWJN6_0_4_4", "SWPZblm10_0_0_1", "SWPZblm10_0_2", "SWPZkbbb11_0_2_1", "YinPinTZfy19_0_0", "YinPinTZys21_0_0_2", "JHX8_0_0", "WBNDyds13_0_0_0", "ZDXS23_0_5", "CHZS1_0_0_4_0_3_9_0", "CHZS1_0_0_7_15234", "YuYongZS3_0_0_8", "YuYinZS4_0_0_0_0_1_12_0", "YuYinZS4_0_0_0_0_1_13_0", "YuYinZS4_0_0_0_0_1_8_1", "CHZS1_0_0_4_0_3_2_0", "CHZS1_0_0_5_0_3_2", "CHZS1_0_0_7_1702", "ZTYJ25_0_0_0_9", "YinPinTZys21_0_0_0", "ZTYJ25_0_1_0_6", "YPLX2_0_2_1", "KYWJN6_0_4_2", "XXCL17_0_1_6", "ZTYJ25_0_0_0_14", "CHZS1_1_0_0_1", "KYWJN6_0_0_2", "XXCL17_0_1_8", "XXCL17_0_1_11", "CHZS1_0_0_4_0_3_1_2", "CHZS1_0_0_4_0_3_13_1", "CHZS1_0_0_5_0_3_4", "CHZS1_0_0_5_0_2_2", "CHZS1_0_0_7_846", "ZTYJ25_0_2_0_14", "ZTYJ25_0_0_0_23", "YPLX2_0_0_5", "YuYinZS4_0_0_4_0_1_0_2", "YuYinZS4_0_0_4_0_0_2", "YuYinZS4_0_0_5_0_0_0_0", "YuYinZS4_0_0_6_0_1", "YuYinZS4_0_0_6_0_0", "YuYinZS4_1_0_2_0", "YFZS20_0_0_2_0_0_0", "YFZS20_0_0_2_0_4_0", "YFZS20_0_0_3_1_100", "YFZS20_0_0_3_15_5", "YFZS20_0_0_3_1_88", "WBNDyds13_0_0_3", "YPLX2_0_0_6", "CHZS1_1_0_1", "YFZS20_0_0_2_0_4_0_0_2", "YFZS20_0_0_3_13_194", "YuYongZS3_0_0_2", "YuYongZS3_0_2_0_0", "YuYongZS3_0_4_0_1_0", "KYWJN6_0_1_1", "ZTYJ25_0_0_0_13", "YPGN16_0_6", "YFZS20_0_0_0_0_1_0_0", "YFZS20_0_0_2_0_0_0_0", "WBNDyds13_0_1_0", "YFZS20_0_0_2_0_0_1_1_8", "YFZS20_0_0_3_13_273", "YuYongZS3_0_4_0_0_0", "KWJN0_0_0_1_5", "YFZS20_0_0_2_0_4_1_0_2", "WBNDyds13_0_0_1", "ZTYJ25_0_0_0_16", "YPGN16_0_8", "YuYinZS4_0_0_1_0_0_1", "YuYinZS4_0_0_5_0_3_0_3", "YFZS20_0_0_2_0_0_3_2", "YFZS20_0_0_3_8_1", "YFZS20_1_0_0_0", "YuYongZS3_0_0_3", "YuYongZS3_0_1_0_2_0", "YuYongZS3_0_3_0_8", "YuYongZS3_0_4_0_2_0", "YuYongZS3_0_4_0_0_1", "YuYongZS3_1_0_1", "CHZS1_0_0_5_0_3_1", "CHZS1_0_0_7_1614", "CHZS1_0_0_4_0_3_2_1", "CHZS1_0_0_4_0_3_2_6", "CHZS1_0_0_5_0_3_6", "CHZS1_0_0_7_1176", "ZTYJ25_0_1_0_3", "YFZS20_0_0_2_0_0_1_0_3", "TLWJN12_0_1_0_0", "YFZS20_0_0_0_0_1_0_1", "YFZS20_0_0_3_1_102", "TLWJN12_0_1_0_2", "YFZS20_0_0_0_0_6_2", "YFZS20_0_0_2_0_0_1_0_1", "TLWJN12_0_1_0_6", "YFZS20_0_0_0_0_1_0_3", "YFZS20_0_0_2_0_4_5_1_0", "YuYinZS4_0_0_0_0_2_22_0", "YuYinZS4_0_0_0_0_4_15", "CHZS1_0_0_7_3064", "KYWJN6_0_4_3", "KWJN0_0_0_1_3", "XXCL17_0_1_0", "YuYinZS4_0_0_0_0_3_3_1", "YuYinZS4_0_0_0_0_1_18_1", "CHZS1_0_0_4_0_1_12", "CHZS1_0_0_5_0_3_12", "CHZS1_0_0_5_0_1_2", "CHZS1_0_0_7_2721", "ZTYJ25_0_0_0_17", "ZTYJ25_0_0_0_22", "CHZS1_0_0_4_0_3_2_3", "CHZS1_0_0_7_2473", "YPLX2_0_0_4", "YFZS20_0_0_2_0_4_0_0_0", "WBNDyds13_0_0_4", "ZTYJ25_0_0_0_4", "YuYinZS4_0_0_4_0_0_0", "YFZS20_0_0_2_0_4_1_0_0", "YFZS20_0_0_2_0_8_2", "YFZS20_0_0_2_0_3_1", "YuYinZS4_0_0_5_0_1_0_0", "YFZS20_0_0_2_0_0_1_0", "YFZS20_0_0_3_13_28", "YPLX2_0_0_2_2", "CHZS1_0_0_3_0_3", "CHZS1_0_0_4_0_3_1_4", "CHZS1_0_0_7_1533", "CHZS1_0_0_4_0_3_0_1", "CHZS1_0_0_5_0_3_3", "CHZS1_0_0_7_3016", "YFZS20_0_0_2_0_0_1_1_7", "YFZS20_0_0_3_13_181", "WBNDyds13_0_1_4", "YFZS20_0_0_3_13_180", "ZTYJ25_0_0_0_20", "YPLX2_0_3_0", "YPZS22_0_0_0", "YPLX2_0_0_2", "CHZS1_0_0_4_0_3_2_5", "CHZS1_0_0_4_0_3_17_2", "CHZS1_0_0_7_922", "ZTYJ25_0_1_0_25", "WBNDyds13_0_1_2", "CHZS1_0_0_4_0_3_1_3", "CHZS1_0_0_7_3060", "YPLX2_0_0_2_0", "YuYinZS4_0_0_0_0_3_1_0", "YuYinZS4_0_0_0_0_1_20_1", "CHZS1_0_0_7_1758", "YuYinZS4_0_0_0_0_1_15_0", "YuYinZS4_0_0_0_0_2_4_0", "CHZS1_0_0_7_483", "YuYinZS4_0_0_0_0_2_25_0", "YuYinZS4_0_0_0_0_1_2_0", "YuYinZS4_0_0_0_0_2_20_0", "CHZS1_0_0_4_0_3_2_2", "CHZS1_0_0_4_0_3_11_1", "CHZS1_0_0_7_438", "YuYinZS4_0_0_0_0_0_15", "YuYinZS4_0_0_0_0_0_12", "YuYinZS4_0_0_0_0_1_0_1", "CHZS1_0_0_7_105", "YFZS20_0_0_2_0_0_1_0_2", "CHZS1_1_0_2_0", "CHZS1_0_0_4_0_3_0_0", "CHZS1_0_0_7_2756", "TLWJN12_0_1_0_3", "YFZS20_0_0_2_0_0_1_1_5", "YFZS20_0_0_2_0_0_1_1_1", "XXCL17_0_1_2", "YPGN16_0_10", "YFZS20_0_0_3_10_6", "ZTYJ25_0_1_0_7", "KYWJN6_0_4_0", "KWJN0_0_0_1_0", "YFZS20_0_0_3_1_42", "YFZS20_0_0_2_0_4_0_1_1", "YFZS20_0_0_2_0_0_1_1_6", "ZTYJ25_0_1_0_23", "WHZS5_0_0_25", "WHZS5_0_1_0_1_4", "WHZS5_1_0_0", "YuYongZS3_0_0_1", "ZTYJ25_0_0_0_7", "TLWJN12_0_1_0_1", "KWJN0_0_0_1_1", "YFZS20_0_0_2_0_0_1_1", "YFZS20_0_0_3_1_75", "WBNDyds13_0_0_2", "YFZS20_0_0_2_0_4_1_1_0", "WBNDyds13_0_1_3", "ZTYJ25_0_1_0_26", "YFZS20_0_0_0_0_0_3_0", "YPZS22_0_0_22", "YPZS22_0_1_0_1", "WHZS5_0_0_28", "WHZS5_0_1_0_1_10", "ZTYJ25_0_0_0_21", "YFZS20_0_0_2_0_0_1_0_0", "KYWJN6_0_3_0", "YinPinTZys21_0_0_1", "YFZS20_0_0_3_13_28_0", "YPGN16_0_4", "YFZS20_0_0_2_0_9_0_0_0", "WBNDyds13_0_1_1", "ZTYJ25_0_1_0_11", "YPGN16_0_14", "YFZS20_0_0_2_0_0_3_0", "ZTYJ25_0_1_0_29", "YFZS20_0_0_0_0_0_2", "YFZS20_0_0_0_0_2_1_1", "WHZS5_0_0_35", "WHZS5_0_1_1_0_0", "WHZS5_0_1_1_1_0", "WHZS5_1_0_1", "CHZS1_0_0_7_402", "CHZS1_0_0_7_1467", "CHZS1_0_0_7_2592", "YFZS20_0_0_2_0_9_1_1", "YPGN16_0_0", "ZMTP24_0_0_0", "YuYinZS4_0_0_1_0_0_0", "YuYinZS4_0_0_3_0_0", "YuYinZS4_0_0_7_0_0_0", "YuYinZS4_0_0_8_0_0_0", "YFZS20_0_0_1_0_0", "YFZS20_0_0_3_0_0", "YPZS22_1_0_0_0", "YPZS22_0_1_0_0", "YPZS22_0_2_0_0_0", "YPZS22_0_3_0_0_0", "YPZS22_0_4_0_0_0", "YuYongZS3_0_0_0", "YuYongZS3_0_1_0_0_0", "YuYongZS3_0_3_0_0", "YuYongZS3_1_0_0_0", "WHZS5_0_0_0", "WHZS5_0_1_0_0_0", "WHZS5_1_0_0_0", "YDWJN15_0_0_0_0", "KWJN0_0_0_0_0", "XZWJN14_0_0_0", "SWPZblm10_0_0", "CHZS1_0_0_7_2559", "CHZS1_0_0_4_0_3_3_0", "CHZS1_0_0_7_2460", "YuYinZS4_0_0_0_0_3_0_0", "YuYinZS4_0_0_0_0_3_26_0", "YuYinZS4_0_0_1_0_2", "YFZS20_0_0_0_0_0_0_0", "YuYongZS3_0_3_0_2", "CHZS1_0_0_7_2409", "YFZS20_0_0_0_0_0_0_1", "YFZS20_0_0_2_0_0_2_0", "YuYinZS4_0_0_0_0_2_32_0", "CHZS1_0_0_7_2676", "YFZS20_0_0_3_1_1", "ZTYJ25_0_2_0_0", "CHZS1_0_0_4_0_3_7_3", "CHZS1_0_0_4_0_3_12_0", "CHZS1_0_0_7_1640", "YFZS20_0_0_3_13_240", "YFZS20_0_0_3_5_0", "YFZS20_0_0_2_0_3_7_0", "YPGN16_0_16", "YFZS20_0_0_2_0_4_0_1_2", "YFZS20_0_0_2_0_0_2_1", "TLWJN12_0_1_2_1", "YuYinZS4_0_0_0_0_1_14_0", "YuYinZS4_0_0_0_0_1_1_0", "YuYinZS4_0_0_0_0_1_0_2", "CHZS1_0_0_3_0_6", "CHZS1_0_0_4_0_1_9", "CHZS1_0_0_7_7", "YuYinZS4_0_0_0_0_1_18_0", "CHZS1_0_0_7_181", "YFZS20_0_0_3_13_56", "CHZS1_0_0_4_0_3_14_0", "CHZS1_0_0_7_2165", "YPLX2_0_0_2_0_1", "YuYinZS4_0_0_0_0_3_6_0", "YuYinZS4_0_0_0_0_2_31_0", "CHZS1_0_0_7_866", "YuYinZS4_0_0_3_0_1", "YuYinZS4_0_0_4_0_1_0_0", "YFZS20_0_0_3_15_0", "CHZS1_0_0_4_0_3_10_0", "CHZS1_0_0_7_166", "ZDXS23_0_1", "YuYinZS4_0_0_0_0_1_20_0", "YuYinZS4_0_0_0_0_2_24_1", "YFZS20_0_0_3_2", "CHZS1_0_0_4_0_3_4_0", "CHZS1_0_0_4_0_3_7_4", "CHZS1_0_0_4_0_3_17_0", "CHZS1_0_0_7_595", "YuYinZS4_0_0_0_0_1_24_2", "CHZS1_0_0_7_134", "YuYinZS4_0_0_5_0_0", "KWJN0_0_0", "CHZS1_0_0_5_0_0_2", "CHZS1_0_0_5_0_2_4", "poster", "YFZS20_0_0_2_0_8_0", "KYWJN6_0_0_6", "YuYinZS4_0_0_0_0_1_11", "CHZS1_0_0_7_527", "mouth", "YFZS20_0_0_3_1_105", "CHZS1_0_0_7_1029", "CHZS1_0_0_7_1748", "ZTYJ25_0_1_0_19", "YPGN16_0_18", "draw", "leg", "YPLX2_0_0_2_0_7", "YuYinZS4_0_0_0_0_1_21_0", "CHZS1_0_0_7_3044", "YFZS20_0_0_0_0_6_0", "CHZS1_0_0_4_0_3_15_0", "three", "ZTYJ25_0_0_0_8", "YFZS20_0_0_3_1_13", "YFZS20_0_0_3_0_27", "YuYinZS4_0_0_0_0_4_6", "CHZS1_0_0_7_1237", "YPGN16_0_12", "YFZS20_0_0_3_12_11", "CHZS1_0_0_4_0_3_4_3", "CHZS1_0_0_7_2150", "YFZS20_0_0_2_0_0_3_1", "two", "KYWJN6_0_4_5", "parrot", "YFZS20_0_0_3_13_251", "they", "YFZS20_0_0_2_0_4_2", "YPLX2_0_0_4_2", "YFZS20_0_0_2_0_9_1_0", "YPLX2_0_0_2_0_0", "YuYinZS4_0_0_0_0_2_36_0", "CHZS1_0_0_4_0_3_5_1", "CHZS1_0_0_7_466", "SWPZblm10_0_5", "RWLX9_0_2", "YuYinZS4_0_0_0_0_1_0_0", "CHZS1_0_0_4_0_3_1_5", "CHZS1_0_0_4_0_3_2_4", "CHZS1_0_0_7_1541", "CHZS1_0_0_7_1054", "YPZS22_0_1_0_13", "KWJN0_0_0_1_6", "YuYinZS4_0_0_0_0_4_14", "CHZS1_0_0_7_2534", "YuYinZS4_0_0_0_0_2_11_0", "CHZS1_0_0_4_0_3_12_1", "CHZS1_0_0_7_327", "YuYinZS4_0_0_0_0_1_17_0", "YuYinZS4_0_0_0_0_2_21_0", "YuYinZS4_0_0_0_0_1_4_3", "CHZS1_0_0_7_2257", "ZTYJ25_0_0_0_18", "CHZS1_0_0_7_1053", "YFZS20_0_0_3_10_8", "ZTYJ25_0_0_0_12", "YFZS20_0_0_3_0_17", "TLWJN12_0_1_0_5", "CHZS1_0_0_4_0_1_0", "CHZS1_0_0_7_193", "CHZS1_0_0_7_1217", "YuYinZS4_0_0_0_0_3_7_0", "YuYinZS4_0_0_0_0_1_24_1", "CHZS1_0_0_7_2820", "ZTYJ25_0_1_0_15", "YFZS20_0_0_3_1_27", "CHZS1_1_0_2", "YFZS20_0_0_3_13", "YuYongZS3_0_3_0_13", "CHZS1_0_0_4_0_3_8_1", "CHZS1_0_0_7_2330", "YFZS20_0_0_3_1_48", "KYWJN6_0_4_1", "YuYinZS4_0_0_0_0_1_3_0", "CHZS1_0_0_7_770", "YuYinZS4_0_0_2_0", "YFZS20_0_0_3_13_59_0", "KYWJN6_0_4_6", "YuYinZS4_0_0_0_0_1_0_5", "CHZS1_0_0_4_0_3_6_1", "CHZS1_0_0_7_281", "YuYinZS4_0_0_0_0_2_25_1", "YFZS20_0_0_0_0_2_1_1_0", "YPLX2_0_0_3_0", "CHZS1_0_0_7_2037", "CHZS1_0_0_7_1854", "CHZS1_0_0_4_0_3_0_2", "CHZS1_0_0_7_2934", "ZTYJ25_0_1_0_22", "HTSXD7_0_2", "CHZS1_0_0_7_1505", "CHZS1_0_0_7_1962", "CHZS1_0_0_4_0_3_16_0", "CHZS1_0_0_4_0_4", "CHZS1_0_0_7_3145", "CHZS1_0_0_7_2295", "CHZS1_0_0_4_0_3_7_2", "CHZS1_0_0_7_351", "CHZS1_0_0_4_0_1_1", "CHZS1_0_0_7_1485", "CHZS1_0_0_7_1590", "CHZS1_0_0_7_299", "CHZS1_0_0_4_0_3_6_0", "CHZS1_0_0_7_8096", "YPSL18_0_1", "YPLX2_0_0_1_0", "ZMTP24_0_0_20", "YuYinZS4_0_0_0_0_0_19", "ZDXS23_0_0", "ZMTP24_0_0_7", "YuYinZS4_0_0_0_0_0_6", "CHZS1_0_0_4_0_3_7_0", "CHZS1_0_0_7_869", "YuYinZS4_0_0_3_0", "YFZS20_0_0_2_0_0_0_1", "YFZS20_0_0_3_1_33", "YFZS20_0_0_3_1_44", "CHZS1_0_0_7_1370", "YPLX2_0_0_1", "ZMTP24_0_0_1", "YuYinZS4_0_0_0_0_0_0", "CHZS1_0_0_7_387", "YuYinZS4_0_0_0_0_4_9", "CHZS1_0_0_7_2012", "YuYinZS4_0_0_0_0_1_8_0", "CHZS1_0_0_4_0_3_9_1", "CHZS1_0_0_7_1698", "CHZS1_0_0_7_2659", "CHZS1_0_0_7_2657", "ZMTP24_0_0_6", "YuYinZS4_0_0_0_0_0_5", "CHZS1_0_0_4_0_3_0_4", "CHZS1_0_0_7_2976", "CHZS1_0_0_7_2826", "CHZS1_0_0_7_2917", "CHZS1_0_0_4_0_3_7_1", "CHZS1_0_0_4_0_3_11_4", "CHZS1_0_0_7_1125", "CHZS1_0_0_7_1959", "CHZS1_0_0_7_1816", "ZTYJ25_0_0_0", "YuYinZS4_0_0_5_0_1", "milk", "YPGN16_0_11", "YFZS20_0_0_2_0_0_2", "CHZS1_0_0_7_2798", "ZTYJ25_0_1_0_14", "YFZS20_0_0_3_13_122", "YFZS20_0_0_2_0_4_2_1_0", "banana", "CHZS1_0_0_7_1732", "YuYinZS4_0_0_0_0_0_14", "ZTYJ25_0_2_0_2", "YFZS20_0_0_0_0_2_0_0", "YFZS20_0_0_3_13_43_0", "YFZS20_0_0_2_0_4_2_0_2", "YFZS20_0_0_2_0_0_1", "song", "YuYinZS4_0_0_4_0_1_1_0", "YFZS20_0_0_3_13_138", "YuYinZS4_0_0_0_0_0_4", "YuYinZS4_0_0_0_0_2_9_0", "CHZS1_0_0_7_1809", "YFZS20_0_0_3_13_185", "ZMTP24_0_0_9", "ZMTP24_0_1_1", "ZMTP24_0_1_0", "ZMTP24_0_1_3", "YuYinZS4_0_0_0_0_0_8", "school", "get up", "YFZS20_0_0_3_13_122_1", "YPLX2_0_0_3_1", "CHZS1_0_0_4_0_2_2", "CHZS1_0_0_7_1489", "CHZS1_0_0_7_1862", "CHZS1_0_0_7_151", "CHZS1_0_0_7_1165", "CHZS1_0_0_7_1935", "CHZS1_0_0_7_2814", "YuYinZS4_0_0_0_0_0_22", "ZMTP24_0_0_14", "YuYinZS4_0_0_0_0_0_13", "YPZS22_0_0_2", "CHZS1_0_0_7_1270", "CHZS1_0_0_7_1948", "CHZS1_0_0_7_3039", "YFZS20_0_0_3_15_4", "TLWJN12_0_5_0_4", "SWPZblm10_0_1_4", "YuYinZS4_0_0_0_0_1_24_0", "YuYinZS4_0_0_0_0_1_14", "YuYinZS4_0_0_0_0_2_19_0", "CHZS1_0_0_7_3023", "YuYinZS4_0_0_0_0_1_22_0", "YuYinZS4_0_0_0_0_2_41_2", "CHZS1_0_0_4_0_1_11", "CHZS1_0_0_7_99", "CHZS1_0_0_7_266", "YFZS20_0_0_2_0_4_1", "CHZS1_0_0_7_3019", "YFZS20_0_0_3_13_0_0", "YFZS20_0_0_3_1_0", "YuYongZS3_0_4_0_1", "YuYinZS4_0_0_0_0_2_12_0", "YuYinZS4_0_0_0_0_1_9_0", "CHZS1_0_0_7_3097", "YuYinZS4_0_0_0_0_3_31_0", "CHZS1_0_0_7_1995", "YFZS20_0_0_3_1", "YuYongZS3_0_2_0_1", "YuYongZS3_0_3_0_1", "CHZS1_0_0_7_334", "CHZS1_0_0_7_2804", "CHZS1_0_0_4_0_3_15_4", "CHZS1_0_0_7_2532", "YFZS20_0_0_3_1_79", "ZTYJ25_0_1_0_9", "CHZS1_0_0_7_211", "CHZS1_0_0_7_1269", "YPLX2_0_0_6_1", "YPLX2_0_2_4", "KYWJN6_0_0_4", "CHZS1_0_0_7_1295", "CHZS1_0_0_7_1361", "CHZS1_0_0_7_1281", "CHZS1_0_0_7_2736", "CHZS1_0_0_7_1039", "CHZS1_0_0_7_2919", "CHZS1_0_0_7_437", "CHZS1_0_0_7_1342", "CHZS1_0_0_7_2294", "CHZS1_0_0_7_2918", "CHZS1_0_0_7_300", "YFZS20_0_0_3_1_113", "ZTYJ25_0_1_0_5", "CHZS1_0_0_7_2660", "CHZS1_0_0_7_444", "ZTYJ25_0_0_0_15", "CHZS1_0_0_4_0_3_15_2", "YFZS20_0_0_3_7_6", "CHZS1_0_0_7_1482", "YFZS20_0_0_2_0_4_4", "CHZS1_0_0_4_0_3_8_2", "CHZS1_0_0_7_556", "CHZS1_0_0_4_0_3_15_5", "CHZS1_0_0_7_2178", "CHZS1_0_0_7_2279", "CHZS1_0_0_7_1298", "CHZS1_0_0_7_1274", "YFZS20_0_0_2_0_8_6", "TLWJN12_0_4_0_13", "CHZS1_0_0_7_1273", "CHZS1_0_0_7_2430", "CHZS1_0_0_7_1958", "YFZS20_0_0_3_5_1", "CHZS1_0_0_7_87", "CHZS1_0_0_7_506", "ZTYJ25_0_1_0_8", "YFZS20_0_0_2_0_4_4_1_1", "YFZS20_0_0_3_13_232", "CHZS1_0_0_7_307", "CHZS1_0_0_7_1941", "YFZS20_0_0_3_1_36", "CHZS1_0_0_7_1052", "shirt", "CHZS1_0_0_7_1123", "YPGN16_0_9", "CHZS1_0_0_7_1211", "CHZS1_0_0_7_1899", "CHZS1_0_0_7_1523", "YFZS20_0_0_2_0_0_1_1_2", "YFZS20_0_0_2_0_0_1_1_3", "CHZS1_0_0_7_2835", "CHZS1_0_0_7_359", "CHZS1_0_0_7_1568", "YFZS20_0_0_0_0_1_4_0", "YFZS20_0_0_3_1_8", "WHZS5_0_0_6", "WHZS5_0_1_0_1_13", "YFZS20_0_0_3_0_13", "monkey", "YPLX2_0_0_3", "CHZS1_0_0_7_863", "YPZS22_1_0_1", "CHZS1_0_0_7_899", "YuYinZS4_0_0_0_0_1_24_3", "CHZS1_0_0_7_121");
        List<String> sectionTagIds = Lists.newArrayList("5f96c08ab9c81600013a900c", "5f96c08ab9c81600013a9008", "5f96c08ab9c81600013a8ff7", "5f96c08ab9c81600013a9009", "5f96c08ab9c81600013a900a", "5f96c08ab9c81600013a900b", "5f96c08eb9c81600013a9567", "5f96c08eb9c81600013a95c1", "5f96c08eb9c81600013a962d", "5f96c08eb9c81600013a9663");
        List<String> gradeTagIds = Lists.newArrayList("5f96c08eb9c81600013a9783", "5f96c08eb9c81600013a959d", "5f96c08eb9c81600013a97cb", "5f96c08eb9c81600013a9662", "5f96c08ab9c81600013a8ffd", "5f96c08ab9c81600013a8ffe", "5f96c08ab9c81600013a9000", "5f96c08ab9c81600013a9001", "5f96c08ab9c81600013a9004", "5f96c08ab9c81600013a9005");

        for (int i = 0; i < request.getCount(); i++) {
            int id = Math.abs(new Random().nextInt());
            KnowledgeGraphMarkResultEntity entity = new KnowledgeGraphMarkResultEntity();
            entity.setResourceId("id_" + id);
            entity.setResourceType(getRandomElement(resourceTypes));
            entity.setTagIdList(getRandomElements(tagIds, 10));
            entity.setResourceSubtype(entity.getResourceType());
            entity.setSectionTagIds(getRandomElements(sectionTagIds, 3));
            entity.setGradeTagIds(getRandomElements(gradeTagIds, 3));
            entity.setSort(id);
            entity.setCreateAtTime(new Date().getTime());
            entity.setUpdateAtTime(new Date().getTime());
            insert(entity);
        }
    }

    public static <T> T getRandomElement(List<T> list) {
        // 打乱列表顺序
        List<T> shuffledList = new ArrayList<>(list);
        Collections.shuffle(shuffledList);
        return shuffledList.get(0);
    }

    public static <T> List<T> getRandomElements(List<T> list, int count) {
        if (list.size() < count) {
            throw new IllegalArgumentException("List size is smaller than the requested count");
        }

        // 打乱列表顺序
        List<T> shuffledList = new ArrayList<>(list);
        Collections.shuffle(shuffledList);

        // 返回前 count 个元素
        return shuffledList.subList(0, count);
    }

    public String insert(KnowledgeGraphMarkResultEntity esEntity) {
        final String id = esEntity.getResourceType() + "_" + esEntity.getResourceId();

        try {
            esEntity.setCreateAtTime(new Date().getTime());
            esEntity.setUpdateAtTime(new Date().getTime());
            IndexRequest<KnowledgeGraphMarkResultEntity> indexRequest = IndexRequest.of(e -> e
                    .index(index_name)
                    .id(id)
                    .document(esEntity)
                    .refresh(Refresh.True)
                    .opType(OpType.Index) // 若主键重复，覆盖原有数据
            );
            client.index(indexRequest);
        } catch (Exception e) {
            log.info("KnowledgeGraphService#insert error.", e);
        }
        return null;
    }
}
