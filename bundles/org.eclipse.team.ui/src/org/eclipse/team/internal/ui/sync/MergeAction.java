package org.eclipse.team.internal.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Applies merge related actions to the selected ITeamNodes.
 */
abstract class MergeAction extends Action {
	public static final int CHECKIN = 0;
	public static final int GET = 1;
	public static final int DELETE_REMOTE = 2;
	public static final int DELETE_LOCAL = 3;

	private SyncCompareInput diffModel;
	private ISelectionProvider selectionProvider;

	// direction can be INCOMING or OUTGOING
	private int direction;

	// one of CHECKIN, GET, DELETE_LOCAL or DELETE_REMOTE
	private int type;
	
	/**
	 * Creates a MergeAction which works on selection and doesn't commit changes.
	 */
	public MergeAction(SyncCompareInput model, ISelectionProvider sp, int type, int direction, String label) {
		super(label);
		this.diffModel = model;
		this.selectionProvider = sp;
		this.direction = direction;
		this.type = type;
	}
	
	/**
	 * Returns true if at least one node can perform the specified action.
	 */
	private boolean isEnabled(Object[] nodes) {
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] instanceof ITeamNode) {
				ITeamNode node = (ITeamNode)nodes[i];
				if (isMatchingKind(node.getKind())) 
					return true;
			} else {
				if (nodes[i] instanceof IDiffContainer)
					if (isEnabled(((IDiffContainer)nodes[i]).getChildren()))
						return true;
			}
		}
		return false;
	}

	protected abstract boolean isMatchingKind(int kind);

	/**
	 * Perform the sychronization operation.
	 */
	public void run() {
		ISelection s = selectionProvider.getSelection();
		if (!(s instanceof IStructuredSelection) || s.isEmpty()) {
			return;
		}
		SyncSet set = new SyncSet((IStructuredSelection)s, direction);
		set.removeNonApplicableNodes();
		diffModel.sync(set, type);
	}

	/**
	 * Updates the action with the latest selection, setting enablement
	 * as necessary.
	 */
	public void update() {
		IStructuredSelection selection = (IStructuredSelection)selectionProvider.getSelection();
		setEnabled(isEnabled(selection.toArray()));
	}
}
