/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;

/**
 * PerspectivesTab
 */
public class PerspectivesTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationListener {
	
	/**
	 * The launch config type this tab pertains to
	 */
	private ILaunchConfigurationType fType = null;
	
	/**
	 * Array containing modes this config type supports
	 */
	private String[] fModeIds = null;
			
	/**
	 * Array of all perspective labels for combo box (including 'None')
	 */
	private String[] fPerspectiveLabels = null;
	
	/**
	 * Map of perspective labels to ids
	 */
	private Map fPerspectiveIds = null;

	/**
	 * Combo boxes corresponding to modes
	 */
	private Combo[] fCombos = null;
	
	private Button fRestoreDefaults;
	
	/**
	 * A selection adapter which responds to widget selections in this tab
	 */
	private SelectionAdapter fSelectionAdapter= new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			Object source= e.getSource();
			if (source == fRestoreDefaults) {
				handleRestoreDefaultsSelected();
			}
			updateLaunchConfigurationDialog();
		}
		private void handleRestoreDefaultsSelected() {
			for (int i = 0; i < fCombos.length; i++) {
				String mode = (String)fCombos[i].getData();
				String def = DebugUIPlugin.getDefault().getPerspectiveManager().getDefaultLaunchPerspective(getLaunchConfigurationType(), mode);
				if (def == null) {
					fCombos[i].setText(LaunchConfigurationsMessages.PerspectivesTab_1); 
				} else {
					IPerspectiveRegistry registry = PlatformUI.getWorkbench().getPerspectiveRegistry();
					IPerspectiveDescriptor descriptor = registry.findPerspectiveWithId(def);
					fCombos[i].setText(descriptor.getLabel());
				}
			}
		}
	};
	
	/**
	 * Flag indicating the UI is updating from the config, and should not
	 * update the config in response to the change.
	 */
	private boolean fInitializing = false;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(this);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		if (!configuration.isWorkingCopy()) {
			if (configuration.getName().startsWith(getLaunchConfigurationType().getIdentifier())) {
				for (int i = 0; i < fModeIds.length; i++) {
					String mode = fModeIds[i];
					try {
						String persp = configuration.getAttribute(mode, (String)null);
						if (persp == null) {
							// default
							persp = IDebugUIConstants.PERSPECTIVE_DEFAULT;
						}
						DebugUITools.setLaunchPerspective(getLaunchConfigurationType(), mode, persp);
					} catch (CoreException e) {
						DebugUIPlugin.log(e);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
	}

	/**
	 * Constructs a new tab
	 * 
	 * @param type
	 */
	public PerspectivesTab(ILaunchConfigurationType type) {
		super();
		fType = type;
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return DebugPluginImages.getImage(IInternalDebugUIConstants.IMG_OBJS_PERSPECTIVE_TAB);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_PERSPECTIVE_TAB);
		final GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(gd);
		composite.setFont(parent.getFont());
		
		Label label = new Label(composite, SWT.LEFT + SWT.WRAP);
		label.setFont(parent.getFont());
		label.setText(MessageFormat.format(LaunchConfigurationsMessages.PerspectivesTab_0, new String[]{getLaunchConfigurationType().getName()})); 
		final GridData finalGd = new GridData();
		finalGd.horizontalSpan = 2;
		label.setLayoutData(finalGd);
		composite.addControlListener(new ControlAdapter(){
			public void controlResized(ControlEvent e){
				finalGd.widthHint = composite.getClientArea().width - 2*layout.marginWidth;
				composite.layout(true);
			}
		});

		
		// init modes
		ILaunchMode[] modes = DebugPlugin.getDefault().getLaunchManager().getLaunchModes();
		ArrayList supported = new ArrayList();
		for (int i = 0; i < modes.length; i++) {
			ILaunchMode mode = modes[i];
			if (getLaunchConfigurationType().supportsMode(mode.getIdentifier())) {
				supported.add(mode.getIdentifier());
			}
		}
		fModeIds = (String[])supported.toArray(new String[supported.size()]);
		
		// init perspective labels
		IPerspectiveRegistry registry = PlatformUI.getWorkbench().getPerspectiveRegistry();
		IPerspectiveDescriptor[] descriptors = registry.getPerspectives();
		fPerspectiveLabels = new String[descriptors.length + 1];
		fPerspectiveLabels[0] = LaunchConfigurationsMessages.PerspectivesTab_1; 
		fPerspectiveIds = new HashMap(descriptors.length);
		for (int i = 0; i < descriptors.length; i++) {
			IPerspectiveDescriptor descriptor = descriptors[i];
			fPerspectiveLabels[i + 1] = descriptor.getLabel();
			fPerspectiveIds.put(descriptor.getLabel(), descriptor.getId());
		}
		
		// spacer
		createVerticalSpacer(composite, 2);
		
		fCombos = new Combo[fModeIds.length];
		for (int i = 0; i < fModeIds.length; i++) {
			label = new Label(composite, SWT.NONE);
			label.setFont(composite.getFont());
			gd = new GridData(GridData.BEGINNING);
			gd.horizontalSpan= 1;
			label.setLayoutData(gd);
			String text = DebugPlugin.getDefault().getLaunchManager().getLaunchMode(fModeIds[i]).getLabel();
			label.setText(MessageFormat.format(LaunchConfigurationsMessages.PerspectivesTab_2, new String[]{text})); 
			
			Combo combo = new Combo(composite, SWT.READ_ONLY);
			combo.setFont(composite.getFont());
			combo.setItems(fPerspectiveLabels);
			combo.setData(fModeIds[i]);
			gd = new GridData(GridData.BEGINNING);
			combo.setLayoutData(gd);
			fCombos[i] = combo;
			combo.addSelectionListener(fSelectionAdapter);
		}
		
		createVerticalSpacer(composite, 2);
		
		fRestoreDefaults = createPushButton(composite, LaunchConfigurationsMessages.PerspectivesTab_3, null); 
		gd= new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan= 2;
		gd.horizontalAlignment= SWT.END;
		gd.verticalAlignment= SWT.END;
		fRestoreDefaults.setLayoutData(gd);
		fRestoreDefaults.addSelectionListener(fSelectionAdapter);
		
		Dialog.applyDialogFont(composite);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		for (int i = 0; i < fModeIds.length; i++) {
			String mode = fModeIds[i];
			// null indicates default
			configuration.setAttribute(mode, (String)null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		// each perspective is stored with its mode identifier
		fInitializing = true;
		IPerspectiveRegistry registry = PlatformUI.getWorkbench().getPerspectiveRegistry();
		for (int i = 0; i < fModeIds.length; i++) {
			String mode = fModeIds[i];
			String persp;
			try {
				persp = configuration.getAttribute(mode, (String)null);
				if (persp == null) {
					// null indicates default
					persp = DebugUITools.getLaunchPerspective(getLaunchConfigurationType(), mode);
				}
				if (IDebugUIConstants.PERSPECTIVE_NONE.equals(persp)) {
					persp = null;
				}
				IPerspectiveDescriptor descriptor = null;
				if (persp != null) {
					descriptor = registry.findPerspectiveWithId(persp);
				}
				if (descriptor == null) {
					// select none
					fCombos[i].setText(LaunchConfigurationsMessages.PerspectivesTab_1); 
				} else {
					fCombos[i].setText(descriptor.getLabel());
				}
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		fInitializing = false;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		for (int i = 0; i < fCombos.length; i++) {
			updateConfigFromCombo(fCombos[i], configuration);
		}
	}
	
	/**
	 * Updates the configuration based on the user's selection in the
	 * perspective combo by setting the given configurations perspective
	 * attribute.
	 * 
	 * @param combo the combo widget
	 * @param workingCopy the launch configuration to update
	 */
	protected void updateConfigFromCombo(Combo combo, ILaunchConfigurationWorkingCopy workingCopy) {
		if (!fInitializing) {
			String mode = (String)combo.getData();
			String persp = combo.getText();
			if (persp.equals(LaunchConfigurationsMessages.PerspectivesTab_1)) { 
				persp = IDebugUIConstants.PERSPECTIVE_NONE;
			} else {
				persp = (String)fPerspectiveIds.get(persp);
			}
			// if the same as default, use null which indicates default
			String def = DebugUIPlugin.getDefault().getPerspectiveManager().getDefaultLaunchPerspective(getLaunchConfigurationType(), mode);
			if (def == null) {
				def = IDebugUIConstants.PERSPECTIVE_NONE;
			}
			if (persp.equals(def)) {
				persp = null;
			}
			workingCopy.setAttribute(mode, persp);
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LaunchConfigurationsMessages.PerspectivesTab_7; 
	}
	
	/**
	 * Returns the launch configuration type this tab was opened on.
	 * 
	 * @return launch config type
	 */
	protected ILaunchConfigurationType getLaunchConfigurationType() {
		return fType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing on activation
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing on deactivation
	}

	/**
	 * Returns the description associated with the current launch configuration
	 * type in the current mode or <code>null</code> if none.
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getMessage()
	 */
	public String getMessage() {
		String description = super.getMessage();
		if(fType != null) {
			String mode = getLaunchConfigurationDialog().getMode();
			LaunchConfigurationPresentationManager manager = LaunchConfigurationPresentationManager.getDefault();
			LaunchConfigurationTabGroupExtension extension = manager.getExtension(fType.getAttribute("id"), mode); //$NON-NLS-1$
			description = extension.getDescription(mode);
		}		
		return description;
	}

}
