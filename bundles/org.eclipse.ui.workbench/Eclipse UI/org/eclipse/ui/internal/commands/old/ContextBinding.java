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

import org.eclipse.ui.commands.IContextBinding;

final class ContextBinding implements IContextBinding {

	private String commandId;
	private String contextId;
	private String pluginId;

	ContextBinding(String commandId, String contextId, String pluginId) {
		super();
		this.commandId = commandId;
		this.contextId = contextId;
		this.pluginId = pluginId;
	}

	public String getCommandId() {
		return commandId;
	}

	public String getContextId() {
		return contextId;
	}

	public String getPluginId() {
		return pluginId;
	}
}
