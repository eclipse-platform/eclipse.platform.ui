/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.outline;

import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.jface.action.Action;

/**
 * An action which toggles sorting in the Ant outline page.
 */
public class ToggleSortAntOutlineAction extends Action {

	private AntEditorContentOutlinePage fPage;
	
	public ToggleSortAntOutlineAction(AntEditorContentOutlinePage page) {
		super(AntOutlineMessages.getString("ToggleSortAntOutlineAction.0")); //$NON-NLS-1$
		fPage = page;
		setImageDescriptor(AntUIImages.getImageDescriptor(IAntUIConstants.IMG_SORT_OUTLINE));
		setToolTipText(AntOutlineMessages.getString("ToggleSortAntOutlineAction.0")); //$NON-NLS-1$
		setChecked(fPage.isSort());
	}
	
	/**
	 * Toggles the sorting of targets in the Ant outline
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fPage.setSort(isChecked());
	}
	
}
