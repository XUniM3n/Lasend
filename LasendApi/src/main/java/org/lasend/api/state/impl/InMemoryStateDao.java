package org.lasend.api.state.impl;

import org.lasend.api.model.GetIdAble;
import org.lasend.api.state.StateDao;

import java.util.ArrayList;
import java.util.List;

public class InMemoryStateDao<T extends GetIdAble> implements StateDao<T> {
    protected final List<T> list;

    public InMemoryStateDao() {
        list = new ArrayList<>();
    }

    @Override
    public void update(T obj) {
        synchronized (list) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId().equals(obj.getId())) {
                    list.set(i, obj);
                }
            }
            list.add(obj);
        }
    }

    @Override
    public void delete(T obj) {
        synchronized (list) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId().equals(obj.getId())) {
                    list.remove(i);
                    break;
                }
            }
        }
    }

    @Override
    public void deleteAll() {
        synchronized (list) {
            list.clear();
        }
    }

    @Override
    public T getById(String id) {
        synchronized (list) {
            for (T obj : list) {
                if (obj.getId().equals(id)) {
                    return obj;
                }
            }
        }

        return null;
    }

    @Override
    public List<T> getAll() {
        return list;
    }

    @Override
    public boolean contains(String id) {
        return getIndexById(id) != -1;
    }

    private int getIndexById(String id) {
        synchronized (list) {
            for (int i = 0; i < list.size(); i++) {
                T obj = list.get(i);
                if (obj.getId().equals(id)) {
                    return i;
                }
            }
        }

        return -1;
    }
}
