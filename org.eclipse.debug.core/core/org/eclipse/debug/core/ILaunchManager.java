package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IProcess;

/**
 * The launch manager manages the set of registered launches, maintaining
 * a collection of active processes and debug targets. Clients interested
 * in launch notification may register with the launch manager.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @see ILaunch
 * @see ILaunchListener
 */
public interface ILaunchManager {
	/**
	 * A launch in a normal, non-debug mode(value <code>"run"</code>).
	 */
	public static final String RUN_MODE= "run"; //$NON-NLS-1$
	/**
	 * A launch in a special debug mode (value <code>"debug"</code>).
	 */
	public static final String DEBUG_MODE= "debug"; //$NON-NLS-1$
	
	/**
	 * Adds the given listener to the collection of registered launch listeners.
	 * Has no effect if an identical listener is already registerd.
	 *
	 * @param listener the listener to register
	 */
	public void addLaunchListener(ILaunchListener listener);
	/**
	 * Removes the specified launch and notifies listeners.
	 * Has no effect if an identical launch is not already
	 * registered.
	 *
	 * @param launch the launch to remove
	 * @since 2.0
	 */
	public void removeLaunch(ILaunch launch);	
	/**
	 * Returns the collection of debug targets currently registered with this
	 * launch manager.
	 *
	 * @return an array of debug targets
	 */
	public IDebugTarget[] getDebugTargets();
	/**
	 * Returns the collection of launches currently registered
	 * with this launch manager.
	 * 
	 * @return an array of launches
	 */
	public ILaunch[] getLaunches();
	/**
	 * Returns the collection of processes currently registered with this
	 * launch manager.
	 *
	 * @return an array of processes
	 */
	public IProcess[] getProcesses();
	/**
	 * Adds the specified launch and notifies listeners. Has no
	 * effect if an identical launch is already registered.
	 * 
	 * @param launch the launch to add
	 * @since 2.0
	 */
	public void addLaunch(ILaunch launch);	
	/**
	 * Removes the given listener from the collection of registered launch listeners.
	 * Has no effect if an identical listener is not already registerd.
	 *
	 * @param listener the listener to deregister
	 */
	public void removeLaunchListener(ILaunchListener listener);
	/**
	 * Returns all launch configurations defined in the workspace.
	 * 
	 * @return all launch configurations defined in the workspace
	 * @exception CoreException if an exception occurrs retrieving configurations
	 * @since 2.0
	 */
	public ILaunchConfiguration[] getLaunchConfigurations() throws CoreException;
	
	/**
	 * Returns all launch configurations defined in the workspace
	 * of the specified type
	 * 
	 * @param type a launch configuration type
	 * @return all launch configurations defined in the workspace
	 *  of the specified type
	 * @exception CoreException if an error occurs while retreiving
	 *  a launch configuration
	 * @since 2.0
	 */
	public ILaunchConfiguration[] getLaunchConfigurations(ILaunchConfigurationType type) throws CoreException;
	
	/**
	 * Returns a handle to the launch configuration contained
	 * in the specified file. The file is not verified to exist
	 * or contain a launch configuration.
	 * 
	 * @param file launch configuration file
	 * @return a handle to the launch configuration contained
	 *  in the specified file
	 * @since 2.0
	 */
	public ILaunchConfiguration getLaunchConfiguration(IFile file);
	
	/**
	 * Returns a handle to the launch configuration specified by
	 * the given memento. The configuration may not exist.
	 * 
	 * @return a handle to the launch configuration specified by
	 *  the given memento
	 * @exception CoreException if the given memento is invalid or
	 *  an exception occurrs parsing the memento
	 * @see ILaunchConfiguration#getMemento()
	 * @since 2.0
	 */
	public ILaunchConfiguration getLaunchConfiguration(String memento) throws CoreException;
	
	/**
	 * Returns all defined launch configuration type extensions
	 * 
	 * @return all defined launch configuration type extensions
	 * @since 2.0
	 */
	public ILaunchConfigurationType[] getLaunchConfigurationTypes();
	
	/**
	 * Returns the launch configuration type extension with the specified
	 * id, or <code>null</code> if it does not exist.
	 * 
	 * @param id unique identifier for a launch configuration type extension
	 * @return the launch configuration type extension with the specified
	 * id, or <code>null</code> if it does not exist
	 * @since 2.0
	 */
	public ILaunchConfigurationType getLaunchConfigurationType(String id);
	
	/**
	 * Adds the given launch configuration listener to the list
	 * of listeners notified when a launch configuration is
	 * added, removed, or changed. Has no effect if the given listener
	 * is already registered.
	 * 
	 * @param listener launch configuration listener
	 * @since 2.0
	 */
	public void addLaunchConfigurationListener(ILaunchConfigurationListener listener);
	
	/**
	 * Removes the given launch configuration listener from the list
	 * of listeners notified when a launch configuration is
	 * added, removed, or changed. Has no effect if the given listener
	 * is not already registered.
	 * 
	 * @param listener launch configuration listener
	 * @since 2.0
	 */
	public void removeLaunchConfigurationListener(ILaunchConfigurationListener listener);	
	
	/**
	 * Return <code>true</code> if there is a launch configuration with the specified name, 
	 * <code>false</code> otherwise.
	 * 
	 * @param name the name of the launch configuration whose existence is being checked
	 * @exception CoreException if unable to retrieve existing launch configuration names
	 * @since 2.0
	 */
	public boolean isExistingLaunchConfigurationName(String name) throws CoreException;

	/**
	 * Return a String that can be used as the name of a launch configuration.  The name
	 * is guaranteed to be unique (no existing launch configurations will have this name).
	 * The name that is returned uses the <code>namePrefix</code> as a starting point.  If 
	 * there is no existing launch configuration with this name, then <code>namePrefix</code>
	 * is returned.  Otherwise, the value returned consists of the specified prefix plus
	 * some suffix that guarantees uniqueness.
	 * 
	 * @param namePrefix the String that the returned name must begin with
	 * @since 2.0
	 */
	public String generateUniqueLaunchConfigurationNameFrom(String namePrefix);

	/**
	 * Creates and returns a new source locator of the specified
	 * type.
	 * 
	 * @param identifier the identifier associated with a 
	 *  persistable source locator extension
	 * @return a source locator
	 * @exception CoreException if an exception occurrs creating
	 *  the source locator
	 * @since 2.0
	 */
	public IPersistableSourceLocator newSourceLocator(String identifier) throws CoreException;
}


