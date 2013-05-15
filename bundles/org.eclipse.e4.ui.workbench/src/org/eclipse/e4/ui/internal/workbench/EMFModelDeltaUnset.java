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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public final class EMFModelDeltaUnset extends ModelDelta {

	private final EStructuralFeature feature;

	public EMFModelDeltaUnset(EObject eObject, EStructuralFeature feature) {
		super(eObject, feature.getName(), null);
		this.feature = feature;
	}

	public IStatus apply() {
		EObject eObject = (EObject) getObject();
		eObject.eUnset(feature);
		return Status.OK_STATUS;
	}

}
