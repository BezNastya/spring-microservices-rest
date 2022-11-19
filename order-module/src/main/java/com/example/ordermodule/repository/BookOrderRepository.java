package com.example.ordermodule.repository;

import com.example.ordermodule.entity.BookOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookOrderRepository extends JpaRepository<BookOrder, Long> {
    BookOrder findBookOrderByBookIdAndUserId(long bookId, long userId);
}
