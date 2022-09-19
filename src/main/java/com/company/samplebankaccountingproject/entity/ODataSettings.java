package com.company.samplebankaccountingproject.entity;

import io.jmix.appsettings.entity.AppSettingsEntity;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@JmixEntity
@Table(name = "O_DATA_SETTINGS")
@Entity
public class ODataSettings extends AppSettingsEntity {
    @Column(name = "O_DATA_URL")
    private String oDataURL;

    @Column(name = "O_DATA_USER")
    private String oDataUser;

    @Column(name = "O_DATA_PASSWORD")
    private String oDataPassword;

    public String getODataPassword() {
        return oDataPassword;
    }

    public void setODataPassword(String oDataPassword) {
        this.oDataPassword = oDataPassword;
    }

    public String getODataUser() {
        return oDataUser;
    }

    public void setODataUser(String oDataUser) {
        this.oDataUser = oDataUser;
    }

    public String getODataURL() {
        return oDataURL;
    }

    public void setODataURL(String oDataURL) {
        this.oDataURL = oDataURL;
    }
}