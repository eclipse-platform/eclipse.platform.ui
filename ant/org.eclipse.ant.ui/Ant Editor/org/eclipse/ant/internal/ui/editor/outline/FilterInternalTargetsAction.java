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

import org.eclipse.ant.internal.ui.model.AntUIImages;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.jface.action.Action;

/** 
 * An action which toggles filtering of internal targets from the Ant outline.
 */
public class FilterInternalTargetsAction extends Action {
	
	private AntEditorContentOutlinePage fPage;
	
	public FilterInternalTargetsAction(AntEditorContentOutlinePage page) {
		super(AntOutlineMessages.getString("FilterInternalTargetsAction.0")); //$NON-NLS-1$
		fPage = page;
		setImageDescriptor(AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT_TARGET_INTERNAL));
		setToolTipText(AntOutlineMessages.getString("FilterInternalTargetsAction.0")); //$NON-NLS-1$
		setChecked(fPage.isFilterInternalTargets());
	}
	
	/**
	 * Toggles the filtering of internal targets from the Ant outline
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fPage.setFilterInternalTargets(isChecked());
	}
}