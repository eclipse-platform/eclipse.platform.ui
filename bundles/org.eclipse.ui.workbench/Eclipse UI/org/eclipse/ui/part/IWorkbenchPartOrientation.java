/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;

/**
 * The IWorkbenchPartOrientation is the interface that defines the orientation
 * of the part. If a type does not implement this interface an orientation of
 * SWT.NONE will be assumed.
 * 
 * @see org.eclipse.swt.SWT#RIGHT_TO_LEFT
 * @see org.eclipse.swt.SWT#LEFT_TO_RIGHT 
 * @see org.eclipse.swt.SWT#NONE
 * @see Window#getDefaultOrientation()
 * @since 3.1
 */
public interface IWorkbenchPartOrientation {
	/**
	 * Return the orientation of this part.
	 * 
	 * @return int SWT#RIGHT_TO_LEFT or SWT#LEFT_TO_RIGHT
	 * @see Window#getDefaultOrientation()
	 * @see SWT#RIGHT_TO_LEFT
	 * @see SWT#LEFT_TO_RIGHT
	 * @see Window#getDefaultOrientation()
	 */
	public int getOrientation();
}
