package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.LaunchWizard;
import org.eclipse.debug.internal.ui.LaunchWizardDialog;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

public class LaunchSelectionAction extends Action {
	
	private ILauncher fLauncher;
	private String fMode;
	private Object fElement;
	
	public LaunchSelectionAction(ILauncher launcher, Object element, String mode) {
		super();
		setLauncher(launcher);
		setMode(mode);
		setElement(element);
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
	 * @see IAction#run()
	 */
	public void run() {
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				if (getElement() != null || !DebugUIPlugin.getDefault().hasWizard(getLauncher())) {
					getLauncher().launch(new Object[] {getElement()}, getMode());
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
						useWizard(new Object[] {getLauncher()}, shell, StructuredSelection.EMPTY, getLauncher(), project);
					}
				}
			}
		});
	}

	/**
	 * Use the launch wizard to do the launch.
	 */
	protected void useWizard(Object[] launchers, Shell shell, IStructuredSelection selection, ILauncher launcher, IProject project) {
		LaunchWizard wizard= new LaunchWizard(launchers, selection, getMode(), project, launcher);
		LaunchWizardDialog dialog= new LaunchWizardDialog(shell, wizard);
		dialog.open();
	}
	
	protected Object getElement() {
		return fElement;
	}

	public void setElement(Object element) {
		fElement = element;
	}

	protected ILauncher getLauncher() {
		return fLauncher;
	}

	protected void setLauncher(ILauncher launcher) {
		fLauncher = launcher;
	}

	protected String getMode() {
		return fMode;
	}

	protected void setMode(String mode) {
		fMode = mode;
	}
}

