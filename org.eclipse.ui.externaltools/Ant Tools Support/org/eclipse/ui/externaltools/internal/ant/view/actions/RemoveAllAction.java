package org.eclipse.ui.externaltools.internal.ant.view.actions;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
import org.eclipse.jface.action.Action;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;

public class RemoveAllAction extends Action {
	private AntView view;
	
	public RemoveAllAction(AntView view) {
		super("Remove All Build Files", ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_REMOVE_ALL));
		setDescription("Remove all build files");
		setToolTipText("Remove All Build Files");
		this.view= view;
	}
	
	public void run() {
		view.removeAllProjects();
	}

}
