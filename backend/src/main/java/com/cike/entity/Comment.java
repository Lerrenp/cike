package com.cike.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论表 t_comment（支持二级回复）
 */
@Data
@TableName("t_comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;

    private Long userId;

    /** 父评论id，0代表一级评论 */
    private Long parentId;

    /** 回复目标用户id */
    private Long replyUserId;

    private String content;

    @TableLogic
    @JsonIgnore
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
