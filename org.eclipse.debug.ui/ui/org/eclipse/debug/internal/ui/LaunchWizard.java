package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
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
	
	/**
	 * The collection of available launchers
	 */
	private Object[] fAvailableLaunchers;

	/**
	 * The selection providing context to determine launchables
	 */
	private IStructuredSelection fSelection;

	/**
	 * The launcher selection page
	 */
	private LaunchWizardSelectionPage fLauncherPage;
	
	/**
	 * The project selection page
	 */
	private LaunchWizardProjectSelectionPage fProjectPage;

	/** 
	 * The mode of the wizard.
	 * @see ExecutionAction#getMode()
	 */
	private String fMode;
	
	/**
	 * The initial project selection, or <code>null</code> if none.
	 */
	private IProject fInitialProject;
	
	/**
	 * The initial launcher selection, or <code>null</code> if none.
	 */
	private ILauncher fInitialLauncher;
	
	/**
	 * The old default launcher set for the <code>IProject</code>
	 * associated with the current selection.
	 */
	private ILauncher fOldDefaultLauncher= null;
	
	/**
	 * Indicates if the default launcher has been set for the <code>IProject</code>
	 * associated with the current selection.
	 */
	private boolean fDefaultLauncherSet= false;

	public LaunchWizard(Object[] availableLaunchers, IStructuredSelection selection, String mode, IProject initialProject, ILauncher initialLauncher) {
		setAvailableLaunchers(availableLaunchers);;
		setSelection(selection);
		setMode(mode);
		setInitialProject(initialProject);
		setInitialLauncher(initialLauncher);
		initialize();
	}
	
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		WorkbenchHelp.setHelp(
			pageContainer,
			IDebugHelpContextIds.LAUNCH_WIZARD);
	}
	
	protected void initialize() {
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		if (fMode.equals(ILaunchManager.DEBUG_MODE)) {
			setWindowTitle(DebugUIMessages.getString("LaunchWizard.Debug_1")); //$NON-NLS-1$
		} else {
			setWindowTitle(DebugUIMessages.getString("LaunchWizard.Run_2")); //$NON-NLS-1$
		}
		setDefaultPageImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_OBJS_LAUNCH_DEBUG));
	}
	/**
	 * @see Wizard#addPages()
	 */
	public void addPages() {
		setProjectPage(new LaunchWizardProjectSelectionPage(getMode(), getInitialProject()));
		addPage(getProjectPage());
		setLauncherPage(new LaunchWizardSelectionPage(getAvailableLaunchers(), getMode(), getInitialLauncher()));
		addPage(getLauncherPage());
	}
	
	public IStructuredSelection getSelection() {
		if (fSelection == null || fSelection.isEmpty()) {
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
		if (getLauncherPage().fSetAsDefaultLauncher.getSelection()) {
			ILauncher launcher= getLauncherPage().getLauncher();
			if (launcher != null) {
				try {
					setOldDefaultLauncher(DebugPlugin.getDefault().getLaunchManager().getDefaultLauncher(project));
					DebugPlugin.getDefault().getLaunchManager().setDefaultLauncher(project, launcher);
					setDefaultLauncherSet(true);
				} catch (CoreException e) {
					DebugUIPlugin.log(e.getStatus());
				}
			}
		}
	}
	
	/**
	 * Returns the <code>IProject</code> that is associated with the context selection,
	 * or <code>null</code> if there is no single project associated with the selection.
	 */
	protected IProject getProject() {
		if (getProjectPage() == null) {
			return getInitialProject();
		}
		return getProjectPage().getProject();
	}
	
	/**
	 * @see IWizard#performFinish()
	 */
	public boolean performFinish() {
		if (!getDefaultLauncherSet()) {
			updateDefaultLauncher();
		}
		return true;
	}
	
	/**
	 * @see IWizard#performCancel()
	 */
	 public boolean performCancel() {
		if (getDefaultLauncherSet()) {
			try {
				DebugPlugin.getDefault().getLaunchManager().setDefaultLauncher(getProject(), getOldDefaultLauncher());
			} catch (CoreException e) {
				return false;
			}
			setDefaultLauncherSet(false);
		}
		return true;
	}
	
	/**
	 * @see IWizard#getNextPage(IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == getLauncherPage()) {
			IWizardNode node= new LaunchWizardNode(page, getLauncherPage().getLauncher(), getMode());			
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
	
	/**
	 * @see IWizard#getStartingPage()
	 */
	public IWizardPage getStartingPage() {
		if (getProject() == null) {
			return getProjectPage();
		} else {
			return getLauncherPage();
		}
	}
	
	protected Object[] getAvailableLaunchers() {
		return fAvailableLaunchers;
	}

	protected void setAvailableLaunchers(Object[] availableLaunchers) {
		fAvailableLaunchers = availableLaunchers;
	}

	protected boolean getDefaultLauncherSet() {
		return fDefaultLauncherSet;
	}

	protected void setDefaultLauncherSet(boolean defaultLauncherSet) {
		fDefaultLauncherSet = defaultLauncherSet;
	}

	protected ILauncher getInitialLauncher() {
		return fInitialLauncher;
	}

	protected void setInitialLauncher(ILauncher initialLauncher) {
		fInitialLauncher = initialLauncher;
	}

	protected IProject getInitialProject() {
		return fInitialProject;
	}

	protected void setInitialProject(IProject initialProject) {
		fInitialProject = initialProject;
	}

	protected LaunchWizardSelectionPage getLauncherPage() {
		return fLauncherPage;
	}

	protected void setLauncherPage(LaunchWizardSelectionPage launcherPage) {
		fLauncherPage = launcherPage;
	}

	protected String getMode() {
		return fMode;
	}

	protected void setMode(String mode) {
		fMode = mode;
	}

	protected ILauncher getOldDefaultLauncher() {
		return fOldDefaultLauncher;
	}

	protected void setOldDefaultLauncher(ILauncher oldDefaultLauncher) {
		fOldDefaultLauncher = oldDefaultLauncher;
	}

	protected LaunchWizardProjectSelectionPage getProjectPage() {
		return fProjectPage;
	}

	protected void setProjectPage(LaunchWizardProjectSelectionPage projectPage) {
		fProjectPage = projectPage;
	}

	protected void setSelection(IStructuredSelection selection) {
		fSelection = selection;
	}
}

