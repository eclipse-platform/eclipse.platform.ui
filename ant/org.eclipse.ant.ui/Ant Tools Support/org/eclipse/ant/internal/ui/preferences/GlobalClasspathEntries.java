
package org.eclipse.ant.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

public class GlobalClasspathEntries {
	protected List elements = new ArrayList();
	private String name;
	
	/**
	 * 
	 */
	public GlobalClasspathEntries(String name) {
		this.name= name;
	}
		
	/**
	 * @return
	 */
	public Object[] getEntries() {
		return elements.toArray();
	}

	/**
	 * @return
	 */
	public boolean hasEntries() {
		return !elements.isEmpty();
	}
	
	public void addEntry(ClasspathEntry entry) {
		elements.add(entry);
	}
	
	public void removeEntry(ClasspathEntry entry) {
		elements.remove(entry);
	}
	
	public boolean contains(ClasspathEntry entry) {
		return elements.contains(entry);
	}
	
	public String toString() {
		return name;
	}
}
