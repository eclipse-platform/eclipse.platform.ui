/**
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.menu.impl;

import java.util.Collection;
import java.util.List;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Trim Contributions</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.TrimContributionsImpl#getTrimContributions <em>Trim Contributions</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TrimContributionsImpl extends EObjectImpl implements MTrimContributions {
	/**
	 * The cached value of the '{@link #getTrimContributions() <em>Trim Contributions</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTrimContributions()
	 * @generated
	 * @ordered
	 */
	protected EList<MTrimContribution> trimContributions;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TrimContributionsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MenuPackageImpl.Literals.TRIM_CONTRIBUTIONS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MTrimContribution> getTrimContributions() {
		if (trimContributions == null) {
			trimContributions = new EObjectContainmentEList<MTrimContribution>(MTrimContribution.class, this, MenuPackageImpl.TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS);
		}
		return trimContributions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case MenuPackageImpl.TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS:
				return ((InternalEList<?>)getTrimContributions()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MenuPackageImpl.TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS:
				return getTrimContributions();
		}
		return super.eGet(featureID, resolve, coreType);
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
			case MenuPackageImpl.TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS:
				getTrimContributions().clear();
				getTrimContributions().addAll((Collection<? extends MTrimContribution>)newValue);
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
			case MenuPackageImpl.TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS:
				getTrimContributions().clear();
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
			case MenuPackageImpl.TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS:
				return trimContributions != null && !trimContributions.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //TrimContributionsImpl
