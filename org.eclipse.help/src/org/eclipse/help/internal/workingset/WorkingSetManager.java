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
package org.eclipse.help.internal.workingset;


import java.io.*;
import java.util.*;

import org.apache.xerces.dom.*;
import org.apache.xerces.parsers.*;
import org.apache.xml.serialize.*;
import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * The working  set manager stores help working sets. Working sets are persisted
 * whenever one is added or removed.
 * @since 2.1
 */
public class WorkingSetManager {

	// Note: keep the following constants in sych with the values defined in IWorkingSetManager.
	//       They are needed to synch the ui and the help working sets, as help should run w/o ui plugins.

	/**
	 * Change event id when a working set is added
	 * newValue of the PropertyChangeEvent will be the added working set.
	 * oldValue will be null.
	 *
	 * @see IPropertyChangeListener
	 */
	public static final String CHANGE_WORKING_SET_ADD = "workingSetAdd";
	/**
	 * Change event id when a working set is removed
	 * newValue of the PropertyChangeEvent will be null.
	 * oldValue will be the removed working set.
	 *
	 * @see IPropertyChangeListener
	 */
	public static final String CHANGE_WORKING_SET_REMOVE = "workingSetRemove";
	/**
	 * Change event id when the working set contents changed
	 * newValue of the PropertyChangeEvent will be the changed working set.
	 * oldValue will be null.
	 *
	 * @see IPropertyChangeListener
	 */
	public static final String CHANGE_WORKING_SET_CONTENT_CHANGE =
		"workingSetContentChange";
	/**
	 * Change event id when the working set name changed.
	 * newValue of the PropertyChangeEvent will be the changed working set.
	 * oldValue will be null.
	 *
	 * @see IPropertyChangeListener
	 */
	public static final String CHANGE_WORKING_SET_NAME_CHANGE = "workingSetNameChange"; //$NON-NLS-1$	

	/**
	 * Synchronize event id. When other working sets repositories are used,
	 * one may want to keep things in synch.
	 *
	 * @see IPropertyChangeListener
	 */
	public static final String CHANGE_WORKING_SETS_SYNCH = "workingSetsSynch";

	// Working set persistence
	private static final String WORKING_SET_STATE_FILENAME = "workingsets.xml";
	private SortedSet workingSets = new TreeSet(new WorkingSetComparator());
	private String locale;
	private PropertyChange.ListenerList propertyChangeListeners =
		new PropertyChange.ListenerList();
	private AdaptableTocsArray root;

	/**
	 * Constructor
	 * @param locale
	 */
	public WorkingSetManager(String locale) {
		this.locale = locale != null ? locale : BootLoader.getNL();
		restoreState();
	}

	public AdaptableTocsArray getRoot() {
		if (root == null)
			root =
				new AdaptableTocsArray(
					HelpSystem.getTocManager().getTocs(locale));
		return root;
	}

	/**
	 * Adds a new working set and saves it
	 */
	public void addWorkingSet(WorkingSet workingSet) {
		if (workingSet == null || workingSets.contains(workingSet))
			return;
		workingSets.add(workingSet);
		saveState();
		firePropertyChange(CHANGE_WORKING_SET_ADD, null, workingSet);
	}

	/**
	 */
	public void addPropertyChangeListener(
		PropertyChange.IPropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}

	/**
	 * Creates a new working set
	 */
	public WorkingSet createWorkingSet(
		String name,
		AdaptableHelpResource[] elements) {
		return new WorkingSet(name, elements);
	}

	/**
	 * Tests the receiver and the object for equality
	 * 
	 * @param object object to compare the receiver to
	 * @return true=the object equals the receiver, it has the same 
	 * 	working sets. false otherwise
	 */
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object instanceof WorkingSetManager) {
			WorkingSetManager workingSetManager = (WorkingSetManager) object;
			return workingSetManager.workingSets.equals(workingSets);
		}
		return false;
	}

	/**
	 * Notify property change listeners about a change to the list of 
	 * working sets.
	 * 
	 * @param changeId one of 
	 * 	CHANGE_WORKING_SET_ADD		 	
	 *  CHANGE_WORKING_SET_REMOVE
	 *  CHANGE_WORKING_SET_CONTENT_CHANGE
	 *  CHANGE_WORKING_SET_NAME_CHANGE
	 * @param oldValue the removed working set or null if a working set 
	 * 	was added or changed.
	 * @param newValue the new or changed working set or null if a working 
	 * 	set was removed.
	 */
	private void firePropertyChange(
		String changeId,
		Object oldValue,
		Object newValue) {
		final PropertyChange.PropertyChangeEvent event =
			new PropertyChange.PropertyChangeEvent(
				this,
				changeId,
				oldValue,
				newValue);

		Object[] listeners = propertyChangeListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			(
				(
					PropertyChange
						.IPropertyChangeListener) listeners[i])
						.propertyChange(
				event);
		}
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
	 * Returns the hash code.
	 * 
	 * @return the hash code.
	 */
	public int hashCode() {
		return workingSets.hashCode();
	}
	/**
	 * Implements IWorkingSetManager.
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager#getWorkingSets()
	 */
	public WorkingSet[] getWorkingSets() {
		return (WorkingSet[]) workingSets.toArray(
			new WorkingSet[workingSets.size()]);
	}
	/**
	 * Returns the file used as the persistence store
	 * 
	 * @return the file used as the persistence store
	 */
	private File getWorkingSetStateFile() {
		IPath path = HelpPlugin.getDefault().getStateLocation();
		path = path.append(locale);
		path = path.append(WORKING_SET_STATE_FILENAME);
		return path.toFile();
	}

	/**
	 * Removes specified working set
	 */
	public void removeWorkingSet(WorkingSet workingSet) {
		workingSets.remove(workingSet);
		saveState();
		firePropertyChange(CHANGE_WORKING_SET_REMOVE, workingSet, null);
	}

	/**
	 * Reads the persistence store and creates the working sets 
	 * stored in it.
	 */
	public boolean restoreState() {
		File stateFile = getWorkingSetStateFile();

		if (stateFile.exists()) {
			try {
				FileInputStream input = new FileInputStream(stateFile);
				InputStreamReader reader = new InputStreamReader(input, "utf-8"); //$NON-NLS-1$

				InputSource inputSource = new InputSource(reader);
				inputSource.setSystemId(stateFile.toString());

				DOMParser parser = new DOMParser();
				parser.parse(inputSource);
				if (parser.getDocument() == null)
					return false;

				Element rootElement = parser.getDocument().getDocumentElement();
				restoreWorkingSetState(rootElement);
				input.close();

				return true;

			} catch (SAXException se) {
				String msg = Resources.getString("E018", stateFile.toString());
				HelpPlugin.logError(msg, se);
				HelpPlugin.logError(Resources.getString("E041"), se);
				return false;
			} catch (IOException ioe) {
				String msg = Resources.getString("E018", stateFile.toString());
				HelpPlugin.logError(msg, ioe);
				HelpPlugin.logError(Resources.getString("E041"), ioe);
				return false;
			}
		}
		return false;
	}

	/**
	 * Recreates all working sets from the persistence store
	 * and adds them to the receiver.
	 * 
	 * @param parent the xml element containing serialized working sets
	 */
	private void restoreWorkingSetState(Element parent) {
		NodeList workingSets = parent.getChildNodes();

		for (int i = 0; i < workingSets.getLength(); i++) {
			if (workingSets.item(i).getNodeType() != Element.ELEMENT_NODE)
				continue;

			WorkingSet workingSet =
				restoreWorkingSet((Element) workingSets.item(i));
			if (workingSet != null) {
				this.workingSets.add(workingSet);
			}
		}
	}

	/**
	 * Recreates a working set from the persistence store.
	 * 
	 * @param memento the persistence store
	 * @return the working set created from the memento or null if
	 * 	creation failed.
	 */
	private WorkingSet restoreWorkingSet(Element workingSetNode) {

		String name = workingSetNode.getAttribute("name");
		NodeList items = workingSetNode.getElementsByTagName("item");
		List helpResources = new ArrayList(items.getLength());
		for (int i = 0; i < items.getLength(); i++) {
			Element item = (Element) items.item(i);
			String href = item.getAttribute("toc");
			if (href == null || href.length() == 0)
				continue;

			String child_pos = item.getAttribute("topic");
			int pos = -1;
			if (child_pos != null) {
				try {
					pos = Integer.parseInt(child_pos);
				} catch (Exception e) {
				}
			}

			AdaptableHelpResource toc = getAdaptableToc(href);

			if (toc == null)
				return null;

			if (pos == -1) {
				// Create the adaptable toc.
				helpResources.add(toc);
			} else {
				// Create the adaptable topic
				AdaptableTopic[] topics = (AdaptableTopic[]) toc.getChildren();
				if (pos >= 0 && topics.length > pos)
					helpResources.add(topics[pos]);
			}
		}

		AdaptableHelpResource[] elements =
			new AdaptableHelpResource[helpResources.size()];
		helpResources.toArray(elements);

		WorkingSet ws = createWorkingSet(name, elements);

		return ws;
	}

	/**
	 */
	public void removePropertyChangeListener(
		PropertyChange.IPropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
	}

	/**
	 * Saves the working sets in the persistence store
	 */
	public synchronized boolean saveState() {
		Document doc = new DocumentImpl();
		Element rootElement = doc.createElement("workingSets");
		doc.appendChild(rootElement);

		saveWorkingSetState(rootElement);

		File stateFile = getWorkingSetStateFile();
		try {
			stateFile.getParentFile().mkdir();
			FileOutputStream stream = new FileOutputStream(stateFile);

			OutputFormat format = new OutputFormat();
			format.setEncoding("UTF-8");
			Serializer serializer =
				SerializerFactory.getSerializerFactory("xml").makeSerializer(
					stream,
					format);

			serializer.asDOMSerializer().serialize(doc);
			stream.close();
			return true;
		} catch (IOException e) {
			stateFile.delete();
			String message = Resources.getString("E40");
			HelpPlugin.logError(message, null);
			return false;
		}
	}

	/**
	 * Saves all persistable working sets in the persistence store.
	 * 
	 * @param parent: the xml node to save to
	 */
	private void saveWorkingSetState(Element parent) {
		Iterator iterator = workingSets.iterator();

		while (iterator.hasNext()) {
			WorkingSet workingSet = (WorkingSet) iterator.next();
			workingSet.saveState(parent);
		}
	}
	/**
	 * Persists all working sets. Should only be called by the webapp working
	 * set dialog.
	 * 
	 * @param changedWorkingSet the working set that has changed
	 */
	public void workingSetChanged(WorkingSet changedWorkingSet) {
		saveState();
		firePropertyChange(
			CHANGE_WORKING_SET_NAME_CHANGE,
			null,
			changedWorkingSet);
		firePropertyChange(
			CHANGE_WORKING_SET_CONTENT_CHANGE,
			null,
			changedWorkingSet);
	}

	/**
	 * Synchronizes the working sets. Should only be called by the webapp
	 * working set manager dialog.
	 *
	 * @param changedWorkingSet the working set that has changed
	 */
	public void synchronizeWorkingSets() {
		firePropertyChange(CHANGE_WORKING_SETS_SYNCH, null, null);
	}

	public AdaptableToc getAdaptableToc(String href) {
		return getRoot().getAdaptableToc(href);
	}

	public AdaptableTopic getAdaptableTopic(String id) {

		if (id == null || id.length() == 0)
			return null;

		// toc id's are hrefs: /pluginId/path/to/toc.xml
		// topic id's are based on parent toc id and index of topic: /pluginId/path/to/toc.xml_index_
		int len = id.length();
		if (id.charAt(len - 1) == '_') {
			// This is a first level topic
			String indexStr =
				id.substring(id.lastIndexOf('_', len - 2) + 1, len - 1);
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
}
