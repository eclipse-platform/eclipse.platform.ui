package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.model.RemoteModule;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * DefineTagAction remembers a tag by name.
 * 
 * This action needs to be reworked in order to associate tags with individual
 * projects, rather than define them globally (for all projects).
 * 
 * The selection object for this action is a RemoteModule.
 */
public class DefineVersionAction extends TeamAction {
	IInputValidator validator = new IInputValidator() {
		public String isValid(String newText) {
			IStatus status = CVSTag.validateTagName(newText);
			if (status.isOK()) return null;
			return status.getMessage();
		}
	};

	/**
	 * Returns the selected remote projects
	 */
	protected RemoteModule[] getSelectedRemoteModules() {
		ArrayList resources = null;
		if (!selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = ((IStructuredSelection)selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof RemoteModule) {
					resources.add(next);
					continue;
				}
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(RemoteModule.class);
					if (adapter instanceof RemoteModule) {
						resources.add(adapter);
						continue;
					}
				}
			}
		}
		if (resources != null && !resources.isEmpty()) {
			RemoteModule[] result = new RemoteModule[resources.size()];
			resources.toArray(result);
			return result;
		}
		return new RemoteModule[0];
	}
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				final RemoteModule[] projects = getSelectedRemoteModules();
				if (projects.length != 1) return;
				Shell shell = getShell();
				shell.getDisplay().syncExec(new Runnable() {
					public void run() {
						InputDialog dialog = new InputDialog(getShell(), Policy.bind("DefineVersionAction.enterTag"), Policy.bind("DefineVersionAction.enterTagLong"), null, validator);
						if (dialog.open() == InputDialog.OK) {
							ICVSRemoteResource resource = projects[0].getCVSResource();
							CVSTag tag = new CVSTag(dialog.getValue(), CVSTag.VERSION);
							CVSUIPlugin.getPlugin().getRepositoryManager().addVersionTags((ICVSFolder)resource, new CVSTag[] {tag});
						}
					}
				});
			}
		}, Policy.bind("DefineVersionAction.tag"), this.PROGRESS_DIALOG);
	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		RemoteModule[] projects = getSelectedRemoteModules();
		if (projects.length != 1) return false;
		return true;
	}
}

