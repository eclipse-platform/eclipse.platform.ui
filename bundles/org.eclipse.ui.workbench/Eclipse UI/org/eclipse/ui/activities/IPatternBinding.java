/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.activities;

/**
 * <p>
 * An instance of <code>IPatternBinding</code> represents a binding between an
 * activity and a pattern.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see org.eclipse.ui.activities.IActivity
 */
public interface IPatternBinding extends Comparable {

	/**
	 * Returns the pattern represented in this binding.
	 * 
	 * @return the pattern. Guaranteed not to be <code>null</code>.
	 */	
	String getPattern();
}
