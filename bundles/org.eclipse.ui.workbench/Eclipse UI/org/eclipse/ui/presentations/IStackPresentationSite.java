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
package org.eclipse.ui.presentations;


/**
 * Represents the main interface between an StackPresentation and the workbench.
 * 
 * Not intended to be implemented by clients.
 * 
 * @since 3.0
 */
public interface IStackPresentationSite extends IPresentationSite {
	/**
	 * Makes the given part active
	 * 
	 * @param toSelect
	 */
	public void selectPart(IPresentablePart toSelect);
}
