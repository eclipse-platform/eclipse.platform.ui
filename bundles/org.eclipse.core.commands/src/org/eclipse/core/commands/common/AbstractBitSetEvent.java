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
package org.eclipse.core.commands.common;

/**
 * <p>
 * An event that carries with it two or more boolean values.  This provides a
 * single integer value which can then be used as a bit set.
 * </p>
 * 
 * @since 3.1
 */
public abstract class AbstractBitSetEvent {

	/**
	 * A collection of bits representing whether certain values have changed. A
	 * bit is set (i.e., <code>1</code>) if the corresponding property has
	 * changed. It can be assumed that this value will be correctly initialized
	 * by the superconstructor.
	 */
	protected int changedValues = 0;
}
