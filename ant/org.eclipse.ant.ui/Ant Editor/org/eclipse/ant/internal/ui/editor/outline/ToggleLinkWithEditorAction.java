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

package org.eclipse.ant.internal.ui.editor.outline;

import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.IAntUIPreferenceConstants;
import org.eclipse.ant.internal.ui.editor.AntEditor;
import org.eclipse.jface.action.Action;

/**
 * This action toggles whether the Ant Outline page links its selection to the
 * active editor.
 * 
 * @since 3.0
 */
public class ToggleLinkWithEditorAction extends Action {
	
	AntEditor fEditor;
	
	public ToggleLinkWithEditorAction(AntEditor editor) {
		super(AntOutlineMessages.getString("ToggleLinkWithEditorAction.0")); //$NON-NLS-1$
		boolean isLinkingEnabled = AntUIPlugin.getDefault().getPreferenceStore().getBoolean(IAntUIPreferenceConstants.OUTLINE_LINK_WITH_EDITOR);
		setChecked(isLinkingEnabled);
		fEditor = editor;
		setToolTipText(AntOutlineMessages.getString("ToggleLinkWithEditorAction.1")); //$NON-NLS-1$
		setDescription(AntOutlineMessages.getString("ToggleLinkWithEditorAction.2")); //$NON-NLS-1$
		setImageDescriptor(AntUIImages.getImageDescriptor(IAntUIConstants.IMG_LINK_WITH_EDITOR));
	}
	
	public void run() {
		AntUIPlugin.getDefault().getPreferenceStore().setValue(IAntUIPreferenceConstants.OUTLINE_LINK_WITH_EDITOR, isChecked());
		if (isChecked())
			fEditor.synchronizeOutlinePage(false);
	}
}