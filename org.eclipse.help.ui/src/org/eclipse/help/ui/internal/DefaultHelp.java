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
package org.eclipse.help.ui.internal;
import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.ui.internal.util.*;

/**
 * This class is an implementation of the pluggable help support.
 * In is registered into the support extension point, and all 
 * requests to display help are delegated to this class.
 * The methods on this class interact with the actual
 * UI component handling the display.
 * <p>Most methods are inherited from the default hep support class; only
 * the UI specific ones are overriden.</p>
 */
public class DefaultHelp extends DefaultHelpSupport {
	private static DefaultHelp instance;
	private ContextHelpDialog f1Dialog = null;

	/**
	 * BaseHelpViewer constructor.
	 */
	public DefaultHelp() {
		super();
		instance = this;
	}

	/**
	 * Singleton method
	 */
	public static DefaultHelp getInstance() {
		return instance;
	}

	/**
	 * Displays context-sensitive help for specified context
	 * @param contexts the context to display
	 * @param x int positioning information
	 * @param y int positioning information
	 */
	public void displayContext(IContext context, int x, int y) {
		if (f1Dialog != null)
			f1Dialog.close();
		if (context == null)
			return;
		f1Dialog = new ContextHelpDialog(context, x, y);
		f1Dialog.open();
		// if any errors or parsing errors have occurred, display them in a pop-up
		ErrorUtil.displayStatus();
	}

	/**
	 * Returns <code>true</code> if the context-sensitive help
	 * window is currently being displayed, <code>false</code> if not.
	 */
	public boolean isContextHelpDisplayed() {
		if (f1Dialog == null) {
			return false;
		}
		return f1Dialog.isShowing();
	}
}
