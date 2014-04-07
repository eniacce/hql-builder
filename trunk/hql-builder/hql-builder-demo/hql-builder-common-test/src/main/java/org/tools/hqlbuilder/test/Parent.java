package org.tools.hqlbuilder.test;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.hibernate.annotations.AccessType;

@MappedSuperclass
@AccessType("field")
public abstract class Parent implements Serializable {
    private static final long serialVersionUID = -2958424236876731630L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version;

    final public Long getId() {
        return id;
    }

    final public void setId(Long id) {
        this.id = id;
    }

    final public Integer getVersion() {
        return version;
    }

    final public void setVersion(Integer version) {
        this.version = version;
    }
}