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
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.actions;

import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.ui.action.CreateChildAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.dialogs.HockeyleagueSetDefaultsDialog;

/**
 * Create child action for the Tabbed Properties View Hockey League Example.
 * 
 * @author Anthony Hunter
 */
public class HockeyleagueCreateChildAction extends CreateChildAction {
	public HockeyleagueCreateChildAction(IEditorPart editorPart,
			ISelection selection, Object descriptor) {
		super(editorPart, selection, descriptor);
	}

	public void run() {
		CreateChildCommand createChildCommand = (CreateChildCommand) this.command;
		HockeyleagueSetDefaultsDialog dialog;
		if (createChildCommand.getCommand() instanceof AddCommand) {
			AddCommand addCommand = (AddCommand) createChildCommand.getCommand();
			dialog = new HockeyleagueSetDefaultsDialog(Display.getCurrent()
					.getActiveShell(), addCommand);
		} else if (createChildCommand.getCommand() instanceof SetCommand) {
			SetCommand setCommand = (SetCommand) createChildCommand.getCommand();
			dialog = new HockeyleagueSetDefaultsDialog(Display.getCurrent()
					.getActiveShell(), setCommand);
		} else {
			dialog = null;
		}
		dialog.open();
		if (dialog.getReturnCode() == Window.CANCEL) {
			return;
		}
		super.run();
	}

}