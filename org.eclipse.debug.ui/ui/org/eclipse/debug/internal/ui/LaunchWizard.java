package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.*;
import java.util.Iterator;

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
	protected Object[] fLaunchers;

	/**
	 * The selection providing context to determine launchables
	 */
	protected IStructuredSelection fSelection;

	/**
	 * The launch page
	 */
	protected LaunchWizardSelectionPage fPage;

	/** 
	 * The mode of the wizard.
	 * @see ExecutionAction#getMode()
	 */
	protected String fMode;
	
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

	/**
	 * Indicates if the wizard needs to determine the launcher to use
	 */
	 protected boolean fSelectLauncher;
	 
	/**
	 * Constructs a wizard with a set of launchers, a selection, a mode 
	 * and whether to select a launcher.
	 */
	public LaunchWizard(Object[] allLaunchers, IStructuredSelection selection, String mode, boolean selectLauncher) {
		fSelectLauncher= selectLauncher;
		fLaunchers= allLaunchers;
		fSelection= selection;
		fMode= mode;
		initialize();
	}
	
	public LaunchWizard(Object[] allLaunchers, IStructuredSelection selection, String mode) {
		this(allLaunchers, selection, mode, true);
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
		if (fSelection == null || fSelection.isEmpty()) {
			addPage(new LaunchWizardProjectSelectionPage(fMode));
		}
		if (fSelectLauncher) {		
			addPage(fPage= new LaunchWizardSelectionPage(fLaunchers, fMode));	
		}
	}
	
	public IStructuredSelection getSelection() {
		return fSelection;
	}

	/**
	 * Updates the default launcher if required - i.e. if the checkbox is
	 * checked.
	 */
	public void updateDefaultLauncher() {
		IProject project= getProject();
		if (fSelectLauncher && fPage.fSetAsDefaultLauncher.getSelection()) {
			ILauncher launcher= fPage.getLauncher();
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
		IProject project= null;
		Iterator elements= fSelection.iterator();
		while (elements.hasNext()) {
			Object e= elements.next();
			IResource res= null;
			if (e instanceof IAdaptable) {
				res= (IResource) ((IAdaptable) e).getAdapter(IResource.class);
			}
			if (res != null) {
				IProject p= res.getProject();
				if (project == null) {
					project= p;
				} else
					if (!project.equals(p)) {
						return null;
					}
			}
		}

		return project;
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
	 * Sets the selection that is the context for the launch.
	 */
	public void setProjectSelection(IStructuredSelection selection) {
		fSelection= selection;
	}
	
	/**
	 * @see IWizard#getNextPage(IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		if (!fSelectLauncher) {
			IWizardNode node= new LaunchWizardNode(page, (ILauncher)fLaunchers[0], fMode);			
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
}

