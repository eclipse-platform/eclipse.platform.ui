package org.eclipse.update.configuration;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.Date;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.IFeatureReference;

/**
 * Installation Change.
 * Represents the changes the reconciler found.
 */
public interface ISessionDelta extends IAdaptable {

	/**
	 * Returns the list of Features found during reconciliation
	 * 
	 * @return an array of feature references, or an empty array
	 * @since 2.0 
	 */
	public IFeatureReference[] getFeatureReferences();

	/**
	 * Returns the date the reconciliation occured
	 * 
	 * @return the date of the reconciliation
	 * @since 2.0 
	 */
	public Date getDate();

	/**
	 * Configure or unconfigure all the feature references of the 
	 * Session Delta.
	 * @param configure true if the feature reference must be configured, false if they must be unconfigured
	 * @param pm the progress monitor
	 * @throws CoreException if an error occurs. 
	 * @since 2.0 
	 */
	public void configureSessionDelta(boolean configure, IProgressMonitor pm) throws CoreException;
}