/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.debug.internal.ui.views.launch.LaunchView;


/**
 * Default handler for terminate and re-launch. See bug 300810.
 */
public class TerminateAndRelaunchHandler extends DebugActionHandler {

	public TerminateAndRelaunchHandler() {
		super(LaunchView.TERMINATE_AND_RELAUNCH);
	}
	

}
