package uz.result.moneymanagerbot.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.result.moneymanagerbot.model.User;
import uz.result.moneymanagerbot.model.UserRole;
import uz.result.moneymanagerbot.model.UserState;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByChatId(Long chatId);

    Optional<User> findByChatId(Long chatId);

    @Query(value = "select role from users where chat_id=:chatId", nativeQuery = true)
    Optional<UserRole> findRoleByChatId(@Param("chatId") Long chatId);

    @Query(value = "select state from users where chat_id=:chatId", nativeQuery = true)
    Optional<UserState> findStateByChatId(@Param("chatId") Long chatId);

    @Modifying
    @Transactional
    @Query(value = "update users set state=:state where chat_id=:chatId", nativeQuery = true)
    void updateUserStateByChatId(@Param("state") String userState, @Param("chatId") Long chatId);

    @Modifying
    @Transactional
    @Query(value = "update users set password=:password where chat_id=:chatId", nativeQuery = true)
    void updateUserPasswordByChatId(@Param("password") String password, @Param("chatId") Long chatId);

    @Modifying
    @Transactional
    @Query(value = "update users set name=:name where id=:id", nativeQuery = true)
    void updateUserNameById(@Param("name") String name, @Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "update users set chat_id=:chatId where id=:id", nativeQuery = true)
    void updateUserChatIdById(@Param("chatId") Long chatId, @Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "update users set role=:role where id=:id", nativeQuery = true)
    void updateUserRoleById(@Param("role") String role, @Param("id") Integer id);

}
