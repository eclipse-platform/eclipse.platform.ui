package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.*;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import java.util.Iterator;

public class OpenBreakpointMarkerAction extends OpenMarkerAction {

	private static final String PREFIX= "open_breakpoint_marker_action.";	
	protected static DelegatingModelPresentation fgPresentation = new DelegatingModelPresentation();
	
	public OpenBreakpointMarkerAction(ISelectionProvider selectionProvider) {
		super(selectionProvider, DebugUIUtils.getResourceString(PREFIX + TEXT));
		setToolTipText(DebugUIUtils.getResourceString(PREFIX + TOOL_TIP_TEXT));
	}

	/**
	 * @see IAction
	 */
	public void run() {
		IWorkbenchWindow dwindow= DebugUIPlugin.getActiveWorkbenchWindow();
		IWorkbenchPage page= dwindow.getActivePage();
		IEditorPart part= null;
		// Get the resource.
		IStructuredSelection selection= (IStructuredSelection)getStructuredSelection();
		//Get the selected marker
		Iterator enum= selection.iterator();
		IMarker marker= (IMarker)enum.next();
		IEditorInput input= fgPresentation.getEditorInput(marker);
		String editorId= fgPresentation.getEditorId(input, marker);
		if (input != null) {
			try {
				part= page.openEditor(input, editorId);
			} catch (PartInitException e) {
				DebugUIUtils.logError(e);
			}
		}
		if (part != null) {
			// Bring editor to front.
			part.setFocus();

			// Goto the bookmark.
			part.gotoMarker(marker);
		}
	}
}

