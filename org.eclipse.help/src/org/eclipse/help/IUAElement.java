/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

import org.eclipse.core.expressions.IEvaluationContext;

/**
 * An element in a UA document, which may have conditional enablement (may be
 * filtered based on certain conditions) and may have sub-elements, or children.
 * 
 * @since 3.3
 */
public interface IUAElement {

	/**
	 * Returns whether or not this element should be enabled in the given
	 * context. Elements may be hidden (filtered) if certain conditions are true.
	 * 
	 * @param context the context in which the element appears
	 * @return whether or not the element is enabled in the given context
	 */
	public boolean isEnabled(IEvaluationContext context);
	
	/**
	 * Returns all sub-elements (children) of this element.
	 * 
	 * @return the sub-elements of this element
	 */
	public IUAElement[] getChildren();
}
