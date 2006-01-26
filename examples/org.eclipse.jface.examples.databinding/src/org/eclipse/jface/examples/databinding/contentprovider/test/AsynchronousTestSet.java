package org.eclipse.jface.examples.databinding.contentprovider.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.jface.databinding.AbstractUpdatableSet;
import org.eclipse.jface.internal.databinding.swt.SWTUtil;
import org.eclipse.swt.widgets.Display;

/**
 * Test set that simulates asynchronously computed elements. The elements
 * of the set are randomly generated Integers. Whenever
 * the "recompute" method is called, the set will spin off a job that
 * sleeps for a period of time and then randomly adds and removes elements
 * from the set.
 * 
 * <p>
 * This simulates a set that wraps a database query or network communication. 
 * These would follow the same pattern (report the set as "stale", perform some
 * slow operation, then make changes to the set).
 * </p>
 * 
 * @since 3.2
 */
public class AsynchronousTestSet extends AbstractUpdatableSet {

	private Set elements = new HashSet();
	private static Random randomNumberGenerator = new Random();
	private Display display;
	private boolean stale = false;
	
	/**
	 * Average number of elements to add or remove
	 */ 
	private static final int AVERAGE_DELTA = 4;
	
	/** 
	 * Average "computation" time -- time taken to do the simulated work (ms)
	 */ 
	private static final int AVERAGE_BUSY_TIME = 1000;
	
	/**
	 * List of all undisposed AsynchronousTestSet instances. Used for the recomputeAll method.
	 */
	private static List allSets = new ArrayList();
	
	public AsynchronousTestSet() {
		display = Display.getCurrent();
		if (display == null) {
			throw new IllegalStateException("This object can only be created in the UI thread");
		}
		recompute();
	}
	
	protected Collection computeElements() {
		return elements;
	}
	
	protected void firstListenerAdded() {
		super.firstListenerAdded();
		allSets.add(this);
	}
	
	protected void lastListenerRemoved() {
		allSets.remove(this);
		super.lastListenerRemoved();
	}
	
	public static void recomputeAll() {
		for (Iterator iter = allSets.iterator(); iter.hasNext();) {
			AsynchronousTestSet next = (AsynchronousTestSet) iter.next();
			
			next.recompute();
		}
	}
	
	public void remove(Collection toRemove) {
		HashSet rem = new HashSet();
		rem.addAll(toRemove);
		rem.retainAll(elements);
		
		elements.removeAll(rem);
		fireRemoved(rem);
	}
	
	public boolean isStale() {
		return stale;
	}
	
	public void recompute() {
		if (!isStale()) {
			setStale(true);
			final int sleepTime = (int)(randomNumberGenerator.nextDouble() * (double)(AVERAGE_BUSY_TIME * 2));
			Thread newThread = new Thread(new Runnable() {
				public void run() {
					
					// Simulate work by sleeping
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
					}

					
					// Add and remove some elements -- important: fire all events in the UI thread
					SWTUtil.greedyExec(display, new Runnable() {
						public void run() {
							final HashSet toAdd = new HashSet();
							final HashSet toRemove = new HashSet();
							
							// Compute elements to add and remove (basically just fills the toAdd 
							// and toRemove sets with random elements)
							int delta = (randomNumberGenerator.nextInt(AVERAGE_DELTA * 4) - AVERAGE_DELTA * 2);
							int extraAdds = randomNumberGenerator.nextInt(AVERAGE_DELTA);
							int addCount = delta + extraAdds;
							int removeCount = -delta + extraAdds;
							
							if (addCount > 0) {
								for (int i = 0; i < addCount; i++) {
									toAdd.add(new Integer(randomNumberGenerator.nextInt(20)));
								}
							} 
							
							if (removeCount > 0) {
								Iterator oldElements = elements.iterator();
								for (int i = 0; i < removeCount && oldElements.hasNext(); i++) {
									toRemove.add(oldElements.next());
								}
							}
							
							setStale(false);
							
							toAdd.removeAll(elements);
							elements.addAll(toAdd);
							fireAdded(toAdd);
							elements.removeAll(toRemove);
							fireRemoved(toRemove);
						}
					});
				}
			});
			
			newThread.start();
		}
	}

	private void setStale(boolean b) {
		if (!stale == b) {
			stale = b;
			fireStale(b);
		}
	}
}
