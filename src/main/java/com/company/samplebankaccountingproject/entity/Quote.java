package com.company.samplebankaccountingproject.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@JmixEntity
@Table(name = "QUOTE", indexes = {
        @Index(name = "IDX_QUOTE_CUSTOMER", columnList = "CUSTOMER_ID")
})
@Entity
public class Quote {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull
    @Column(name = "DATE_", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "NUMBER_", length = 20)
    private String number;

    @JoinColumn(name = "CUSTOMER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    @Column(name = "ID1C", length = 50)
    private String id1C;

    public String getId1C() {
        return id1C;
    }

    public void setId1C(String id1C) {
        this.id1C = id1C;
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
    @DependsOnProperties({"number", "date"})
    public String getInstanceName() {
        return String.format("%s %s", number, date);
    }
}