package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * RemoveModuleVersionAction removes a tag.
 */
public class RemoveModuleVersionAction extends TeamAction {
	/**
	 * Returns the selected versions
	 */
	protected ICVSRemoteFolder[] getSelectedModuleVersions() {
		ArrayList tags = null;
		if (!selection.isEmpty()) {
			tags = new ArrayList();
			Iterator elements = ((IStructuredSelection)selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof ICVSRemoteFolder) {
					tags.add(next);
					continue;
				}
			}
		}
		if (tags != null && !tags.isEmpty()) {
			ICVSRemoteFolder[] result = new ICVSRemoteFolder[tags.size()];
			tags.toArray(result);
			return result;
		}
		return new ICVSRemoteFolder[0];
	}
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				ICVSRemoteFolder[] folders = getSelectedModuleVersions();
				if (folders.length == 0) return;
				RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
				for (int i = 0; i < folders.length; i++) {
					ICVSRemoteFolder folder = folders[i];
					manager.removeVersionTags(folder, new CVSTag[] {folder.getTag()});
				}
			}
		}, Policy.bind("RemoveModuleVersionAction.removeTag"), this.PROGRESS_BUSYCURSOR);

	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSRemoteFolder[] versions = getSelectedModuleVersions();
		if (versions.length == 0) return false;
		for (int i = 0; i < versions.length; i++) {
			CVSTag tag = versions[i].getTag();
			if (tag == null) return false;
			if (tag.getType() != CVSTag.VERSION) return false;
			if (versions[i].getRemoteParent() != null) return false;
		}
		return true;
	}
}

