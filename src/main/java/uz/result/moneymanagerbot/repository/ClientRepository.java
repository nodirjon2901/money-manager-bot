package uz.result.moneymanagerbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.result.moneymanagerbot.model.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
}
