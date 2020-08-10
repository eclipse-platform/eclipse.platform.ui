/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
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
import org.eclipse.emf.ecore.xmi.XMLSave;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

public class E4XMIResource extends XMIResourceImpl {

	public static final String OPTION_FILTER_PERSIST_STATE = "E4_FILTER_PERSISTED_STATE"; //$NON-NLS-1$

	private Map<EObject, String> objectMap = new WeakHashMap<>();
	private Set<String> knownIds = new HashSet<>();

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
			setID(eObject, id);
			return id;
		}

		id = getUniqueId();
		setID(eObject, id);
		return id;
	}

	/*
	 * Create custom XML save to allow filtering of volatile UI elements.
	 */
	@Override
	protected XMLSave createXMLSave(Map<?, ?> options) {
		if (options != null && Boolean.TRUE.equals(options.get(OPTION_FILTER_PERSIST_STATE))) {
			return new E4XMISave(createXMLHelper());
		}
		return super.createXMLSave(options);
	}
}
