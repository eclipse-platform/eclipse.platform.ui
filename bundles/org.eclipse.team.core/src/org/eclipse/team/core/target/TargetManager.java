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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.*;
import org.eclipse.team.internal.core.*;
import org.eclipse.team.core.target.*;

/**
 * @version 	1.0
 * @author
 */
public class TargetManager {
	private static final String TARGET_LOCATIONS_FILE = ".targetLocations";

	private static Map factories = new Hashtable();
	private static List locations = new ArrayList();
	
	
	public static ITargetFactory getFactory(String id) {
		ITargetFactory factory = (ITargetFactory) factories.get(id);
		if(factory == null) {
			factory = getTargetFactory(id);
			if(factory != null) {
				factories.put(id, factory);
				return factory;
			}
		}
		return null;
	}
	
	public static void startup() {
		readLocations();
	}

	public static void save() {
		writeLocations();
	}

	public static TargetLocation[] getLocations() {
		return (TargetLocation[]) locations.toArray(new TargetLocation[locations.size()]);
	}
	
	public static void add(TargetLocation location) {
		locations.add(location);
	}
		
	private static void readLocations() {
		// read saved locations list from disk, only if the file exists
		IPath pluginStateLocation = TeamPlugin.getPlugin().getStateLocation().append(TARGET_LOCATIONS_FILE);
		File f = pluginStateLocation.toFile();
		if( f.exists() ) {
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream(f));
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
		File stateFile = pluginStateLocation.append(TARGET_LOCATIONS_FILE).toFile();
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));
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

	private static void readLocations(DataInputStream dis) throws IOException {
		int repoCount = dis.readInt();
		for (int i = 0; i < repoCount; i++) {
			String type = dis.readUTF();
			String locationData = dis.readUTF();
			ITargetFactory factory = getFactory(type);
			if(factory == null) {
				//todo: log error
				return;
			}
			TargetLocation loc = factory.decode(locationData);
			locations.add(loc);
		}
	}
	
	private static void writeLocations(DataOutputStream dos) throws IOException {
		dos.writeInt(locations.size());
		Iterator iter = locations.iterator();
		while(iter.hasNext()) {
			TargetLocation loc = (TargetLocation) iter.next();
			dos.writeUTF(loc.getType());
			dos.writeUTF(loc.encode());
		}
	}
			
	private static ITargetFactory getTargetFactory(String id) {
		TeamPlugin plugin = TeamPlugin.getPlugin();
		if (plugin != null) {
			IExtensionPoint extension = plugin.getDescriptor().getExtensionPoint(TeamPlugin.TARGETS_EXTENSION);
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						String extensionId = configElements[j].getAttribute("id"); //$NON-NLS-1$
						if (extensionId != null && extensionId.equals(id)) {
							try {
								return (ITargetFactory) configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
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
