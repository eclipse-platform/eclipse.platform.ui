package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILauncher;

/**
 * A launcher delegate is an implementation of the
 * <code>org.eclipse.debug.core.launchers</code> extension point. The
 * debug plug-in creates a proxy to each launcher extension, and
 * lazily instantiates an extension when required. A launch delegate
 * starts a debug session with a specific debug model, and/or
 * launches one or more system processes, registering the result
 * with the launch manager. A launch delegate is also capable
 * of persisting and restoring elements that it can launch.
 * <p>
 * A launcher extension is defined in <code>plugin.xml</code>.
 * Following is an example definition of a launcher extension.
 * <pre>
 * &lt;extension point="org.eclipse.debug.core.launchers"&gt;
 *   &lt;launcher 
 *      id="com.example.ExampleIdentifier"
 *      class="com.example.ExampleLauncher"
 *      modes="run, debug"
 *      label="Example Launcher"
 *      wizard="com.example.ExampleLaunchWizard"
 *      layout="com.example.JavaLayout"&gt;
 *   &lt;/launcher&gt;
 * &lt;/extension&gt;
 * </pre>
 * The attributes are specified as follows:
 * <ul>
 * <li><code>id</code> specifies a unique identifier for this launcher.</li>
 * <li><code>class</code> specifies the fully qualified name of the java class
 *   that implements this interface.</li>
 * <li><code>modes</code> specifies a comma separated list of the modes this
 *    launcher supports - <code>"run"</code> and/or <code>"debug</code>.</li>
 * <li><code>label</code> specifies a human readable label for this launcher
 *    extension.</li>
 * <li><code>wizard</code> specifies the fully qualified name of the java class
 *    that implements <code>org.eclipse.debug.ui.ILaunchWizard</code>. The debug UI
 *    may invoke the wizard to perform a launch, or this launcher extension
 *    may present the wizard when asked to launch.</li>
 * <li></code>layout</code> specifies the identifier of a layout that should be
 *    used to present the resulting launch.</li>
 * </ul>
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see org.eclipse.debug.core.ILaunch
 * @see org.eclipse.debug.core.Launch
 * @see org.eclipse.debug.core.ILaunchManager
 * @see org.eclipse.debug.core.ILauncher
 */
public interface ILauncherDelegate {
	/**
	 * Notifies this launcher delegate to launch in the given mode, and
	 * registers the resulting launch with  the launch manager.
	 * Returns <code>true</code> if successful, otherwise <code>false</code>.
	 * This typically results in the creation of one or more processes and/or
	 * a debug target. The collection of elements provides context for the 
	 * launch. The determination of which or how many objects to launch
	 * is launcher dependent. This method blocks until the launch is
	 * complete. The given launcher is the owner of this delegate.
	 * <p>
	 * For example, when the debug UI invokes a launcher, the
	 * collection of elements passed in could represent the selected elements
	 * in the UI, and the mode specified will be run or debug (depending on
	 * which launch button was pressed). A launcher will generally examine the 
	 * element collection to determine what to launch. For example, if the
	 * collection contains one launchable program, or one runnable program
	 * can be inferred from the collection, the launcher will launch
	 * that program. If the collection contains more than one runnable
	 * program, or more than one runnable program can be inferred from the
	 * collection, the launcher may ask the user to select a program to launch.
	 * </p>
	 * <p>
	 * Launching the program itself usually requires running an external
	 * program specific to a debug architecture/debug model, and wrapping the
	 * result in a debug target or process that can be registered in a launch object. 
	 * </p>
	 *
	 * @param elements an array of objects providing a context for the launch
	 * @param mode run or debug (as defined by <code>ILaunchManager.RUN_MODE</code>,
	 *    <code>ILaunchManager.DEBUG_MODE</code>)
	 * @param launcher the proxy to this lazily instantiated extension which needs
	 *    to be supplied in the resulting launch object
	 * @return whether the launch succeeded
	 *
	 * @see org.eclipse.debug.core.ILaunch
	 * @see org.eclipse.debug.core.Launch
	 * @see IDebugTarget
	 * @see IProcess
	 * @see org.eclipse.debug.core.ILaunchManager#registerLaunch
	 */
	public boolean launch(Object[] elements, String mode, ILauncher launcher);
	
	/**
	 * Returns a memento for an object that this delegate has launched, such
	 * that launched elements can be persisted across workspace invocations.
	 * The memento is used to re-create the launched element.
	 * 
	 * @param element an element this delegate has launched
	 * @return a String representing a memento for the given element,
	 *		or <code>null</code> if unable to create a memento for
	 * 		the element
	 * 
	 * @see #getLaunchObject
	 */
	public String getLaunchMemento(Object element);
	
	/**
	 * Returns the object represented by the given memento, or <code>null</code>
	 * if unable to re-create an element from the given memento, or if
	 * the given object no longer exists.
	 * 
	 * @param memento a memento created by this delegate
	 * @return the object represented by the memento, or <code>null</code>
	 * 
	 * @see #getLaunchMemento
	 */
	public Object getLaunchObject(String memento);
}


