package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 
/**
 * A launch configuration tab group is used to edit/view attributes
 * of a specific type of launch configuration. Launch
 * configurations are presented in a dialog, with a tab folder.
 * Each tab manipulates one or more attributes of a launch
 * configuration. The tab group controls which tabs are
 * displayed for a specific type of launch configuration,
 * and provides a mechanism for overriding configuration
 * initialization performed by tabs.
 * <p>
 * The tab group's lifecycle begins when <code>createTabs(ILaunchConfigurationDialog, String)</code>
 * is called. A tab group may then be asked repeatedly to initialize its
 * tabs to display values for a launch configuration (see
 * <code>initializeFrom(ILaunchConfiguration)</code>), and to
 * apply its current settings to a launch configuration (see
 * <code>performApply(ILaunchConfigurationWorkingCopy)</code>).
 * While a user manipulates a tab's controls, the tab is not
 * intended to update a launch configuration. Updating a launch
 * configuration should only be performed when <code>performApply</code>
 * is called. To end a tab group's lifecyle, <code>dispose()</code> will
 * be called. Note that a tab group can be disposed before its controls
 * have been created.
 * </p>
 * When a user leaves a tab, a tab is asked to apply its current settings
 * to a launch configuration working copy. When a tab is entered, it is
 * asked to initialize itself from a working copy. This mechanism is used
 * to support inter-tab dependencies.
 * <p>
 * To support single-click launching, a tab group is required to initialize
 * default values into launch configurations (possibly when controls
 * have not been created). See <code>setDefaults(ILaunchConfigurationWorkingCopy)</code>.
 * As well, the method <code>launched</code> can be called when the tab's
 * control does not exist.
 * </p>
 * <p>
 * A launch configuration group extension is defined in <code>plugin.xml</code>.
 * Following is an example definition of a launch configuration
 * group extension.
 * <pre>
 * &lt;extension point="org.eclipse.debug.ui.launchConfigurationTabGroups"&gt;
 *   &lt;launchConfigurationTabGroup 
 *      id="com.example.ExampleTabGroup"
 *      type="com.example.ExampleLaunchConfigurationTypeIdentifier"
 *      class="com.example.ExampleLaunchConfigurationTabGroupClass"&gt;
 *   &lt;/launchConfigurationTabGroup&gt;
 * &lt;/extension&gt;
 * </pre>
 * The attributes are specified as follows:
 * <ul>
 * <li><code>id</code> specifies a unique identifier for this launch configuration
 *  tab group.</li>
 * <li><code>type</code> specifies launch configuration type that this tab
 *  group is applicable to (corresponds to the id of a launch configuration type
 *  extension).</li>
 * <li><code>class</code> specifies a fully qualified name of a Java class
 *  that implements <code>ILaunchConfigurationTabGroup</code>.</li>
 * </ul>
 * </p>
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 * @see org.eclipse.debug.core.ILaunchConfigurationType
 * @see org.eclipse.debug.core.ILaunchConfiguration
 * @see org.eclipse.debug.ui.ILaunchConfigurationTab
 * @since 2.0
 */
public interface ILaunchConfigurationTabGroup {

	/**
	 * Creates the tabs contained in this tab group for the specified
	 * launch mode. The tabs control's are not created. This is the
	 * fist method called in the lifecycle of a tab group.
	 * 
	 * @param dialog the launch configuration dialog this tab group
	 *  is contained in
	 * @param mode the mode the launch configuration dialog was
	 *  opened in
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode);
	
	/**
	 * Returns the tabs contained in this tab group.
	 * 
	 * @return the tabs contained in this tab group
	 */
	public ILaunchConfigurationTab[] getTabs();

	/**
	 * Notifies this launch configuration tab group that it has
	 * been disposed, and disposes this group's tabs. Marks the end
	 * of this tab group's lifecycle, allowing this tab group to
	 * perform any cleanup required.
	 */
	public void dispose();
			
	/**
	 * Initializes the given launch configuration with
	 * default values for this tab group. This method
	 * is called when a new launch configuration is created
	 * such that the configuration can be initialized with
	 * meaningful values. This method may be called before
	 * tab controls are created, to support single-click launching.
	 * 
	 * @param configuration launch configuration
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration);	
	
	/**
	 * Initializes this group's tab controls with values from the given
	 * launch configuration. This method is called when
	 * a configuration is selected to view or edit.
	 * 
	 * @param configuration launch configuration
	 */
	public void initializeFrom(ILaunchConfiguration configuration);		
		
	/**
	 * Copies values from this group's tabs into the given 
	 * launch configuration.
	 * 
	 * @param configuration launch configuration
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration);
	
	/**
	 * Notifies this tab that a configuration has been
	 * launched, resulting in the given launch. This method can be
	 * called when a tab's control does not exist, to support single-click
	 * launching.
	 * 
	 * @param launch the result of launching the current
	 *  launch configuration
	 */
	public void launched(ILaunch launch);
}

