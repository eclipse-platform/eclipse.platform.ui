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
package org.eclipse.debug.internal.ui.views.variables;

import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * Experimental: Provides logical content for a value, rather than all content.
 * 
 * @since 3.0
 */
public interface IValueContentProvider {

	/**
	 * Returns a logical set of variables contained in the given value.
	 * 
	 * @param value
	 * @return
	 */
	public IVariable[] getVariables(IValue value);
}
