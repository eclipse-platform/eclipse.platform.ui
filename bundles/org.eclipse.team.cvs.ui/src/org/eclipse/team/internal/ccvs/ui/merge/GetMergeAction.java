package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.internal.ccvs.ui.sync.*;
import org.eclipse.team.ui.sync.ITeamNode;
import org.eclipse.team.ui.sync.SyncSet;
import org.eclipse.team.ui.sync.TeamFile;

public class GetMergeAction extends GetSyncAction {
	public GetMergeAction(CVSSyncCompareInput model, ISelectionProvider sp, int direction, String label, Shell shell) {
		super(model, sp, direction, label, shell);
	}
	protected SyncSet run(SyncSet syncSet, IProgressMonitor monitor) {
		// If there is a conflict in the syncSet, we need to prompt the user before proceeding.
		if (syncSet.hasConflicts() || syncSet.hasOutgoingChanges()) {
			String[] buttons = new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL};
			String question = Policy.bind("GetSyncAction.questionCatchup");
			String title = Policy.bind("GetSyncAction.titleCatchup");
			String[] tips = new String[] {
				Policy.bind("GetSyncAction.catchupAll"),
				Policy.bind("GetSyncAction.catchupPart"),
				Policy.bind("GetSyncAction.cancelCatchup")
			};
			Shell shell = getShell();
			final ToolTipMessageDialog dialog = new ToolTipMessageDialog(shell, title, null, question, MessageDialog.QUESTION, buttons, tips, 0);
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					dialog.open();
				}
			});
			switch (dialog.getReturnCode()) {
				case 0:
					// Yes, synchronize conflicts as well
					break;
				case 1:
					// No, only synchronize non-conflicting changes.
					syncSet.removeConflictingNodes();
					break;
				case 2:
				default:
					// Cancel
					return null;
			}	
		}
		ITeamNode[] changed = syncSet.getChangedNodes();
		if (changed.length == 0) {
			return syncSet;
		}
		
		try {
			for (int i = 0; i < changed.length; i++) {
				switch (changed[i].getKind() & Differencer.CHANGE_TYPE_MASK) {
					case Differencer.ADDITION:
						IResource resource = changed[i].getResource();
						if (resource instanceof IFile) {
							createParentStructure(((IFile)resource).getParent(), monitor);
							InputStream stream = ((TeamFile)changed[i]).getMergeResource().getSyncElement().getRemote().getContents(monitor);
							((IFile)resource).create(stream, true, monitor);
						} else if (resource instanceof IFolder) {
							createParentStructure((IFolder)resource, monitor);
						}
						break;
					case Differencer.DELETION:
						changed[i].getResource().delete(true, monitor);
						break;
					case Differencer.CHANGE:
						resource = changed[i].getResource();
						if (resource instanceof IFile) {
							InputStream stream = ((TeamFile)changed[i]).getMergeResource().getSyncElement().getRemote().getContents(monitor);
							((IFile)resource).setContents(stream, true, true, monitor);
						}
						break;
				}
			}
		} catch (TeamException e) {
			ErrorDialog.openError(getShell(), null, null, e.getStatus());
			return null;
		} catch (CoreException e) {
			CVSUIPlugin.log(e.getStatus());
			return null;
		}
		return syncSet;
	}
	/**
	 * Create the folder and all its parents
	 */
	private void createParentStructure(IContainer container, IProgressMonitor monitor) throws CoreException {
		Vector v = new Vector();
		if (container.exists()) return;
		while (!container.exists()) {
			v.add(0, container);
			container = container.getParent();
		}
		Iterator it = v.iterator();
		while (it.hasNext()) {
			((IFolder)it.next()).create(true, true, monitor);
		}
	}
}
