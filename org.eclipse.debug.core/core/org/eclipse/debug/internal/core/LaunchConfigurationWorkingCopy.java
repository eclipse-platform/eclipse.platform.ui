package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * A working copy launch configuration
 */
public class LaunchConfigurationWorkingCopy extends LaunchConfiguration implements ILaunchConfigurationWorkingCopy {
	
	/**
	 * Handle of original launch configuration this
	 * working copy is based on
	 */
	private LaunchConfiguration fOriginal;
	
	/**
	 * Working copy of attributes.
	 */
	private LaunchConfigurationInfo fInfo;
	
	/**
	 * Whether this working copy has been modified since
	 * it was created
	 */
	private boolean fDirty = false;
	
	/**
	 * The name for this configuration.
	 */
	private String fName;
	
	/**
	 * Indicates whether this working copy has been explicitly renamed.
	 */
	private boolean fRenamed = false;
	
	/**
	 * Suppress change notification until created
	 */
	private boolean fSuppressChange = true;
	
	/**
	 * The container this working copy will be
	 * stored in when saved.
	 */
	private IContainer fContainer;

	/**
	 * Constructs a working copy of the specified launch 
	 * configuration.
	 * 
	 * @param original launch configuration to make
	 *  a working copy of
	 * @exception CoreException if unable to initialize this
	 *  working copy's attributes based on the original configuration
	 */
	protected LaunchConfigurationWorkingCopy(LaunchConfiguration original) throws CoreException {
		super(original.getLocation());
		setName(original.getName());
		copyFrom(original);
		setOriginal(original);
		fSuppressChange = false;
	}
	
	/**
	 * Constructs a copy of the specified launch 
	 * configuration, with the given (new) name.
	 * 
	 * @param original launch configuration to make
	 *  a working copy of
	 * @param name the new name for the copy of the launch
	 *  configuration
	 * @exception CoreException if unable to initialize this
	 *  working copy's attributes based on the original configuration
	 */
	protected LaunchConfigurationWorkingCopy(LaunchConfiguration original, String name) throws CoreException {
		super(original.getLocation());
		copyFrom(original);
		setName(name);
		fSuppressChange = false;
	}
	
	/**
	 * Constructs a new working copy to be created in the specified
	 * location.
	 * 
	 * @param container the container that the configuration will be created in
	 *  or <code>null</code> if to be local
	 * @param name the name of the new launch configuration
	 * @param type the type of this working copy
	 */
	protected LaunchConfigurationWorkingCopy(IContainer container, String name, ILaunchConfigurationType type) {
		super((IPath)null);
		setName(name);
		setInfo(new LaunchConfigurationInfo());
		getInfo().setType(type);
		setContainer(container);
		fSuppressChange = false;
	}

	/**
	 * @see ILaunchConfigurationWorkingCopy#isDirty()
	 */
	public boolean isDirty() {
		return fDirty;
	}

	/**
	 * @see ILaunchConfigurationWorkingCopy#doSave()
	 */
	public ILaunchConfiguration doSave() throws CoreException {
		if (isDirty()) {
			IWorkspaceRunnable wr = new IWorkspaceRunnable() {
				public void run(IProgressMonitor pm) throws CoreException {
					// write the new file
					LaunchConfigurationWorkingCopy.this.writeNewFile();
					// delete the old file if this is not a new configuration
					// or the file was renamed/moved
					if (!LaunchConfigurationWorkingCopy.this.isNew()) {
						if (LaunchConfigurationWorkingCopy.this.isMoved()) {
							LaunchConfigurationWorkingCopy.this.getOriginal().delete();
						}
					}
					resetDirty();
				}
			};
			
			ResourcesPlugin.getWorkspace().run(wr, null);
		}
		
		return new LaunchConfiguration(getLocation());		
	}
	
	/**
	 * Writes the new configuration information to a file.
	 * 
	 * @exception CoreException if writing the file fails
	 */
	protected void writeNewFile() throws CoreException {
		String xml = null;
		try {
			xml = getInfo().getAsXML();
		} catch (IOException e) {
			throw new DebugException(
				new Status(
				 Status.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchConfigurationWorkingCopy.{0}_occurred_generating_launch_configuration_XML._1"), new String[]{e.toString()}), null //$NON-NLS-1$
				)
			);					
		}
		
		if (isLocal()) {
			// use java.io to update configuration file
			try {
				boolean added = false;
				File file = getLocation().toFile();
				File dir = getLocation().removeLastSegments(1).toFile();
				dir.mkdirs();
				if (!file.exists()) {
					added = true;
					file.createNewFile();
				}
				FileOutputStream stream = new FileOutputStream(file);
				stream.write(xml.getBytes("UTF8")); //$NON-NLS-1$
				stream.close();
				if (added) {
					getLaunchManager().launchConfigurationAdded(new LaunchConfiguration(getLocation()));
				} else {
					getLaunchManager().launchConfigurationChanged(new LaunchConfiguration(getLocation()));
				}
			} catch (IOException e) {
				throw new DebugException(
					new Status(
					 Status.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
					 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchConfigurationWorkingCopy.{0}_occurred_generating_launch_configuration_XML._1"), new String[]{e.toString()}), null //$NON-NLS-1$
					)
				);				
			}
		} else {
			// use resource API to update configuration file
			IFile file = getFile();
			IContainer dir = file.getParent();
			if (!dir.exists()) {
				throw new DebugException(
					new Status(
					 Status.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
					 DebugException.REQUEST_FAILED, DebugCoreMessages.getString("LaunchConfigurationWorkingCopy.Specified_container_for_launch_configuration_does_not_exist_2"), null //$NON-NLS-1$
					)
				);				
			}
			ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
			if (!file.exists()) {
				file.create(stream, false, null);
				//getLaunchManager().launchConfigurationAdded(new LaunchConfiguration(getLocation()));
			} else {
				file.setContents(stream, false, false, null);
				//getLaunchManager().launchConfigurationChanged(new LaunchConfiguration(getLocation()));
			}
		}		
	}

	/**
	 * @see ILaunchConfigurationWorkingCopy#setAttribute(String, int)
	 */
	public void setAttribute(String attributeName, int value) {
		getInfo().setAttribute(attributeName, new Integer(value));
		setDirty();
	}

	/**
	 * @see ILaunchConfigurationWorkingCopy#setAttribute(String, String)
	 */
	public void setAttribute(String attributeName, String value) {
		getInfo().setAttribute(attributeName, value);
		setDirty();
	}

	/**
	 * @see ILaunchConfigurationWorkingCopy#setAttribute(String, boolean)
	 */
	public void setAttribute(String attributeName, boolean value) {
		getInfo().setAttribute(attributeName, new Boolean(value));
		setDirty();	
	}

	/**
	 * @see ILaunchConfigurationWorkingCopy#setAttribute(String, List)
	 */
	public void setAttribute(String attributeName, List value) {
		getInfo().setAttribute(attributeName, value);
		setDirty();
	}

	/**
	 * @see ILaunchConfigurationWorkingCopy#setAttribute(String, Map)
	 */
	public void setAttribute(String attributeName, Map value) {
		getInfo().setAttribute(attributeName, value);
		setDirty();
	}

	/**
	 * @see ILaunchConfigurationWorkingCopy#getOriginal()
	 */
	public ILaunchConfiguration getOriginal() {
		return fOriginal;
	}
	
	/**
	 * Sets the launch configuration this working copy
	 * is based on. Initializes the attributes of this
	 * working copy to the current values of the given
	 * configuration.
	 * 
	 * @param originl the launch configuration this working
	 *  copy is based on.
	 * @exception CoreException if unable to initialize this
	 *  working copy based on the original's current attribute
	 *  set
	 */
	private void copyFrom(LaunchConfiguration original) throws CoreException {
		LaunchConfigurationInfo info = original.getInfo();
		setInfo(info.getCopy());
		setContainer(original.getContainer());
		resetDirty();
	}
	
	/**
	 * Sets the launch configuration this working copy
	 * is based on.
	 * 
	 * @param originl the launch configuration this working
	 *  copy is based on.
	 */
	private void setOriginal(LaunchConfiguration original) {
		fOriginal = original;
	}	
	
	/**
	 * Sets the working copy info object for this working copy.
	 * 
	 * @param info a copy of attributes from this working copy's
	 * 	original launch configuration
	 */
	protected void setInfo(LaunchConfigurationInfo info) {
		fInfo = info;
	}

	/**
	 * @see ILaunchConfiguration#isWorkingCopy()
	 */
	public boolean isWorkingCopy() {
		return true;
	}
	
	/**
	 * A working copy keeps a local info object that is not
	 * cached with the launch manager.
	 * 
	 * @see LaunchConfiguration#getInfo()
	 */
	protected LaunchConfigurationInfo getInfo() {
		return fInfo;
	}
	
	/**
	 * Sets this working copy's state to dirty.
	 * Notifies listeners that this working copy has
	 * changed.
	 */
	private void setDirty() {
		fDirty = true;
		if (!suppressChangeNotification()) {
			getLaunchManager().notifyChanged(this);
		}	
	}
	
	/**
	 * Sets this working copy's state to not dirty.
	 */
	private void resetDirty() {
		fDirty = false;
	}	
		
	/**
	 * @see ILaunchConfigurationWorkingCopy#rename(String)
	 */
	public void rename(String name) {
		if (!getName().equals(name)) {
			setName(name);
			fRenamed = isNew() || !(getOriginal().getName().equals(name));
		}
	}

	/**
	 * Sets the new name for this configuration.
	 * 
	 * @param name the new name for this configuration
	 */
	private void setName(String name) {
		fName = name;
		setDirty();
	}
	
	/**
	 * @see ILaunchConfiguration#getName()
	 */
	public String getName() {
		return fName;
	}
	
	/**
	 * @see ILaunchConfiguration#isLocal()
	 */
	public boolean isLocal() {
		return getContainer() == null;
	}	
	
	/**
	 * Returns the location this launch configuration will reside at
	 * when saved.
	 * 
	 * @see ILaunchConfiguration#getLocation()
	 */
	public IPath getLocation() {
		if (isMoved()) {
			IPath path = null;
			if (isLocal()) {
				path = LaunchManager.LOCAL_LAUNCH_CONFIGURATION_CONTAINER_PATH;
			} else {
				path = getContainer().getLocation();
			}
			path = path.append(getName() + "." + LAUNCH_CONFIGURATION_FILE_EXTENSION); //$NON-NLS-1$
			return path;
		} else {
			return getOriginal().getLocation();
		}
	}
	
	/**
	 * Returns whether this working copy is new, or is a
	 * working copy of another launch configuration.
	 * 
	 * @return whether this working copy is new, or is a
	 *  working copy of another launch configuration
	 */
	protected boolean isNew() {
		return getOriginal() == null;
	}
	
	/**
	 * Returns whether this working copy is new or if its
	 * location has changed from that of its original.
	 * 
	 * @return whether this working copy is new or if its
	 * location has changed from that of its original
	 */
	protected boolean isMoved() {
		if (isNew() || fRenamed) {
			return true;
		}
		IContainer newContainer = getContainer();
		IContainer originalContainer = ((LaunchConfiguration)getOriginal()).getContainer();
		if (newContainer == originalContainer) {
			return false;
		}
		if (newContainer == null) {
			return !originalContainer.equals(newContainer);
		} else {
			return !newContainer.equals(originalContainer);
		}
	}		
	
	/**
	 * A working copy cannot generate a memento.
	 * 
	 * @see ILaunchConfiguration#getMemento()
	 */
	public String getMemento() {
		return null;
	}	
	
	/**
	 * Returns whether change notification should be
	 * suppressed
	 */
	protected boolean suppressChangeNotification() {
		return fSuppressChange;
	}
	
	/**
	 * @see ILaunchConfigurationWorkingCopy#setContainer(IContainer)
	 */
	public void setContainer(IContainer container) {
		if (container == fContainer) {
			return;
		}
		if (container != null) {
			if (container.equals(fContainer)) {
				return;
			}
		} else {
			if (fContainer.equals(container)) {
				return;
			}
		}
		fContainer = container;
		setDirty();
	}
	
	/**
	 * Returns the container this working copy will be
	 * stored in when saved, or <code>null</code> if
	 * this working copy is local.
	 * 
	 * @return the container this working copy will be
	 *  stored in when saved, or <code>null</code> if
	 *  this working copy is local
	 */
	protected IContainer getContainer() {
		return fContainer;
	}	
}

