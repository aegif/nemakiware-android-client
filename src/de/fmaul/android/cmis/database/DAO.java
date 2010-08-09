package de.fmaul.android.cmis.database;

import java.util.List;

public interface DAO<T> {
	T findById(long id);

	List<T> findAll();

	boolean delete(long id);

	/*
	boolean update(T object);

	long insert(T object);
	*/
}
