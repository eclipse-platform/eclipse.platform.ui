package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
	 * Constructs a launch configuration from the given
	 * memento.
	 * 
	 * @param memento launch configuration memento
	 * @exception CoreException if the memento is invalid or
	 * 	an exception occurrs reading the memento
	 */
	protected LaunchConfiguration(String memento) throws CoreException {
		Exception ex = null;
		try {
			Element root = null;
			DocumentBuilder parser =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader(memento);
			InputSource source = new InputSource(reader);
			root = parser.parse(source).getDocumentElement();
			
			String localString = root.getAttribute("local");
			String path = root.getAttribute("path");

			String message = null;				
			if (path == null) {
				message = "Invalid launch configuration memento: missing path attribute.";
			} else if (localString == null) {
				message = "Invalid launch configuration memento: missing local attribute.";
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
				location = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(path);
			}
			setLocation(location);
			return;
		} catch (ParserConfigurationException e) {
			ex = e;			
		} catch (SAXException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		}
		IStatus s = newStatus("Exception occurred parsing memento.", DebugException.INTERNAL_ERROR, ex);
		throw new CoreException(s);
	}
	
	/**
	 * Creates and returns a new error status based on 
	 * the given mesasge, code, and exception.
	 * 
	 * @param message error message
	 * @param code error code
	 * @param e exception or <code>null</code>
	 * @return status
	 */
	protected IStatus newStatus(String message, int code, Throwable e) {
		return new Status(IStatus.ERROR, DebugPlugin.PLUGIN_ID, code, message, e);
	}
	
	/**
	 * @see ILaunchConfiguration#launch(String, IProgressMonitor)
	 */
	public ILaunch launch(String mode, IProgressMonitor montior) throws CoreException {
		ILaunch launch = getDelegate().launch(this, mode, montior);
		if (launch != null) {
			getLaunchManager().addLaunch(launch);
			initializeSourceLocator(launch);
		}
		return launch;
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
			if (type != null) {
				IPersistableSourceLocator locator = getLaunchManager().newSourceLocator(type);
				String memento = getAttribute(ATTR_SOURCE_LOCATOR_MEMENTO, (String)null);
				if (memento == null) {
					locator.initializeDefaults(this);
				} else {
					locator.initiatlizeFromMemento(memento);
				}
				launch.setSourceLocator(locator);
			}
		}
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
		return getLastLocationSegment();
	}
	
	private String getLastLocationSegment() {
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
	 * @see ILaunchConfiguration#getAttribute(String, List)
	 */
	public List getAttribute(String attributeName, List defaultValue) throws CoreException {
		return getInfo().getListAttribute(attributeName, defaultValue);
	}

	/**
	 * @see ILaunchConfiguration#getAttribute(String, Map)
	 */
	public Map getAttribute(String attributeName, Map defaultValue) throws CoreException {
		return getInfo().getMapAttribute(attributeName, defaultValue);
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
	 * @see ILaunchConfiguration#copy(String name)
	 */
	public ILaunchConfigurationWorkingCopy copy(String name) throws CoreException {
		ILaunchConfigurationWorkingCopy copy = new LaunchConfigurationWorkingCopy(this, name);
		return copy;
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
						 DebugException.REQUEST_FAILED, DebugCoreMessages.getString("LaunchConfiguration.Failed_to_delete_launch_configuration._1"), null) //$NON-NLS-1$
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
	 * @return launch manager
	 */
	protected LaunchManager getLaunchManager() {
		return (LaunchManager)DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * @see ILaunchConfiguration#getMemento()
	 */
	public String getMemento() throws CoreException {
		IPath relativePath = null;
		if (isLocal()) {
			IPath rootPath = LaunchManager.LOCAL_LAUNCH_CONFIGURATION_CONTAINER_PATH;
			IPath configPath = getLocation();
			relativePath = configPath.removeFirstSegments(rootPath.segmentCount());
		} else {
			relativePath = getFile().getFullPath();
		}
		relativePath = relativePath.setDevice(null);
		
		Document doc = new DocumentImpl();
		Element node = doc.createElement("launchConfiguration");
		node.setAttribute("local", (new Boolean(isLocal())).toString());
		node.setAttribute("path", relativePath.toString());
		
		// produce a String output
		StringWriter writer = new StringWriter();
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		Serializer serializer =
			SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
				writer,
				format);
		
		try {
			serializer.asDOMSerializer().serialize(node);
		} catch (IOException e) {
			IStatus status = newStatus("Exception occurred creating launch configuration memento.", DebugException.INTERNAL_ERROR,  e);
			throw new CoreException(status);
		}
		return writer.toString();
	}

	/**
	 * @see ILaunchConfiguration#getFile()
	 */	
	public IFile getFile() {
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(getLocation());
	}

	/**
	 * @see ILaunchConfiguration#contentsEqual(Object)
	 */
	public boolean contentsEqual(Object object) {
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
	
	/**
	 * @see Object#hashCode()
	 */
	public int hashCode() {
		return getLocation().hashCode();
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
}

