package com.cike.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cike.common.BusinessException;
import com.cike.common.UserContext;
import com.cike.dto.NotePublishRequest;
import com.cike.entity.*;
import com.cike.mapper.*;
import com.cike.service.NoteAssembler;
import com.cike.service.NoteService;
import com.cike.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;
    private final NoteImageMapper noteImageMapper;
    private final NoteTopicMapper noteTopicMapper;
    private final TopicMapper topicMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private final UserLikeNoteMapper userLikeNoteMapper;
    private final UserCollectNoteMapper userCollectNoteMapper;
    private final NoteAssembler noteAssembler;

    /** 话题类分类（按话题过滤） */
    private static final List<String> TOPIC_CATEGORIES = List.of("美食", "穿搭", "风景", "干货");

    @Override
    public PageVO<NoteVO> feed(int page, int size, String category) {
        int p = Math.max(page, 1);
        int s = size <= 0 ? 10 : Math.min(size, 100);

        LambdaQueryWrapper<Note> wrapper = Wrappers.<Note>lambdaQuery()
                .eq(Note::getVisible, 1)
                .eq(Note::getStatus, 1);

        if (TOPIC_CATEGORIES.contains(category)) {
            // 按话题分类过滤
            Topic topic = topicMapper.selectOne(
                    Wrappers.<Topic>lambdaQuery().eq(Topic::getTopicName, "#" + category));
            if (topic != null) {
                List<Long> noteIds = noteTopicMapper.selectList(
                                Wrappers.<NoteTopic>lambdaQuery().eq(NoteTopic::getTopicId, topic.getId()))
                        .stream().map(NoteTopic::getNoteId).distinct().toList();
                if (noteIds.isEmpty()) {
                    return new PageVO<>(Collections.emptyList(), 0, p, s);
                }
                wrapper.in(Note::getId, noteIds);
            }
        }

        if ("hot".equals(category)) {
            wrapper.orderByDesc(Note::getLikeCount).orderByDesc(Note::getCreateTime);
        } else {
            wrapper.orderByDesc(Note::getCreateTime);
        }

        Page<Note> pg = noteMapper.selectPage(new Page<>(p, s), wrapper);
        List<NoteVO> records = noteAssembler.toVOList(pg.getRecords());
        return new PageVO<>(records, pg.getTotal(), p, s);
    }

    @Override
    public NoteDetailVO detail(Long id) {
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException(404, "笔记不存在");
        }
        // 浏览量 +1
        note.setViewCount(note.getViewCount() == null ? 1 : note.getViewCount() + 1);
        noteMapper.updateById(note);

        NoteVO noteVO = noteAssembler.toVO(note);
        List<String> images = noteImageMapper.selectList(
                        Wrappers.<NoteImage>lambdaQuery().eq(NoteImage::getNoteId, id).orderByAsc(NoteImage::getSort))
                .stream().map(NoteImage::getImageUrl).toList();
        List<String> topicNames = loadTopicNames(id);

        Long currentUserId = UserContext.getUserId();
        boolean liked = currentUserId != null && userLikeNoteMapper.selectCount(
                Wrappers.<UserLikeNote>lambdaQuery()
                        .eq(UserLikeNote::getUserId, currentUserId)
                        .eq(UserLikeNote::getNoteId, id)) > 0;
        boolean collected = currentUserId != null && userCollectNoteMapper.selectCount(
                Wrappers.<UserCollectNote>lambdaQuery()
                        .eq(UserCollectNote::getUserId, currentUserId)
                        .eq(UserCollectNote::getNoteId, id)) > 0;

        NoteDetailVO vo = new NoteDetailVO();
        vo.setNote(noteVO);
        vo.setImages(images);
        vo.setTopics(topicNames);
        vo.setLiked(liked);
        vo.setCollected(collected);
        vo.setComments(loadComments(id));
        return vo;
    }

    @Override
    @Transactional
    public Long publish(NotePublishRequest request, Long userId) {
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BusinessException(400, "标题不能为空");
        }
        if (!StringUtils.hasText(request.getContent())) {
            throw new BusinessException(400, "正文不能为空");
        }
        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(request.getTitle().trim());
        note.setContent(request.getContent());
        note.setCoverUrl(CollectionUtils.isEmpty(request.getImages()) ? ""
                : request.getImages().get(0));
        note.setViewCount(0);
        note.setLikeCount(0);
        note.setCollectCount(0);
        note.setCommentCount(0);
        note.setVisible(request.getVisible() == null ? 1 : request.getVisible());
        note.setStatus(1);
        noteMapper.insert(note);

        // 图片
        if (!CollectionUtils.isEmpty(request.getImages())) {
            for (int i = 0; i < request.getImages().size(); i++) {
                NoteImage img = new NoteImage();
                img.setNoteId(note.getId());
                img.setImageUrl(request.getImages().get(i));
                img.setSort(i);
                noteImageMapper.insert(img);
            }
        }

        // 话题（不存在则创建）
        if (!CollectionUtils.isEmpty(request.getTopics())) {
            for (String t : request.getTopics()) {
                if (!StringUtils.hasText(t)) {
                    continue;
                }
                String name = t.startsWith("#") ? t : "#" + t;
                Topic topic = topicMapper.selectOne(
                        Wrappers.<Topic>lambdaQuery().eq(Topic::getTopicName, name));
                if (topic == null) {
                    topic = new Topic();
                    topic.setTopicName(name);
                    topic.setNoteCount(0);
                    topicMapper.insert(topic);
                }
                Topic finalTopic = topic;
                noteTopicMapper.insert(buildNoteTopic(note.getId(), finalTopic.getId()));
                topic.setNoteCount((topic.getNoteCount() == null ? 0 : topic.getNoteCount()) + 1);
                topicMapper.updateById(topic);
            }
        }

        // 发布者笔记数 +1
        User author = userMapper.selectById(userId);
        if (author != null) {
            author.setNoteCount((author.getNoteCount() == null ? 0 : author.getNoteCount()) + 1);
            userMapper.updateById(author);
        }
        return note.getId();
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException(404, "笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(400, "只能删除自己的笔记");
        }
        noteMapper.deleteById(id);
        // 逻辑删除关联图片与话题
        noteImageMapper.delete(Wrappers.<NoteImage>lambdaQuery().eq(NoteImage::getNoteId, id));
        noteTopicMapper.delete(Wrappers.<NoteTopic>lambdaQuery().eq(NoteTopic::getNoteId, id));
        // 发布者笔记数 -1
        User author = userMapper.selectById(userId);
        if (author != null && author.getNoteCount() != null && author.getNoteCount() > 0) {
            author.setNoteCount(author.getNoteCount() - 1);
            userMapper.updateById(author);
        }
    }

    // ==================== 私有辅助 ====================

    private NoteTopic buildNoteTopic(Long noteId, Long topicId) {
        NoteTopic nt = new NoteTopic();
        nt.setNoteId(noteId);
        nt.setTopicId(topicId);
        return nt;
    }

    private List<String> loadTopicNames(Long noteId) {
        List<Long> topicIds = noteTopicMapper.selectList(
                        Wrappers.<NoteTopic>lambdaQuery().eq(NoteTopic::getNoteId, noteId))
                .stream().map(NoteTopic::getTopicId).toList();
        if (topicIds.isEmpty()) {
            return Collections.emptyList();
        }
        return topicMapper.selectBatchIds(topicIds).stream()
                .map(Topic::getTopicName).toList();
    }

    private List<CommentVO> loadComments(Long noteId) {
        List<Comment> comments = commentMapper.selectList(
                Wrappers.<Comment>lambdaQuery().eq(Comment::getNoteId, noteId)
                        .orderByAsc(Comment::getCreateTime));
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = comments.stream().map(Comment::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> users = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        List<Long> replyIds = comments.stream()
                .map(Comment::getReplyUserId)
                .filter(rid -> rid != null && rid > 0)
                .distinct().collect(Collectors.toList());
        Map<Long, User> replyUsers = replyIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(replyIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));

        List<CommentVO> vos = new ArrayList<>();
        for (Comment c : comments) {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setNoteId(c.getNoteId());
            vo.setUserId(c.getUserId());
            vo.setParentId(c.getParentId());
            vo.setReplyUserId(c.getReplyUserId());
            vo.setContent(c.getContent());
            vo.setCreateTime(c.getCreateTime());
            User author = users.get(c.getUserId());
            if (author != null) {
                vo.setAuthorId(author.getId());
                vo.setAuthorNickname(author.getNickname());
                vo.setAuthorAvatar(author.getAvatar());
            }
            if (c.getReplyUserId() != null && c.getReplyUserId() > 0) {
                User ru = replyUsers.get(c.getReplyUserId());
                if (ru != null) {
                    vo.setReplyNickname(ru.getNickname());
                }
            }
            vos.add(vo);
        }
        return vos;
    }
}
