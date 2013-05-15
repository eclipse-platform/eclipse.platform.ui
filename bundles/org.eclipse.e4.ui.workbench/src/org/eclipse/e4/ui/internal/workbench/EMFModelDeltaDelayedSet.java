/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.List;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EStructuralFeature;

public class EMFModelDeltaDelayedSet extends EMFModelDeltaSet {

	private Object root;
	private String id;

	private Object value;

	public EMFModelDeltaDelayedSet(Object object, EStructuralFeature feature, Object root, String id) {
		super(object, feature, null);
		this.root = root;
		this.id = id;
	}

	@Override
	public Object getAttributeValue() {
		return value;
	}

	@Override
	public IStatus apply() {
		List<Object> references = XMLModelReconciler.getReferences(root);
		value = XMLModelReconciler.findReference(references, id);
		if (value == null) {
			return Status.CANCEL_STATUS;
		}
		return super.apply();
	}

}
