package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchManager;import org.eclipse.debug.core.ILauncher;import org.eclipse.debug.ui.IDebugUIConstants;import org.eclipse.jface.action.Action;import org.eclipse.jface.resource.ImageDescriptor;import org.eclipse.jface.viewers.IStructuredSelection;import org.eclipse.jface.viewers.StructuredSelection;import org.eclipse.swt.custom.BusyIndicator;import org.eclipse.swt.widgets.Display;import org.eclipse.swt.widgets.Shell;import org.eclipse.ui.help.WorkbenchHelp;

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
		ImageDescriptor descriptor= null;
		if (fMode.equals(ILaunchManager.DEBUG_MODE)) {
			descriptor= DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_DEBUG);
		} else {
			descriptor= DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_ACT_RUN);
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
				if (fElement != null) {
					fLauncher.launch(new Object[] {fElement}, fMode);
				} else {
					Shell shell= DebugUIPlugin.getActiveWorkbenchWindow().getShell();
					if (shell != null) {
						useWizard(new Object[] {fLauncher}, shell, StructuredSelection.EMPTY);
					}
				}
			}
		});
	}

	/**
	 * Use the launch wizard to do the launch.
	 */
	protected void useWizard(Object[] launchers, Shell shell, IStructuredSelection selection) {
		LaunchWizard wizard= new LaunchWizard(launchers, selection, fMode, false);
		LaunchWizardDialog dialog= new LaunchWizardDialog(shell, wizard);
		dialog.open();
	}
}

