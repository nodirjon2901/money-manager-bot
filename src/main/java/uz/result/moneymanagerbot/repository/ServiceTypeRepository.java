package uz.result.moneymanagerbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.result.moneymanagerbot.model.ServiceType;

import java.util.Optional;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, Integer> {

    boolean existsByName(String name);

    Optional<ServiceType> findByName(String name);

}
