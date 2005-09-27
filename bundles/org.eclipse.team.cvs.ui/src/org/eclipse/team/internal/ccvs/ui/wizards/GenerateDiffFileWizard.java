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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Diff;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.operations.*;
import org.eclipse.team.internal.ccvs.ui.subscriber.CreatePatchWizardParticipant;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;
import org.eclipse.team.internal.core.subscribers.SubscriberSyncInfoCollector;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.PageBook;

/**
 * A wizard for creating a patch file by running the CVS diff command.
 */
public class GenerateDiffFileWizard extends Wizard {
	
	//temp falg for enabling workspace patches
	public final static String WORKSPACEPATCH_FLAG = "workspacePatch"; //$NON-NLS-1$
	//The initial size of this wizard.
    private final static int INITIAL_WIDTH = 300;
    private final static int INITIAL_HEIGHT = 350;
   
	public static void run(IWorkbenchPart part, final IResource[] resources) {
		final String title = CVSUIMessages.GenerateCVSDiff_title; 
		final GenerateDiffFileWizard wizard = new GenerateDiffFileWizard(part,resources);
		wizard.setWindowTitle(title);
		WizardDialog dialog = new WizardDialog(part.getSite().getShell(), wizard);
		dialog.setMinimumPageSize(INITIAL_WIDTH, INITIAL_HEIGHT);
		dialog.open();
	}
	
    /**
     * Page to select a patch file. Overriding validatePage was necessary to allow
     * entering a file name that already exists.
     */
    public class LocationPage extends WizardPage {
        
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
    	protected Text wsPathText;
        private Button wsBrowseButton;
       
        protected CreatePatchWizardParticipant fParticipant;
        private Button chgSelectAll;
        private Button chgDeselectAll;
        
        /**
         * State information of this page, updated by the listeners.
         */
        protected boolean canValidate;
        protected boolean pageValid;
        protected IContainer wsSelectedContainer;
        protected IPath[] foldersToCreate;
        protected int selectedLocation;
        
        /**
         * The default values store used to initialize the selections.
         */
        private final DefaultValuesStore store;
	
        
    	class LocationPageContentProvider extends BaseWorkbenchContentProvider {
    		//Never show closed projects
    		boolean showClosedProjects=false;
    		
    		public Object[] getChildren(Object element) {
    			if (element instanceof IWorkspace) {
    	            // check if closed projects should be shown
    	            IProject[] allProjects = ((IWorkspace) element).getRoot().getProjects();
    	            if (showClosedProjects)
    	                return allProjects;

    	            ArrayList accessibleProjects = new ArrayList();
    	            for (int i = 0; i < allProjects.length; i++) {
    	                if (allProjects[i].isOpen()) {
    	                    accessibleProjects.add(allProjects[i]);
    	                }
    	            }
    	            return accessibleProjects.toArray();
    	        } 
    			
    			return super.getChildren(element);
    		}
    	}
    	
    	class WorkspaceDialog extends TitleAreaDialog {

    		protected TreeViewer wsTreeViewer;
    	    protected Text wsFilenameText;
    	    protected Image dlgTitleImage;
    	    
			public WorkspaceDialog(Shell shell) {
				super(shell);
			}
			
			protected Control createContents(Composite parent) {
		        Control control = super.createContents(parent);
				setTitle(CVSUIMessages.Select_a_folder_then_type_in_the_file_name__8);
			    //create title image
				dlgTitleImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_DIFF).createImage();
				setTitleImage(dlgTitleImage);
				
		        return control;
			}
			
			protected Control createDialogArea(Composite parent){
				Composite composite = (Composite) super.createDialogArea(parent);
				
				
				GridLayout layout = new GridLayout();
				layout.numColumns = 1;	
				composite.setLayout(layout);
				final GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
				composite.setLayoutData(data);
	            
				getShell().setText(CVSUIMessages.GenerateDiffFileWizard_9); 
		
                wsTreeViewer = new TreeViewer(composite, SWT.BORDER);
                final GridData gd= new GridData(SWT.FILL, SWT.FILL, true, true);
                gd.widthHint= 550;
                gd.heightHint= 250;
                wsTreeViewer.getTree().setLayoutData(gd);
               
                wsTreeViewer.setContentProvider(new LocationPageContentProvider());
                wsTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
                wsTreeViewer.setInput(ResourcesPlugin.getWorkspace());
                
                //Open to whatever is selected in the workspace field
                IPath existingWorkspacePath = new Path(wsPathText.getText());
                if (existingWorkspacePath != null){
                	//Ensure that this workspace path is valid
                	IResource selectedResource = ResourcesPlugin.getWorkspace().getRoot().findMember(existingWorkspacePath);
                	if (selectedResource != null) {
                		wsTreeViewer.expandToLevel(selectedResource, 0);
                		wsTreeViewer.setSelection(new StructuredSelection(selectedResource));
            		}
                }
                
                final Composite group = new Composite(composite, SWT.NONE);
                layout = new GridLayout(2, false);
                layout.marginWidth = 0;
                group.setLayout(layout);
                group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
                
                final Label label = new Label(group, SWT.NONE);
                label.setLayoutData(new GridData());
                label.setText(CVSUIMessages.Fi_le_name__9); 
                
                wsFilenameText = new Text(group,SWT.BORDER);
                wsFilenameText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
				
                setupListeners();
				
				return parent;
			}
		
			protected void okPressed() {
				//make sure that a filename has been typed in 
				
				String patchName = wsFilenameText.getText();
				
				if (patchName.equals("")){ //$NON-NLS-1$
					setErrorMessage(CVSUIMessages.GenerateDiffFileWizard_2);
			    	return;
				}
				
				//make sure that the filename does not contain more than one segment
				if (!(ResourcesPlugin.getWorkspace().validateName(patchName, IResource.FILE)).isOK()){
					wsFilenameText.setText(""); //$NON-NLS-1$
					setErrorMessage(CVSUIMessages.GenerateDiffFileWizard_File_multisegments);
			    	return;
				}
				
				IFile file = wsSelectedContainer.getFile(new Path(wsFilenameText.getText()));
				if (file != null)
					wsPathText.setText(file.getFullPath().toString());
				
				validatePage();
				super.okPressed();
			}
			
			protected void cancelPressed() {
			  validatePage();
			  super.cancelPressed();
			}
		 
		   public boolean close() {
		        if (dlgTitleImage != null)
		            dlgTitleImage.dispose();
		        return super.close();
		    }
			  
			void setupListeners(){
				 wsTreeViewer.addSelectionChangedListener(
                new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        IStructuredSelection s = (IStructuredSelection)event.getSelection();
                        Object obj=s.getFirstElement();
                        if (obj instanceof IContainer)
                        	wsSelectedContainer = (IContainer) obj;
                        else if (obj instanceof IFile){
                        	IFile tempFile = (IFile) obj;
                        	wsSelectedContainer = tempFile.getParent();
                        	wsFilenameText.setText(tempFile.getName());
                        }
          
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
		              setErrorMessage(null);
		            }
		        });
			}
    	}
    	
        LocationPage(String pageName, String title, ImageDescriptor image, DefaultValuesStore store) {
            super(pageName, title, image);
            setPageComplete(false);
            this.store= store;
            this.canValidate=false;
        }
        
        /**
         * Allow the user to finish if a valid file has been entered. 
         */
        protected boolean validatePage() {
            
        	if (!canValidate)
        		return false;
        	
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
            //make sure that the field actually has a filename in it
            if (wsPathText.getText().equals("")){ //$NON-NLS-1$
            	if (selectedLocation ==WORKSPACE)
            		setErrorMessage(CVSUIMessages.GenerateDiffFileWizard_5);
            	return false;
            }
            
            //Make sure that all the segments but the last one (i.e. project + all
            //folders) exist - file doesn't have to exist. It may have happened that
            //some folder refactoring has been done since this path was last saved.
            //
            //Assume that the path will always be in format project/{folders}*/file - this
            //is controlled by the workspace location dialog
            
            
            IPath pathToWorkspaceFile = new Path(wsPathText.getText());
            //Trim file name from path
            IPath containerPath = pathToWorkspaceFile.removeLastSegments(1);
            
            IResource container =ResourcesPlugin.getWorkspace().getRoot().findMember(containerPath);
            if (container == null) {
            	if (selectedLocation == WORKSPACE)
            		setErrorMessage(CVSUIMessages.GenerateDiffFileWizard_4); 
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
                final String filename= wsPathText.getText().trim();
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                final IFile file= root.getFile(new Path(filename));
                return file.getLocation().toFile();
            }
            return null;
        }
        
        /**
         * Answers the workspace string entered in the dialog or <code>null</code> if the user
         * selected to save the patch in the clipboard or file system.
         */
        public String getWorkspaceLocation() {
           
            if (pageValid && selectedLocation == WORKSPACE) {
                final String filename= wsPathText.getText().trim();
                return filename;
            }
            return null;
        }
        
        /**
         * Get the selected workspace resource if the patch is to be saved in the 
         * workspace, or null otherwise.
         */
        public IResource getResource() {
            if (pageValid && selectedLocation == WORKSPACE) {
                IPath pathToWorkspaceFile = new Path(wsPathText.getText().trim());
                //Trim file name from path
                IPath containerPath = pathToWorkspaceFile.removeLastSegments(1);
                return ResourcesPlugin.getWorkspace().getRoot().findMember(containerPath);
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
            
            //Create a location group
            Group locationGroup = new Group(composite, SWT.None);
            GridLayout layout = new GridLayout();
            locationGroup.setLayout(layout);
            GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
            locationGroup.setLayoutData(data);
            locationGroup.setText(CVSUIMessages.GenerateDiffFileWizard_9);
            //
            setupClipboardControls(locationGroup);    			
            setupFilesystemControls(locationGroup);
            setupWorkspaceControls(locationGroup);
            
            initializeDefaultValues();
            
			fParticipant = new CreatePatchWizardParticipant(new ResourceScope(((GenerateDiffFileWizard)this.getWizard()).resources), (GenerateDiffFileWizard) this.getWizard());
			try {
				getAllOutOfSync();
			} catch (CVSException e) {}
	           
			final PixelConverter converter= new PixelConverter(parent);
			createChangesArea(composite, converter);

			createSelectionButtons(composite);
           
            Dialog.applyDialogFont(parent);
     
            /**
             * Ensure the page is in a valid state. 
             */
            /*if (!validatePage()) {
                store.storeRadioSelection(CLIPBOARD);
                initializeDefaultValues();
                validatePage();
            }
            pageValid= true;*/
            validatePage();
            
            updateEnablements();
            setupListeners();
        }
        
        
        private void createSelectionButtons(Composite composite) {
        	final Composite buttonGroup = new Composite(composite,SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.numColumns = 2;
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            layout.horizontalSpacing = 0;
            layout.verticalSpacing = 0;
            buttonGroup.setLayout(layout);
            GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END
                    | GridData.VERTICAL_ALIGN_CENTER);
            buttonGroup.setLayoutData(data);
            
            chgSelectAll = createSelectionButton(CVSUIMessages.GenerateDiffFileWizard_SelectAll, buttonGroup);
            chgDeselectAll = createSelectionButton(CVSUIMessages.GenerateDiffFileWizard_DeselectAll, buttonGroup);
		}

		private Button createSelectionButton(String buttonName, Composite buttonGroup) {
			Button button = new Button(buttonGroup,SWT.PUSH);
			button.setText(buttonName);
			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	        int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
	        Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	        data.widthHint = Math.max(widthHint, minSize.x);
	        button.setLayoutData(data);
	        return button;
		}

		/**
         * Setup the controls for the workspace option.
         */
        private void setupWorkspaceControls(Composite composite) {
            GridLayout layout;
            
            wsRadio= new Button(composite, SWT.RADIO);
            wsRadio.setText(CVSUIMessages.Save_In_Workspace_7); 
            
            final Composite nameGroup = new Composite(composite,SWT.NONE);
            layout = new GridLayout();
            layout.numColumns = 2;
            layout.marginWidth = 0;
            nameGroup.setLayout(layout);
            final GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
            nameGroup.setLayoutData(data);
            
            wsPathText= new Text(nameGroup, SWT.BORDER);
            GridData gd= new GridData(GridData.FILL_HORIZONTAL);
            wsPathText.setLayoutData(gd);
            wsPathText.setEditable(false);
            
            wsBrowseButton = new Button(nameGroup, SWT.NULL);
            wsBrowseButton.setText(CVSUIMessages.Browse____4); 
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
            final GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
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
        
    	private ParticipantPagePane fPagePane;
        private PageBook bottomChild;
        private ISynchronizePageConfiguration fConfiguration;
        
        private void createChangesArea(Composite parent, PixelConverter converter) {
            
            int size = fParticipant.getSyncInfoSet().size();
            if (size > getFileDisplayThreshold()) {
                // Create a page book to allow eventual inclusion of changes
                bottomChild = new PageBook(parent, SWT.NONE);
                bottomChild.setLayoutData(SWTUtils.createGridData(SWT.DEFAULT, SWT.DEFAULT, SWT.FILL, SWT.FILL, true, false));
                // Create composite for showing the reason for not showing the changes and a button to show them
                Composite changeDesc = new Composite(bottomChild, SWT.NONE);
                changeDesc.setLayout(SWTUtils.createGridLayout(1, converter, SWTUtils.MARGINS_NONE));
                SWTUtils.createLabel(changeDesc, NLS.bind(CVSUIMessages.CommitWizardCommitPage_1, new String[] { Integer.toString(size), Integer.toString(getFileDisplayThreshold()) })); 
                Button showChanges = new Button(changeDesc, SWT.PUSH);
                showChanges.setText(CVSUIMessages.CommitWizardCommitPage_5); 
                showChanges.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        showChangesPane();
                    }
                });
                showChanges.setLayoutData(new GridData());
                bottomChild.showPage(changeDesc);
            } else {
                final Composite composite= new Composite(parent, SWT.NONE);
                composite.setLayout(SWTUtils.createGridLayout(1, converter, SWTUtils.MARGINS_NONE));
                composite.setLayoutData(SWTUtils.createGridData(SWT.DEFAULT, SWT.DEFAULT, SWT.FILL, SWT.FILL, true, true));
                
                createPlaceholder(composite);
               
                Control c = createChangesPage(composite, fParticipant);
                c.setLayoutData(SWTUtils.createHVFillGridData());
            }
        }
        
        protected void showChangesPane() {
            Control c = createChangesPage(bottomChild, fParticipant);
            bottomChild.setLayoutData(SWTUtils.createGridData(SWT.DEFAULT, SWT.DEFAULT, SWT.FILL, SWT.FILL, true, true));
            bottomChild.showPage(c);
            Dialog.applyDialogFont(getControl());
            ((Composite)getControl()).layout();
        }

        private Control createChangesPage(final Composite composite, WorkspaceSynchronizeParticipant participant) {
            fConfiguration= participant.createPageConfiguration();
            fPagePane= new ParticipantPagePane(getShell(), true /* modal */, fConfiguration, participant);
            Control control = fPagePane.createPartControl(composite);
            return control;
        }

    	private int getFileDisplayThreshold() {
            return CVSUIPlugin.getPlugin().getPreferenceStore().getInt(ICVSUIConstants.PREF_COMMIT_FILES_DISPLAY_THRESHOLD);
        }
    	
    	 private void createPlaceholder(final Composite composite) {
    	        final Composite placeholder= new Composite(composite, SWT.NONE);
    	        placeholder.setLayoutData(new GridData(SWT.DEFAULT, convertHorizontalDLUsToPixels(IDialogConstants.VERTICAL_SPACING) /3));
    	 }
        /**
         * Initialize the controls with the saved default values which are
         * obtained from the DefaultValuesStore.
         */
        private void initializeDefaultValues() {

            selectedLocation= store.getRadioSelection();
            
            updateRadioButtons();
            
            /**
             * Text fields.
             */
            fsPathText.setText(store.getFilesystemPath());
            //We need to ensure that we have a valid workspace path - user
            //could have altered workspace since last time this was saved
            wsPathText.setText(store.getWorkspacePath());
            if(!validateWorkspaceLocation()) {
            	wsPathText.setText("");     //$NON-NLS-1$
            	
            	//Don't open wizard with an error - instead change selection
            	//to clipboard
            	if (selectedLocation == WORKSPACE){
            		//clear the error message caused by the workspace not having
            		//any workspace path entered
            		setErrorMessage(null);
            		selectedLocation=CLIPBOARD;
            		updateRadioButtons();
            	}
            }
        }

		private void updateRadioButtons() {
			/**
             * Radio buttons
             */
            cpRadio.setSelection(selectedLocation == CLIPBOARD);
            fsRadio.setSelection(selectedLocation == FILESYSTEM);
            wsRadio.setSelection(selectedLocation == WORKSPACE);
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
            
           
            
            wsBrowseButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                	final WorkspaceDialog dialog = new WorkspaceDialog(getShell());
                	dialog.open();
                	
                
                }
            });		
            
            
            chgSelectAll.addSelectionListener(new SelectionAdapter() {
    			public void widgetSelected(SelectionEvent e) {
    				initCheckedItems();
    				//Only bother changing isPageComplete state if the current state
    				//is not enabled
    				if (!isPageComplete())
    					setPageComplete((getSelectedResources()).length > 0);
    			}
    		});
            
            chgDeselectAll.addSelectionListener(new SelectionAdapter() {
    			public void widgetSelected(SelectionEvent e) {
    				ISynchronizePage page = fConfiguration.getPage();
    				if (page != null){
    					Viewer viewer = page.getViewer();
    	    			if (viewer instanceof CheckboxTreeViewer) {
    	    				CheckboxTreeViewer treeViewer =(CheckboxTreeViewer)viewer;
    	    				treeViewer.setCheckedElements(new Object[0]);
    	    			}
    				}
    				//Only bother changing isPageComplete state if the current state
    				//is enabled
    				if (isPageComplete())
    					setPageComplete((getSelectedResources()).length > 0);
    			}
    		});
            
            ISynchronizePage page = fConfiguration.getPage();
    		if (page != null) {
    			Viewer viewer = page.getViewer();
    			if (viewer instanceof CheckboxTreeViewer) {
    				((CheckboxTreeViewer)viewer).addCheckStateListener(new ICheckStateListener() {
    					public void checkStateChanged(CheckStateChangedEvent event) {
    						setPageComplete((resources = getSelectedResources()).length > 0);
    					}
    				});
    			}
    		}
        }

        protected void initCheckedItems() {
        	ISynchronizePage page = fConfiguration.getPage();
    		if (page != null) {
				Viewer viewer = page.getViewer();
				if (viewer instanceof CheckboxTreeViewer) {
					TreeItem[] items=((CheckboxTreeViewer)viewer).getTree().getItems();
					for (int i = 0; i < items.length; i++) {
						((CheckboxTreeViewer)viewer).setChecked(items[i].getData(), true);
					}
				}
    		}
		}

		protected IResource[] getSelectedResources() {
    		ISynchronizePage page = fConfiguration.getPage();
    		if (page != null) {
    			Viewer viewer = page.getViewer();
    			if (viewer instanceof CheckboxTreeViewer) {
    				Object[] elements = ((CheckboxTreeViewer)viewer).getCheckedElements();
    				IResource[]selectedResources = Utils.getResources(elements);
    				ArrayList result = new ArrayList();
    				for (int i = 0; i < selectedResources.length; i++) {
    					IResource resource = selectedResources[i];
    					if (fConfiguration.getSyncInfoSet().getSyncInfo(resource) != null) {
    						result.add(resource);
    					}
    				}
    				return (IResource[]) result.toArray(new IResource[result.size()]);
    			}
    		}
    		return new IResource[0];
    	}
        
        /**
         * Enable and disable controls based on the selected radio button.
         */
        public void updateEnablements() {
            fsBrowseButton.setEnabled(selectedLocation == FILESYSTEM);
            fsPathText.setEnabled(selectedLocation == FILESYSTEM);
            wsPathText.setEnabled(selectedLocation == WORKSPACE);
            wsBrowseButton.setEnabled(selectedLocation == WORKSPACE);
        }
        
        public int getSelectedLocation() {
            return selectedLocation;
        }
        
        private SyncInfoSet getAllOutOfSync() throws CVSException {
    		final SubscriberSyncInfoCollector syncInfoCollector = fParticipant.getSubscriberSyncInfoCollector();
        	//WaitForChangesJob waits for the syncInfoCollector to get all the changes
    		//before checking off the tree items and validating the page
    		class WaitForChangesJob extends Job{
        		LocationPage fLocationPage;
        		
        		 public WaitForChangesJob(LocationPage page) {
        	         super(""); //$NON-NLS-1$
        	         fLocationPage=page;
        	      }
        	      public IStatus run(IProgressMonitor monitor) {
        	    	  monitor.beginTask(CVSUIMessages.CommitWizard_4, IProgressMonitor.UNKNOWN); 
			    	  syncInfoCollector.waitForCollector(monitor);
			    	  Utils.syncExec(new Runnable() {
			                public void run() {
			                    fLocationPage.initCheckedItems();
			                    fLocationPage.canValidate=true;
			                    fLocationPage.validatePage();
			                }
			          }, getControl());
			    	  monitor.done();
        	         return Status.OK_STATUS;
        	      }
        	}
        	WaitForChangesJob job =new WaitForChangesJob(this);
        	//Don't need the job in the UI, make it a system job
        	job.setSystem(true);
			job.schedule();
    		return fParticipant.getSyncInfoSet();
    	}
     
    }
        
    /**
     * Page to select the options for creating the patch.
     */
    private class OptionsPage extends WizardPage {
       
    	private Button unifiedDiffOption;
    	private Button unified_workspaceRelativeOption; //multi-patch format
    	private Button unified_projectRelativeOption; //full project path
    	private Button unified_selectionRelativeOption; //use path of whatever is selected
        private Button contextDiffOption;
        private Button regularDiffOption;

        private boolean patchHasCommonRoot=true;
        protected IPath patchRoot=ResourcesPlugin.getWorkspace().getRoot().getFullPath();
        
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
                        
            Group diffTypeGroup = new Group(composite, SWT.NONE);
            layout = new GridLayout();
            layout.marginHeight = 0;
            diffTypeGroup.setLayout(layout);
            GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
            diffTypeGroup.setLayoutData(data);
            diffTypeGroup.setText(CVSUIMessages.Diff_output_format_12); 
            

            unifiedDiffOption = new Button(diffTypeGroup, SWT.RADIO);
            unifiedDiffOption.setText(CVSUIMessages.Unified__format_required_by_Compare_With_Patch_feature__13);
            
            contextDiffOption = new Button(diffTypeGroup, SWT.RADIO);
            contextDiffOption.setText(CVSUIMessages.Context_14); 
            regularDiffOption = new Button(diffTypeGroup, SWT.RADIO);
            regularDiffOption.setText(CVSUIMessages.Standard_15); 
            
            //Unified Format Options
            Group unifiedGroup = new Group(composite, SWT.None);
            layout = new GridLayout();
            unifiedGroup.setLayout(layout);
            data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
            unifiedGroup.setLayoutData(data);
            unifiedGroup.setText(CVSUIMessages.GenerateDiffFileWizard_10);
            
            unified_workspaceRelativeOption = new Button(unifiedGroup, SWT.RADIO);
            unified_workspaceRelativeOption.setText(CVSUIMessages.GenerateDiffFileWizard_6);
            unified_workspaceRelativeOption.setSelection(true);
            
            unified_projectRelativeOption = new Button(unifiedGroup, SWT.RADIO);
            unified_projectRelativeOption.setText(CVSUIMessages.GenerateDiffFileWizard_7);
            
            unified_selectionRelativeOption = new Button(unifiedGroup, SWT.RADIO);
            unified_selectionRelativeOption.setText(CVSUIMessages.GenerateDiffFileWizard_8);
            
            //temp disable if workspacePatch flag not set and default to selection 
            if (!Boolean.getBoolean(GenerateDiffFileWizard.WORKSPACEPATCH_FLAG)){
            	unified_selectionRelativeOption.setSelection(true);
            	unified_workspaceRelativeOption.setSelection(false);
            	unified_workspaceRelativeOption.setEnabled(false);
            	unified_projectRelativeOption.setEnabled(false);
            	unified_selectionRelativeOption.setEnabled(false);
            }
            
            Dialog.applyDialogFont(parent);
            
            //add listeners
            unifiedDiffOption.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setEnableUnifiedGroup(true);
					updateEnablements();
				}
			});
            
            contextDiffOption.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setEnableUnifiedGroup(false);
					updateEnablements();
				}
			});
            
            regularDiffOption.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setEnableUnifiedGroup(false);
					updateEnablements();
				}
			});
            
           calculatePatchRoot();
           unifiedDiffOption.setSelection(true);
           updateEnablements();
        }    
        
        protected void updateEnablements() {
			if (!patchHasCommonRoot){
				unified_selectionRelativeOption.setEnabled(false);
				unified_projectRelativeOption.setEnabled(false);
				contextDiffOption.setEnabled(false);
				regularDiffOption.setEnabled(false);
			}
		}

        private void calculatePatchRoot(){
        	//check to see if this is a multi select patch, if so disable 
        	IResource[] tempResources = ((GenerateDiffFileWizard)this.getWizard()).resources;
			if (((GenerateDiffFileWizard)this.getWizard()).resources.length > 1){
				//Check to see is the selected resources are contained by the same parent (climbing
				//parent by parent to the project root)
				//If so, then allow selection relative patches -> set the relative path to the common
				//parent [also allow project relative patches]
				//If parents are different projects, allow only multiproject selection
				
				patchHasCommonRoot=true;
				int segmentMatch=-1;
				IPath path = tempResources[0].getFullPath().removeLastSegments(1);
				for (int i = 1; i < tempResources.length; i++) {
					int segments=path.matchingFirstSegments(tempResources[i].getFullPath());
					//Keep track of the lowest number of matches that were found - the common
					//path will be this number
					if (segmentMatch == -1 ||
						segmentMatch>segments){
						segmentMatch=segments;
					}
					//However, if no segments for any one resource - break out of the loop
					if (segments == 0){
						patchHasCommonRoot=false;
						break;
					}
				}
				if (patchHasCommonRoot){
					IPath tempPath = path.uptoSegment(segmentMatch);
					/*IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
					while (!root.exists(tempPath) &&
							!tempPath.isRoot()){
						tempPath = tempPath.removeLastSegments(1);
					}*/
					patchRoot=tempPath; 
				}
			} else {
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				
				//take the file name off the path and use that as the patch root
				//patchRoot = tempResources[0].getFullPath().removeLastSegments(1);
				IPath fullPath = tempResources[0].getFullPath();
				IResource resource = root.findMember(fullPath);
				
				//keep trimming the path until we find something that can be used as the
				//patch root
				while (resource == null &&
					   !(resource instanceof IWorkspaceRoot)){
					fullPath=fullPath.removeLastSegments(1);
					resource=root.findMember(fullPath);
				}
				patchRoot = resource.getFullPath();
				if (resource.getType() == IResource.FILE)
					patchRoot =resource.getFullPath().removeLastSegments(1);				 	
			}
        }
		/**
         * Return the list of Diff command options configured on this page.
         */
        public LocalOption[] getOptions() {
            List options = new ArrayList(5);
          /*  if(includeNewFilesOptions.getSelection()) {
                options.add(Diff.INCLUDE_NEWFILES);
            }
            if(!recurseOption.getSelection()) {
                options.add(Command.DO_NOT_RECURSE);
            }*/
            
            //Add new files for now
            options.add(Diff.INCLUDE_NEWFILES);
            
            if(unifiedDiffOption.getSelection() || unified_workspaceRelativeOption.getSelection()) {
                options.add(Diff.UNIFIED_FORMAT);
            } else if(contextDiffOption.getSelection()) {
                options.add(Diff.CONTEXT_FORMAT);
            } 
            
            return (LocalOption[]) options.toArray(new LocalOption[options.size()]);
        }		
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            if (visible) {
                unified_workspaceRelativeOption.setFocus();
            }
        }
        protected void setEnableUnifiedGroup(boolean enabled){
        	unified_workspaceRelativeOption.setEnabled(enabled);
        	unified_projectRelativeOption.setEnabled(enabled);
        	unified_selectionRelativeOption.setEnabled(enabled);
        }
    }
 
    /**
     * Class to retrieve and store the default selected values. 
     */
    private final class DefaultValuesStore {
        
        private static final String PREF_LAST_SELECTION= "org.eclipse.team.internal.ccvs.ui.wizards.GenerateDiffFileWizard.PatchFileSelectionPage.lastselection"; //$NON-NLS-1$
        private static final String PREF_LAST_FS_PATH= "org.eclipse.team.internal.ccvs.ui.wizards.GenerateDiffFileWizard.PatchFileSelectionPage.filesystem.path"; //$NON-NLS-1$
        private static final String PREF_LAST_WS_PATH= "org.eclipse.team.internal.ccvs.ui.wizards.GenerateDiffFileWizard.PatchFileSelectionPage.workspace.path"; //$NON-NLS-1$
        
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
        
        public String getWorkspacePath() {
            final String path= dialogSettings.get(PREF_LAST_WS_PATH);
            return path != null ? path : ""; //$NON-NLS-1$
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
    }
    
    private LocationPage locationPage;
    private OptionsPage optionsPage;
    
    protected IResource[] resources;
    private final DefaultValuesStore defaultValuesStore;
	private final IWorkbenchPart part;
  
	
    public GenerateDiffFileWizard(IWorkbenchPart part, IResource[] resources) {
        super();
		this.part = part;
        this.resources = resources;
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
    
        //Is this a multi-patch?
        boolean multiPatch=false;
        if (optionsPage.unifiedDiffOption.getSelection() && optionsPage.unified_workspaceRelativeOption.getSelection())
        	multiPatch=true;
        
       
        //If not a multipatch, patch should use project relative or selection relative paths[default]?
        boolean useProjectRelativePaths=false;
        if (optionsPage.unifiedDiffOption.getSelection() && 
        	optionsPage.unified_projectRelativeOption.getSelection())
        	useProjectRelativePaths=true;
        
        /**
         * Perform diff operation.
         */
	    try {
	     if (file != null) {
	    	generateDiffToFile(file,multiPatch,useProjectRelativePaths);
	     } else {
			generateDiffToClipboard(multiPatch,useProjectRelativePaths);
	     }
		} catch (TeamException e) {}
        
        /**
         * Refresh workspace if necessary and save default selection.
         */
        switch (location) {
        
        case LocationPage.WORKSPACE:
            final String workspaceResource= locationPage.getWorkspaceLocation();
            if (workspaceResource != null){
                defaultValuesStore.storeRadioSelection(LocationPage.WORKSPACE);
	            defaultValuesStore.storeWorkspacePath(workspaceResource);
	           /* try {
	                workspaceResource.getParent().refreshLocal(IResource.DEPTH_ONE, null);
	            } catch(CoreException e) {
	                CVSUIPlugin.openError(getShell(), CVSUIMessages.GenerateCVSDiff_error, null, e); 
	                return false;
	            } */
            } else {
            	//Problem with workspace location, open with clipboard next time 
            	defaultValuesStore.storeRadioSelection(LocationPage.CLIPBOARD);
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
    
	private void generateDiffToClipboard(boolean multiPatch, boolean useProjectRelativePaths) throws TeamException {
		DiffOperation diffop = new ClipboardDiffOperation(part,RepositoryProviderOperation.asResourceMappers(resources),optionsPage.getOptions(),multiPatch, useProjectRelativePaths, optionsPage.patchRoot);
    	try {
			diffop.run();
		} catch (InvocationTargetException e) {} 
		  catch (InterruptedException e) {}
	}

	private void generateDiffToFile(File file, boolean multiPatch, boolean useProjectRelativePaths) throws TeamException {
		DiffOperation diffop = null;
		if (locationPage.selectedLocation == LocationPage.WORKSPACE){
			diffop = new WorkspaceFileDiffOperation(part,RepositoryProviderOperation.asResourceMappers(resources),optionsPage.getOptions(),file, multiPatch, useProjectRelativePaths, optionsPage.patchRoot);
		}
		else {
			diffop = new FileDiffOperation(part,RepositoryProviderOperation.asResourceMappers(resources),optionsPage.getOptions(),file, multiPatch, useProjectRelativePaths, optionsPage.patchRoot);
		}
		
    	try {
			diffop.run();
		} catch (InvocationTargetException e) {} 
		  catch (InterruptedException e) {}
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

	public LocationPage getLocationPage() {
		return locationPage;
	}
	
}
