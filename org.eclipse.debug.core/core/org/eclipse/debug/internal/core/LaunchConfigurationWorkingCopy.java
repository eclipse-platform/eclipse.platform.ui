/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

 
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
			boolean useRunnable= true;
			if (isLocal()) {
				if (isMoved()) {
					// If this config was moved from a shared location, saving
					// it will delete the original from the workspace. Use runnable.
					useRunnable= !isNew() && !getOriginal().isLocal();
				} else {
					useRunnable= false;
				}
			}

			if (useRunnable) {
				IWorkspaceRunnable wr = new IWorkspaceRunnable() {
					public void run(IProgressMonitor pm) throws CoreException {
						doSave0();
					}
				};
				
				ResourcesPlugin.getWorkspace().run(wr, null, 0, null);
			} else {
				//file is persisted in the metadata not the workspace
				doSave0();
			}

			getLaunchManager().setMovedFromTo(null, null);
		}

		return new LaunchConfiguration(getLocation());
	}

	
	private void doSave0() throws CoreException {
		// set up from/to information if this is a move
		boolean moved = (!isNew() && isMoved());
		if (moved) {
			ILaunchConfiguration to = new LaunchConfiguration(getLocation());
			ILaunchConfiguration from = getOriginal();
			getLaunchManager().setMovedFromTo(from, to);
		}
		// delete the old file if this is not a new configuration
		// or the file was renamed/moved
		if (moved) {
			getOriginal().delete();
		}
		// write the new file
		writeNewFile();
		resetDirty();
	}

	/**
	 * Writes the new configuration information to a file.
	 * 
	 * @exception CoreException if writing the file fails
	 */
	protected void writeNewFile() throws CoreException {
		String xml = null;
		Exception e= null;
		try {
			xml = getInfo().getAsXML();
		} catch (IOException ioe) {
			e= ioe;			
		} catch (ParserConfigurationException pce) {
			e= pce;
		} catch (TransformerException te) {
			e= te;		
		}
		if (e != null) {
			throw new DebugException(
				new Status(
					IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
					DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.LaunchConfigurationWorkingCopy__0__occurred_generating_launch_configuration_XML__1, new String[]{e.toString()}), null 
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
			} catch (IOException ie) {
				throw new DebugException(
					new Status(
					 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
					 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.LaunchConfigurationWorkingCopy__0__occurred_generating_launch_configuration_XML__1, new String[]{ie.toString()}), null 
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
					 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
					 DebugException.REQUEST_FAILED, DebugCoreMessages.LaunchConfigurationWorkingCopy_Specified_container_for_launch_configuration_does_not_exist_2, null 
					)
				);				
			}
			ByteArrayInputStream stream = null;
			try {
				stream = new ByteArrayInputStream(xml.getBytes("UTF8")); //$NON-NLS-1$
			} catch (UnsupportedEncodingException ue) {
				throw new DebugException(
					new Status(
						 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
						 DebugException.REQUEST_FAILED, DebugCoreMessages.LaunchConfigurationWorkingCopy_5, null 
					));
			}
			if (!file.exists()) {
				file.create(stream, false, null);
			} else {
				// validate edit
				if (file.isReadOnly()) {
					IStatus status = ResourcesPlugin.getWorkspace().validateEdit(new IFile[] {file}, null);
					if (!status.isOK()) {
						throw new CoreException(status);
					}
				}				
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
		getInfo().setAttribute(attributeName, Boolean.valueOf(value));
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
	 * @param original the launch configuration this working
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
	 * @param original the launch configuration this working 
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
			getLaunchManager().getConfigurationNotifier().notify(this, LaunchManager.CHANGED);
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
		} 
		return getOriginal().getLocation();
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
		} 
		return !newContainer.equals(originalContainer);
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
	
	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationWorkingCopy#setAttributes(java.util.Map)
	 */
	public void setAttributes(Map attributes) {
		getInfo().setAttributes(attributes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationWorkingCopy#setResource(org.eclipse.core.resources.IResource)
	 */
	public void setMappedResources(IResource[] resource) {
		ArrayList resources = null;
		if(resource != null) {
			resources = new ArrayList(resource.length);
			for (int i = 0; i < resource.length; i++) {
				if(resource[i] != null) {
					resources.add(resource[i].getFullPath().toPortableString());
				}
			}
			setAttribute(LaunchConfiguration.ATTR_MAPPED_RESOURCE, resources);
		}
		setAttribute(LaunchConfiguration.ATTR_MAPPED_RESOURCE, resources);
	}//end setResource

}//end class

