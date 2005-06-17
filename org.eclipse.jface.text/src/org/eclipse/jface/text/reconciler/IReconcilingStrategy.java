/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.reconciler;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;


/**
 * A reconciling strategy is used by an reconciler to reconcile a model
 * based on text of a particular content type. It provides methods for
 * incremental as well as non-incremental reconciling.
 * <p>
 * If a reconcile strategy consists of several steps between which
 * model transformation is desired the each step should implement
 * {@link org.eclipse.jface.text.reconciler.IReconcileStep}.
 * </p>
 * <p>
 * In order to provide backward compatibility for clients of <code>IReconcilingStrategy</code>, extension
 * interfaces are used to provide a means of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension} since version 2.0 introducing
 *		the following functions:
 *			<ul>
 *				<li>usage of a progress monitor</li>
 *				<li>initial reconciling step: if a reconciler runs as periodic activity in the background, this
 *					methods offers the reconciler a chance for initializing its strategies and achieving a
 *					reconciled state before the periodic activity starts.</li>
 *			</ul>
 * </li>
 * </ul>
 * </p>
 * <p>
 * This interface must be implemented by clients. Implementers should be
 * registered with a reconciler in order get involved in the reconciling
 * process.
 * </p>
 */
public interface IReconcilingStrategy {

	/**
	 * Tells this reconciling strategy on which document it will
	 * work. This method will be called before any other method
	 * and can be called multiple times. The regions passed to the
	 * other methods always refer to the most recent document
	 * passed into this method.
	 *
	 * @param document the document on which this strategy will work
	 */
	void setDocument(IDocument document);

	/**
	 * Activates incremental reconciling of the specified dirty region.
	 * As a dirty region might span multiple content types, the segment of the
	 * dirty region which should be investigated is also provided to this
	 * reconciling strategy. The given regions refer to the document passed into
	 * the most recent call of {@link #setDocument(IDocument)}.
	 *
	 * @param dirtyRegion the document region which has been changed
	 * @param subRegion the sub region in the dirty region which should be reconciled
	 */
	void reconcile(DirtyRegion dirtyRegion, IRegion subRegion);

	/**
	 * Activates non-incremental reconciling. The reconciling strategy is just told
	 * that there are changes and that it should reconcile the given partition of the
	 * document most recently passed into {@link #setDocument(IDocument)}.
	 *
	 * @param partition the document partition to be reconciled
	 */
	void reconcile(IRegion partition);
}
