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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedRegion;


/**
 * Standard implementation of {@link org.eclipse.jface.text.reconciler.IReconciler}.
 * The reconciler is configured with a set of {@linkplain org.eclipse.jface.text.reconciler.IReconcilingStrategy reconciling strategies}
 * each of which is responsible for a particular content type.
 * <p>
 * Usually, clients instantiate this class and configure it before using it.
 * </p>
 *
 * @see org.eclipse.jface.text.IDocumentListener
 * @see org.eclipse.jface.text.ITextInputListener
 * @see org.eclipse.jface.text.reconciler.DirtyRegion
 */
public class Reconciler extends AbstractReconciler implements IReconcilerExtension {

	/** The map of reconciling strategies. */
	private Map fStrategies;

	/**
	 * The partitioning this reconciler uses.
	 *@since 3.0
	 */
	private String fPartitioning;

	/**
	 * Creates a new reconciler with the following configuration: it is
	 * an incremental reconciler with a standard delay of 500 milliseconds. There
	 * are no predefined reconciling strategies. The partitioning it uses
	 * is the default partitioning {@link IDocumentExtension3#DEFAULT_PARTITIONING}.
	 */
	public Reconciler() {
		super();
		fPartitioning= IDocumentExtension3.DEFAULT_PARTITIONING;
	}

	/**
	 * Sets the document partitioning for this reconciler.
	 *
	 * @param partitioning the document partitioning for this reconciler
	 * @since 3.0
	 */
	public void setDocumentPartitioning(String partitioning) {
		Assert.isNotNull(partitioning);
		fPartitioning= partitioning;
	}

	/*
	 * @see org.eclipse.jface.text.reconciler.IReconcilerExtension#getDocumentPartitioning()
	 * @since 3.0
	 */
	public String getDocumentPartitioning() {
		return fPartitioning;
	}

	/**
	 * Registers a given reconciling strategy for a particular content type.
	 * If there is already a strategy registered for this type, the new strategy
	 * is registered instead of the old one.
	 *
	 * @param strategy the reconciling strategy to register, or <code>null</code> to remove an existing one
	 * @param contentType the content type under which to register
	 */
	public void setReconcilingStrategy(IReconcilingStrategy strategy, String contentType) {

		Assert.isNotNull(contentType);

		if (fStrategies == null)
			fStrategies= new HashMap();

		if (strategy == null)
			fStrategies.remove(contentType);
		else {
			fStrategies.put(contentType, strategy);
			if (strategy instanceof IReconcilingStrategyExtension && getProgressMonitor() != null) {
				IReconcilingStrategyExtension extension= (IReconcilingStrategyExtension) strategy;
				extension.setProgressMonitor(getProgressMonitor());
			}
		}
	}

	/*
	 * @see IReconciler#getReconcilingStrategy(String)
	 */
	public IReconcilingStrategy getReconcilingStrategy(String contentType) {

		Assert.isNotNull(contentType);

		if (fStrategies == null)
			return null;

		return (IReconcilingStrategy) fStrategies.get(contentType);
	}

	/**
	 * Processes a dirty region. If the dirty region is <code>null</code> the whole
	 * document is consider being dirty. The dirty region is partitioned by the
	 * document and each partition is handed over to a reconciling strategy registered
	 * for the partition's content type.
	 *
	 * @param dirtyRegion the dirty region to be processed
	 * @see AbstractReconciler#process(DirtyRegion)
	 */
	protected void process(DirtyRegion dirtyRegion) {

		IRegion region= dirtyRegion;

		if (region == null)
			region= new Region(0, getDocument().getLength());

		ITypedRegion[] regions= computePartitioning(region.getOffset(), region.getLength());

		for (int i= 0; i < regions.length; i++) {
			ITypedRegion r= regions[i];
			IReconcilingStrategy s= getReconcilingStrategy(r.getType());
			if (s == null)
				continue;

			if(dirtyRegion != null)
				s.reconcile(dirtyRegion, r);
			else
				s.reconcile(r);
		}
	}

	/*
	 * @see AbstractReconciler#reconcilerDocumentChanged(IDocument)
	 * @since 2.0
	 */
	protected void reconcilerDocumentChanged(IDocument document) {
		if (fStrategies != null) {
			Iterator e= fStrategies.values().iterator();
			while (e.hasNext()) {
				IReconcilingStrategy strategy= (IReconcilingStrategy) e.next();
				strategy.setDocument(document);
			}
		}
	}

	/*
	 * @see AbstractReconciler#setProgressMonitor(IProgressMonitor)
	 * @since 2.0
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		super.setProgressMonitor(monitor);

		if (fStrategies != null) {
			Iterator e= fStrategies.values().iterator();
			while (e.hasNext()) {
				IReconcilingStrategy strategy= (IReconcilingStrategy) e.next();
				if (strategy instanceof IReconcilingStrategyExtension) {
					IReconcilingStrategyExtension extension= (IReconcilingStrategyExtension) strategy;
					extension.setProgressMonitor(monitor);
				}
			}
		}
	}

	/*
	 * @see AbstractReconciler#initialProcess()
	 * @since 2.0
	 */
	protected void initialProcess() {
		ITypedRegion[] regions= computePartitioning(0, getDocument().getLength());
		List contentTypes= new ArrayList(regions.length);
		for (int i= 0; i < regions.length; i++) {
			String contentType= regions[i].getType();
			if( contentTypes.contains(contentType))
				continue;
			contentTypes.add(contentType);
			IReconcilingStrategy s= getReconcilingStrategy(contentType);
			if (s instanceof IReconcilingStrategyExtension) {
				IReconcilingStrategyExtension e= (IReconcilingStrategyExtension) s;
				e.initialReconcile();
			}
		}
	}

	/**
	 * Computes and returns the partitioning for the given region of the input document
	 * of the reconciler's connected text viewer.
	 *
	 * @param offset the region offset
	 * @param length the region length
	 * @return the computed partitioning
	 * @since 3.0
	 */
	private ITypedRegion[] computePartitioning(int offset, int length) {
		ITypedRegion[] regions= null;
		try {
			regions= TextUtilities.computePartitioning(getDocument(), getDocumentPartitioning(), offset, length, false);
		} catch (BadLocationException x) {
			regions= new TypedRegion[0];
		}
		return regions;
	}
}
