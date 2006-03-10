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
package org.eclipse.debug.internal.ui.viewers.provisional;


/**
 * A model selection policy factory creates model selection policy adapter for elements based on 
 * specific presentation contexts. A model selection policy factory is provided for
 * a model element by registering a model selection policy factory adapter for
 * an element.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IModelSelectionPolicyAdapter
 * @since 3.2
 */
public interface IModelSelectionPolicyFactoryAdapter {
	/**
	 * Creates and returns a model selection policy adapter for the given element in the specified
	 * context or <code>null</code> if none.
	 * 
	 * @param element model element to create a model proxy for
	 * @param context presentation context
	 * @return model selection policy adapter or <code>null</code>
	 */
	public IModelSelectionPolicyAdapter createModelSelectionPolicyAdapter(Object element, IPresentationContext context);
}
