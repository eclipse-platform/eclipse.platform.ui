/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;

/**
 * The ILabelUpdateValidator is an object provided by the caller
 * of the update that takes objects given to it and determines if
 * they require and update.
 * @since 3.2
 * <p>
 * <strong>NOTE: </strong> This API is EXPERIMENTAL and subject to
 * change during the 3.2 release cycle.
 * </p>
 *
 */
public interface ILabelUpdateValidator {


	/**
	 * Return a String that indicates what sort of update should
	 * occur on element.
	 * @param element
	 * @return String or <code>null</code> if this element 
	 * should not be updated.
	 */
	public String shouldUpdate(Object element);

}
