package org.tools.hqlbuilder.webservice.wicket.forms;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.StringUtils;

public class FormElementSettings implements Serializable, Cloneable {
    private static final long serialVersionUID = -2716372832273804363L;

    protected boolean required = false;

    public FormElementSettings() {
        super();
    }

    public FormElementSettings(FormElementSettings other) {
        try {
            BeanUtils.copyProperties(this, other);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(new CloneNotSupportedException(String.valueOf(ex)));
        }
    }

    public FormElementSettings(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return this.required;
    }

    public FormElementSettings setRequired(boolean required) {
        this.required = required;
        return this;
    }

    @Override
    public FormElementSettings clone() {
        try {
            return getClass().cast(BeanUtils.cloneBean(this));
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException ex) {
            throw new RuntimeException(new CloneNotSupportedException(String.valueOf(ex)));
        }
    }

    @Override
    public String toString() {
        try {
            StringBuilder builder = new StringBuilder("{");
            PropertyUtilsBean propertyUtils = BeanUtilsBean.getInstance().getPropertyUtils();
            for (PropertyDescriptor desc : propertyUtils.getPropertyDescriptors(this)) {
                String name = desc.getName();
                if ("class".equals(name) || skipForExport(name)) {
                    continue;
                }
                Object value = propertyUtils.getSimpleProperty(this, name);
                if (value == null) {
                    continue;
                }
                if (String.class.equals(desc.getPropertyType())) {
                    if (StringUtils.isBlank(String.class.cast(value))) {
                        continue;
                    }
                    builder.append(name).append(":").append("\"").append(value).append("\"").append(",");
                } else if (Enum.class.isAssignableFrom(desc.getPropertyType())) {
                    builder.append(name).append(":").append("\"").append(value).append("\"").append(",");
                } else {
                    builder.append(name).append(":").append(value).append(",");
                }
            }
            if (builder.length() == 1) {
                return "{}";
            }
            return builder.deleteCharAt(builder.length() - 1).append("}").toString();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected boolean skipForExport(String propertyName) {
        return "required".equals(propertyName);
    }
}