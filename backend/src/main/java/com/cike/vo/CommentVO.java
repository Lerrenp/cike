package com.cike.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论 VO：评论 + 评论者信息 + 回复目标信息
 */
@Data
public class CommentVO {

    private Long id;
    private Long noteId;
    private Long userId;
    private Long parentId;
    private Long replyUserId;
    private String content;
    private LocalDateTime createTime;

    private Long authorId;
    private String authorNickname;
    private String authorAvatar;
    private String replyNickname;
}
