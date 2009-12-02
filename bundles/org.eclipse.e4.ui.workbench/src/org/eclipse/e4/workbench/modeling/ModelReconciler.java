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

package org.eclipse.e4.workbench.modeling;

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;

public abstract class ModelReconciler {

	/**
	 * Begin recording changes on the specified object. All changes contained within child elements
	 * of the object will also be recorded. When the desired changes have been captured,
	 * {@link #serialize()} should be called.
	 * 
	 * @param object
	 *            the object to monitor changes for, must not be <code>null</code>
	 */
	public abstract void recordChanges(Object object);

	/**
	 * Serializes all the changes that have been captured since the last call to
	 * {@link #recordChanges(Object)} and returns an object that can be used later with
	 * {@link #applyDeltas(Object, Object)}.
	 */
	public abstract Object serialize();

	/**
	 * Applies the captured changes to the object in question.
	 * 
	 * @param object
	 *            the object to apply changes to
	 * @param serializedState
	 *            an object that was returned from {@link #serialize()}
	 * @return a collection of operations that can be applied to alter the model to the state it was
	 *         in due to the serialized delta changes
	 */
	public abstract Collection<ModelDeltaOperation> applyDeltas(Object object,
			Object serializedState);

	protected String getModelId(Object object) {
		String id = null;

		if (object instanceof MApplicationElement) {
			id = ((MApplicationElement) object).getId();
			if (id != null) {
				return id;
			}
		}

		if (object instanceof MContribution) {
			id = ((MContribution) object).getURI();
			if (id != null) {
				return id;
			}
		}

		return null;
	}
}
