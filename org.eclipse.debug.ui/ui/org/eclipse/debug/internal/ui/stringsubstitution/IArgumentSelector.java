/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.stringsubstitution;

import org.eclipse.core.variables.IStringVariable;
import org.eclipse.swt.widgets.Shell;


/**
 * A variable presentation extension can contribute an argument selector
 * which is use to configure the argument for a string substitution 
 * variable.
 * 
 * @since 3.0
 */
public interface IArgumentSelector {

	/**
	 * Selects and returns an argument for the given variable, 
	 * or <code>null</code> if none.
	 * 
	 * @param variable the variable an arugment is being seleted for
	 * @param the shell to create any dialogs on, or <code>null</code> if none
	 * @return argument for the given variable or <code>null</code>
	 *  if none
	 */
	public String selectArgument(IStringVariable variable, Shell shell);
}
