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
package org.eclipse.e4.ui.model.application.ui.menu.impl;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.e4.ui.model.application.ui.MLocalizable;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Item</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuItemImpl#getMnemonics <em>Mnemonics</em>}</li>
 * </ul>
 *
 * @since 1.0
 * @generated
 */
public abstract class MenuItemImpl extends ItemImpl implements MMenuItem {
	/**
	 * The default value of the '{@link #getMnemonics() <em>Mnemonics</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMnemonics()
	 * @generated
	 * @ordered
	 */
	protected static final String MNEMONICS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMnemonics() <em>Mnemonics</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMnemonics()
	 * @generated
	 * @ordered
	 */
	protected String mnemonics = MNEMONICS_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MenuItemImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MenuPackageImpl.Literals.MENU_ITEM;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getMnemonics() {
		return mnemonics;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMnemonics(String newMnemonics) {
		String oldMnemonics = mnemonics;
		mnemonics = newMnemonics;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.MENU_ITEM__MNEMONICS, oldMnemonics,
					mnemonics));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * This class does not support this feature.
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLocalizedMnemonics() {
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 */
	@Override
	public void updateLocalization() {
		super.updateLocalization();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case MenuPackageImpl.MENU_ITEM__MNEMONICS:
			return getMnemonics();
		default:
			return super.eGet(featureID, resolve, coreType);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case MenuPackageImpl.MENU_ITEM__MNEMONICS:
			setMnemonics((String) newValue);
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
		case MenuPackageImpl.MENU_ITEM__MNEMONICS:
			setMnemonics(MNEMONICS_EDEFAULT);
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
		case MenuPackageImpl.MENU_ITEM__MNEMONICS:
			return MNEMONICS_EDEFAULT == null ? mnemonics != null : !MNEMONICS_EDEFAULT.equals(mnemonics);
		default:
			return super.eIsSet(featureID);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == MMenuElement.class) {
			switch (derivedFeatureID) {
			case MenuPackageImpl.MENU_ITEM__MNEMONICS:
				return MenuPackageImpl.MENU_ELEMENT__MNEMONICS;
			default:
				return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == MMenuElement.class) {
			switch (baseFeatureID) {
			case MenuPackageImpl.MENU_ELEMENT__MNEMONICS:
				return MenuPackageImpl.MENU_ITEM__MNEMONICS;
			default:
				return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedOperationID(int baseOperationID, Class<?> baseClass) {
		if (baseClass == MLocalizable.class) {
			switch (baseOperationID) {
			case UiPackageImpl.LOCALIZABLE___UPDATE_LOCALIZATION:
				return MenuPackageImpl.MENU_ITEM___UPDATE_LOCALIZATION;
			default:
				return super.eDerivedOperationID(baseOperationID, baseClass);
			}
		}
		if (baseClass == MUIElement.class) {
			switch (baseOperationID) {
			case UiPackageImpl.UI_ELEMENT___UPDATE_LOCALIZATION:
				return MenuPackageImpl.MENU_ITEM___UPDATE_LOCALIZATION;
			default:
				return super.eDerivedOperationID(baseOperationID, baseClass);
			}
		}
		if (baseClass == MUILabel.class) {
			switch (baseOperationID) {
			case UiPackageImpl.UI_LABEL___UPDATE_LOCALIZATION:
				return MenuPackageImpl.MENU_ITEM___UPDATE_LOCALIZATION;
			default:
				return super.eDerivedOperationID(baseOperationID, baseClass);
			}
		}
		if (baseClass == MItem.class) {
			switch (baseOperationID) {
			case MenuPackageImpl.ITEM___UPDATE_LOCALIZATION:
				return MenuPackageImpl.MENU_ITEM___UPDATE_LOCALIZATION;
			default:
				return super.eDerivedOperationID(baseOperationID, baseClass);
			}
		}
		if (baseClass == MMenuElement.class) {
			switch (baseOperationID) {
			case MenuPackageImpl.MENU_ELEMENT___GET_LOCALIZED_MNEMONICS:
				return MenuPackageImpl.MENU_ITEM___GET_LOCALIZED_MNEMONICS;
			case MenuPackageImpl.MENU_ELEMENT___UPDATE_LOCALIZATION:
				return MenuPackageImpl.MENU_ITEM___UPDATE_LOCALIZATION;
			default:
				return -1;
			}
		}
		return super.eDerivedOperationID(baseOperationID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
		case MenuPackageImpl.MENU_ITEM___UPDATE_LOCALIZATION:
			updateLocalization();
			return null;
		case MenuPackageImpl.MENU_ITEM___GET_LOCALIZED_MNEMONICS:
			return getLocalizedMnemonics();
		default:
			return super.eInvoke(operationID, arguments);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) {
			return super.toString();
		}

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (mnemonics: "); //$NON-NLS-1$
		result.append(mnemonics);
		result.append(')');
		return result.toString();
	}

} //MenuItemImpl
