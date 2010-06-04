/**
 * Copyright (c) 2008 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Contributions</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuContributionsImpl#getMenuContributions <em>Menu Contributions</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MenuContributionsImpl extends EObjectImpl implements MMenuContributions {
	/**
	 * The cached value of the '{@link #getMenuContributions() <em>Menu Contributions</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMenuContributions()
	 * @generated
	 * @ordered
	 */
	protected EList<MMenuContribution> menuContributions;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MenuContributionsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MenuPackageImpl.Literals.MENU_CONTRIBUTIONS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MMenuContribution> getMenuContributions() {
		if (menuContributions == null) {
			menuContributions = new EObjectContainmentEList<MMenuContribution>(MMenuContribution.class, this, MenuPackageImpl.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS);
		}
		return menuContributions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case MenuPackageImpl.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS:
				return ((InternalEList<?>)getMenuContributions()).basicRemove(otherEnd, msgs);
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
			case MenuPackageImpl.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS:
				return getMenuContributions();
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
			case MenuPackageImpl.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS:
				getMenuContributions().clear();
				getMenuContributions().addAll((Collection<? extends MMenuContribution>)newValue);
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
			case MenuPackageImpl.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS:
				getMenuContributions().clear();
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
			case MenuPackageImpl.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS:
				return menuContributions != null && !menuContributions.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //MenuContributionsImpl
