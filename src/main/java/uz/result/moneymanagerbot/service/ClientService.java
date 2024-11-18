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

    public Client save(Client client) {
        return clientRepository.save(client);
    }

    public void defaultClientSave() {
        ServiceType service = serviceTypeService.findByName("Брендинг");
        Client client = Client.builder()
                .fullName("Client 1")
                .phoneNumber("+99894 456 56 56")
                .serviceType(service)
                .build();
        clientRepository.save(client);
        System.out.println("Deafult client qo'shildi");
    }

    public void deleteById(Long id) {
        clientRepository.deleteById(id);
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client is not found with id: " + id));
    }

    public void updateClientPhoneNumber(String phoneNumber, Long id) {
        clientRepository.updateClientPhoneNumberById(phoneNumber, id);
    }

    public void updateClientServiceCategory(Long id, ServiceType service) {
        Client client = clientRepository.findById(id).orElseThrow(() -> new NotFoundException("Client is not found with id: " + id));
        client.setServiceType(service);
        clientRepository.save(client);
    }

    public boolean existById(Long id) {
        return clientRepository.existsById(id);
    }

    public void updateClientFullName(Long id, String fullName) {
        clientRepository.updateClientFullNameById(fullName, id);
    }
}
