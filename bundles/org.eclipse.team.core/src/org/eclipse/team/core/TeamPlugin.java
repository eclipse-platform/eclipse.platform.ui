package org.eclipse.team.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.internal.FileTypeRegistry;
import org.eclipse.team.core.internal.Policy;

/**
 * <code>TeamPlugin</code> is the plug-in runtime class for the Team 
 * resource management plugin.
 * <p>
 * This plugin provides a lightweight registration and lookup service for
 * managing global ignores patterns and file type associations. Also it 
 * serves as a boot strap for registration of RepositoryProviderType classes.
 * </p>
 * 
 * @see RepositoryProvider
 * @see RepositoryProviderType
 */
final public class TeamPlugin extends Plugin {

	// The id of the core team plug-in
	public static final String ID = "org.eclipse.team.core"; //$NON-NLS-1$

	// The id of the providers extension point
	private static final String PROVIDER_EXTENSION = "repository-provider-type"; //$NON-NLS-1$
	
	// The id of the file types extension point
	private static final String FILE_TYPES_EXTENSION = "fileTypes"; //$NON-NLS-1$
	
	// The id of the global ignore extension point
	private static final String IGNORE_EXTENSION = "ignore"; //$NON-NLS-1$
	
	// File name of the persisted global ignore patterns
	private final static String GLOBALIGNORE_FILE = ".globalIgnores"; //$NON-NLS-1$
	
	// The ignore list that is read at startup from the persisted file
	private static Map globalIgnore = new HashMap(11);

	// The file type registry
	private static FileTypeRegistry registry;
	
	// The one and only plug-in instance
	private static TeamPlugin plugin;	

	/** 
	 * Constructs a plug-in runtime class for the given plug-in descriptor.
	 */
	public TeamPlugin(IPluginDescriptor pluginDescriptor) {
		super(pluginDescriptor);
		plugin = this;
	}
	
	/**
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		try {
			Policy.localize("org.eclipse.team.core.internal.messages"); //$NON-NLS-1$
			registry = new FileTypeRegistry();
			registry.startup();
			
			// XXX forces loading of all repository plugins, may instead want to
			// lazilly initialize plugin descriptors then on demand, create the executable
			// extension.
			initializeRepositoryProviderTypes();
			readState();
			initializePluginIgnores();
		} catch(TeamException e) {
			throw new CoreException(e.getStatus());
		}
	}
	
	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() {
		registry.shutdown();
	}
	
	/**
	 * Returns the Team plug-in.
	 *
	 * @return the single instance of this plug-in runtime class
	 */
	public static TeamPlugin getPlugin() {
		return plugin;
	}
	
	/**
	 * Returns the file type registry.
	 */
	public static IFileTypeRegistry getFileTypeRegistry() {
		return registry;
	}
	
	/**
	 * Returns the plug-in's log
	 */
	public static void log(int severity, String message, Throwable e) {
		plugin.getLog().log(new Status(severity, ID, 0, message, e));
	}
	
	/**
	 * Returns the plug-in's log
	 */
	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}
	
	/**
	 * Returns the list of global ignores.
	 */
	public IIgnoreInfo[] getGlobalIgnore() {
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
	 * Add patterns to the list of global ignores.
	 */
	public void setGlobalIgnore(String[] patterns, boolean[] enabled) {
		globalIgnore = new Hashtable(11);
		for (int i = 0; i < patterns.length; i++) {
			globalIgnore.put(patterns[i], new Boolean(enabled[i]));
		}
		try {
			// make sure that we update our state on disk
			savePluginState();
		} catch (TeamException ex) {
			TeamPlugin.log(IStatus.WARNING, Policy.bind("TeamPlugin_setting_global_ignore_7"), ex); //$NON-NLS-1$
		}
	}
	
	/**
	 * Utility for adding a nature to a project.
	 */
	static public void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws TeamException {
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
	
	/**
	 * Utility for removing a project nature.
	 */
	static public void removeNatureFromProject(IProject proj, String natureId, IProgressMonitor monitor) throws TeamException {
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
	 * Find and initialize all the registered providers
	 */
	private void initializeRepositoryProviderTypes() throws TeamException {

		IExtensionPoint extensionPoint = Platform.getPluginRegistry().getExtensionPoint(TeamPlugin.ID, TeamPlugin.PROVIDER_EXTENSION);
		if (extensionPoint == null) {
			throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("manager.providerExtensionNotFound"), null)); //$NON-NLS-1$
		}

		IExtension[] extensions = extensionPoint.getExtensions();
		if (extensions.length == 0)
			return;
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] configs = extension.getConfigurationElements();
			if (configs.length == 0) {
				// there is no configuration element
				// log as an error but continue to instantiate other executable extensions.
				TeamPlugin.log(IStatus.ERROR, Policy.bind("manager.providerNoConfigElems", extension.getUniqueIdentifier()), null); //$NON-NLS-1$
				continue;
			}
			IConfigurationElement config = configs[0];
			String configName = config.getName();
			if (!"repository".equals(config.getName())) { //$NON-NLS-1$
				String message = Policy.bind("resources.natureFormat", configName); //$NON-NLS-1$
				throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, message, null));
			}

			try {
				RepositoryProviderType providerType = (RepositoryProviderType) config.createExecutableExtension("provider-type"); //$NON-NLS-1$
				RepositoryProviderType.addProviderType(providerType);
			} catch (ClassCastException e) {
				String message = Policy.bind("resources.natureImplement", configName); //$NON-NLS-1$
				throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, message, null));
			} catch (CoreException e){
				String message = Policy.bind("resources.natureImplement", configName); //$NON-NLS-1$
				throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, message, null));
			}
		}
	}
	
	/*
	 * Reads the ignores currently defined by extensions.
	 */
	private void initializePluginIgnores() {
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
	 * Save global ignore file
	 */
	private void savePluginState() throws TeamException {
		// save global ignore list to disk
		IPath pluginStateLocation = TeamPlugin.getPlugin().getStateLocation();
		File tempFile = pluginStateLocation.append(GLOBALIGNORE_FILE + ".tmp").toFile(); //$NON-NLS-1$
		File stateFile = pluginStateLocation.append(GLOBALIGNORE_FILE).toFile();
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));
			writeState(dos);
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
	 * Write the global ignores to the stream
	 */
	private void writeState(DataOutputStream dos) throws IOException {
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
	
	private void readState() throws TeamException {
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
			} catch(FileNotFoundException e) {
				// not a fatal error, there just happens not to be any state to read
			} catch (IOException ex) {
				throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("TeamPlugin_closing_stream_23"), ex));			 //$NON-NLS-1$
			}
		}
	}
}