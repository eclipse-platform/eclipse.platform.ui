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

package org.eclipse.ui.internal.csm.commands;

import java.util.List;

public interface ICommandRegistry {

	void addCommandRegistryListener(ICommandRegistryListener commandRegistryListener);

	List getCommandDefinitions();

	List getCommandPatternBindingDefinitions();
	
	void removeCommandRegistryListener(ICommandRegistryListener commandRegistryListener);
}
