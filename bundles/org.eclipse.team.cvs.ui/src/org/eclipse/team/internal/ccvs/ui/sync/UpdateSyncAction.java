package org.eclipse.team.internal.ccvs.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.ui.sync.ChangedTeamContainer;
import org.eclipse.team.ui.sync.ITeamNode;
import org.eclipse.team.ui.sync.SyncSet;
import org.eclipse.team.ui.sync.TeamFile;

/**
 * UpdateSyncAction is run on a set of sync nodes when the "Update" menu item is performed
 * in the Synchronize view.
 */
public class UpdateSyncAction extends MergeAction {
	public static class ConfirmDialog extends MessageDialog {

		private boolean autoMerge = true;
		private Button radio1;
		private Button radio2;
		
		public ConfirmDialog(Shell parentShell) {
			super(
				parentShell, 
				Policy.bind("UpdateSyncAction.Conflicting_changes_found_1"),  //$NON-NLS-1$
				null,	// accept the default window icon
				Policy.bind("UpdateSyncAction.You_have_local_changes_you_are_about_to_overwrite_2"), //$NON-NLS-1$
				MessageDialog.QUESTION, 
				new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL},
				0); 	// yes is the default
		}
		
		protected Control createCustomArea(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			radio1 = new Button(composite, SWT.RADIO);
			radio1.addSelectionListener(selectionListener);
			
			radio1.setText(Policy.bind("UpdateSyncAction.Only_update_resources_that_can_be_automatically_merged_3")); //$NON-NLS-1$

			radio2 = new Button(composite, SWT.RADIO);
			radio2.addSelectionListener(selectionListener);

			radio2.setText(Policy.bind("UpdateSyncAction.Update_all_resources,_overwriting_local_changes_with_remote_contents_4")); //$NON-NLS-1$
			
			// set initial state
			radio1.setSelection(autoMerge);
			radio2.setSelection(!autoMerge);
			
			return composite;
		}
		
		private SelectionListener selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button)e.widget;
				if (button.getSelection()) {
					autoMerge = (button == radio1);
				}
			}
		};
		
		public boolean getAutomerge() {
			return autoMerge;
		}
	}

	public UpdateSyncAction(CVSSyncCompareInput model, ISelectionProvider sp, String label, Shell shell) {
		super(model, sp, label, shell);
	}

	protected SyncSet run(final SyncSet syncSet, IProgressMonitor monitor) {
		// If there are conflicts or outgoing changes in the syncSet, we need to warn the user.
		boolean onlyUpdateAutomergeable = false;
		if (syncSet.hasConflicts() || syncSet.hasOutgoingChanges()) {
			if (syncSet.hasAutoMergeableConflicts()) {
				switch (promptForMergeableConflicts()) {
					case 0: // cancel
						return null;
					case 1: // only update auto-mergeable conflicts
						onlyUpdateAutomergeable = true;
						syncSet.removeNonMergeableNodes();
						break;
					case 2: // update all conflicts
						onlyUpdateAutomergeable = false;
						break;
				}				
			} else {
				if (! promptForConflicts()) return null;				
			}
		}
		
		ITeamNode[] changed = syncSet.getChangedNodes();
		if (changed.length == 0) {
			return syncSet;
		}
		
		List updateIgnoreLocalShallow = new ArrayList();
		List updateDeep = new ArrayList();
		List updateShallow = new ArrayList();

		// A list of diff elements in the sync set which are incoming folder additions
		Set parentCreationElements = new HashSet();
		// A list of diff elements in the sync set which are folder conflicts
		Set parentConflictElements = new HashSet();
		// A list of the team nodes that we need to perform makeIncoming on
		List makeIncoming = new ArrayList();
		// A list of diff elements that need to be unmanaged and locally deleted
		List deletions = new ArrayList();
		
		for (int i = 0; i < changed.length; i++) {
			IDiffContainer parent = changed[i].getParent();
			if (parent != null) {
				int parentKind = changed[i].getParent().getKind();
				if (((parentKind & Differencer.CHANGE_TYPE_MASK) == Differencer.ADDITION) &&
					((parentKind & Differencer.DIRECTION_MASK) == ITeamNode.INCOMING)) {
					parentCreationElements.add(parent);
				} else if ((parentKind & Differencer.DIRECTION_MASK) == ITeamNode.CONFLICTING) {
					parentConflictElements.add(parent);
				}
			}
			ITeamNode changedNode = changed[i];
			int kind = changedNode.getKind();
			switch (kind & Differencer.DIRECTION_MASK) {
				case ITeamNode.INCOMING:
					switch (kind & Differencer.CHANGE_TYPE_MASK) {
						case Differencer.ADDITION:
							updateIgnoreLocalShallow.add(changedNode);
							break;
						case Differencer.DELETION:
						case Differencer.CHANGE:
							updateDeep.add(changedNode);
							break;
					}
					break;
				case ITeamNode.OUTGOING:
					switch (kind & Differencer.CHANGE_TYPE_MASK) {
						case Differencer.ADDITION:
							// Unmanage the file if necessary and delete it.
							deletions.add(changedNode);
							break;
						case Differencer.DELETION:
							makeIncoming.add(changedNode);
							updateDeep.add(changedNode);
							break;
						case Differencer.CHANGE:
							updateIgnoreLocalShallow.add(changedNode);
							break;
					}
					break;
				case ITeamNode.CONFLICTING:
					switch (kind & Differencer.CHANGE_TYPE_MASK) {
						case Differencer.ADDITION:
							// To do: conflicting addition: must make incoming first
							makeIncoming.add(changedNode);
							deletions.add(changedNode);
							updateIgnoreLocalShallow.add(changedNode);
							break;
						case Differencer.DELETION:
							// Doesn't happen, these nodes don't appear in the tree.
							break;
						case Differencer.CHANGE:
							// Depends on the flag.
							if (onlyUpdateAutomergeable && (changedNode.getKind() & IRemoteSyncElement.AUTOMERGE_CONFLICT) != 0) {
								updateShallow.add(changedNode);
							} else {
								updateIgnoreLocalShallow.add(changedNode);
							}
							break;
					}
					break;
			}
		}
		try {
			// Calculate the total amount of work needed
			int work = (makeIncoming.size() + (deletions.size() * 2) + updateShallow.size() + updateIgnoreLocalShallow.size() + updateDeep.size()) * 100;
			monitor.beginTask(null, work);

			RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
			if (parentCreationElements.size() > 0) {
				// If a node has a parent that is an incoming folder creation, we have to 
				// create that folder locally and set its sync info before we can get the
				// node itself. We must do this for all incoming folder creations (recursively)
				// in the case where there are multiple levels of incoming folder creations.
				Iterator it = parentCreationElements.iterator();
				while (it.hasNext()) {
					makeInSync((IDiffElement)it.next());
				}				
			}
			if (parentConflictElements.size() > 0) {
				// If a node has a parent that is a folder conflict, that means that the folder
				// exists locally but has no sync info. In order to get the node, we have to 
				// create the sync info for the folder (and any applicable parents) before we
				// get the node itself.
				Iterator it = parentConflictElements.iterator();
				while (it.hasNext()) {
					makeInSync((IDiffElement)it.next());
				}				
			}
			// Make any outgoing changes or deletions into incoming changes before updating.
			Iterator it = makeIncoming.iterator();
			while (it.hasNext()) {
				ITeamNode node = (ITeamNode)it.next();
				CVSRemoteSyncElement element = CVSSyncCompareInput.getSyncElementFrom(node);
				element.makeIncoming(Policy.subMonitorFor(monitor, 100));
			}
			// Outgoing additions must be unmanaged (if necessary) and locally deleted.
			it = deletions.iterator();
			while (it.hasNext()) {
				ITeamNode node = (ITeamNode)it.next();
				CVSRemoteSyncElement element = CVSSyncCompareInput.getSyncElementFrom(node);
				element.makeIncoming(Policy.subMonitorFor(monitor, 100));
				element.getLocal().delete(true, Policy.subMonitorFor(monitor, 100));
			}
			
			if (updateShallow.size() > 0) {
				runUpdateShallow((ITeamNode[])updateShallow.toArray(new ITeamNode[updateShallow.size()]), manager, Policy.subMonitorFor(monitor, 100));
			}
			if (updateIgnoreLocalShallow.size() > 0) {
				runUpdateIgnoreLocalShallow((ITeamNode[])updateIgnoreLocalShallow.toArray(new ITeamNode[updateIgnoreLocalShallow.size()]), manager, Policy.subMonitorFor(monitor, 100));
			}
			if (updateDeep.size() > 0) {
				runUpdateDeep((ITeamNode[])updateDeep.toArray(new ITeamNode[updateDeep.size()]), manager, Policy.subMonitorFor(monitor, 100));
			}
		} catch (final TeamException e) {
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					ErrorDialog.openError(getShell(), null, null, e.getStatus());
				}
			});
			return null;
		} catch (final CoreException e) {
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					ErrorDialog.openError(getShell(), Policy.bind("simpleInternal"), Policy.bind("internal"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			});
			CVSUIPlugin.log(e.getStatus());
			return null;
		} finally {
			monitor.done();
		}
		return syncSet;
	}
	
	protected void runUpdateDeep(ITeamNode[] nodes, RepositoryManager manager, IProgressMonitor monitor) throws TeamException {
		manager.update(getIResourcesFrom(nodes), Command.NO_LOCAL_OPTIONS, false, monitor);
	}
	
	protected void runUpdateIgnoreLocalShallow(ITeamNode[] nodes, RepositoryManager manager, IProgressMonitor monitor) throws TeamException {
		manager.update(getIResourcesFrom(nodes), new Command.LocalOption[] { Update.IGNORE_LOCAL_CHANGES, Command.DO_NOT_RECURSE }, false, monitor);
	}
	
	protected void runUpdateShallow(ITeamNode[] nodes, RepositoryManager manager, IProgressMonitor monitor) throws TeamException {
		manager.update(getIResourcesFrom(nodes), new Command.LocalOption[] { Command.DO_NOT_RECURSE }, false, monitor);
	}
	
	protected void makeInSync(IDiffElement parentElement) throws TeamException {
		// Recursively make the parent element (and its parents) in sync.
		// Walk up and find the parents which need to be made in sync too. (For
		// each parent that doesn't already have sync info).
		Vector v = new Vector();
		int parentKind = parentElement.getKind();
		while (((parentKind & Differencer.CHANGE_TYPE_MASK) == Differencer.ADDITION) &&
			((parentKind & Differencer.DIRECTION_MASK) == ITeamNode.INCOMING)) {
			v.add(0, parentElement);
			parentElement = parentElement.getParent();
			parentKind = parentElement == null ? 0 : parentElement.getKind();
		}
		Iterator parentIt = v.iterator();
		while (parentIt.hasNext()) {
			IDiffElement next = (IDiffElement)parentIt.next();
			if (next instanceof ChangedTeamContainer) {
				CVSRemoteSyncElement syncElement = (CVSRemoteSyncElement)((ChangedTeamContainer)next).getMergeResource().getSyncElement();
				// Create the sync info
				syncElement.makeInSync(new NullProgressMonitor());
			}
		}
	}
			
	protected IResource[] getIResourcesFrom(ITeamNode[] nodes) {
		List resources = new ArrayList(nodes.length);
		for (int i = 0; i < nodes.length; i++) {
			resources.add(nodes[i].getResource());
		}
		return (IResource[]) resources.toArray(new IResource[resources.size()]);
	}
	
	protected boolean isEnabled(ITeamNode node) {
		// The update action is enabled only for non-conflicting incoming changes
		return new SyncSet(new StructuredSelection(node)).hasIncomingChanges();
	}
	
	/**
	 * Prompt for mergeable conflicts.
	 * @return 0 to cancel, 1 to only update mergeable conflicts, 2 to overwrite if unmergeable
	 */
	protected int promptForMergeableConflicts() {
		final boolean doAutomerge[] = new boolean[] {false};
		final int[] result = new int[] {Dialog.CANCEL};
		final Shell shell = getShell();
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				ConfirmDialog dialog = new ConfirmDialog(shell);
				result[0] = dialog.open();
				doAutomerge[0] = dialog.getAutomerge();
			}
		});
		if (result[0] == Dialog.CANCEL) return 0;
		return doAutomerge[0] ? 1 : 2;
	}
	
	/**
	 * Prompt for non-automergeable conflicts.
	 * @return false to cancel, true to overwrite local changes
	 */
	protected boolean promptForConflicts() {
		final boolean[] result = new boolean[] { false };
		final Shell shell = getShell();
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				result[0] = MessageDialog.openQuestion(shell, Policy.bind("UpdateSyncAction.Overwrite_local_changes__5"), Policy.bind("UpdateSyncAction.You_have_local_changes_you_are_about_to_overwrite._Do_you_wish_to_continue__6")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
		return result[0];
	}
	protected void removeNonApplicableNodes(SyncSet set, int syncMode) {
		set.removeConflictingNodes();
		set.removeOutgoingNodes();
	}
}
