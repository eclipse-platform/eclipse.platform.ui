package org.eclipse.team.internal.ccvs.ui.actions;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.wizards.GenerateDiffFileWizard;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action to generate a patch file using the CVS diff command.
 * 
 * NOTE: This is a temporary action and should eventually be replaced
 * by a create patch command in the compare viewer.
 */
public class GenerateDiffFileAction implements IObjectActionDelegate {
	private ISelection selection;
	private IWorkbenchPart part;

	public GenerateDiffFileAction() {
	}
	/**
	 * Makes sure that the projects of all selected resources are shared.
	 * Returns true if all resources are shared, and false otherwise.
	 */
	protected boolean checkSharing(IResource[] resources) throws CoreException {
		HashSet projects = new HashSet(10);

		for (int i = 0; i < resources.length; i++) {
			ITeamProvider provider = TeamPlugin.getManager().getProvider(resources[i]);
			if (!(provider instanceof CVSTeamProvider))
				return false;
		}
		return true;
	}
	/**
	 * Convenience method: extract all <code>IResources</code> from given selection.
	 * Never returns null.
	 */
	public static IResource[] getResources(ISelection selection) {
		ArrayList tmp = new ArrayList();
		if (selection instanceof IStructuredSelection) {
			Object[] s = ((IStructuredSelection) selection).toArray();
			for (int i = 0; i < s.length; i++) {
				Object o = s[i];
				if (o instanceof IResource) {
					tmp.add(o);
					continue;
				}
				if (o instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) o;
					Object adapter = a.getAdapter(IResource.class);
					if (adapter instanceof IResource)
						tmp.add(adapter);
					continue;
				}
			}
		}
		IResource[] resourceSelection = new IResource[tmp.size()];
		tmp.toArray(resourceSelection);
		return resourceSelection;
	}
	/**
	 * Convenience method for getting the current shell.
	 */
	protected Shell getShell() {
		return CVSUIPlugin
			.getPlugin()
			.getWorkbench()
			.getActiveWorkbenchWindow()
			.getShell();
	}
	/** (Non-javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		final String title = Policy.bind("GenerateCVSDiff.title");
		final String message = Policy.bind("GenerateCVSDiff.pageTitle");
		try {
			final IResource[] resources = getResources(selection);
			if (!checkSharing(resources)) {
				//canceled
				return;
			}

			GenerateDiffFileWizard wizard =
				new GenerateDiffFileWizard(new StructuredSelection(resources), resources);
			wizard.setWindowTitle(title);
			WizardDialog dialog = new WizardDialog(getShell(), wizard);
			dialog.setMinimumPageSize(350, 250);
			dialog.open();
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(), title, null, e.getStatus());
		}
	}
	/** (Non-javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void selectionChanged(IAction action, ISelection s) {
		selection = s;
		IResource[] resources = getResources(s);
		for (int i = 0; i < resources.length; i++) {
			if (!resources[i].isAccessible()) {
				action.setEnabled(false);
				return;
			}
		}
		action.setEnabled(true);
	}
	/** (Non-javadoc)
	 * Method declared on IObjectActionDelegate.
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.part = targetPart;
	}
}