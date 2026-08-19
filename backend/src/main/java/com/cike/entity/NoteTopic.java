package com.cike.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记-话题关联表 t_note_topic
 */
@Data
@TableName("t_note_topic")
public class NoteTopic {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;

    private Long topicId;

    @TableLogic
    @JsonIgnore
    private Integer isDeleted;

    private LocalDateTime createTime;
}
