package org.eclipse.ui.externaltools.internal.ant.view.actions;
/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.externaltools.internal.ant.view.AntView;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;

public class SearchForBuildFilesAction extends Action {
	private AntView view;
	
	public SearchForBuildFilesAction(AntView view) {
		super("Search", ExternalToolsImages.getImageDescriptor(IExternalToolsUIConstants.IMG_SEARCH));
		setToolTipText("Add build files with search");
		this.view= view;
	}
	
	public void run() {
		SearchForBuildFilesDialog dialog= new SearchForBuildFilesDialog();
		if (dialog.open() != Dialog.CANCEL) {
			IFile[] files= dialog.getResults();
			for (int i = 0; i < files.length; i++) {
				view.addBuildFile(files[i].getLocation().toString());				
			}
		}		
	}

}
