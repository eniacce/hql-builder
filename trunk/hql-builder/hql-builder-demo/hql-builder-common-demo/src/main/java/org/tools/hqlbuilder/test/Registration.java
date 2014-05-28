package org.tools.hqlbuilder.test;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDateTime;

@XmlRootElement
@Entity
public class Registration extends EntityParent {
    private static final long serialVersionUID = -78719256082799222L;

    @Column(updatable = false)
    @NotNull
    private String password;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private String email;

    @NotNull
    private String username;

    private String regkey;

    private LocalDateTime verification;

    private Date dateOfBirth;

    public String getPassword() {
        return this.password;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getEmail() {
        return this.email;
    }

    public String getUsername() {
        return this.username;
    }

    public String getRegkey() {
        return this.regkey;
    }

    public LocalDateTime getVerification() {
        return this.verification;
    }

    public Date getDateOfBirth() {
        return this.dateOfBirth;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRegkey(String regkey) {
        this.regkey = regkey;
    }

    public void setVerification(LocalDateTime verification) {
        this.verification = verification;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
