package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.text.MessageFormat;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
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
	 * The new name for this configuration, or <code>null</code>
	 * if this configuration is not to be re-named or created.
	 */
	private String fName;
	
	/**
	 * The new project for this launch configuration, or <code>null</code>
	 * if this configuration is based on an existing configuration.
	 */
	private IProject fProject;
	
	/**
	 * Whether this configuration is to be local or shared.
	 */
	private boolean fLocal;

	/**
	 * Constructs a working for the specified lanuch 
	 * configuration.
	 * 
	 * @param original launch configuration to make
	 *  a working copy of
	 * @exception CoreException if unable to initialize this
	 *  working copy's attributes based on the original configuration
	 */
	protected LaunchConfigurationWorkingCopy(LaunchConfiguration original) throws CoreException {
		super(original.getLocation());
		setOriginal(original);
		setLocal(original.isLocal());
	}
	
	/**
	 * Constructs a new working to be created in the specified
	 * location.
	 * 
	 * @param project the project that will own this launch configuration
	 * @param name the name of the new launch configuration
	 * @param local whether this configuration will be local or shared
	 * @param type the type of this working copy
	 */
	protected LaunchConfigurationWorkingCopy(IProject project, String name, boolean local, ILaunchConfigurationType type) {
		super(null);
		setNewProject(project);
		setNewName(name);
		setLocal(local);
		setInfo(new LaunchConfigurationInfo());
		getInfo().setType(type);
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
						if (LaunchConfigurationWorkingCopy.this.isMoved() ||
						LaunchConfigurationWorkingCopy.this.isRenamed()) {
							LaunchConfigurationWorkingCopy.this.getOriginal().delete();
						}
					}
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
				stream.write(xml.getBytes());
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
			IFolder dir = (IFolder)file.getParent();
			if (!dir.exists()) {
				dir.create(false, true, null);
			}
			ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
			if (!file.exists()) {
				file.create(stream, false, null);
			} else {
				file.setContents(stream, false, false, null);
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
	 * @see ILaunchConfigurationWorkingCopy#initializeDefaults(Object)
	 */
	public void initializeDefaults(Object object) throws CoreException {
		getDelegate().initializeDefaults(this, object);
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
	private void setOriginal(LaunchConfiguration original) throws CoreException {
		fOriginal = original;
		LaunchConfigurationInfo info = original.getInfo();
		setInfo(info.getCopy());
	}
	
	/**
	 * Sets the working copy info object for this working copy.
	 * 
	 * @param info a copy of attributes from this working copy's
	 * 	original launch configuration
	 */
	private void setInfo(LaunchConfigurationInfo info) {
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
	 */
	private void setDirty() {
		fDirty = true;
	}
		
	/**
	 * @see ILaunchConfigurationWorkingCopy#rename(String)
	 */
	public void rename(String name) {
		setNewName(name);
	}

	/**
	 * Sets the new name for this configuration.
	 * 
	 * @param name the new name for this configuration
	 */
	private void setNewName(String name) {
		fName = name;
		setDirty();
	}
	
	/**
	 * Returns the new name for this configuration, or
	 * <code>null</code> if this configuration is not
	 * to be renamed or created.
	 * 
	 * @return the new name for this configuration, or
	 *  <code>null</code> if this configuration is not
	 *  to be renamed or created
	 */
	private String getNewName() {
		return fName;
	}
	
	/**
	 * Returns the new project for this configuration, or
	 * <code>null</code> if this configuration is based
	 * on an existing configuration.
	 * 
	 * @return the new project for this configuration, or
	 *  <code>null</code> if this configuration is based
	 *  on an existing configuration
	 */
	private IProject getNewProject() {
		return fProject;
	}	
	
	/**
	 * Sets the new project for this configuration.
	 * 
	 * @param project the new project for this configuration
	 */
	private void setNewProject(IProject project) {
		fProject = project;
		setDirty();
	}
	
	/**
	 * @see ILaunchConfiguration#getProject()
	 */
	public IProject getProject() {
		if (getNewProject() == null) {
			return getOriginal().getProject();
		} else {
			return getNewProject();
		}
	}
	
	/**
	 * @see ILaunchConfiguration#getName()
	 */
	public String getName() {
		if (getNewName() == null) {
			return super.getName();
		} else {
			return getNewName();
		}
	}

	/**
	 * @see ILaunchConfigurationWorkingCopy#setLocal(boolean)
	 */
	public void setLocal(boolean local) {
		fLocal = local;
		if (!isNew() && (local != getOriginal().isLocal())) {
			setDirty();
		}
	}
	
	
	/**
	 * @see ILaunchConfiguration#isLocal()
	 */
	public boolean isLocal() {
		return fLocal;
	}	
	
	/**
	 * Returns the location this launch configuration will reside at
	 * when saved.
	 * 
	 * @see ILaunchConfiguration#getLocation()
	 */
	public IPath getLocation() {
		if (isRenamed() || isMoved()) {
			IPath path = null;
			if (isLocal()) {
				path = getProject().getPluginWorkingLocation(DebugPlugin.getDefault().getDescriptor());
			} else {
				path = getProject().getLocation();
			}
			path = path.append(".launches"); //$NON-NLS-1$
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
	 * Returns whether this working copy is new or has
	 * been renamed.
	 * 
	 * @return whether this working copy is new or has
	 *  been renamed
	 */
	protected boolean isRenamed() {
		return getNewName() != null;
	}
	
	/**
	 * Returns whether this working copy is new or if its
	 * local property is being changed from its original
	 * configuration.
	 * 
	 * @return whether this working copy is new or if its
	 *  local property is being changed from its original
	 *  configuration
	 */
	protected boolean isMoved() {
		return isNew() || (isLocal() != getOriginal().isLocal());
	}		
	
	/**
	 * A working copy cannot generate a memento.
	 * 
	 * @see ILaunchConfiguration#getMemento()
	 */
	public String getMemento() {
		return null;
	}	
	
}

