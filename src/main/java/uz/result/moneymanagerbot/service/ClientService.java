package uz.result.moneymanagerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.result.moneymanagerbot.exceptions.NotFoundException;
import uz.result.moneymanagerbot.model.Client;
import uz.result.moneymanagerbot.model.ServiceType;
import uz.result.moneymanagerbot.repository.ClientRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    private final ServiceTypeService serviceTypeService;

    public void defaultClientSave() {
        ServiceType service = serviceTypeService.findByName("Брендинг");
        Client client = Client.builder()
                .fullName("Client 1")
                .phoneNumber("+99894 456 56 56")
                .serviceType(service)
                .build();
        clientRepository.save(client);
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client is not found with id: " + id));
    }

}
