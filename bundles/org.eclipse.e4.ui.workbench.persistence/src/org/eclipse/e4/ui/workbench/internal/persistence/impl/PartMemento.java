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

import org.eclipse.e4.ui.workbench.internal.persistence.IPartMemento;
import org.eclipse.e4.ui.workbench.internal.persistence.IPersistencePackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Part Memento</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.workbench.internal.persistence.impl.PartMemento#getPartId <em>Part Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.workbench.internal.persistence.impl.PartMemento#getMemento <em>Memento</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PartMemento extends MinimalEObjectImpl.Container implements IPartMemento {
	/**
	 * The default value of the '{@link #getPartId() <em>Part Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPartId()
	 * @generated
	 * @ordered
	 */
	protected static final String PART_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPartId() <em>Part Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPartId()
	 * @generated
	 * @ordered
	 */
	protected String partId = PART_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getMemento() <em>Memento</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMemento()
	 * @generated
	 * @ordered
	 */
	protected static final String MEMENTO_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMemento() <em>Memento</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMemento()
	 * @generated
	 * @ordered
	 */
	protected String memento = MEMENTO_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PartMemento() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return IPersistencePackage.Literals.PART_MEMENTO;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getPartId() {
		return partId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPartId(String newPartId) {
		String oldPartId = partId;
		partId = newPartId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IPersistencePackage.PART_MEMENTO__PART_ID, oldPartId, partId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getMemento() {
		return memento;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMemento(String newMemento) {
		String oldMemento = memento;
		memento = newMemento;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, IPersistencePackage.PART_MEMENTO__MEMENTO, oldMemento, memento));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case IPersistencePackage.PART_MEMENTO__PART_ID:
				return getPartId();
			case IPersistencePackage.PART_MEMENTO__MEMENTO:
				return getMemento();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case IPersistencePackage.PART_MEMENTO__PART_ID:
				setPartId((String)newValue);
				return;
			case IPersistencePackage.PART_MEMENTO__MEMENTO:
				setMemento((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case IPersistencePackage.PART_MEMENTO__PART_ID:
				setPartId(PART_ID_EDEFAULT);
				return;
			case IPersistencePackage.PART_MEMENTO__MEMENTO:
				setMemento(MEMENTO_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case IPersistencePackage.PART_MEMENTO__PART_ID:
				return PART_ID_EDEFAULT == null ? partId != null : !PART_ID_EDEFAULT.equals(partId);
			case IPersistencePackage.PART_MEMENTO__MEMENTO:
				return MEMENTO_EDEFAULT == null ? memento != null : !MEMENTO_EDEFAULT.equals(memento);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (partId: "); //$NON-NLS-1$
		result.append(partId);
		result.append(", memento: "); //$NON-NLS-1$
		result.append(memento);
		result.append(')');
		return result.toString();
	}

} //PartMemento
