package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.ui.part.ResourceTransfer;

public class HistoryDropAdapter extends ViewerDropAdapter {
	HistoryView view;
	
	public HistoryDropAdapter(StructuredViewer viewer, HistoryView view) {
		super(viewer);
		this.view = view;
	}
	/*
	 * Override dragOver to slam the detail to DROP_LINK, as we do not
	 * want to really execute a DROP_MOVE, although we want to respond
	 * to it.
	 */
	public void dragOver(DropTargetEvent event) {
		if ((event.operations & DND.DROP_LINK) == DND.DROP_LINK) {
			event.detail = DND.DROP_LINK;
		}
		super.dragOver(event);
	}
	/*
	 * Override drop to slam the detail to DROP_LINK, as we do not
	 * want to really execute a DROP_MOVE, although we want to respond
	 * to it.
	 */
	public void drop(DropTargetEvent event) {
		super.drop(event);
		event.detail = DND.DROP_LINK;
	}
	public boolean performDrop(Object data) {
		if (data == null) return false;
		IResource[] sources = (IResource[])data;
		if (sources.length == 0) return false;
		IResource resource = sources[0];
		if (!(resource instanceof IFile)) return false;
		try {
			ITeamProvider teamProvider = TeamPlugin.getManager().getProvider(resource);
			if (teamProvider == null) return false;
			if (!(teamProvider instanceof CVSTeamProvider)) return false;
			CVSTeamProvider cvsProvider = (CVSTeamProvider)teamProvider;
			ICVSRemoteFile file = (ICVSRemoteFile)cvsProvider.getRemoteResource(resource);
			if (file == null) return false;
			view.showHistory(file);
		} catch (TeamException e) {
			return false;
		}
/*		try {
			IVersionHistory history = getHistory(resource);
			if (history == null) {
				MessageDialog.openInformation(getViewer().getControl().getShell(), WorkbenchVCMPlugin.getResourceString("BrowseHistoryAction.noHistoryShort"), WorkbenchVCMPlugin.getResourceString("BrowseHistoryAction.noHistoryLong"));
				return false;
			}
			view.showHistory(history);
		} catch (CoreException e) {
			// Log this error
			return false;
		}*/
		return true;
	}
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		if (transferType != null && ResourceTransfer.getInstance().isSupportedType(transferType)) {
			return true;
		}
		return false;
	}
}

