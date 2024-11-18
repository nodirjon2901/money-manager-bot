package uz.result.moneymanagerbot.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.result.moneymanagerbot.model.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsById(Long id);

    @Modifying
    @Transactional
    @Query(value = "update client set phone_number=:number where id=:id", nativeQuery = true)
    void updateClientPhoneNumberById(@Param("number") String number, @Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "update client set full_name=:name where id=:id", nativeQuery = true)
    void updateClientFullNameById(@Param("name") String fullName, @Param("id") Long id);

}
