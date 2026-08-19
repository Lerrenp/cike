package com.cike.vo;

import com.cike.entity.Comment;
import lombok.Data;

import java.util.List;

/**
 * 笔记详情 VO
 */
@Data
public class NoteDetailVO {

    private NoteVO note;
    /** 图片 url 列表 */
    private List<String> images;
    /** 话题列表，如 ["#美食"] */
    private List<String> topics;
    private boolean isLiked;
    private boolean isCollected;
    /** 评论区（带作者信息） */
    private List<CommentVO> comments;
}
