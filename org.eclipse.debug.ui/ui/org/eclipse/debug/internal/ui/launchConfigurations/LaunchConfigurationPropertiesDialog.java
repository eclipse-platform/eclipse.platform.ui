package org.eclipse.debug.internal.ui.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.PixelConverter;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;
 

/**
 * A dialog used to edit a single launch configuration.
 */
public class LaunchConfigurationPropertiesDialog extends TitleAreaDialog implements ILaunchConfigurationDialog {
	
	/**
	 * The lanuch configuration to display
	 */
	private ILaunchConfiguration fLaunchConfiguration;
			
	/**
	 * Tab edit area
	 */
	private LaunchConfigurationTabGroupViewer fTabViewer;
	
	/**
	 * True while setting the input to the tab viewer
	 */
	private boolean fInitializingTabs;
	
	/**
	 * The launch configuration edit area.
	 */
	private Composite fEditArea;
		
	/**
	 * The launch groupd being displayed
	 */
	private LaunchGroupExtension fGroup;
	
	/**
	 * Banner image
	 */
	private Image fBannerImage;
					
	/**
	 * Constant specifying how wide this dialog is allowed to get (as a percentage of
	 * total available screen width) as a result of tab labels in the edit area.
	 */
	private static final float MAX_DIALOG_WIDTH_PERCENT = 0.75f;

	/**
	 * Empty array
	 */
	protected static final Object[] EMPTY_ARRAY = new Object[0];	
	
	/**
	 * Size of this dialog if there is no preference specifying a size.
	 */
	protected static final Point DEFAULT_INITIAL_DIALOG_SIZE = new Point(620, 560);

	/**
	 * Status area messages
	 */
	protected static final String LAUNCH_STATUS_OK_MESSAGE = LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Ready_to_launch_2"); //$NON-NLS-1$
	protected static final String LAUNCH_STATUS_STARTING_FROM_SCRATCH_MESSAGE 
										= LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Select_a_configuration_to_launch_or_a_config_type_to_create_a_new_configuration_3"); //$NON-NLS-1$
	
	/**
	 * Constructs a new launch configuration dialog on the given
	 * parent shell.
	 * 
	 * @param shell the parent shell
	 * @param selection the selection used to initialize this dialog, typically the 
	 *  current workbench selection
	 * @param group lanuch group
	 */
	public LaunchConfigurationPropertiesDialog(Shell shell, ILaunchConfiguration launchConfiguration, LaunchGroupExtension group) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setLaunchGroup(group);
		setLaunchConfiguration(launchConfiguration);
	}
	
	/**
	 * Sets the launch configration to be displayed.
	 * 
	 * @param configuration
	 */
	private void setLaunchConfiguration(ILaunchConfiguration configuration) {
		fLaunchConfiguration = configuration;
	}
	
	/**
	 * Returns the launch configuration being displayed.
	 * 
	 * @return ILaunchConfiguration
	 */
	protected ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}

	/**
	 * @see Dialog#createContents(Composite)
	 */
	protected Control createContents(Composite parent) { 
		Control contents = super.createContents(parent);
		initializeBounds();
		fTabViewer.setInput(getLaunchConfiguration());
		resize();
		return contents;
	}
		
	/**
	 * Write out this dialog's Shell size, location & sash weights to the preference store.
	 */
	private void persistShellGeometry() {
		// TODO:
//		Point shellLocation = getShell().getLocation();
//		Point shellSize = getShell().getSize();
//		int[] sashWeights = getSashForm().getWeights();
//		String locationString = serializeCoords(shellLocation);
//		String sizeString = serializeCoords(shellSize);
//		String sashWeightString = serializeCoords(new Point(sashWeights[0], sashWeights[1]));
//		getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_LAUNCH_CONFIGURATION_DIALOG_LOCATION, locationString);
//		getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_LAUNCH_CONFIGURATION_DIALOG_SIZE, sizeString);
//		getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_LAUNCH_CONFIGURATION_DIALOG_SASH_WEIGHTS, sashWeightString);
	}
		
	/**
	 * @see Window#close()
	 */
	public boolean close() {
		persistShellGeometry();
		getBannerImage().dispose();
		fTabViewer.dispose();
		return super.close();
	}
		
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		GridData gd;
		Composite dialogComp = (Composite)super.createDialogArea(parent);
		Composite topComp = new Composite(dialogComp, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		topComp.setLayoutData(gd);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 1;
		topLayout.marginHeight = 5;
		topLayout.marginWidth = 0;
		topComp.setLayout(topLayout);
	
		// Set the things that TitleAreaDialog takes care of 
		setTitle(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Create,_manage,_and_run_launch_configurations_8")); //$NON-NLS-1$
		setMessage(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Ready_to_launch_2")); //$NON-NLS-1$
		setModeLabelState();
	
		// Build the launch configuration edit area and put it into the composite.
		Composite editAreaComp = createLaunchConfigurationEditArea(topComp);
		setEditArea(editAreaComp);
		gd = new GridData(GridData.FILL_BOTH);
		editAreaComp.setLayoutData(gd);
			
		// Build the separator line that demarcates the button bar
		Label separator = new Label(topComp, SWT.HORIZONTAL | SWT.SEPARATOR);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		separator.setLayoutData(gd);
		
		dialogComp.layout(true);
		
		return dialogComp;
	}
	
	/**
	 * Set the title area image based on the mode this dialog was initialized with
	 */
	protected void setModeLabelState() {
		setTitleImage(getBannerImage());
	}
	
	/**
	 * Update buttons and message.
	 */
	protected void refreshStatus() {
		updateMessage();
		updateButtons();
	}
			
	private Display getDisplay() {
		Shell shell = getShell();
		if (shell != null) {
			return shell.getDisplay();
		} else {
			return Display.getDefault();
		}
	}		
	
	/**
	 * Creates the launch configuration edit area of the dialog.
	 * This area displays the name of the launch configuration
	 * currently being edited, as well as a tab folder of tabs
	 * that are applicable to the launch configuration.
	 * 
	 * @return the composite used for launch configuration editing
	 */ 
	private Composite createLaunchConfigurationEditArea(Composite parent) {
		fTabViewer = new LaunchConfigurationTabGroupViewer(parent, this);
		fTabViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			/**
			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
			 */
			public void selectionChanged(SelectionChangedEvent event) {
				handleTabSelectionChanged(event);
			}
		});
		return (Composite)fTabViewer.getControl();
	}	
		
	/**
	 * Sets the title for the dialog, and establishes the help context.
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(Shell);
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		String title = MessageFormat.format("Properties for {0}", new String[]{getLaunchConfiguration().getName()});
		if (title == null) {
			title = LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Launch_Configurations_18"); //$NON-NLS-1$
		}
		shell.setText(title);
		WorkbenchHelp.setHelp(
			shell,
			IDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG);
	}
	
	/**
	 * @see Window#getInitialLocation(Point)
	 */
	protected Point getInitialLocation(Point initialSize) {	
		String locationString = getPreferenceStore().getString(IDebugPreferenceConstants.PREF_LAUNCH_CONFIGURATION_DIALOG_LOCATION);
		if (locationString.length() > 0) {
			Point locationPoint = parseCoordinates(locationString);
			if (locationPoint != null) {
				return locationPoint;
			}
		}
		return super.getInitialLocation(initialSize);
	}

	/**
	 * @see Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		String sizeString = getPreferenceStore().getString(IDebugPreferenceConstants.PREF_LAUNCH_CONFIGURATION_DIALOG_SIZE);
		if (sizeString.length() > 0) {
			Point sizePoint = parseCoordinates(sizeString);
			if (sizePoint != null) {
				return sizePoint;
			}
		}
		return DEFAULT_INITIAL_DIALOG_SIZE;
	}
	
	/**
	 * Given a coordinate String of the form "123x456" return a Point object whose
	 * X value is 123 and Y value is 456.  Return <code>null</code> if the String
	 * is not in the specified form.
	 */
	private Point parseCoordinates(String coordString) {
		int byIndex = coordString.indexOf('x');
		if (byIndex < 0) {
			return null;
		}
		
		try {
			int x = Integer.parseInt(coordString.substring(0, byIndex));
			int y = Integer.parseInt(coordString.substring(byIndex + 1));			
			return new Point(x, y);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}
	
	/**
	 * Given a Point object, return a String of the form "XCoordxYCoord".
	 */
	private String serializeCoords(Point coords) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(coords.x);
		buffer.append('x');
		buffer.append(coords.y);
		return buffer.toString();
	}
	
	/**
	 * Returns the launch manager.
	 * 
	 * @return the launch manager
	 */
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Returns whether this dialog is currently open
	 */
	private boolean isVisible() {
		return fEditArea != null;
	}	
		  	
  	protected boolean isEqual(Object o1, Object o2) {
  		if (o1 == o2) {
  			return true;
  		} else if (o1 == null) {
  			return false;
  		} else {
  			return o1.equals(o2);
  		}
  	}
  	
  	
  	protected void resize() {
  		// determine the maximum tab dimensions
  		PixelConverter pixelConverter = new PixelConverter(getEditArea());
  		int runningTabWidth = 0;
  		ILaunchConfigurationTabGroup group = getTabGroup();
  		if (group == null) {
  			return;
  		}
  		ILaunchConfigurationTab[] tabs = group.getTabs();
  		Point contentSize = new Point(0, 0);
  		for (int i = 0; i < tabs.length; i++) {
  			String name = tabs[i].getName();
  			Image image = tabs[i].getImage();
  			runningTabWidth += pixelConverter.convertWidthInCharsToPixels(name.length() + 5);
  			if (image != null) {
  				runningTabWidth += image.getBounds().width;
  			}
  			Control control = tabs[i].getControl();
  			if (control != null) {
  				Point size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
  				if (size.x > contentSize.x) {
  					contentSize.x = size.x;
  				}
  				if (size.y > contentSize.y) {
  					contentSize.y = size.y;
  				}
  			}
  		}

  		// Determine if more space is needed to show all tab labels across the top of the
  		// tab folder.  If so, only increase size of dialog to some percent of the available
  		// screen real estate.
  		if (runningTabWidth > contentSize.x) {
  			int maxAllowedWidth = (int) (getDisplay().getBounds().width * MAX_DIALOG_WIDTH_PERCENT);
  			if (runningTabWidth > maxAllowedWidth) {
  				contentSize.x = maxAllowedWidth;
  			} else {
  				contentSize.x = runningTabWidth;
  			}
  		}

  		// Adjust the maximum tab dimensions to account for the extra space required for the tab labels
  		Rectangle tabFolderBoundingBox = getEditArea().computeTrim(0, 0, contentSize.x, contentSize.y);
  		contentSize.x = tabFolderBoundingBox.width;
  		contentSize.y = tabFolderBoundingBox.height;

  		// Force recalculation of sizes
  		getEditArea().layout(true);

  		// Calculate difference between required space for tab folder and current size,
  		// then increase size of this dialog's Shell by that amount
  		Rectangle rect = getEditArea().getClientArea();
  		Point containerSize= new Point(rect.width, rect.height);
  		int hdiff= contentSize.x - containerSize.x;
  		int vdiff= contentSize.y - containerSize.y;
  		// Only increase size of dialog, never shrink it
  		if (hdiff > 0 || vdiff > 0) {
  			hdiff= Math.max(0, hdiff);
  			vdiff= Math.max(0, vdiff);
  			Shell shell= getShell();
  			Point shellSize= shell.getSize();
  			setShellSize(shellSize.x + hdiff, shellSize.y + vdiff);
  		}  		
  	}
  	
	/**
	 * Notification that tab selection has changed.
	 *
	 * @param event selection changed event
	 */
	protected void handleTabSelectionChanged(SelectionChangedEvent event) {
		refreshStatus();
	}
	 	
 	private void setInitializingTabs(boolean init) {
 		fInitializingTabs = init;
 	}
 	
 	private boolean isInitializingTabs() {
 		return fInitializingTabs;
 	}
 	
 	/**
 	 * Increase the size of this dialog's <code>Shell</code> by the specified amounts.
 	 * Do not increase the size of the Shell beyond the bounds of the Display.
 	 */
	private void setShellSize(int width, int height) {
		Rectangle bounds = getShell().getDisplay().getBounds();
		getShell().setSize(Math.min(width, bounds.width), Math.min(height, bounds.height));
	}
 	 
 	/** 
 	 * @see ILaunchConfigurationDialog#getMode()
 	 */
 	public String getMode() {
 		return getLaunchGroup().getMode();
 	}
 	 	 	
 	/**
 	 * Returns the current tab group
 	 * 
 	 * @return the current tab group, or <code>null</code> if none
 	 */
 	public ILaunchConfigurationTabGroup getTabGroup() {
 		return fTabViewer.getTabGroup();
 	}
 	
 	/**
 	 * @see ILaunchConfigurationDialog#getTabs()
 	 */
 	public ILaunchConfigurationTab[] getTabs() {
 		if (getTabGroup() == null) {
 			return null;
 		} else {
 			return getTabGroup().getTabs();
 		}
 	} 	
 				
	private IPreferenceStore getPreferenceStore() {
		return DebugUIPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * @see ILaunchConfigurationDialog#updateButtons()
	 */
	public void updateButtons() {
		if (isInitializingTabs()) {
			return;
		}
				
		// apply/revert buttons
		fTabViewer.refresh();
		getButton(IDialogConstants.OK_ID).setEnabled(fTabViewer.canSave());
		
	}
	
	/**
	 * @see ILaunchConfigurationDialog#getActiveTab()
	 */
	public ILaunchConfigurationTab getActiveTab() {
		return fTabViewer.getActiveTab();
	}

	/**
	 * @see ILaunchConfigurationDialog#updateMessage()
	 */
	public void updateMessage() {
		if (isInitializingTabs()) {
			return;
		}
		setErrorMessage(fTabViewer.getErrorMesssage());
		setMessage(fTabViewer.getMesssage());				
	}
	
	/**
	 * Show the default informational message that explains how to create a new configuration.
	 */
	private void setDefaultMessage() {
		setMessage(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Select_a_type_of_configuration_to_create,_and_press___new__51")); //$NON-NLS-1$		
	}
		
	/**
	 * Returns the launch configuration edit area control.
	 * 
	 * @return control
	 */
	private Composite getEditArea() {
		return fEditArea;
	}

	/**
	 * Sets the launch configuration edit area control.
	 * 
	 * @param editArea control
	 */
	private void setEditArea(Composite editArea) {
		fEditArea = editArea;
	}
	
	/**
	 * @see ILaunchConfigurationDialog#setName(String)
	 */
	public void setName(String name) {
		fTabViewer.setName(name);
	}
	
	/**
	 * @see ILaunchConfigurationDialog#generateName(String)
	 */
	public String generateName(String name) {
		if (name == null) {
			name = ""; //$NON-NLS-1$
		}
		return getLaunchManager().generateUniqueLaunchConfigurationNameFrom(name);
	}
			
	/**
	 * Returns the banner image to display in the title area	 */
	protected Image getBannerImage() {
		if (fBannerImage == null) {
			ImageDescriptor descriptor = getLaunchGroup().getBannerImageDescriptor(); 
			if (descriptor != null) {
				fBannerImage = descriptor.createImage();
			} 		
		}
		return fBannerImage;
	}
	
	/**
	 * Sets the launch group to display.
	 * 
	 * @param group launch group
	 */
	protected void setLaunchGroup(LaunchGroupExtension group) {
		fGroup = group;
	}
	
	/**
	 * Returns the launch group being displayed.
	 * 
	 * @return launch group	 */
	public LaunchGroupExtension getLaunchGroup() {
		return fGroup;
	}
	
	/**
	 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		// TODO: or do nothing
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		fTabViewer.handleApplyPressed();
		super.okPressed();
	}

}