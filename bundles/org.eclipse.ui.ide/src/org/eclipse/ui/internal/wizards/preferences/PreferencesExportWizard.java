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
package org.eclipse.ui.internal.wizards.preferences;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Standard workbench wizard for exporting preferences from the workspace
 * to the local file system.
 * <p>
 * This class may be instantiated and used without further configuration;
 * this class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * IWizard wizard = new PreferencesExportWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, the user-selected workspace preferences 
 * are exported to the user-specified location in the local file system,
 * the dialog closes, and the call to <code>open</code> returns.
 * </p>
 * 
 * @since 3.1
 * 
 */
public class PreferencesExportWizard extends Wizard implements IExportWizard {
    private IStructuredSelection selection;

    private WizardPreferencesExportPage1 mainPage;

    /**
     * Creates a wizard for exporting workspace preferences to the local file system.
     */
    public PreferencesExportWizard() {
        AbstractUIPlugin plugin = (AbstractUIPlugin) Platform
                .getPlugin(PlatformUI.PLUGIN_ID);
        IDialogSettings workbenchSettings = plugin.getDialogSettings();
        IDialogSettings section = workbenchSettings
                .getSection("PreferencesExportWizard");//$NON-NLS-1$
        if (section == null)
            section = workbenchSettings.addNewSection("PreferencesExportWizard");//$NON-NLS-1$
        setDialogSettings(section);
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public void addPages() {
        super.addPages();
        mainPage = new WizardPreferencesExportPage1();
        addPage(mainPage);
    }

    /**
     * Returns the image descriptor with the given relative path.
     */
    private ImageDescriptor getImageDescriptor(String relativePath) {
        String iconPath = "icons/full/";//$NON-NLS-1$	
        try {
            AbstractUIPlugin plugin = (AbstractUIPlugin) Platform
                    .getPlugin(PlatformUI.PLUGIN_ID);
            URL installURL = plugin.getDescriptor().getInstallURL();
            URL url = new URL(installURL, iconPath + relativePath);
            return ImageDescriptor.createFromURL(url);
        } catch (MalformedURLException e) {
            // Should not happen
            return null;
        }
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchWizard.
     */
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
//      Currently not concerned about selection, maybe in the future it will make sense
//      to support exporting project preferences based on the selection  
        
//        this.selection = currentSelection;
//        List selectedResources = IDE.computeSelectedResources(currentSelection);
//        if (!selectedResources.isEmpty()) {
//            this.selection = new StructuredSelection(selectedResources);
//        }
//
//        // look it up if current selection (after resource adapting) is empty
//        if (selection.isEmpty() && workbench.getActiveWorkbenchWindow() != null) {
//            IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
//                    .getActivePage();
//            if (page != null) {
//                IEditorPart currentEditor = page.getActiveEditor();
//                if (currentEditor != null) {
//                    Object selectedResource = currentEditor.getEditorInput()
//                            .getAdapter(IResource.class);
//                    if (selectedResource != null) {
//                        selection = new StructuredSelection(selectedResource);
//                    }
//                }
//            }
//        }

        setWindowTitle(PreferencesMessages.PreferencesExportWizard_export);
        setDefaultPageImageDescriptor(getImageDescriptor("wizban/exportdir_wiz.gif"));//$NON-NLS-1$
        setNeedsProgressMonitor(true);
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public boolean performFinish() {
        return mainPage.finish();
    }
    
    
}
