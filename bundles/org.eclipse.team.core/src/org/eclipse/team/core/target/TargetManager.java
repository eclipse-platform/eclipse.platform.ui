package org.eclipse.team.core.target;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.target.LocationMapping;

public class TargetManager {
	private static final String TARGET_LOCATIONS_FILE = ".targetLocations";

	private static QualifiedName TARGET_MAPPINGS =
		new QualifiedName("org.eclipse.team.core.target", "mappings");

	private static Map factories = new Hashtable();
	private static List locations = new ArrayList();

	public static void startup() {
		ResourcesPlugin.getWorkspace().getSynchronizer().add(TARGET_MAPPINGS);
		readLocations();
	}

	public static TargetLocation[] getLocations() {
		return (TargetLocation[]) locations.toArray(
			new TargetLocation[locations.size()]);
	}

	public static void addLocation(TargetLocation location) {
		locations.add(location);
		save();
	}

	/**
	* @see TargetProvider#map(IProject)
	*/
	public static void map(IProject project, TargetLocation location, IPath path) throws TeamException {
		try {
			ISynchronizer s = ResourcesPlugin.getWorkspace().getSynchronizer();
			byte[] mappingBytes = s.getSyncInfo(TARGET_MAPPINGS, project);
			if (mappingBytes != null) {
				throw new TeamException("Problems mapping project. Project is already mapped.", null);
			}
			LocationMapping mapping = new LocationMapping(location, path);
			s.setSyncInfo(
				TARGET_MAPPINGS,
				project,
				mapping.encode());
		} catch (CoreException e) {
			throw new TeamException("Problems mapping project" + project.getName(), e);
		} catch (IOException e) {
			throw new TeamException("Problems mapping project" + project.getName(), e);
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
				throw new TeamException("Unable to unmap project. It wasn't mapped to the location." + project.getName(), null);
			} else {
				TargetProvider provider = getProvider(project);
				provider.deregister(project);
				s.flushSyncInfo(TARGET_MAPPINGS, project, IResource.DEPTH_ZERO);
			}
		} catch (CoreException e) {
			throw new TeamException("Problems unmapping project" + project.getName(), e);
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
				TargetLocation location =
					getLocation(mapping.getType(), mapping.getLocationId());
				if (location != null) {
					return location.newProvider(mapping.getPath());
				}
			}
			return null;
		} catch (CoreException e) {
			throw new TeamException("Problems getting default target provider" + project.getName(), e);
		} catch (IOException e) {
			throw new TeamException("Problems getting default target provider" + project.getName(), e);
		}
	}

	public static TargetLocation getLocation(String type, String id) {
		for (Iterator it = locations.iterator(); it.hasNext();) {
			TargetLocation element = (TargetLocation) it.next();
			if (element.getType().equals(type)
				&& element.getUniqueIdentifier().equals(id)) {
				return element;
			}
		}
		return null;
	}

	private static void readLocations() {
		// read saved locations list from disk, only if the file exists
		IPath pluginStateLocation =
			TeamPlugin.getPlugin().getStateLocation().append(
				TARGET_LOCATIONS_FILE);
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
		File tempFile = pluginStateLocation.append(TARGET_LOCATIONS_FILE + ".tmp").toFile(); //$NON-NLS-1$
		File stateFile =
			pluginStateLocation.append(TARGET_LOCATIONS_FILE).toFile();
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
			String locationData = dis.readUTF();
			ILocationFactory factory =
				(ILocationFactory) getLocationFactory(id);
			if (factory == null) {
				//todo: log error
				return;
			}
			TargetLocation loc = factory.newLocation(locationData);
			locations.add(loc);
		}
	}

	private static void writeLocations(DataOutputStream dos)
		throws IOException {
		dos.writeInt(locations.size());
		Iterator iter = locations.iterator();
		while (iter.hasNext()) {
			TargetLocation loc = (TargetLocation) iter.next();
			dos.writeUTF(loc.getType());
			dos.writeUTF(loc.encode());
		}
	}

	public static ILocationFactory getLocationFactory(String id) {
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
								return (ILocationFactory) configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
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