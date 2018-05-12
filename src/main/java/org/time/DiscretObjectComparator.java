package org.time;

import java.util.Comparator;

/**
 * Comparator used to order discret objects threads. 
 * @author gaetan
 *
 */
public class DiscretObjectComparator implements Comparator<DiscretObject>{
	@Override
	public int compare(DiscretObject thread1, DiscretObject thread2){
		return thread1.getDiscretPriority() - thread2.getDiscretPriority();
	}
}
