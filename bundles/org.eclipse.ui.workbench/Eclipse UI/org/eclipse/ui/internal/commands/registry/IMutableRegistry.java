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

package org.eclipse.ui.internal.commands.registry;

import java.io.IOException;
import java.util.List;

public interface IMutableRegistry extends IRegistry {

	void save()
		throws IOException;

	void setActiveGestureConfigurations(List activeGestureConfigurations)
		throws IllegalArgumentException;
	
	void setActiveKeyConfigurations(List activeKeyConfigurations)
		throws IllegalArgumentException;
	
	void setCategories(List categories)
		throws IllegalArgumentException;
	
	void setCommands(List commands)
		throws IllegalArgumentException;

	void setContextBindings(List contextBindings)
		throws IllegalArgumentException;

	void setContexts(List contexts)
		throws IllegalArgumentException;
	
	void setGestureBindings(List gestureBindings)
		throws IllegalArgumentException;
	
	void setGestureConfigurations(List gestureConfigurations)
		throws IllegalArgumentException;
	
	void setKeyBindings(List keyBindings)
		throws IllegalArgumentException;
	
	void setKeyConfigurations(List keyConfigurations)
		throws IllegalArgumentException;
}
