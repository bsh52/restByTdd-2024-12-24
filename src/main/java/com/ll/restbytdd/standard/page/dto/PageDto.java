package com.ll.restbytdd.standard.page.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageDto<T> {
    private int currentPageNumber;
    private int pageSize;
    private int totalPages;
    private int totalElements;
    private List<T> content;

    public PageDto(Page<T> page) {
        this.currentPageNumber = page.getNumber() + 1;
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getNumberOfElements();
        this.content = page.getContent();
    }
}
