/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerLabelProvider;
import org.eclipse.swt.graphics.Rectangle;

class OwnerDrawLabelProvider extends ViewerLabelProvider{
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
	 */
	public void update(ViewerCell cell) {
		Rectangle cellBounds = cell.getBounds();
		cell.getControl().redraw(cellBounds.x, cellBounds.y, cellBounds.width,
				cellBounds.height, true);
	}
}