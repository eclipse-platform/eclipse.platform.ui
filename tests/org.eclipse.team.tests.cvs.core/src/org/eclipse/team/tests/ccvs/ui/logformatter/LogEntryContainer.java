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
package org.eclipse.team.tests.ccvs.ui.logformatter;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class LogEntryContainer extends LogEntry {
	private List /* of LogEntry */ list = new ArrayList();
	
	public LogEntryContainer(LogEntryContainer parent, String name) {
		super(parent, name);
	}

	/**
	 * Accepts a visitor for each child in the order in which they are listed.
	 * @param visitor the visitor
	 */
	public void acceptChildren(ILogEntryVisitor visitor) {
		Iterator it = list.iterator();
		while (it.hasNext()) {
			LogEntry entry = (LogEntry) it.next();
			entry.accept(visitor);
		}
	}
	
	/**
	 * Returns the list of children in this container.
	 */
	public LogEntry[] members() {
		return (LogEntry[]) list.toArray(new LogEntry[list.size()]);
	}
	
	/**
	 * Returns the member with the specified name and class.
	 */
	public LogEntry findMember(String name, Class clazz) {
		Iterator it = list.iterator();
		while (it.hasNext()) {
			LogEntry entry = (LogEntry) it.next();
			if (name.equals(entry.getName()) &&
				clazz.isAssignableFrom(entry.getClass())) return entry;
		}
		return null;
	}
	
	/*
	 * Adds the specified entry to the end of the list.
	 */
	void addEntry(LogEntry entry) {
		list.add(entry);
	}
}
