package uz.result.moneymanagerbot.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    MoneyType moneyType;

    Double summa;

    String transactionDate;

    @ManyToOne
    ExpenseCategory expenseCategory;

    @ManyToOne
    Client client;

    @Enumerated(EnumType.STRING)
    TransactionStatus transactionStatus;

    @Column(length = 10000)
    String comment;

    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true)
    FileEntity file;

}
