package org.eclipse.jface.text.reconciler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.core.runtime.IProgressMonitor;

 
/**
 * Extends <code>IReconcilingStrategy</code> with new functionality.
 */
public interface IReconcilingStrategyExtension {

	/**
	 * Tells this reconciling strategy with which progress monitor
	 * it will work. This method will be called before any other 
	 * method and can be called multiple times.
	 *
	 * @param monitor the progress monitor with which this strategy will work
	 */
	void setProgressMonitor(IProgressMonitor monitor);
	
	/**
	 * Called only once in the life time of this reconciling strategy.
	 */
	void initialReconcile();
}
