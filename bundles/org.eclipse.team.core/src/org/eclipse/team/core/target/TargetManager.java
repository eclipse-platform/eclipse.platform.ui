/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.core.target;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.core.target.LocationMapping;

public class TargetManager {
	private static final String TARGET_SITES_FILE = ".targetSites"; //$NON-NLS-1$

	private static QualifiedName TARGET_MAPPINGS =
		new QualifiedName("org.eclipse.team.core.target", "mappings"); //$NON-NLS-1$ //$NON-NLS-2$

	private static Map factories = new Hashtable();
	private static List sites = new ArrayList();
	private static List listeners = new ArrayList();

	public static void startup() {
		ResourcesPlugin.getWorkspace().getSynchronizer().add(TARGET_MAPPINGS);
		readLocations();
	}

	public static Site[] getSites() {
		return (Site[]) sites.toArray(
			new Site[sites.size()]);
	}

	public static void addSite(Site site) {
		sites.add(site);
		save();
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			ISiteListener element = (ISiteListener) it.next();
			element.siteAdded(site);
		}
	}
	
	public static void removeSite(Site site) {
		sites.remove(site);
		save();
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			ISiteListener element = (ISiteListener) it.next();
			element.siteRemoved(site);
		}
	}

   /**
	* @see TargetProvider#map(IProject)
	*/
	public static void map(IProject project, Site site, IPath path) throws TeamException {
		try {
			ISynchronizer s = ResourcesPlugin.getWorkspace().getSynchronizer();
			byte[] mappingBytes = s.getSyncInfo(TARGET_MAPPINGS, project);
			if (mappingBytes != null) {
				throw new TeamException(Policy.bind("TargetManager.Problems_mapping_project._Project_is_already_mapped._4")); //$NON-NLS-1$
			}
			LocationMapping mapping = new LocationMapping(site, path);
			s.setSyncInfo(
				TARGET_MAPPINGS,
				project,
				mapping.encode());
		   project.setPersistentProperty(new QualifiedName("org.eclipse.team", "target"), "basic");
  		} catch (CoreException e) {
			throw new TeamException(Policy.bind("TargetManager.Problems_mapping_project", project.getName()), e); //$NON-NLS-1$
		} catch (IOException e) {
			throw new TeamException(Policy.bind("TargetManager.Problems_mapping_project", project.getName()), e); //$NON-NLS-1$
		}
	}

	/**
	 * @see TargetProvider#unmap(IProject)
	 */
	public static void unmap(IProject project) throws TeamException {
		try {
			ISynchronizer s = ResourcesPlugin.getWorkspace().getSynchronizer();
			byte[] mappingBytes = s.getSyncInfo(TARGET_MAPPINGS, project);
			if (mappingBytes == null) {
				throw new TeamException(Policy.bind("TargetManager.unableToUnmap", project.getName()), null); //$NON-NLS-1$
			} else {
				TargetProvider provider = getProvider(project);
				provider.deregister(project);
				s.flushSyncInfo(TARGET_MAPPINGS, project, IResource.DEPTH_ZERO);
			}
		   project.setPersistentProperty(new QualifiedName("org.eclipse.team", "target"), null);	// null arg removes
		} catch (CoreException e) {
			throw new TeamException(Policy.bind("TargetManager.problemsUnmapping", project.getName()), e); //$NON-NLS-1$
		}
	}

	public static TargetProvider getProvider(IProject project) throws TeamException {
		try {
			ISynchronizer s = ResourcesPlugin.getWorkspace().getSynchronizer();
			byte[] mappingBytes = s.getSyncInfo(TARGET_MAPPINGS, project);
			if (mappingBytes == null) {
				return null;
			} else {
				LocationMapping mapping = new LocationMapping(mappingBytes);
				Site site =
					getSite(mapping.getType(), mapping.getURL());
				if (site != null) {
					return site.newProvider(mapping.getPath());
				}
			}
			return null;
		} catch (CoreException e) {
			throw new TeamException(Policy.bind("TargetManager.problemsGettingProvider", project.getName()), e); //$NON-NLS-1$
		} catch (IOException e) {
			throw new TeamException(Policy.bind("TargetManager.problemsGettingProvider", project.getName()), e); //$NON-NLS-1$
		}
	}

	public static Site getSite(String type, URL url) {
		return getSite(type, url.toExternalForm());
	}

	public static Site getSite(String type, String urlID) {
		for (Iterator it = sites.iterator(); it.hasNext();) {
			Site element = (Site) it.next();
			if (element.getType().equals(type)
				&& element.getURL().toExternalForm().equals(urlID)) {
				return element;
			}
		}
		return null;
	}

	public static void addSiteListener(ISiteListener listener) {
		listeners.add(listener);
	} 
	
	public static void removeSiteListener(ISiteListener listener) {
		listeners.remove(listener);
	}

	private static void readLocations() {
		// read saved locations list from disk, only if the file exists
		IPath pluginStateLocation =
			TeamPlugin.getPlugin().getStateLocation().append(
				TARGET_SITES_FILE);
		File f = pluginStateLocation.toFile();
		if (f.exists()) {
			try {
				DataInputStream dis =
					new DataInputStream(new FileInputStream(f));
				readLocations(dis);
			} catch (IOException e) {
				TeamPlugin.log(new Status(Status.ERROR, TeamPlugin.ID, 0, Policy.bind("Config.error"), e)); //$NON-NLS-1$
			}
		}
	}

	private static void writeLocations() {
		// save repositories to disk
		IPath pluginStateLocation = TeamPlugin.getPlugin().getStateLocation();
		File tempFile = pluginStateLocation.append(TARGET_SITES_FILE + ".tmp").toFile(); //$NON-NLS-1$
		File stateFile =
			pluginStateLocation.append(TARGET_SITES_FILE).toFile();
		try {
			DataOutputStream dos =
				new DataOutputStream(new FileOutputStream(tempFile));
			writeLocations(dos);
			dos.close();
			if (stateFile.exists())
				stateFile.delete();
			boolean renamed = tempFile.renameTo(stateFile);
			if (!renamed) {
				//todo: log the error
			}
		} catch (IOException e) {
			TeamPlugin.log(new Status(Status.ERROR, TeamPlugin.ID, 0, Policy.bind("Config.error"), e)); //$NON-NLS-1$
		}
	}

	private static void save() {
		writeLocations();
	}

	private static void readLocations(DataInputStream dis) throws IOException {
		int repoCount = dis.readInt();
		for (int i = 0; i < repoCount; i++) {
			String id = dis.readUTF();
			ISiteFactory factory =
				(ISiteFactory) getSiteFactory(id);
			if (factory == null) {
				//todo: log error
				return;
			}
			Site site = factory.newSite(new ObjectInputStream(dis));
			sites.add(site);
		}
	}

	private static void writeLocations(DataOutputStream dos)	
																	throws IOException {
		dos.writeInt(sites.size());
		Iterator iter = sites.iterator();
		while (iter.hasNext()) {
			Site site = (Site) iter.next();
			dos.writeUTF(site.getType());
			site.writeObject(new ObjectOutputStream(dos));
		}
		dos.flush();
		dos.close();
	}

	public static ISiteFactory getSiteFactory(String id) {
		TeamPlugin plugin = TeamPlugin.getPlugin();
		if (plugin != null) {
			IExtensionPoint extension =
				plugin.getDescriptor().getExtensionPoint(
					TeamPlugin.TARGETS_EXTENSION);
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] configElements =
						extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						String extensionId = configElements[j].getAttribute("id"); //$NON-NLS-1$
						if (extensionId != null && extensionId.equals(id)) {
							try {
								return (ISiteFactory) configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
							} catch (CoreException e) {
								TeamPlugin.log(e.getStatus());
								return null;
							}
						}
					}
				}
			}
		}
		return null;
	}
}