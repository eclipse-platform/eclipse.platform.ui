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


public class ResolveBuildfileAction extends Action {
	
	public ResolveBuildfileAction() {
		super("Resolve Buildfile",  AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT));
		setToolTipText("Resolve the Ant Buildfile to Check for Errors");
		setChecked(AntUIPlugin.getDefault().getPreferenceStore().getBoolean(AntEditorPreferenceConstants.RESOLVE_BUILDFILES));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		AntUIPlugin.getDefault().getPreferenceStore().setValue(AntEditorPreferenceConstants.RESOLVE_BUILDFILES, isChecked());
	}
}
