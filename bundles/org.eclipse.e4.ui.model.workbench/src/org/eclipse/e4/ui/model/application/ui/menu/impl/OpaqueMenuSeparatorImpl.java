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

import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuSeparator;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Opaque Menu Separator</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.OpaqueMenuSeparatorImpl#getOpaqueItem <em>Opaque Item</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class OpaqueMenuSeparatorImpl extends MenuSeparatorImpl implements MOpaqueMenuSeparator {
	/**
	 * The default value of the '{@link #getOpaqueItem() <em>Opaque Item</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOpaqueItem()
	 * @generated
	 * @ordered
	 */
	protected static final Object OPAQUE_ITEM_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getOpaqueItem() <em>Opaque Item</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOpaqueItem()
	 * @generated
	 * @ordered
	 */
	protected Object opaqueItem = OPAQUE_ITEM_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OpaqueMenuSeparatorImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MenuPackageImpl.Literals.OPAQUE_MENU_SEPARATOR;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getOpaqueItem() {
		return opaqueItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOpaqueItem(Object newOpaqueItem) {
		Object oldOpaqueItem = opaqueItem;
		opaqueItem = newOpaqueItem;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.OPAQUE_MENU_SEPARATOR__OPAQUE_ITEM, oldOpaqueItem, opaqueItem));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MenuPackageImpl.OPAQUE_MENU_SEPARATOR__OPAQUE_ITEM:
				return getOpaqueItem();
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
			case MenuPackageImpl.OPAQUE_MENU_SEPARATOR__OPAQUE_ITEM:
				setOpaqueItem(newValue);
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
			case MenuPackageImpl.OPAQUE_MENU_SEPARATOR__OPAQUE_ITEM:
				setOpaqueItem(OPAQUE_ITEM_EDEFAULT);
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
			case MenuPackageImpl.OPAQUE_MENU_SEPARATOR__OPAQUE_ITEM:
				return OPAQUE_ITEM_EDEFAULT == null ? opaqueItem != null : !OPAQUE_ITEM_EDEFAULT.equals(opaqueItem);
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
		result.append(" (opaqueItem: "); //$NON-NLS-1$
		result.append(opaqueItem);
		result.append(')');
		return result.toString();
	}

} //OpaqueMenuSeparatorImpl
