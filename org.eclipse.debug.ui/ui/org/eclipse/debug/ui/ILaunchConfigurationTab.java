package org.eclipse.debug.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
 
/**
 * A launch configuration tab is used to edit/view attributes
 * of a specific type of launch configuration. Launch
 * configurations are presented in a dialog, with a tab folder.
 * Each tab manipulates one ore more attributes of a launch
 * configuration. 
 * <p>
 * The tab's lifecycle begins when <code>setLaunchConfigurationDialog(ILaunchConfigurationDialog)</code>
 * is called. A tab may then be asked repeatedly to initialize its
 * controls to display values for a launch configuration (see
 * <code>initializeFrom(ILaunchConfiguration)</code>), and to
 * apply its current settings to a launch configuration (see
 * <code>performApply(ILaunchConfigurationWorkingCopy)</code>).
 * While a user manipulates a tab's controls, the tab is not
 * intended to update a launch configuration. Updating a launch
 * configuration should only be performed when <code>performApply</code>
 * is called. To end a tab's lifecyle, <code>dispose()</code> will
 * be called. Note that a tab can be disposed before its control
 * has been created.
 * </p>
 * <p>
 * To support single-click launching, tabs are required to initialize
 * default values into launch configurations (possibly when their controls
 * have not been created). See <code>setDefault(ILaunchConfigurationWorkingCopy)</code>.
 * As well, the method <code>launched</code> can be called when the tab's
 * control does not exist.
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
 *      mode="debug"
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
 * <li><code>mode</code> optionally specifies the mode for which this tab should
 *  be displayed - one of "run" or "debug". This tab is displayed for
 *  both modes if unspecified.</li>
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
 * @since 2.0
 */
public interface ILaunchConfigurationTab {

	/**
	 * Returns whether it is ok to leave this tab and display
	 * another tab.
	 *
	 * @return whether it is ok to leave this tab and display
	 *  another tab
	 */
	public boolean okToLeave();

	/**
	 * Creates the top level control for this launch configuration
	 * tab under the given parent composite. Marks the beginning
	 * of this tab's lifecycle. This method is called once on
	 * tab creation, after <code>setLaunchConfigurationDialog</code>
	 * is called.
	 * <p>
	 * Implementors are responsible for ensuring that
	 * the created control can be accessed via <code>getControl</code>
	 * </p>
	 *
	 * @param parent the parent composite
	 */
	public void createControl(Composite parent);
	
	/**
	 * Returns the top level control for this tab.
	 * <p>
	 * May return <code>null</code> if the control
	 * has not been created yet.
	 * </p>
	 *
	 * @return the top level control or <code>null</code>
	 */
	public Control getControl();	
	
	/**
	 * Initializes the given launch configuration with
	 * default values for this tab. This method
	 * is called when a new launch configuration is created
	 * such that the configuration can be initialized with
	 * meaningful values. This method may be called before this
	 * tab's control is created, to support single-click launching.
	 * 
	 * @param configuration launch configuration
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration);	
	
	/**
	 * Initializes this tab's controls with values from the given
	 * launch configuration. This method is called when
	 * a configuration is selected to view or edit, after this
	 * tab's control has been created.
	 * 
	 * @param configuration launch configuration
	 */
	public void initializeFrom(ILaunchConfiguration configuration);		
	
	/**
	 * Notifies this launch configuration tab that it has
	 * been disposed. Marks the end of this tab's lifecycle,
	 * allowing this tab to perform any cleanup required.
	 */
	public void dispose();
	
	/**
	 * Copies values from this tab into the given 
	 * launch configuration.
	 * 
	 * @param configuration launch configuration
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration);
	
	/**
	 * Returns the current error message for this tab.
	 * May be <code>null</null> to indicate no error message.
	 * <p>
	 * An error message should describe some error state,
	 * as opposed to a message which may simply provide instruction
	 * or information to the user.
	 * </p>
	 * 
	 * @return the error message, or <code>null</code> if none
	 */
	public String getErrorMessage();
	
	/**
	 * Returns the current message for this tab.
	 * <p>
	 * A message provides instruction or information to the 
	 * user, as opposed to an error message which should 
	 * describe some error state.
	 * </p>
	 * 
	 * @return the message, or <code>null</code> if none
	 */
	public String getMessage();	
	
	/**
	 * Returns whether this tab is in a valid state.
	 * <p>
	 * This information is typically used by the launch configuration
	 * dialog to decide when it is okay to launch.
	 * </p>
	 *
	 * @return whether this tab is in a valid state
	 */
	public boolean isValid();	
	
	/**
	 * Sets the launch configuration dialog that hosts this tab.
	 * This is the first method called on a launch configuration
	 * tab, and marks the beginning of this tab's lifecycle.
	 * 
	 * @param dilaog launch configuration dialog
	 */
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog);
	
	/**
	 * Notifies this tab that the specified configuration has been
	 * launched, resulting in the given launch. This method can be
	 * called when a tab's control does not exist, to support single-click
	 * launching.
	 * 
	 * @param launch the result of launching the current
	 *  launch configuration
	 */
	public void launched(ILaunch launch);
}

