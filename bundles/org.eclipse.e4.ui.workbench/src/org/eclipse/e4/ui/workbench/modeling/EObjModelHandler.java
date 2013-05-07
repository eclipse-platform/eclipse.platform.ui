/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.modeling;

import java.util.Iterator;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * @noreference This class is not intended to be referenced by clients.
 * @since 1.0
 */
public class EObjModelHandler extends ModelHandlerBase implements IAdapterFactory {
	public EObjModelHandler() {
	}

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		return this;
	}

	public Class[] getAdapterList() {
		return new Class[] { ModelHandlerBase.class };
	}

	@Override
	public Object[] getChildren(Object element, String id) {
		EObject eObj = (EObject) element;
		return eObj.eContents().toArray();
	}

	@Override
	public Object getProperty(Object element, String id) {
		EObject eObj = (EObject) element;
		if (eObj == null)
			return null;

		EStructuralFeature eFeature = eObj.eClass().getEStructuralFeature(id);
		if (eFeature == null)
			return null;

		return eObj.eGet(eFeature);
	}

	@Override
	public String[] getPropIds(Object element) {
		if (element == null)
			return new String[0];

		EObject eObj = (EObject) element;
		EList<EStructuralFeature> features = eObj.eClass().getEAllStructuralFeatures();
		String[] ids = new String[features.size()];
		int count = 0;
		for (Iterator iterator = features.iterator(); iterator.hasNext();) {
			EStructuralFeature structuralFeature = (EStructuralFeature) iterator.next();
			String featureName = structuralFeature.getName();
			ids[count++] = featureName;
		}

		return ids;
	}

	@Override
	public void setProperty(Object element, String id, Object value) {
		EObject eObj = (EObject) element;
		if (eObj == null)
			return;

		EStructuralFeature eFeature = eObj.eClass().getEStructuralFeature(id);
		if (eFeature == null)
			return;

		eObj.eSet(eFeature, value);
	}
}
