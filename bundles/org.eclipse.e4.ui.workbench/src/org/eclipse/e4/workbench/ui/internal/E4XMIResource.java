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

package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

public class E4XMIResource extends XMIResourceImpl {

	private int count = 0;

	public E4XMIResource() {
	}

	public E4XMIResource(URI uri) {
		super(uri);
	}

	@Override
	protected boolean useIDs() {
		return true;
	}

	@Override
	public void setID(EObject eObject, String id) {
		super.setID(eObject, id);

		if (id != null) {
			count++;
		}
	}

	private String createId() {
		//				String id = "element." + count; //$NON-NLS-1$
		// count++;
		// return id;
		return EcoreUtil.generateUUID();
	}

	private String getUniqueId() {
		String id = createId();
		while (getEObjectByID(id) != null) {
			id = createId();
		}
		return id;
	}

	@Override
	public String getID(EObject eObject) {
		String id = super.getID(eObject);
		if (id != null) {
			return id;
		}

		MApplicationElement element = (MApplicationElement) eObject;
		id = element.getId();
		if (id != null) {
			super.setID((EObject) element, id);// getUniqueId());
			return id;
		}

		id = getUniqueId();

		element.setId(id);
		super.setID((EObject) element, id);
		return id;
	}
}
