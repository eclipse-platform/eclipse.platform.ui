/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.databinding.viewers;

/**
 * Constants used to describe properties of JFace viewers.
 * 
 * @since 3.2
 * 
 */
public interface ViewersProperties {

	/**
	 * Property ID constant denoting the content collection property of JFace
	 * structured viewers.
	 */
	public static final String CONTENT = "content"; //$NON-NLS-1$

	/**
	 * Property ID constant denoting the (single) selection property of JFace
	 * structured viewers.
	 */
	public static final String SINGLE_SELECTION = "single_selection"; //$NON-NLS-1$

	/**
	 * Property ID constant denoting the (multi) selection collection property
	 * of JFace structured viewers.
	 */
	public static final String MULTI_SELECTION = "multi_selection"; //$NON-NLS-1$

}
