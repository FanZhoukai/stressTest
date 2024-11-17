package com.stressTest.controller;

import com.stressTest.knowledgeGraph.KnowledgeGraphInsertRequest;
import com.stressTest.knowledgeGraph.KnowledgeGraphMarkResultResponse;
import com.stressTest.knowledgeGraph.KnowledgeGraphService;
import com.stressTest.knowledgeGraph.KnowledgeGraphTestQueryRequest;
import com.stressTest.util.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class HelloController {

    @Resource
    private KnowledgeGraphService knowledgeGraphService;

    @GetMapping("/hello")
    public String hello(String param) {
        return "hello, received:" + param;
    }


    @PostMapping("/query")
    public PageResult<KnowledgeGraphMarkResultResponse> query(@RequestBody KnowledgeGraphTestQueryRequest request) {
        return knowledgeGraphService.queryTerms(request);
    }

    @PostMapping("/batchInsert")
    public void batchInsert(@RequestBody KnowledgeGraphInsertRequest request) {
        knowledgeGraphService.batchInsert(request);
    }
}
