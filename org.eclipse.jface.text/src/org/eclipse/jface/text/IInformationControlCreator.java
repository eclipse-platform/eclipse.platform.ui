/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;


import org.eclipse.swt.widgets.Shell;


/**
 * Interface of a factory of information controls.
 * 
 * @since 2.0
 */
public interface IInformationControlCreator {
	
	/**
	 * Creates a new information control with the given shell as the control's parent.
	 * 
	 * @param parent the parent shell
	 * @return the created information control
	 */
	IInformationControl createInformationControl(Shell parent);
}

