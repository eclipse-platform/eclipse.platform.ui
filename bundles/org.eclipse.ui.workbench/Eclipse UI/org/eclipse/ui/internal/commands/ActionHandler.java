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

package org.eclipse.ui.internal.commands;

import org.eclipse.swt.widgets.Event;

public class ActionHandler implements org.eclipse.ui.commands.IAction {

	private org.eclipse.jface.action.IAction action;

	public ActionHandler(org.eclipse.jface.action.IAction action) {
		super();
		this.action = action;
	}

	public org.eclipse.jface.action.IAction getAction() {
		return action;
	}

	public void addPropertyListener(XPropertyListener propertyListener) {
	}

	public void execute() {	
		action.run();
	}

	public void execute(Event event) {
		action.runWithEvent(event);
	}

	public Object getProperty(String name)
		throws Exception {
		return null;
	}

	public String[] getPropertyNames() {
		return null;
	}

	public boolean isEnabled() {
		return action.isEnabled();
	}
	
	public void removePropertyListener(XPropertyListener propertyListener) {	
	}
}
