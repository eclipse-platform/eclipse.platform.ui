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
 * An action which toggles filtering of subtargets from the Ant outline.
 */
public class FilterSubtargetsAction extends Action {
	
	private AntEditorContentOutlinePage fPage;
	
	public FilterSubtargetsAction(AntEditorContentOutlinePage page) {
		super(AntOutlineMessages.getString("FilterSubtargetsAction.0")); //$NON-NLS-1$
		fPage = page;
		setImageDescriptor(AntUIImages.getImageDescriptor(IAntUIConstants.IMG_FILTER_SUBTARGETS));
		setToolTipText(AntOutlineMessages.getString("FilterSubtargetsAction.1")); //$NON-NLS-1$
		setChecked(fPage.isFilterSubtargets());
	}
	
	/**
	 * Toggles the filtering of subtarget from the Ant outline
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		fPage.setFilterSubtargets(isChecked());
	}
}