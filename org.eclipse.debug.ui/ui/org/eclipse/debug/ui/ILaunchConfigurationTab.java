package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabItem;
 
/**
 * Note: This interface is yet experimental.
 * <p>
 * Extension that provides a user interface to manipulate a
 * launch configuration. Launch configurations are presented in
 * a dialog, with a tab folder. Each tab may manipulate one
 * ore more attributes of a launch configuration working copy. 
 * </p>
 * <p>
 * A launch configuration tab extension is defined in <code>plugin.xml</code>.
 * Following is an example definition of a launch configuration
 * tab extension.
 * <pre>
 * &lt;extension point="org.eclipse.debug.ui.launchConfigurationTab"&gt;
 *   &lt;launchConfigurationType 
 *      id="com.example.ExampleTabIdentifier"
 *      type="com.example.ExampleLaunchConfigurationTypeIdentifier"
 *      class="com.example.ExampleLaunchConfigurationTabClass"
 *      name="Example Tab Page"
 *   &lt;/launchConfigurationType&gt;
 * &lt;/extension&gt;
 * </pre>
 * The attributes are specified as follows:
 * <ul>
 * <li><code>id</code> specifies a unique identifier for this launch configuration
 *  tab.</li>
 * <li><code>type</code> specifies launch configuration type that this tab
 *  is applicable to (corresponds to the id of a launch configuration type
 *  extension). Or, if unspecified, this tab will appear for all launch
 *  configurations.</li>
 * <li><code>class</code>specifies a fully qualified name of a Java class
 *  that implements <code>ILanuchConfigurationTab</code>.</li>
 * <li><code>name</code> specifies a human readable name for this
 *  launch configuration tab that will appear as the title for this
 *  tab, in the tab page.</li>
 * </ul>
 * </p>
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 * <p>
 * <b>NOTE:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see ILaunchConfigurationType
 * @see ILaunchConfiguration
 */
public interface ILaunchConfigurationTab {

	/**
	 * Creates and returns the control to be used for this
	 * tab, in the given tab item. Marks the beginning
	 * of this tabs lifecycle. This is called once after
	 * the tab is created.
	 * 
	 * @param tabItem the tabItem in which to create
	 *   for this tab's control
	 * @return the control to be used for this tab
	 */
	public Control createTabControl(ILaunchConfigurationDialog dialog, TabItem tabItem);
	
	/**
	 * Sets the launch configuration (working copy) that this
	 * tab is currently presenting/editing. This page should
	 * be updated to reflect the configuration's attribute
	 * values. Discards any previous launch configuration
	 * this tab was displaying. This can be called multiple
	 * times, but only after <code>createTabControl</code>
	 * has been called.
	 * 
	 * @param launchConfiguration working copy of a launch
	 *  configuration to display and edit
	 */
	public void setLaunchConfiguration(ILaunchConfigurationWorkingCopy launchConfiguration);
	
	/**
	 * Notifies this launch configuration tab that it has
	 * been disposed. Marks the end of this tabs lifecycle,
	 * allowing this tab to perform any cleanup required.
	 * This is called once, after <code>createTabControl</code>
	 * has been called.
	 */
	public void dispose();
}

