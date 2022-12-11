package com.example.usermodule.feign;

import com.example.usermodule.BooksList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "book-module", url = "${bookmodule.url}")
public interface BookModuleClient {
    @RequestMapping(method = RequestMethod.GET, value = "/books/{userId}")
    BooksList getBooksByAuthor(@RequestHeader("Authorization") String token,
                               @PathVariable("userId") Long userId);
}
