package com.cike.service;

import com.cike.entity.Note;
import com.cike.entity.User;
import com.cike.mapper.UserMapper;
import com.cike.vo.AuthorVO;
import com.cike.vo.NoteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 笔记 -> NoteVO 装配器（补充作者信息），供笔记/用户服务复用
 */
@Component
@RequiredArgsConstructor
public class NoteAssembler {

    private final UserMapper userMapper;

    public NoteVO toVO(Note note) {
        if (note == null) {
            return null;
        }
        NoteVO vo = new NoteVO();
        vo.setId(note.getId());
        vo.setUserId(note.getUserId());
        vo.setTitle(note.getTitle());
        vo.setContent(note.getContent());
        vo.setCoverUrl(note.getCoverUrl());
        vo.setViewCount(note.getViewCount());
        vo.setLikeCount(note.getLikeCount());
        vo.setCollectCount(note.getCollectCount());
        vo.setCommentCount(note.getCommentCount());
        vo.setVisible(note.getVisible());
        vo.setStatus(note.getStatus());
        vo.setCreateTime(note.getCreateTime());
        vo.setUpdateTime(note.getUpdateTime());
        User author = userMapper.selectById(note.getUserId());
        if (author != null) {
            vo.setAuthor(new AuthorVO(author.getId(), author.getNickname(), author.getAvatar()));
        }
        return vo;
    }

    public List<NoteVO> toVOList(List<Note> notes) {
        if (notes == null || notes.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = notes.stream().map(Note::getUserId).distinct().toList();
        Map<Long, User> users = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        List<NoteVO> vos = new ArrayList<>();
        for (Note note : notes) {
            NoteVO vo = new NoteVO();
            vo.setId(note.getId());
            vo.setUserId(note.getUserId());
            vo.setTitle(note.getTitle());
            vo.setContent(note.getContent());
            vo.setCoverUrl(note.getCoverUrl());
            vo.setViewCount(note.getViewCount());
            vo.setLikeCount(note.getLikeCount());
            vo.setCollectCount(note.getCollectCount());
            vo.setCommentCount(note.getCommentCount());
            vo.setVisible(note.getVisible());
            vo.setStatus(note.getStatus());
            vo.setCreateTime(note.getCreateTime());
            vo.setUpdateTime(note.getUpdateTime());
            User author = users.get(note.getUserId());
            if (author != null) {
                vo.setAuthor(new AuthorVO(author.getId(), author.getNickname(), author.getAvatar()));
            }
            vos.add(vo);
        }
        return vos;
    }
}
