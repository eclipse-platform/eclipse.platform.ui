package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Manages registered launches.
 *
 * @see ILaunchManager
 */
public class LaunchManager implements ILaunchManager, IResourceChangeListener {
	
	/**
	 * Collection of defined launch configuration type
	 * extensions.
	 */
	private List fLaunchConfigurationTypes = new ArrayList(5);
	
	/**
	 * Launch configuration cache. Keys are <code>IPath</code>,
	 * values are <code>LaunchConfigurationInfo</code>.
	 */
	private HashMap fLaunchConfigurations = new HashMap(10);
	
	/**
	 * Collection of all lanuch configurations in the workspace.
	 * <code>List</code> of <code>ILanuchConfiguratin</code>.
	 */
	private List fLaunchConfigurationIndex = new ArrayList(10);
	
	/**
	 * Constant for use as local name part of <code>QualifiedName</code>
	 * for persisting the default launcher.
	 */
	private static final String DEFAULT_LAUNCHER= "launcher"; //$NON-NLS-1$
	 
	/**
	 * Types of notifications
	 */
	private static final int REGISTERED = 0;
	private static final int DEREGISTERED = 1;

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
	 * @see ILaunchManager#addLaunchListener(ILaunchListener)
	 */
	public void addLaunchListener(ILaunchListener listener) {
		fListeners.add(listener);
	}
	
	/**
	 * @see ILaunchManager#deregisterLaunch(ILaunch)
	 */
	public void deregisterLaunch(ILaunch launch) {
		if (launch == null) {
			return;
		}
		fLaunches.remove(launch);
		fireUpdate(launch, DEREGISTERED);
	}

	/**
	 * @see ILaunchManager#findLaunch(IProcess)
	 */
	public ILaunch findLaunch(IProcess process) {
		synchronized (fLaunches) {
			for (int i= 0; i < fLaunches.size(); i++) {
				ILaunch l= (ILaunch) fLaunches.elementAt(i);
				IProcess[] ps= l.getProcesses();
				for (int j= 0; j < ps.length; j++) {
					if (ps[j].equals(process)) {
							return l;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @see ILaunchManager#findLaunch(IDebugTarget)
	 */
	public ILaunch findLaunch(IDebugTarget target) {
		synchronized (fLaunches) {
			for (int i= 0; i < fLaunches.size(); i++) {
				ILaunch l= (ILaunch) fLaunches.elementAt(i);
				if (target.equals(l.getDebugTarget())) {
					return l;
				}
			}
		}
		return null;
	}

	/**
	 * Fires notification to the listeners that a launch has been (de)registered.
	 */
	public void fireUpdate(ILaunch launch, int update) {
		Object[] copiedListeners= fListeners.getListeners();
		for (int i= 0; i < copiedListeners.length; i++) {
			ILaunchListener listener = (ILaunchListener)copiedListeners[i];
			switch (update) {
				case REGISTERED:
					listener.launchRegistered(launch);
					break;
				case DEREGISTERED:
					listener.launchDeregistered(launch);
					break;
			}
		}
	}

	/**
	 * @see ILaunchManager#getDebugTargets()
	 */
	public IDebugTarget[] getDebugTargets() {
		List targets= new ArrayList(fLaunches.size());
		if (fLaunches.size() > 0) {
			Iterator e= fLaunches.iterator();
			while (e.hasNext()) {
				IDebugTarget target= ((ILaunch) e.next()).getDebugTarget();
				if (target != null)
					targets.add(target);
			}
		}
		return (IDebugTarget[])targets.toArray(new IDebugTarget[targets.size()]);
	}

	/**
	 * @see ILaunchManager#getDefaultLauncher(IProject)
	 */
	public ILauncher getDefaultLauncher(IProject project) throws CoreException {
		ILauncher launcher= null;
		if ((project != null) && project.isOpen()) {
			String launcherID = project.getPersistentProperty(new QualifiedName(DebugPlugin.PLUGIN_ID, DEFAULT_LAUNCHER));
			if (launcherID != null) {
				launcher= getLauncher(launcherID);
			}
		}
		return launcher;
	}
		
	/**
	 * Returns the launcher with the given id, or <code>null</code>.
	 */
	public ILauncher getLauncher(String id) {
		ILauncher[] launchers= getLaunchers();
		for (int i= 0; i < launchers.length; i++) {
			if (launchers[i].getIdentifier().equals(id)) {
				return launchers[i];
			}
		}
		return null;
	}

	/**
	 * @see ILaunchManager#getLaunchers()
	 */
	public ILauncher[] getLaunchers() {
		return DebugPlugin.getDefault().getLaunchers();
	}
	
	/**
	 * @see ILaunchManager#getLaunchers(String)
	 */
	public ILauncher[] getLaunchers(String mode) {
		ILauncher[] launchers = getLaunchers();
		ArrayList list = new ArrayList();
		for (int i = 0; i < launchers.length; i++) {
			if (launchers[i].getModes().contains(mode)) {
				list.add(launchers[i]);
			}
		}
		return (ILauncher[])list.toArray(new ILauncher[list.size()]);
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
	 * @see ILaunchManager#registerLaunch(ILaunch)
	 */
	public void registerLaunch(ILaunch launch) {
		fLaunches.add(launch);
		fireUpdate(launch, REGISTERED);
	}
	
	/**
	 * @see ILaunchManager#removeLaunchListener(ILaunchListener)
	 */
	public void removeLaunchListener(ILaunchListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * @see ILaunchManager#setDefaultLauncher(IProject, ILauncher)
	 */
	public void setDefaultLauncher(IProject resource, ILauncher launcher) throws CoreException {
		String id = null;
		if (launcher != null) {
			id = launcher.getIdentifier();
		}
		resource.setPersistentProperty(new QualifiedName(DebugPlugin.PLUGIN_ID, DEFAULT_LAUNCHER), id);
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
				DebugCoreUtils.logError(e);
			}
		}
		// persist project indicies of lanuch configs
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (projects[i].isOpen()) {
				persistIndex(projects[i]);
			}
		}
		// persist local index of launch configs
		List configs = getLocalLaunchConfigurations();
		IPath path = new Path(DebugPlugin.getDefault().getStateLocation() + ".launchindex");
		persistIndex(configs, path);
		
		fLaunchConfigurationTypes.clear();
		fLaunchConfigurationIndex.clear();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}
	
	/**
	 * Creates lanuch configuration types for each defined extension.
	 * 
	 * @exception CoreException if an exception occurrs processing
	 *  the extensions
	 */
	public void startup() throws CoreException {
		IPluginDescriptor descriptor= DebugPlugin.getDefault().getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(DebugPlugin.EXTENSION_POINT_LAUNCH_CONFIGURATION_TYPES);
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
		for (int i= 0; i < infos.length; i++) {
			fLaunchConfigurationTypes.add(new LaunchConfigurationType(infos[i]));
		}		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		
		// restore project launch configuration indicies
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (projects[i].isOpen()) {
				restoreIndex(projects[i]);
			}
		}	
		// restore local launch configuration index	
		IPath path = new Path(DebugPlugin.getDefault().getStateLocation() + ".launchindex");
		restoreIndex(path);		
	}
	
	/**
	 * Returns the info object for the specified launch configuration.
	 * If the configuration exists, but is not yet in the cache,
	 * an info object is built and added to the cache.
	 * 
	 * @exception CoreException if an exception occurrs building
	 *  the info object
	 * @exception DebugException if the config does not exist
	 */
	protected LaunchConfigurationInfo getInfo(ILaunchConfiguration config) throws CoreException {
		LaunchConfigurationInfo info = (LaunchConfigurationInfo)fLaunchConfigurations.get(config.getLocation());
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
					Element root = null;
					DocumentBuilder parser =
						DocumentBuilderFactory.newInstance().newDocumentBuilder();
					root = parser.parse(new InputSource(stream)).getDocumentElement();
					info = new LaunchConfigurationInfo();
					info.initializeFromXML(root);
				} catch (FileNotFoundException e) {
					throw new DebugException(
						new Status(Status.ERROR, DebugPlugin.getDefault().getDefault().getDescriptor().getUniqueIdentifier(),
						 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e) //$NON-NLS-1$
					);					
				} catch (SAXException e) {
					throw new DebugException(
						new Status(Status.ERROR, DebugPlugin.getDefault().getDefault().getDescriptor().getUniqueIdentifier(),
						 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e) //$NON-NLS-1$
					);
				} catch (ParserConfigurationException e) {
					throw new DebugException(
						new Status(Status.ERROR, DebugPlugin.getDefault().getDefault().getDescriptor().getUniqueIdentifier(),
						 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e) //$NON-NLS-1$
					);		
				} catch (IOException e) {
					throw new DebugException(
						new Status(Status.ERROR, DebugPlugin.getDefault().getDefault().getDescriptor().getUniqueIdentifier(),
						 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e) //$NON-NLS-1$
					);										
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException e) {
							throw new DebugException(
								new Status(Status.ERROR, DebugPlugin.getDefault().getDefault().getDescriptor().getUniqueIdentifier(),
								 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_file._1"), new String[]{e.toString()}), e) //$NON-NLS-1$
							);																	
						}
					}
				}
		
			} else {
				throw new DebugException(
					new Status(
					 Status.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
					 DebugException.REQUEST_FAILED, DebugCoreMessages.getString("LaunchManager.Launch_configuration_does_not_exist._6"), null //$NON-NLS-1$
					)
				);
			}
		}
		return info;
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
	public ILaunchConfiguration[] getLaunchConfigurations() {
		return (ILaunchConfiguration[])fLaunchConfigurationIndex.toArray(new ILaunchConfiguration[fLaunchConfigurationIndex.size()]);
	}	
	
	/**
	 * @see ILaunchManager#getLaunchConfigurations(ILaunchConfigurationType)
	 */
	public ILaunchConfiguration[] getLaunchConfigurations(ILaunchConfigurationType type) throws CoreException {
		Iterator iter = fLaunchConfigurationIndex.iterator();
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
		Iterator iter = fLaunchConfigurationIndex.iterator();
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
		Iterator iter = fLaunchConfigurationIndex.iterator();
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
	public ILaunchConfiguration getLaunchConfiguration(String memento) {
		Path path = new Path(memento);
		return new LaunchConfiguration(path);
	}
	
	/**
	 * @see ILaunchManager#getLaunchConfigurationTypes()
	 */
	public ILaunchConfigurationType[] getLaunchConfigurationTypes() {
		return (ILaunchConfigurationType[])fLaunchConfigurationTypes.toArray(new ILaunchConfigurationType[fLaunchConfigurationTypes.size()]);
	}
	
	/**
	 * @see ILaunchManager#getLaunchConfigurationType(String)
	 */
	public ILaunchConfigurationType getLaunchConfigurationType(String id) {
		Iterator iter = fLaunchConfigurationTypes.iterator();
		while (iter.hasNext()) {
			ILaunchConfigurationType type = (ILaunchConfigurationType)iter.next();
			if (type.getIdentifier().equals(id)) {
				return type;
			}
		}
		return null;
	}	
	
	/**
	 * Notifies the launch manager that a launch configuration
	 * has been deleted. The configuration is removed from the
	 * cache of info's and from the index of configurations by
	 * project, and listeners are notified.
	 * 
	 * @param config the launch configuration that was deleted
	 */
	protected void launchConfigurationDeleted(ILaunchConfiguration config) {
		removeInfo(config);
		fLaunchConfigurationIndex.remove(config);
		if (fLaunchConfigurationListeners.size() > 0) {
			Object[] listeners = fLaunchConfigurationListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				ILaunchConfigurationListener listener = (ILaunchConfigurationListener)listeners[i];
				listener.launchConfigurationRemoved(config);
			}
		}
	}
	
	/**
	 * Notifies the launch manager that a launch configuration
	 * has been added. The configuration is added to the index of
	 * configurations by project, and listeners are notified.
	 * 
	 * @param config the launch configuration that was added
	 */
	protected void launchConfigurationAdded(ILaunchConfiguration config) {
		fLaunchConfigurationIndex.add(config);
		if (fLaunchConfigurationListeners.size() > 0) {
			Object[] listeners = fLaunchConfigurationListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				ILaunchConfigurationListener listener = (ILaunchConfigurationListener)listeners[i];
				listener.launchConfigurationAdded(config);
			}
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
		notifyChanged(config);
					
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
	 * Persists launch configuration index for the specified project.
	 * A file is written to the project's working area for the
	 * debug plug-in, with an entry for each launch configuration
	 * stored in the project.
	 * 
	 * @param project the project for which to persist the launch
	 *  configuration index.
	 * @exception CoreException if an exception occurrs writing the
	 * 	index
	 */
	protected void persistIndex(IProject project) throws CoreException {
		List configs = getLaunchConfigurations(project);
		IPath path = project.getPluginWorkingLocation(DebugPlugin.getDefault().getDescriptor());
		path = path.append(".launchindex"); //$NON-NLS-1$
		persistIndex(configs, path);
	}
	
	/**
	 * Persists an index of the given launch configurations,
	 * to a file in the specified location.
	 * 
	 * @param configs the configurations to persist
	 * @param location an absolute local file system path
	 * @exception CoreException if an exception occurrs writing the
	 * 	index
	 */
	protected void persistIndex(List configs, IPath path) throws CoreException {
		String xml = null;
		try {
			xml = getConfigsAsXML(configs);
		} catch (IOException e) {
			throw new DebugException(
				new Status(
				 Status.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_generating_launch_configuration_index._7"), new String[]{e.toString()}), null //$NON-NLS-1$
				)
			);					
		}
		
		try {
			File file = path.toFile();
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(xml.getBytes());
			stream.close();
		} catch (IOException e) {
			throw new DebugException(
				new Status(
				 Status.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_generating_launch_configuration_index._7"), new String[]{e.toString()}), null //$NON-NLS-1$
				)
			);				
		}
	}	
	
	/**
	 * Restores the launch configurations from the index file of
	 * the specified project.
	 * 
	 * @param project the project for which to restore launch
	 *  configurations from.
	 * @exception CoreException if an exception occurrs reading the
	 * 	index
	 */
	protected void restoreIndex(IProject project) throws CoreException {
		IPath path = project.getPluginWorkingLocation(DebugPlugin.getDefault().getDescriptor());
		path = path.append(".launchindex"); //$NON-NLS-1$
		restoreIndex(path);
	}	
	
	/**
	 * Restores the launch configurations from the index file at the
	 * specified location.
	 * 
	 * @param path absolute path to an index file in the local file system
	 * @exception CoreException if an exception occurrs reading the
	 * 	index
	 */
	protected void restoreIndex(IPath path) throws CoreException {
		InputStream stream = null;
		try {
			File file = path.toFile();
			if (!file.exists()) {
				// no index to restore
				return;
			}
			stream = new FileInputStream(file);
			Element root = null;
			DocumentBuilder parser =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			root = parser.parse(new InputSource(stream)).getDocumentElement();
			List configs = getConfigsFromXML(root);
			fLaunchConfigurationIndex.addAll(configs);
		} catch (FileNotFoundException e) {
			throw new DebugException(
				new Status(Status.ERROR, DebugPlugin.getDefault().getDefault().getDescriptor().getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_index._11"), new String[]{e.toString()}), e) //$NON-NLS-1$
			);					
		} catch (SAXException e) {
			throw new DebugException(
				new Status(Status.ERROR, DebugPlugin.getDefault().getDefault().getDescriptor().getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_index._11"), new String[]{e.toString()}), e) //$NON-NLS-1$
			);
		} catch (ParserConfigurationException e) {
			throw new DebugException(
				new Status(Status.ERROR, DebugPlugin.getDefault().getDefault().getDescriptor().getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_index._11"), new String[]{e.toString()}), e) //$NON-NLS-1$
			);		
		} catch (IOException e) {
			throw new DebugException(
				new Status(Status.ERROR, DebugPlugin.getDefault().getDefault().getDescriptor().getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_index._11"), new String[]{e.toString()}), e) //$NON-NLS-1$
			);										
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					throw new DebugException(
						new Status(Status.ERROR, DebugPlugin.getDefault().getDefault().getDescriptor().getUniqueIdentifier(),
						 DebugException.REQUEST_FAILED, MessageFormat.format(DebugCoreMessages.getString("LaunchManager.{0}_occurred_while_reading_launch_configuration_index._11"), new String[]{e.toString()}), e) //$NON-NLS-1$
					);																	
				}
			}
		}			
	}	
	
	/**
	 * Traverses the delta looking for added/removed/changed lanuch
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
				DebugCoreUtils.logError(e);
			}
		}		
	}

	/**
	 * Returns XML that can be used to persist the specified
	 * launch configurations.
	 * 
	 * @param configs list of configurations
	 * @return XML
	 * @exception IOException if an exception occurrs creating the XML
	 */
	protected String getConfigsAsXML(List configs) throws IOException {

		Document doc = new DocumentImpl();
		Element configRootElement = doc.createElement("launchConfigurations"); //$NON-NLS-1$
		doc.appendChild(configRootElement);
		
		for (int i = 0; i < configs.size(); i++) {
			ILaunchConfiguration lc = (ILaunchConfiguration)configs.get(i);
			String memento = lc.getMemento();
			Element element = doc.createElement("launchConfiguration"); //$NON-NLS-1$
			element.setAttribute("memento", memento); //$NON-NLS-1$
			configRootElement.appendChild(element);
		}

		// produce a String output
		StringWriter writer = new StringWriter();
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		Serializer serializer =
			SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
				writer,
				format);
		serializer.asDOMSerializer().serialize(doc);
		return writer.toString();
			
	}
	
	/**
	 * Returns the launch configurations specified by the given
	 * XML document.
	 * 
	 * @param root XML document
	 * @return list of launch configurations
	 * @exception IOException if an exception occurrs reading the XML
	 */	
	protected List getConfigsFromXML(Element root) throws CoreException {
		DebugException invalidFormat = 
			new DebugException(
				new Status(
				 Status.ERROR, DebugPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
				 DebugException.REQUEST_FAILED, DebugCoreMessages.getString("Invalid_launch_configuration_index._18"), null //$NON-NLS-1$
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
	 * The specified project has just openned - restore its
	 * launch configuration index.
	 * 
	 * @param project the project that has been openned
	 * @exception CoreException if reading the index fails
	 */
	protected void projectOpenned(IProject project) throws CoreException {
		restoreIndex(project);
	}
	
	/**
	 * The specified project has just closed - persist its
	 * launch configuration index.
	 * 
	 * @param project the project that has been closed
	 * @exception CoreException if writing the index fails
	 */
	protected void projectClosed(IProject project) throws CoreException {
		List configs = getLaunchConfigurations(project);
		if (!configs.isEmpty()) {
			persistIndex(project);
			fLaunchConfigurationIndex.removeAll(configs);
		}
	}	
	
	/**
	 * Visitor for handling resource deltas.
	 */
	class LaunchManagerVisitor implements IResourceDeltaVisitor {
		/**
		 * @see IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
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
							LaunchManager.this.projectOpenned(project);
						} else { 
						    LaunchManager.this.projectClosed(project);
						}
					} catch (CoreException e) {
						DebugCoreUtils.logError(e);
					}
				}
				return false;
			}
			IResource resource = delta.getResource();
			if (resource instanceof IFile) {
				IFile file = (IFile)resource;
				if (ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION.equals(file.getFileExtension())) {
					ILaunchConfiguration handle = new LaunchConfiguration(file.getLocation());
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

}
