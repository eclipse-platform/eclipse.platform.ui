package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.*;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This wizard is used when the debug or run button is pressed, and
 * the launcher/element resolution is not 1:1. It allows the user to
 * choose a launcher and element to launch.
 *
 * <p>The renderer used to render elements to launch is pluggable,
 * allowing launchers to provide custom renderers for the elements
 * they can launch.
 */

public class LaunchWizard extends Wizard {
	
	//NLS
	private static final String PREFIX = "launch_wizard.";
	private static final String DEBUG = PREFIX + "title.debug";
	private static final String RUN = PREFIX + "title.run";

	/**
	 * The collection of available launchers
	 */
	protected Object[] fAvailableLaunchers;

	/**
	 * The selection providing context to determine launchables
	 */
	protected IStructuredSelection fSelection;

	/**
	 * The launcher selection page
	 */
	protected LaunchWizardSelectionPage fLauncherPage;
	
	/**
	 * The project selection page
	 */
	protected LaunchWizardProjectSelectionPage fProjectPage;

	/** 
	 * The mode of the wizard.
	 * @see ExecutionAction#getMode()
	 */
	protected String fMode;
	
	/**
	 * The initial project selection, or <code>null</code> if none.
	 */
	protected IProject fInitialProject;
	
	/**
	 * The initial launcher selection, or <code>null</code> if none.
	 */
	protected ILauncher fInitialLauncher;
	
	/**
	 * The old default launcher set for the <code>IProject</code>
	 * associated with the current selection.
	 */
	protected ILauncher fOldDefaultLauncher= null;
	
	/**
	 * Indicates if the default launcher has been set for the <code>IProject</code>
	 * associated with the current selection.
	 */
	protected boolean fDefaultLauncherSet= false;

	public LaunchWizard(Object[] availableLaunchers, IStructuredSelection selection, String mode, IProject initialProject, ILauncher initialLauncher) {
		fAvailableLaunchers= availableLaunchers;
		fSelection= selection;
		fMode= mode;
		fInitialProject = initialProject;
		fInitialLauncher = initialLauncher;
		initialize();
	}
	
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		WorkbenchHelp.setHelp(
			pageContainer,
			new Object[] { IDebugHelpContextIds.LAUNCH_WIZARD });
	}
	
	protected void initialize() {
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		if (fMode.equals(ILaunchManager.DEBUG_MODE)) {
			setWindowTitle(DebugUIUtils.getResourceString(DEBUG));
		} else {
			setWindowTitle(DebugUIUtils.getResourceString(RUN));
		}
		setDefaultPageImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_LAUNCH_DEBUG));
	}
	/**
	 * @see Wizard#addPages
	 */
	public void addPages() {
		addPage(fProjectPage = new LaunchWizardProjectSelectionPage(fMode, fInitialProject));
		addPage(fLauncherPage= new LaunchWizardSelectionPage(fAvailableLaunchers, fMode, fInitialLauncher));
	}
	
	public IStructuredSelection getSelection() {
		if (fSelection == null) {
			return new StructuredSelection(getProject());
		} else {
			return fSelection;
		}
	}

	/**
	 * Updates the default launcher if required - i.e. if the checkbox is
	 * checked.
	 */
	public void updateDefaultLauncher() {
		IProject project= getProject();
		if (fLauncherPage.fSetAsDefaultLauncher.getSelection()) {
			ILauncher launcher= fLauncherPage.getLauncher();
			if (launcher != null) {
				try {
					fOldDefaultLauncher= DebugPlugin.getDefault().getLaunchManager().getDefaultLauncher(project);
					DebugPlugin.getDefault().getLaunchManager().setDefaultLauncher(project, launcher);
					fDefaultLauncherSet= true;
				} catch (CoreException e) {
				}
			}
		}
	}
	
	/**
	 * Returns the <code>IProject</code> that is associated with the context selection,
	 * or <code>null</code> if there is no single project associated with the selection.
	 */
	protected IProject getProject() {
		if (fProjectPage == null) {
			return fInitialProject;
		}
		return fProjectPage.getProject();
	}
	
	/**
	 * @see IWizard#performFinish
	 */
	public boolean performFinish() {
		if (!fDefaultLauncherSet) {
			updateDefaultLauncher();
		}
		return true;
	}
	
	/**
	 * @see IWizard#performCancel
	 */
	 public boolean performCancel() {
		if (fDefaultLauncherSet) {
			try {
				DebugPlugin.getDefault().getLaunchManager().setDefaultLauncher(getProject(), fOldDefaultLauncher);
			} catch (CoreException e) {
				return false;
			}
			fDefaultLauncherSet= false;
		}
		return true;
	}
	
	/**
	 * @see IWizard#getNextPage(IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == fLauncherPage) {
			IWizardNode node= new LaunchWizardNode(page, fLauncherPage.getLauncher(), fMode);			
			IWizard wizard = node.getWizard();
			wizard.addPages();
			return wizard.getStartingPage();
		}
		return super.getNextPage(page);
	}
	
	/**
	 * @see IWizard#canFinish()
	 */
	public boolean canFinish() {
		//it is the nested wizard that will finish
		return false;
	}
	
	public IWizardPage getStartingPage() {
		if (getProject() == null) {
			return fProjectPage;
		} else {
			return fLauncherPage;
		}
	}
}

