package com.company.samplebankaccountingproject.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.UUID;

@DiscriminatorValue("LEGAL")
@JmixEntity
@Table(name = "CUSTOMER")
@Entity
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING, length = 10)
@Inheritance(strategy = InheritanceType.JOINED)
public class Customer {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "TYPE_", length = 10)
    private String type;

    @Column(name = "ADDRESS", length = 500)
    private String address;

    @Column(name = "ID1C", length = 50)
    private String id1C;

    public String getId1C() {
        return id1C;
    }

    public void setId1C(String id1C) {
        this.id1C = id1C;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}