/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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
import org.eclipse.emf.ecore.xmi.impl.XMIHelperImpl;
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

	/**
	 * Functional interface for creating objects
	 */
	private interface ObjectCreator {
		MApplicationElement create();
	}

	static final Map<String, ObjectCreator> deprecatedTypeMappings = new HashMap<String, ObjectCreator>();
	static {
		deprecatedTypeMappings.put("OpaqueMenu", new ObjectCreator() { //$NON-NLS-1$

					@Override
					public MApplicationElement create() {
						return OpaqueElementUtil.createOpaqueMenu();
					}
				});
		deprecatedTypeMappings.put("OpaqueMenuItem", new ObjectCreator() { //$NON-NLS-1$

					@Override
					public MApplicationElement create() {
						return OpaqueElementUtil.createOpaqueMenuItem();
					}
				});
		deprecatedTypeMappings.put("OpaqueMenuSeparator", new ObjectCreator() { //$NON-NLS-1$

					@Override
					public MApplicationElement create() {
						return OpaqueElementUtil.createOpaqueMenuSeparator();
					}
				});
		deprecatedTypeMappings.put("OpaqueToolItem", new ObjectCreator() { //$NON-NLS-1$

					@Override
					public MApplicationElement create() {
						return OpaqueElementUtil.createOpaqueToolItem();
					}
				});
		deprecatedTypeMappings.put("RenderedMenu", new ObjectCreator() { //$NON-NLS-1$

					@Override
					public MApplicationElement create() {
						return RenderedElementUtil.createRenderedMenu();
					}
				});
		deprecatedTypeMappings.put("RenderedMenuItem", new ObjectCreator() { //$NON-NLS-1$

					@Override
					public MApplicationElement create() {
						return RenderedElementUtil.createRenderedMenuItem();
					}
				});
		deprecatedTypeMappings.put("RenderedToolBar", new ObjectCreator() { //$NON-NLS-1$

					@Override
					public MApplicationElement create() {
						return RenderedElementUtil.createRenderedToolBar();
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl#createXMLHelper()
	 */
	@Override
	protected XMLHelper createXMLHelper() {
		// Handle mapping of deprecated types
		return new XMIHelperImpl(this) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.emf.ecore.xmi.impl.XMLHelperImpl#createObject(org.eclipse.emf.ecore.EFactory
			 * , org.eclipse.emf.ecore.EClassifier)
			 */
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

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.emf.ecore.xmi.impl.XMLHelperImpl#getType(org.eclipse.emf.ecore.EFactory,
			 * java.lang.String)
			 */
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
}
