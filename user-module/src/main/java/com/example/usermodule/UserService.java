package com.example.usermodule;

import com.example.usermodule.exceptions.UserHasBooksException;
import com.example.usermodule.exceptions.UserNotFoundException;
import com.example.usermodule.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.usermodule.config.ActiveMQConfiguration.USERS_BOOK_QUEUE;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @JmsListener(destination = "user_management")
    public void deleteUserById(Long userId) {

            User user = userRepository.findUserById(userId);
            if(user==null)
                throw new UserNotFoundException("User not found");
//            if(!user.getBooks().isEmpty())
//                throw new UserHasBooksException("User has books. Can not delete him");

        userRepository.deleteById(userId);
        log.info("Thread finished");

    }

//    @JmsListener(destination = USERS_BOOK_QUEUE, selector = "JMSType = 'DELETE'")
//    public void deleteBooksInUsers(long bookId){
//        Book book = new Book();
//        book.setId(bookId);
//        List<User> users = userRepository.findUsersByBooksContains(book);
////        users.stream().forEach(u -> {
////            List<Book> userBooks =  u.getBooks().stream()
////                    .filter(b -> b.getId() != book.getId())
////                    .collect(Collectors.toList());
////            u.setBooks(userBooks);
////        });
//
//        userRepository.saveAll(users);
//    }

}
