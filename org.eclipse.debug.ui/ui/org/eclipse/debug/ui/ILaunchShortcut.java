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
package org.eclipse.debug.ui;


import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

/**
 * A launch shortcut is capable of launching a selection
 * or active editor in the workbench. The delegate is responsible for
 * interpretting the selection or active editor (if it applies), and launching
 * an application. This may require creating a new launch configuration
 * with default values, or re-using an existing launch configuration.
 * <p>
 * A launch shortcut is defined as an extension
 * of type <code>org.eclipse.debug.ui.launchShortcuts</code>.
 * A shortcut specifies the perspectives in which is should be available
 * from the "Run/Debug" cascade menus.
 * </p>
 * <p>
 * A launch shortcut extension is defined in <code>plugin.xml</code>.
 * Following is an example definition of a launch shortcut extension.
 * <pre>
 * &lt;extension point="org.eclipse.debug.ui.launchShortcuts"&gt;
 *   &lt;launchShortcut
 *      id="com.example.ExampleLaunchShortcut"
 *      class="com.example.ExampleLaunchShortcutClass"
 *      filterClass="com.example.ExampleLaunchShortcutFilterClass"
 * 		label="Example Label"
 * 		icon="\icons\exampleshortcut.gif"
 * 		helpContextId="com.example.shortcut_context"
 * 		modes="run, debug"&gt;
 *      &lt;perspective id="com.example.perspectiveId1"/&gt;
 *      &lt;perspective id="com.example.perspectiveId2"/&gt;
 *      &lt;filter
 *           name="NameMatches"
 *           value="*.java"/&gt;
 *      &lt;filter
 *        	name="ContextualLaunchActionFilter"
 *        	value="supportsContextualLaunch"/&gt;
 *      &lt;contextLabel
 *        	mode="run"
 *        	label="%RunJavaApplet.label"/&gt;
 * 	    &lt;contextLabel
 * 		 	mode="debug"
 * 		 	label="%DebugJavaApplet.label"/&gt;
 * 		  ...
 *   &lt;/launchShortcut&gt;
 * &lt;/extension&gt;
 * </pre>
 * The attributes are specified as follows:
 * <ul>
 * <li><code>id</code> specifies a unique identifier for this launch shortcut.</li>
 * <li><code>class</code> specifies a fully qualified name of a Java class
 *  that implements <code>ILaunchShortcut</code>.</li><li>
 * <code>filterClass</code> optionally specifies a fully qualified name of a Java class
 *  that implements <code>ILaunchFilter</code> for context menu filtering.</li>
 * <li><code>label</code> specifies a label used to render this shortcut.</li>
 * <li><code>icon</code> specifies a plug-in relative path to an icon used to
 * 	render this shortcut.</li>
 * <li><code>helpContextId</code> optional attribute specifying the help context
 * identifier to associate with the launch shortcut action in a menu.</li>
 * <li><code>modes</code> specifies a comma separated list of modes this shortcut
 *  supports.</li>
 * <li><code>perspective</code> one or more perspective entries enumerate the
 * 	perspectives that this shortcut is avilable in, from the run/debug cascade
 * 	menus.</li>
 * <li><code>filter</code> zero or more filter entries specify the attribute
 * <code>name</code> and attribute <code>value</code> that will be supplied to
 * the <code>testAttribute</code> method implemented by the <code>filterClass</code>
 * Java Class. If all filters in this list return <code>true</code> when applied
 * to a selection target, the shortcut will be avilable in the run/debug context menu.
 * 	menu.</li>
 * <li><code>contextLabel</code> zero or more context menu labels. For
 * shortcuts that pass their filter tests, the specified label will appear
 * in the "Run ->" context menu and be bound to a launch action of the
 * specified mode (e.g. run,debug,profile).</li>
 * </ul>
 * </p>
 * @since 2.0
 */
public interface ILaunchShortcut {

	/**
	 * Locates a launchable entity in the given selection and launches
	 * an application in the specified mode. This launch configuration
	 * shortcut is responsible for progress reporting as well
	 * as error handling, in the event that a launchable entity cannot
	 * be found, or launching fails.
	 * 
	 * @param selection workbench selection
	 * @param mode one of the launch modes defined by the 
	 * 	launch manager
	 * @see org.eclipse.debug.core.ILaunchManager
	 */
	public void launch(ISelection selection, String mode);
	
	/**
	 * Locates a launchable entity in the given active editor, and launches
	 * an application in the specified mode. This launch configuration
	 * shortcut is responsible for progress reporting as well as error
	 * handling, in the event that a launchable entity cannot be found,
	 * or launching fails.
	 * 
	 * @param editor the active editor in the workbench
	 * @param mode one of the launch modes defined by the launch
	 * 		manager
	 * @see org.eclipse.debug.core.ILaunchManager
	 */
	public void launch(IEditorPart editor, String mode);
}
