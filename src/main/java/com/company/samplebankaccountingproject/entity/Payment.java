package com.company.samplebankaccountingproject.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.NumberFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@JmixEntity
@Table(name = "PAYMENT", indexes = {
        @Index(name = "IDX_PAYMENT_CUSTOMER", columnList = "CUSTOMER_ID"),
        @Index(name = "IDX_PAYMENT_INCOMINGDESCRIPTIO", columnList = "INCOMING_DESCRIPTION_ID"),
        @Index(name = "IDX_PAYMENT_BANK_ACCOUNT", columnList = "BANK_ACCOUNT_ID"),
        @Index(name = "IDX_PAYMENT_QUOTE", columnList = "QUOTE_ID")
})
@Entity
public class Payment {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "DATE_", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date date;

    @Column(name = "NUMBER_", length = 20)
    private String number;

    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Customer customer;

    @JoinColumn(name = "QUOTE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Quote quote;

    @JoinColumn(name = "INCOMING_DESCRIPTION_ID")
    @OneToOne(fetch = FetchType.LAZY)
    private IncomingDescription incomingDescription;

    @JoinColumn(name = "BANK_ACCOUNT_ID")
    @OneToOne(fetch = FetchType.LAZY)
    private BankAccount bankAccount;

    @NumberFormat(pattern = "#,##0.##")
    @Column(name = "SUM_", precision = 15, scale = 2)
    private BigDecimal sum;

    public Quote getQuote() {
        return quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public IncomingDescription getIncomingDescription() {
        return incomingDescription;
    }

    public void setIncomingDescription(IncomingDescription incomingDescription) {
        this.incomingDescription = incomingDescription;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @InstanceName
    @DependsOnProperties({"number", "date", "bankAccount", "customer"})
    public String getInstanceName() {
        return String.format("%s %s %s %s", number, date, bankAccount.getName(), customer.getName());
    }
}