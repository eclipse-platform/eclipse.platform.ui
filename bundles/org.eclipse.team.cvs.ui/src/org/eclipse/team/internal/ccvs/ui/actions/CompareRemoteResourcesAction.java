package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.ui.CVSCompareEditorInput;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.ResourceEditionNode;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * This action is used for comparing two arbitrary resource
 * editions.
 */
public class CompareRemoteResourcesAction extends TeamAction {
	/* (non-Javadoc)
	 * Method declared in IActionDelegate.
	 */
	public void run(IAction action) {
		ICVSRemoteResource[] editions = getSelectedRemoteResources();
		if (editions == null || editions.length != 2) {
			MessageDialog.openError(getShell(), Policy.bind("CompareRemoteResourcesAction.unableToCompare"), Policy.bind("CompareRemoteResourcesAction.selectTwoResources"));
			return;
		}
		ResourceEditionNode left = new ResourceEditionNode(editions[0]);
		ResourceEditionNode right = new ResourceEditionNode(editions[1]);
		CompareUI.openCompareEditor(new CVSCompareEditorInput(left, right));
	}

	/**
	 * Returns the selected remote resources
	 */
	protected ICVSRemoteResource[] getSelectedRemoteResources() {
		ArrayList resources = null;
		if (!selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = ((IStructuredSelection)selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof ICVSRemoteResource) {
					resources.add(next);
					continue;
				}
				if (next instanceof ILogEntry) {
					resources.add(((ILogEntry)next).getRemoteFile());
					continue;
				}
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(ICVSRemoteResource.class);
					if (adapter instanceof ICVSRemoteResource) {
						resources.add(adapter);
						continue;
					}
				}
			}
		}
		if (resources != null && !resources.isEmpty()) {
			ICVSRemoteResource[] result = new ICVSRemoteResource[resources.size()];
			resources.toArray(result);
			return result;
		}
		return new ICVSRemoteResource[0];
	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSRemoteResource[] resources = getSelectedRemoteResources();
		return resources.length == 2;
	}
}
