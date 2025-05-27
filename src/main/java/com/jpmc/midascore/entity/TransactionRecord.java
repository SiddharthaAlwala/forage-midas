package com.jpmc.midascore.entity;

import com.jpmc.midascore.foundation.Incentive;
import jakarta.persistence.*;


@Entity
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UserRecord sender;
    @ManyToOne
    private UserRecord receiver;

    private float amount;

    @Column(nullable = false)
    private float incentive;

    public TransactionRecord() {
    }

    public UserRecord getSender() {
        return sender;
    }

    public void setSender(UserRecord sender) {
        this.sender = sender;
    }

    public UserRecord getReceiver() {
        return receiver;
    }

    public void setReceiver(UserRecord receiver) {
        this.receiver = receiver;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
    public float getIncentive() {
        return incentive;
    }
    public void setIncentive(float incentive) {
        this.incentive = incentive;
    }

    public TransactionRecord(UserRecord sender, UserRecord receiver, float amount, float incentive) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.incentive = incentive;
    }

}
