package com.cike.service;

import com.cike.dto.NotePublishRequest;
import com.cike.vo.NoteDetailVO;
import com.cike.vo.NoteVO;
import com.cike.vo.PageVO;

public interface NoteService {

    PageVO<NoteVO> feed(int page, int size, String category);

    NoteDetailVO detail(Long id);

    Long publish(NotePublishRequest request, Long userId);

    void delete(Long id, Long userId);
}
