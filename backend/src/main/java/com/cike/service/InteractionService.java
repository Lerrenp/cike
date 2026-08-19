package com.cike.service;

import com.cike.dto.CommentRequest;
import com.cike.vo.CommentVO;

import java.util.Map;

public interface InteractionService {

    /** 点赞/取消点赞 */
    Map<String, Object> like(Long noteId, Long userId);

    /** 收藏/取消收藏 */
    Map<String, Object> collect(Long noteId, Long userId);

    CommentVO addComment(CommentRequest request, Long userId);

    void deleteComment(Long id, Long userId);
}
