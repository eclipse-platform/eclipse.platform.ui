/*
 * Created on Feb 11, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.ui.views.bookmarkexplorer;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.part.MarkerTransfer;

/**
 * @author jhalhead
 */
public class PasteBookmarkAction extends BookmarkAction {
	
	private BookmarkNavigator view;
	
	public PasteBookmarkAction(BookmarkNavigator view) {
		super(view, BookmarkMessages.getString("PasteBookmark.text"));//$NON-NLS-1$
		this.view = view;
		setEnabled(false);
	}

	/**
	 * Implementation of method defined on <code>IAction</code>.
	 */
	public void run() {
		// Get the markers from the clipboard
		MarkerTransfer transfer = MarkerTransfer.getInstance();
		final IMarker[] markerData = (IMarker[]) view.getClipboard().getContents(transfer);
	
		if (markerData == null) 
			return;

		final ArrayList newMarkers = new ArrayList();

		try {
			view.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					for (int i = 0; i < markerData.length; i++) {
						// Only paste tasks (not problems)
						if (!markerData[i].getType().equals(IMarker.BOOKMARK))
							continue;
						
						// Paste to the same resource as the original
						IResource resource = markerData[i].getResource();
						Map attributes = markerData[i].getAttributes();
						IMarker marker = resource.createMarker(IMarker.BOOKMARK);
						marker.setAttributes(attributes);
						newMarkers.add(marker);
					}
				}
			}, null);
		} catch (CoreException e) {
			ErrorDialog.openError(
				view.getShell(),
				BookmarkMessages.getString("PasteBookmark.errorTitle"), //$NON-NLS-1$
				null,
				e.getStatus());
			return;
		}

		// Need to do this in an asyncExec, even though we're in the UI thread here,
		// since the bookmark navigator updates itself with the addition in an asyncExec,
		// which hasn't been processed yet.
		// Must be done outside IWorkspaceRunnable above since notification for add is
		// sent after IWorkspaceRunnable is run.
		if (newMarkers.size() > 0) {
			view.getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					view.getViewer().setSelection(new StructuredSelection(newMarkers));
					view.updatePasteEnablement();	
				}
			});
		}
	}

}
