/*******************************************************************************
 * Copyright (c) 2011 Texas Instruments and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Texas Instruments - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;

/**
 * Extension to allow column presentation to specify initial column sizes in a 
 * TreeModelViewer.
 * 
 * @since 3.7
 * 
 * @see TreeModelViewer
 */
public interface IColumnPresentation2 extends IColumnPresentation {
	
	/**
	 * Returns the initial column width for the column with the given identifier.
	 * 
	 * @param id a column identifier included in <code>getAvailableColumns()</code>
	 * @param treeWidgetWidth tree widget width
	 * @param visibleColumnIds identifiers of visible columns 
	 * @return initial column width. Return -1 if this implementation does not decide the width
	 *         and let the caller to decide the width. The caller may make the initial width
	 *         be treeWidgetWidth / visibleColumnIds.length
	 */
	public int getInitialColumnWidth(String id, int treeWidgetWidth, String[] visibleColumnIds);

}
