
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
	private GlobalClasspathEntries globalEntry;
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
			if (!elements.contains(entry)) {
				elements.add(entry);
				return entry;
			}
			return null;
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
				parent= globalEntry;
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
		if (parent != null) {
			((GlobalClasspathEntries)parent).addEntry(newEntry);
		} else {
			elements.add(newEntry);
		}
		return newEntry;		
	}
	
	public Object[] getURLEntries(int entryType) {
		Object[] classpathEntries= null;
		switch (entryType) {
			case GLOBAL :
				 classpathEntries= globalEntry.getEntries();
				break;
			case GLOBAL_USER :
				classpathEntries= userGlobalEntry.getEntries();
				break;
			default :
				return null;
		}
		Object[] entries= new Object[classpathEntries.length];
		Object entry;
		for (int i = 0; i < classpathEntries.length; i++) {
			ClasspathEntry classpathEntry = (ClasspathEntry) classpathEntries[i];
			entry= classpathEntry.getURL();
			if (entry == null) {
				entry= classpathEntry.getVariableString();
			}
			entries[i]= entry;
		}
		return entries;
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
		globalEntry.removeAll();
		userGlobalEntry.removeAll();
	}
	
	public void removeAll(Object[] entries) {
		
		for (int i = 0; i < entries.length; i++) {
			Object object = entries[i];
			if (object instanceof ClasspathEntry) {
				Object parent= ((ClasspathEntry)object).getParent();
				if (parent instanceof GlobalClasspathEntries) {
					((GlobalClasspathEntries)parent).removeEntry((ClasspathEntry) object);
				} else if (parent instanceof ClasspathModel) {
					((ClasspathModel)parent).remove(object);
				}
			}
		}
	}

	/**
	 * @param urls
	 */
	public void setGlobalClasspath(URL[] urls) {
		if (globalEntry == null) {
			String name= "Global Ant Classpath";
			globalEntry= createGlobalEntry(urls, name);
		} else {
			globalEntry.removeAll();
			for (int i = 0; i < urls.length; i++) {
				URL url = urls[i];
				globalEntry.addEntry(new ClasspathEntry(url, globalEntry));
			}
		}
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
		if (userGlobalEntry == null) {
			String name= "Global User Ant Classpath";
			userGlobalEntry= createGlobalEntry(urls, name);
		} else {
			userGlobalEntry.removeAll();
			for (int i = 0; i < urls.length; i++) {
				URL url = urls[i];
				userGlobalEntry.addEntry(new ClasspathEntry(url, userGlobalEntry));
			}
		}
	}
}
