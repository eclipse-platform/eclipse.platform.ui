package org.eclipse.ui.tests.statushandlers.handlers;

import org.eclipse.core.internal.registry.osgi.OSGIUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.osgi.framework.Bundle;

/**
 * An EclipseStatus retrieves the bundle which sent the embedded status.
 * 
 */
public class EclipseStatus extends MultiStatus implements IStatus {

	private Bundle bundle;
	private String location;

	/**
	 * @param pluginId
	 * @param code
	 * @param newChildren
	 * @param message
	 * @param exception
	 */
	public EclipseStatus(String pluginId, int code, IStatus[] newChildren,
			String message, Throwable exception) {
		super(pluginId, code, newChildren, message, exception);
		initLocation(pluginId);
	}

	/**
	 * @param pluginId
	 * @param code
	 * @param message
	 * @param exception
	 */
	public EclipseStatus(String pluginId, int code, String message,
			Throwable exception) {
		super(pluginId, code, message, exception);
		initLocation(pluginId);
	}

	private void initLocation(String pluginId) {
		bundle = OSGIUtils.getDefault().getBundle(pluginId);
		if (bundle != null) {
			location = bundle.getLocation();
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(location);
		buf.append(super.toString());
		return buf.toString();
	}
}
