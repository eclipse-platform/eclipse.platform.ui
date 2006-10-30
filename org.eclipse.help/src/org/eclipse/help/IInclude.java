/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

/**
 * <p>
 * An include node is a marker to signal that content from another document
 * should be pulled into the document in which the include resides.
 * </p>
 * <p>
 * IMPORTANT: This API is still subject to change in 3.3. This interface may be
 * removed in favor of using an untyped model similar to DOM.
 * </p>
 * 
 * @since 3.3
 */
public interface IInclude extends INode {

	/**
	 * Returns a reference to the target content to be included. The format of
	 * the string depends on the context of the include.
	 * 
	 * @return the include target
	 */
	public String getTarget();
}
