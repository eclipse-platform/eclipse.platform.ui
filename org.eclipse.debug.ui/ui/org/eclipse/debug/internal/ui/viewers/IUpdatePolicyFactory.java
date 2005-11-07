/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers;


/**
 * Used to create update policies for elements. An update policy factory
 * should be registered as adapters for root elements in a model. 
 * 
 * @since 3.2
 */
public interface IUpdatePolicyFactory {
	
	/**
	 * Creates and returns an update policy to use for the given element in the
	 * specified context, or <code>null</code> if none.
	 * 
	 * @param element the element to be updated
	 * @param context the context in which the element is being presented
	 * @return update policy or <code>null</code>
	 */
	public IUpdatePolicy createUpdatePolicy(IPresentationContext context);

}
