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

import java.io.IOException;
import java.util.List;

import org.eclipse.ui.internal.util.Util;

abstract class AbstractMutableRegistry extends AbstractRegistry
	implements IMutableRegistry {

	protected AbstractMutableRegistry() {
		super();
	}

	public abstract void save()
		throws IOException;

	public void setActiveGestureConfigurations(List activeGestureConfigurations)
		throws IllegalArgumentException {
		this.activeGestureConfigurations = Util.safeCopy(activeGestureConfigurations, ActiveConfiguration.class);
		fireRegistryChanged();
	}
	
	public void setActiveKeyConfigurations(List activeKeyConfigurations)
		throws IllegalArgumentException {
		this.activeKeyConfigurations = Util.safeCopy(activeKeyConfigurations, ActiveConfiguration.class);
		fireRegistryChanged();
	}
	
	public void setCategories(List categories)
		throws IllegalArgumentException {
		this.categories = Util.safeCopy(categories, Category.class);	
		fireRegistryChanged();
	}
	
	public void setCommands(List commands)
		throws IllegalArgumentException {
		this.commands = Util.safeCopy(commands, Command.class);	
		fireRegistryChanged();
	}

	public void setContextBindings(List contextBindings)
		throws IllegalArgumentException {
		this.contextBindings = Util.safeCopy(contextBindings, ContextBinding.class);	
		fireRegistryChanged();
	}

	public void setContexts(List contexts)
		throws IllegalArgumentException {
		this.contexts = Util.safeCopy(contexts, Context.class);	
		fireRegistryChanged();
	}
	
	public void setGestureBindings(List gestureBindings)
		throws IllegalArgumentException {
		this.gestureBindings = Util.safeCopy(gestureBindings, SequenceBinding.class);	
		fireRegistryChanged();
	}
	
	public void setGestureConfigurations(List gestureConfigurations)
		throws IllegalArgumentException {
		this.gestureConfigurations = Util.safeCopy(gestureConfigurations, Configuration.class);	
		fireRegistryChanged();
	}
	
	public void setKeyBindings(List keyBindings)
		throws IllegalArgumentException {
		this.keyBindings = Util.safeCopy(keyBindings, SequenceBinding.class);	
		fireRegistryChanged();
	}
	
	public void setKeyConfigurations(List keyConfigurations)
		throws IllegalArgumentException {
		this.keyConfigurations = Util.safeCopy(keyConfigurations, Configuration.class);		
		fireRegistryChanged();
	}
}
