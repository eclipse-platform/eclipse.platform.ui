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

public final class ContextBinding implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ContextBinding.class.getName().hashCode();
	
	public static ContextBinding create(String command, String plugin, String context)
		throws IllegalArgumentException {
		return new ContextBinding(command, plugin, context);
	}

	private String command;
	private String plugin;
	private String context;

	private ContextBinding(String command, String plugin, String context)
		throws IllegalArgumentException {
		super();
		
		if (context == null)
			throw new IllegalArgumentException();	
		
		this.command = command;	
		this.context = context;
	}

	public int compareTo(Object object) {
		ContextBinding contextBinding = (ContextBinding) object;
		int compareTo = Util.compare(command, contextBinding.command);

		if (compareTo == 0) {		
			compareTo = Util.compare(plugin, contextBinding.plugin);

			if (compareTo == 0)
				compareTo = context.compareTo(contextBinding.context);
		}

		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ContextBinding))
			return false;
		
		ContextBinding contextBinding = (ContextBinding) object;
		return Util.equals(command, contextBinding.command) && Util.equals(plugin, contextBinding.plugin) && context.equals(contextBinding.context);
	}

	public String getCommand() {
		return command;
	}

	public String getPlugin() {
		return plugin;
	}

	public String getContext() {
		return context;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + Util.hashCode(command);		
		result = result * HASH_FACTOR + Util.hashCode(plugin);	
		result = result * HASH_FACTOR + context.hashCode();
		return result;
	}
}
