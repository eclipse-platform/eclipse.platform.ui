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

package org.eclipse.ui.internal.commands.old;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.commands.old.CommandHandlerServiceEvent;
import org.eclipse.ui.commands.old.ICommandHandlerService;
import org.eclipse.ui.commands.old.ICommandHandlerServiceListener;

/**
 * <p>
 * TODO javadoc
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public abstract class AbstractHandlerService implements ICommandHandlerService {
	
	private CommandHandlerServiceEvent handlerServiceEvent;
	private List handlerServiceListeners;

	/**
	 * TODO javadoc
	 */		
	public AbstractHandlerService() {
		super();
	}

	/**
	 * Registers an IHandlerServiceListener instance with this handler service.
	 *
	 * @param handlerServiceListener the IHandlerServiceListener instance to register.
	 */
	public void addHandlerServiceListener(ICommandHandlerServiceListener handlerServiceListener) {
		if (handlerServiceListeners == null)
			handlerServiceListeners = new ArrayList();
		
		if (!handlerServiceListeners.contains(handlerServiceListener))
			handlerServiceListeners.add(handlerServiceListener);
	}

	/**
	 * Unregisters an IHandlerServiceListener instance with this handler service.
	 *
	 * @param handlerServiceListener the IHandlerServiceListener instance to unregister.
	 */
	public void removeHandlerServiceListener(ICommandHandlerServiceListener handlerServiceListener) {
		if (handlerServiceListeners != null) {
			handlerServiceListeners.remove(handlerServiceListener);
			
			if (handlerServiceListeners.isEmpty())
				handlerServiceListeners = null;
		}
	}

	/**
	 * TODO javadoc
	 */
	protected void fireHandlerServiceChanged() {
		if (handlerServiceListeners != null) {
			Iterator iterator = handlerServiceListeners.iterator();
			
			if (iterator.hasNext()) {
				if (handlerServiceEvent == null)
					handlerServiceEvent = new CommandHandlerServiceEvent(this);
				
				while (iterator.hasNext())	
					((ICommandHandlerServiceListener) iterator.next()).handlerServiceChanged(handlerServiceEvent);
			}							
		}			
	}
}
