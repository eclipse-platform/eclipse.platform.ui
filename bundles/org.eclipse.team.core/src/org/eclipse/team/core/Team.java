package org.eclipse.team.core;

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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.internal.Policy;
import org.eclipse.team.core.internal.StringMatcher;
import org.omg.CORBA.UNKNOWN;

/**
 * The Team class provides a global point of reference for the global ignore set
 * and the text/binary registry.
 */
public final class Team {
	// File type constants
	public static final int UNKNOWN = 0;
	public static final int TEXT = 1;
	public static final int BINARY = 2;
	
	// File name of the persisted file type information
	private static final String STATE_FILE = ".fileTypes"; //$NON-NLS-1$
	
	// File name of the persisted global ignore patterns
	private final static String GLOBALIGNORE_FILE = ".globalIgnores"; //$NON-NLS-1$

	// Keys: file extensions. Values: Integers
	private static Hashtable table;

	// The ignore list that is read at startup from the persisted file
	private static Map globalIgnore = new HashMap(11);

	private static class FileTypeInfo implements IFileTypeInfo {
		private String extension;
		private int type;
		
		public FileTypeInfo(String extension, int type) {
			this.extension = extension;
			this.type = type;
		}
		public String getExtension() {
			return extension;
		}
		public int getType() {
			return type;
		}
	}
	
	private static class IgnoreInfo implements IIgnoreInfo {
		private String pattern;
		private boolean enabled;
		
		public IgnoreInfo(String pattern, boolean enabled) {
			this.pattern = pattern;
			this.enabled = enabled;
		}
		public String getPattern() {
			return pattern;
		}
		public boolean getEnabled() {
			return enabled;
		}
	};
	
	/**
	 * Return the type of the given file.
	 * 
	 * Valid return values are:
	 * Team.TEXT
	 * Team.BINARY
	 * Team.UNKNOWN
	 * 
	 * @param file  the file
	 * @return whether files with the given extension are TEXT, BINARY, or UNKNOWN
	 */
	public static int getType(IFile file) {
		String extension = file.getFileExtension();
		if (extension == null) return UNKNOWN;
		Integer integer = (Integer)table.get(extension);
		if (integer == null) return UNKNOWN;
		return integer.intValue();
	}

	/**
	 * Returns whether the given file is ignored by any of the global ignore patterns.
	 */
	public static boolean isIgnored(IFile file) {
		IIgnoreInfo[] ignorePatterns = getAllIgnores();
		StringMatcher matcher;
		for (int i = 0; i < ignorePatterns.length; i++) {
			IIgnoreInfo info = ignorePatterns[i];
			if (info.getEnabled()) {
				matcher = new StringMatcher(info.getPattern(), true, false);
				if (matcher.match(file.getName())) return true;
			}
		}
		return false;
	}

	/**
	 * Return all known file types.
	 * 
	 * @return all known file types
	 */
	public static IFileTypeInfo[] getAllTypes() {
		List result = new ArrayList();
		Enumeration e = table.keys();
		while (e.hasMoreElements()) {
			String string = (String)e.nextElement();
			int type = ((Integer)table.get(string)).intValue();
			result.add(new FileTypeInfo(string, type));
		}
		return (IFileTypeInfo[])result.toArray(new IFileTypeInfo[result.size()]);
	}
	
	/**
	 * Returns the list of global ignores.
	 */
	public static IIgnoreInfo[] getAllIgnores() {
		IIgnoreInfo[] result = new IIgnoreInfo[globalIgnore.size()];
		Iterator e = globalIgnore.keySet().iterator();
		int i = 0;
		while (e.hasNext() ) {
			final String pattern = (String)e.next();
			final boolean enabled = ((Boolean)globalIgnore.get(pattern)).booleanValue();
			result[i++] = new IIgnoreInfo() {
				private String p = pattern;
				private boolean e = enabled;
				public String getPattern() {
					return p;
				}
				public boolean getEnabled() {
					return e;
				}
			};
		}
		return result;
	}

	/**
	 * Set the file type for the give extension to the given type.
	 *
	 * Valid types are:
	 * Team.TEXT
	 * Team.BINARY
	 * Team.UNKNOWN
	 * 
	 * @param extension  the file extension
	 * @param type  the file type
	 */
	public static void setAllTypes(String[] extensions, int[] types) {
		table = new Hashtable(11);
		for (int i = 0; i < extensions.length; i++) {
			table.put(extensions[i], new Integer(types[i]));
		}
	}
	
	/**
	 * Add patterns to the list of global ignores.
	 */
	public static void setAllIgnores(String[] patterns, boolean[] enabled) {
		globalIgnore = new Hashtable(11);
		for (int i = 0; i < patterns.length; i++) {
			globalIgnore.put(patterns[i], new Boolean(enabled[i]));
		}
	}
	
	/**
	 * Utility method for removing a project nature from a project.
	 * 
	 * @param proj the project to remove the nature from
	 * @param natureId the nature id to remove
	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 */
	public static void removeNatureFromProject(IProject proj, String natureId, IProgressMonitor monitor) throws TeamException {
		try {
			IProjectDescription description = proj.getDescription();
			String[] prevNatures= description.getNatureIds();
			List newNatures = new ArrayList(Arrays.asList(prevNatures));
			newNatures.remove(natureId);
			description.setNatureIds((String[])newNatures.toArray(new String[newNatures.size()]));
			proj.setDescription(description, monitor);
		} catch(CoreException e) {
				throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("manager.errorRemovingNature",  //$NON-NLS-1$
														 proj.getName(), natureId), e));
		}
	}
	
	/**
	 * Utility method for adding a nature to a project.
	 * 
	 * @param proj the project to add the nature
	 * @param natureId the id of the nature to assign to the project
	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * 
	 * @exception TeamException if a problem occured setting the nature
	 */
	public static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws TeamException {
		try {
			IProjectDescription description = proj.getDescription();
			String[] prevNatures= description.getNatureIds();
			String[] newNatures= new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length]= natureId;
			description.setNatureIds(newNatures);
			proj.setDescription(description, monitor);
		} catch(CoreException e) {
				throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("manager.errorSettingNature",  //$NON-NLS-1$
														 proj.getName(), natureId), e));
		}
	}
	
	/*
	 * TEXT
	 * 
	 * Reads the text patterns currently defined by extensions.
	 */
	private static void initializePluginPatterns() {
		TeamPlugin plugin = TeamPlugin.getPlugin();
		if (plugin != null) {
			IExtensionPoint extension = plugin.getDescriptor().getExtensionPoint(TeamPlugin.FILE_TYPES_EXTENSION);
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						String ext = configElements[j].getAttribute("extension"); //$NON-NLS-1$
						if (ext != null) {
							String type = configElements[j].getAttribute("type"); //$NON-NLS-1$
							// If the extension doesn't already exist, add it.
							if (!table.containsKey(ext)) {
								if (type.equals("text")) { //$NON-NLS-1$
									table.put(ext, new Integer(TEXT));
								} else if (type.equals("binary")) { //$NON-NLS-1$
									table.put(ext, new Integer(BINARY));
								}
							}
						}
					}
				}
			}		
		}
	}
	
	/*
	 * TEXT
	 * 
	 * Read the saved file type state from the given input stream.
	 * 
	 * @param dis  the input stream to read the saved state from
	 * @throws IOException if an I/O problem occurs
	 */
	private static void readTextState(DataInputStream dis) throws IOException {
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
			table.put(extension, new Integer(type));
		}
	}
	
	/*
	 * TEXT
	 * 
	 * Write the current state to the given output stream.
	 * 
	 * @param dos  the output stream to write the saved state to
	 * @throws IOException if an I/O problem occurs
	 */
	private static void writeTextState(DataOutputStream dos) throws IOException {
		dos.writeInt(table.size());
		Iterator it = table.keySet().iterator();
		while (it.hasNext()) {
			String extension = (String)it.next();
			dos.writeUTF(extension);
			Integer integer = (Integer)table.get(extension);
			dos.writeInt(integer.intValue());
		}
	}
	
	/*
	 * TEXT
	 * 
	 * Load the file type registry saved state. This loads the previously saved
	 * contents, as well as discovering any values contributed by plug-ins.
	 */
	private static void loadTextState() {
		IPath pluginStateLocation = TeamPlugin.getPlugin().getStateLocation().append(STATE_FILE);
		File f = pluginStateLocation.toFile();
		if (f.exists()) {
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream(f));
				readTextState(dis);
				dis.close();
			} catch (IOException ex) {
				// Throw an exception here
			}
		}
		// Read values contributed by plugins
		initializePluginPatterns();
	}
	
	/*
	 * TEXT
	 * 
	 * Save the file type registry state.
	 */
	private static void saveTextState() {
		IPath pluginStateLocation = TeamPlugin.getPlugin().getStateLocation();
		File tempFile = pluginStateLocation.append(STATE_FILE + ".tmp").toFile(); //$NON-NLS-1$
		File stateFile = pluginStateLocation.append(STATE_FILE).toFile();
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));
			writeTextState(dos);
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
	
	/*
	 * IGNORE
	 * 
	 * Reads the ignores currently defined by extensions.
	 */
	private static void initializePluginIgnores() {
		TeamPlugin plugin = TeamPlugin.getPlugin();
		if (plugin != null) {
			IExtensionPoint extension = plugin.getDescriptor().getExtensionPoint(TeamPlugin.IGNORE_EXTENSION);
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						String pattern = configElements[j].getAttribute("pattern"); //$NON-NLS-1$
						if (pattern != null) {
							String selected = configElements[j].getAttribute("selected"); //$NON-NLS-1$
							boolean enabled = selected != null && selected.equalsIgnoreCase("true"); //$NON-NLS-1$
							// if this ignore doesn't already exist, add it to the global list
							if (!globalIgnore.containsKey(pattern)) {
								globalIgnore.put(pattern, new Boolean(enabled));
							}
						}
					}
				}
			}		
		}
	}
	
	/*
	 * IGNORE
	 * 
	 * Save global ignore file
	 */
	private static void saveIgnoreState() throws TeamException {
		// save global ignore list to disk
		IPath pluginStateLocation = TeamPlugin.getPlugin().getStateLocation();
		File tempFile = pluginStateLocation.append(GLOBALIGNORE_FILE + ".tmp").toFile(); //$NON-NLS-1$
		File stateFile = pluginStateLocation.append(GLOBALIGNORE_FILE).toFile();
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));
			writeIgnoreState(dos);
			dos.close();
			if (stateFile.exists())
				stateFile.delete();
			boolean renamed = tempFile.renameTo(stateFile);
			if (!renamed)
				throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("TeamPlugin_renaming_21"), null)); //$NON-NLS-1$
		} catch (IOException ex) {
			throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("TeamPlugin_closing_stream_22"), ex)); //$NON-NLS-1$
		}
	}
	
	/*
	 * IGNORE
	 * 
	 * Write the global ignores to the stream
	 */
	private static void writeIgnoreState(DataOutputStream dos) throws IOException {
		// write the global ignore list
		int ignoreLength = globalIgnore.size();
		dos.writeInt(ignoreLength);
		Iterator e = globalIgnore.keySet().iterator();
		while (e.hasNext()) {
			String pattern = (String)e.next();
			boolean enabled = ((Boolean)globalIgnore.get(pattern)).booleanValue();
			dos.writeUTF(pattern);
			dos.writeBoolean(enabled);
		}
	}
	
	/*
	 * IGNORE
	 * 
	 * Reads the global ignore file
	 */
	private static void readIgnoreState() throws TeamException {
		// read saved repositories list and ignore list from disk, only if the file exists
		IPath pluginStateLocation = TeamPlugin.getPlugin().getStateLocation().append(GLOBALIGNORE_FILE);
		File f = pluginStateLocation.toFile();
		if(f.exists()) {
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream(f));
				globalIgnore = new Hashtable(11);
				int ignoreCount = 0;
				try {
					ignoreCount = dis.readInt();
				} catch (EOFException e) {
					// Ignore the exception, it will occur if there are no ignore
					// patterns stored in the provider state file.
					return;
				}
				for (int i = 0; i < ignoreCount; i++) {
					String pattern = dis.readUTF();
					boolean enabled = dis.readBoolean();
					globalIgnore.put(pattern, new Boolean(enabled));
				}
			} catch (FileNotFoundException e) {
				// not a fatal error, there just happens not to be any state to read
			} catch (IOException ex) {
				throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("TeamPlugin_closing_stream_23"), ex));			 //$NON-NLS-1$
			}
		}
	}

	/*
	 * Initialize the registry, restoring its state
	 */
	static void startup() throws CoreException {
		try {
			table = new Hashtable(11);
			loadTextState();
			readIgnoreState();
			initializePluginIgnores();
		} catch (TeamException e) {
			throw new CoreException(e.getStatus());
		}
	}
	
	/*
	 * Shut down the registry, persisting its state
	 */	
	static void shutdown() {
		saveTextState();
		try {
			// make sure that we update our state on disk
			saveIgnoreState();
		} catch (TeamException ex) {
			TeamPlugin.log(IStatus.WARNING, Policy.bind("TeamPlugin_setting_global_ignore_7"), ex); //$NON-NLS-1$
		}
	}
}
