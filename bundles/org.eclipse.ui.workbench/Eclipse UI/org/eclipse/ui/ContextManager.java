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

package org.eclipse.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.ContextResolver;
import org.eclipse.jface.action.IContextResolver;
import org.eclipse.ui.internal.commands.Command;
import org.eclipse.ui.internal.commands.CoreRegistry;
import org.eclipse.ui.internal.commands.LocalRegistry;
import org.eclipse.ui.internal.commands.PreferenceRegistry;

public class ContextManager implements IContextResolver {

	private static ContextManager instance;

	public static ContextManager getInstance() {
		if (instance == null)
			instance = new ContextManager();
			
		return instance;	
	}

	private Map commandsById;
	private Set contexts;

	private ContextManager() {
		super();
		ContextResolver.getInstance().setContextResolver(this);
		reset();
	}
	
	public Set getContexts() {
		return contexts;		
	}
	
	public void setContexts(Set contexts)
		throws IllegalArgumentException {
		this.contexts = Collections.unmodifiableSet(new HashSet(contexts));		
		Iterator iterator = this.contexts.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof String))
				throw new IllegalArgumentException();			
	}

	public boolean inContext(String commandId) {
		/*
		if (commandId != null) {
			Command command = (Command) commandsById.get(commandId);
			
			if (command != null) {
				List contexts = command.getContexts();
				
				if (contexts != null && contexts.size() >= 1) {
					Iterator iterator = contexts.iterator();
					
					while (iterator.hasNext()) {
						String context = (String) iterator.next();
						
						if (this.contexts.contains(context))
							return true;
					}
					
					return false;				
				}
			}
		}
		*/

		return true;			
	}
	
	public void reset() {
		contexts = Collections.EMPTY_SET;
		CoreRegistry coreRegistry = CoreRegistry.getInstance();
		LocalRegistry localRegistry = LocalRegistry.getInstance();
		PreferenceRegistry preferenceRegistry = PreferenceRegistry.getInstance();
			
		try {
			coreRegistry.load();
		} catch (IOException eIO) {
		}
	
		try {
			localRegistry.load();
		} catch (IOException eIO) {
		}
	
		try {
			preferenceRegistry.load();
		} catch (IOException eIO) {
		}		

		List commands = new ArrayList();
		commands.addAll(coreRegistry.getCommands());
		commands.addAll(localRegistry.getCommands());
		commands.addAll(preferenceRegistry.getCommands());		
		commandsById = Collections.unmodifiableSortedMap(Command.sortedMapById(commands));
	}
}
