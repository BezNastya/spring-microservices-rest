import com.example.usermodule.User;
import com.example.usermodule.exceptions.UserHasBooksException;
import com.example.usermodule.exceptions.UserNotFoundException;
import com.example.usermodule.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;

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

    }
}
