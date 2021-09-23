/**
 * Copyright (c) 2021 EclipseSource GmbH and others.
 *  
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *  
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:   
 *     EclipseSource GmbH - initial API and implementation
 */
package org.eclipse.e4.ui.workbench.internal.persistence.impl;

import org.eclipse.e4.ui.workbench.internal.persistence.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class PersistenceFactory extends EFactoryImpl implements IPersistenceFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static IPersistenceFactory init() {
		try {
			IPersistenceFactory thePersistenceFactory = (IPersistenceFactory)EPackage.Registry.INSTANCE.getEFactory(IPersistencePackage.eNS_URI);
			if (thePersistenceFactory != null) {
				return thePersistenceFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new PersistenceFactory();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PersistenceFactory() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case IPersistencePackage.WORKBENCH_STATE: return createWorkbenchState();
			case IPersistencePackage.PART_MEMENTO: return createPartMemento();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IWorkbenchState createWorkbenchState() {
		WorkbenchState workbenchState = new WorkbenchState();
		return workbenchState;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IPartMemento createPartMemento() {
		PartMemento partMemento = new PartMemento();
		return partMemento;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IPersistencePackage getPersistencePackage() {
		return (IPersistencePackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static IPersistencePackage getPackage() {
		return IPersistencePackage.eINSTANCE;
	}

} //PersistenceFactory
