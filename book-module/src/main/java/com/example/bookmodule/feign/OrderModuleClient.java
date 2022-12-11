package com.example.bookmodule.feign;

import com.example.bookmodule.dto.BookOrderList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "order-module", url = "${ordermodule.url}")
public interface OrderModuleClient {
    @RequestMapping(method = RequestMethod.GET, value = "/orders/all")
    BookOrderList getAllBookOrders();

    @RequestMapping(method = RequestMethod.GET, value = "/orders/{userId}")
    BookOrderList getBookOrdersByUserId(@PathVariable("userId") Long userId);
}
