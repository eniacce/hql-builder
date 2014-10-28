package org.tools.hqlbuilder.webservice.wicket.tables;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

@SuppressWarnings("serial")
public abstract class DefaultDataProvider<T extends Serializable> implements DataProvider<T>, ISortState<String> {
    protected Map<String, SortOrder> sort = new LinkedHashMap<String, SortOrder>();

    protected int rowsPerPage = 10;

    protected boolean add = true;

    protected boolean delete = true;

    protected boolean edit = true;

    protected boolean stateless = false;

    protected int ajaxRefreshSeconds = 60;

    protected String ajaxRefreshUrl;

    protected String ajaxRefreshMethod = "GET";

    protected String idProperty;

    @Override
    public IModel<T> model(T object) {
        return Model.of(object);
    }

    @Override
    public void detach() {
        //
    }

    @Override
    public Iterator<? extends T> iterator(long first, long count) {
        return select(first, count, sort);
    }

    protected abstract Iterator<T> select(long first, long count, Map<String, SortOrder> sorting);

    @Override
    public ISortState<String> getSortState() {
        return this;
    }

    @Override
    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public DefaultDataProvider<T> setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
        return this;
    }

    @Override
    public void setPropertySortOrder(String property, SortOrder sortOrder) {
        sort.put(property, sortOrder);
    }

    @Override
    public SortOrder getPropertySortOrder(String property) {
        SortOrder sortOrder = sort.get(property);
        if (sortOrder == null) {
            sortOrder = SortOrder.NONE;
        }
        return sortOrder;
    }

    @Override
    public String getAjaxRefreshUrl() {
        return this.ajaxRefreshUrl;
    }

    @Override
    public String getAjaxRefreshMethod() {
        return this.ajaxRefreshMethod;
    }

    @Override
    public boolean canAdd() {
        return this.add;
    }

    @Override
    public boolean canDelete() {
        return this.delete;
    }

    @Override
    public boolean canEdit() {
        return this.edit;
    }

    @Override
    public boolean isStateless() {
        return this.stateless;
    }

    public DefaultDataProvider<T> setAdd(boolean add) {
        this.add = add;
        return this;
    }

    public DefaultDataProvider<T> setAjaxRefreshUrl(String ajaxRefreshUrl) {
        this.ajaxRefreshUrl = ajaxRefreshUrl;
        return this;
    }

    public DefaultDataProvider<T> setAjaxRefreshMethod(String ajaxRefreshMethod) {
        this.ajaxRefreshMethod = ajaxRefreshMethod;
        return this;
    }

    public DefaultDataProvider<T> setDelete(boolean delete) {
        this.delete = delete;
        return this;
    }

    public DefaultDataProvider<T> setEdit(boolean edit) {
        this.edit = edit;
        return this;
    }

    public DefaultDataProvider<T> setStateless(boolean stateless) {
        this.stateless = stateless;
        return this;
    }

    @Override
    public int getAjaxRefreshSeconds() {
        return this.ajaxRefreshSeconds;
    }

    public DefaultDataProvider<T> setAjaxRefreshSeconds(int ajaxRefreshSeconds) {
        this.ajaxRefreshSeconds = ajaxRefreshSeconds;
        return this;
    }

    public Map<String, SortOrder> getSort() {
        return this.sort;
    }

    public void setSort(Map<String, SortOrder> sort) {
        this.sort = sort;
    }

    public boolean isAdd() {
        return this.add;
    }

    public boolean isDelete() {
        return this.delete;
    }

    public boolean isEdit() {
        return this.edit;
    }

    @Override
    public String getId(IModel<T> model) {
        return getIdProperty() == null ? null : new PropertyModel<String>(model, getIdProperty()).getObject();
    }

    @Override
    public String getIdProperty() {
        return idProperty;
    }

    public DefaultDataProvider<T> setIdProperty(String idProperty) {
        this.idProperty = idProperty;
        return this;
    }
}