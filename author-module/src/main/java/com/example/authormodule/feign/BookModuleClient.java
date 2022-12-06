package com.example.authormodule.feign;

import com.example.authormodule.dto.BooksList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "book-module", url = "${bookmodule.url}")
public interface BookModuleClient {
    @RequestMapping(method = RequestMethod.GET, value = "/books/booksByAuthor/{authorId}")
    BooksList getBooksByAuthor(@RequestHeader("Authorization") String token,
            @PathVariable("authorId") Long authorId);
}
