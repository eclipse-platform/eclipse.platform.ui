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

import org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenuItem;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Rendered Menu Item</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.RenderedMenuItemImpl#getContributionItem <em>Contribution Item</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RenderedMenuItemImpl extends MenuItemImpl implements MRenderedMenuItem {
	/**
	 * The default value of the '{@link #getContributionItem() <em>Contribution Item</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContributionItem()
	 * @generated
	 * @ordered
	 */
	protected static final Object CONTRIBUTION_ITEM_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getContributionItem() <em>Contribution Item</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContributionItem()
	 * @generated
	 * @ordered
	 */
	protected Object contributionItem = CONTRIBUTION_ITEM_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RenderedMenuItemImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MenuPackageImpl.Literals.RENDERED_MENU_ITEM;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getContributionItem() {
		return contributionItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setContributionItem(Object newContributionItem) {
		Object oldContributionItem = contributionItem;
		contributionItem = newContributionItem;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.RENDERED_MENU_ITEM__CONTRIBUTION_ITEM, oldContributionItem, contributionItem));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MenuPackageImpl.RENDERED_MENU_ITEM__CONTRIBUTION_ITEM:
				return getContributionItem();
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
			case MenuPackageImpl.RENDERED_MENU_ITEM__CONTRIBUTION_ITEM:
				setContributionItem(newValue);
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
			case MenuPackageImpl.RENDERED_MENU_ITEM__CONTRIBUTION_ITEM:
				setContributionItem(CONTRIBUTION_ITEM_EDEFAULT);
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
			case MenuPackageImpl.RENDERED_MENU_ITEM__CONTRIBUTION_ITEM:
				return CONTRIBUTION_ITEM_EDEFAULT == null ? contributionItem != null : !CONTRIBUTION_ITEM_EDEFAULT.equals(contributionItem);
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

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (contributionItem: "); //$NON-NLS-1$
		result.append(contributionItem);
		result.append(')');
		return result.toString();
	}

} //RenderedMenuItemImpl
