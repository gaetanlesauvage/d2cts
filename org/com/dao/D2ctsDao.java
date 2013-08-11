package org.com.dao;

import java.sql.SQLException;

public interface D2ctsDao<E> extends Iterable<E>{
	public void close() throws SQLException;
	public void load() throws SQLException;
	public int insert(E bean) throws SQLException;
	public String getLoadQuery();
	public int size();
}
