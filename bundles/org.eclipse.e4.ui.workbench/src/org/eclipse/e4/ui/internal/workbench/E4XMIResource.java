/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

public class E4XMIResource extends XMIResourceImpl {

	private Map<EObject, String> objectMap = new WeakHashMap<EObject, String>();
	private Set<String> knownIds = new HashSet<String>();

	public E4XMIResource() {
	}

	public E4XMIResource(URI uri) {
		super(uri);
	}

	public void setInternalId(EObject object, String id) {
		objectMap.put(object, id);
		knownIds.add(id);
	}

	public String getInternalId(EObject object) {
		return objectMap.get(object);
	}

	@Override
	protected boolean useIDs() {
		return true;
	}

	private String createId() {
		return EcoreUtil.generateUUID();
	}

	private String getUniqueId() {
		String id = createId();
		while (knownIds.contains(id)) {
			id = createId();
		}
		return id;
	}

	@Override
	public void setID(EObject eObject, String id) {
		if (id != null) {
			String internalId = objectMap.get(eObject);
			if (internalId != null) {
				super.setID(eObject, internalId);
			}
			objectMap.put(eObject, id);
			knownIds.add(id);
		}
		super.setID(eObject, id);
	}

	@Override
	public String getID(EObject eObject) {
		if (eObject instanceof Entry<?, ?>) {
			return null;
		}

		String id = super.getID(eObject);
		if (id != null) {
			return id;
		}

		id = objectMap.get(eObject);
		if (id != null) {
			return id;
		}

		id = getUniqueId();
		setID(eObject, id);
		return id;
	}
}
