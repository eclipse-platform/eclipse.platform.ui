package org.eclipse.help.internal.filter;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;

import org.apache.xerces.dom.*;
import org.apache.xerces.parsers.*;
import org.apache.xml.serialize.*;
import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
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
	// Working set persistence
	private static final String WORKING_SET_STATE_FILENAME = "workingsets.xml";
	private SortedSet workingSets = new TreeSet(new WorkingSetComparator());
	private String locale;

	/**
	 * Constructor
	 * @param locale
	 */
	public WorkingSetManager(String locale) {
		if (locale == null)
			locale = BootLoader.getNL();
		this.locale = locale;
		restoreState();
	}

	/**
	 * Adds a new working set and saves it
	 */
	public void addWorkingSet(WorkingSet workingSet) {
		if (workingSets.contains(workingSet))
			return;
		workingSets.add(workingSet);
		saveState();
	}

	/**
	 * Creates a new working set
	 */
	public WorkingSet createWorkingSet(String name, IHelpResource[] elements) {
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
		path = path.append(WORKING_SET_STATE_FILENAME);
		return path.toFile();
	}

	/**
	 * Removes specified working set
	 */
	public void removeWorkingSet(WorkingSet workingSet) {
		workingSets.remove(workingSet);
		saveState();
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

				Element root = parser.getDocument().getDocumentElement();
				restoreWorkingSetState(root);
				input.close();

				return true;

			} catch (SAXException se) {
				String msg = Resources.getString("E018", stateFile.toString());
				Logger.logError(msg, se);
				Logger.logError(Resources.getString("E041"), se);
				return false;
			} catch (IOException ioe) {
				String msg = Resources.getString("E018", stateFile.toString());
				Logger.logError(msg, ioe);
				Logger.logError(Resources.getString("E041"), ioe);
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
				addWorkingSet(workingSet);
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
		WorkingSet ws = new WorkingSet(name);
		NodeList items = workingSetNode.getElementsByTagName("item");
		List tocs = new ArrayList(items.getLength());
		for (int i = 0; i < items.getLength(); i++) {
			Element item = (Element) items.item(i);
			IToc toc =
				HelpSystem.getTocManager().getToc(
					item.getAttribute("href"),
					locale);
			ws.addElement(toc);
		}

		return ws;
	}

	/**
	 * Saves the working sets in the persistence store
	 */
	private boolean saveState() {
		Document doc = new DocumentImpl();
		Element root = doc.createElement("workingSets");
		doc.appendChild(root);
;
		saveWorkingSetState(root);

		File stateFile = getWorkingSetStateFile();

		try {
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
			Logger.logError(message, null);
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
	 * Persists all working sets. Should only be called by WorkingSet.
	 * 
	 * @param changedWorkingSet the working set that has changed
	 */
	public void workingSetChanged(WorkingSet changedWorkingSet) {
		saveState();
	}
}