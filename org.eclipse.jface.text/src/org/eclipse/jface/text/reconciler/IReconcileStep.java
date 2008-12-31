/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.reconciler;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.IRegion;


/**
 * A reconcile step is one of several steps of a
 * {@linkplain org.eclipse.jface.text.reconciler.IReconcilingStrategy reconcile strategy}
 * that consists of several steps. This relationship is not coded into an interface but
 * should be used by clients who's reconcile strategy consists of several steps.
 * <p>
 * If a reconcile step has an {@linkplain org.eclipse.jface.text.reconciler.IReconcilableModel input model}
 * it will compute the correct model for the next step in the chain and set the next steps
 * input model before <code>reconcile</code> gets called on that next step. After the last
 * step has reconciled the {@linkplain org.eclipse.jface.text.reconciler.IReconcileResult reconcile result}
 * array gets returned to the previous step. Each step in the chain adapts the result to its
 * input model and returns it to its previous step.
 * </p>
 * <p>
 * Example: Assume a strategy consists of steps A, B and C. And the main model is M.
 * The strategy will set M to be A's input model. What will happen is:
 * <ol>
 *	<li>A.setInputModel(M)</li>
 * 	<li>A.reconcile: A reconciles M</li>
 *	<li>A computes the model for B =&gt; MB</li>
 *	<li>B.setInputModel(MB)</li>
 * 	<li>B.reconcile: B reconciles MB</li>
 *	<li>B computes the model for C =&gt; MC</li>
 *	<li>C.setInputModel(MC)</li>
 * 	<li>C.reconcile: C reconciles MC</li>
 * 	<li>C returns result RC to step B</li>
 *	<li>B adapts the RC to MB and merges with its own results</li>
 * 	<li>B returns result RB to step A</li>
 *	<li>A adapts the result to M and merges with its own results</li>
 * 	<li>A returns the result to the reconcile strategy</li>
 * </ol>
 * </p>
 * <p>
 * This interface must be implemented by clients.
 * </p>
 * @since 3.0
 */
public interface IReconcileStep {

	/**
	 * Returns whether this is the last reconcile step or not.
	 *
	 * @return <code>true</code> iff this is the last reconcile step
	 */
	boolean isLastStep();

	/**
	 * Returns whether this is the first reconcile step or not.
	 *
	 * @return <code>true</code> iff this is the first reconcile step
	 */
	boolean isFirstStep();

	/**
	 * Sets the step which is in front of this step in the pipe.
	 * <p>
	 * Note: This method must be called at most once per reconcile step.
	 * </p>
	 *
	 * @param step the previous step
	 * @throws RuntimeException if called more than once
	 */
	void setPreviousStep(IReconcileStep step);

	/**
	 * Activates incremental reconciling of the specified dirty region.
	 * As a dirty region might span multiple content types, the segment of the
	 * dirty region which should be investigated is also provided to this
	 * reconciling strategy. The given regions refer to the document passed into
	 * the most recent call of {@link IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)}.
	 *
	 * @param dirtyRegion the document region which has been changed
	 * @param subRegion the sub region in the dirty region which should be reconciled
	 * @return an array with reconcile results
	 */
	IReconcileResult[] reconcile(DirtyRegion dirtyRegion, IRegion subRegion);

	/**
	 * Activates non-incremental reconciling. The reconciling strategy is just told
	 * that there are changes and that it should reconcile the given partition of the
	 * document most recently passed into {@link IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)}.
	 *
	 * @param partition the document partition to be reconciled
	 * @return an array with reconcile results
	 */
	IReconcileResult[] reconcile(IRegion partition);

	/**
	 * Sets the progress monitor for this reconcile step.
	 *
	 * @param monitor the progress monitor to be used
	 */
	void setProgressMonitor(IProgressMonitor monitor);

	/**
	 * Returns the progress monitor used to report progress.
	 *
	 * @return a progress monitor or <code>null</code> if no progress monitor is available
	 */
	public IProgressMonitor getProgressMonitor();

	/**
	 * Tells this reconcile step on which model it will
	 * work. This method will be called before any other method
	 * and can be called multiple times. The regions passed to the
	 * other methods always refer to the most recent model
	 * passed into this method.
	 *
	 * @param inputModel the model on which this step will work
	 */
	void setInputModel(IReconcilableModel inputModel);
}
