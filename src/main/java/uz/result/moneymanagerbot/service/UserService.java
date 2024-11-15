package uz.result.moneymanagerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.result.moneymanagerbot.exceptions.NotFoundException;
import uz.result.moneymanagerbot.model.User;
import uz.result.moneymanagerbot.model.UserRole;
import uz.result.moneymanagerbot.model.UserState;
import uz.result.moneymanagerbot.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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

}
