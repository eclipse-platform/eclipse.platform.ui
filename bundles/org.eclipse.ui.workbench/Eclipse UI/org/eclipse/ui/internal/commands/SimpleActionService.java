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

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.action.IAction;

public final class SimpleActionService extends AbstractActionService {
	
	private SortedMap actionMap;

	public SimpleActionService() {
		super();
		this.actionMap = Collections.unmodifiableSortedMap(new TreeMap());
	}

	public SortedMap getActionMap() {
		return actionMap;
	}
	
	public void setActionMap(SortedMap actionMap)
		throws IllegalArgumentException {
		this.actionMap = Util.safeCopy(actionMap, String.class, IAction.class);    
		fireActionServiceChanged(); 
    }
}
