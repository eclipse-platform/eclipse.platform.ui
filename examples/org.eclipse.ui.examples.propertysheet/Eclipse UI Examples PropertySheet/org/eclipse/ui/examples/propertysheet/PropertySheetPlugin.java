package org.eclipse.ui.examples.propertysheet;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This is the top-level class of the property sheet example.
 *
 * @see AbstractUIPlugin for additional information on UI plugins
 */
public class PropertySheetPlugin extends AbstractUIPlugin {
	// Default instance of the receiver
	private static PropertySheetPlugin inst;
/**
 * Create the PropertySheet plugin and cache its default instance
 *
 * @param descriptor  the plugin descriptor which the receiver is made from
 */
public PropertySheetPlugin(IPluginDescriptor descriptor) {
	super(descriptor);
	if (inst==null) inst = this;
}
/**
 * Returns the plugin singleton.
 *
 * @return the default PropertySheetPlugin instance
 */
static public PropertySheetPlugin getDefault() {
	return inst;
}
}
