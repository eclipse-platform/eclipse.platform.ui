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

import org.eclipse.jface.viewers.ISelection;

/**
 * Resolves selection conflicts within a model. When
 * a selection exists in a viewer, and a client asks
 * to set another selection, the selection policy for
 * that model is asked to determine whether the new
 * selection should override the previous selection.
 * When a selection is from a different model, the
 * original selection is maintained if its model selection
 * policy specifies the selection to be 'sticky'.
 * <p>
 * A selection policy is obtained as an adapter from
 * an element in a viewer, and the adapter represents
 * selections from an instance of a model.
 * </p>
 * @since 3.2
 */
public interface IModelSelectionPolicy {
	
	/**
	 * Returns whether the given selection is contained in
	 * this model.
	 * 
	 * @param selection
	 * @param context
	 * @return
	 */
	public boolean contains(ISelection selection, IPresentationContext context);
	
	/**
	 * Returns whether the candidate selection overrides the
	 * existing selection. The policy is only asked about selections
	 * that it contains.
	 * 
	 * @param existing
	 * @param candidate
	 * @param context
	 * @return
	 */
	public boolean overrides(ISelection existing, ISelection candidate, IPresentationContext context);

	/**
	 * Returns whether the given selection should be maintained in the
	 * face of a selection attempt from a different model.
	 *  
	 * @param selection
	 * @param context
	 * @return
	 */
	public boolean isSticky(ISelection selection, IPresentationContext context);
	
}
