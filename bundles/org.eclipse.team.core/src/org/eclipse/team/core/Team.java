/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.StringMatcher;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * The Team class provides a global point of reference for the global ignore set
 * and the text/binary registry.
 * 
 * @since 2.0
 */
public final class Team {
	
	public static final String PREF_TEAM_IGNORES = "ignore_files"; //$NON-NLS-1$
	public static final String PREF_TEAM_TYPES = "file_types"; //$NON-NLS-1$
	public static final String PREF_TEAM_SEPARATOR = "\n"; //$NON-NLS-1$
	public static final Status OK_STATUS = new Status(Status.OK, TeamPlugin.ID, Status.OK, Policy.bind("ok"), null); //$NON-NLS-1$
	
	// File type constants
	public static final int UNKNOWN = 0;
	public static final int TEXT = 1;
	public static final int BINARY = 2;
	
	// Keys: file extensions. Values: Integers
	private static Hashtable fileTypes, pluginTypes;

	// The ignore list that is read at startup from the persisted file
	private static Map globalIgnore, pluginIgnore;
	private static StringMatcher[] ignoreMatchers;

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
	 * Return the type of the given IStorage.
	 * 
	 * Valid return values are:
	 * Team.TEXT
	 * Team.BINARY
	 * Team.UNKNOWN
	 * 
	 * @param storage  the IStorage
	 * @return whether the given IStorage is TEXT, BINARY, or UNKNOWN
	 */
	public static int getType(IStorage storage) {
		String extension = getFileExtension(storage.getName());
		if (extension == null) return UNKNOWN;
		Hashtable table = getFileTypeTable();
		Integer integer = (Integer)table.get(extension);
		if (integer == null) return UNKNOWN;
		return integer.intValue();
	}

	/**
	 * Returns whether the given file should be ignored.
	 * 
	 * This method answers true if the file matches one of the global ignore
	 * patterns, or if the file is marked as derived.
	 * 
	 * @param file  the file
	 * @return whether the file should be ignored
	 */
	public static boolean isIgnoredHint(IResource resource) {
		if (resource.isDerived()) return true;
		return matchesEnabledIgnore(resource);
	}
	
	/**
	 * Returns whether the given file should be ignored.
	 * @deprecated use isIgnoredHint(IResource) instead
	 */
	public static boolean isIgnoredHint(IFile file) {
		if (file.isDerived()) return true;
		return matchesEnabledIgnore(file);
	}
	
	private static boolean matchesEnabledIgnore(IResource resource) {
		StringMatcher[] matchers = getStringMatchers();
		for (int i = 0; i < matchers.length; i++) {
			if (matchers[i].match(resource.getName())) return true;
		}
		return false;
	}
	
	/**
	 * Returns whether the given file should be ignored.
	 * @deprecated use isIgnoredHint instead
	 */
	public static boolean isIgnored(IFile file) {
		return matchesEnabledIgnore(file);
	}

	/**
	 * Return all known file types.
	 * 
	 * @return all known file types
	 */
	public static IFileTypeInfo[] getAllTypes() {
		List result = new ArrayList();
		Hashtable table = getFileTypeTable();
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
	public synchronized static IIgnoreInfo[] getAllIgnores() {
		if (globalIgnore == null) {
			globalIgnore = new HashMap();
			ignoreMatchers = null;
			try {
				readIgnoreState();
			} catch (TeamException e) {
				TeamPlugin.log(IStatus.ERROR, Policy.bind("Team.Error_loading_ignore_state_from_disk_1"), e); //$NON-NLS-1$
			}
			initializePluginIgnores();
		}
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

	private synchronized static StringMatcher[] getStringMatchers() {
		if (ignoreMatchers==null) {
			IIgnoreInfo[] ignorePatterns = getAllIgnores();
			Vector matchers = new Vector(ignorePatterns.length);
			for (int i = 0; i < ignorePatterns.length; i++) {
				if (ignorePatterns[i].getEnabled()) {
					matchers.add(new StringMatcher(ignorePatterns[i].getPattern(), true, false));
				}
			}
			ignoreMatchers = new StringMatcher[matchers.size()];
			matchers.copyInto(ignoreMatchers);
		}
		return ignoreMatchers;
	}
	
	private synchronized static Hashtable getFileTypeTable() {
		if (fileTypes == null) loadTextState();
		return fileTypes;
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
		if (pluginTypes == null) {
			loadTextState();
		}
		fileTypes = new Hashtable(11);
		for (int i = 0; i < extensions.length; i++) {
			fileTypes.put(extensions[i], new Integer(types[i]));
		}
		// Now set into preferences
		StringBuffer buf = new StringBuffer();
		Iterator e = fileTypes.keySet().iterator();
		while (e.hasNext()) {
			String extension = (String)e.next();
			boolean isCustom = (!pluginTypes.containsKey(extension)) ||
				!((Integer)pluginTypes.get(extension)).equals((Integer)pluginTypes.get(extension));
			if (isCustom) {
				buf.append(extension);
				buf.append(PREF_TEAM_SEPARATOR);
				Integer type = (Integer)fileTypes.get(extension);
				buf.append(type);
				buf.append(PREF_TEAM_SEPARATOR);
			}
			
		}
		TeamPlugin.getPlugin().getPluginPreferences().setValue(PREF_TEAM_TYPES, buf.toString());
	}
	
	/**
	 * Add patterns to the list of global ignores.
	 */
	public static void setAllIgnores(String[] patterns, boolean[] enabled) {
		globalIgnore = new Hashtable(11);
		ignoreMatchers = null;
		for (int i = 0; i < patterns.length; i++) {
			globalIgnore.put(patterns[i], new Boolean(enabled[i]));
		}
		// Now set into preferences
		StringBuffer buf = new StringBuffer();
		Iterator e = globalIgnore.keySet().iterator();
		while (e.hasNext()) {
			String pattern = (String)e.next();
			boolean isCustom = (!pluginIgnore.containsKey(pattern)) ||
				!((Boolean)pluginIgnore.get(pattern)).equals((Boolean)globalIgnore.get(pattern));
			if (isCustom) {
				buf.append(pattern);
				buf.append(PREF_TEAM_SEPARATOR);
				boolean en = ((Boolean)globalIgnore.get(pattern)).booleanValue();
				buf.append(en);
				buf.append(PREF_TEAM_SEPARATOR);
			}
			
		}
		TeamPlugin.getPlugin().getPluginPreferences().setValue(PREF_TEAM_IGNORES, buf.toString());
	}
	
	/**
	 * Utility method for removing a project nature from a project.
	 * 
	 * @param proj the project to remove the nature from
	 * @param natureId the nature id to remove
	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * 
	 * @deprecated
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
			throw wrapException(Policy.bind("manager.errorRemovingNature", proj.getName(), natureId), e); //$NON-NLS-1$
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
	 * 
	 * @deprecated
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
			throw wrapException(Policy.bind("manager.errorSettingNature", proj.getName(), natureId), e); //$NON-NLS-1$
		}
	}
	
	/*
	 * TEXT
	 * 
	 * Reads the text patterns currently defined by extensions.
	 */
	private static void initializePluginPatterns() {
		pluginTypes = new Hashtable();
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
							if (!fileTypes.containsKey(ext)) {
								if (type.equals("text")) { //$NON-NLS-1$
									pluginTypes.put(ext, new Integer(TEXT));
									fileTypes.put(ext, new Integer(TEXT));
								} else if (type.equals("binary")) { //$NON-NLS-1$
									fileTypes.put(ext, new Integer(BINARY));
									pluginTypes.put(ext, new Integer(BINARY));
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
			fileTypes.put(extension, new Integer(type));
		}
	}
	
	/*
	 * TEXT
	 * 
	 * Load the file type registry saved state. This loads the previously saved
	 * contents, as well as discovering any values contributed by plug-ins.
	 */
	private static void loadTextState() {
		fileTypes = new Hashtable(11);
		boolean old = loadBackwardCompatibleTextState();
		if (!old) loadTextPreferences();
		initializePluginPatterns();
		if (old) TeamPlugin.getPlugin().savePluginPreferences();
	}

	private static void loadTextPreferences() {
		Preferences pref = TeamPlugin.getPlugin().getPluginPreferences();
		if (!pref.contains(PREF_TEAM_TYPES)) return;
		String prefTypes = pref.getString(PREF_TEAM_TYPES);
		StringTokenizer tok = new StringTokenizer(prefTypes, PREF_TEAM_SEPARATOR);
		String extension, integer;
		try {
			while (true) {
				extension = tok.nextToken();
				if (extension.length()==0) return;
				integer = tok.nextToken();
				fileTypes.put(extension, Integer.valueOf(integer));
			} 
		} catch (NoSuchElementException e) {
			return;
		}
			
	}
	/*
	 * If the workspace is an old 2.0 one, read the old file and delete it
	 */
	private static boolean loadBackwardCompatibleTextState() {
		// File name of the persisted file type information
		String STATE_FILE = ".fileTypes"; //$NON-NLS-1$
		IPath pluginStateLocation = TeamPlugin.getPlugin().getStateLocation().append(STATE_FILE);
		File f = pluginStateLocation.toFile();
		if (!f.exists()) return false;
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(f));
			try {
				readTextState(dis);
			} finally {
				dis.close();
			}
		} catch (IOException ex) {
			TeamPlugin.log(Status.ERROR, ex.getMessage(), ex);
			return false;
		}
		f.delete();
		return true;
	}
	
	/*
	 * IGNORE
	 * 
	 * Reads the ignores currently defined by extensions.
	 */
	private static void initializePluginIgnores() {
		pluginIgnore = new Hashtable();
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
							String selected = configElements[j].getAttribute("enabled"); //$NON-NLS-1$
							if (selected == null) {
								// Check for selected because this used to be the field name
								selected = configElements[j].getAttribute("selected"); //$NON-NLS-1$
							}
							boolean enabled = selected != null && selected.equalsIgnoreCase("true"); //$NON-NLS-1$
							// if this ignore doesn't already exist, add it to the global list
							pluginIgnore.put(pattern, new Boolean(enabled));
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
	 * Reads global ignore preferences and populates globalIgnore
	 */
	private static void readIgnoreState() throws TeamException {
		if (readBackwardCompatibleIgnoreState()) return;
		Preferences pref = TeamPlugin.getPlugin().getPluginPreferences();
		if (!pref.contains(PREF_TEAM_IGNORES)) return;
		String prefIgnores = pref.getString(PREF_TEAM_IGNORES);
		StringTokenizer tok = new StringTokenizer(prefIgnores, PREF_TEAM_SEPARATOR);
		String pattern, enabled;
		try {
			while (true) {
				pattern = tok.nextToken();
				if (pattern.length()==0) return;
				enabled = tok.nextToken();
				globalIgnore.put(pattern, new Boolean(enabled));
			} 
		} catch (NoSuchElementException e) {
			return;
		}
	}

	/*
	 * For backward compatibility, we still look at if we have .globalIgnores
	 */
	private static boolean readBackwardCompatibleIgnoreState() throws TeamException {
		String GLOBALIGNORE_FILE = ".globalIgnores"; //$NON-NLS-1$
		IPath pluginStateLocation = TeamPlugin.getPlugin().getStateLocation().append(GLOBALIGNORE_FILE);
		File f = pluginStateLocation.toFile();
		if (!f.exists()) return false;
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(f));
			try {
				int ignoreCount = 0;
				try {
					ignoreCount = dis.readInt();
				} catch (EOFException e) {
					// Ignore the exception, it will occur if there are no ignore
					// patterns stored in the provider state file.
					return false;
				}
				for (int i = 0; i < ignoreCount; i++) {
					String pattern = dis.readUTF();
					boolean enabled = dis.readBoolean();
					globalIgnore.put(pattern, new Boolean(enabled));
				}
			} finally {
				dis.close();
			}
			f.delete();
		} catch (FileNotFoundException e) {
			// not a fatal error, there just happens not to be any state to read
		} catch (IOException ex) {
			throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("Team.readError"), ex));			 //$NON-NLS-1$
		}
		return true;
	}
	/**
	 * Initialize the registry, restoring its state.
	 * 
	 * This method is called by the plug-in upon startup, clients should not call this method
	 */
	public static void startup() throws CoreException {
		// Register a delta listener that will tell the provider about a project move
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				IResourceDelta[] projectDeltas = event.getDelta().getAffectedChildren();
				for (int i = 0; i < projectDeltas.length; i++) {							
					IResourceDelta delta = projectDeltas[i];
					IResource resource = delta.getResource();
					// Only consider project additions that are moves
					if (delta.getKind() != IResourceDelta.ADDED) continue;
					if ((delta.getFlags() & IResourceDelta.MOVED_FROM) == 0) continue;
					// Only consider projects that have a provider
					if (!RepositoryProvider.isShared(resource.getProject())) continue;
					RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject());
					if (provider == null) continue;
					// Only consider providers whose project is not mapped properly already
					if (provider.getProject().equals(resource.getProject())) continue;
					// Tell the provider about it's new project
					provider.setProject(resource.getProject());
				}
			}
		}, IResourceChangeEvent.PRE_AUTO_BUILD);
	}
	
	/**
	 * Shut down the registry, persisting its state.
	 * 
	 * This method is called by the plug-in upon shutdown, clients should not call this method
	 */	
	public static void shutdown() {
		TeamPlugin.getPlugin().savePluginPreferences();
	}
	public static IProjectSetSerializer getProjectSetSerializer(String id) {
		TeamPlugin plugin = TeamPlugin.getPlugin();
		if (plugin != null) {
			IExtensionPoint extension = plugin.getDescriptor().getExtensionPoint(TeamPlugin.PROJECT_SET_EXTENSION);
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						String extensionId = configElements[j].getAttribute("id"); //$NON-NLS-1$
						if (extensionId != null && extensionId.equals(id)) {
							try {
								return (IProjectSetSerializer)configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
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
	private static TeamException wrapException(String message, CoreException e) {
		MultiStatus status = new MultiStatus(TeamPlugin.ID, 0, message, e);
		status.merge(e.getStatus());
		return new TeamException(status);
	}
	
	private static String getFileExtension(String name) {
		if (name == null) return null;
		int index = name.lastIndexOf('.');
		if (index == -1)
			return null;
		if (index == (name.length() - 1))
			return ""; //$NON-NLS-1$
		return name.substring(index + 1);
	}
}
