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

import java.util.Collections;
import java.util.List;

abstract class AbstractRegistry implements IRegistry {

	protected List activeKeyConfigurationDefinitions = Collections.EMPTY_LIST;
	protected List categoryDefinitions = Collections.EMPTY_LIST; 
	protected List commandDefinitions = Collections.EMPTY_LIST; 
	protected List contextBindingDefinitions = Collections.EMPTY_LIST;
	protected List imageBindingDefinitions = Collections.EMPTY_LIST;
	protected List keyBindingDefinitions = Collections.EMPTY_LIST;
	protected List keyConfigurationDefinitions = Collections.EMPTY_LIST;
	
	protected AbstractRegistry() {
		super();
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
}	
