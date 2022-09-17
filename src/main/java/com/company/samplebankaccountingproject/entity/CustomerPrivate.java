package com.company.samplebankaccountingproject.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@DiscriminatorValue("PRIVATE")
@JmixEntity
@Table(name = "CUSTOMER_PRIVATE")
@Entity
@PrimaryKeyJoinColumn(name = "ID", referencedColumnName = "ID")
public class CustomerPrivate extends Customer {
    @NotNull
    @Column(name = "PASSPORT_ID", nullable = false, length = 11)
    private String passportID;

    @Column(name = "LEGAL_AUTHORITY", length = 1000)
    private String legalAuthority;

    @Column(name = "PASSPORT_ISSUE_DATE")
    private LocalDate passportIssueDate;

    public LocalDate getPassportIssueDate() {
        return passportIssueDate;
    }

    public void setPassportIssueDate(LocalDate passportIssueDate) {
        this.passportIssueDate = passportIssueDate;
    }

    public String getLegalAuthority() {
        return legalAuthority;
    }

    public void setLegalAuthority(String legalAuthority) {
        this.legalAuthority = legalAuthority;
    }

    public String getPassportID() {
        return passportID;
    }

    public void setPassportID(String passportID) {
        this.passportID = passportID;
    }
}