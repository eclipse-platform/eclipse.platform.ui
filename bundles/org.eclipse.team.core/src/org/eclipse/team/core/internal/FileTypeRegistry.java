package org.eclipse.team.core.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;

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
	private static final String STATE_FILE = ".fileTypes"; //$NON-NLS-1$
	
	// The id of the file types extension point
	private static final String FILE_TYPES_EXTENSION = "fileTypes"; //$NON-NLS-1$

	// Keys: file extensions. Values: Integers
	private Hashtable table;
	
	/**
	 * Create a new FileTypeRegistry.
	 */
	public FileTypeRegistry() {
		this.table = new Hashtable(11);
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
	 * @see IFileTypeRegistry#getType
	 */	
	public int getType(String extension) {
		Integer integer = (Integer)table.get(extension);
		if (integer == null) return UNKNOWN;
		return integer.intValue();
	}
	
	/**
	 * @see IFileTypeRegistry#getExtensions
	 */	
	public String[] getExtensions() {
		return (String[])table.keySet().toArray(new String[0]);
	}
	
	/**
	 * @see IFileTypeRegistry#setValue
	 */	
	public void setValue(String extension, int type) {
		table.put(extension, new Integer(type));
	}
	
	/**
	 * @see IFileTypeRegistry#containsKey
	 */	
	public boolean containsExtension(String extension) {
		return table.containsKey(extension);
	}

	/**
	 * Reads the text patterns currently defined by extensions.
	 */
	private void initializePluginPatterns() {
		TeamPlugin plugin = TeamPlugin.getPlugin();
		if (plugin != null) {
			IExtensionPoint extension = plugin.getDescriptor().getExtensionPoint(FILE_TYPES_EXTENSION);
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						String ext = configElements[j].getAttribute("extension"); //$NON-NLS-1$
						if (ext != null) {
							String type = configElements[j].getAttribute("type"); //$NON-NLS-1$
							// If the extension doesn't already exist, add it.
							if (!containsExtension(ext)) {
								if (type.equals("text")) { //$NON-NLS-1$
									setValue(ext, TEXT);
								} else if (type.equals("binary")) { //$NON-NLS-1$
									setValue(ext, BINARY);
								}
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
		table = new Hashtable(11);
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
			int type = dis.readInt();
			setValue(extension, type);
		}
	}
	/**
	 * Write the current state to the given output stream.
	 * 
	 * @param dos  the output stream to write the saved state to
	 * @throws IOException if an I/O problem occurs
	 */
	private void writeState(DataOutputStream dos) throws IOException {
		dos.writeInt(table.size());
		Iterator it = table.keySet().iterator();
		while (it.hasNext()) {
			String extension = (String)it.next();
			dos.writeUTF(extension);
			Integer integer = (Integer)table.get(extension);
			dos.writeInt(integer.intValue());
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
		File tempFile = pluginStateLocation.append(STATE_FILE + ".tmp").toFile(); //$NON-NLS-1$
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
