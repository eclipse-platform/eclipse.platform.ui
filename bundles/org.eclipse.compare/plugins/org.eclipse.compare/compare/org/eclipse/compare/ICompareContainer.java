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
package org.eclipse.compare;

import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * A compare container is used to represent any UI that can contain compare viewers.
 * <p>
 * This interface is not intended to be implemented by clients.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * 
 * @since 3.3
 */
public interface ICompareContainer {
	
	/**
	 * Register for change events for the given compare input. Although clients can register
	 * with the compare input directly, registering through the container allows for 
	 * deterministic and optimized behavior in some cases. Registering multiple times for the 
	 * same compare input has no effect.
	 * @param input the compare input
	 * @param listener the compare input change listener
	 */
	public void addCompareInputChangeListener(ICompareInput input, ICompareInputChangeListener listener);
	
	/**
	 * Remove the change listener from the given compare input. Removing a listener that is not
	 * registered has no effect.
	 * @param input the compare input
	 * @param listener the compare input change listener
	 */
	public void removeCompareInputChangeListener(ICompareInput input, ICompareInputChangeListener listener);

	/**
	 * Register the content menu with the container to give the container a chance to
	 * add additional items to the context menu such as popup menu object contributions.
	 * The provided menu should have a {@link IWorkbenchActionConstants#MB_ADDITIONS}
	 * separator as this is where the container will add actions.
	 * @param menu the menu being registered
	 * @param selectionProvider the selection provider
	 */
	public void registerContextMenu(MenuManager menu, ISelectionProvider selectionProvider);
	
}
