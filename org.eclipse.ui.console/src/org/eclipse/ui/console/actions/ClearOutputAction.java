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
package org.eclipse.ui.console.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.console.ConsoleMessages;
import org.eclipse.ui.internal.console.ConsolePluginImages;
import org.eclipse.ui.internal.console.IConsoleHelpContextIds;
import org.eclipse.ui.internal.console.IInternalConsoleConstants;

/**
 * Clears the output in a text console.
 * <p>
 * Clients may instantiate this class; this class is not intended to 
 * be subclassed.
 * </p>
 * @since 3.0
 */
public class ClearOutputAction extends Action {

	private ITextViewer fViewer;

	public ClearOutputAction(ITextViewer viewer) {
		super(ConsoleMessages.getString("ClearOutputAction.title")); //$NON-NLS-1$
		fViewer= viewer;
		setToolTipText(ConsoleMessages.getString("ClearOutputAction.toolTipText")); //$NON-NLS-1$
		setHoverImageDescriptor(ConsolePluginImages.getImageDescriptor(IConsoleConstants.IMG_LCL_CLEAR));		
		setDisabledImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_DLCL_CLEAR));
		setImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_ELCL_CLEAR));
		WorkbenchHelp.setHelp(this, IConsoleHelpContextIds.CLEAR_CONSOLE_ACTION);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		BusyIndicator.showWhile(ConsolePlugin.getStandardDisplay(), new Runnable() {
			public void run() {
				IDocument document = fViewer.getDocument();
				if (document != null) {
					document.set(""); //$NON-NLS-1$
				}
				fViewer.setSelectedRange(0, 0);
				fViewer.getTextWidget().redraw();
			}
		});
	}
}