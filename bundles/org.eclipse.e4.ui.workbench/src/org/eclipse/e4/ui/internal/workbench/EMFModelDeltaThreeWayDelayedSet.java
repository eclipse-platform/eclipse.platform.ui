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
import org.eclipse.emf.ecore.EStructuralFeature;

public final class EMFModelDeltaThreeWayDelayedSet extends EMFModelDeltaSet {

	private List<?> originalReferences;
	private List<?> userReferences;
	private List<?> currentReferences;

	public EMFModelDeltaThreeWayDelayedSet(Object object, EStructuralFeature feature,
			List<?> originalReferences, List<?> userReferences, List<?> currentReferences) {
		super(object, feature, null);
		this.originalReferences = originalReferences;
		this.userReferences = userReferences;
		this.currentReferences = currentReferences;
	}

	@Override
	public Object getAttributeValue() {
		userReferences = (List<?>) convert(userReferences);
		return XMLModelReconciler.threeWayMerge(originalReferences, userReferences,
				currentReferences);
	}
}
