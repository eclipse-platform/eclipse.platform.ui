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

package org.eclipse.ui.commands.internal;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.commands.IActionService;
import org.eclipse.ui.internal.commands.Util;

public final class ActionService implements IActionService {
	
	private SortedMap actionMap;
	private IActionServiceObserver actionServiceObserver;
	
	public ActionService(IActionServiceObserver actionServiceObserver)
		throws IllegalArgumentException {
		super();

		if (actionServiceObserver == null)
			throw new IllegalArgumentException();

		this.actionMap = Collections.unmodifiableSortedMap(new TreeMap());
		this.actionServiceObserver = actionServiceObserver;
	}

	public SortedMap getActionMap() {
		return actionMap;
	}
	
	public void setActionMap(SortedMap actionMap)
		throws IllegalArgumentException {
		this.actionMap = Util.safeCopy(actionMap, String.class, IAction.class);    
		actionServiceObserver.actionServiceChanged(this); 
    }
}
