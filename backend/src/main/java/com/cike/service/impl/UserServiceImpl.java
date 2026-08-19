package com.cike.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cike.common.BusinessException;
import com.cike.common.UserContext;
import com.cike.entity.*;
import com.cike.mapper.*;
import com.cike.service.NoteAssembler;
import com.cike.service.UserService;
import com.cike.vo.NoteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final NoteMapper noteMapper;
    private final UserLikeNoteMapper userLikeNoteMapper;
    private final UserCollectNoteMapper userCollectNoteMapper;
    private final NoteAssembler noteAssembler;

    @Override
    public List<User> listUsers() {
        return userMapper.selectList(Wrappers.<User>lambdaQuery()
                .orderByAsc(User::getId));
    }

    @Override
    public User getUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    @Override
    public User updateUser(Long id, User update) {
        // 只能修改自己的信息
        Long current = UserContext.requireUserId();
        if (!current.equals(id)) {
            throw new BusinessException(401, "无权修改他人信息");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (StringUtils.hasText(update.getNickname())) {
            user.setNickname(update.getNickname());
        }
        if (update.getAvatar() != null) {
            user.setAvatar(update.getAvatar());
        }
        if (update.getBio() != null) {
            user.setBio(update.getBio());
        }
        userMapper.updateById(user);
        return user;
    }

    @Override
    public List<NoteVO> myNotes(Long id) {
        requireOwner(id);
        List<Note> notes = noteMapper.selectList(
                Wrappers.<Note>lambdaQuery().eq(Note::getUserId, id).orderByDesc(Note::getCreateTime));
        return noteAssembler.toVOList(notes);
    }

    @Override
    public List<NoteVO> myLikes(Long id) {
        requireOwner(id);
        List<Long> noteIds = userLikeNoteMapper.selectList(
                        Wrappers.<UserLikeNote>lambdaQuery().eq(UserLikeNote::getUserId, id)
                                .orderByDesc(UserLikeNote::getCreateTime))
                .stream().map(UserLikeNote::getNoteId).toList();
        if (noteIds.isEmpty()) {
            return Collections.emptyList();
        }
        return noteAssembler.toVOList(noteMapper.selectBatchIds(noteIds));
    }

    @Override
    public List<NoteVO> myCollects(Long id) {
        requireOwner(id);
        List<Long> noteIds = userCollectNoteMapper.selectList(
                        Wrappers.<UserCollectNote>lambdaQuery().eq(UserCollectNote::getUserId, id)
                                .orderByDesc(UserCollectNote::getCreateTime))
                .stream().map(UserCollectNote::getNoteId).toList();
        if (noteIds.isEmpty()) {
            return Collections.emptyList();
        }
        return noteAssembler.toVOList(noteMapper.selectBatchIds(noteIds));
    }

    private void requireOwner(Long id) {
        Long current = UserContext.requireUserId();
        if (!current.equals(id)) {
            throw new BusinessException(401, "无权查看他人私有数据");
        }
    }
}
