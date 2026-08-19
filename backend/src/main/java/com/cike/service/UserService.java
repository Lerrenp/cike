package com.cike.service;

import com.cike.entity.User;
import com.cike.vo.NoteVO;

import java.util.List;

public interface UserService {

    List<User> listUsers();

    User getUser(Long id);

    User updateUser(Long id, User update);

    List<NoteVO> myNotes(Long id);

    List<NoteVO> myLikes(Long id);

    List<NoteVO> myCollects(Long id);
}
