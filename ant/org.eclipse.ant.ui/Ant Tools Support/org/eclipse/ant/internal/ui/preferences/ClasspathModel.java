/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.preferences;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.internal.ui.model.AntUtil;

/** 
 * This class is a work in progress
 *
 */
public class ClasspathModel extends AbstractClasspathEntry {
	
	public static final int GLOBAL= 0;
	public static final int GLOBAL_USER= 1;
	public static final int USER= 2;
	
	private GlobalClasspathEntries antHomeGlobalEntry;
	private GlobalClasspathEntries userGlobalEntry;
	
	public Object addEntry(Object entry) {
		if (entry instanceof GlobalClasspathEntries) {
			if (!childEntries.contains(entry)) {
				childEntries.add(entry);
				return entry;
			}
			return null;
		} else {
			ClasspathEntry newEntry= createEntry(entry, null);
			Iterator entries= childEntries.iterator();
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
			childEntries.add(newEntry);
			return newEntry;
		}
	}
	
	public Object addEntry(int entryType, Object entry) {
		IClasspathEntry entryParent= null;
		switch (entryType) {
			case GLOBAL :
				if (antHomeGlobalEntry == null) {
					String name= AntPreferencesMessages.getString("ClasspathModel.2"); //$NON-NLS-1$
					antHomeGlobalEntry= createGlobalEntry(new URL[0], name, false, true);
				}
				entryParent= antHomeGlobalEntry;
				break;
			case GLOBAL_USER :
				if (userGlobalEntry == null) {
					String name= AntPreferencesMessages.getString("ClasspathModel.3"); //$NON-NLS-1$
					userGlobalEntry= createGlobalEntry(new URL[0], name, true, true);
				}
				entryParent= userGlobalEntry;
				break;
			default :
				break;
		}
			
		ClasspathEntry newEntry= createEntry(entry, entryParent);
		Iterator entries= childEntries.iterator();
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
		if (entryParent != null) {
			((GlobalClasspathEntries)entryParent).addEntry(newEntry);
		} else {
			childEntries.add(newEntry);
		}
		return newEntry;		
	}
	
	public Object[] getURLEntries(int entryType) {
		Object[] classpathEntries= null;
		switch (entryType) {
			case GLOBAL :
				if (antHomeGlobalEntry != null) {
					classpathEntries= antHomeGlobalEntry.getEntries();
				}
				break;
			case GLOBAL_USER :
				if (userGlobalEntry != null) {
					classpathEntries= userGlobalEntry.getEntries();
				}
				break;
			case USER : 
				classpathEntries= getUserEntries();
				break;
			default :
				return null;
		}
		if (classpathEntries == null) {
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
		childEntries.remove(entry);
		if (entry == userGlobalEntry) {
			userGlobalEntry= null;
		}
	}
	
	public ClasspathEntry createEntry(Object entry, IClasspathEntry entryParent) {
		if (entryParent == null) {
			entryParent= this;
		} 
		return new ClasspathEntry(entry, entryParent);
	}

	public void removeAll() {
		if (antHomeGlobalEntry != null) {
			antHomeGlobalEntry.removeAll();
		} 
		if (userGlobalEntry != null) {
			userGlobalEntry.removeAll();
		}
	}
	
	public void removeAll(int entryType) {
		switch (entryType) {
			case GLOBAL :
				if (antHomeGlobalEntry != null) {
					antHomeGlobalEntry.removeAll();
				}
				break;
			case GLOBAL_USER :
				if (userGlobalEntry != null) {
					userGlobalEntry.removeAll();
				}
				break;
			default :
				break;
		}
	}
	
	public void removeAll(Object[] entries) {
		
		for (int i = 0; i < entries.length; i++) {
			Object object = entries[i];
			if (object instanceof ClasspathEntry) {
				IClasspathEntry entryParent= ((ClasspathEntry)object).getParent();
				if (entryParent instanceof GlobalClasspathEntries) {
					((GlobalClasspathEntries)entryParent).removeEntry((ClasspathEntry) object);
				} else {
					remove(object);
				}
			} else {
				remove(object);
			}
		}
	}

	/**
	 * @param urls
	 */
	public void setAntHomeEntries(URL[] urls) {
		if (antHomeGlobalEntry == null) {
			String name= AntPreferencesMessages.getString("ClasspathModel.2"); //$NON-NLS-1$
			antHomeGlobalEntry= createGlobalEntry(urls, name, false, true);
		} else {
			antHomeGlobalEntry.removeAll();
			for (int i = 0; i < urls.length; i++) {
				URL url = urls[i];
				antHomeGlobalEntry.addEntry(new ClasspathEntry(url, antHomeGlobalEntry));
			}
		}
	}

	private GlobalClasspathEntries createGlobalEntry(URL[] urls, String name, boolean canBeRemoved, boolean addEntry) {
		
		GlobalClasspathEntries global= new GlobalClasspathEntries(name, this, canBeRemoved);
		
		for (int i = 0; i < urls.length; i++) {
			URL url = urls[i];
			global.addEntry(new ClasspathEntry(url, global));
		}
		
		if (addEntry) {
			addEntry(global);
		}
		return global;
	}

	/**
	 * @param urls
	 */
	public void setGlobalEntries(URL[] urls) {
		if (userGlobalEntry == null) {
			String name= AntPreferencesMessages.getString("ClasspathModel.3"); //$NON-NLS-1$
			userGlobalEntry= createGlobalEntry(urls, name, true, true);
		} else {
			userGlobalEntry.removeAll();
			for (int i = 0; i < urls.length; i++) {
				URL url = urls[i];
				userGlobalEntry.addEntry(new ClasspathEntry(url, userGlobalEntry));
			}
		}
	}
	
	private IClasspathEntry[] getUserEntries() {
		List userEntries= new ArrayList(childEntries.size());
		Iterator itr= childEntries.iterator();
		while (itr.hasNext()) {
			IClasspathEntry element = (IClasspathEntry) itr.next();
			if (element instanceof GlobalClasspathEntries) {
				continue;
			}
			userEntries.add(element);
		}
		return (IClasspathEntry[])userEntries.toArray(new IClasspathEntry[userEntries.size()]);
	}
	
	public String serializeClasspath() {
		Iterator itr= childEntries.iterator();
		StringBuffer buff= new StringBuffer();
		while (itr.hasNext()) {
			IClasspathEntry element = (IClasspathEntry) itr.next();
			if (element instanceof GlobalClasspathEntries) {
				if (element == antHomeGlobalEntry) {
					buff.append(AntUtil.ANT_GLOBAL_CLASSPATH_PLACEHOLDER);
				} else {
					buff.append(AntUtil.ANT_GLOBAL_USER_CLASSPATH_PLACEHOLDER);
				}
			} else {
				buff.append(element.toString());
			}
			buff.append(AntUtil.ATTRIBUTE_SEPARATOR);
		}
		if (buff.length() > 0) {
			return buff.substring(0, buff.length() - 1);
		} else {
			return ""; //$NON-NLS-1$
		}
	}
	
	public ClasspathModel(String serializedClasspath, boolean customAntHome) {
		StringTokenizer tokenizer= new StringTokenizer(serializedClasspath, AntUtil.ATTRIBUTE_SEPARATOR);
		
		while (tokenizer.hasMoreTokens()) {
			String string = tokenizer.nextToken().trim();
			if (string.equals(AntUtil.ANT_GLOBAL_CLASSPATH_PLACEHOLDER)) {
				URL[] antHomeURLS= new URL[0];
				if (!customAntHome) {
					antHomeURLS= AntCorePlugin.getPlugin().getPreferences().getAntHomeClasspathEntries();
				} 
				setAntHomeEntries(antHomeURLS);
			} else if (string.equals(AntUtil.ANT_GLOBAL_USER_CLASSPATH_PLACEHOLDER)) {
				setGlobalEntries(AntCorePlugin.getPlugin().getPreferences().getAdditionalClasspathEntries());
			} else {
				Object entry= null;
				if (string.charAt(0) == '*') {
					//old customclasspath
					string= string.substring(1);
				}
				try {
					entry=  new URL("file:" + string); //$NON-NLS-1$
				} catch (MalformedURLException e) {
					entry= string;
				}
				addEntry(entry);
			}
		}
		
	}
	
	public ClasspathModel() {
		super();
	}

	/**
	 * @return
	 */
	public Object[] getRemovedGlobalEntries() {
		if (userGlobalEntry == null) {
			String name= AntPreferencesMessages.getString("ClasspathModel.3"); //$NON-NLS-1$
			return new Object[] {createGlobalEntry(new URL[] {}, name, true, false)};
		}
		return new Object[] {};
	}

	/**
	 * @return
	 */
	public Object getAntHomeEntry() {
		return antHomeGlobalEntry;
	}
}