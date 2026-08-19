package com.cike.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cike.common.BusinessException;
import com.cike.dto.CommentRequest;
import com.cike.entity.*;
import com.cike.mapper.*;
import com.cike.service.InteractionService;
import com.cike.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final NoteMapper noteMapper;
    private final UserLikeNoteMapper userLikeNoteMapper;
    private final UserCollectNoteMapper userCollectNoteMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Map<String, Object> like(Long noteId, Long userId) {
        Note note = findNote(noteId);
        boolean already = userLikeNoteMapper.selectCount(
                Wrappers.<UserLikeNote>lambdaQuery()
                        .eq(UserLikeNote::getUserId, userId)
                        .eq(UserLikeNote::getNoteId, noteId)) > 0;

        Map<String, Object> data = new HashMap<>();
        if (already) {
            // 取消点赞
            userLikeNoteMapper.delete(Wrappers.<UserLikeNote>lambdaQuery()
                    .eq(UserLikeNote::getUserId, userId)
                    .eq(UserLikeNote::getNoteId, noteId));
            note.setLikeCount(Math.max(0, (note.getLikeCount() == null ? 0 : note.getLikeCount()) - 1));
            data.put("liked", false);
        } else {
            UserLikeNote like = new UserLikeNote();
            like.setUserId(userId);
            like.setNoteId(noteId);
            userLikeNoteMapper.insert(like);
            note.setLikeCount((note.getLikeCount() == null ? 0 : note.getLikeCount()) + 1);
            data.put("liked", true);
            // 作者获赞总数 +1
            addAuthorLike(note.getUserId(), 1);
        }
        noteMapper.updateById(note);
        data.put("likeCount", note.getLikeCount());
        return data;
    }

    @Override
    @Transactional
    public Map<String, Object> collect(Long noteId, Long userId) {
        Note note = findNote(noteId);
        boolean already = userCollectNoteMapper.selectCount(
                Wrappers.<UserCollectNote>lambdaQuery()
                        .eq(UserCollectNote::getUserId, userId)
                        .eq(UserCollectNote::getNoteId, noteId)) > 0;

        Map<String, Object> data = new HashMap<>();
        if (already) {
            userCollectNoteMapper.delete(Wrappers.<UserCollectNote>lambdaQuery()
                    .eq(UserCollectNote::getUserId, userId)
                    .eq(UserCollectNote::getNoteId, noteId));
            note.setCollectCount(Math.max(0, (note.getCollectCount() == null ? 0 : note.getCollectCount()) - 1));
            data.put("collected", false);
        } else {
            UserCollectNote item = new UserCollectNote();
            item.setUserId(userId);
            item.setNoteId(noteId);
            userCollectNoteMapper.insert(item);
            note.setCollectCount((note.getCollectCount() == null ? 0 : note.getCollectCount()) + 1);
            data.put("collected", true);
        }
        noteMapper.updateById(note);
        data.put("collectCount", note.getCollectCount());
        return data;
    }

    @Override
    @Transactional
    public CommentVO addComment(CommentRequest request, Long userId) {
        if (request.getNoteId() == null) {
            throw new BusinessException(400, "笔记id不能为空");
        }
        if (!StringUtils.hasText(request.getContent())) {
            throw new BusinessException(400, "评论内容不能为空");
        }
        Note note = findNote(request.getNoteId());

        Comment comment = new Comment();
        comment.setNoteId(request.getNoteId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        comment.setReplyUserId(request.getReplyUserId() == null ? 0L : request.getReplyUserId());
        comment.setContent(request.getContent().trim());
        commentMapper.insert(comment);

        note.setCommentCount((note.getCommentCount() == null ? 0 : note.getCommentCount()) + 1);
        noteMapper.updateById(note);

        return toCommentVO(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(401, "只能删除自己的评论");
        }
        commentMapper.deleteById(id);
        Note note = noteMapper.selectById(comment.getNoteId());
        if (note != null && note.getCommentCount() != null && note.getCommentCount() > 0) {
            note.setCommentCount(note.getCommentCount() - 1);
            noteMapper.updateById(note);
        }
    }

    // ==================== 辅助 ====================

    private Note findNote(Long noteId) {
        if (noteId == null) {
            throw new BusinessException(400, "笔记id不能为空");
        }
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException(404, "笔记不存在");
        }
        return note;
    }

    private void addAuthorLike(Long authorId, int delta) {
        if (authorId == null) {
            return;
        }
        User author = userMapper.selectById(authorId);
        if (author != null) {
            author.setLikeTotal((author.getLikeTotal() == null ? 0 : author.getLikeTotal()) + delta);
            userMapper.updateById(author);
        }
    }

    private CommentVO toCommentVO(Comment c) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setNoteId(c.getNoteId());
        vo.setUserId(c.getUserId());
        vo.setParentId(c.getParentId());
        vo.setReplyUserId(c.getReplyUserId());
        vo.setContent(c.getContent());
        vo.setCreateTime(c.getCreateTime());
        User author = userMapper.selectById(c.getUserId());
        if (author != null) {
            vo.setAuthorId(author.getId());
            vo.setAuthorNickname(author.getNickname());
            vo.setAuthorAvatar(author.getAvatar());
        }
        if (c.getReplyUserId() != null && c.getReplyUserId() > 0) {
            User ru = userMapper.selectById(c.getReplyUserId());
            if (ru != null) {
                vo.setReplyNickname(ru.getNickname());
            }
        }
        return vo;
    }
}
