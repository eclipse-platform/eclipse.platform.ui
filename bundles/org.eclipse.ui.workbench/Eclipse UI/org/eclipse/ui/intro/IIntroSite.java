/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.intro;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 *  
 * @since 3.0
 */
public interface IIntroSite extends IWorkbenchPartSite {

	/**
	 * Returns intro area action bars in case the intro part needs to contribute
	 * actions to menu, tool bar or status bar of the enclosing window.
	 * 
	 * @return the <code>IActionBars</code>.
	 */
	IActionBars getActionBars();
}
