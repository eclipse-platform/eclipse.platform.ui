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


import org.xml.sax.Attributes;

public class CaseEntry extends LogEntryContainer {
	private String className;
	
	public CaseEntry(LogEntryContainer parent, Attributes attributes) {
		this(parent, attributes.getValue("name"), attributes.getValue("class"));
	}
	
	public CaseEntry(LogEntryContainer parent, String name, String className) {
		super(parent, name);
		this.className = (className != null) ? className : "unknown";
	}
	
	public void accept(ILogEntryVisitor visitor) {
		visitor.visitCaseEntry(this);
	}
	
	/**
	 * Returns the class name of the test case.
	 */
	public String getClassName() {
		return className;
	}	
}
