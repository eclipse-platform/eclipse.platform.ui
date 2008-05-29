/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.console.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.internal.console.ConsoleMessages;
import org.eclipse.ui.internal.console.ConsolePluginImages;
import org.eclipse.ui.internal.console.IConsoleHelpContextIds;
import org.eclipse.ui.internal.console.IInternalConsoleConstants;

/**
 * Clears the output in a text console.
 * <p>
 * Clients may instantiate this class.
 * </p>
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ClearOutputAction extends Action {

	private ITextViewer fViewer;
	private TextConsole fIOConsole;

	/**
	 * Constructs a clear output action.
	 * 
	 * @since 3.1
	 */
	private ClearOutputAction() {
		super(ConsoleMessages.ClearOutputAction_title); 
		setToolTipText(ConsoleMessages.ClearOutputAction_toolTipText); 
		setHoverImageDescriptor(ConsolePluginImages.getImageDescriptor(IConsoleConstants.IMG_LCL_CLEAR));		
		setDisabledImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_DLCL_CLEAR));
		setImageDescriptor(ConsolePluginImages.getImageDescriptor(IInternalConsoleConstants.IMG_ELCL_CLEAR));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IConsoleHelpContextIds.CLEAR_CONSOLE_ACTION);	    
	}
	
	/**
	 * Constructs a clear output action for an I/O console. Clearing an I/O console
	 * is performed via API on the <code>IOConsole</code>, rather than clearing
	 * its document directly.
	 * 
	 * @param ioConsole I/O console the action is associated with
	 * @since 3.1
	 */
	public ClearOutputAction(TextConsole ioConsole) {
		this();
		fIOConsole = ioConsole;
	}
	
	/**
	 * Constructs an action to clear the document associated with a text viewer.
	 * 
	 * @param viewer viewer whose document this action is associated with 
	 */
	public ClearOutputAction(ITextViewer viewer) {
	    this();
	    fViewer = viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		BusyIndicator.showWhile(ConsolePlugin.getStandardDisplay(), new Runnable() {
			public void run() {
			    if (fIOConsole == null) {
					IDocument document = fViewer.getDocument();
					if (document != null) {
						document.set(""); //$NON-NLS-1$
					}
					fViewer.setSelectedRange(0, 0);
			    } else {
			        fIOConsole.clearConsole();
			    }
			}
		});
	}
}
