
package org.eclipse.ant.internal.ui.preferences;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** 
 * This class is a work in progress
 *
 */
public class ClasspathModel {
	
	public static final int GLOBAL= 0;
	public static final int GLOBAL_USER= 1;
	
	protected List elements = new ArrayList();
	private GlobalClasspathEntries antGlobalEntry;
	private GlobalClasspathEntries userGlobalEntry;
	
	protected Object[] getEntries() {
		return elements.toArray();
	}

	/**
	 * @return
	 */
	public boolean hasEntries() {
		return !elements.isEmpty();
	}
	
	public Object addEntry(Object entry) {
		if (entry instanceof GlobalClasspathEntries) {
			elements.add(entry);
			return entry;
		} else {
			ClasspathEntry newEntry= createEntry(entry, null);
			Iterator entries= elements.iterator();
			while (entries.hasNext()) {
				Object element = entries.next();
				if (element instanceof GlobalClasspathEntries) {
					if(((GlobalClasspathEntries)element).contains(newEntry)) {
						return null;
					}
				} else if (element.equals(newEntry)) {
					return null;
				}
			}
			elements.add(newEntry);
			return newEntry;
		}
	}
	
	public Object addEntry(int entryType, Object entry) {
		Object parent= null;
		switch (entryType) {
			case GLOBAL :
				parent= antGlobalEntry;
				break;
			case GLOBAL_USER :
				parent= userGlobalEntry;
				break;
			default :
				break;
		}
			
		ClasspathEntry newEntry= createEntry(entry, parent);
		Iterator entries= elements.iterator();
		while (entries.hasNext()) {
			Object element = entries.next();
			if (element instanceof GlobalClasspathEntries) {
				if(((GlobalClasspathEntries)element).contains(newEntry)) {
					return null;
				}
			} else if (element.equals(newEntry)) {
				return null;
			}
		}
		elements.add(newEntry);
		return newEntry;		
	}
	
	public void remove(Object entry) {
		elements.remove(entry);
	}
	
	public ClasspathEntry createEntry(Object entry, Object parent) {
		if (parent == null) {
			parent= this;
		} 
		return new ClasspathEntry(entry, parent);
	}

	public void removeAll() {
		elements.clear();
	}
	
	public void removeAll(Object[] entries) {
		
		for (int i = 0; i < entries.length; i++) {
			Object object = entries[i];
			if (object instanceof ClasspathEntry) {
				Object parent= ((ClasspathEntry)object).getParent();
				if (parent != null) {
					((GlobalClasspathEntries)parent).removeEntry((ClasspathEntry) object);
					continue;
				} 
			}
			remove(object);
		}
		
	}

	/**
	 * @param urls
	 */
	public void setGlobalClasspath(URL[] urls) {
		String name= "Global Ant Classpath";
		antGlobalEntry= createGlobalEntry(urls, name);	
	}

	private GlobalClasspathEntries createGlobalEntry(URL[] urls, String name) {
		GlobalClasspathEntries global= new GlobalClasspathEntries(name);
		for (int i = 0; i < urls.length; i++) {
			URL url = urls[i];
			global.addEntry(new ClasspathEntry(url, global));
		}
		
		addEntry(global);
		return global;
	}

	/**
	 * @param urls
	 */
	public void setGlobalUserClasspath(URL[] urls) {
		String name= "Global User Ant Classpath";
		userGlobalEntry= createGlobalEntry(urls, name);	
	}

	/**
	 * 
	 */
	public void setToDefaults() {
		// TODO Auto-generated method stub
		
	}
}
