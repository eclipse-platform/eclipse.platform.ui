/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 
package org.eclipse.ant.internal.ui.editor;

import org.eclipse.ant.internal.ui.model.AntUIImages;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.jface.action.Action;


public class ValidateBuildfileAction extends Action {
	
	public ValidateBuildfileAction() {
		super(AntEditorMessages.getString("ValidateBuildfileAction.0"),  AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT)); //$NON-NLS-1$
		setToolTipText(AntEditorMessages.getString("ValidateBuildfileAction.1")); //$NON-NLS-1$
		setChecked(AntUIPlugin.getDefault().getPreferenceStore().getBoolean(AntEditorPreferenceConstants.VALIDATE_BUILDFILES));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		AntUIPlugin.getDefault().getPreferenceStore().setValue(AntEditorPreferenceConstants.VALIDATE_BUILDFILES, isChecked());
	}
}