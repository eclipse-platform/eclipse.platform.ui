package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;

/** 
 * Instances of this class represent resolutions which have not yet been 
 * instantiated. Typically this situation arises when the plugin which
 * contributed the resolution has not been loaded. 
 * <p>
 * Calling <code>runWithEvent</code> will cause the resolution to be 
 * instantiated (and the plugin to load).
 * </p>
 */
public class MarkerResolutionDelegate implements IMarkerResolution {
	/**
	 * The configuration element for the resolution
	 */  
	private IConfigurationElement element;
	/**
	 * The marker for this resolution
	 */
	private IMarker marker;
	/**
	 * A short label for this resolution
	 */
	private String label;
	/**
	 * Resolution label attribute name in configuration element
	 */
	private static final String ATT_LABEL = "label"; //$NON-NLS-1$
	/**
	 * Resolution class attribute name in configuration element
	 */
	private static final String ATT_CLASS = "class"; //$NON-NLS-1$
	
	/**
	 * Creates a new instance of this class for the given 
	 * configuration element.
	 */
	public MarkerResolutionDelegate(IConfigurationElement configElement) {
		element = configElement;
		label = element.getAttribute(ATT_LABEL);
	}
	
	/* (non-Javadoc)
	 * Method declared on IMarkerResolution.
	 */
	public void init(IMarker targetMarker) {
		marker = targetMarker;
	}

	/* (non-Javadoc)
	 * Method declared on IMarkerResolution.
	 */
	public String getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * Method declared on IMarkerResolution.
	 */
	public boolean isAppropriate() {
		return true;
    } 

	/* (non-Javadoc)
	 * Method declared on IMarkerResolution.
	 */
	public void run() {
		// Instantiate the resolution
		IMarkerResolution resolution = null;
		try {
			resolution = (IMarkerResolution)element.createExecutableExtension(ATT_CLASS);						
		} catch (CoreException e) {
			WorkbenchPlugin.log("Unable to instantiate resolution", e.getStatus()); //$NON-NLS-1$
		}
		if (resolution != null) {
			resolution.init(marker);
			resolution.run();
		}
	}
}
