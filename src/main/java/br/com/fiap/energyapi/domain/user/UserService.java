package br.com.fiap.energyapi.domain.user;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    public User updateUser(String userId, User userDetails) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setEmail(userDetails.getEmail());
            user.setCreatedAt(userDetails.getCreatedAt());
            user.setDevices(userDetails.getDevices());
            user.setEnergyMeters(userDetails.getEnergyMeters());
            user.setReports(userDetails.getReports());
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found");
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }
}

