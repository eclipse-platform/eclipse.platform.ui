/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

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

/**
 * RemoveBranchTagAction removes a tag.
 */
public class RemoveBranchTagAction extends CVSAction {
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
	public void execute(IAction action) throws InterruptedException, InvocationTargetException {
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
		}, false, this.PROGRESS_BUSYCURSOR);
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

