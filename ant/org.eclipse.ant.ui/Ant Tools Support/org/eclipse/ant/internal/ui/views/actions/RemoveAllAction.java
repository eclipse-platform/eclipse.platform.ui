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
package org.eclipse.ant.internal.ui.views.actions;


import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.IAntUIHelpContextIds;
import org.eclipse.ant.internal.ui.views.AntView;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;

public class RemoveAllAction extends Action implements IUpdate {
	private AntView view;
	
	public RemoveAllAction(AntView view) {
		super(AntViewActionMessages.getString("RemoveAllAction.Remove_All"), AntUIImages.getImageDescriptor(IAntUIConstants.IMG_REMOVE_ALL)); //$NON-NLS-1$
		setDescription(AntViewActionMessages.getString("RemoveAllAction.Remove_All")); //$NON-NLS-1$
		setToolTipText(AntViewActionMessages.getString("RemoveAllAction.Remove_All")); //$NON-NLS-1$
		this.view= view;
		WorkbenchHelp.setHelp(this, IAntUIHelpContextIds.REMOVE_ALL_ACTION);
	}
	
	public void run() {
		view.removeAllProjects();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setEnabled(view.getProjectViewer().getTree().getItemCount() != 0);
	}
}
