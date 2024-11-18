package uz.result.moneymanagerbot.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.result.moneymanagerbot.model.ServiceType;

import java.util.Optional;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, Integer> {

    boolean existsById(Integer id);

    boolean existsByName(String name);

    Optional<ServiceType> findByName(String name);

    @Modifying
    @Transactional
    @Query(value = "update service_type set name=:name where id=:id", nativeQuery = true)
    void updateServiceTypeNameById(@Param("name") String name, @Param("id") Integer id);

}
