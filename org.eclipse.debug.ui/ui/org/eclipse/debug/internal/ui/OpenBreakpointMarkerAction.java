package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Iterator;import org.eclipse.core.resources.IMarker;import org.eclipse.jface.viewers.ISelectionProvider;import org.eclipse.jface.viewers.IStructuredSelection;import org.eclipse.ui.*;import org.eclipse.ui.help.WorkbenchHelp;

public class OpenBreakpointMarkerAction extends OpenMarkerAction {

	private static final String PREFIX= "open_breakpoint_marker_action.";	
	protected static DelegatingModelPresentation fgPresentation = new DelegatingModelPresentation();
	
	public OpenBreakpointMarkerAction(ISelectionProvider selectionProvider) {
		super(selectionProvider, DebugUIUtils.getResourceString(PREFIX + TEXT));
		setToolTipText(DebugUIUtils.getResourceString(PREFIX + TOOL_TIP_TEXT));
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.OPEN_BREAKPOINT_ACTION });
	}

	/**
	 * @see IAction
	 */
	public void run() {
		IWorkbenchWindow dwindow= DebugUIPlugin.getActiveWorkbenchWindow();
		IWorkbenchPage page= dwindow.getActivePage();
		if (page == null) {
			return;
		}
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

