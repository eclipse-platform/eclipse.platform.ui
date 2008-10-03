/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

 
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import com.ibm.icu.text.MessageFormat;

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
	 * Handle to a parent working copy
	 * @since 3.3
	 */
	private LaunchConfigurationWorkingCopy fParent =  null;
	
	/**
	 * Working copy of attributes.
	 */
	private LaunchConfigurationInfo fInfo;
	
	/**
	 * Whether this working copy has been modified since
	 * it was created
	 */
	private boolean fDirty;
		
	/**
	 * Indicates whether this working copy has been explicitly renamed.
	 */
	private boolean fRenamed;
	
	/**
	 * Suppress change notification until created
	 */
	private boolean fSuppressChange ;
		
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
		super(original.getName(), original.getContainer());
		copyFrom(original);
		setOriginal(original);
		fSuppressChange = false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.LaunchConfiguration#initialize()
	 */
	protected void initialize() {
		fDirty = false;
		fRenamed = false;
		fSuppressChange = true;
		super.initialize();
	}
	
	/**
	 * Constructs a working copy of the specified launch configuration as its parent.
	 * 
	 * @param parent launch configuration to make
	 *  a working copy of
	 * @exception CoreException if unable to initialize this
	 *  working copy's attributes based on the original configuration
	 */
	protected LaunchConfigurationWorkingCopy(LaunchConfigurationWorkingCopy parent) throws CoreException {
		super(parent.getName(), parent.getContainer());
		copyFrom(parent);
		setOriginal((LaunchConfiguration) parent.getOriginal());
		fParent = parent;
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
		super(name, original.getContainer());
		copyFrom(original);
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
		super(name, container);
		setInfo(new LaunchConfigurationInfo());
		getInfo().setType(type);
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
	public synchronized ILaunchConfiguration doSave() throws CoreException {
		return doSave(new NullProgressMonitor());
	}

	/**
	 * Saves with progress.
	 * 
	 * @param monitor
	 * @return the saved <code>ILaunchConfiguration</code>
	 * @throws CoreException
	 * 
	 * @since 3.3
	 */
	public synchronized ILaunchConfiguration doSave(IProgressMonitor monitor) throws CoreException {
		SubMonitor lmonitor = SubMonitor.convert(monitor, 1);
		try {
		if (getParent() != null) {
			// save to parent working copy
			LaunchConfigurationWorkingCopy wc = (LaunchConfigurationWorkingCopy) getParent();
			if(isMoved()) {
				wc.rename(getName());
				wc.setContainer(getContainer());
			}
			wc.setAttributes(getInfo().getAttributes());
			updateMonitor(lmonitor, 1);
			return wc;
		}
		else {
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
						doSave0(pm);
					}
				};
				ResourcesPlugin.getWorkspace().run(wr, null, 0, lmonitor.newChild(1));
			} else {
				//file is persisted in the metadata not the workspace
				doSave0(lmonitor.newChild(1));
			}
			getLaunchManager().setMovedFromTo(null, null);
		}
		}
		finally {
			if(lmonitor != null) {
				lmonitor.done();
			}
		}
		return new LaunchConfiguration(getName(), getContainer());
	}
	
	/**
	 * Performs the actual saving of the launch configuration.
	 * @throws CoreException
	 */
	private void doSave0(IProgressMonitor monitor) throws CoreException {
		SubMonitor lmonitor = SubMonitor.convert(monitor, MessageFormat.format(DebugCoreMessages.LaunchConfigurationWorkingCopy_0, new String[] {getName()}), 2);
		try {
			// set up from/to information if this is a move
			boolean moved = (!isNew() && isMoved());
			if (moved) {
				ILaunchConfiguration to = new LaunchConfiguration(getName(), getContainer());
				ILaunchConfiguration from = getOriginal();
				getLaunchManager().setMovedFromTo(from, to);
			}
			ILaunchConfiguration orig = getOriginal();
			updateMonitor(lmonitor, 1);
			writeNewFile(lmonitor.newChild(1));
			// delete the old file if this is not a new configuration
			// or the file was renamed/moved
			if (moved) {
				orig.delete();
			}
			fDirty = false;
		}
		finally {
			if(lmonitor != null) {
				lmonitor.done();
			}
		}
	}
	
	/**
	 * Writes the new configuration information to a file.
	 * 
	 * @exception CoreException if writing the file fails
	 */
	protected void writeNewFile(IProgressMonitor monitor) throws CoreException {
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
		SubMonitor lmonitor = SubMonitor.convert(monitor, "", 5); //$NON-NLS-1$
		try {
			if (isLocal()) {
				// use java.io to update configuration file
				try {
					boolean added = false;
					lmonitor.subTask(DebugCoreMessages.LaunchConfigurationWorkingCopy_1);
					IFileStore file = getFileStore();
					if (file == null) {
						throw new DebugException(
								new Status(
								 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
								 DebugException.REQUEST_FAILED, DebugCoreMessages.LaunchConfigurationWorkingCopy_4, null 
								)
							);
					}
					IFileStore dir = file.getParent();
					dir.mkdir(EFS.SHALLOW, null);
					if (!file.fetchInfo().exists()) {
						added = true;
						updateMonitor(lmonitor, 1);
					}
					BufferedOutputStream stream = null;
					try {
						stream = new BufferedOutputStream(file.openOutputStream(EFS.NONE, null));
						stream.write(xml.getBytes("UTF8")); //$NON-NLS-1$
					}
					finally {
						if(stream != null) {
							stream.close();
						}
					}
					if (added) {
						getLaunchManager().launchConfigurationAdded(new LaunchConfiguration(getName(), getContainer()));
					} else {
						getLaunchManager().launchConfigurationChanged(new LaunchConfiguration(getName(), getContainer()));
					}
					//notify file saved
					updateMonitor(lmonitor, 1);
				} catch (IOException ie) {
					lmonitor.done();
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
				if (file == null) {
					lmonitor.done();
					throw new DebugException(
							new Status(
								 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
								 DebugException.REQUEST_FAILED, DebugCoreMessages.LaunchConfigurationWorkingCopy_5, null 
							));
				}
				IContainer dir = file.getParent();
				if (!dir.exists()) {
					lmonitor.done();
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
					lmonitor.done();
					throw new DebugException(
						new Status(
							 IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
							 DebugException.REQUEST_FAILED, DebugCoreMessages.LaunchConfigurationWorkingCopy_5, ue 
						));
				}
				SubMonitor smonitor = null;
				if (!file.exists()) {
					//create file input stream: work one unit in a sub monitor
					smonitor = lmonitor.newChild(1);
					smonitor.setTaskName(MessageFormat.format(DebugCoreMessages.LaunchConfigurationWorkingCopy_2, new String[] {getName()}));
					file.create(stream, false, smonitor);
				} else {
					// validate edit
					if (file.isReadOnly()) {
						IStatus status = ResourcesPlugin.getWorkspace().validateEdit(new IFile[] {file}, null);
						if (!status.isOK()) {
							lmonitor.done();
							throw new CoreException(status);
						}
					}				
					//set the contents of the file: work 1 unit in a sub monitor
					smonitor = lmonitor.newChild(1);
					smonitor.setTaskName(MessageFormat.format(DebugCoreMessages.LaunchConfigurationWorkingCopy_3, new String[] {getName()}));
					file.setContents(stream, true, false, smonitor);
				}
			}
		}
		finally {
			if(lmonitor != null) {
				lmonitor.done();
			}
		}
	}

	/**
	 * Updates the given monitor with the given tick count and polls for cancellation. If the monitor
	 * is cancelled an {@link OperationCanceledException} is thrown
	 * @param monitor
	 * @param ticks
	 * @throws OperationCanceledException
	 */
	private void updateMonitor(IProgressMonitor monitor, int ticks) throws OperationCanceledException {
		if(monitor != null) {
			monitor.worked(ticks);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
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
		ILaunchConfiguration config = fOriginal;
		ILaunchConfigurationWorkingCopy parent = fParent;
		while(parent != null) {
			config = parent.getOriginal();
			parent = parent.getParent();
		}
		return config;
	}
	
	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationWorkingCopy#getParent()
	 */
	public ILaunchConfigurationWorkingCopy getParent() {
		return fParent;
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
		fDirty = false;
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
	 * @see org.eclipse.debug.core.ILaunchConfigurationWorkingCopy#setModes(java.util.Set)
	 */
	public void setModes(Set modes) {
		getInfo().setAttribute(ATTR_LAUNCH_MODES, (modes.size() > 0 ? modes : null));
		setDirty();
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationWorkingCopy#addModes(java.util.Set)
	 */
	public void addModes(Set modes) {
		try {
			Set opts = getModes();
			if(opts.addAll(modes)) {
				getInfo().setAttribute(ATTR_LAUNCH_MODES, opts);
				setDirty();
			}
		} 
		catch (CoreException e) {
			DebugPlugin.log(e);
		}
	}
	
	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationWorkingCopy#removeModes(java.util.Set)
	 */
	public void removeModes(Set options) {
		try {
			Set opts = getModes();
			if(opts.removeAll(options)) {
				getInfo().setAttribute(ATTR_LAUNCH_MODES, (opts.size() < 1 ? null : opts));
				setDirty();
			}
		} 
		catch (CoreException e) {
			DebugPlugin.log(e);
		}
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
	protected void setName(String name) {
		super.setName(name);
		setDirty();
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
		if (equalOrNull(getContainer(), container)) {
			return;
		}
		super.setContainer(container);
		setDirty();
	}
	
	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationWorkingCopy#setAttributes(java.util.Map)
	 */
	public void setAttributes(Map attributes) {
		getInfo().setAttributes(attributes);
		setDirty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationWorkingCopy#setResource(org.eclipse.core.resources.IResource)
	 */
	public void setMappedResources(IResource[] resources) {
		ArrayList paths = null;
		ArrayList types = null;
		if(resources != null && resources.length > 0) {
			paths = new ArrayList(resources.length);
			types = new ArrayList(resources.length);
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if(resource != null) {
					paths.add(resource.getFullPath().toPortableString());
					types.add(new Integer(resource.getType()).toString());
				}
			}
		}
		setAttribute(LaunchConfiguration.ATTR_MAPPED_RESOURCE_PATHS, paths);
		setAttribute(LaunchConfiguration.ATTR_MAPPED_RESOURCE_TYPES, types);
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationWorkingCopy#setPreferredLaunchDelegate(java.util.Set, java.lang.String)
	 */
	public void setPreferredLaunchDelegate(Set modes, String delegateId) {
		if(modes != null) {
			try {
				Map delegates = getAttribute(LaunchConfiguration.ATTR_PREFERRED_LAUNCHERS, (Map)null);
					//copy map to avoid pointer issues
					Map map = new HashMap();
					if(delegates != null) {
						map.putAll(delegates);
					}
					if(delegateId == null) {
						map.remove(modes.toString());
					}
					else {
						map.put(modes.toString(), delegateId);
					}
					setAttribute(LaunchConfiguration.ATTR_PREFERRED_LAUNCHERS, map);
			}
			catch (CoreException ce) {DebugPlugin.log(ce);}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.LaunchConfiguration#getWorkingCopy()
	 * CONTEXTLAUNCHING
	 */
	public ILaunchConfigurationWorkingCopy getWorkingCopy() throws CoreException {
		return new LaunchConfigurationWorkingCopy(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationWorkingCopy#removeAttribute(java.lang.String)
	 */
	public Object removeAttribute(String attributeName) {
		return getInfo().removeAttribute(attributeName);
	}
}

