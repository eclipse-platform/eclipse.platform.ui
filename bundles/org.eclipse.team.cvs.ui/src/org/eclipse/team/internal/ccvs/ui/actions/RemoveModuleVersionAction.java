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
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.internal.ccvs.ui.model.ModuleVersion;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * RemoveTagAction removes a tag.
 */
public class RemoveModuleVersionAction extends TeamAction {
	/**
	 * Returns the selected versions
	 */
	protected ModuleVersion[] getSelectedModuleVersions() {
		ArrayList tags = null;
		if (!selection.isEmpty()) {
			tags = new ArrayList();
			Iterator elements = ((IStructuredSelection)selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof ModuleVersion) {
					tags.add(next);
					continue;
				}
			}
		}
		if (tags != null && !tags.isEmpty()) {
			ModuleVersion[] result = new ModuleVersion[tags.size()];
			tags.toArray(result);
			return result;
		}
		return new ModuleVersion[0];
	}
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				ModuleVersion[] tags = getSelectedModuleVersions();
				if (tags.length == 0) return;
				RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
				for (int i = 0; i < tags.length; i++) {
					ModuleVersion tag = tags[i];
					manager.removeVersionTags(tag.getCVSResource(), new CVSTag[] {tag.getTag()});
				}
			}
		}, Policy.bind("RemoveModuleVersionAction.removeTag"), this.PROGRESS_BUSYCURSOR);

	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ModuleVersion[] tags = getSelectedModuleVersions();
		if (tags.length == 0) return false;
		for (int i = 0; i < tags.length; i++) {
			if (tags[i].getTag().equals("HEAD")) return false;
		}
		return true;
	}
}

