package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

/**
 * Launch configuration handle.
 * 
 * @see ILaunchConfiguration
 */
public class LaunchConfiguration extends PlatformObject implements ILaunchConfiguration {
	
	/**
	 * Location this configuration is stored in. This 
	 * is the key for a launch configuration handle.
	 */
	private IPath fLocation;
		
	/**
	 * Constructs a launch configuration in the given location.
	 * 
	 * @param location path to where this launch configuration's
	 *  underlying file is located
	 */
	protected LaunchConfiguration(IPath location) {
		setLocation(location);
	}
	
	/**
	 * Sets the location of this configuration's underlying
	 * file.
	 * 
	 * @param location the location of this configuration's underlying
	 *  file
	 */
	private void setLocation(IPath location) {
		fLocation = location;
	}

	/**
	 * @see ILaunchConfiguration#launch(String)
	 */
	public ILaunch launch(String mode) throws CoreException {
		ILaunch launch = getDelegate().launch(this, mode);
		if (launch != null) {
			getLaunchManager().registerLaunch(launch);
		}
		return launch;
	}

	/**
	 * @see ILaunchConfiguration#verify(String)
	 */
	public void verify(String mode) throws CoreException {
		getDelegate().verify(this, mode);
	}

	/**
	 * @see ILaunchConfiguration#supportsMode(String)
	 */
	public boolean supportsMode(String mode) throws CoreException {
		return getType().supportsMode(mode);
	}

	/**
	 * A configuration's name is that of the last segment
	 * in it's location (subtract the ".launch" extension).
	 * 
	 * @see ILaunchConfiguration#getName()
	 */
	public String getName() {
		String name = getLocation().lastSegment();
		name = name.substring(0, name.length() - (LAUNCH_CONFIGURATION_FILE_EXTENSION.length() + 1));
		return name;
	}

	/**
	 * @see ILaunchConfiguration#getLocation()
	 */
	public IPath getLocation() {
		return fLocation;
	}

	/**
	 * @see ILaunchConfiguration#exists()
	 */
	public boolean exists() {
		IFile file = getFile();
		if (file == null) {
			return getLocation().toFile().exists();
		} else {
			return file.exists();
		}
	}

	/**
	 * @see ILaunchConfiguration#getAttribute(String, int)
	 */
	public int getAttribute(String attributeName, int defaultValue) throws CoreException {
		return getInfo().getIntAttribute(attributeName, defaultValue);
	}

	/**
	 * @see ILaunchConfiguration#getAttribute(String, String)
	 */
	public String getAttribute(String attributeName, String defaultValue) throws CoreException {
		return getInfo().getStringAttribute(attributeName, defaultValue);
	}

	/**
	 * @see ILaunchConfiguration#getAttribute(String, boolean)
	 */
	public boolean getAttribute(String attributeName, boolean defaultValue) throws CoreException {
		return getInfo().getBooleanAttribute(attributeName, defaultValue);
	}

	/**
	 * @see ILaunchConfiguration#getProject()
	 */
	public IProject getProject() {
		IFile file = getFile();
		if (file == null) {
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (int i = 0; i < projects.length; i++) {
				if (projects[i].getPluginWorkingLocation(DebugPlugin.getDefault().getDescriptor()).isPrefixOf(getLocation())) {
					return projects[i];
				}
			}
			// XXX: this method not spec'd to return null.
			// May have to cache project name at creation
			return null;
		} else {
			return file.getProject();
		}
	}

	/**
	 * @see ILaunchConfiguration#getType()
	 */
	public ILaunchConfigurationType getType() throws CoreException {
		return getInfo().getType();
	}

	/**
	 * @see ILaunchConfiguration#isLocal()
	 */
	public boolean isLocal() {
		return  getFile() == null;
	}

	/**
	 * @see ILaunchConfiguration#getWorkingCopy()
	 */
	public ILaunchConfigurationWorkingCopy getWorkingCopy() throws CoreException {
		return new LaunchConfigurationWorkingCopy(this);
	}

	/**
	 * @see ILaunchConfiguration#isWorkingCopy()
	 */
	public boolean isWorkingCopy() {
		return false;
	}

	/**
	 * @see ILaunchConfiguration#delete()
	 */
	public void delete() throws CoreException {
		if (exists()) {
			if (isLocal()) {
				if (!(getLocation().toFile().delete())) {
					throw new DebugException(
						new Status(Status.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
						 DebugException.REQUEST_FAILED, "Failed to delete launch configuration.", null)
					);
				}
				// manually update the launch manager cache since there
				// will be no resource delta
				getLaunchManager().launchConfigurationDeleted(this);
			} else {
				// delete the resource using IFile API such that
				// resource deltas are fired.
				IResource file = getFile();
				if (file != null) {
					file.delete(true, null);
				} else {
					// Error - the exists test passed, but could not locate file 
				}
			}
		}
	}
	
	/**
	 * Returns the info object containing the attributes
	 * of this configuration
	 * 
	 * @return info for this handle
	 * @exception CoreException if unable to retrieve the
	 *  info object
	 */
	protected LaunchConfigurationInfo getInfo() throws CoreException {
		return getLaunchManager().getInfo(this);
	}
	
	/**
	 * Returns the launch configuration delegate for this
	 * launch configuration.
	 * 
	 * @return launch configuration delegate
	 * @exception CoreException if the delegate was unable
	 *  to be created
	 */
	protected ILaunchConfigurationDelegate getDelegate() throws CoreException {
		return ((LaunchConfigurationType)getType()).getDelegate();
	}
	
	/**
	 * Returns the launch manager
	 * 
	 * @return lanuch manager
	 */
	protected LaunchManager getLaunchManager() {
		return (LaunchManager)DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * @see ILaunchConfiguration#getMemento()
	 */
	public String getMemento() {
		return getLocation().toOSString();
	}

	/**
	 * Returns the file in the workspace for this configuration or,
	 * <code>null</code> if none (i.e. if local).
	 * 
	 * @return file or <code>null</code> if local
	 */	
	protected IFile getFile() {
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(getLocation());
	}

	/**
	 * Returns whether this configuration is equal to the
	 * given configuration. Two configurations are equal if
	 * they are stored in the same location (and neither one
	 * is a working copy).
	 * 
	 * @return whether this configuration is equal to the
	 *  given configuration
	 * @see Object.equals(Object)
	 */
	public boolean equals(Object object) {
		if (object instanceof ILaunchConfiguration) {
			if (isWorkingCopy()) {
				return this == object;
			} else {
				return  ((ILaunchConfiguration)object).getLocation().equals(getLocation());
			}
		}
		return false;
	}
	
	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return getLocation().hashCode();
	}
}

