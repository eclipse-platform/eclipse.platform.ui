/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui;

 
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchHistory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.ide.IDEEncoding;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

import com.ibm.icu.text.MessageFormat;

/**
 * Launch configuration tab used to specify the location a launch configuration
 * is stored in, whether it should appear in the favorites list, and perspective
 * switching behavior for an associated launch.
 * <p>
 * Clients may call {@link #setHelpContextId(String)} on this tab prior to control
 * creation to alter the default context help associated with this tab. 
 * </p>
 * <p>
 * Clients may instantiate this class.
 * </p>
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CommonTab extends AbstractLaunchConfigurationTab {
	
	/**
	 * Constant representing the id of the {@link IDialogSettings} location for the {@link ContainerSelectionDialog} used
	 * on this tab
	 * 
	 * @since 3.6
	 */
	private final String SHARED_LAUNCH_CONFIGURATON_DIALOG = IDebugUIConstants.PLUGIN_ID + ".SHARED_LAUNCH_CONFIGURATON_DIALOG"; //$NON-NLS-1$
	private final String WORKSPACE_SELECTION_DIALOG = IDebugUIConstants.PLUGIN_ID + ".WORKSPACE_SELECTION_DIALOG"; //$NON-NLS-1$
	
	/**
	 * This attribute exists solely for the purpose of making sure that invalid shared locations
	 * can be revertible. This attribute is not saveable and will never appear in a saved
	 * launch configuration.
	 * @since 3.3
	 */
	private static final String BAD_CONTAINER = "bad_container_name"; //$NON-NLS-1$
	
	// Local/shared UI widgets
	private Button fLocalRadioButton;
	private Button fSharedRadioButton;
	private Text fSharedLocationText;
	private Button fSharedLocationButton;
	private Button fLaunchInBackgroundButton;
    private Button fDefaultEncodingButton;
    private Button fAltEncodingButton;
    private Combo fEncodingCombo;
    private Button fConsoleOutput;
    private Button fFileOutput;
    private Button fFileBrowse;
    private Text fFileText;
    private Button fVariables;
    private Button fAppend;
    private Button fWorkspaceBrowse;
	
	/**
	 * Check box list for specifying favorites
	 */
	private CheckboxTableViewer fFavoritesTable;
			
	/**
	 * Modify listener that simply updates the owning launch configuration dialog.
	 */
	private ModifyListener fBasicModifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent evt) {
			scheduleUpdateJob();
		}
	};
	
    /**
	 * Constructs a new tab with default context help.
	 */
	public CommonTab() {
		setHelpContextId(IDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_COMMON_TAB);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), getHelpContextId());
		comp.setLayout(new GridLayout(2, true));
		comp.setFont(parent.getFont());
		
		createSharedConfigComponent(comp);
		createFavoritesComponent(comp);
		createEncodingComponent(comp);
		createOutputCaptureComponent(comp);
		createLaunchInBackgroundComponent(comp);
	}
	
	/**
	 * Returns the {@link IDialogSettings} for the given id
	 * 
	 * @param id the id of the dialog settings to get
	 * @return the {@link IDialogSettings} to pass into the {@link ContainerSelectionDialog}
	 * @since 3.6
	 */
	IDialogSettings getDialogBoundsSettings(String id) {
		IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(id);
		if (section == null) {
			section = settings.addNewSection(id);
		} 
		return section;
	}
	
	/**
	 * Creates the favorites control
	 * @param parent the parent composite to add this one to
	 * @since 3.2
	 */
	private void createFavoritesComponent(Composite parent) {
		Group favComp = SWTFactory.createGroup(parent, LaunchConfigurationsMessages.CommonTab_Display_in_favorites_menu__10, 1, 1, GridData.FILL_BOTH);
		fFavoritesTable = CheckboxTableViewer.newCheckList(favComp, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		Control table = fFavoritesTable.getControl();
		GridData gd = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gd);
		table.setFont(parent.getFont());
		fFavoritesTable.setContentProvider(new FavoritesContentProvider());
		fFavoritesTable.setLabelProvider(new FavoritesLabelProvider());
		fFavoritesTable.addCheckStateListener(new ICheckStateListener() {
				public void checkStateChanged(CheckStateChangedEvent event) {
					updateLaunchConfigurationDialog();
				}
			});
	}
	
	/**
	 * Creates the shared config component
	 * @param parent the parent composite to add this component to
	 * @since 3.2
	 */
	private void createSharedConfigComponent(Composite parent) {
		Group group = SWTFactory.createGroup(parent, LaunchConfigurationsMessages.CommonTab_0, 3, 2, GridData.FILL_HORIZONTAL);
		Composite comp = SWTFactory.createComposite(group, parent.getFont(), 3, 3, GridData.FILL_BOTH, 0, 0);
		fLocalRadioButton = createRadioButton(comp, LaunchConfigurationsMessages.CommonTab_L_ocal_3);
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		fLocalRadioButton.setLayoutData(gd);
		fSharedRadioButton = createRadioButton(comp, LaunchConfigurationsMessages.CommonTab_S_hared_4);
		fSharedRadioButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleSharedRadioButtonSelected();
			}
		});
		fSharedLocationText = SWTFactory.createSingleText(comp, 1);
		fSharedLocationText.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				e.result =  LaunchConfigurationsMessages.CommonTab_S_hared_4;
			}
		});
		fSharedLocationText.addModifyListener(fBasicModifyListener);
		fSharedLocationButton = createPushButton(comp, LaunchConfigurationsMessages.CommonTab__Browse_6, null);	 
		fSharedLocationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleSharedLocationButtonSelected();
			}
		});	

		fLocalRadioButton.setSelection(true);
		setSharedEnabled(false);	
	}
	
    /**
     * Creates the component set for the capture output composite
     * @param parent the parent to add this component to
     */
    private void createOutputCaptureComponent(Composite parent) {
        Group group = SWTFactory.createGroup(parent, LaunchConfigurationsMessages.CommonTab_4, 5, 2, GridData.FILL_HORIZONTAL);
        Composite comp = SWTFactory.createComposite(group, 5, 5, GridData.FILL_BOTH);
        GridLayout ld = (GridLayout)comp.getLayout();
        ld.marginWidth = 1;
        ld.marginHeight = 1;
        fConsoleOutput = createCheckButton(comp, LaunchConfigurationsMessages.CommonTab_5); 
        GridData gd = new GridData(SWT.BEGINNING, SWT.NORMAL, true, false);
        gd.horizontalSpan = 5;
        fConsoleOutput.setLayoutData(gd);
        fConsoleOutput.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        
        fFileOutput = createCheckButton(comp, LaunchConfigurationsMessages.CommonTab_6); 
        fFileOutput.setLayoutData(new GridData(SWT.BEGINNING, SWT.NORMAL, false, false));
        fFileOutput.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                enableOuputCaptureWidgets(fFileOutput.getSelection());
                updateLaunchConfigurationDialog();
            }
        });
        fFileText = SWTFactory.createSingleText(comp, 4);
        fFileText.getAccessible().addAccessibleListener(new AccessibleAdapter() {
        	public void getName(AccessibleEvent e) {
        		e.result = LaunchConfigurationsMessages.CommonTab_6;
        	}
        });
        fFileText.addModifyListener(fBasicModifyListener);
        
        Composite bcomp = SWTFactory.createComposite(comp, 3, 5, GridData.HORIZONTAL_ALIGN_END);
        ld = (GridLayout)bcomp.getLayout();
        ld.marginHeight = 1;
        ld.marginWidth = 0;
        fWorkspaceBrowse = createPushButton(bcomp, LaunchConfigurationsMessages.CommonTab_12, null); 
        fWorkspaceBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider());
                dialog.setTitle(LaunchConfigurationsMessages.CommonTab_13); 
                dialog.setMessage(LaunchConfigurationsMessages.CommonTab_14); 
                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot()); 
                dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
                dialog.setDialogBoundsSettings(getDialogBoundsSettings(WORKSPACE_SELECTION_DIALOG), Dialog.DIALOG_PERSISTSIZE);
                if (dialog.open() == IDialogConstants.OK_ID) {
                    IResource resource = (IResource) dialog.getFirstResult();
                    if(resource != null) {
                    	String arg = resource.getFullPath().toString();
                    	String fileLoc = VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression("workspace_loc", arg); //$NON-NLS-1$
                    	fFileText.setText(fileLoc);
                    }
                }
            }
        });
        fFileBrowse = createPushButton(bcomp, LaunchConfigurationsMessages.CommonTab_7, null);
        fFileBrowse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                String filePath = fFileText.getText();
                FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
                filePath = dialog.open();
                if (filePath != null) {
                    fFileText.setText(filePath);
                }
            }
        });
        fVariables = createPushButton(bcomp, LaunchConfigurationsMessages.CommonTab_9, null); 
        fVariables.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
				dialog.open();
				String variable = dialog.getVariableExpression();
				if (variable != null) {
					fFileText.insert(variable);
				}
            }
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        fAppend = createCheckButton(comp, LaunchConfigurationsMessages.CommonTab_11); 
        gd = new GridData(SWT.LEFT, SWT.TOP, true, false);
        gd.horizontalSpan = 4;
        fAppend.setLayoutData(gd);
		fAppend.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(SelectionEvent e) {
		        updateLaunchConfigurationDialog();
		    }
		});   
    }

    /**
     * Enables or disables the output capture widgets based on the the specified enablement
     * @param enable if the output capture widgets should be enabled or not
     * @since 3.2
     */
    private void enableOuputCaptureWidgets(boolean enable) {
    	fFileText.setEnabled(enable);
        fFileBrowse.setEnabled(enable);
        fWorkspaceBrowse.setEnabled(enable);
        fVariables.setEnabled(enable);
        fAppend.setEnabled(enable);
    }
    
    /**
     * Returns the default encoding for the specified config
     * @param config the configuration to get the encoding for
     * @return the default encoding
     * 
     * @since 3.4
     */
    private String getDefaultEncoding(ILaunchConfiguration config) {
    	try {
	    	IResource[] resources = config.getMappedResources();
			if(resources != null && resources.length > 0) {
				IResource res = resources[0];
				if(res instanceof IFile) {
					return ((IFile)res).getCharset();
				}
				else if(res instanceof IContainer) { 
					return ((IContainer)res).getDefaultCharset();
				}
			}
    	}
    	catch(CoreException ce) {
    		DebugUIPlugin.log(ce);
    	}
    	return ResourcesPlugin.getEncoding();
    }
    
    /**
     * Creates the encoding component
     * @param parent the parent to add this composite to
     */
    private void createEncodingComponent(Composite parent) {
	    Group group = SWTFactory.createGroup(parent, LaunchConfigurationsMessages.CommonTab_1, 2, 1, GridData.FILL_BOTH);
	
	    fDefaultEncodingButton = createRadioButton(group, IInternalDebugCoreConstants.EMPTY_STRING); 
	    GridData gd = new GridData(SWT.BEGINNING, SWT.NORMAL, true, false);
	    gd.horizontalSpan = 2;
	    fDefaultEncodingButton.setLayoutData(gd);
	   
	    fAltEncodingButton = createRadioButton(group, LaunchConfigurationsMessages.CommonTab_3);  
	    fAltEncodingButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
	    
	    fEncodingCombo = new Combo(group, SWT.NONE);
	    fEncodingCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    fEncodingCombo.setFont(parent.getFont());
	    List allEncodings = IDEEncoding.getIDEEncodings();
        String[] encodingArray = (String[]) allEncodings.toArray(new String[0]);
	    fEncodingCombo.setItems(encodingArray);
        if (encodingArray.length > 0) {
            fEncodingCombo.select(0); 
        }
        fEncodingCombo.getAccessible().addAccessibleListener(new AccessibleAdapter() {
        	public void getName(AccessibleEvent e) {
        		e.result = LaunchConfigurationsMessages.CommonTab_3;
        	}
        });
	    SelectionListener listener = new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	        	if(e.getSource() instanceof Button) {
	        		Button button = (Button)e.getSource();
	        		if(button.getSelection()) {
		        		updateLaunchConfigurationDialog();
			            fEncodingCombo.setEnabled(fAltEncodingButton.getSelection() == true);
	        		}
	        	}
	        	else {
	        		updateLaunchConfigurationDialog();
	        	}
	        }
	    };
	    fAltEncodingButton.addSelectionListener(listener);
	    fDefaultEncodingButton.addSelectionListener(listener);
	    fEncodingCombo.addSelectionListener(listener);
	    fEncodingCombo.addKeyListener(new KeyAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyReleased(KeyEvent e) {
				scheduleUpdateJob();
			}
		});
	}
    
	/**
	 * Returns whether or not the given encoding is valid.
	 * 
	 * @param enc
	 *            the encoding to validate
	 * @return <code>true</code> if the encoding is valid, <code>false</code>
	 *         otherwise
	 */
	private boolean isValidEncoding(String enc) {
		try {
			return Charset.isSupported(enc);
		} catch (IllegalCharsetNameException e) {
			// This is a valid exception
			return false;
		}
	}

	/**
	 * Creates the controls needed to edit the launch in background
	 * attribute of an external tool
	 *
	 * @param parent the composite to create the controls in
	 */
	protected void createLaunchInBackgroundComponent(Composite parent) {
		fLaunchInBackgroundButton = createCheckButton(parent, LaunchConfigurationsMessages.CommonTab_10); 
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		fLaunchInBackgroundButton.setLayoutData(data);
		fLaunchInBackgroundButton.setFont(parent.getFont());
		fLaunchInBackgroundButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
	}

	/**
	 * handles the shared radio button being selected
	 */
	private void handleSharedRadioButtonSelected() {
		setSharedEnabled(isShared());
		updateLaunchConfigurationDialog();
	}
	
	/**
	 * Sets the widgets for specifying that a launch configuration is to be shared to the enable value
	 * @param enable the enabled value for 
	 */
	private void setSharedEnabled(boolean enable) {
		fSharedLocationText.setEnabled(enable);
		fSharedLocationButton.setEnabled(enable);
	}
	
	private String getDefaultSharedConfigLocation(ILaunchConfiguration config) {
		String path = IInternalDebugCoreConstants.EMPTY_STRING;
		try {
			IResource[] res = config.getMappedResources();
			if(res != null) {
				IProject  proj;
				for (int i = 0; i < res.length; i++) {
					proj = res[i].getProject();
					if(proj != null && proj.isAccessible()) {
						return proj.getFullPath().toOSString();
					}
				}
			}
		} 
		catch (CoreException e) {DebugUIPlugin.log(e);}
		return path;
	}
	
	/**
	 * if the shared radio button is selected, indicating that the launch configuration is to be shared
	 * @return true if the radio button is selected, false otherwise
	 */
	private boolean isShared() {
		return fSharedRadioButton.getSelection();
	}
	
	/**
	 * Handles the shared location button being selected
	 */
	private void handleSharedLocationButtonSelected() { 
		String currentContainerString = fSharedLocationText.getText();
		IContainer currentContainer = getContainer(currentContainerString);
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(),
				   currentContainer,
				   false,
				   LaunchConfigurationsMessages.CommonTab_Select_a_location_for_the_launch_configuration_13);
		dialog.showClosedProjects(false);
		dialog.setDialogBoundsSettings(getDialogBoundsSettings(SHARED_LAUNCH_CONFIGURATON_DIALOG), Dialog.DIALOG_PERSISTSIZE);
		dialog.open();
		Object[] results = dialog.getResult();	
		if ((results != null) && (results.length > 0) && (results[0] instanceof IPath)) {
			IPath path = (IPath)results[0];
			String containerName = path.toOSString();
			fSharedLocationText.setText(containerName);
		}		
	}
	
	/**
	 * gets the container form the specified path
	 * @param path the path to get the container from
	 * @return the container for the specified path or null if one cannot be determined
	 */
	private IContainer getContainer(String path) {
		Path containerPath = new Path(path);
		return (IContainer) getWorkspaceRoot().findMember(containerPath);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		boolean isShared = !configuration.isLocal();
		fSharedRadioButton.setSelection(isShared);
		fLocalRadioButton.setSelection(!isShared);
		setSharedEnabled(isShared);
		fSharedLocationText.setText(getDefaultSharedConfigLocation(configuration));
		if(isShared) {
			String containerName = IInternalDebugCoreConstants.EMPTY_STRING;
			IFile file = configuration.getFile();
			if (file != null) {
				IContainer parent = file.getParent();
				if (parent != null) {
					containerName = parent.getFullPath().toOSString();
				}
			}
			fSharedLocationText.setText(containerName);
		}
		updateFavoritesFromConfig(configuration);
		updateLaunchInBackground(configuration);
		updateEncoding(configuration);
		updateConsoleOutput(configuration);
	}
	
    /**
     * Updates the console output form the local configuration
     * @param configuration the local configuration
     */
    private void updateConsoleOutput(ILaunchConfiguration configuration) {
        boolean outputToConsole = true;
        String outputFile = null;
        boolean append = false;
        
        try {
            outputToConsole = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
            outputFile = configuration.getAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, (String)null);
            append = configuration.getAttribute(IDebugUIConstants.ATTR_APPEND_TO_FILE, false);
        } catch (CoreException e) {
        }
        
        fConsoleOutput.setSelection(outputToConsole);
        fAppend.setSelection(append);
        boolean haveOutputFile= outputFile != null;
        if (haveOutputFile) {
            fFileText.setText(outputFile);
        }
        fFileOutput.setSelection(haveOutputFile);
        enableOuputCaptureWidgets(haveOutputFile);
    }

    /**
     * Updates the launch on background check button
     * @param configuration the local launch configuration
     */
    protected void updateLaunchInBackground(ILaunchConfiguration configuration) { 
		fLaunchInBackgroundButton.setSelection(isLaunchInBackground(configuration));
	}
	
	/**
	 * Updates the encoding
	 * @param configuration the local configuration
	 */
	private void updateEncoding(ILaunchConfiguration configuration) {
	    String encoding = null;
	    try {
	        encoding = configuration.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, (String)null);
        } catch (CoreException e) {
        }
	    String defaultEncoding = getDefaultEncoding(configuration);
	    fDefaultEncodingButton.setText(MessageFormat.format(LaunchConfigurationsMessages.CommonTab_2, new String[]{defaultEncoding}));
	    fDefaultEncodingButton.pack();
        if (encoding != null) {
            fAltEncodingButton.setSelection(true);
            fDefaultEncodingButton.setSelection(false);
            fEncodingCombo.setText(encoding);
            fEncodingCombo.setEnabled(true);
        } else {
            fDefaultEncodingButton.setSelection(true);
            fAltEncodingButton.setSelection(false);
            fEncodingCombo.setEnabled(false);
        }
	}
	
	/**
	 * Returns whether the given configuration should be launched in the background.
	 * 
	 * @param configuration the configuration
	 * @return whether the configuration is configured to launch in the background
	 */
	public static boolean isLaunchInBackground(ILaunchConfiguration configuration) {
		boolean launchInBackground= true;
		try {
			launchInBackground= configuration.getAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
		} catch (CoreException ce) {
			DebugUIPlugin.log(ce);
		}
		return launchInBackground;
	}

	/**
	 * Updates the favorites selections from the local configuration
	 * @param config the local configuration
	 */
	private void updateFavoritesFromConfig(ILaunchConfiguration config) {
		fFavoritesTable.setInput(config);
		fFavoritesTable.setCheckedElements(new Object[]{});
		try {
			List groups = config.getAttribute(IDebugUIConstants.ATTR_FAVORITE_GROUPS, new ArrayList());
			if (groups.isEmpty()) {
				// check old attributes for backwards compatible
				if (config.getAttribute(IDebugUIConstants.ATTR_DEBUG_FAVORITE, false)) {
					groups.add(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
				}
				if (config.getAttribute(IDebugUIConstants.ATTR_RUN_FAVORITE, false)) {
					groups.add(IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
				}
			}
			if (!groups.isEmpty()) {
				List list = new ArrayList();
				Iterator iterator = groups.iterator();
				while (iterator.hasNext()) {
					String id = (String)iterator.next();
					LaunchGroupExtension extension = getLaunchConfigurationManager().getLaunchGroup(id);
					if (extension != null) {
						list.add(extension);
					}
				}
				fFavoritesTable.setCheckedElements(list.toArray());
			}
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}
	}

	/**
	 * Updates the configuration form the local shared config working copy
	 * @param config the local shared config working copy
	 */
	private void updateConfigFromLocalShared(ILaunchConfigurationWorkingCopy config) {
		if (isShared()) {
			String containerPathString = fSharedLocationText.getText();
			IContainer container = getContainer(containerPathString);
			if(container == null) {
				//we need to force an attribute to allow the invalid container path to be revertable
				config.setAttribute(BAD_CONTAINER, containerPathString);
			}
			else {
				config.setContainer(container);
			}
		} else {
			config.setContainer(null);
		}
	}
	
	/**
	 * Convenience accessor
	 * @return the singleton {@link LaunchConfigurationManager}
	 */
	protected LaunchConfigurationManager getLaunchConfigurationManager() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager();
	}
	
	/**
	 * Update the favorite settings.
	 * 
	 * NOTE: set to <code>null</code> instead of <code>false</code> for backwards compatibility
	 *  when comparing if content is equal, since 'false' is default
	 * 	and will be missing for older configurations.
	 * @param config the configuration to update
	 */
	private void updateConfigFromFavorites(ILaunchConfigurationWorkingCopy config) {
		try {
			Object[] checked = fFavoritesTable.getCheckedElements();
			boolean debug = config.getAttribute(IDebugUIConstants.ATTR_DEBUG_FAVORITE, false);
			boolean run = config.getAttribute(IDebugUIConstants.ATTR_RUN_FAVORITE, false);
			if (debug || run) {
				// old attributes
				List groups = new ArrayList();
				int num = 0;
				if (debug) {
					groups.add(getLaunchConfigurationManager().getLaunchGroup(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP));
					num++;
				}
				if (run) {
					num++;
					groups.add(getLaunchConfigurationManager().getLaunchGroup(IDebugUIConstants.ID_RUN_LAUNCH_GROUP));
				}
				// see if there are any changes
				if (num == checked.length) {
					boolean different = false;
					for (int i = 0; i < checked.length; i++) {
						if (!groups.contains(checked[i])) {
							different = true;
							break;
						}
					}
					if (!different) {
						return;
					}
				}
			} 
			config.setAttribute(IDebugUIConstants.ATTR_DEBUG_FAVORITE, (String)null);
			config.setAttribute(IDebugUIConstants.ATTR_RUN_FAVORITE, (String)null);
			List groups = null;
			for (int i = 0; i < checked.length; i++) {
				LaunchGroupExtension group = (LaunchGroupExtension)checked[i];
				if (groups == null) {
					groups = new ArrayList();
				}
				groups.add(group.getIdentifier());
			}
			config.setAttribute(IDebugUIConstants.ATTR_FAVORITE_GROUPS, groups);
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}		
	}	
	
	/**
	 * Convenience method for getting the workspace root.
	 * @return the singleton {@link IWorkspaceRoot}
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		setMessage(null);
		setErrorMessage(null);
		
		return validateLocalShared() && validateRedirectFile() && validateEncoding();
	}
	
    /**
     * validates the encoding selection
     * @return true if the validate encoding is allowable, false otherwise
     */
    private boolean validateEncoding() {
        if (fAltEncodingButton.getSelection()) {
            if (fEncodingCombo.getSelectionIndex() == -1) {
            	if (!isValidEncoding(fEncodingCombo.getText().trim())) {
                	setErrorMessage(LaunchConfigurationsMessages.CommonTab_15);
                	return false;
                }
            }
        }
        return true;
    }

    /**
     * Validates if the redirect file is valid
     * @return true if the filename is not zero, false otherwise
     */
    private boolean validateRedirectFile() {
        if(fFileOutput.getSelection()) {
            int len = fFileText.getText().trim().length();
            if (len == 0) {
                setErrorMessage(LaunchConfigurationsMessages.CommonTab_8); 
                return false;
            }
        }
        return true;
    }

    /**
     * validates the local shared config file location
     * @return true if the local shared file exists, false otherwise
     */
    private boolean validateLocalShared() {
		if (isShared()) {
			String path = fSharedLocationText.getText().trim();
			IContainer container = getContainer(path);
			if (container == null || container.equals(ResourcesPlugin.getWorkspace().getRoot())) {
				setErrorMessage(LaunchConfigurationsMessages.CommonTab_Invalid_shared_configuration_location_14); 
				return false;
			} else if (!container.getProject().isOpen()) {
				setErrorMessage(LaunchConfigurationsMessages.CommonTab_Cannot_save_launch_configuration_in_a_closed_project__1); 
				return false;				
			}
		}
		return true;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setContainer(null);
		setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, config, true, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		updateConfigFromLocalShared(configuration);
		updateConfigFromFavorites(configuration);
		setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, configuration, fLaunchInBackgroundButton.getSelection(), true);
		String encoding = null;
		if(fAltEncodingButton.getSelection()) {
		    encoding = fEncodingCombo.getText().trim();
		}
		configuration.setAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, encoding);
		boolean captureOutput = false;
		if (fConsoleOutput.getSelection()) {
		    captureOutput = true;
		    configuration.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, (String)null);
		} else {
		    configuration.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, false);
		}
		if (fFileOutput.getSelection()) {
		    captureOutput = true;
		    String file = fFileText.getText();
		    configuration.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, file);
		    if(fAppend.getSelection()) {
		        configuration.setAttribute(IDebugUIConstants.ATTR_APPEND_TO_FILE, true);
		    } else {
		        configuration.setAttribute(IDebugUIConstants.ATTR_APPEND_TO_FILE, (String)null);
		    }
		} else {
		    configuration.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, (String)null);
		}
		
		if (!captureOutput) {
		    configuration.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, false);
		} else {
		    configuration.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, (String)null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LaunchConfigurationsMessages.CommonTab__Common_15; 
	}
	
	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 * 
	 * @since 3.3
	 */
	public String getId() {
		return "org.eclipse.debug.ui.commonTab"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#canSave()
	 */
	public boolean canSave() {
		return validateLocalShared();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return DebugUITools.getImage(IInternalDebugUIConstants.IMG_OBJS_COMMON_TAB);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {}

	/**
	 * Content provider for the favorites table
	 */
	class FavoritesContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			ILaunchGroup[] groups = DebugUITools.getLaunchGroups();
			List possibleGroups = new ArrayList();
			ILaunchConfiguration configuration = (ILaunchConfiguration)inputElement;
			for (int i = 0; i < groups.length; i++) {
				ILaunchGroup extension = groups[i];
				LaunchHistory history = getLaunchConfigurationManager().getLaunchHistory(extension.getIdentifier());
				if (history != null && history.accepts(configuration)) {
					possibleGroups.add(extension);
				} 
			}
			return possibleGroups.toArray();
		}

		public void dispose() {}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

	}
	
	/**
	 * Provides the labels for the favorites table
	 *
	 */
	class FavoritesLabelProvider implements ITableLabelProvider {
		
		private Map fImages = new HashMap();

		public Image getColumnImage(Object element, int columnIndex) {
			Image image = (Image)fImages.get(element);
			if (image == null) {
				ImageDescriptor descriptor = ((LaunchGroupExtension)element).getImageDescriptor();
				if (descriptor != null) {
					image = descriptor.createImage();
					fImages.put(element, image);
				}
			}
			return image;
		}

		public String getColumnText(Object element, int columnIndex) {
			String label = ((LaunchGroupExtension)element).getLabel();
			return DebugUIPlugin.removeAccelerators(label);
		}

		public void addListener(ILabelProviderListener listener) {}

		public void dispose() {
			Iterator images = fImages.values().iterator();
			while (images.hasNext()) {
				Image image = (Image)images.next();
				image.dispose();
			}
		}

		public boolean isLabelProperty(Object element, String property) {return false;}

		public void removeListener(ILabelProviderListener listener) {}		
	}

}
