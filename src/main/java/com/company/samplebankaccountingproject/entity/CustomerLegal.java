package com.company.samplebankaccountingproject.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Table(name = "CUSTOMER_LEGAL")
@DiscriminatorValue("LEGAL")
@JmixEntity
@Entity
@PrimaryKeyJoinColumn(name = "ID", referencedColumnName = "ID")
public class CustomerLegal extends Customer {

    @NotNull
    @Column(name = "INN", nullable = false, length = 10)
    private String inn;

    @NotNull
    @Column(name = "KPP", nullable = false, length = 9)
    private String kpp;

    @Column(name = "OGRN", length = 13)
    private String ogrn;

    public String getOgrn() {
        return ogrn;
    }

    public void setOgrn(String ogrn) {
        this.ogrn = ogrn;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }
}