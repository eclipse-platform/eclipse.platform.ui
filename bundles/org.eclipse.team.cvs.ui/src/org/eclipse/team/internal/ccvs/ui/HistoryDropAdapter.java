package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
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
	/**
	 * Returns a resource history for the given resource, or null
	 * if none available.
	 */
	/*private IVersionHistory getHistory(IResource resource) throws CoreException {
		ISharingManager manager = VCMPlugin.getProvider().getSharingManager();
		if (resource.getType() == IResource.PROJECT) {
	        ITeamStream stream = manager.getSharing(resource);
	        if (stream != null) {
		        return stream.getRepository().fetchProjectHistory(resource.getName(), null);
	        }
		} else {
			IResourceEdition base = manager.getBaseVersion(resource);
			if (base != null) {
				return base.fetchVersionHistory(null);
			}
			IResourceEdition remote = manager.getRemoteResource(resource);
			if (remote != null) {
				try {
					return remote.fetchVersionHistory(null);
				} catch (CoreException e) {
					//this exception means there is no remote resource
					//so return null for the version history.
					return null;
				}
			}
		}
		return null;
	}*/
	public boolean performDrop(Object data) {
		if (data == null) return false;
		IResource[] sources = (IResource[])data;
		if (sources.length == 0) return false;
		IResource resource = sources[0];
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

