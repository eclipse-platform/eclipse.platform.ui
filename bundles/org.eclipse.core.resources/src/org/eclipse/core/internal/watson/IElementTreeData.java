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
package org.eclipse.core.internal.watson;

/**
 * User data that can be attached to the element tree itself.
 */
public interface IElementTreeData extends Cloneable {
	/**
	 * ElementTreeData must define a publicly accessible clone method.
	 * This method can simply invoke Object's clone method.
	 */
	public Object clone();
}
