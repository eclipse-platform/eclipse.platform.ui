/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
 * The primary interface between an intro part and the workbench.
 * <p>
 * The workbench exposes its implemention of intro part sites via this 
 * interface, which is not intended to be implemented or extended by clients.
 * </p>
 *  
 * @since 3.0
 */
public interface IIntroSite extends IWorkbenchPartSite {

	/**
	 * Returns the action bars for this part site.
	 *
	 * @return the <code>IActionBars</code>
	 */
	IActionBars getActionBars();
}
