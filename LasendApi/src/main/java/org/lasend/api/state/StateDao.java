package org.lasend.api.state;

import org.lasend.api.model.GetIdAble;

import java.util.List;

public interface StateDao<T extends GetIdAble> {
    void update(T obj);

    void delete(T obj);

    void deleteAll();

    T getById(String id);

    List<T> getAll();

    boolean contains(String id);
}
