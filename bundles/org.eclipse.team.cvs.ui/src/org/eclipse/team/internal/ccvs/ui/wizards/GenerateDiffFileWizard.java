/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.wizards;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Diff;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.misc.ContainerContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A wizard for creating a patch file by running the CVS diff command.
 */
public class GenerateDiffFileWizard extends Wizard {
    
    /**
     * Page to select a patch file. Overriding validatePage was necessary to allow
     * entering a file name that already exists.
     */
    private class LocationPage extends WizardPage {
        
        /**
         * The possible locations to save a patch.
         */
        public final static int CLIPBOARD = 1;
        public final static int FILESYSTEM = 2;
        public final static int WORKSPACE = 3;
        
        /**
         * GUI controls for clipboard (cp), filesystem (fs) and workspace (ws).
         */
        private Button cpRadio;
        
        private Button fsRadio;
        protected Text fsPathText;
        private Button fsBrowseButton;
        
        private Button wsRadio;
        protected TreeViewer wsTreeViewer;
        private Text wsFilenameText;
        
        
        /**
         * State information of this page, updated by the listeners.
         */
        protected boolean pageValid;
        protected IContainer wsSelectedContainer;
        protected int selectedLocation;
        
        /**
         * The default values store used to initialize the selections.
         */
        private final DefaultValuesStore store;
        
        LocationPage(String pageName, String title, ImageDescriptor image, DefaultValuesStore store) {
            super(pageName, title, image);
            setPageComplete(false);
            this.store= store;
        }
        
        /**
         * Allow the user to finish if a valid file has been entered. 
         */
        protected boolean validatePage() {
            
            switch (selectedLocation) {
            case WORKSPACE:
                pageValid= validateWorkspaceLocation();
                break;
            case FILESYSTEM:
                pageValid= validateFilesystemLocation();
                break;
            case CLIPBOARD:
                pageValid= true;
                break;
            }
            
            /**
             * Avoid draw flicker by clearing error message
             * if all is valid.
             */
            if (pageValid) {
                setMessage(null);
                setErrorMessage(null);
            }
            setPageComplete(pageValid);
            return pageValid;
        }
        
        /**
         * The following conditions must hold for the file system location
         * to be valid:
         * - the path must be valid and non-empty
         * - the path must be absolute
         * - the specified file must be of type file
         * - the parent must exist (new folders can be created via the browse button)
         */
        private boolean validateFilesystemLocation() {
            final String pathString= fsPathText.getText().trim();
            if (pathString.length() == 0 || !new Path("").isValidPath(pathString)) { //$NON-NLS-1$
                setErrorMessage(CVSUIMessages.GenerateDiffFileWizard_0); 
                return false;
            }
            
            final File file= new File(pathString);
            if (!file.isAbsolute()) {
                setErrorMessage(CVSUIMessages.GenerateDiffFileWizard_0); 
                return false;
            }
            
            if (file.isDirectory()) {
                setErrorMessage(CVSUIMessages.GenerateDiffFileWizard_2); 
                return false;
            }
            
            if (pathString.endsWith("/") || pathString.endsWith("\\")) {  //$NON-NLS-1$//$NON-NLS-2$
                setErrorMessage(CVSUIMessages.GenerateDiffFileWizard_3); 
                return false;
            }
            
            final File parent= file.getParentFile();
            if (!(parent.exists() && parent.isDirectory())) {
                setErrorMessage(CVSUIMessages.GenerateDiffFileWizard_3); 
                return false;
            }
            return true;
        }
        
        /**
         * The following conditions must hold for the file system location to be valid:
         * - a parent must be selected in the workspace tree view
         * - the resource name must be valid 
         */
        private boolean validateWorkspaceLocation() {
            if (wsSelectedContainer == null) {
                setErrorMessage(CVSUIMessages.GenerateDiffFileWizard_4); 
                return false;
            }
            final String filename= wsFilenameText.getText().trim();
            if (!new Path("").isValidSegment(filename)) { //$NON-NLS-1$
                setErrorMessage(CVSUIMessages.GenerateDiffFileWizard_5); 
                return false;
            }
            return true;
        }
        
        /**
         * Answers a full path to a file system file or <code>null</code> if the user
         * selected to save the patch in the clipboard.
         */
        public File getFile() {
            if (pageValid && selectedLocation == FILESYSTEM) {
                return new File(fsPathText.getText().trim());
            } 
            if (pageValid && selectedLocation == WORKSPACE) {
                final String filename= wsFilenameText.getText().trim();
                final IFile file= wsSelectedContainer.getFile(new Path(filename));
                return file.getLocation().toFile();
            }
            return null;
        }
        
        /**
         * Get the selected workspace resource if the patch is to be saved in the 
         * workspace, or null otherwise.
         */
        public IResource getResource() {
            if (pageValid && selectedLocation == WORKSPACE) {
                final String filename= wsFilenameText.getText().trim();
                return wsSelectedContainer.getFile(new Path(null, filename));
            }
            return null;
        }
        
        /**
         * Allow the user to chose to save the patch to the workspace or outside
         * of the workspace.
         */
        public void createControl(Composite parent) {
            
            final Composite composite= new Composite(parent, SWT.NULL);
            composite.setLayout(new GridLayout());
            setControl(composite);
            initializeDialogUnits(composite);
            
            // set F1 help
            PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.PATCH_SELECTION_PAGE);
            
            setupClipboardControls(composite);    			
            setupFilesystemControls(composite);
            setupWorkspaceControls(composite);
            
            Dialog.applyDialogFont(parent);
            
            initializeDefaultValues();
            
            /**
             * Ensure the page is in a valid state. 
             */
            if (!validatePage()) {
                store.storeRadioSelection(CLIPBOARD);
                initializeDefaultValues();
                validatePage();
            }
            pageValid= true;
            
            updateEnablements();
            setupListeners();
        }
        
        
        /**
         * Setup the controls for the workspace option.
         */
        private void setupWorkspaceControls(Composite composite) {
            wsRadio= new Button(composite, SWT.RADIO);
            wsRadio.setText(CVSUIMessages.Save_In_Workspace_7); 
 
            new Label(composite, SWT.LEFT).setText(CVSUIMessages.Select_a_folder_then_type_in_the_file_name__8);		 
            
            wsTreeViewer = new TreeViewer(composite, SWT.BORDER);
            final GridData gd= new GridData(SWT.FILL, SWT.FILL, true, true);
            gd.widthHint= 0;
            gd.heightHint= 0;
            wsTreeViewer.getTree().setLayoutData(gd);
            
            final ContainerContentProvider cp = new ContainerContentProvider();
            cp.showClosedProjects(false);
            wsTreeViewer.setContentProvider(cp);
            wsTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
            wsTreeViewer.setInput(ResourcesPlugin.getWorkspace());
            
            final Composite group = new Composite(composite, SWT.NONE);
            final GridLayout layout = new GridLayout(2, false);
            layout.marginWidth = 0;
            group.setLayout(layout);
            group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            
            final Label label = new Label(group, SWT.NONE);
            label.setLayoutData(new GridData());
            label.setText(CVSUIMessages.Fi_le_name__9); 
            
            wsFilenameText = new Text(group,SWT.BORDER);
            wsFilenameText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        }
        
        /**
         * Setup the controls for the file system option.
         */
        private void setupFilesystemControls(final Composite composite) {
            GridLayout layout;
            fsRadio= new Button(composite, SWT.RADIO);

            fsRadio.setText(CVSUIMessages.Save_In_File_System_3); 
            
            final Composite nameGroup = new Composite(composite,SWT.NONE);
            layout = new GridLayout();
            layout.numColumns = 2;
            layout.marginWidth = 0;
            nameGroup.setLayout(layout);
            final GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
            nameGroup.setLayoutData(data);
            
            fsPathText= new Text(nameGroup, SWT.BORDER);
            GridData gd= new GridData(GridData.FILL_HORIZONTAL);
            fsPathText.setLayoutData(gd);
            
            fsBrowseButton = new Button(nameGroup, SWT.NULL);
            fsBrowseButton.setText(CVSUIMessages.Browse____4); 
        }
        
        /**
         * Setup the controls for the clipboard option.
         */
        private void setupClipboardControls(final Composite composite) {
            cpRadio= new Button(composite, SWT.RADIO);
            cpRadio.setText(CVSUIMessages.Save_To_Clipboard_2); 
        }
        
        
        /**
         * Initialize the controls with the saved default values which are
         * obtained from the DefaultValuesStore.
         */
        private void initializeDefaultValues() {

            selectedLocation= store.getRadioSelection();
            wsSelectedContainer= store.getWorkspaceSelection();
            
            /**
             * Radio buttons
             */
            cpRadio.setSelection(selectedLocation == CLIPBOARD);
            fsRadio.setSelection(selectedLocation == FILESYSTEM);
            wsRadio.setSelection(selectedLocation == WORKSPACE);
            
            /**
             * Text fields.
             */
            fsPathText.setText(store.getFilesystemPath());
            wsFilenameText.setText(store.getWorkspaceFilename());
            
            /**
             * Tree viewer.
             */
            if (wsSelectedContainer != null) {
                final ISelection selection= new StructuredSelection(wsSelectedContainer);
                wsTreeViewer.setSelection(selection, true);
            }
        }
        
        /**
         * Setup all the listeners for the controls. 
         */
        private void setupListeners() {

            cpRadio.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    selectedLocation= CLIPBOARD;
                    validatePage();
                    updateEnablements();
                }
            });
            fsRadio.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    selectedLocation= FILESYSTEM;
                    validatePage();
                    updateEnablements();
                }
            });
            
            wsRadio.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    selectedLocation= WORKSPACE;
                    validatePage();
                    updateEnablements();
                }
            });
            
            fsPathText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    validatePage();
                }
            });
            
            fsBrowseButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    final FileDialog dialog = new FileDialog(getShell(), SWT.PRIMARY_MODAL | SWT.SAVE);
                    if (pageValid) {
                        final File file= new File(fsPathText.getText());
                        dialog.setFilterPath(file.getParent());
                    }
                    dialog.setText(CVSUIMessages.Save_Patch_As_5); 
                    dialog.setFileName(CVSUIMessages.patch_txt_6); 
                    final String path = dialog.open();
                    if (path != null) {
                        fsPathText.setText(new Path(path).toOSString());
                    }			
                }
            });		
            
            wsTreeViewer.addSelectionChangedListener(
                    new ISelectionChangedListener() {
                        public void selectionChanged(SelectionChangedEvent event) {
                            IStructuredSelection s = (IStructuredSelection)event.getSelection();
                            wsSelectedContainer = ((IContainer) s.getFirstElement());
                            validatePage();
                        }
                    });
            
            wsTreeViewer.addDoubleClickListener(
                    new IDoubleClickListener() {
                        public void doubleClick(DoubleClickEvent event) {
                            ISelection s= event.getSelection();
                            if (s instanceof IStructuredSelection) {
                                Object item = ((IStructuredSelection)s).getFirstElement();
                                if (wsTreeViewer.getExpandedState(item))
                                    wsTreeViewer.collapseToLevel(item, 1);
                                else
                                    wsTreeViewer.expandToLevel(item, 1);
                            }
                        }
                    });
            
            wsFilenameText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    validatePage();
                }
            });
        }

        /**
         * Enable and disable controls based on the selected radio button.
         */
        protected void updateEnablements() {
            fsBrowseButton.setEnabled(selectedLocation == FILESYSTEM);
            fsPathText.setEnabled(selectedLocation == FILESYSTEM);
            wsTreeViewer.getTree().setEnabled(selectedLocation == WORKSPACE);
            wsFilenameText.setEnabled(selectedLocation == WORKSPACE);
        }
        
        public int getSelectedLocation() {
            return selectedLocation;
        }
    }
        
    /**
     * Page to select the options for creating the patch.
     */
    private class OptionsPage extends WizardPage {
        
        private Button recurseOption;
        private Button contextDiffOption;
        private Button unifiedDiffOption;
        private Button regularDiffOption;
        private Button includeNewFilesOptions;
        
        /**
         * Constructor for PatchFileCreationOptionsPage.
         */
        protected OptionsPage(String pageName, String title, ImageDescriptor titleImage) {
            super(pageName, title, titleImage);
        }
        
        /*
         * @see IDialogPage#createControl(Composite)
         */
        public void createControl(Composite parent) {
            Composite composite= new Composite(parent, SWT.NULL);
            GridLayout layout= new GridLayout();
            composite.setLayout(layout);
            composite.setLayoutData(new GridData());
            setControl(composite);
            
            // set F1 help
            PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IHelpContextIds.PATCH_OPTIONS_PAGE);
            
            recurseOption = new Button(composite, SWT.CHECK);
            recurseOption.setText(CVSUIMessages.Do_not_recurse_into_sub_folders_10); 
            recurseOption.setSelection(true);
            
            includeNewFilesOptions = new Button(composite, SWT.CHECK);
            includeNewFilesOptions.setText(CVSUIMessages.Do_not_include_new_files_in_patch_11); 
            includeNewFilesOptions.setSelection(true);
            
            Group diffTypeGroup = new Group(composite, SWT.NONE);
            layout = new GridLayout();
            layout.marginHeight = 0;
            diffTypeGroup.setLayout(layout);
            GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
            diffTypeGroup.setLayoutData(data);
            diffTypeGroup.setText(CVSUIMessages.Diff_output_format_12); 
            
            unifiedDiffOption = new Button(diffTypeGroup, SWT.RADIO);
            unifiedDiffOption.setText(CVSUIMessages.Unified__format_required_by_Compare_With_Patch_feature__13); 
            unifiedDiffOption.setSelection(true);
            contextDiffOption = new Button(diffTypeGroup, SWT.RADIO);
            contextDiffOption.setText(CVSUIMessages.Context_14); 
            regularDiffOption = new Button(diffTypeGroup, SWT.RADIO);
            regularDiffOption.setText(CVSUIMessages.Standard_15); 
            
            Dialog.applyDialogFont(parent);
        }
        
        /**
         * Return the list of Diff command options configured on this page.
         */
        public LocalOption[] getOptions() {
            List options = new ArrayList(5);
            if(includeNewFilesOptions.getSelection()) {
                options.add(Diff.INCLUDE_NEWFILES);
            }
            if(!recurseOption.getSelection()) {
                options.add(Command.DO_NOT_RECURSE);
            }
            if(unifiedDiffOption.getSelection()) {
                options.add(Diff.UNIFIED_FORMAT);
            } else if(contextDiffOption.getSelection()) {
                options.add(Diff.CONTEXT_FORMAT);
            }
            return (LocalOption[]) options.toArray(new LocalOption[options.size()]);
        }		
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            if (visible) {
                recurseOption.setFocus();
            }
        }
    }
 
    /**
     * Class to retrieve and store the default selected values. 
     */
    private final class DefaultValuesStore {
        
        private static final String PREF_LAST_SELECTION= "org.eclipse.team.internal.ccvs.ui.wizards.GenerateDiffFileWizard.PatchFileSelectionPage.selection"; //$NON-NLS-1$
        private static final String PREF_LAST_FS_PATH= "org.eclipse.team.internal.ccvs.ui.wizards.GenerateDiffFileWizard.PatchFileSelectionPage.fs.path"; //$NON-NLS-1$
        private static final String PREF_LAST_WS_FILENAME= "org.eclipse.team.internal.ccvs.ui.wizards.GenerateDiffFileWizard.PatchFileSelectionPage.ws.filename"; //$NON-NLS-1$
        private static final String PREF_LAST_WS_PATH= "org.eclipse.team.internal.ccvs.ui.wizards.GenerateDiffFileWizard.PatchFileSelectionPage.ws.path"; //$NON-NLS-1$
        
        private final IDialogSettings dialogSettings;
        
        public DefaultValuesStore() {
            dialogSettings= CVSUIPlugin.getPlugin().getDialogSettings(); 
        }
        
        public int getRadioSelection() {
            int value= LocationPage.CLIPBOARD;
            try {
                value= dialogSettings.getInt(PREF_LAST_SELECTION);
            } catch (NumberFormatException e) {
            }
            
            switch (value) {
            case LocationPage.FILESYSTEM:
            case LocationPage.WORKSPACE:
            case LocationPage.CLIPBOARD:
                return value;
            default:
                return LocationPage.CLIPBOARD;
            }
        }
        
        public String getFilesystemPath() {
            final String path= dialogSettings.get(PREF_LAST_FS_PATH);
            return path != null ? path : "";  //$NON-NLS-1$
        }
        
        public String getWorkspaceFilename() {
            final String filename= dialogSettings.get(PREF_LAST_WS_FILENAME);
            return filename != null ? filename : ""; //$NON-NLS-1$
        }
        
        public IContainer getWorkspaceSelection() {
            final String value= dialogSettings.get(PREF_LAST_WS_PATH);
            if ( value != null ) {
                final IPath path= new Path(value);
                final IResource container= ResourcesPlugin.getWorkspace().getRoot().findMember(path);
                if (container instanceof IContainer) {
                    return (IContainer)container;
                }
            }
            return null;
        }
        
        public void storeRadioSelection(int defaultSelection) {
            dialogSettings.put(PREF_LAST_SELECTION, defaultSelection);
        }
        
        public void storeFilesystemPath(String path) {
            dialogSettings.put(PREF_LAST_FS_PATH, path);
        }
        
        public void storeWorkspacePath(String path) {
            dialogSettings.put(PREF_LAST_WS_PATH, path);
        }
        
        public void storeWorkspaceFilename(String filename) {
            dialogSettings.put(PREF_LAST_WS_FILENAME, filename);
        }
    }
    
    
    private LocationPage locationPage;
    private OptionsPage optionsPage;
    
    private final IResource resource;
    private final DefaultValuesStore defaultValuesStore;
    
    
    public GenerateDiffFileWizard(IResource resource) {
        super();
        this.resource = resource;
        setWindowTitle(CVSUIMessages.GenerateCVSDiff_title); 
        initializeDefaultPageImageDescriptor();
        defaultValuesStore= new DefaultValuesStore();
    }
    
    public void addPages() {
        String pageTitle = CVSUIMessages.GenerateCVSDiff_pageTitle; 
        String pageDescription = CVSUIMessages.GenerateCVSDiff_pageDescription; 
        locationPage = new LocationPage(pageTitle, pageTitle, CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_DIFF), defaultValuesStore);
        locationPage.setDescription(pageDescription);
        addPage(locationPage);
        
        pageTitle = CVSUIMessages.Advanced_options_19; 
        pageDescription = CVSUIMessages.Configure_the_options_used_for_the_CVS_diff_command_20; 
        optionsPage = new OptionsPage(pageTitle, pageTitle, CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_DIFF));
        optionsPage.setDescription(pageDescription);
        addPage(optionsPage);		
    }
    
    /**
     * Declares the wizard banner iamge descriptor
     */
    protected void initializeDefaultPageImageDescriptor() {
        final String iconPath= "icons/full/"; //$NON-NLS-1$
        try {
            final URL installURL = CVSUIPlugin.getPlugin().getBundle().getEntry("/"); //$NON-NLS-1$
            final URL url = new URL(installURL, iconPath + "wizards/newconnect_wiz.gif");	//$NON-NLS-1$
            ImageDescriptor desc = ImageDescriptor.createFromURL(url);
            setDefaultPageImageDescriptor(desc);
        } catch (MalformedURLException e) {
            // Should not happen.  Ignore.
        }
    }
    
    /* (Non-javadoc)
     * Method declared on IWizard.
     */
    public boolean needsProgressMonitor() {
        return true;
    }
    
    /**
     * Completes processing of the wizard. If this method returns <code>
     * true</code>, the wizard will close; otherwise, it will stay active.
     */
    public boolean performFinish() {
        
        final int location= locationPage.getSelectedLocation();

        final File file= location != LocationPage.CLIPBOARD? locationPage.getFile() : null;
        
        if (!(file == null || validateFile(file))) {
            return false;
        }
        
        /**
         * Perform diff operation.
         */
        try {
            getContainer().run(true, true, new GenerateDiffFileOperation(resource, file, optionsPage.getOptions(), getShell()));
        } catch (InterruptedException e1) {
            return true;
        } catch (InvocationTargetException e2) {
            CVSUIPlugin.openError(getShell(), null, null, e2);
            return false;
        }
        
        /**
         * Refresh workspace if necessary and save default selection.
         */
        switch (location) {
        
        case LocationPage.WORKSPACE:
            defaultValuesStore.storeRadioSelection(LocationPage.WORKSPACE);
            final IResource workspaceResource= locationPage.getResource();
            defaultValuesStore.storeWorkspacePath(workspaceResource.getParent().getFullPath().toString());
            defaultValuesStore.storeWorkspaceFilename(workspaceResource.getName());
            try {
                workspaceResource.getParent().refreshLocal(IResource.DEPTH_ONE, null);
            } catch(CoreException e) {
                CVSUIPlugin.openError(getShell(), CVSUIMessages.GenerateCVSDiff_error, null, e); 
                return false;
            }
            break;
            
        case LocationPage.FILESYSTEM:
            defaultValuesStore.storeFilesystemPath(file.getPath());
        	defaultValuesStore.storeRadioSelection(LocationPage.FILESYSTEM);
        	break;
        	
        case LocationPage.CLIPBOARD:
            defaultValuesStore.storeRadioSelection(LocationPage.CLIPBOARD);
        	break;
        	
        default:
            return false;
        }
        return true;
    }
    
    public boolean validateFile(File file) {
        
        if (file == null)
            return false;
        
        /**
         * Consider file valid if it doesn't exist for now.
         */
        if (!file.exists())
            return true;
        
    	/**
    	 * The file exists.
    	 */
    	if (!file.canWrite()) {
    	    final String title= CVSUIMessages.GenerateCVSDiff_1; 
    	    final String msg= CVSUIMessages.GenerateCVSDiff_2; 
    	    final MessageDialog dialog= new MessageDialog(getShell(), title, null, msg, MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL }, 0);
    	    dialog.open();
    	    return false;
    	}
    	
    	final String title = CVSUIMessages.GenerateCVSDiff_overwriteTitle; 
    	final String msg = CVSUIMessages.GenerateCVSDiff_overwriteMsg; 
    	final MessageDialog dialog = new MessageDialog(getShell(), title, null, msg, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
    	dialog.open();
    	if (dialog.getReturnCode() != 0)            
    	    return false;
    	
    	return true;
    }
}
