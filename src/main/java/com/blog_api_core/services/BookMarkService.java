package com.blog_api_core.services;

import com.blog_api_core.payload.BookMarkPayload;
import com.blog_api_core.repository.BookMarkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookMarkService {
    private final BookMarkRepository bookMarkRepository;
    public BookMarkService(BookMarkRepository bookMarkRepository) {
        this.bookMarkRepository = bookMarkRepository;
    }
    public List<BookMarkPayload> getAllBookMarksOfPost(Long postId) {
        return bookMarkRepository.findBookMarkByPost(postId);
    }
}
