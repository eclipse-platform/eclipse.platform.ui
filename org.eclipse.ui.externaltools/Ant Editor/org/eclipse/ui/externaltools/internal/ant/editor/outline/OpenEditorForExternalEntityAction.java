package org.eclipse.ui.externaltools.internal.ant.editor.outline;

/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 ******************************************************************************/

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlElement;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class OpenEditorForExternalEntityAction extends Action {

	private ContentOutlinePage page;
	
	public OpenEditorForExternalEntityAction(ContentOutlinePage page) {
		super(AntOutlineMessages.getString("OpenEditorForExternalEntityAction.&Open_Editor_1")); //$NON-NLS-1$
		setDescription(AntOutlineMessages.getString("OpenEditorForExternalEntityAction.Open_an_editor")); //$NON-NLS-1$
		this.page= page;
		WorkbenchHelp.setHelp(this, IExternalToolsHelpContextIds.OPEN_EDITOR_ACTION);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		IStructuredSelection selection= (IStructuredSelection)page.getSelection();
		XmlElement element= (XmlElement)selection.getFirstElement();
		String path= element.getFilePath();
		if (path != null) {
			IPath resourcePath= new Path(path);
			//resourcePath.
			IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
			IResource resource= root.getFileForLocation(resourcePath);
			if (resource != null && resource.getType() == IResource.FILE) {
				try {
					IEditorPart editor= page.getSite().getPage().openEditor((IFile)resource);
					if (!(editor instanceof ITextEditor)) {
						return;
					}
					ITextEditor textEditor= (ITextEditor)editor;
					textEditor.selectAndReveal(element.getOffset(), element.getLength());
				} catch (PartInitException e) {
					IStatus status= new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, IStatus.ERROR, "Error within External tools UI: ", e); //$NON-NLS-1$	
					ErrorDialog.openError(page.getSite().getShell(), AntOutlineMessages.getString("OpenEditorForExternalEntityAction.Error"), AntOutlineMessages.getString("OpenEditorForExternalEntityAction.Error_occurred"), status); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#isEnabled()
	 */
	public boolean isEnabled() {
		ISelection iselection= page.getSelection();
		if (iselection instanceof IStructuredSelection) {
			IStructuredSelection selection= (IStructuredSelection)iselection;
			if (selection.size() == 1) {
				Object selected= selection.getFirstElement();
				if (selected instanceof XmlElement) {
					XmlElement element= (XmlElement)selected;
					return element.isExternal() && !element.isRootExternal();
				}
			}
		}
		return false;
	}
}
