package com.cike.controller;

import com.cike.common.Result;
import com.cike.entity.Topic;
import com.cike.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 话题模块 /api/v1/topics
 */
@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    /** 19. 话题列表 */
    @GetMapping
    public Result<List<Topic>> list() {
        return Result.ok(topicService.list());
    }
}
