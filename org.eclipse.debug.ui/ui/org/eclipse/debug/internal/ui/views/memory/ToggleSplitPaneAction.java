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

import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Toggles the visibility of the rendering view pane 2.
 * @since 3.1
 *
 */
public class ToggleSplitPaneAction extends ToggleViewPaneAction {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.ToggleViewPaneAction#getPaneId()
	 */
	public String getPaneId() {
		return IDebugUIConstants.ID_RENDERING_VIEW_PANE_2;
	}

}
