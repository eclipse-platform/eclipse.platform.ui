package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * A preference page to enable/disable default launchers for a resource.
 */
public class LauncherPropertyPage extends PropertyPage implements IWorkbenchPreferencePage {

	protected Combo fCombo;
	
	/**
	 * Utility method that creates a combo instance
	 * and sets the default layout data.
	 */

	private Combo createCombo(Composite parent, String[] items) {
		Combo combo= new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setItems(items);
		GridData data= new GridData();
		data.horizontalAlignment= GridData.HORIZONTAL_ALIGN_BEGINNING;
		combo.setLayoutData(data);
		return combo;
	}

	/**
	 * Creates composite control and sets the default layout data.
	 */

	private Composite createComposite(Composite parent, int numColumns) {
		Composite composite= new Composite(parent, SWT.NULL);

		//GridLayout
		GridLayout layout= new GridLayout();
		layout.numColumns= numColumns;
		composite.setLayout(layout);

		//GridData
		GridData data= new GridData();
		data.verticalAlignment= GridData.FILL;
		data.horizontalAlignment= GridData.FILL;
		composite.setLayoutData(data);
		return composite;
	}

	protected Control createContents(Composite parent){
		return null;
	}
	/**
	 * Creates preference page controls on demand.
	 */
	public void createControl(Composite parent) {

		if (!getProject().isOpen()) {
			createForClosedProject(parent);
			return;
		}
		// Table to choose which tags are shown in the outliner
		Composite pageComponent= createComposite(parent, 1);
		createLabel(pageComponent, DebugUIMessages.getString("LauncherPropertyPage.Select_default_launcher_1")); //$NON-NLS-1$
		ImageRegistry registry= DebugUIPlugin.getDefault().getImageRegistry();

		ILauncher[] launchers= getLaunchManager().getLaunchers();
		List list = new ArrayList(1);
		list.add(DebugUIMessages.getString("LauncherPropertyPage.none_2")); //$NON-NLS-1$
		for (int i = 0; i < launchers.length; i++) {
			if (DebugUIPlugin.getDefault().isVisible(launchers[i]))
				list.add(launchers[i].getLabel());
		}
		String[] items= new String[list.size()];
		list.toArray(items);

		Composite launcherComponent= createComposite(pageComponent, 2);
		createLabel(launcherComponent, DebugUIMessages.getString("LauncherPropertyPage.Run/Debug_3"), registry.get(IDebugUIConstants.IMG_ACT_DEBUG)); //$NON-NLS-1$
		fCombo= createCombo(launcherComponent, items);

		initializeValues();
		setControl(pageComponent);
	}

	private void createForClosedProject(Composite parent) {
		Label label= new Label(parent, SWT.LEFT);
		label.setText(DebugUIMessages.getString("LauncherPropertyPage.Launcher_information_is_not_available_for_a_closed_project._4")); //$NON-NLS-1$
		label.setFont(parent.getFont());
		setControl(label);
	}
	/**
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 */
	private Label createLabel(Composite parent, String text) {
		Label label= new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data= new GridData();
		data.horizontalAlignment= GridData.FILL;
		label.setLayoutData(data);
		return label;
	}

	/**
	 * Utility method that creates a composite label instance
	 * and sets the default layout data.
	 */

	private Composite createLabel(Composite parent, String text, Image image) {
		Composite composite= createComposite(parent, 2);
		Label imageLabel= new Label(composite, SWT.LEFT);
		imageLabel.setImage(image);
		Label textLabel= new Label(composite, SWT.LEFT);
		textLabel.setText(text);

		GridData data= new GridData();
		data.horizontalAlignment= GridData.HORIZONTAL_ALIGN_BEGINNING;
		composite.setLayoutData(data);

		return composite;
	}

	/**
	 * The default button has been pressed.
	 */
	protected void performDefaults() {
		super.performDefaults();
		// Select "none"
		fCombo.select(0);
	}

	/**
	 * Returns preference store that belongs to the our plugin.
	 * This is important because we want to store
	 * our preferences separately from the desktop.
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return DebugUIPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * This is a hook for subclasses to do special things when the ok
	 * button is pressed.
	 */
	public boolean performOk() {
		IProject project= getProject();
		if (project != null && project.isOpen()) {
			String selection= fCombo.getText();
			return saveLauncherProperty(project, selection);
		}
		return true;
	}

	/**
	 * Convenience method to get the launch manager
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Returns the project this page works on
	 */
	protected IProject getProject() {
		IAdaptable element= getElement();
		IResource resource= (IResource)element.getAdapter(IResource.class);
		if (resource != null) {
			return resource.getProject();
		}
		return null;
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench desktop) {
		doGetPreferenceStore();
	}

	/**
	 * Initializes states of the controls.
	 */
	private void initializeValues() {

		IProject project= getProject();
		if (project != null) {
			try {
				ILauncher launcher= getLaunchManager().getDefaultLauncher(project);
				if (launcher == null) {
					fCombo.select(0);
				} else {
					fCombo.setText(launcher.getLabel());
				}
			} catch (CoreException ce) {
				fCombo.select(0);
				DebugUIPlugin.log(ce);
			}
		}
	}

	/**
	 * Find the launcher with the given selection (name), and save it as the preference.
	 */
	protected boolean saveLauncherProperty(IProject project, String selection) {
		try {
			ILauncher[] launchers= getLaunchManager().getLaunchers();
			for (int i= 0; i < launchers.length; i++) {
				ILauncher launcher= launchers[i];
				if (launcher.getLabel().equals(selection)) {
					getLaunchManager().setDefaultLauncher(project, launcher);
					return true;
				}
			}
			getLaunchManager().setDefaultLauncher(project, null);
		} catch (CoreException e) {
			DebugUIPlugin.errorDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), DebugUIMessages.getString("LauncherPropertyPage.Launcher_property_page_5"), DebugUIMessages.getString("LauncherPropertyPage.Exceptions_occurred_saving_the_launcher_preferences_6"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		return true;
	}
}
