package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.HashMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.BreakpointPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;

/*package*/ class BreakpointPropertiesAdapterFactory implements IAdapterFactory {

	/**
	 * The configuration elements for all breakpoint extensions keyed by marker type
	 */
	private static HashMap fAdapterExtensions = new HashMap(5);

	/**
	 * Load the adapter extension map only once
	 */
	static {
		IExtensionPoint ep= DebugUIPlugin.getDefault().getDescriptor().getExtensionPoint("breakpointPropertyAdapters"); //$NON-NLS-1$
		IConfigurationElement[] elements = ep.getConfigurationElements();
		for (int i= 0; i < elements.length; i++) {
			fAdapterExtensions.put(elements[i].getAttribute(IBreakpoint.MARKER_TYPE), elements[i]);
		}
	}

	/**
	 * @see IAdapterFactory#getAdapter(Object, Class)
	 * If asked for an IPropertySource adapter for an IBreakpoint, find the configuration
	 * element that corresponds to the precise type of breakpoint, then create an instance
	 * of the 'propertyAdapterClass' exectuable extension and return it.
	 */
	public Object getAdapter(Object obj, Class adapterType) {
		if (adapterType.isInstance(obj)) {
			return obj;
		}
		if (adapterType == IPropertySource.class) {
			if (obj instanceof IBreakpoint) {
				IBreakpoint breakpoint = (IBreakpoint) obj;
				IMarker marker = breakpoint.getMarker();
				if (marker.exists()) {
					try {
						IConfigurationElement config = (IConfigurationElement)fAdapterExtensions.get(marker.getType());
						IPropertySource propertySource = (IPropertySource)config.createExecutableExtension("class"); //$NON-NLS-1$
						// If no adapter was specified, use the default adapter
						if (propertySource == null) {
							propertySource = new BreakpointPropertySource();
						}
						propertySource.setPropertyValue("breakpoint", breakpoint); //$NON-NLS-1$
						return propertySource;
					} catch (CoreException ce) {
						DebugUIPlugin.logError(ce);
						return null;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @see IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] {
			IPropertySource.class
		};
	}
}

