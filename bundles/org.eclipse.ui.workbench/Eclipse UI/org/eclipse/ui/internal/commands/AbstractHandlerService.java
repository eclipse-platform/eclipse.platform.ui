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

import org.eclipse.ui.commands.IHandlerService;
import org.eclipse.ui.commands.IHandlerServiceListener;

public abstract class AbstractHandlerService implements IHandlerService {
	
	private List handlerServiceListeners;
	
	public AbstractHandlerService() {
		super();
	}

	public void addHandlerServiceListener(IHandlerServiceListener handlerServiceListener) {
		if (handlerServiceListeners == null)
			handlerServiceListeners = new ArrayList();
		
		if (!handlerServiceListeners.contains(handlerServiceListener))
			handlerServiceListeners.add(handlerServiceListener);
	}

	public void removeHandlerServiceListener(IHandlerServiceListener handlerServiceListener) {
		if (handlerServiceListeners != null) {
			handlerServiceListeners.remove(handlerServiceListener);
			
			if (handlerServiceListeners.isEmpty())
				handlerServiceListeners = null;
		}
	}

	protected void fireHandlerServiceChanged() {
		if (handlerServiceListeners != null) {
			Iterator iterator = handlerServiceListeners.iterator();
			
			while (iterator.hasNext())
				((IHandlerServiceListener) iterator.next()).handlerServiceChanged(this);							
		}			
	}
}
