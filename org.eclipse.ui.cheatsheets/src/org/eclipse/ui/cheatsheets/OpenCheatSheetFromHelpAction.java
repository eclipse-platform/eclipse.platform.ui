/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.cheatsheets;

import org.eclipse.help.ILiveHelpAction;
import org.eclipse.swt.widgets.Display;

/**
 * Live help action for launching a cheat sheet from a help book.
 * <p>
 * The initialization string passed to {@link #setInitializationString(String)}
 * is the id of a cheat sheet contributed to the <code>cheatsheetContent</code>
 * extension point.
 * </p>
 * 
 * @since 3.0
 */
public final class OpenCheatSheetFromHelpAction implements ILiveHelpAction {
	
	/**
	 * Cheat sheet id; null until initialized.
	 */
	private String cheatsheetID = null;

	/**
	 * Creates a new live help action.
	 */
	public OpenCheatSheetFromHelpAction() {
		super();
	}

	/* (non-javadoc)
	 * This method is called by the eclipse framework.  The initialization string must be the id of a 
	 * registered cheat sheet in order for the action to work.
	 * @see ILiveHelpAction#setInitializationString(String)
	 */
	public void setInitializationString(String data) {
		cheatsheetID = data;
	}

	/* (non-javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// Active help does not run on the UI thread, so we must use syncExec
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				new OpenCheatSheetAction(cheatsheetID).run();
			}
		});
	}
}
