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

package org.eclipse.ui.internal.commands.registry.old;

import org.eclipse.ui.internal.util.Util;

public final class ContextBinding implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ContextBinding.class.getName().hashCode();
	
	public static ContextBinding create(String command, String context, String plugin)
		throws IllegalArgumentException {
		return new ContextBinding(command, context, plugin);
	}

	private String command;
	private String context;
	private String plugin;

	private ContextBinding(String command, String context, String plugin)
		throws IllegalArgumentException {
		super();
		
		if (context == null)
			throw new IllegalArgumentException();	
		
		this.command = command;	
		this.context = context;
		this.plugin = plugin;
	}

	public int compareTo(Object object) {
		ContextBinding contextBinding = (ContextBinding) object;
		int compareTo = Util.compare(command, contextBinding.command);

		if (compareTo == 0) {		
			compareTo = context.compareTo(contextBinding.context);
			
			if (compareTo == 0)
				compareTo = Util.compare(plugin, contextBinding.plugin);
		}

		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ContextBinding))
			return false;
		
		ContextBinding contextBinding = (ContextBinding) object;
		return Util.equals(command, contextBinding.command) && context.equals(contextBinding.context) && Util.equals(plugin, contextBinding.plugin);
	}

	public String getCommand() {
		return command;
	}

	public String getContext() {
		return context;
	}

	public String getPlugin() {
		return plugin;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + Util.hashCode(command);		
		result = result * HASH_FACTOR + context.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(plugin);	
		return result;
	}
}
