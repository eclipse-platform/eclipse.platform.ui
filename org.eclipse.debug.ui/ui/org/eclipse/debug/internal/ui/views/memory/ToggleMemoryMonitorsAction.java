/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;


/**
 * Toggles the visiblity of a view pane.
 * @since 3.1
 *
 */
public class ToggleMemoryMonitorsAction extends ToggleViewPaneAction {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.ToggleViewPaneAction#getPaneId()
	 */
	public String getPaneId() {
		return MemoryBlocksTreeViewPane.PANE_ID;
	}

}
