package org.eclipse.ant.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.Task;

/**
 * The plug-in runtime class for the Releng plug-in.
 */
public final class AntPlugin extends Plugin {
	/**
	 * The single instance of this plug-in runtime class.
	 */
	private static AntPlugin plugin;

	/**
	 * Table of Ant tasks added through the antTasks extension point
	 */
	private Map taskExtensions;

	/**
	 * Table of Ant objects added through the antObjects extension point
	 */
	private Map objectExtensions;

	/**
	 * Table of Ant types added through the antTypes extension point
	 */
	private Map typeExtensions;

	/**
	 * Unique identifier constant (value <code>"org.eclipse.ant.core"</code>)
	 * for the Ant plug-in.
	 */
	public static final String PI_ANT= "org.eclipse.ant.core";

	/**
	 * Simple identifier constant (value <code>"antTasks"</code>)
	 * for the Ant tasks extension point.
	 */
	public static final String PT_ANTTASKS = "antTasks";

	/**
	 * Simple identifier constant (value <code>"antTypes"</code>)
	 * for the Ant types extension point.
	 */
	public static final String PT_ANTTYPES = "antTypes";

	/**
	 * Simple identifier constant (value <code>"antObjects"</code>)
	 * for the Ant objects extension point.
	 */
	public static final String PT_ANTOBJECTS = "antObjects";

	protected static final String CLASS = "class";
	protected static final String NAME = "name";
	/** 
	 * Constructs an instance of this plug-in runtime class.
	 * <p>
	 * Instances of plug-in runtime classes are automatically created 
	 * by the platform in the course of plug-in activation.
	 * <b>No client or plug-in should ever explicitly instantiate
	 * a plug-in runtime class</b>.
	 * </p>
	 * 
	 * @param pluginDescriptor the plug-in descriptor for the
	 *   Resources plug-in
	 */
	public AntPlugin(IPluginDescriptor pluginDescriptor) {
		super(pluginDescriptor);
		plugin= this;
	}
public Map getObjectExtensions() {
	return objectExtensions;
}
	/**
	 * Returns this plug-in.
	 *
	 * @return the single instance of this plug-in runtime class
	 */
	public static AntPlugin getPlugin() {
		return plugin;
	}
public Map getTaskExtensions() {
	return taskExtensions;
}
public Map getTypeExtensions() {
	return typeExtensions;
}
	/**
	 * This implementation of the corresponding <code>Plugin</code> method
	 */
	public void shutdown() throws CoreException {
	}
/**
 * This implementation of the corresponding <code>Plugin</code> method
 */
public void startup() throws CoreException {
	IExtensionPoint extensionPoint = getDescriptor().getExtensionPoint(PT_ANTTASKS);
	if (extensionPoint != null) {
		IConfigurationElement[] extensions = extensionPoint.getConfigurationElements();
		taskExtensions = new HashMap(extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			String name = extensions[i].getAttribute(NAME);
			taskExtensions.put(name, extensions[i]);
		}
	}
	extensionPoint = getDescriptor().getExtensionPoint(PT_ANTTYPES);
	if (extensionPoint != null) {
		IConfigurationElement[] extensions = extensionPoint.getConfigurationElements();
		typeExtensions = new HashMap(extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			String name = extensions[i].getAttribute(NAME);
			typeExtensions.put(name, extensions[i]);
		}
	}
	extensionPoint = getDescriptor().getExtensionPoint(PT_ANTOBJECTS);
	if (extensionPoint != null) {
		IConfigurationElement[] extensions = extensionPoint.getConfigurationElements();
		objectExtensions = new HashMap(extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			String name = extensions[i].getAttribute(NAME);
			objectExtensions.put(name, extensions[i]);
		}
	}
}
}
