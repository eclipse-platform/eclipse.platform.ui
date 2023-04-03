/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.basic.impl;

import java.util.Collection;
import java.util.List;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.impl.GenericTrimContainerImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Trim Bar</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.TrimBarImpl#getPendingCleanup <em>Pending Cleanup</em>}</li>
 * </ul>
 *
 * @since 1.0
 * @generated
 */
public class TrimBarImpl extends GenericTrimContainerImpl<MTrimElement> implements MTrimBar {
	/**
	 * The cached value of the '{@link #getPendingCleanup() <em>Pending Cleanup</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPendingCleanup()
	 * @noreference
	 * @generated
	 * @ordered
	 */
	protected EList<MTrimElement> pendingCleanup;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TrimBarImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BasicPackageImpl.Literals.TRIM_BAR;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @noreference
	 * @generated
	 */
	@Override
	public List<MTrimElement> getPendingCleanup() {
		if (pendingCleanup == null) {
			pendingCleanup = new EObjectResolvingEList<>(MTrimElement.class, this,
					BasicPackageImpl.TRIM_BAR__PENDING_CLEANUP);
		}
		return pendingCleanup;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case BasicPackageImpl.TRIM_BAR__PENDING_CLEANUP:
			return getPendingCleanup();
		default:
			return super.eGet(featureID, resolve, coreType);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case BasicPackageImpl.TRIM_BAR__PENDING_CLEANUP:
			getPendingCleanup().clear();
			getPendingCleanup().addAll((Collection<? extends MTrimElement>) newValue);
			return;
		default:
			super.eSet(featureID, newValue);
			return;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case BasicPackageImpl.TRIM_BAR__PENDING_CLEANUP:
			getPendingCleanup().clear();
			return;
		default:
			super.eUnset(featureID);
			return;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case BasicPackageImpl.TRIM_BAR__PENDING_CLEANUP:
			return pendingCleanup != null && !pendingCleanup.isEmpty();
		default:
			return super.eIsSet(featureID);
		}
	}

} //TrimBarImpl
