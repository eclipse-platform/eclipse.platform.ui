
package org.eclipse.ant.internal.ui.preferences;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class ClasspathModel {
	protected List elements = new ArrayList();
	
	protected Object[] getEntries() {
		return elements.toArray();
	}

	/**
	 * @return
	 */
	public boolean hasEntries() {
		return !elements.isEmpty();
	}
	
	public void addEntry(Object entry) {
		if (entry instanceof GlobalClasspathEntries) {
			elements.add(entry);
		} else {
			ClasspathEntry newEntry= createEntry(entry, null);
			Iterator entries= elements.iterator();
			while (entries.hasNext()) {
				Object element = entries.next();
				if (element instanceof GlobalClasspathEntries) {
					if(((GlobalClasspathEntries)element).contains(newEntry)) {
						return;
					}
				} else if (element.equals(newEntry)) {
					return;
				}
			}
			elements.add(newEntry);
		}
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
		elements.removeAll(Arrays.asList(entries));
	}

	/**
	 * @param urls
	 */
	public void setGlobalClasspath(URL[] urls) {
		String name= "Global Ant Classpath";
		createGlobalEntry(urls, name);	
	}

	private void createGlobalEntry(URL[] urls, String name) {
		GlobalClasspathEntries global= new GlobalClasspathEntries(name);
		for (int i = 0; i < urls.length; i++) {
			URL url = urls[i];
			global.addEntry(new ClasspathEntry(url, global));
		}
		
		addEntry(global);
	}

	/**
	 * @param urls
	 */
	public void setGlobalUserClasspath(URL[] urls) {
		String name= "Global User Ant Classpath";
		createGlobalEntry(urls, name);	
	}

	/**
	 * 
	 */
	public void setToDefaults() {
		// TODO Auto-generated method stub
		
	}
}
