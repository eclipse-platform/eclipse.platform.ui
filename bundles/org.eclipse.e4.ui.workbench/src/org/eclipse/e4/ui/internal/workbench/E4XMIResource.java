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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLSave;
import org.eclipse.emf.ecore.xmi.impl.XMIHelperImpl;
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

	/**
	 * Functional interface for creating objects
	 */
	private interface ObjectCreator {
		MApplicationElement create();
	}

	static final Map<String, ObjectCreator> deprecatedTypeMappings = new HashMap<>();
	static {
		deprecatedTypeMappings.put("OpaqueMenu", OpaqueElementUtil::createOpaqueMenu //$NON-NLS-1$
				);
		deprecatedTypeMappings.put("OpaqueMenuItem", OpaqueElementUtil::createOpaqueMenuItem //$NON-NLS-1$
				);
		deprecatedTypeMappings.put("OpaqueMenuSeparator", OpaqueElementUtil::createOpaqueMenuSeparator //$NON-NLS-1$
				);
		deprecatedTypeMappings.put("OpaqueToolItem", OpaqueElementUtil::createOpaqueToolItem //$NON-NLS-1$
				);
		deprecatedTypeMappings.put("RenderedMenu", RenderedElementUtil::createRenderedMenu //$NON-NLS-1$
				);
		deprecatedTypeMappings.put("RenderedMenuItem", RenderedElementUtil::createRenderedMenuItem //$NON-NLS-1$
				);
		deprecatedTypeMappings.put("RenderedToolBar", RenderedElementUtil::createRenderedToolBar //$NON-NLS-1$
				);
	}

	@Override
	protected XMLHelper createXMLHelper() {
		// Handle mapping of deprecated types
		return new XMIHelperImpl(this) {

			@Override
			public EObject createObject(EFactory eFactory, EClassifier type) {
				if (MMenuFactory.INSTANCE == eFactory && type != null && type.getName() != null) {
					final ObjectCreator objectCreator = deprecatedTypeMappings.get(type.getName());
					if (objectCreator != null) {
						return (EObject) objectCreator.create();
					}
				}
				return super.createObject(eFactory, type);
			}

			@Override
			public EClassifier getType(EFactory eFactory, String typeName) {
				if (deprecatedTypeMappings.containsKey(typeName)) {
					// need a temp instance of the now removed EClass so that
					// createObject, above, can do it's work.
					final EClass tempEClass = EcoreFactory.eINSTANCE.createEClass();
					tempEClass.setName(typeName);
					return tempEClass;
				}
				return super.getType(eFactory, typeName);
			}

		};
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
