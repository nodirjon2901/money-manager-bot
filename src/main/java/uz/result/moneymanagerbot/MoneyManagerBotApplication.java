package uz.result.moneymanagerbot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import uz.result.moneymanagerbot.model.User;
import uz.result.moneymanagerbot.model.UserRole;
import uz.result.moneymanagerbot.repository.UserRepository;
import uz.result.moneymanagerbot.service.ClientService;
import uz.result.moneymanagerbot.service.ExpenseCategoryService;
import uz.result.moneymanagerbot.service.ServiceTypeService;

@SpringBootApplication
@RequiredArgsConstructor
@EnableScheduling
public class MoneyManagerBotApplication implements CommandLineRunner {

    @Value("${admin.chatId}")
    private Long chatId;

    @Value("${admin.password}")
    private String password;

    private final UserRepository userRepository;

    private final ExpenseCategoryService expenseCategoryService;

    private final ServiceTypeService serviceTypeService;

    private final ClientService clientService;

    public static void main(String[] args) {
        SpringApplication.run(MoneyManagerBotApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByChatId(chatId)) {
            User superAdmin = User.builder()
                    .chatId(chatId)
                    .name("Nodir")
                    .password(password)
                    .role(UserRole.SUPPER_ADMIN)
                    .build();
            userRepository.save(superAdmin);
            System.out.println("Nodir qo'shildi");
        }if (!userRepository.existsByChatId(56938L)) {
            User superAdmin = User.builder()
                    .chatId(56938L)
                    .name("Sardor aka")
                    .password(password)
                    .role(UserRole.SUPPER_ADMIN)
                    .build();
            userRepository.save(superAdmin);
            System.out.println("Sardor aka qo'shildi");
        }if (!userRepository.existsByChatId(6691713706L)) {
            User superAdmin = User.builder()
                    .chatId(6691713706L)
                    .name("Rashid aka")
                    .password(password)
                    .role(UserRole.SUPPER_ADMIN)
                    .build();
            userRepository.save(superAdmin);
            System.out.println("Rashid aka qo'shildi");
        }if (!userRepository.existsByChatId(1762041853L)) {
            User superAdmin = User.builder()
                    .chatId(1762041853L)
                    .name("Davlat aka")
                    .password(password)
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(superAdmin);
            System.out.println("Davlat aka qo'shildi");
        }
        expenseCategoryService.defaultCategorySave();
        serviceTypeService.defaultServiceTypeListSave();
        clientService.defaultClientSave();
    }
}