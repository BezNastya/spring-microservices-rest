package com.example.usermodule;

import com.example.usermodule.User;
import com.example.usermodule.exceptions.UserHasBooksException;
import com.example.usermodule.exceptions.UserNotFoundException;
import com.example.usermodule.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

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
            if(!user.getBooks().isEmpty())
                throw new UserHasBooksException("User has books. Can not delete him");

        userRepository.deleteById(userId);
        log.info("Thread finished");

    }
}
