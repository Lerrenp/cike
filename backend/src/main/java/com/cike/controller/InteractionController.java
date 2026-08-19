package com.cike.controller;

import com.cike.common.Result;
import com.cike.common.UserContext;
import com.cike.dto.CommentRequest;
import com.cike.dto.NoteIdRequest;
import com.cike.service.InteractionService;
import com.cike.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 互动模块（点赞 / 收藏 / 评论）
 */
@RestController
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    /** 15. 点赞 */
    @PostMapping("/like")
    public Result<Map<String, Object>> like(@RequestBody NoteIdRequest request) {
        return Result.ok(interactionService.like(request.getNoteId(), UserContext.requireUserId()));
    }

    /** 15. 取消点赞 */
    @DeleteMapping("/like/{noteId}")
    public Result<Map<String, Object>> unlike(@PathVariable Long noteId) {
        return Result.ok(interactionService.like(noteId, UserContext.requireUserId()));
    }

    /** 16. 收藏 */
    @PostMapping("/collect")
    public Result<Map<String, Object>> collect(@RequestBody NoteIdRequest request) {
        return Result.ok(interactionService.collect(request.getNoteId(), UserContext.requireUserId()));
    }

    /** 16. 取消收藏 */
    @DeleteMapping("/collect/{noteId}")
    public Result<Map<String, Object>> uncollect(@PathVariable Long noteId) {
        return Result.ok(interactionService.collect(noteId, UserContext.requireUserId()));
    }

    /** 17. 发表评论 */
    @PostMapping("/comments")
    public Result<CommentVO> addComment(@RequestBody CommentRequest request) {
        return Result.ok("评论成功", interactionService.addComment(request, UserContext.requireUserId()));
    }

    /** 18. 删除评论 */
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        interactionService.deleteComment(id, UserContext.requireUserId());
        return Result.ok("删除成功");
    }
}
