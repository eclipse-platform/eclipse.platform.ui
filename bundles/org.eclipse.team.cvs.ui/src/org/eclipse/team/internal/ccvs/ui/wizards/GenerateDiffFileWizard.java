/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.team.internal.ccvs.core.client.Diff;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.misc.ContainerContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A wizard for creating a patch file by running the CVS diff command.
 */
public class GenerateDiffFileWizard extends Wizard {
	
	private PatchFileSelectionPage mainPage;
	private PatchFileCreationOptionsPage optionsPage;
	
	private IStructuredSelection selection;
	private IResource resource;

	/**
	 * Page to select a patch file. Overriding validatePage was necessary to allow
	 * entering a file name that already exists.
	 */
	private class PatchFileSelectionPage extends WizardPage {
		private Text filenameCombo;
		private Button browseButton;
		
		private TreeViewer treeViewer;
		private IContainer selectedContainer;
		private Text workspaceFilename;
		private Button saveInFilesystem;
		private Button saveInWorkspace;
		private Button saveToClipboard;
		
		public final int CLIPBOARD = 1;
		public final int FILESYSTEM = 2;
		public final int WORKSPACE = 3;
		
		// sizing constants
		private static final int SIZING_SELECTION_PANE_HEIGHT = 125;
		private static final int SIZING_SELECTION_PANE_WIDTH = 200;
		
		PatchFileSelectionPage(String pageName, String title, ImageDescriptor image, IStructuredSelection selection) {
			super(pageName, title, image);
			setPageComplete(false);
		}
		
		/**
		 * Allow the user to finish if a valid file has been entered. 
		 */
		protected boolean validatePage() {
			boolean valid = false;									
			
			switch (getSaveType()) {
				case WORKSPACE:
					if (selectedContainer != null && getWorkspaceFile() != null) {
						valid = true;
					}
					break;
				case FILESYSTEM:
					File file = new File(getFilesystemFile());
					valid = isValidFile(file);
					break;
				case CLIPBOARD:
					valid = true;
					break;
			}
					
			// Avoid draw flicker by clearing error message
			// if all is valid.
			if (valid) {
				setMessage(null);
				setErrorMessage(null);
			} else {
				setErrorMessage(Policy.bind("Enter_a_valid_file_name_or_select_the_clipboard_option_1")); //$NON-NLS-1$
			}
			setPageComplete(valid);
			return valid;
		}

		private boolean isValidFile(File file) {
			if (!file.isAbsolute()) return false;
			if (file.isDirectory()) return false;
			File parent = file.getParentFile();
			if (parent==null) return false;
			if (!parent.exists()) return false;
			if (!parent.isDirectory()) return false;
			return true;
		}
		/**
		 * Answers a full path to a file system file or <code>null</code> if the user
		 * selected to save the patch in the workspace. 
		 */
		public String getFilesystemFile() {
			if(saveInFilesystem.getSelection()) {
				return filenameCombo.getText();
			} 
			return null;
		}
		
		/**
		 * Answers a workspace file or <code>null</code> if the user selected to save
		 * the patch outside of the workspace.
		 */
		public IFile getWorkspaceFile() {
			if(saveInWorkspace.getSelection() && selectedContainer !=null) {
				String filename = workspaceFilename.getText();
				if(filename==null || filename.length() == 0) {
					return null;
				}
				return selectedContainer.getFile(new Path(workspaceFilename.getText()));
			}
			return null;
		}

		/**
		 * Allow the user to chose to save the patch to the workspace or outside
		 * of the workspace.
		 */
		public void createControl(Composite parent) {
			
			Composite composite= new Composite(parent, SWT.NULL);
			GridLayout layout= new GridLayout();
			composite.setLayout(layout);
			composite.setLayoutData(new GridData());
			setControl(composite);
			initializeDialogUnits(composite);

			// set F1 help
			WorkbenchHelp.setHelp(composite, IHelpContextIds.PATCH_SELECTION_PAGE);
					
			// Clipboard
			saveToClipboard= new Button(composite, SWT.RADIO);
			saveToClipboard.setText(Policy.bind("Save_To_Clipboard_2")); //$NON-NLS-1$
			saveToClipboard.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					validatePage();
					updateEnablements();
				}
			});
			
			// File System
			saveInFilesystem= new Button(composite, SWT.RADIO);
			saveInFilesystem.setText(Policy.bind("Save_In_File_System_3")); //$NON-NLS-1$
			saveInFilesystem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					validatePage();
					updateEnablements();
				}
			});

			Composite nameGroup = new Composite(composite,SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginWidth = 0;
			nameGroup.setLayout(layout);
			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
			nameGroup.setLayoutData(data);
			
			filenameCombo= new Text(nameGroup, SWT.BORDER);
			GridData gd= new GridData(GridData.FILL_HORIZONTAL);
			filenameCombo.setLayoutData(gd);
			filenameCombo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validatePage();
				}
			});

			browseButton = new Button(nameGroup, SWT.NULL);
			browseButton.setText(Policy.bind("Browse..._4")); //$NON-NLS-1$
			data = new GridData(GridData.HORIZONTAL_ALIGN_END);
			data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
			browseButton.setLayoutData(data);
			browseButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					FileDialog d = new FileDialog(getShell(), SWT.PRIMARY_MODAL | SWT.SAVE);
					d.setText(Policy.bind("Save_Patch_As_5")); //$NON-NLS-1$
					d.setFileName(Policy.bind("patch.txt_6")); //$NON-NLS-1$
					String file = d.open();
					if(file!=null) {
						IPath path = new Path(file);
						setFilesystemFilename(path.toOSString());
					}			
				}
			});			
			
			// Workspace
			saveInWorkspace= new Button(composite, SWT.RADIO);
			saveInWorkspace.setText(Policy.bind("Save_In_Workspace_7")); //$NON-NLS-1$
			saveInWorkspace.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					validatePage();
					updateEnablements();
				}
			});
			
			createTreeViewer(composite);		
			saveToClipboard.setSelection(true);
			validatePage();
			updateEnablements();
		}
		
		/**
		 * Sets the file name in the file system text.
		 */
		protected void setFilesystemFilename(String filename) {
			filenameCombo.setText(filename);
		}
		
		/**
		 * Create the tree viewer that shows the container available in the workspace. The user
		 * can then enter a filename in the text box below the viewer.
		 */
		protected void createTreeViewer(Composite parent) {
			// Create tree viewer inside drill down.
			new Label(parent, SWT.LEFT).setText(Policy.bind("Select_a_folder_then_type_in_the_file_name__8"));		 //$NON-NLS-1$
			
			treeViewer = new TreeViewer(parent, SWT.BORDER);
			ContainerContentProvider cp = new ContainerContentProvider();
			cp.showClosedProjects(false);
			GridData data = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL |
								  		  GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
			
			data.widthHint = SIZING_SELECTION_PANE_WIDTH;
			data.heightHint = SIZING_SELECTION_PANE_HEIGHT;
					
			treeViewer.getTree().setLayoutData(data);
			treeViewer.setContentProvider(cp);
			treeViewer.setLabelProvider(new WorkbenchLabelProvider());
			treeViewer.addSelectionChangedListener(
				new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						IStructuredSelection selection = (IStructuredSelection)event.getSelection();
						containerSelectionChanged((IContainer) selection.getFirstElement()); // allow null
						validatePage();
					}
				});
			
			treeViewer.addDoubleClickListener(
				new IDoubleClickListener() {
					public void doubleClick(DoubleClickEvent event) {
						ISelection selection = event.getSelection();
						if (selection instanceof IStructuredSelection) {
							Object item = ((IStructuredSelection)selection).getFirstElement();
							if (treeViewer.getExpandedState(item))
								treeViewer.collapseToLevel(item, 1);
							else
								treeViewer.expandToLevel(item, 1);
						}
					}
				});
		
			// This has to be done after the viewer has been laid out
			treeViewer.setInput(ResourcesPlugin.getWorkspace());
			
			// name group
			Composite nameGroup = new Composite(parent,SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginWidth = 0;
			nameGroup.setLayout(layout);
			data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
			nameGroup.setLayoutData(data);
		
			Label label = new Label(nameGroup,SWT.NONE);
			label.setText(Policy.bind("Fi&le_name__9")); //$NON-NLS-1$
		
			// resource name entry field
			workspaceFilename = new Text(nameGroup,SWT.BORDER);
			data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
			workspaceFilename.setLayoutData(data);
			workspaceFilename.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validatePage();
				}
			});
		}
	
		/**
		 * Enable and disable controls based on the selected radio button.
		 */
		protected void updateEnablements() {
			int type = getSaveType();

			browseButton.setEnabled(type==FILESYSTEM);
			filenameCombo.setEnabled(type==FILESYSTEM);
			treeViewer.getTree().setEnabled(type==WORKSPACE);
			workspaceFilename.setEnabled(type==WORKSPACE);
		}
		
		/**
		 * Answers the type of the patch file destination.
		 */		
		public int getSaveType() {
			if(saveInFilesystem.getSelection()) {
				return FILESYSTEM;
			} else if(saveToClipboard.getSelection()) {
				return CLIPBOARD;
			} else {
				return WORKSPACE;
			}
		}
		
		/**
		 * Remember the container selected in the tree viewer.
		 */
		public void containerSelectionChanged(IContainer container) {
			selectedContainer = container;
		}
	}
	// end of PatchFileSelectionPage
	
	/**
	 * Page to select the options for creating the patch.
	 */
	public class PatchFileCreationOptionsPage extends WizardPage {

		private Button recurseOption;
		private Button contextDiffOption;
		private Button unifiedDiffOption;
		private Button regularDiffOption;
		private Button includeNewFilesOptions;
		
		/**
		 * Constructor for PatchFileCreationOptionsPage.
		 */
		protected PatchFileCreationOptionsPage(String pageName) {
			super(pageName);
		}

		/**
		 * Constructor for PatchFileCreationOptionsPage.
		 */
		protected PatchFileCreationOptionsPage(String pageName, String title, ImageDescriptor titleImage) {
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
			WorkbenchHelp.setHelp(composite, IHelpContextIds.PATCH_OPTIONS_PAGE);
						
			recurseOption = new Button(composite, SWT.CHECK);
			recurseOption.setText(Policy.bind("Do_not_recurse_into_sub-folders_10")); //$NON-NLS-1$
			recurseOption.setSelection(true);
			
			includeNewFilesOptions = new Button(composite, SWT.CHECK);
			includeNewFilesOptions.setText(Policy.bind("Do_not_include_new_files_in_patch_11")); //$NON-NLS-1$
			includeNewFilesOptions.setSelection(true);
			
			Group diffTypeGroup = new Group(composite, SWT.NONE);
			layout = new GridLayout();
			layout.marginHeight = 0;
			diffTypeGroup.setLayout(layout);
			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
			diffTypeGroup.setLayoutData(data);
			diffTypeGroup.setText(Policy.bind("Diff_output_format_12")); //$NON-NLS-1$
			
			unifiedDiffOption = new Button(diffTypeGroup, SWT.RADIO);
			unifiedDiffOption.setText(Policy.bind("Unified_(format_required_by_Compare_With_Patch_feature)_13")); //$NON-NLS-1$
			unifiedDiffOption.setSelection(true);
			contextDiffOption = new Button(diffTypeGroup, SWT.RADIO);
			contextDiffOption.setText(Policy.bind("Context_14")); //$NON-NLS-1$
			regularDiffOption = new Button(diffTypeGroup, SWT.RADIO);
			regularDiffOption.setText(Policy.bind("Standard_15")); //$NON-NLS-1$
		}
		
		/**
		 * Answers if the difference operation should be run recursively.
		 */
		public boolean isRecursive() {
			return !recurseOption.getSelection();
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
				options.add(Diff.DO_NOT_RECURSE);
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
	// end of PatchFileCreationOptionsPage
	
	public GenerateDiffFileWizard(IStructuredSelection selection, IResource resource) {
		super();
		this.selection = selection;
		this.resource = resource;
		setWindowTitle(Policy.bind("GenerateCVSDiff.title")); //$NON-NLS-1$
		initializeDefaultPageImageDescriptor();
	}

	public void addPages() {
		String pageTitle = Policy.bind("GenerateCVSDiff.pageTitle"); //$NON-NLS-1$
		String pageDescription = Policy.bind("GenerateCVSDiff.pageDescription"); //$NON-NLS-1$
		mainPage = new PatchFileSelectionPage(pageTitle, pageTitle, CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_DIFF), selection);
		mainPage.setDescription(pageDescription);
		addPage(mainPage);
		
		pageTitle = Policy.bind("Advanced_options_19"); //$NON-NLS-1$
		pageDescription = Policy.bind("Configure_the_options_used_for_the_CVS_diff_command_20"); //$NON-NLS-1$
		optionsPage = new PatchFileCreationOptionsPage(pageTitle, pageTitle, CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_DIFF));
		optionsPage.setDescription(pageDescription);
		addPage(optionsPage);		
	}
		
	/**
	 * Initializes this creation wizard using the passed workbench and
	 * object selection.
	 *
	 * @param workbench the current workbench
	 * @param selection the current object selection
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	/**
	 * Declares the wizard banner iamge descriptor
	 */
	protected void initializeDefaultPageImageDescriptor() {
		String iconPath;
		iconPath = "icons/full/"; //$NON-NLS-1$
		try {
			URL installURL = CVSUIPlugin.getPlugin().getBundle().getEntry("/"); //$NON-NLS-1$
			URL url = new URL(installURL, iconPath + "wizards/newconnect_wiz.gif");	//$NON-NLS-1$
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
		String fs = mainPage.getFilesystemFile();
		IFile ws = mainPage.getWorkspaceFile();
		int type = mainPage.getSaveType();

		try {
			if(type != mainPage.CLIPBOARD) {
				File file = new File(fs!=null ? fs : ws.getLocation().toOSString());
				if (file.exists()) {
					// prompt then delete
					String title = Policy.bind("GenerateCVSDiff.overwriteTitle"); //$NON-NLS-1$
					String msg = Policy.bind("GenerateCVSDiff.overwriteMsg"); //$NON-NLS-1$
					final MessageDialog dialog = new MessageDialog(getShell(), title, null, msg, MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.CANCEL_LABEL }, 0);

					dialog.open();
	
					if (dialog.getReturnCode() != 0) {
						// cancel
						return false;
					}
				}
				getContainer().run(true, true, new GenerateDiffFileOperation(resource, file, false, optionsPage.getOptions(), getShell()));
				if(type==mainPage.WORKSPACE) {
					ws.getParent().refreshLocal(IResource.DEPTH_ONE, null);
				}
			} else {
				getContainer().run(true, true, new GenerateDiffFileOperation(resource, null, true, optionsPage.getOptions(), getShell()));
			}
			return true;
		} catch (InterruptedException e1) {
			return true;
		} catch(CoreException e) {
			CVSUIPlugin.openError(getShell(), Policy.bind("GenerateCVSDiff.error"), null, e); //$NON-NLS-1$
			return false;
		} catch (InvocationTargetException e2) {
			CVSUIPlugin.openError(getShell(), Policy.bind("GenerateCVSDiff.error"), null, e2); //$NON-NLS-1$
			return false;
		}
	}
}
