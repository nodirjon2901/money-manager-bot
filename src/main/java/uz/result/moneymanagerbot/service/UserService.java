package uz.result.moneymanagerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.result.moneymanagerbot.exceptions.NotFoundException;
import uz.result.moneymanagerbot.model.User;
import uz.result.moneymanagerbot.model.UserRole;
import uz.result.moneymanagerbot.model.UserState;
import uz.result.moneymanagerbot.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void save(User user) {
        userRepository.save(user);
    }

    public UserRole findRoleByChatId(Long chatId) {
        return userRepository.findRoleByChatId(chatId)
                .orElseThrow(() -> new NotFoundException("User is not found by chatId: " + chatId));
    }

    public UserState findStateByChatId(Long chatId) {
        return userRepository.findStateByChatId(chatId)
                .orElse(UserState.DEFAULT);
    }

    public void updateStateByChatId(Long chatId, UserState state) {
        userRepository.updateUserStateByChatId(state.name(), chatId);
    }

    public User findByChatId(Long chatId) {
        return userRepository.findByChatId(chatId)
                .orElseThrow(() -> new NotFoundException("User is not found with chatId: " + chatId));
    }

    public void updateUserPasswordById(String password, Long chatId) {
        userRepository.updateUserPasswordByChatId(password, chatId);
    }

    public List<User> findAllExpectSuperAdmin() {
        return userRepository.findAll()
                .stream()
                .filter(user -> !user.getRole().equals(UserRole.SUPPER_ADMIN))
                .collect(Collectors.toList());
    }

    public User findById(Integer id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User is not found with id: " + id));
    }

    public void delete(Integer id) {
        userRepository.deleteById(id);
    }

    public void updateUserNameById(String name, Integer id) {
        userRepository.updateUserNameById(name, id);
    }

    public void updateUserChatIdById(Long chatId, Integer id) {
        userRepository.updateUserChatIdById(chatId, id);
    }

    public void updateUserRoleById(String role, Integer id) {
        userRepository.updateUserRoleById(role, id);
    }

}
