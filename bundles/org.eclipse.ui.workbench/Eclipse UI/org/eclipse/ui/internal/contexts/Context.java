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

package org.eclipse.ui.internal.contexts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.contexts.IContext;
import org.eclipse.ui.contexts.IContextEvent;
import org.eclipse.ui.contexts.IContextListener;
import org.eclipse.ui.contexts.NotDefinedException;

final class Context implements IContext {

	private IContextEvent contextEvent;
	private List contextListeners;
	private ContextManager contextManager;
	private String id;

	Context(ContextManager contextManager, String id) {
		super();
		this.contextManager = contextManager;
		this.id = id;
	}

	public void addContextListener(IContextListener contextListener)
		throws IllegalArgumentException {
		if (contextListener == null)
			throw new IllegalArgumentException();
		
		if (contextListeners == null)
			contextListeners = new ArrayList();
		
		if (!contextListeners.contains(contextListener))
			contextListeners.add(contextListener);
	}

	public String getDescription() 
		throws NotDefinedException {
		ContextElement contextElement = (ContextElement) contextManager.getContextElement(id);

		if (contextElement != null && contextManager.getDefinedContextIds().contains(id))
			return contextElement.getDescription();
		else 
			throw new NotDefinedException();
	}

	public String getId() {
		return id;
	}

	public String getName() 
		throws NotDefinedException {
		ContextElement contextElement = (ContextElement) contextManager.getContextElement(id);

		if (contextElement != null && contextManager.getDefinedContextIds().contains(id))
			return contextElement.getName();
		else 
			throw new NotDefinedException();
	}

	public String getParentId() 
		throws NotDefinedException {
		ContextElement contextElement = (ContextElement) contextManager.getContextElement(id);

		if (contextElement != null && contextManager.getDefinedContextIds().contains(id))
			return contextElement.getParentId();
		else 
			throw new NotDefinedException();
	}

	public String getPluginId() 
		throws NotDefinedException {
		ContextElement contextElement = (ContextElement) contextManager.getContextElement(id);

		if (contextElement != null && contextManager.getDefinedContextIds().contains(id))
			return contextElement.getPluginId();
		else 
			throw new NotDefinedException();
	}

	public boolean isActive() {
		return contextManager.getActiveContextIds().contains(id);
	}

	public boolean isDefined() {
		return contextManager.getContextElement(id) != null && contextManager.getDefinedContextIds().contains(id);
	}

	public void removeContextListener(IContextListener contextListener)
		throws IllegalArgumentException {
		if (contextListener == null)
			throw new IllegalArgumentException();

		if (contextListeners != null) {
			contextListeners.remove(contextListener);
			
			if (contextListeners.isEmpty())
				contextListeners = null;
		}
	}
	
	void fireContextChanged() {
		if (contextListeners != null) {
			// TODO copying to avoid ConcurrentModificationException
			Iterator iterator = new ArrayList(contextListeners).iterator();			
			
			if (iterator.hasNext()) {
				if (contextEvent == null)
					contextEvent = new ContextEvent(this);
				
				while (iterator.hasNext())	
					((IContextListener) iterator.next()).contextChanged(contextEvent);
			}							
		}			
	}		
}
