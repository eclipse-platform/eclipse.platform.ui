package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.compare.structuremergeviewer.DiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.IgnoreResourcesDialog;
import org.eclipse.team.internal.ui.sync.ChangedTeamContainer;
import org.eclipse.team.internal.ui.sync.ITeamNode;
import org.eclipse.team.internal.ui.sync.MergeResource;
import org.eclipse.team.internal.ui.sync.SyncSet;
import org.eclipse.team.internal.ui.sync.TeamFile;
import org.eclipse.team.internal.ui.sync.UnchangedTeamContainer;

public class IgnoreAction extends Action {
	Shell shell;
	private CVSSyncCompareInput diffModel;
	private ISelectionProvider selectionProvider;

	public IgnoreAction(CVSSyncCompareInput model, ISelectionProvider sp, String label, Shell shell) {
		super(label);
		this.shell = shell;
		this.diffModel = model;
		this.selectionProvider = sp;
	}
	public void run() {
		IStructuredSelection selection = (IStructuredSelection)selectionProvider.getSelection();
		if (selection.isEmpty()) return;
		// Do the update
		Object first = selection.getFirstElement();
		ICVSResource cvsResource = null;
		IResource resource = null;
		if (first instanceof TeamFile) {
			resource = ((TeamFile)first).getMergeResource().getResource();
			cvsResource = CVSWorkspaceRoot.getCVSFileFor((IFile) resource);
		} else if (first instanceof ChangedTeamContainer) {
			resource = ((ChangedTeamContainer)first).getMergeResource().getResource();
			cvsResource = CVSWorkspaceRoot.getCVSFolderFor((IContainer) resource);
		}
		if (resource != null) {
			try {
				IgnoreResourcesDialog dialog = new IgnoreResourcesDialog(shell, new IResource[] {resource});
				if (dialog.open() != IgnoreResourcesDialog.OK) return;
				String pattern = dialog.getIgnorePatternFor(resource);
				cvsResource.setIgnoredAs(pattern);
			} catch (CVSException e) {
				ErrorDialog.openError(shell, null, null, e.getStatus());
				return;
			}
			removeNodes(new SyncSet(selection).getChangedNodes());
			diffModel.refresh();
		}
	}
	/**
	 * Enabled if only one item is selected and it is an outgoing addition.
	 * 
	 * This may be a folder or a single file, which will be handled differently.
	 */
	protected boolean isEnabled(Object[] nodes) {
		if (nodes.length != 1) return false;
		if (!(nodes[0] instanceof ITeamNode)) return false;
		ITeamNode node = (ITeamNode)nodes[0];
		if (node.getKind() != (ITeamNode.OUTGOING | IRemoteSyncElement.ADDITION)) return false;
		IResource resource = node.getResource();
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		try {
			return !cvsResource.isManaged();
		} catch (CVSException e) {
			CVSUIPlugin.log(e.getStatus());
			return false;
		}
	}
	public void update() {
		IStructuredSelection selection = (IStructuredSelection)selectionProvider.getSelection();
		setEnabled(isEnabled(selection.toArray()));
	}
	/**
	 * The given nodes have been synchronized.  Remove them from
	 * the sync set.
	 */
	private void removeNodes(final ITeamNode[] nodes) {
		// Update the model
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].getClass() == UnchangedTeamContainer.class) {
				// Unchanged containers get removed automatically when all
				// children are removed
				continue;
			}
			if (nodes[i].getClass() == ChangedTeamContainer.class) {
				// If this node still has children, convert to an
				// unchanged container, then it will disappear when
				// all children have been removed.
				ChangedTeamContainer container = (ChangedTeamContainer)nodes[i];
				IDiffElement[] children = container.getChildren();
				if (children.length > 0) {
					IDiffContainer parent = container.getParent();
					UnchangedTeamContainer unchanged = new UnchangedTeamContainer(parent, container.getResource());
					for (int j = 0; j < children.length; j++) {
						unchanged.add(children[j]);
					}
					parent.removeToRoot(container);
					continue;
				}
				// No children, it will get removed below.
			}
			nodes[i].getParent().removeToRoot(nodes[i]);	
		}
	}
}
