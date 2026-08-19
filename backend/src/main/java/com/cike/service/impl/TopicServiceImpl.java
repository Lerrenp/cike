package com.cike.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cike.entity.Topic;
import com.cike.mapper.TopicMapper;
import com.cike.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicMapper topicMapper;

    @Override
    public List<Topic> list() {
        return topicMapper.selectList(Wrappers.<Topic>lambdaQuery().orderByAsc(Topic::getId));
    }
}
