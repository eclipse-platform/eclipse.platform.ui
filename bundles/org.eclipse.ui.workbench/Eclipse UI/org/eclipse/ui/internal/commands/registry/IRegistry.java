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

public interface IRegistry {

	List getActiveGestureConfigurations();

	List getActiveKeyConfigurations();

	List getCategories();
	
	List getCommands();

	List getContextBindings();

	List getContexts();

	List getGestureBindings();

	List getGestureConfigurations();
	
	List getKeyBindings();
	
	List getKeyConfigurations();
	
	void load()
		throws IOException;
}	
