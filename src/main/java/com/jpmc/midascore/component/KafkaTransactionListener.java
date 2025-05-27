package com.jpmc.midascore.component;

import com.fasterxml.jackson.annotation.OptBoolean;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;


@Component
public class KafkaTransactionListener {


    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    private RestTemplate restTemplate;

    public KafkaTransactionListener(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-consumer-group")
    public void listenTranscation(Transaction transaction) {
        Optional <UserRecord> senderId =userRepository.findById(transaction.getSenderId());
        Optional <UserRecord> receiverId =userRepository.findById(transaction.getRecipientId());

        if(senderId.isEmpty() || receiverId.isEmpty()){
            return;
        }

        UserRecord sender = senderId.get();
        UserRecord recipient = receiverId.get();
        if(sender.getBalance() < transaction.getAmount()){
            return;
        }

        sender.setBalance(sender.getBalance() - transaction.getAmount());
        Incentive incentive;
        try {
            incentive = restTemplate.postForObject(
                    "http://localhost:8080/incentive",
                    transaction,
                    Incentive.class
            );
        } catch (Exception e) {
            incentive = new Incentive(0f); // fallback to 0 incentive
        }

        float incentiveAmount = (incentive != null) ? incentive.getAmount() : 0f;

        recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

        userRepository.save(sender);
        userRepository.save(recipient);

        TransactionRecord record = new TransactionRecord(sender, recipient, transaction.getAmount(), incentiveAmount);
        transactionRepository.save(record);
    }
}
