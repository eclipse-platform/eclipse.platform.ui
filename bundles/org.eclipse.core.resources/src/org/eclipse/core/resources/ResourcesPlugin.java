package org.eclipse.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Assert;
import java.io.File;

/**
 * The plug-in runtime class for the Resources plug-in.  This is
 * the starting point for all workspace and resource manipulation.
 * A typical sequence of events would be for a dependent plug-in
 * to call <code>ResourcesPlugin.getWorkspace()</code>.
 * Doing so would cause this plug-in to be activated and the workspace
 * (if any) to be loaded from disk and initialized.
 */
public final class ResourcesPlugin extends Plugin {
	/**
	 * Unique identifier constant (value <code>"org.eclipse.core.resources"</code>)
	 * for the standard Resources plug-in.
	 */
	public static final String PI_RESOURCES = "org.eclipse.core.resources";

	/*====================================================================
	 * Constants defining the ids of the standard workspace extension points:
	 *====================================================================*/

	/**
	 * Simple identifier constant (value <code>"builders"</code>)
	 * for the builders extension point.
	 */
	public static final String PT_BUILDERS = "builders";
	
	/**
	 * Simple identifier constant (value <code>"natures"</code>)
	 * for the natures extension point.
	 */
	public static final String PT_NATURES = "natures";

	/**
	 * Simple identifier constant (value <code>"markers"</code>)
	 * for the markers extension point.
	 */
	public static final String PT_MARKERS = "markers";

	/**
	 * Simple identifier constant (value <code>"fileModificationValidator"</code>)
	 * for the file modification validator extension point.
	 */
	public static final String PT_FILE_MODIFICATION_VALIDATOR = "fileModificationValidator";

	/**
	 * Simple identifier constant (value <code>"moveDeleteHook"</code>)
	 * for the move/delete hook extension point.
	 * 
	 * @since 2.0
	 */
	public static final String PT_MOVE_DELETE_HOOK = "moveDeleteHook";

	/**
	 * Name of a preference indicating the encoding to use when reading text 
	 * files in the workspace.  The value is a string, and may 
	 * be the default empty string, indicating that the file system encoding should
	 * be used instead.  The file system encoding can be retrieved using
	 * <code>System.getProperty("file.encoding")</code>.
	 * There is also a convenience method <code>getEncoding</code> which returns
	 * the value of this preference, or the file system encoding if this 
	 * preference is not set.
	 * <p>
	 * Note that there is no guarantee that the value is a supported encoding.
	 * Callers should be prepared to handle <code>UnsupportedEncodingException</code>
	 * where this encoding is used.
	 * </p>
	 * 
	 * @see #getEncoding()
	 * @see java.io.UnsupportedEncodingException
	 */
	public static final String PREF_ENCODING = "encoding";
	
	/**
	 * The single instance of this plug-in runtime class.
	 */
	private static ResourcesPlugin plugin;

	/**
	 * The workspace managed by the single instance of this
	 * plug-in runtime class, or <code>null</code> is there is none.
	 */
	private static Workspace workspace = null;
/** 
 * Constructs an instance of this plug-in runtime class.
 * <p>
 * An instance of this plug-in runtime class is automatically created 
 * when the facilities provided by the Resources plug-in are required.
 * <b>Cliens must never explicitly instantiate a plug-in runtime class.</b>
 * </p>
 * 
 * @param pluginDescriptor the plug-in descriptor for the
 *   Resources plug-in
 */
public ResourcesPlugin(IPluginDescriptor pluginDescriptor) {
	super(pluginDescriptor);
	plugin = this;
}
/**
 * Constructs a brand new workspace structure at the location in the local file system
 * identified by the given path and returns a new workspace object.
 * 
 * @exception CoreException if the workspace structure could not be constructed.
 * Reasons include:
 * <ll>
 * <li> There is an existing workspace structure on at the given location
 *      in the local file system.
 * <li> A file exists at the given location in the local file system.
 * <li> A directory could not be created at the given location in the
 *      local file system.
 * </ll>
 * @see #containsWorkspace
 */
private static void constructWorkspace() throws CoreException {
	WorkspaceDescription description = Workspace.defaultWorkspaceDescription();
	new LocalMetaArea().write(description);
}

/**
 * Returns the encoding to use when reading text files in the workspace.
 * This is the value of the <code>PREF_ENCODING</code> preference, or the
 * file system encoding (<code>System.getProperty("file.encoding")</code>)
 * if the preference is not set.
 * <p>
 * Note that this method does not check whether the result is a supporteed
 * encoding.  Callers should be prepared to handle 
 * <code>UnsupportedEncodingException</code> where this encoding is used.
 * 
 * @see java.io.UnsupportedEncodingException
 */
public static String getEncoding() {
	String enc = getPlugin().getPluginPreferences().getString(PREF_ENCODING);
	if (enc == null || enc.length() == 0) {
		enc = System.getProperty("file.encoding");
	}
	return enc;
}

/**
 * Returns the Resources plug-in.
 *
 * @return the single instance of this plug-in runtime class
 */
public static ResourcesPlugin getPlugin() {
	return plugin;
}
/**
 * Returns the workspace.
 *
 * @return the workspace that was created by the single instance of this
 *   plug-in runtime class
 */
public static IWorkspace getWorkspace() {
	return workspace;
}
/**
 * This implementation of the corresponding <code>Plugin</code> method
 * closes the workspace (without saving).
 * @see Plugin#shutdown
 */
public void shutdown() throws CoreException {
	if (workspace == null) {
		return;
	}
	workspace.close(null);
	
	/* Forget workspace only if successfully closed, to
	 * make it easier to debug cases where close() is failing.
	 */
	workspace = null;
}
/**
 * This implementation of the corresponding <code>Plugin</code> method
 * opens the workspace.
 * @see Plugin#startup
 */
public void startup() throws CoreException {
	if (!new LocalMetaArea().hasSavedWorkspace()) {
		constructWorkspace();
	}
	Workspace.DEBUG = ResourcesPlugin.getPlugin().isDebugging();
	// Remember workspace before opening, to
	// make it easier to debug cases where open() is failing.
	workspace = new Workspace();
	PlatformURLResourceConnection.startup(Platform.getLocation());
	IStatus result = workspace.open(null);
	if (!result.isOK())
		getLog().log(result);
}
}
