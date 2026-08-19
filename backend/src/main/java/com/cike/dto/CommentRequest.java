package com.cike.dto;

import lombok.Data;

/**
 * 发表评论请求
 */
@Data
public class CommentRequest {

    private Long noteId;
    private String content;
    /** 父评论id，0 为一级评论 */
    private Long parentId;
    /** 回复目标用户id */
    private Long replyUserId;
}
