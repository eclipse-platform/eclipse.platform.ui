package org.eclipse.core.internal.plugins;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
/**
 * Dummy plugin runtime class implementation
 */
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.IPluginDescriptor;

public class DefaultPlugin extends Plugin {

public DefaultPlugin(IPluginDescriptor descriptor) {
	super(descriptor);
}
}
