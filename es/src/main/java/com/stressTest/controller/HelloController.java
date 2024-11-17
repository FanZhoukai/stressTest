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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

@RestController
public class HelloController {

    @Resource
    private KnowledgeGraphService knowledgeGraphService;

    private BufferedWriter writer;

    @PostConstruct
    public void init() throws IOException {
        // 初始化 BufferedWriter
        writer = new BufferedWriter(new FileWriter("execution_time.log", true));
    }
    @PreDestroy
    public void destroy() throws IOException {
        // 关闭 BufferedWriter
        if (writer != null) {
            writer.close();
        }
    }

    @GetMapping("/hello")
    public String hello(String param) {
        return "hello, received:" + param;
    }


    @PostMapping("/query")
    public PageResult<KnowledgeGraphMarkResultResponse> query(@RequestBody KnowledgeGraphTestQueryRequest request) {
        long startTime = System.currentTimeMillis(); // 记录开始时间

        try {
            return knowledgeGraphService.queryTerms(request);
        } finally {
            long endTime = System.currentTimeMillis(); // 记录结束时间
            long duration = endTime - startTime;

            // 获取当前时间戳
            String timestamp = Instant.now().toString();
            String logEntry = String.format("Timestamp: %s, Execution Time: %d ms%n", timestamp, duration);

            // 写入日志
            try {
                synchronized (writer) {
                    writer.write(logEntry);
                    writer.flush(); // 确保写入立即生效
                }
            } catch (IOException e) {
                e.printStackTrace(); // 处理写入异常
            }
        }
    }

    @PostMapping("/batchInsert")
    public void batchInsert(@RequestBody KnowledgeGraphInsertRequest request) {
        knowledgeGraphService.batchInsert(request);
    }
}
