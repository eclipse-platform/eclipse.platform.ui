/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.reconciler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;

/**
 * Abstract implementation of a reconcile step.
 * 
 * @see org.eclipse.jface.text.reconciler.IReconcileStep
 * @since 3.0
 */
public abstract class AbstractReconcileStep implements IReconcileStep {

	private IReconcileStep fNextStep;
	private IReconcileStep fPreviousStep;
	private IProgressMonitor fProgressMonitor;
	protected IReconcilableModel fInputModel;

	/**
	 * Creates an intermediate reconcile step which adds
	 * the given step to the pipe.
	 * 
	 * @param step the reconcile step
	 */
	public AbstractReconcileStep(IReconcileStep step) {
		Assert.isNotNull(step);
		fNextStep= step;
		fNextStep.setPreviousStep(this);
	}

	/**
	 * Creates the last reconcile step of the pipe.
	 */
	public AbstractReconcileStep() {
	}

	public boolean isLastStep() {
		return fNextStep == null;
	}

	public boolean isFirstStep() {
		return fPreviousStep == null;
	}

	/*
	 * @see org.eclipse.text.reconcilerpipe.IReconcilerResultCollector#setProgressMonitor(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor= monitor;

		if (!isLastStep())
			fNextStep.setProgressMonitor(monitor);
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcileStep#getProgressMonitor()
	 */
	public IProgressMonitor getProgressMonitor() {
		return fProgressMonitor;
	}

	/*
	 * @see IReconcileStep#reconcile(IRegion)
	 */
	public final IReconcileResult[] reconcile(IRegion partition) {
		IReconcileResult[] result= reconcileModel(null, partition);
		if (!isLastStep()) {
			fNextStep.setInputModel(getModel());
			IReconcileResult[] nextResult= fNextStep.reconcile(partition);
			return merge(result, convertToInputModel(nextResult));
		} else
			return result;
	}

	/*
	 * @see IReconcileStep#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion, org.eclipse.jface.text.IRegion)
	 */
	public final IReconcileResult[] reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		IReconcileResult[] result= reconcileModel(dirtyRegion, subRegion);
		if (!isLastStep()) {
			fNextStep.setInputModel(getModel());
			IReconcileResult[] nextResult= fNextStep.reconcile(dirtyRegion, subRegion);
			return merge(result, convertToInputModel(nextResult));
		} else
			return result;
	}

	
	/**
	 * Reconciles the model of this reconcile step. The
	 * result is based on the input model.
	 * 
	 * @param dirtyRegion the document region which has been changed
	 * @param subRegion the sub region in the dirty region which should be reconciled
	 * @return an array with reconcile results 
	 */
	abstract protected IReconcileResult[] reconcileModel(DirtyRegion dirtyRegion, IRegion subRegion);

	protected IReconcileResult[] convertToInputModel(IReconcileResult[] inputResults) {
		return inputResults;
	}
	
	private IReconcileResult[] merge(IReconcileResult[] results1, IReconcileResult[] results2) {
		if (results1 == null)
			return results2;

		if (results2 == null)
			return results1;
		
		// XXX: not yet performance optimized 
		Collection collection= new ArrayList(Arrays.asList(results1));
		collection.addAll(Arrays.asList(results2));
		return (IReconcileResult[])collection.toArray(new IReconcileResult[collection.size()]); 
	}

	/*
	 * @see IProgressMonitor#isCanceled() 
	 */
	protected final boolean isCanceled() {
		return fProgressMonitor != null && fProgressMonitor.isCanceled();
	}

	/*
	 * @see IReconcileStep#setPreviousStep(IReconcileStep)
	 */
	public void setPreviousStep(IReconcileStep step) {
		Assert.isNotNull(step);
		Assert.isTrue(fPreviousStep == null);
		fPreviousStep= step;
	}

	/*
	 * @see IReconcileStep#setInputModel(Object)
	 */
	public void setInputModel(IReconcilableModel inputModel) {
		fInputModel= inputModel;
		
		if (!isLastStep())
			fNextStep.setInputModel(getModel());
	}

	public IReconcilableModel getInputModel() {
		return fInputModel;
	}
	
	abstract public IReconcilableModel getModel();
}
