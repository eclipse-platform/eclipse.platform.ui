/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.servlet;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.webapp.*;
import org.eclipse.help.internal.workingset.*;

/**
 * The Infocenter working set manager stores help working sets. Working sets are
 * persisted in client cookies whenever one is added or removed.
 * 
 * @since 3.0
 */
public class InfocenterWorkingSetManager implements IHelpWorkingSetManager {
	private static final String COOKIE_NAME = "wset"; //$NON-NLS-1$
	private static final int MAX_COOKIES = 15;
	private HttpServletRequest request;
	private HttpServletResponse response;

	// Current working set , empty string means all documents
	private String currentWorkingSet = ""; //$NON-NLS-1$
	private SortedSet workingSets = new TreeSet(new WorkingSetComparator());
	private String locale;
	private AdaptableTocsArray root;

	/**
	 * Constructor
	 * 
	 * @param locale
	 */
	public InfocenterWorkingSetManager(HttpServletRequest request,
			HttpServletResponse response, String locale) {
		this.request = request;
		this.response = response;
		this.locale = locale;
		restoreState();
	}

	public AdaptableTocsArray getRoot() {
		if (root == null)
			root = new AdaptableTocsArray(HelpPlugin.getTocManager().getTocs(
					locale));
		return root;
	}

	/**
	 * Adds a new working set and saves it
	 */
	public void addWorkingSet(WorkingSet workingSet) throws IOException {
		if (workingSet == null || workingSets.contains(workingSet))
			return;
		workingSets.add(workingSet);
		saveState();
	}

	/**
	 * Creates a new working set
	 */
	public WorkingSet createWorkingSet(String name,
			AdaptableHelpResource[] elements) {
		return new WorkingSet(name, elements);
	}

	/**
	 * Returns a working set by name
	 *  
	 */
	public WorkingSet getWorkingSet(String name) {
		if (name == null || workingSets == null)
			return null;

		Iterator iter = workingSets.iterator();
		while (iter.hasNext()) {
			WorkingSet workingSet = (WorkingSet) iter.next();
			if (name.equals(workingSet.getName()))
				return workingSet;
		}
		return null;
	}

	/**
	 * Implements IWorkingSetManager.
	 * 
	 * @see org.eclipse.help.internal.workingset.IHelpWorkingSetManager#getWorkingSets()
	 */
	public WorkingSet[] getWorkingSets() {
		return (WorkingSet[]) workingSets.toArray(new WorkingSet[workingSets
				.size()]);
	}

	/**
	 * Removes specified working set
	 */
	public void removeWorkingSet(WorkingSet workingSet) {
		workingSets.remove(workingSet);
		try {
			saveState();
		} catch (IOException ioe) {
		}
	}

	private void restoreState() {
		String data = CookieUtil.restoreString(COOKIE_NAME, request);
		if (data == null) {
			return;
		}

		String[] values = data.split("\\|", -1); //$NON-NLS-1$
		if (values.length < 1) {
			return;
		}
		currentWorkingSet = URLCoder.decode(values[0] /* , "UTF8" */
		);
		i : for (int i = 1; i < values.length; i++) {
			String[] nameAndHrefs = values[i].split("&", -1); //$NON-NLS-1$

			String name = URLCoder.decode(nameAndHrefs[0] /* , "UTF8" */
			);

			AdaptableHelpResource[] elements = new AdaptableHelpResource[nameAndHrefs.length - 1];
			// for each href (working set resource)
			for (int e = 0; e < nameAndHrefs.length - 1; e++) {
				int h = e + 1;
				elements[e] = getAdaptableToc(URLCoder.decode(nameAndHrefs[h]
				/* , "UTF8" */
				));
				if (elements[e] == null) {
					elements[e] = getAdaptableTopic(URLCoder
							.decode(nameAndHrefs[h]
							/* , "UTF8" */
							));
				}
				if (elements[e] == null) {
					// working set cannot be restored
					continue i;
				}
			}
			WorkingSet ws = createWorkingSet(name, elements);
			workingSets.add(ws);
		}
	}

	/***************************************************************************
	 * Persists all working sets. Should only be called by the webapp working
	 * set dialog. Saves the working sets in the persistence store (cookie)
	 * format: curentWorkingSetName|name1&href11&href12|name2&href22
	 */
	private void saveState() throws IOException {
		StringBuffer data = new StringBuffer();
		data.append(URLCoder.encode(currentWorkingSet /* , "UTF8" */
		));

		for (Iterator i = workingSets.iterator(); i.hasNext();) {
			data.append('|');
			WorkingSet ws = (WorkingSet) i.next();
			data.append(URLCoder.encode(ws.getName() /* , "UTF8" */
			));

			AdaptableHelpResource[] resources = ws.getElements();
			for (int j = 0; j < resources.length; j++) {
				data.append('&');

				IAdaptable parent = resources[j].getParent();
				if (parent == getRoot()) {
					// saving toc
					data.append(URLCoder.encode(resources[j].getHref()
					/* , "UTF8" */
					));
				} else {
					// saving topic as tochref_topic#_
					AdaptableToc toc = (AdaptableToc) parent;
					AdaptableHelpResource[] siblings = (toc).getChildren();
					for (int t = 0; t < siblings.length; t++) {
						if (siblings[t] == resources[j]) {
							data.append(URLCoder.encode(toc.getHref()
							/* , "UTF8" */
							));
							data.append('_');
							data.append(t);
							data.append('_');
							break;
						}
					}
				}
			}
		}

		try {
			CookieUtil.saveString(COOKIE_NAME, data.toString(), MAX_COOKIES,
					request, response);
		} catch (IOException ioe) {
			if (HelpWebappPlugin.DEBUG_WORKINGSETS) {
				System.out
						.println("InfocenterWorkingSetManager.saveState(): Too much data to save: " //$NON-NLS-1$
								+ data.toString());
			}
			throw ioe;
		}
	}

	/**
	 * *
	 * 
	 * @param changedWorkingSet
	 *            the working set that has changed
	 */
	public void workingSetChanged(WorkingSet changedWorkingSet)
			throws IOException {
		saveState();
	}

	public AdaptableToc getAdaptableToc(String href) {
		return getRoot().getAdaptableToc(href);
	}

	public AdaptableTopic getAdaptableTopic(String id) {

		if (id == null || id.length() == 0)
			return null;

		// toc id's are hrefs: /pluginId/path/to/toc.xml
		// topic id's are based on parent toc id and index of topic:
		// /pluginId/path/to/toc.xml_index_
		int len = id.length();
		if (id.charAt(len - 1) == '_') {
			// This is a first level topic
			String indexStr = id.substring(id.lastIndexOf('_', len - 2) + 1,
					len - 1);
			int index = 0;
			try {
				index = Integer.parseInt(indexStr);
			} catch (Exception e) {
			}

			String tocStr = id.substring(0, id.lastIndexOf('_', len - 2));
			AdaptableToc toc = getAdaptableToc(tocStr);
			if (toc == null)
				return null;
			IAdaptable[] topics = toc.getChildren();
			if (index < 0 || index >= topics.length)
				return null;
			else
				return (AdaptableTopic) topics[index];
		}

		return null;
	}

	public String getCurrentWorkingSet() {
		return currentWorkingSet;
	}

	public void setCurrentWorkingSet(String workingSet) {
		currentWorkingSet = workingSet;
		try {
			saveState();
		} catch (IOException ioe) {
		}
	}

}
