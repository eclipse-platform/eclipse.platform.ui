/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

/**
 * FIXME: missing Javadoc.
 *
 * @since 3.4
 */
public interface IDelayedInputChangeListener {

	/**
	 * Called when a delayed input change request comes in.
	 * 
	 * @param input the new input, or <code>null</code> iff the listener should cancel
	 * operation and not show any input.
	 */
	void inputChanged(Object input);
}
