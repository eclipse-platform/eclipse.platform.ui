/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/


package org.eclipse.ui.texteditor;


import java.util.ResourceBundle;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

import org.eclipse.ui.views.tasklist.TaskPropertiesDialog;



/**
 * Adapter for the marker ruler action creating/removing tasks.
 * @since 2.0
 */
public class TaskRulerAction extends AbstractRulerActionDelegate {
	
	/**
	 * Adds a task marker over the ruler context menu. Uses the Workbench's Task properties dialog.
	 */
	static class TaskMarkerRulerAction extends MarkerRulerAction {
		
		/**
		 * Creates a new action for the given ruler and editor. The action configures
		 * its visual representation from the given resource bundle.
		 *
		 * @param bundle the resource bundle
		 * @param prefix a prefix to be prepended to the various resource keys
		 *   (described in <code>ResourceAction</code> constructor), or 
		 *   <code>null</code> if none
		 * @param editor the editor
		 * @param ruler the ruler
		 * @see ResourceAction#ResourceAction
		 */
		public TaskMarkerRulerAction(ResourceBundle bundle, String prefix, ITextEditor editor, IVerticalRulerInfo ruler) {
			super(bundle, prefix, editor, ruler, IMarker.TASK, false);
		}
		
		/*
		 * @see MarkerRulerAction#addMarker()
		 */
		protected void addMarker() {
			IResource resource= getResource();
			if (resource == null)
				return;
				
           TaskPropertiesDialog dialog = new TaskPropertiesDialog(getTextEditor().getSite().getShell());
            dialog.setResource(resource);
            dialog.setInitialAttributes(getInitialAttributes());
            dialog.open();
		}
	};

	/*
	 * @see AbstractRulerActionDelegate#createAction(ITextEditor, IVerticalRulerInfo)
	 */
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		return new TaskMarkerRulerAction(EditorMessages.getResourceBundle(), "Editor.ManageTasks.", editor, rulerInfo); //$NON-NLS-1$
	}
}
