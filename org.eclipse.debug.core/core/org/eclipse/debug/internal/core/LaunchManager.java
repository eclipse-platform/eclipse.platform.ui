package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IProcess;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Manages launch configurations, launch configuration types, and registered launches.
 *
 * @see ILaunchManager
 */
public class LaunchManager implements ILaunchManager, IResourceChangeListener {
	
	/**
	 * Collection of defined launch configuration type
	 * extensions.
	 */
	private List fLaunchConfigurationTypes = null;
	
	/**
	 * Launch configuration cache. Keys are <code>LaunchConfiguration</code>,
	 * values are <code>LaunchConfigurationInfo</code>.
	 */
	private HashMap fLaunchConfigurations = new HashMap(10);
	
	/**
	 * A cache of launch configuration names currently in the workspace.
	 */
	private String[] fSortedConfigNames = null;
	
	/**
	 * Collection of all launch configurations in the workspace.
	 * <code>List</code> of <code>ILaunchConfiguration</code>.
	 */
	private List fLaunchConfigurationIndex = null;
	
	/**
	 * Launch configuration comparator extensions,
	 * keyed by attribute name.
	 */
	private HashMap fComparators = null;
	
	/**
	 * Constant for use as local name part of <code>QualifiedName</code>
	 * for persisting the default launch configuration type.
	 */
	private static final String DEFAULT_CONFIG_TYPE = "defaultLaunchConfigurationType"; //$NON-NLS-1$
	
	/**
	 * Types of notifications
	 */
	public static final int ADDED = 0;
	public static final int REMOVED= 1;
	public static final int CHANGED= 2;

	/**
	 * Collection of launches
	 */
	private Vector fLaunches= new Vector(10);

	/**
	 * Collection of listeners
	 */
	private ListenerList fListeners= new ListenerList(5);
	
	/**
	 * Visitor used to process resource deltas,
	 * to update launch configuration index.
	 */
	private IResourceDeltaVisitor fgVisitor;
	
	/**
	 * Launch configuration listeners
	 */
	private ListenerList fLaunchConfigurationListeners = new ListenerList(5);
	
	/**
	 * Table of source locator extensions. Keys
	 * are identifiers, and values are associated
	 * configuration elements.
	 */
	private Map fSourceLocators = null;
	
	/**
	 * Path to the local directory where local launch configurations
	 * are stored with the workspace.
	 */
	protected static final IPath LOCAL_LAUNCH_CONFIGURATION_CONTAINER_PATH =
		DebugPlugin.getDefault().getStateLocation().append(".launches"); //$NON-NLS-1$
		
	/**
	 * Serializes a XML document into a string - encoded in UTF8 format,
	 * with platform line separators.
	 * 
	 * @param doc document to serialize
	 * @return the document as a string
	 */
	public static String serializeDocument(Document doc) throws IOException {
		ByteArrayOutputStream s= new ByteArrayOutputStream();
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		format.setLineSeparator(System.getProperty("line.separator"));  //$NON-NLS-1$
		
		Serializer serializer =
			SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
				new OutputStreamWriter(s, "UTF8"), //$NON-NLS-1$
				format);
		serializer.asDOMSerializer().serialize(doc);
		return s.toString("UTF8"); //$NON-NLS-1$		
	}	
			
	/**
	 * @see ILaunchManager#addLaunchListener(ILaunchListener)
	 */
	public void addLaunchListener(ILaunchListener listener) {
		fListeners.add(listener);
	}

	/**
	 * Returns a collection of all launch configuration handles in 
	 * the workspace. This collection is initialized lazily.
	 * 
	 * @return all launch configuration handles
	 */
	private List getAllLaunchConfigurations() throws CoreException {
		if (fLaunchConfigurationIndex == null) {
			fLaunchConfigurationIndex = new ArrayList(20);
			List configs = findLocalLaunchConfigurations();
			verifyConfigurations(configs, fLaunchConfigurationIndex);
			configs = findLaunchConfigurations(getWorkspaceRoot());
			verifyConfigurations(configs, fLaunchConfigurationIndex);
		}
		return fLaunchConfigurationIndex;
	}
	
	/**
	 * Verify basic integrity of launch configurations in the given list,
	 * adding valid configs to the collection of all launch configurations.
	 * Excpetions are logged for invalid configs.
	 * 
	 * @param verify the list of configs to verify
	 * @param valid the list to place valid configrations in
	 */
	protected void verifyConfigurations(List verify, List valid) {
		Iterator configs = verify.iterator();
		while (configs.hasNext()) {
			ILaunchConfiguration config = (ILaunchConfiguration)configs.next();
			if (isValid(config)) {
				valid.add(config);
			}
		}		
	}
	
	/**
	 * Returns whether the given launch configuration passes a basic
	 * integritiy test by retrieving its type.
	 * 
	 * @param config the configuration to verify
	 * @return whether the config meets basic integrity constraints
	 */
	protected boolean isValid(ILaunchConfiguration config) {
		try {
			config.getType();
		} catch (CoreException e) {
			IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugException.INTERNAL_ERROR, 
				MessageFormat.format(DebugCoreMessages.getString("LaunchManager.Unable_to_restore_invalid_launch_configuration"),new String[] {config.getLocation().toOSString()} ), e); //$NON-NLS-1$
			DebugPlugin.log(status);
			return false;
		}
		return true;
	}
	
	/**
	 * Clears all launch configurations (if any have been accessed)
	 */
	private void clearAllLaunchConfigurations() {
		if (fLaunchConfigurationTypes != null) {
			fLaunchConfigurationTypes.clear();
		}
		if (fLaunchConfigurationIndex != null) {
			fLaunchConfigurationIndex.clear();
		}
	}
		
	/**
	 * @see ILaunchManager#removeLaunch(ILaunch)
	 */
	public void removeLaunch(ILaunch launch) {
		if (launch == null) {
			return;
		}
		fLaunches.remove(launch);
		fireUpdate(launch, REMOVED);
	}	

	/**
	 * Fires notification to the listeners that a launch has been (de)registered.
	 */
	public void fireUpdate(ILaunch launch, int update) {
		Object[] copiedListeners= fListeners.getListeners();
		for (int i= 0; i < copiedListeners.length; i++) {
			ILaunchListener listener = (ILaunchListener)copiedListeners[i];
			switch (update) {
				case ADDED:
					listener.launchAdded(launch);
					break;
				case REMOVED:
					listener.launchRemoved(launch);
					break;
				case CHANGED:
					listener.launchChanged(launch);
					break;
			}
		}
	}

	/**
	 * @see ILaunchManager#getDebugTargets()
	 */
	public IDebugTarget[] getDebugTargets() {
		List allTargets= new ArrayList(fLaunches.size());
		if (fLaunches.size() > 0) {
			Iterator e= fLaunches.iterator();
			while (e.hasNext()) {
				IDebugTarget[] targets= ((ILaunch) e.next()).getDebugTargets();
				for (int i = 0; i < targets.length; i++) {
					allTargets.add(targets[i]);
				}
			}
		}
		return (IDebugTarget[])allTargets.toArray(new IDebugTarget[allTargets.size()]);
	}
			
	/**
	 * @see ILaunchManager#getLaunches()
	 */
	public ILaunch[] getLaunches() {
		return (ILaunch[])fLaunches.toArray(new ILaunch[fLaunches.size()]);
	}

	/**
	 * @see ILaunchManager#getProcesses()
	 */
	public IProcess[] getProcesses() {
		List allProcesses= new ArrayList(fLaunches.size());
		Iterator e= fLaunches.iterator();
		while (e.hasNext()) {
			IProcess[] processes= ((ILaunch) e.next()).getProcesses();
			for (int i= 0; i < processes.length; i++) {
				allProcesses.add(processes[i]);
			}
		}
		return (IProcess[])allProcesses.toArray(new IProcess[allProcesses.size()]);
	}
	
	/**
	 * @see ILaunchManager#addLaunch(ILaunch)
	 */
	public void addLaunch(ILaunch launch) {
		if (fLaunches.contains(launch)) {
			return;
		}
		
		fLaunches.add(launch);
		fireUpdate(launch, ADDED);
	}	
	
	/**
	 * @see ILaunchManager#removeLaunchListener(ILaunchListener)
	 */
	public void removeLaunchListener(ILaunchListener listener) {
		fListeners.remove(listener);
	}
		
	/**
	 * Return a LaunchConfigurationInfo object initialized from XML contained in
	 * the specified stream.  Simply pass out any exceptions encountered so that
	 * caller can deal with them.  This is important since caller may need access to the
	 * actual exception.
	 */
	protected LaunchConfigurationInfo createInfoFromXML(InputStream stream) throws CoreException,
																			 ParserConfigurationException,
																			 IOException,
																			 SAXException {
		Element root = null;
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		root = parser.parse(new InputSource(stream)).getDocumentElement();
		LaunchConfigurationInfo info = new LaunchConfigurationInfo();
		info.initializeFromXML(root);
		return info;
	}

	/**
	 * Terminates/Disconnects any active debug targets/processes.
	 * Clears launch configuration types.
	 */
	public void shutdown() throws CoreException {
		fListeners.removeAll();
		ILaunch[] launches = getLaunches();
		for (int i= 0; i < launches.length; i++) {
			ILaunch launch= launches[i];
			try {
				launch.terminate();
			} catch (DebugException e) {
				DebugPlugin.log(e);
			}
		}
		
		clearAllLaunchConfigurations();

		getWorkspace().removeResourceChangeListener(this);
	}
	
	/**
	 * Creates launch configuration types for each defined extension.
	 * 
	 * @exception CoreException if an exception occurs processing
	 *  the extensions
	 */
	public void startup() throws CoreException {
		getWorkspace().addResourceChangeListener(this);
	}
							
	/**
	 * Returns the info object for the specified launch configuration.
	 * If the configuration exists, but is not yet in the cache,
	 * an info object is built and added to the cache.
	 * 
	 * @exception CoreException if an exception occurs building
	 *  the info object
	 * @exception DebugException if the config does not exist
	 */
	protected LaunchConfigurationInfo getInfo(ILaunchConfiguration config) throws CoreException {
		LaunchConfigurationInfo info = (LaunchConfigurationInfo)fLaunchConfigurations.get(config);
		if (info == null) {
			if (config.exists()) {
				InputStream stream = null;
				try {
					if (config.isLocal()) {
						IPath path = config.getLocation();
						File file = path.toFile();				
						stream = new FileInputStream(file);
					} else {
						IFile file = ((LaunchConfiguration) config).getFile();
						stream = file.getContents();
					}
					info = createInfoFromXML(stream);
					fLaunchConfigurations.put(config, info);
				} catch (FileNotFoundException e) {
					throw createDebugException(MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e); //$NON-NLS-1$					
				} catch (SAXException e) {
					throw createDebugException(MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e); //$NON-NLS-1$					
				} catch (ParserConfigurationException e) {
					throw createDebugException(MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e); //$NON-NLS-1$					
				} catch (IOException e) {
					throw createDebugException(MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e); //$NON-NLS-1$					
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException e) {
							throw createDebugException(MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e); //$NON-NLS-1$					
						}
					}
				}
		
			} else {
				throw createDebugException(DebugCoreMessages.getString("LaunchManager.Launch_configuration_does_not_exist._6"), null); //$NON-NLS-1$
			}
		}
		return info;
	}	
	
	/**
	 * Return an instance of DebugException containing the specified message and Throwable.
	 */
	protected DebugException createDebugException(String message, Throwable throwable) {
		return new DebugException(
					new Status(
					 Status.ERROR, DebugPlugin.getUniqueIdentifier(),
					 DebugException.REQUEST_FAILED, message, throwable 
					)
				);
	}
	
	/**
	 * Removes the given launch configuration from the cache of configurations.
	 * When a local configuration is deleted, this method is called, as there will
	 * be no resource delta generated to auto-update the cache.
	 * 
	 * @param configuration the configuration to remove
	 */
	private void removeInfo(ILaunchConfiguration configuration) {
		fLaunchConfigurations.remove(configuration);
	}
	
	/**
	 * @see ILaunchManager#getLaunchConfigurations()
	 */
	public ILaunchConfiguration[] getLaunchConfigurations() throws CoreException {
		List allConfigs = getAllLaunchConfigurations();
		return (ILaunchConfiguration[])allConfigs.toArray(new ILaunchConfiguration[allConfigs.size()]);
	}	
	
	/**
	 * @see ILaunchManager#getLaunchConfigurations(ILaunchConfigurationType)
	 */
	public ILaunchConfiguration[] getLaunchConfigurations(ILaunchConfigurationType type) throws CoreException {
		Iterator iter = getAllLaunchConfigurations().iterator();
		List configs = new ArrayList();
		while (iter.hasNext()) {
			ILaunchConfiguration config = (ILaunchConfiguration)iter.next();
			if (config.getType().equals(type)) {
				configs.add(config);
			}
		}
		return (ILaunchConfiguration[])configs.toArray(new ILaunchConfiguration[configs.size()]);
	}
	
	/**
	 * Returns all launch configurations that are stored as resources
	 * in the given project.
	 * 
	 * @param project a project
	 * @return collection of launch configurations that are stored as resources
	 *  in the given project
	 */
	protected List getLaunchConfigurations(IProject project) throws CoreException {
		Iterator iter = getAllLaunchConfigurations().iterator();
		List configs = new ArrayList();
		while (iter.hasNext()) {
			ILaunchConfiguration config = (ILaunchConfiguration)iter.next();
			IFile file = config.getFile();
			if (file != null && file.getProject().equals(project)) {
				configs.add(config);
			}
		}
		return configs;
	}	
	
	/**
	 * Returns all launch configurations that are stored locally.
	 * 
	 * @return collection of launch configurations stored lcoally
	 */
	protected List getLocalLaunchConfigurations() throws CoreException {
		Iterator iter = getAllLaunchConfigurations().iterator();
		List configs = new ArrayList();
		while (iter.hasNext()) {
			ILaunchConfiguration config = (ILaunchConfiguration)iter.next();
			if (config.isLocal()) {
				configs.add(config);
			}
		}
		return configs;
	}		
	
	/**
	 * @see ILaunchManager#getLaunchConfiguration(IFile)
	 */
	public ILaunchConfiguration getLaunchConfiguration(IFile file) {
		return new LaunchConfiguration(file.getLocation());
	}
	
	/**
	 * @see ILaunchManager#getLaunchConfiguration(String)
	 */
	public ILaunchConfiguration getLaunchConfiguration(String memento) throws CoreException {
		return new LaunchConfiguration(memento);
	}
	
	/**
	 * @see ILaunchManager#getLaunchConfigurationTypes()
	 */
	public ILaunchConfigurationType[] getLaunchConfigurationTypes() {
		List types= getLaunchConfigurationTypeList();
		return (ILaunchConfigurationType[])types.toArray(new ILaunchConfigurationType[types.size()]);
	}
	
	/**
	 * @see ILaunchManager#getLaunchConfigurationType(String)
	 */
	public ILaunchConfigurationType getLaunchConfigurationType(String id) {
		Iterator iter = getLaunchConfigurationTypeList().iterator();
		while (iter.hasNext()) {
			ILaunchConfigurationType type = (ILaunchConfigurationType)iter.next();
			if (type.getIdentifier().equals(id)) {
				return type;
			}
		}
		return null;
	}
	
	private List getLaunchConfigurationTypeList() {
		if (fLaunchConfigurationTypes == null) {
			initializeLaunchConfigurationTypes();
		}
		return fLaunchConfigurationTypes;
	}
	
	private void initializeLaunchConfigurationTypes() {
		IPluginDescriptor descriptor= DebugPlugin.getDefault().getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(DebugPlugin.EXTENSION_POINT_LAUNCH_CONFIGURATION_TYPES);
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
		fLaunchConfigurationTypes= new ArrayList(infos.length);
		for (int i= 0; i < infos.length; i++) {
			IConfigurationElement configurationElement = infos[i];
			LaunchConfigurationType configType = new LaunchConfigurationType(configurationElement); 			
			fLaunchConfigurationTypes.add(configType);
		}		
	}
	
	/**
	 * Notifies the launch manager that a launch configuration
	 * has been deleted. The configuration is removed from the
	 * cache of info's and from the index of configurations by
	 * project, and listeners are notified.
	 * 
	 * @param config the launch configuration that was deleted
	 */
	protected void launchConfigurationDeleted(ILaunchConfiguration config) throws CoreException {
		removeInfo(config);
		getAllLaunchConfigurations().remove(config);
		if (fLaunchConfigurationListeners.size() > 0) {
			Object[] listeners = fLaunchConfigurationListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				ILaunchConfigurationListener listener = (ILaunchConfigurationListener)listeners[i];
				listener.launchConfigurationRemoved(config);
			}
		}
		clearConfigNameCache();			
	}
	
	/**
	 * Notifies the launch manager that a launch configuration
	 * has been added. The configuration is added to the index of
	 * configurations by project, and listeners are notified.
	 * 
	 * @param config the launch configuration that was added
	 */
	protected void launchConfigurationAdded(ILaunchConfiguration config) throws CoreException {
		if (isValid(config)) {
			List allConfigs = getAllLaunchConfigurations();
			if (!allConfigs.contains(config)) {
				allConfigs.add(config);
			}
			if (fLaunchConfigurationListeners.size() > 0) {
				Object[] listeners = fLaunchConfigurationListeners.getListeners();
				for (int i = 0; i < listeners.length; i++) {
					ILaunchConfigurationListener listener = (ILaunchConfigurationListener)listeners[i];
					listener.launchConfigurationAdded(config);
				}
			}
			clearConfigNameCache();			
		} else {
			launchConfigurationDeleted(config);
		}
	}
	
	/**
	 * Notifies the launch manager that a launch configuration
	 * has been changed. The configuration is removed from the
	 * cache of info objects such that the new attributes will
	 * be updated on the next access. Listeners are notified of
	 * the change.
	 * 
	 * @param config the launch configuration that was changed
	 */
	protected void launchConfigurationChanged(ILaunchConfiguration config) {
		removeInfo(config);
		clearConfigNameCache();
		if (isValid(config)) {
			notifyChanged(config);
		} else {
			try {
				launchConfigurationDeleted(config);
			} catch (CoreException e) {
				DebugPlugin.log(e);
			}
		}								
	}
	
	/**
	 * Notifies listeners that the given launch configuration
	 * has changed.
	 * 
	 * @param configuration the changed launch configuration
	 */
	protected void notifyChanged(ILaunchConfiguration configuration) {
		if (fLaunchConfigurationListeners.size() > 0) {
			Object[] listeners = fLaunchConfigurationListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				ILaunchConfigurationListener listener = (ILaunchConfigurationListener)listeners[i];
				listener.launchConfigurationChanged(configuration);
			}
		}		
	}
	
	/**
	 * @see ILaunchManager#isExistingLaunchConfigurationName(String)
	 */
	public boolean isExistingLaunchConfigurationName(String name) throws CoreException {
		String[] sortedConfigNames = getAllSortedConfigNames();
		int index = Arrays.binarySearch(sortedConfigNames, name);
		if (index < 0) {
			return false;
		} 
		return true;
	}
	
	/**
	 * @see org.eclipse.debug.core.ILaunchManager#generateUniqueLaunchConfigurationNameFrom(String)
	 */
	public String generateUniqueLaunchConfigurationNameFrom(String namePrefix) {
		int index = 1;
		String baseName = namePrefix;
		int copyIndex = baseName.lastIndexOf(" (");
		if (copyIndex > -1) {
			String trailer = baseName.substring(copyIndex + 1);
			try {
				index = Integer.parseInt(trailer);
				baseName = namePrefix.substring(0, copyIndex);
			} catch (NumberFormatException nfe) {
			}
		} 
		String newName = baseName;
		try {
			while (isExistingLaunchConfigurationName(newName)) {
				StringBuffer buffer = new StringBuffer(baseName);
				buffer.append(" (");
				buffer.append(String.valueOf(index));
				index++;
				buffer.append(')');
				newName = buffer.toString();		
			}		
		} catch (CoreException e) {
			DebugPlugin.log(e);
		}
		return newName;
	}
	
	/**
	 * Return a sorted array of the names of all <code>ILaunchConfiguration</code>s in 
	 * the workspace.  These are cached, and cache is cleared when a new config is added,
	 * deleted or changed.
	 */
	protected String[] getAllSortedConfigNames() throws CoreException {
		if (fSortedConfigNames == null) {
			ILaunchConfiguration[] configs = getLaunchConfigurations();
			fSortedConfigNames = new String[configs.length];
			for (int i = 0; i < configs.length; i++) {
				fSortedConfigNames[i] = configs[i].getName();
			}
			Arrays.sort(fSortedConfigNames);
		}
		return fSortedConfigNames;
	}
	
	/**
	 * The launch config name cache is cleared when a config is added, deleted or changed.
	 */
	protected void clearConfigNameCache() {
		fSortedConfigNames = null;
	}
		
	/**
	 * Finds and returns all local launch configurations.
	 *
	 * @return all local launch configurations
	 * @exception CoreException if there is a lower level
	 *  IO exception
	 */
	protected List findLocalLaunchConfigurations() throws CoreException {
		IPath containerPath = LOCAL_LAUNCH_CONFIGURATION_CONTAINER_PATH;
		List configs = new ArrayList(10);
		final File directory = containerPath.toFile();
		if (directory.isDirectory()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return dir.equals(directory) &&
							name.endsWith(ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION);
				}
			};
			String[] files = directory.list(filter);
			for (int i = 0; i < files.length; i++) {
				LaunchConfiguration config = new LaunchConfiguration(containerPath.append(files[i]));
				configs.add(config);
			}
		}
		return configs;
	}
	
	/**
	 * Finds and returns all launch configurations in the given
	 * container (and subcontainers)
	 * 
	 * @param container the container to search
	 * @exception CoreException an exception occurs traversing
	 *  the container.
	 * @return all launch configurations in the given container
	 */
	protected List findLaunchConfigurations(IContainer container) throws CoreException {
		List list = new ArrayList(10);
		if (container instanceof IProject && !((IProject)container).isOpen()) {
			return list;
		}
		searchForFiles(container, ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION, list);
		Iterator iter = list.iterator();
		List configs = new ArrayList(list.size());
		while (iter.hasNext()) {
			IFile file = (IFile)iter.next();
			configs.add(getLaunchConfiguration(file));
		}
		return configs;
	}
	
	/**
	 * Recursively searches the given container for files with the given
	 * extension.
	 * 
	 * @param container the container to search in
	 * @param extension the file extension being searched for
	 * @param list the list to add the matching files to
	 * @exception CoreException if an exception occurs traversing
	 *  the container
	 */
	protected void searchForFiles(IContainer container, String extension, List list) throws CoreException {
		IResource[] members = container.members();
		for (int i = 0; i < members.length; i++) {
			if (members[i] instanceof IContainer) {
				if (members[i] instanceof IProject && !((IProject)members[i]) .isOpen()) {
					continue;
				}
				searchForFiles((IContainer)members[i], extension, list);
			} else if (members[i] instanceof IFile) {
				IFile file = (IFile)members[i];
				if (ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION.equalsIgnoreCase(file.getFileExtension())) {
					list.add(file);
				}
			}
		}
	}
	
	/**
	 * Traverses the delta looking for added/removed/changed launch
	 * configuration files.
	 * 
	 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (delta != null) {
			try {
				if (fgVisitor == null) {
					fgVisitor= new LaunchManagerVisitor();
				}
				delta.accept(fgVisitor);
			} catch (CoreException e) {
				DebugPlugin.log(e);
			}
		}		
	}
	
	/**
	 * Returns the launch configurations specified by the given
	 * XML document.
	 * 
	 * @param root XML document
	 * @return list of launch configurations
	 * @exception IOException if an exception occurs reading the XML
	 */	
	protected List getConfigsFromXML(Element root) throws CoreException {
		DebugException invalidFormat = 
			new DebugException(
				new Status(
				 Status.ERROR, DebugPlugin.getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, DebugCoreMessages.getString("LaunchManager.Invalid_launch_configuration_index._18"), null //$NON-NLS-1$
				)
			);		
			
		if (!root.getNodeName().equalsIgnoreCase("launchConfigurations")) { //$NON-NLS-1$
			throw invalidFormat;
		}
		
		// read each launch configuration 
		List configs = new ArrayList(4);	
		NodeList list = root.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element entry = (Element) node;
				String nodeName = entry.getNodeName();
				if (!nodeName.equals("launchConfiguration")) { //$NON-NLS-1$
					throw invalidFormat;
				}
				String memento = entry.getAttribute("memento"); //$NON-NLS-1$
				if (memento == null) {
					throw invalidFormat;
				}
				configs.add(getLaunchConfiguration(memento));
			}
		}
		return configs;
	}		
	
	/**
	 * The specified project has just opened - add all launch
	 * configs in the project to the index of all configs.
	 * 
	 * @param project the project that has been opened
	 * @exception CoreException if reading the index fails
	 */
	protected void projectOpened(IProject project) throws CoreException {
		List configs = findLaunchConfigurations(project);
		if (!configs.isEmpty()) {
			List allList = getAllLaunchConfigurations();
			Iterator iterator = configs.iterator();
			while (iterator.hasNext()) {
				ILaunchConfiguration config = (ILaunchConfiguration) iterator.next();
				if (!allList.contains(config)) {
					allList.add(config);
				}
			}			
		}
	}
	
	/**
	 * The specified project has just closed - remove its
	 * launch configurations from the cached index.
	 * 
	 * @param project the project that has been closed
	 * @exception CoreException if writing the index fails
	 */
	protected void projectClosed(IProject project) throws CoreException {
		List configs = getLaunchConfigurations(project);
		if (!configs.isEmpty()) {
			getAllLaunchConfigurations().removeAll(configs);
		}
	}	
	
	/**
	 * Visitor for handling resource deltas.
	 */
	class LaunchManagerVisitor implements IResourceDeltaVisitor {
		/**
		 * @see IResourceDeltaVisitor#visit(IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) {
			if (delta == null) {
				return false;
			}
			if (0 != (delta.getFlags() & IResourceDelta.OPEN)) {
				if (delta.getResource() instanceof IProject) {
					IProject project = (IProject)delta.getResource();
					try {
						if (project.isOpen()) {
							LaunchManager.this.projectOpened(project);
						} else { 
						    LaunchManager.this.projectClosed(project);
						}
					} catch (CoreException e) {
						DebugPlugin.log(e);
					}
				}
				return false;
			}
			IResource resource = delta.getResource();
			if (resource instanceof IFile) {
				IFile file = (IFile)resource;
				if (ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION.equals(file.getFileExtension())) {
					IPath configPath = file.getLocation();
					// If the file has already been deleted, reconstruct the full
					// filesystem path
					if (configPath == null) {
						IPath workspaceRelativePath = delta.getFullPath();
						configPath = getWorkspaceRoot().getLocation().append(workspaceRelativePath);
					}
					ILaunchConfiguration handle = new LaunchConfiguration(configPath);
					try {
						switch (delta.getKind()) {						
							case IResourceDelta.ADDED :
								LaunchManager.this.launchConfigurationAdded(handle);
								break;
							case IResourceDelta.REMOVED :
								LaunchManager.this.launchConfigurationDeleted(handle);
								break;
							case IResourceDelta.CHANGED :
								LaunchManager.this.launchConfigurationChanged(handle);
								break;
						}					
					} catch (CoreException e) {
						DebugPlugin.log(e);
					}
				}
				return false;
			} else if (resource instanceof IContainer) {
				return true;
			}
			return true;
		}		
	}
	
	/**
	 * @see ILaunchManager#addLaunchConfigurationListener(ILaunchConfigurationListener)
	 */
	public void addLaunchConfigurationListener(ILaunchConfigurationListener listener) {
		fLaunchConfigurationListeners.add(listener);
	}

	/**
	 * @see ILaunchManager#removeLaunchConfigurationListener(ILaunchConfigurationListener)
	 */
	public void removeLaunchConfigurationListener(ILaunchConfigurationListener listener) {
		fLaunchConfigurationListeners.remove(listener);
	}

	/**
	 * Register source locators.
	 * 
	 * @exception CoreException if an exception occurrs reading
	 *  the extensions
	 */
	private void initializeSourceLocators() throws CoreException {
		IPluginDescriptor descriptor= DebugPlugin.getDefault().getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(DebugPlugin.EXTENSION_POINT_SOURCE_LOCATORS);
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
		fSourceLocators= new HashMap(infos.length);
		for (int i= 0; i < infos.length; i++) {
			IConfigurationElement configurationElement = infos[i];
			String id = configurationElement.getAttribute("id"); //$NON-NLS-1$			
			if (id != null) {
				fSourceLocators.put(id,configurationElement);
			} else {
				// invalid status handler
				IStatus s = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugException.INTERNAL_ERROR,
				MessageFormat.format(DebugCoreMessages.getString("LaunchManager.Invalid_source_locator_extentsion_defined_by_plug-in___{0}______id___not_specified_12"), new String[] {configurationElement.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier()} ), null);  //$NON-NLS-1$
				DebugPlugin.getDefault().log(s);
			}
		}			
	}
	
	
	/**
	 * Load comparator extensions.
	 * 
	 * @exception CoreException if an exception occurrs reading
	 *  the extensions
	 */
	private void initializeComparators() {
		IPluginDescriptor descriptor= DebugPlugin.getDefault().getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(DebugPlugin.EXTENSION_POINT_LAUNCH_CONFIGURATION_COMPARATORS);
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
		fComparators = new HashMap(infos.length);
		for (int i= 0; i < infos.length; i++) {
			IConfigurationElement configurationElement = infos[i];
			String attr = configurationElement.getAttribute("attribute"); //$NON-NLS-1$			
			if (attr != null) {
				fComparators.put(attr, new LaunchConfigurationComparator(configurationElement));
			} else {
				// invalid status handler
				IStatus s = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugException.INTERNAL_ERROR,
				MessageFormat.format(DebugCoreMessages.getString("LaunchManager.Invalid_launch_configuration_comparator_extension_defined_by_plug-in_{0}_-_attribute_not_specified_1"), new String[] {configurationElement.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier()}), null); //$NON-NLS-1$
				DebugPlugin.getDefault().log(s);
			}
		}			
	}
		
	/**
	 * @see ILaunchManager#newSourceLocator(String)
	 */
	public IPersistableSourceLocator newSourceLocator(String identifier) throws CoreException {
		if (fSourceLocators == null) {
			initializeSourceLocators();
		}
		IConfigurationElement config = (IConfigurationElement)fSourceLocators.get(identifier);
		if (config == null) {
			throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugException.INTERNAL_ERROR,
				MessageFormat.format(DebugCoreMessages.getString("LaunchManager.Source_locator_does_not_exist__{0}_13"), new String[] {identifier} ), null)); //$NON-NLS-1$
		} else {
			return (IPersistableSourceLocator)config.createExecutableExtension("class"); //$NON-NLS-1$
		}
		
	}

	/**
	 * Returns the comparator registered for the given attribute, or
	 * <code>null</code> if none.
	 * 
	 * @param attributeName attribute for which a comparator is required
	 * @return comparator, or <code>null</code> if none
	 */
	protected Comparator getComparator(String attributeName) {
		 HashMap map = getComparators();
		 return (Comparator)map.get(attributeName);
	}
	
	/**
	 * Returns comparators, loading if required
	 */
	protected HashMap getComparators() {
		if (fComparators == null) {
			initializeComparators();
		}
		return fComparators;
	}
	
	private IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	private IWorkspaceRoot getWorkspaceRoot() {
		return getWorkspace().getRoot();
	}
	
}
