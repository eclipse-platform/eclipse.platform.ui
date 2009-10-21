/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.menu;

import org.eclipse.debug.ui.actions.OpenLaunchDialogAction;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;

/**
 * Opens the launch config dialog on the external tools launch group.
 */
public class OpenExternalToolsConfigurations extends OpenLaunchDialogAction {

	public OpenExternalToolsConfigurations() {
		super(IExternalToolConstants.ID_EXTERNAL_TOOLS_LAUNCH_GROUP);
	}
}
