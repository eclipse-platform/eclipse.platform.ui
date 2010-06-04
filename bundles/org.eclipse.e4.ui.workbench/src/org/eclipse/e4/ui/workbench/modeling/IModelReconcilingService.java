/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.modeling;

import java.util.Collection;
import org.eclipse.core.runtime.IStatus;

public interface IModelReconcilingService {

	/**
	 * Creates a model reconciler that is capable of tracking delta changes of a model and applying
	 * said changes to a model.
	 * 
	 * @return a reconciler for tracking and applying model changes
	 */
	public ModelReconciler createModelReconciler();

	/**
	 * Applies all of the deltas to the model and returns a status representing the result of the
	 * merging operation.
	 * <p>
	 * This is a convenience method, fully equivalent to <code>applyDeltas(deltas, null)</code>.
	 * </p>
	 * 
	 * @param deltas
	 *            the deltas to apply to the model
	 * @return the resulting outcome of the merge
	 */
	public IStatus applyDeltas(Collection<ModelDelta> deltas);

	/**
	 * Applies all of the deltas to the model and returns a status representing the result of the
	 * merging operation.
	 * 
	 * @param deltas
	 *            the deltas to apply to the model
	 * @param filters
	 *            a list of filters for preventing the application of a certain delta, valid
	 *            candidates are listed in {@link ModelReconciler}
	 * @return the resulting outcome of the merge
	 */
	public IStatus applyDeltas(Collection<ModelDelta> deltas, String[] filters);
}
