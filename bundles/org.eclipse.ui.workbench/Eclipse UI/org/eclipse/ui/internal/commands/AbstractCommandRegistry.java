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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.commands.registry.ICommandRegistry;
import org.eclipse.ui.commands.registry.ICommandRegistryEvent;
import org.eclipse.ui.commands.registry.ICommandRegistryListener;

abstract class AbstractCommandRegistry implements ICommandRegistry {

	private ICommandRegistryEvent commandRegistryEvent;
	private List commandRegistryListeners;
	
	protected List activeKeyConfigurationDefinitions = Collections.EMPTY_LIST;
	protected List categoryDefinitions = Collections.EMPTY_LIST; 
	protected List commandDefinitions = Collections.EMPTY_LIST; 
	protected List contextBindingDefinitions = Collections.EMPTY_LIST;
	protected List imageBindingDefinitions = Collections.EMPTY_LIST;
	protected List keyBindingDefinitions = Collections.EMPTY_LIST;
	protected List keyConfigurationDefinitions = Collections.EMPTY_LIST;
	
	protected AbstractCommandRegistry() {
		super();
	}

	public void addCommandRegistryListener(ICommandRegistryListener commandRegistryListener) {
		if (commandRegistryListener == null)
			throw new NullPointerException();
			
		if (commandRegistryListeners == null)
			commandRegistryListeners = new ArrayList();
		
		if (!commandRegistryListeners.contains(commandRegistryListener))
			commandRegistryListeners.add(commandRegistryListener);
	}

	public List getActiveKeyConfigurationDefinitions() {
		return activeKeyConfigurationDefinitions;
	}

	public List getCategoryDefinitions() {
		return categoryDefinitions;
	}
	
	public List getCommandDefinitions() {
		return commandDefinitions;
	}

	public List getContextBindingDefinitions() {
		return contextBindingDefinitions;
	}

	public List getImageBindingDefinitions() {
		return imageBindingDefinitions;
	}
	
	public List getKeyBindingDefinitions() {
		return keyBindingDefinitions;
	}

	public List getKeyConfigurationDefinitions() {
		return keyConfigurationDefinitions;
	}
	
	public void removeCommandRegistryListener(ICommandRegistryListener commandRegistryListener) {
		if (commandRegistryListener == null)
			throw new NullPointerException();
			
		if (commandRegistryListeners != null) {
			commandRegistryListeners.remove(commandRegistryListener);
			
			if (commandRegistryListeners.isEmpty())
				commandRegistryListeners = null;
		}
	}

	protected void fireCommandRegistryChanged() {
		if (commandRegistryListeners != null) {
			// TODO copying to avoid ConcurrentModificationException
			Iterator iterator = new ArrayList(commandRegistryListeners).iterator();	
			
			if (iterator.hasNext()) {
				if (commandRegistryEvent == null)
					commandRegistryEvent = new CommandRegistryEvent(this);
				
				while (iterator.hasNext())	
					((ICommandRegistryListener) iterator.next()).commandRegistryChanged(commandRegistryEvent);
			}							
		}			
	}
}	
