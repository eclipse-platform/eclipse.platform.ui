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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.commands.IActionService;
import org.eclipse.ui.commands.IActionServiceListener;

public abstract class AbstractActionService implements IActionService {
	
	private List actionServiceListeners;
	
	public AbstractActionService() {
		super();
	}

	public void addActionServiceListener(IActionServiceListener actionServiceListener) {
		if (actionServiceListeners == null)
			actionServiceListeners = new ArrayList();
		
		if (!actionServiceListeners.contains(actionServiceListener))
			actionServiceListeners.add(actionServiceListener);
	}

	public void removeActionServiceListener(IActionServiceListener actionServiceListener) {
		if (actionServiceListeners != null) {
			actionServiceListeners.remove(actionServiceListener);
			
			if (actionServiceListeners.isEmpty())
				actionServiceListeners = null;
		}
	}

	protected void fireActionServiceChanged() {
		if (actionServiceListeners != null) {
			Iterator iterator = actionServiceListeners.iterator();
			
			while (iterator.hasNext())
				((IActionServiceListener) iterator.next()).actionServiceChanged(this);							
		}			
	}
}
