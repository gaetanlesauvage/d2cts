package org.display.panes;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThreadSafeTableModel extends AbstractTableModel implements Runnable {
		private static final long serialVersionUID = -8776420526389773428L;

		ReadWriteLock lock;
		AtomicIntegerArray dim;
		Object[][] data;
		List<Object> colNames;
		ConcurrentLinkedQueue<TableModelEvent> events;
		AtomicBoolean dispatching;

		public ThreadSafeTableModel() {
			lock = new ReentrantReadWriteLock();
			//lock.writeLock().lock();
			events = new ConcurrentLinkedQueue<TableModelEvent>();
			colNames = Collections.synchronizedList(new ArrayList<Object>());
			dim = new AtomicIntegerArray(new int[] { 0, 0 });
			dispatching = new AtomicBoolean(false);
			data = new Object[0][0];
			//lock.writeLock().unlock();
		}
		
		public ThreadSafeTableModel(Object[] colNames){
			this();
			//lock.writeLock().lock();
			dim.set(0, colNames.length);
			for(Object o : colNames){
				this.colNames.add(o);
			}
			//lock.writeLock().unlock();
			fireTableStructureChanged();
		}
		
		@Override
		public int getColumnCount() {
			return dim.get(0);
		}

		@Override
		public int getRowCount() {
			return dim.get(1);
		}
		
		@Override
		public Class<?> getColumnClass(int column){
			  if (column >= 0 && column <= getColumnCount())
		          return colNames.get(column).getClass();
		        else
		          return Object.class;
		}

	    /**
	     * Returns the column name.
	     *
	     * @return a name for this column using the string value of the
	     * appropriate member in <code>columnIdentifiers</code>.
	     * If <code>columnIdentifiers</code> does not have an entry
	     * for this index, returns the default
	     * name provided by the superclass.
	     */
		@Override
	    public String getColumnName(int column) {
	        Object id = null;
	        // This test is to cover the case when
	        // getColumnCount has been subclassed by mistake ...
	        if (column < dim.get(0) && (column >= 0)) {
	            id = colNames.get(column);
	        }
	        return (id == null) ? super.getColumnName(column)
	                            : id.toString();
	    }
		@Override
		public boolean isCellEditable(int iRowIndex, int iColumnIndex) { 
			return false;
		}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			//System.err.print("GET VALUE @"+rowIndex+","+columnIndex+" ... ");
			Object obj;

			lock.readLock().lock();
			obj = data[rowIndex][columnIndex];
			lock.readLock().unlock();
			//System.err.println("GETTED !");
			return obj;
		}
		
		public void addRow(Object[] values){
			lock.writeLock().lock();
			int rowCount = dim.get(1);
			for(int i=0; i<values.length; i++){
				setValueAt(values[i], rowCount, i);
			}
			lock.writeLock().unlock();
		}
		
		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			//System.err.print("SET VALUE "+value+" ... ");
				
			lock.writeLock().lock();

			if (columnIndex >= dim.get(0)) {
				for (int i = 0; i < dim.get(1); i++)
					data[i] = Arrays.copyOf(data[i], columnIndex + 1);

				dim.set(0, columnIndex + 1);
			}

			if (rowIndex >= dim.get(1)) {
				Object[][] tmp = new Object[rowIndex + 1][dim.get(0)];
				
				for (int i = 0; i < dim.get(1); i++)
					tmp[i] = data[i];

				data = tmp;
				dim.set(1, rowIndex + 1);
			}

			data[rowIndex][columnIndex] = value;

			lock.writeLock().unlock();
			//System.err.println("SETTED !");
			fireTableDataChanged();
		}

		public void run() {
			TableModelEvent e;

			while ((e = events.poll()) != null) 
				fireTableChanged(e);
			
			dispatching.set(false);
		}
		
		@Override
		public void fireTableChanged(TableModelEvent e) {
			if (SwingUtilities.isEventDispatchThread())
				super.fireTableChanged(e);
			else {
				events.add(e);
				if (dispatching.compareAndSet(false, true))
					SwingUtilities.invokeLater(this);
			}
		}
	}