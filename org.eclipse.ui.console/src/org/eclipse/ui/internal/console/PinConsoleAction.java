/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.console;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Pins the currently visible console in a console view.
 */
public class PinConsoleAction extends Action implements IUpdate {
	
	private IConsoleView fView = null;

	/**
	 * Constructs a 'pin console' action
	 */
	public PinConsoleAction(IConsoleView view) {
		super(ConsoleMessages.PinConsoleAction_0, IAction.AS_CHECK_BOX); 
		setToolTipText(ConsoleMessages.PinConsoleAction_1); 
		setImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_ELCL_PIN));
		setDisabledImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_DLCL_PIN));
		setHoverImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_LCL_PIN));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IConsoleHelpContextIds.CONSOLE_PIN_CONSOLE_ACITON);
		fView = view;
		update();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
        fView.setPinned(isChecked());
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setEnabled(fView.getConsole() != null);
		setChecked(fView.isPinned());
	}
}
