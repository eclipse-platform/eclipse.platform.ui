package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.ui.ILaunchWizard;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Point;

/**
 * A wizard node represents a "potential" wizard. Wizard nodes
 * allow the user to pick from several available nested wizards.
 */
public class LaunchWizardNode implements IWizardNode {
	
	private static final String PREFIX= "launch_wizard_node.";
	private static final String ERROR= PREFIX + "error.";
	
	protected IWizard fWizard;
	protected IWizardPage fParentWizardPage;
	protected ILauncher fLauncher;
	
	protected String fMode;
	/**
	 * Creates a node that holds onto a wizard element.
	 * The wizard element provides information on how to create
	 * the wizard supplied by the ISV's extension.
	 */
	public LaunchWizardNode(IWizardPage aWizardPage, ILauncher launcher, String mode) {
		fParentWizardPage= aWizardPage;
		fLauncher= launcher;
		fMode= mode;
	}

	/**
	 * Returns the wizard represented by this wizard node.
	 */
	public ILaunchWizard createWizard() throws CoreException {
		IConfigurationElement config= fLauncher.getConfigurationElement();
		ILaunchWizard wizard= (ILaunchWizard)DebugUIPlugin.getDefault().createExtension(config, "wizard");
		wizard.init(fLauncher, fMode, ((LaunchWizard)fParentWizardPage.getWizard()).getSelection());
		return wizard;
	}
	
	/**
	 * @see org.eclipse.jface.wizard.IWizardNode#getExtent()
	 */
	public Point getExtent() {
		return new Point(-1, -1);
	}

	/**
	 * @see org.eclipse.jface.wizards.IWizardNode#getWizard()
	 */
	public IWizard getWizard() {
		if (fWizard != null) {
			return fWizard; // we've already created it
		}
		try {
			fWizard= createWizard(); // create instance of target wizard
		} catch (CoreException e) {			
			DebugUIUtils.errorDialog(fParentWizardPage.getControl().getShell(), ERROR, e.getStatus());
			return null;
		}

		return fWizard;
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizardNode#isContentCreated()
	 */
	public boolean isContentCreated() {
		return fWizard != null;
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizardNode#dispose()
	 */
	public void dispose() {
		// Do nothing since the wizard wasn't created via reflection.
		fWizard= null;
	}
	
	/**
	 * Returns the description specified for the launcher associated
	 * with the wizard node.
	 */
	public String getDescription() {
		IConfigurationElement config= fLauncher.getConfigurationElement();
		String description= config.getAttribute("description");
		if (description == null) {
			description= "";
		}
		return description;
	}
}

