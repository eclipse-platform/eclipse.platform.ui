/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardContainer2;
import org.eclipse.swt.widgets.Composite;

/**
 * Displays tabs for editing a configuration
 */
public class ConfigurationPage extends AbstractLaunchPage implements ILaunchConfigurationDialog {
    
    private LaunchConfigurationTabGroupViewer fViewer;
    
    public ConfigurationPage(String pageName) {
        super(pageName, null, null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        fViewer = new LaunchConfigurationTabGroupViewer(parent, this);
        setControl(fViewer.getControl());
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#updateButtons()
     */
    public void updateButtons() {
        fViewer.refresh();
        setPageComplete(fViewer.canLaunch());
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#updateMessage()
     */
    public void updateMessage() {
        setErrorMessage(fViewer.getErrorMesssage());
        String text = fViewer.getMessage();
        if (text == null || text.equals(getDescription(getLaunchConfigurationType()))) {
            text = LaunchConfigurationsMessages.getString("ConfigurationPage.0"); //$NON-NLS-1$
        }
		setMessage(text);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#setName(java.lang.String)
     */
    public void setName(String name) {
        fViewer.setName(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#generateName(java.lang.String)
     */
    public String generateName(String name) {
		if (name == null) {
			name = ""; //$NON-NLS-1$
		}
		return DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#getTabs()
     */
    public ILaunchConfigurationTab[] getTabs() {
        return fViewer.getTabs();
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#getActiveTab()
     */
    public ILaunchConfigurationTab getActiveTab() {
        return fViewer.getActiveTab();
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#getMode()
     */
    public String getMode() {
        return super.getMode();
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#setActiveTab(org.eclipse.debug.ui.ILaunchConfigurationTab)
     */
    public void setActiveTab(ILaunchConfigurationTab tab) {
        fViewer.setActiveTab(tab);
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#setActiveTab(int)
     */
    public void setActiveTab(int index) {
        fViewer.setActiveTab(index);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
     */
    public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
        getWizard().getContainer().run(fork, cancelable, runnable);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        LaunchConfigurationTabGroupExtension extension = LaunchConfigurationPresentationManager.getDefault().getExtension(getLaunchConfigurationType().getIdentifier(), getMode());
        ImageDescriptor descriptor = extension.getBannerImageDescriptor();
        if (descriptor == null) {
            descriptor = getLaunchGroup().getBannerImageDescriptor();
        }
        fViewer.setInput(getLaunchConfiguration());
        super.setVisible(visible);
        setImageDescriptor(descriptor);
        setMessage(LaunchConfigurationsMessages.getString("ConfigurationPage.1")); //$NON-NLS-1$
        try {
            setTitle(getLaunchConfiguration().getType().getName());
        } catch (CoreException e) {
            setErrorMessage(e.getMessage());
        }
        IWizardContainer container = getWizard().getContainer();
        if (container instanceof IWizardContainer2) {
            ((IWizardContainer2)container).updateSize();
        }
    }
        
    /**
     * Saves the current contents of the configuration being edited.
     */
    protected void saveIfNeeded() {
        if (isCurrentPage() && fViewer.isDirty()) {
            fViewer.handleApplyPressed();
        }
    }
    
	/**
	 * Return whether the current configuration can be discarded.  This involves determining
	 * if it is dirty, and if it is, asking the user what to do.
	 */
	private boolean canDiscardCurrentConfig() {				
		if (fViewer.isDirty()) {
			return showUnsavedChangesDialog();
		}
		return true;
	}
	
	/**
	 * Show the user a dialog appropriate to whether the unsaved changes in the current config
	 * can be saved or not.  Return <code>true</code> if the user indicated that they wish to replace
	 * the current config, either by saving changes or by discarding the, return <code>false</code>
	 * otherwise.
	 */
	private boolean showUnsavedChangesDialog() {
		if (fViewer.canSave()) {
			return showSaveChangesDialog();
		}
		return showDiscardChangesDialog();
	}    
	
	/**
	 * Create and return a dialog that asks the user whether they want to save
	 * unsaved changes.  Return <code>true </code> if they chose to save changes,
	 * <code>false</code> otherwise.
	 */
	private boolean showSaveChangesDialog() {
		String message = null;
		if (getActiveTab() instanceof PerspectivesTab) {
			try {
				message = MessageFormat.format(LaunchConfigurationsMessages.getString("LaunchConfigurationsDialog.45"), new String[]{fViewer.getWorkingCopy().getType().getName()}); //$NON-NLS-1$
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		} else {
			message = MessageFormat.format(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.The_configuration___29"), new String[]{fViewer.getWorkingCopy().getName()}); //$NON-NLS-1$
		}
		MessageDialog dialog = new MessageDialog(getShell(), 
												 LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Save_changes__31"), //$NON-NLS-1$
												 null,
												 message,
												 MessageDialog.QUESTION,
												 new String[] {LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Yes_32"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.No_33"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Cancel_34")}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
												 0);
		// If user clicked 'Cancel' or closed dialog, return false
		int selectedButton = dialog.open();
		if ((selectedButton < 0) || (selectedButton == 2)) {
			return false;
		}
		
		// If they hit 'Yes', save the working copy 
		if (selectedButton == 0) {
			fViewer.handleApplyPressed();
		}
		return true;
	}	
	
	/**
	 * Create and return a dialog that asks the user whether they want to discard
	 * unsaved changes.  Return <code>true</code> if they chose to discard changes,
	 * <code>false</code> otherwise.
	 */
	private boolean showDiscardChangesDialog() {
		StringBuffer buffer = new StringBuffer(MessageFormat.format(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.The_configuration___35"), new String[]{fViewer.getWorkingCopy().getName()})); //$NON-NLS-1$
		buffer.append(fViewer.getErrorMesssage());
		buffer.append(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Do_you_wish_to_discard_changes_37")); //$NON-NLS-1$
		MessageDialog dialog = new MessageDialog(getShell(), 
												 LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Discard_changes__38"), //$NON-NLS-1$
												 null,
												 buffer.toString(),
												 MessageDialog.QUESTION,
												 new String[] {LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Yes_32"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.No_33")}, //$NON-NLS-1$ //$NON-NLS-2$
												 1);
		// If user clicked 'Yes', return true
		int selectedButton = dialog.open();
		if (selectedButton == 0) {
			return true;
		}
		return false;
	}

    /**
     * @return
     */
    protected boolean performCancel() {
        if (isCurrentPage()) {
            boolean cancel = canDiscardCurrentConfig(); 
            if (cancel) {
                // ensure cofig is reverted if not save - no harm if saved
               fViewer.handleRevertPressed();
            }
            return cancel;
        }
        return true;
    }	
}
