/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.util.List;

abstract class AbstractCommandRegistry implements ICommandRegistry {

	protected List activeKeyConfigurationDefinitions = Collections.EMPTY_LIST;
	protected List categoryDefinitions = Collections.EMPTY_LIST;
	protected List commandDefinitions = Collections.EMPTY_LIST;

	private CommandRegistryEvent commandRegistryEvent;
	private List commandRegistryListeners;
	protected List handlers = Collections.EMPTY_LIST;
	protected List imageBindingDefinitions = Collections.EMPTY_LIST;
	protected List keyConfigurationDefinitions = Collections.EMPTY_LIST;
	protected List keySequenceBindingDefinitions = Collections.EMPTY_LIST;

	protected AbstractCommandRegistry() {
	    // Do nothing
	}

	public void addCommandRegistryListener(ICommandRegistryListener commandRegistryListener) {
		if (commandRegistryListener == null)
			throw new NullPointerException();

		if (commandRegistryListeners == null)
			commandRegistryListeners = new ArrayList();

		if (!commandRegistryListeners.contains(commandRegistryListener))
			commandRegistryListeners.add(commandRegistryListener);
	}

	protected void fireCommandRegistryChanged() {
		if (commandRegistryListeners != null) {
			for (int i = 0; i < commandRegistryListeners.size(); i++) {
				if (commandRegistryEvent == null)
					commandRegistryEvent = new CommandRegistryEvent(this);

				(
					(ICommandRegistryListener) commandRegistryListeners.get(
						i)).commandRegistryChanged(
					commandRegistryEvent);
			}
		}
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

    /**
     * An accessor for the handlers read into this registry.
     * 
     * @return The list of handlers; this value may be empty, but it is never
     *         <code>null</code>.
     */
    public List getHandlers() {
        return handlers;
    }

	public List getImageBindingDefinitions() {
		return imageBindingDefinitions;
	}

	public List getKeyConfigurationDefinitions() {
		return keyConfigurationDefinitions;
	}

	public List getKeySequenceBindingDefinitions() {
		return keySequenceBindingDefinitions;
	}

	public void removeCommandRegistryListener(ICommandRegistryListener commandRegistryListener) {
		if (commandRegistryListener == null)
			throw new NullPointerException();

		if (commandRegistryListeners != null)
			commandRegistryListeners.remove(commandRegistryListener);
	}
}
