package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.internal.ccvs.ui.model.BranchTag;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * RemoveBranchTagAction removes a tag.
 */
public class RemoveBranchTagAction extends TeamAction {
	/**
	 * Returns the selected versions
	 */
	protected BranchTag[] getSelectedBranchTags() {
		ArrayList tags = null;
		if (!selection.isEmpty()) {
			tags = new ArrayList();
			Iterator elements = ((IStructuredSelection)selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof BranchTag) {
					tags.add(next);
					continue;
				}
			}
		}
		if (tags != null && !tags.isEmpty()) {
			BranchTag[] result = new BranchTag[tags.size()];
			tags.toArray(result);
			return result;
		}
		return new BranchTag[0];
	}
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				BranchTag[] tags = getSelectedBranchTags();
				if (tags.length == 0) return;
				RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
				for (int i = 0; i < tags.length; i++) {
					BranchTag tag = tags[i];
					manager.removeBranchTag(tag.getRoot(), new CVSTag[] {tag.getTag()});
				}
			}
		}, Policy.bind("RemoveBranchTagAction.removeTag"), this.PROGRESS_BUSYCURSOR); //$NON-NLS-1$

	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		BranchTag[] tags = getSelectedBranchTags();
		if (tags.length == 0) return false;
		for (int i = 0; i < tags.length; i++) {
			if (tags[i].getTag().getName().equals("HEAD")) return false; //$NON-NLS-1$
		}
		return true;
	}
}

