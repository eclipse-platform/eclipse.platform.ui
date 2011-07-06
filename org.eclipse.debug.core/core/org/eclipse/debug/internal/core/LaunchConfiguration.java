/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sascha Radike - bug 56642
 *******************************************************************************/
package org.eclipse.debug.internal.core;

 
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
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
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.IStatusHandler;
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

import com.ibm.icu.text.MessageFormat;

/**
 * Launch configuration handle.
 * 
 * @see ILaunchConfiguration
 */
public class LaunchConfiguration extends PlatformObject implements ILaunchConfiguration {
	
	/**
	 * Launch configuration attribute that specifies the resources paths mapped to it.
	 * Not all launch configurations will have a mapped resource unless migrated.
	 * Value is a list of resource paths stored as portable strings, or <code>null</code>
	 * if none.
	 * 
	 * @since 3.2
	 */
	public static final String ATTR_MAPPED_RESOURCE_PATHS = DebugPlugin.getUniqueIdentifier() + ".MAPPED_RESOURCE_PATHS"; //$NON-NLS-1$
	
	/**
	 * Launch configuration attribute that specifies the resources types mapped to it.
	 * Not all launch configurations will have a mapped resource unless migrated.
	 * Value is a list of resource type integers, or <code>null</code> if none.
	 * 
	 * @since 3.2
	 */	
	public static final String ATTR_MAPPED_RESOURCE_TYPES = DebugPlugin.getUniqueIdentifier() + ".MAPPED_RESOURCE_TYPES"; //$NON-NLS-1$
	
	/**
	 * The launch modes set on this configuration.
	 * 
	 * @since 3.3
	 */
	public static final String ATTR_LAUNCH_MODES = DebugPlugin.getUniqueIdentifier() + ".LAUNCH_MODES"; //$NON-NLS-1$
	
	/**
	 * Launch configuration attribute storing a list 
	 * of preferred launchers for associated mode sets.
	 * This attribute is a list of launchers stored by mode set
	 * and relating to the id of the preferred launcher, which happens to be an <code>ILaunchDelegate</code>
	 * 
	 * @since 3.3
	 */
	public static final String ATTR_PREFERRED_LAUNCHERS = DebugPlugin.getUniqueIdentifier() + ".preferred_launchers"; //$NON-NLS-1$	
	
	/**
	 * Status handler to prompt in the UI thread
	 * 
	 * @since 3.3
	 */
	protected static final IStatus promptStatus = new Status(IStatus.INFO, "org.eclipse.debug.ui", 200, "", null);  //$NON-NLS-1$//$NON-NLS-2$
	
	/**
	 * Status handler to prompt the user to resolve the missing launch delegate issue
	 * @since 3.3
	 */
	protected static final IStatus delegateNotAvailable = new Status(IStatus.INFO, "org.eclipse.debug.core", 226, "", null); //$NON-NLS-1$ //$NON-NLS-2$
	
	/**
	 * Status handle to prompt the user to resolve duplicate launch delegates being detected
	 * 
	 *  @since 3.3
	 */
	protected static final IStatus duplicateDelegates = new Status(IStatus.INFO, "org.eclipse.debug.core", 227, "", null);  //$NON-NLS-1$//$NON-NLS-2$
	
	/**
	 * This configuration's name
	 * @since 3.5
	 */
	private String fName;
	
	/**
	 * The container this configuration is stored in or <code>null</code> if stored locally
	 * with workspace metadata.
	 * @since 3.5
	 */
	private IContainer fContainer;

	/**
	 * Constructs a launch configuration with the given name. The configuration
	 * is stored in the given container or locally with workspace metadata if
	 * the specified container is <code>null</code>.
	 * 
	 * @param name launch configuration name
	 * @param container parent container or <code>null</code>
	 * @since 3.5
	 */
	protected LaunchConfiguration(String name, IContainer container) {
		initialize();
		setName(name);
		setContainer(container);
	}

	/**
	 * Initialize any state variables - called first in the constructor.
	 * Subclasses must override as appropriate.
	 */
	protected void initialize() {	
	}
	
	/**
	 * Constructs a launch configuration on the given workspace file.
	 * 
	 * @param file workspace .launch file
	 * @since 3.5
	 */
	protected LaunchConfiguration(IFile file) {
		this(getSimpleName(file.getName()), file.getParent());
	}
	
	/**
	 * Given a name that ends with .launch, return the simple name of the configuration.
	 * 
	 * @param fileName the name to parse
	 * @return simple name
	 * @since 3.5
	 */
	protected static String getSimpleName(String fileName) {
		IPath path = new Path(fileName);
		if(ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION.equals(path.getFileExtension())) {
			return path.removeFileExtension().toString();
		}
		return fileName;
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
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			StringReader reader = new StringReader(memento);
			InputSource source = new InputSource(reader);
			root = parser.parse(source).getDocumentElement();
			
			String localString = root.getAttribute(IConfigurationElementConstants.LOCAL); 
			String path = root.getAttribute(IConfigurationElementConstants.PATH);

			String message = null;				
			if (path == null || IInternalDebugCoreConstants.EMPTY_STRING.equals(path)) {
				message = DebugCoreMessages.LaunchConfiguration_18;  
			} else if (localString == null || IInternalDebugCoreConstants.EMPTY_STRING.equals(localString)) {
				message = DebugCoreMessages.LaunchConfiguration_19;  
			}
			if (message != null) {
				throw new CoreException(newStatus(message, DebugException.INTERNAL_ERROR, null));
			}
			
			
			boolean local = (Boolean.valueOf(localString)).booleanValue();
			IPath iPath = new Path(path);
			String name = getSimpleName(iPath.lastSegment());
			IContainer container = null;
			if (!local) {
				container = ResourcesPlugin.getWorkspace().getRoot().getFile(iPath).getParent();
			}
			setName(name);
			setContainer(container);
			return;
		} catch (ParserConfigurationException e) {
			ex = e;			
		} catch (SAXException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		}
		IStatus s = newStatus(DebugCoreMessages.LaunchConfiguration_17, DebugException.INTERNAL_ERROR, ex); 
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
				 	 && equalOrNull(getContainer(), otherConfig.getContainer())
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
			IFile file = getFile();
			if (file == null) {
				IFileStore store = getFileStore();
				if (store != null) {
					store.delete(EFS.NONE, null);
					if ((store.fetchInfo().exists())) {
						throw new DebugException(
							new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(),
							 DebugException.REQUEST_FAILED, DebugCoreMessages.LaunchConfiguration_Failed_to_delete_launch_configuration__1, null) 
						);
					}
				}
			} else {
				// Delete the resource using IFile API such that
				// resource deltas are fired.
				// First do validate edit to ensure the resource is local
				if (file.isReadOnly()) {
					IStatus status = ResourcesPlugin.getWorkspace().validateEdit(new IFile[] {file}, null);
					if (!status.isOK()) {
						throw new CoreException(status);
					}
				}
				file.delete(true, null);
			}
			// update the launch manager cache synchronously
			getLaunchManager().launchConfigurationDeleted(this);
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
			LaunchConfiguration config = (LaunchConfiguration) object;
			if (!config.isWorkingCopy()) {
				return getName().equals(config.getName()) &&
					equalOrNull(getContainer(), config.getContainer());
			}
		}
		return false;
	}
	
	/**
	 * Returns whether the given objects are equal or both <code>null</code>.
	 * 
	 * @param o1 the object
	 * @param o2 the object to be compared to o1
	 * @return whether the given objects are equal or both <code>null</code>
	 * @since 3.5
	 */
	protected boolean equalOrNull(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		} else if (o2 != null) {
			return o1.equals(o2);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#exists()
	 */
	public boolean exists() {
		IFile file = getFile();
		if (file != null) {
			return file.exists();
		}
		try {
			IFileStore store = getFileStore();
			if (store != null) {
				return store.fetchInfo().exists();
			}
		} catch (CoreException e) {
		}
		return false;
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

	/**
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getAttribute(java.lang.String, java.util.Set)
	 */
	public Set getAttribute(String attributeName, Set defaultValue) throws CoreException {
		return getInfo().getSetAttribute(attributeName, defaultValue);
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getFile()
	 */
	public IFile getFile() {
		IContainer container = getContainer();
		if (container != null) {
			return container.getFile(new Path(getFileName()));
		}
		return null;
	}
	
	/**
	 * Returns the simple file name of this launch configuration.
	 * 
	 * @return the simple file name of this launch configuration - for example
	 *  	"Abc.launch"
	 */
	protected String getFileName() {
		StringBuffer buf = new StringBuffer(getName());
		buf.append('.');
		buf.append(ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION);
		return buf.toString();
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
		try {
			IFileStore store = getFileStore();
			if (store != null) {
				File localFile = store.toLocalFile(EFS.NONE, null);
				if (localFile != null) {
					return new Path(localFile.getAbsolutePath());
				}
			}
		} catch (CoreException e) {
		}
		return null;
	}
	
	/**
	 * Returns the file store this configuration is persisted in or <code>null</code> if
	 * a file store cannot be derived. The file may or may not exist. If this configuration
	 * is in a project that is closed or does not exist, <code>null</code> is returned.
	 * 
	 * @return file store this configuration is persisted in or <code>null</code>
	 * @throws CoreException if a problem is encountered
	 * @since 3.5
	 */
	public IFileStore getFileStore() throws CoreException {
		if (isLocal()) {
			return EFS.getLocalFileSystem().fromLocalFile(
				LaunchManager.LOCAL_LAUNCH_CONFIGURATION_CONTAINER_PATH.append(getFileName()).toFile());
		}
		URI uri = getFile().getLocationURI();
		if (uri != null) {
			return EFS.getStore(uri);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getResource()
	 */
	public IResource[] getMappedResources() throws CoreException {
		List paths = getAttribute(ATTR_MAPPED_RESOURCE_PATHS, (List)null);
		if (paths == null || paths.size() == 0) {
			return null;
		}
		List types = getAttribute(ATTR_MAPPED_RESOURCE_TYPES, (List)null);
		if (types == null || types.size() != paths.size()) {
			throw new CoreException(newStatus(DebugCoreMessages.LaunchConfiguration_0, DebugPlugin.ERROR, null)); 
		}
		ArrayList list = new ArrayList();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for(int i = 0; i < paths.size(); i++) {
			String pathStr = (String) paths.get(i);
			String typeStr= (String) types.get(i);
			int type = -1;
			try {
				type = Integer.decode(typeStr).intValue();
			} catch (NumberFormatException e) {
				throw new CoreException(newStatus(DebugCoreMessages.LaunchConfiguration_0, DebugPlugin.ERROR, e)); 
			}
			IPath path = Path.fromPortableString(pathStr);
			IResource res = null;
			switch (type) {
			case IResource.FILE:
				res = root.getFile(path);
				break;
			case IResource.PROJECT:
				res = root.getProject(pathStr);
				break;
			case IResource.FOLDER:
				res = root.getFolder(path);
				break;
			case IResource.ROOT:
				res = root;
				break;
			default:
				throw new CoreException(newStatus(DebugCoreMessages.LaunchConfiguration_0, DebugPlugin.ERROR, null));
			}
			if(res != null) {
				list.add(res);
			}
		}
		if (list.isEmpty()) {
			return null;
		}
		return (IResource[])list.toArray(new IResource[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getMemento()
	 */
	public String getMemento() throws CoreException {
		IPath relativePath = null;
		IFile file = getFile();
		boolean local = true;
		if (file == null) {
			relativePath = new Path(getName());
		} else {
			local = false;
			relativePath = file.getFullPath();
		}
		Exception e= null;
		try {
			Document doc = LaunchManager.getDocument();
			Element node = doc.createElement(IConfigurationElementConstants.LAUNCH_CONFIGURATION);
			doc.appendChild(node);
			node.setAttribute(IConfigurationElementConstants.LOCAL, (Boolean.valueOf(local)).toString());
			node.setAttribute(IConfigurationElementConstants.PATH, relativePath.toString());
			return LaunchManager.serializeDocument(doc);
		} catch (IOException ioe) {
			e= ioe;
		} catch (ParserConfigurationException pce) {
			e= pce;
		} catch (TransformerException te) {
			e= te;
		}
		IStatus status = newStatus(DebugCoreMessages.LaunchConfiguration_16, DebugException.INTERNAL_ERROR,  e);  
		throw new CoreException(status);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getName()
	 */
	public String getName() {
		return fName;
	}
	
	/**
	 * Returns the container this configuration is stored in, or <code>null</code>
	 * if this configuration is local.
	 * 
	 * @return the container this configuration is stored in, or <code>null</code>
	 * if this configuration is local
	 * @since 3.5
	 */
	protected IContainer getContainer() {
		return fContainer;
	}		

	public Set getModes() throws CoreException {
		Set options = getAttribute(ATTR_LAUNCH_MODES, (Set)null); 
		return (options != null ? new HashSet(options) : new HashSet(0));
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
		IContainer container = getContainer();
		if (container == null) {
			return getName().hashCode();
		} else {
			return getName().hashCode() + container.hashCode();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#hasAttribute(java.lang.String)
	 */
	public boolean hasAttribute(String attributeName) throws CoreException {
		return getInfo().hasAttribute(attributeName);
	}
	
	/**
	 * Set the source locator to use with the launch, if specified 
	 * by this configuration.
	 * 
	 * @param launch the launch on which to set the source locator
	 * @throws CoreException if a problem is encountered
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
		return getContainer() == null;
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
    	if (monitor == null) {
			monitor = new NullProgressMonitor();
    	}
    	/* Setup progress monitor
    	 * - Prepare delegate (0)
    	 * - Pre-launch check (1)
    	 * - [Build before launch (7)]					if build
    	 * - [Incremental build before launch (3)]		if build
    	 * - Final launch validation (1)
    	 * - Initialize source locator (1)
    	 * - Launch delegate (10) */
    	if (build) {
    		monitor.beginTask("", 23); //$NON-NLS-1$
    	}
    	else {
    		monitor.beginTask("", 13); //$NON-NLS-1$
    	}
		monitor.subTask(DebugCoreMessages.LaunchConfiguration_9);
    	try {
			// bug 28245 - force the delegate to load in case it is interested in launch notifications
	    	Set modes = getModes();
	    	modes.add(mode);
	    	ILaunchDelegate[] delegates = getType().getDelegates(modes);
	    	ILaunchConfigurationDelegate delegate = null;
	    	if (delegates.length == 1) {
	    		delegate = delegates[0].getDelegate();
	    	} else if (delegates.length == 0) {
	    		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(promptStatus);
	    		if (handler != null) {
	    			handler.handleStatus(delegateNotAvailable, new Object[] {this, mode});
	    		}
	    		IStatus status = new Status(IStatus.CANCEL, DebugPlugin.getUniqueIdentifier(), DebugPlugin.ERROR, DebugCoreMessages.LaunchConfiguration_11, null); 
	    		throw new CoreException(status);
	    	} else {
	    		ILaunchDelegate del = getPreferredDelegate(modes);
	    		if(del == null) {
	    			del = getType().getPreferredDelegate(modes);
	    		}
	    		if(del == null) {
	    			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(promptStatus);
	    			IStatus status = null;
	    			if (handler != null) {
	    				status = (IStatus) handler.handleStatus(duplicateDelegates, new Object[] {this, mode});
	    			}
					if(status != null && status.isOK()) {
						del = getPreferredDelegate(modes);
						if(del == null) {
							del = getType().getPreferredDelegate(modes);
						}
						if(del != null) {
							delegate = del.getDelegate();
						}
						else {
							status = new Status(IStatus.CANCEL, DebugPlugin.getUniqueIdentifier(), DebugPlugin.ERROR, DebugCoreMessages.LaunchConfiguration_13, null); 
				    		throw new CoreException(status);
						}
					}
					else {
						status = new Status(IStatus.CANCEL, DebugPlugin.getUniqueIdentifier(), DebugPlugin.ERROR, DebugCoreMessages.LaunchConfiguration_13, null); 
			    		throw new CoreException(status);
					}
	    		}
	    		else {
	    			delegate = del.getDelegate();
	    		}
	    	}
	    	
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
					IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.ERROR, 
							MessageFormat.format(DebugCoreMessages.LaunchConfiguration_14, new String[]{mode, launch.getLaunchMode()}), null); 
					throw new CoreException(status);
				}
			}
			launch.setAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP, Long.toString(System.currentTimeMillis()));
			boolean captureOutput = getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
			if(!captureOutput) {
			    launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, "false"); //$NON-NLS-1$
			} else {
			    launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, null);
			}
			launch.setAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, getLaunchManager().getEncoding(this));
			
		// perform initial pre-launch sanity checks
			monitor.subTask(DebugCoreMessages.LaunchConfiguration_8);
			
			if (delegate2 != null) {
				if (!(delegate2.preLaunchCheck(this, mode, new SubProgressMonitor(monitor, 1)))) {
					return launch;
				}
			}
			else {
				monitor.worked(1); /* No pre-launch-check */
			}
		// perform pre-launch build
			if (build) {
				IProgressMonitor buildMonitor = new SubProgressMonitor(monitor, 10, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
				buildMonitor.beginTask(DebugCoreMessages.LaunchConfiguration_7, 10);			
				buildMonitor.subTask(DebugCoreMessages.LaunchConfiguration_6);
				if (delegate2 != null) {
					build = delegate2.buildForLaunch(this, mode, new SubProgressMonitor(buildMonitor, 7));
				}
				if (build) {
					buildMonitor.subTask(DebugCoreMessages.LaunchConfiguration_5);
					ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new SubProgressMonitor(buildMonitor, 3));				
				}
				else {
					buildMonitor.worked(3); /* No incremental build required */
				}
			}
		// final validation
			monitor.subTask(DebugCoreMessages.LaunchConfiguration_4);
			if (delegate2 != null) {
				if (!(delegate2.finalLaunchCheck(this, mode, new SubProgressMonitor(monitor, 1)))) {
					return launch;
				}
			}
			else {
				monitor.worked(1); /* No validation */
			}
			if (register) {
			    getLaunchManager().addLaunch(launch);
			}
			
			try {
				//initialize the source locator
				monitor.subTask(DebugCoreMessages.LaunchConfiguration_3);
				initializeSourceLocator(launch);
				monitor.worked(1);

				/* Launch the delegate */
				monitor.subTask(DebugCoreMessages.LaunchConfiguration_2);
				delegate.launch(this, mode,	launch,	new SubProgressMonitor(monitor, 10));
			} catch (CoreException e) {
				// if there was an exception, and the launch is empty, remove it
				if (!launch.hasChildren()) {
					getLaunchManager().removeLaunch(launch);
				}
				throw e;
			} catch (RuntimeException e) {
				// if there was a runtime exception, and the launch is empty, remove it
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
    	finally {
   			monitor.done();
    	}
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
	 * Sets the new name for this configuration.
	 * 
	 * @param name the new name for this configuration
	 * @since 3.5
	 */
	protected void setName(String name) {
		fName = name;
	}
	
	/**
	 * Sets this configurations container or <code>null</code> if stored in the
	 * local metadata.
	 * 
	 * @param container or <code>null</code>
	 * @since 3.5
	 */
	protected void setContainer(IContainer container) {
		fContainer = container;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#supportsMode(java.lang.String)
	 */
	public boolean supportsMode(String mode) throws CoreException {
		return getType().supportsMode(mode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfiguration#isReadOnly()
	 */
	public boolean isReadOnly() {
		try {
			IFileStore fileStore = getFileStore();
			if (fileStore != null) {
				return fileStore.fetchInfo().getAttribute(EFS.ATTRIBUTE_READ_ONLY);
			}
		} catch (CoreException e) {
		}
		return true;
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfiguration#getPreferredDelegate(java.util.Set)
	 */
	public ILaunchDelegate getPreferredDelegate(Set modes) throws CoreException {
		Map delegates = getAttribute(LaunchConfiguration.ATTR_PREFERRED_LAUNCHERS, (Map)null);
		if(delegates != null) {
			String id = (String) delegates.get(modes.toString());
			if(id != null) {
				return getLaunchManager().getLaunchDelegate(id);
			}
		}
		return null;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}
	
}

