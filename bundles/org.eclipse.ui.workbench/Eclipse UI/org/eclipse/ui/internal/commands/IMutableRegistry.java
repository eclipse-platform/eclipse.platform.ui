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

import java.io.IOException;
import java.util.List;

interface IMutableRegistry extends IRegistry {

	void save()
		throws IOException;

	void setActiveKeyConfigurations(List activeKeyConfigurations);
	
	void setCategories(List categories);
	
	void setCommands(List commands);
	
	void setContextBindings(List contextBindings);

	void setImageBindings(List imageBindings);

	void setKeyBindings(List keyBindings);
	
	void setKeyConfigurations(List keyConfigurations);
}
