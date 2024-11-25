package com.ms.springms.model.utils;

import lombok.Data;

@Data
public class PageResponse<T>{
   private final T data;
   private final int currentPage;
   private  final int totalPages;
   private final int pageSize;
   private final long totalElements;

}
