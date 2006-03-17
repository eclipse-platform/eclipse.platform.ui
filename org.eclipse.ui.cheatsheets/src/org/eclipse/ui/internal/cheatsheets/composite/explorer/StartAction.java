/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.composite.explorer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.Messages;
import org.eclipse.ui.internal.cheatsheets.composite.model.AbstractTask;
import org.eclipse.ui.internal.provisional.cheatsheets.ICompositeCheatSheetTask;

public class StartAction extends Action {
	private AbstractTask task;
	private static final String START_CCS_TASK_GIF = "start_ccs_task.gif"; //$NON-NLS-1$

	public StartAction(ICompositeCheatSheetTask task) {
		this.task = (AbstractTask) task;
		this.setText(Messages.COMPOSITE_MENU_START);
		IPath path = CheatSheetPlugin.ICONS_PATH.append(CheatSheetPlugin.T_ELCL).append(START_CCS_TASK_GIF);
		ImageDescriptor startImage = CheatSheetPlugin.createImageDescriptor(CheatSheetPlugin.getPlugin().getBundle(), path);	
		this.setImageDescriptor(startImage);
	}
	
	public void run() {
		task.setState(ICompositeCheatSheetTask.IN_PROGRESS);
	}

}
