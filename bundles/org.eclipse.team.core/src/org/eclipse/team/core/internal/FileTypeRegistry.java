package org.eclipse.team.core.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.core.IFileTypeRegistry;
import org.eclipse.team.core.TeamPlugin;

/**
 * This class is the temporary home of functionality to determine
 * whether a particular IResource should be treated as Text or Binary.
 */
public class FileTypeRegistry implements IFileTypeRegistry {
	// Constant for the saved state file name
	private static final String STATE_FILE = ".fileTypeState";
	
	// The registry hash table
	private Hashtable registry;
	
	/**
	 * Create a new FileTypeRegistry.
	 */
	public FileTypeRegistry() {
		this.registry = new Hashtable(11);
	}

	/**
	 * Initialize the registry, restoring its state
	 */
	public void startup() {
		loadPluginState();
	}
	
	/**
	 * Shut down the registry, persisting its state
	 */	
	public void shutdown() {
		savePluginState();
	}
	
	/**
	 * @see IFileTypeRegistry#getValue(String, String)
	 */	
	public String getValue(String extension, String key) {
		Hashtable keyTable = (Hashtable)registry.get(extension);
		if (keyTable == null) return null;
		return (String)keyTable.get(key);
	}
	/**
	 * @see IFileTypeRegistry#getExtensions
	 */	
	public String[] getExtensions(String key) {
		String[] result = new String[registry.size()];
		registry.keySet().toArray(result);
		return result;
	}
	/**
	 * @see IFileTypeRegistry#setValue
	 */	
	public void setValue(String extension, String key, String value) {
		Hashtable keyTable = (Hashtable)registry.get(extension);
		if (keyTable == null) {
			keyTable = new Hashtable();
			registry.put(extension, keyTable);
		}
		keyTable.put(key, value);
	}
	/**
	 * @see IFileTypeRegistry#containsKey
	 */	
	public boolean containsKey(String extension, String key) {
		Hashtable keyTable = (Hashtable)registry.get(extension);
		if (keyTable == null) return false;
		return ((Hashtable)keyTable).containsKey(key);
	}

	/**
	 * Reads the text patterns currently defined by extensions.
	 */
	private void initializePluginPatterns() {
		TeamPlugin plugin = TeamPlugin.getPlugin();
		if (plugin != null) {
			IExtensionPoint extension = plugin.getDescriptor().getExtensionPoint(TeamPlugin.FILE_TYPES_EXTENSION);
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						String ext = configElements[j].getAttribute("extension");
						if (ext != null) {
							String key = configElements[j].getAttribute("key");
							String value = configElements[j].getAttribute("value");
							// if this pattern doesn't already exist, add it to the registry
							if (!containsKey(ext, key)) {
								setValue(ext, key, value);
							}
						}
					}
				}
			}		
		}
	}
	
	/**
	 * Read the saved file type state from the given input stream.
	 * 
	 * @param dis  the input stream to read the saved state from
	 * @throws IOException if an I/O problem occurs
	 */
	private void readState(DataInputStream dis) throws IOException {
		registry = new Hashtable(11);
		int extensionCount = 0;
		try {
			extensionCount = dis.readInt();
		} catch (EOFException e) {
			// Ignore the exception, it will occur if there are no
			// patterns stored in the state file.
			return;
		}
		for (int i = 0; i < extensionCount; i++) {
			String extension = dis.readUTF();
			int keyCount = dis.readInt();
			for (int j = 0; j < keyCount; j++) {
				String key = dis.readUTF();
				String value = dis.readUTF();
				setValue(extension, key, value);
			}
		}
	}
	/**
	 * Write the currentstate to the given output stream.
	 * 
	 * @param dos  the output stream to write the saved state to
	 * @throws IOException if an I/O problem occurs
	 */
	private void writeState(DataOutputStream dos) throws IOException {
		dos.writeInt(registry.size());
		Iterator it = registry.keySet().iterator();
		while (it.hasNext()) {
			String extension = (String)it.next();
			dos.writeUTF(extension);
			Hashtable keyTable = (Hashtable)registry.get(extension);
			dos.writeInt(keyTable.size());
			Iterator keyIt = keyTable.keySet().iterator();
			while (keyIt.hasNext()) {
				String key = (String)keyIt.next();
				dos.writeUTF(key);
				dos.writeUTF((String)keyTable.get(key));
			}
		}
	}
	/**
	 * Load the file type registry saved state. This loads the previously saved
	 * contents, as well as discovering any values contributed by plug-ins.
	 */
	private void loadPluginState() {
		IPath pluginStateLocation = TeamPlugin.getPlugin().getStateLocation().append(STATE_FILE);
		File f = pluginStateLocation.toFile();
		if (f.exists()) {
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream(f));
				readState(dis);
				dis.close();
			} catch (IOException ex) {
				// Throw an exception here
			}
		}
		// Read values contributed by plugins
		initializePluginPatterns();
	}
	/**
	 * Save the file type registry state.
	 */
	private void savePluginState() {
		IPath pluginStateLocation = TeamPlugin.getPlugin().getStateLocation();
		File tempFile = pluginStateLocation.append(STATE_FILE + ".tmp").toFile();
		File stateFile = pluginStateLocation.append(STATE_FILE).toFile();
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));
			writeState(dos);
			dos.close();
			if (stateFile.exists() && !stateFile.delete()) {
				// Throw an exception here
			}
			boolean renamed = tempFile.renameTo(stateFile);
			if (!renamed) {
				// Throw an exception here
			}
		} catch (Exception e) {
			// Throw an exception here
		}
	}
}
