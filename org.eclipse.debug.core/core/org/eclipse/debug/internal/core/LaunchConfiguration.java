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

 
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.sourcelookup.IPersistableSourceLocator2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Launch configuration handle.
 * 
 * @see ILaunchConfiguration
 */
public class LaunchConfiguration extends PlatformObject implements ILaunchConfiguration {
	
	/**
	 * Launch configuration attribute that specifies the resource mapped to it.
	 * Not all launch configurations will have a mapped resource unless migrated
	 * (as of 3.2). The mapping is provided to put tighter constraints on launch configuration
	 * choices presented to the user during context sensitive launching and for project state changes (i.e. launch
	 * configuration not available for close projects).
	 * 
	 * @since 3.2
	 */
	public static final String ATTR_MAPPED_RESOURCE = DebugPlugin.getUniqueIdentifier() + ".MAPPED_RESOURCE"; //$NON-NLS-1$
	
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
	 * Constructs a launch configuration from the given
	 * memento.
	 * 
	 * @param memento launch configuration memento
	 * @exception CoreException if the memento is invalid or
	 * 	an exception occurs reading the memento
	 */
	protected LaunchConfiguration(String memento) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			StringReader reader = new StringReader(memento);
			InputSource source = new InputSource(reader);
			root = parser.parse(source).getDocumentElement();
			
			String localString = root.getAttribute("local"); //$NON-NLS-1$
			String path = root.getAttribute("path"); //$NON-NLS-1$

			String message = null;				
			if (path == null) {
				message = DebugCoreMessages.LaunchConfiguration_Invalid_launch_configuration_memento__missing_path_attribute_3; 
			} else if (localString == null) {
				message = DebugCoreMessages.LaunchConfiguration_Invalid_launch_configuration_memento__missing_local_attribute_4; 
			}
			if (message != null) {
				IStatus s = newStatus(message, DebugException.INTERNAL_ERROR, null);
				throw new CoreException(s);
			}
			
			IPath location = null;
			boolean local = (Boolean.valueOf(localString)).booleanValue();
			if (local) {
				location = LaunchManager.LOCAL_LAUNCH_CONFIGURATION_CONTAINER_PATH.append(path);
			} else {
				location = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path)).getLocation();
			}
			setLocation(location);
			if (location == null) {
				IStatus s = newStatus(MessageFormat.format(DebugCoreMessages.LaunchConfiguration_Unable_to_restore_location_for_launch_configuration_from_memento___0__1, new String[]{path}), DebugPlugin.INTERNAL_ERROR, null); 
				throw new CoreException(s);
			}
			return;
		} catch (ParserConfigurationException e) {
			ex = e;			
		} catch (SAXException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		}
		IStatus s = newStatus(DebugCoreMessages.LaunchConfiguration_Exception_occurred_parsing_memento_5, DebugException.INTERNAL_ERROR, ex); 
		throw new CoreException(s);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#contentsEqual(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean contentsEqual(ILaunchConfiguration object) {
		try {
			if (object instanceof LaunchConfiguration) {
				LaunchConfiguration otherConfig = (LaunchConfiguration) object;
				return getName().equals(otherConfig.getName())
				 	 && getType().equals(otherConfig.getType())
				 	 && getLocation().equals(otherConfig.getLocation())
					 && getInfo().equals(otherConfig.getInfo());
			}
			return false;
		} catch (CoreException ce) {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#copy(java.lang.String)
	 */
	public ILaunchConfigurationWorkingCopy copy(String name) throws CoreException {
		ILaunchConfigurationWorkingCopy copy = new LaunchConfigurationWorkingCopy(this, name);
		return copy;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#delete()
	 */
	public void delete() throws CoreException {
		if (exists()) {
			if (isLocal()) {
				if (!(getLocation().toFile().delete())) {
					throw new DebugException(
						new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
						 DebugException.REQUEST_FAILED, DebugCoreMessages.LaunchConfiguration_Failed_to_delete_launch_configuration__1, null) 
					);
				}
				// manually update the launch manager cache since there
				// will be no resource delta
				getLaunchManager().launchConfigurationDeleted(this);
			} else {
				// delete the resource using IFile API such that
				// resource deltas are fired.
				IFile file = getFile();
				if (file != null) {
					// validate edit
					if (file.isReadOnly()) {
						IStatus status = ResourcesPlugin.getWorkspace().validateEdit(new IFile[] {file}, null);
						if (!status.isOK()) {
							throw new CoreException(status);
						}
					}
					file.delete(true, null);
				} else {
					// Error - the exists test passed, but could not locate file 
				}
			}
		}
	}

	/**
	 * Returns whether this configuration is equal to the
	 * given configuration. Two configurations are equal if
	 * they are stored in the same location (and neither one
	 * is a working copy).
	 * 
	 * @return whether this configuration is equal to the
	 *  given configuration
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (object instanceof ILaunchConfiguration) {
			if (isWorkingCopy()) {
				return this == object;
			} 
			ILaunchConfiguration config = (ILaunchConfiguration) object;
			if (!config.isWorkingCopy()) {
				return config.getLocation().equals(getLocation());
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#exists()
	 */
	public boolean exists() {
		return getLocation().toFile().exists();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getAttribute(java.lang.String, boolean)
	 */
	public boolean getAttribute(String attributeName, boolean defaultValue) throws CoreException {
		return getInfo().getBooleanAttribute(attributeName, defaultValue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getAttribute(java.lang.String, int)
	 */
	public int getAttribute(String attributeName, int defaultValue) throws CoreException {
		return getInfo().getIntAttribute(attributeName, defaultValue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getAttribute(java.lang.String, java.util.List)
	 */
	public List getAttribute(String attributeName, List defaultValue) throws CoreException {
		return getInfo().getListAttribute(attributeName, defaultValue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getAttribute(java.lang.String, java.util.Map)
	 */
	public Map getAttribute(String attributeName, Map defaultValue) throws CoreException {
		return getInfo().getMapAttribute(attributeName, defaultValue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getAttribute(java.lang.String, java.lang.String)
	 */
	public String getAttribute(String attributeName, String defaultValue) throws CoreException {
		return getInfo().getStringAttribute(attributeName, defaultValue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getAttributes()
	 */
	public Map getAttributes() throws CoreException {
		LaunchConfigurationInfo info = getInfo();
		return info.getAttributes();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getCategory()
	 */
	public String getCategory() throws CoreException {
		return getType().getCategory();
	}

	/**
	 * Returns the container this launch configuration is 
	 * stored in, or <code>null</code> if this launch configuration
	 * is stored locally.
	 * 
	 * @return the container this launch configuration is 
	 * stored in, or <code>null</code> if this launch configuration
	 * is stored locally
	 */
	protected IContainer getContainer() {
		IFile file = getFile();
		if (file != null) {
			return file.getParent();
		}
		return null;
	}

	/**
	 * Returns the launch configuration delegate for this
	 * launch configuration, for the specified launch mode.
	 * 
	 * @param mode launch mode
	 * @return launch configuration delegate
	 * @exception CoreException if the delegate was unable
	 *  to be created
	 */
	protected ILaunchConfigurationDelegate getDelegate(String mode) throws CoreException {
		return getType().getDelegate(mode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getFile()
	 */
	public IFile getFile() {
		if (isLocal()) {
			return null;
		}
		IFile[] files= ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(getLocation());
		if (files.length > 0) {
			return files[0];
		}
		return null;
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
	 * Returns the last segment from the location
	 * @return the last segment from the location
	 */
	private String getLastLocationSegment() {
		String name = getLocation().lastSegment();
		if (name.length() > LAUNCH_CONFIGURATION_FILE_EXTENSION.length()) {
			name = name.substring(0, name.length() - (LAUNCH_CONFIGURATION_FILE_EXTENSION.length() + 1));
		}
		return name;
	}
	
	/**
	 * Returns the launch manager
	 * 
	 * @return launch manager
	 */
	protected LaunchManager getLaunchManager() {
		return (LaunchManager)DebugPlugin.getDefault().getLaunchManager();
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getLocation()
	 */
	public IPath getLocation() {
		return fLocation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getResource()
	 */
	public IResource[] getMappedResources() throws CoreException {
		List name = getInfo().getListAttribute(ATTR_MAPPED_RESOURCE, new ArrayList());
		ArrayList list = new ArrayList();
		IResource res = null;
		for(int i = 0; i < name.size(); i++) {
			res = ResourcesPlugin.getWorkspace().getRoot().findMember(name.get(i).toString());
			if(res != null) {
				list.add(res);
			}
		}
		return (IResource[])list.toArray(new IResource[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getMemento()
	 */
	public String getMemento() throws CoreException {
		IPath relativePath = null;
		if (isLocal()) {
			IPath rootPath = LaunchManager.LOCAL_LAUNCH_CONFIGURATION_CONTAINER_PATH;
			IPath configPath = getLocation();
			relativePath = configPath.removeFirstSegments(rootPath.segmentCount());
			relativePath = relativePath.setDevice(null);
		} else {
			IFile file = getFile();
			if (file == null) {
				// cannot generate memento - missing file
				IStatus status = newStatus(MessageFormat.format(DebugCoreMessages.LaunchConfiguration_Unable_to_generate_memento_for__0___shared_file_does_not_exist__1, new String[]{getName()}), DebugException.INTERNAL_ERROR, null); 
				throw new CoreException(status); 
			}
			relativePath = getFile().getFullPath();
		}
		Exception e= null;
		try {
			Document doc = LaunchManager.getDocument();
			Element node = doc.createElement("launchConfiguration"); //$NON-NLS-1$
			doc.appendChild(node);
			node.setAttribute("local", (Boolean.valueOf(isLocal())).toString()); //$NON-NLS-1$
			node.setAttribute("path", relativePath.toString()); //$NON-NLS-1$
			return LaunchManager.serializeDocument(doc);
		} catch (IOException ioe) {
			e= ioe;
		} catch (ParserConfigurationException pce) {
			e= pce;
		} catch (TransformerException te) {
			e= te;
		}
		if (e != null) {
			IStatus status = newStatus(DebugCoreMessages.LaunchConfiguration_Exception_occurred_creating_launch_configuration_memento_9, DebugException.INTERNAL_ERROR,  e); 
			throw new CoreException(status);
		}
		//execution will never reach here
		return null;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getName()
	 */
	public String getName() {
		return getLastLocationSegment();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getType()
	 */
	public ILaunchConfigurationType getType() throws CoreException {
		return getInfo().getType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getWorkingCopy()
	 */
	public ILaunchConfigurationWorkingCopy getWorkingCopy() throws CoreException {
		return new LaunchConfigurationWorkingCopy(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getLocation().hashCode();
	}

	/**
	 * Set the source locator to use with the launch, if specified 
	 * by this configuration.
	 * 
	 * @param launch the launch on which to set the source locator
	 */
	protected void initializeSourceLocator(ILaunch launch) throws CoreException {
		if (launch.getSourceLocator() == null) {
			String type = getAttribute(ATTR_SOURCE_LOCATOR_ID, (String)null);
			if (type == null) {
				type = getType().getSourceLocatorId();
			}
			if (type != null) {
				IPersistableSourceLocator locator = getLaunchManager().newSourceLocator(type);
				String memento = getAttribute(ATTR_SOURCE_LOCATOR_MEMENTO, (String)null);
				if (memento == null) {
					locator.initializeDefaults(this);
				} else {
					if(locator instanceof IPersistableSourceLocator2)
						((IPersistableSourceLocator2)locator).initializeFromMemento(memento, this);
					else
						locator.initializeFromMemento(memento);
				}
				launch.setSourceLocator(locator);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#isLocal()
	 */
	public boolean isLocal() {
		IPath localPath = LaunchManager.LOCAL_LAUNCH_CONFIGURATION_CONTAINER_PATH;
		return localPath.isPrefixOf(getLocation());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#isMigrationCandidate()
	 */
	public boolean isMigrationCandidate() throws CoreException {
		return ((LaunchConfigurationType)getType()).isMigrationCandidate(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#isWorkingCopy()
	 */
	public boolean isWorkingCopy() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#launch(java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ILaunch launch(String mode, IProgressMonitor monitor) throws CoreException {
		return launch(mode, monitor, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#launch(java.lang.String, org.eclipse.core.runtime.IProgressMonitor, boolean)
	 */
	public ILaunch launch(String mode, IProgressMonitor monitor, boolean build) throws CoreException {
	    return launch(mode, monitor, build, true);
	}

	/* (non-Javadoc)
     * @see org.eclipse.debug.core.ILaunchConfiguration#launch(java.lang.String, org.eclipse.core.runtime.IProgressMonitor, boolean, boolean)
     */
    public ILaunch launch(String mode, IProgressMonitor monitor, boolean build, boolean register) throws CoreException {
		// bug 28245 - force the delegate to load in case it is interested in launch notifications
		ILaunchConfigurationDelegate delegate= getDelegate(mode);
		ILaunchConfigurationDelegate2 delegate2 = null;
		if (delegate instanceof ILaunchConfigurationDelegate2) {
			delegate2 = (ILaunchConfigurationDelegate2) delegate;
		}
		// allow the delegate to provide a launch implementation
		ILaunch launch = null;
		if (delegate2 != null) {
			launch = delegate2.getLaunch(this, mode);
		}
		if (launch == null) {
			launch = new Launch(this, mode, null);
		} else {
			// ensure the launch mode is valid
			if (!mode.equals(launch.getLaunchMode())) {
				IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, 
						MessageFormat.format(DebugCoreMessages.LaunchConfiguration_13, new String[]{mode, launch.getLaunchMode()}), null); 
				throw new CoreException(status);
			}
		}
		
		boolean captureOutput = getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
		if(!captureOutput) {
		    launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, "false"); //$NON-NLS-1$
		} else {
		    launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, null);
		}
		
		
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}		
		// perform initial pre-launch sanity checks
		if (delegate2 != null) {
			if (!(delegate2.preLaunchCheck(this, mode, monitor))) {
				// canceled
				monitor.setCanceled(true);
				return launch;
			}
		}
		// preform pre-launch build
		IProgressMonitor subMonitor = monitor;
		if (build) {
			subMonitor = new SubProgressMonitor(monitor, 100);
			if (delegate2 != null) {
				build = delegate2.buildForLaunch(this, mode, subMonitor);
			}
			if (build) {
				ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, subMonitor);				
			}
			subMonitor = new SubProgressMonitor(monitor, 100);
		}
		// final validation
		if (delegate2 != null) {
			if (!(delegate2.finalLaunchCheck(this, mode, subMonitor))) {
				// canceled
				monitor.setCanceled(true);
				return launch;
			}
		}
		
		if (register) {
		    getLaunchManager().addLaunch(launch);
		}
		try {
			initializeSourceLocator(launch);
			delegate.launch(this, mode, launch, subMonitor);
		} catch (CoreException e) {
			// if there was an exception, and the launch is empty, remove it
			if (!launch.hasChildren()) {
				getLaunchManager().removeLaunch(launch);
			}
			throw e;
		}
		if (monitor.isCanceled()) {
			getLaunchManager().removeLaunch(launch);
		}
		return launch;
    }
	
    /* (non-Javadoc)
     * @see org.eclipse.debug.core.ILaunchConfiguration#migrate()
     */
    public void migrate() throws CoreException {
		((LaunchConfigurationType)getType()).migrate(this);
	}

	/**
	 * Creates and returns a new error status based on 
	 * the given message, code, and exception.
	 * 
	 * @param message error message
	 * @param code error code
	 * @param e exception or <code>null</code>
	 * @return status
	 */
	protected IStatus newStatus(String message, int code, Throwable e) {
		return new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), code, message, e);
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#supportsMode(java.lang.String)
	 */
	public boolean supportsMode(String mode) throws CoreException {
		return getType().supportsMode(mode);
	}
	
}//end class

