package uz.result.moneymanagerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.result.moneymanagerbot.exceptions.NotFoundException;
import uz.result.moneymanagerbot.model.ServiceType;
import uz.result.moneymanagerbot.repository.ServiceTypeRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;

    public void defaultServiceTypeListSave() {
        List<ServiceType> serviceTypeList = new ArrayList<>();
        if (!serviceTypeRepository.existsByName("Разработка сайта"))
            serviceTypeList.add(ServiceType.builder().name("Разработка сайта").build());
        if (!serviceTypeRepository.existsByName("Разработка бота"))
            serviceTypeList.add(ServiceType.builder().name("Разработка бота").build());
        if (!serviceTypeRepository.existsByName("SMM"))
            serviceTypeList.add(ServiceType.builder().name("SMM").build());
        if (!serviceTypeRepository.existsByName("Запуск контекстной рекламы"))
            serviceTypeList.add(ServiceType.builder().name("Запуск контекстной рекламы").build());
        if (!serviceTypeRepository.existsByName("Запуск таргетированной рекламы"))
            serviceTypeList.add(ServiceType.builder().name("Запуск таргетированной рекламы").build());
        if (!serviceTypeRepository.existsByName("Брендинг"))
            serviceTypeList.add(ServiceType.builder().name("Брендинг").build());
        if (!serviceTypeRepository.existsByName("SEO"))
            serviceTypeList.add(ServiceType.builder().name("SEO").build());

        if (!serviceTypeList.isEmpty())
            serviceTypeRepository.saveAll(serviceTypeList);
    }

    public ServiceType findByName(String name) {
        return serviceTypeRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Service is not found with this name: " + name));
    }

}
