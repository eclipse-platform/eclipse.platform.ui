/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     John-Mason P. Shackelford - bug 34548
 *******************************************************************************/
package org.eclipse.ant.internal.ui.launchConfigurations;

public class AntLaunchShortcutWithDialog extends AntLaunchShortcut {
	
	/**
	 * Creates a new Ant launch shortcut that will open the
	 * launch configuration dialog.
	 */
	public AntLaunchShortcutWithDialog() {
		super();
		setShowDialog(true);
	}
	
}
