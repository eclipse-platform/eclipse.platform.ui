package org.eclipse.core.internal.plugins;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
