/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import java.util.*;

import org.eclipse.ui.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetElement;

/**
 * Cheat sheet manager class.  Manages cheat sheet data and optional listner.
 */
public class CheatSheetManager implements ICheatSheetManager {

	private String cheatsheetID;
	private List listeners;
	private Hashtable dataTable = null;
	
	public CheatSheetManager(CheatSheetElement element) {
		cheatsheetID = element.getID();
		listeners = new ArrayList();
		CheatSheetListener listener = element.createListenerInstance();
		if (listener != null) {
			addListener(listener);
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetManager#getCheatSheetID()
	 */
	public String getCheatSheetID() {
		return cheatsheetID;
	}

	public void fireEvent(int eventType) {
		// Send an event to every listener
		for (Iterator iterator = listeners.iterator();iterator.hasNext();) {
		    ICheatSheetEvent event = new CheatSheetEvent(eventType, cheatsheetID, this);
		    CheatSheetListener listener = (CheatSheetListener)iterator.next();
		    listener.cheatSheetEvent(event);
		}
	}

	/**
	 * returns the hashtable with all manager data stored.
	 */
	public Map getData() {
		return dataTable;
	}

	public String getData(String key) {
		if (dataTable == null)
			return null;
		return (String) dataTable.get(key);
	}

	public String getVariableData(String variable) {
		String result = variable;
		if(variable != null && variable.startsWith("${") && variable.endsWith("}")) { //$NON-NLS-1$ //$NON-NLS-2$
			result = variable.substring(2,variable.length()-1);
			result = getData(result);
		}
		return result;
	}

	/*package*/ void setData(Hashtable data) {
		dataTable = data;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.ICheatSheetManager#setData(java.lang.String, java.lang.String)
	 */
	public void setData(String key, String data) {
		if (key == null) {
			throw new IllegalArgumentException();
		}

		if(data == null && dataTable != null) {
			dataTable.remove(key);
			return;
		}

		if (dataTable == null) {
			dataTable = new Hashtable(30);
		}

		dataTable.put(key, data);
	}

	/**
	 * Add a listener for cheatsheet events
	 * @param listener
	 */
	public void addListener(CheatSheetListener listener) {
		if (listener != null) {
		    listeners.add(listener);
		}
	}
}
