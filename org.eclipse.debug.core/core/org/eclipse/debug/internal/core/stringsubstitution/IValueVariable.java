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
package org.eclipse.debug.internal.core.stringsubstitution;

/**
 * A variable with a value that can be set and retrieved. The context in which
 * a value variable is referenced does not effect the value of the variable.
 * A value variable can be contributed by an extension or created programmatically.
 * 
 * @since 3.0
 */
public interface IValueVariable extends IStringVariable {

	/**
	 * Sets the value of this variable to the given String. A value of
	 * <code>null</code> indicates the value of this variable is undefined.
	 * 
	 * @param value variable value, possibly <code>null</code>
	 */
	public void setValue(String value);
}
