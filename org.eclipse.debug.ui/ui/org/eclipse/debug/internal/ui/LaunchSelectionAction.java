package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

public class LaunchSelectionAction extends Action {
	
	protected ILauncher fLauncher;
	protected String fMode;
	protected Object fElement;
	
	public LaunchSelectionAction(ILauncher launcher, Object element, String mode) {
		super();
		fLauncher= launcher;
		fMode= mode;
		fElement= element;
		setText(new DelegatingModelPresentation().getText(launcher));
		ImageDescriptor descriptor= DebugPluginImages.getImageDescriptor(launcher.getIdentifier());
		if (descriptor == null) {
			if (fMode.equals(ILaunchManager.DEBUG_MODE)) {
				descriptor= DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_DEBUG);
			} else {
				descriptor= DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN);
			}
		}

		if (descriptor != null) {
			setImageDescriptor(descriptor);
		}
		
		WorkbenchHelp.setHelp(
			this,
			new Object[] { IDebugHelpContextIds.LAUNCH_SELECTION_ACTION });
	}

	/**
	 * @see IAction
	 */
	public void run() {
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				if (fElement != null || !DebugUIPlugin.getDefault().hasWizard(fLauncher)) {
					boolean ok= fLauncher.launch(new Object[] {fElement}, fMode);
					if (!ok) {
						String string= DebugUIMessages.getString("LaunchSelectionAction.The_launcher,_{0},_failed_to_launch._1"); //$NON-NLS-1$
						String message= MessageFormat.format(string, new String[] {fLauncher.getLabel()});
						MessageDialog.openError(DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), DebugUIMessages.getString("LaunchSelectionAction.Launch_Failed_2"), message);	 //$NON-NLS-1$
					}
				} else {
					Shell shell= DebugUIPlugin.getActiveWorkbenchWindow().getShell();
					if (shell != null) {
						IProject project = null;
						IWorkbenchWindow dwindow= DebugUIPlugin.getActiveWorkbenchWindow();
						IStructuredSelection selection= ExecutionAction.resolveSelection(dwindow);
						IProject[] projects = null;
						if (selection == null) {
							projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
						} else {
							projects = ExecutionAction.resolveProjects(selection);
						}
						if (projects.length == 1) {
							project = projects[0];
						}
						useWizard(new Object[] {fLauncher}, shell, StructuredSelection.EMPTY, fLauncher, project);
					}
				}
			}
		});
	}

	/**
	 * Use the launch wizard to do the launch.
	 */
	protected void useWizard(Object[] launchers, Shell shell, IStructuredSelection selection, ILauncher launcher, IProject project) {
		LaunchWizard wizard= new LaunchWizard(launchers, selection, fMode, project, launcher);
		LaunchWizardDialog dialog= new LaunchWizardDialog(shell, wizard);
		dialog.open();
	}
}

