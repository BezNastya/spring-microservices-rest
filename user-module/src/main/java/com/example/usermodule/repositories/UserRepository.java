package com.example.usermodule.repositories;

import com.example.usermodule.Book;
import com.example.usermodule.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByLogin(String login);
    User findUserById(Long id);
    List<User> findUsersByBooksContains(Book book);
}
