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
import org.eclipse.e4.ui.model.LocalizationHelper;
import org.eclipse.e4.ui.model.application.ui.MLocalizable;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.impl.UIElementImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Element</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuElementImpl#getLabel <em>Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuElementImpl#getIconURI <em>Icon URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuElementImpl#getTooltip <em>Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuElementImpl#getLocalizedLabel <em>Localized Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuElementImpl#getLocalizedTooltip <em>Localized Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.MenuElementImpl#getMnemonics <em>Mnemonics</em>}</li>
 * </ul>
 *
 * @since 1.0
 * @generated
 */
public abstract class MenuElementImpl extends UIElementImpl implements MMenuElement {
	/**
	 * The default value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected static final String LABEL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected String label = LABEL_EDEFAULT;

	/**
	 * The default value of the '{@link #getIconURI() <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIconURI()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected static final String ICON_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getIconURI() <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIconURI()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected String iconURI = ICON_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getTooltip() <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTooltip()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected static final String TOOLTIP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTooltip() <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTooltip()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected String tooltip = TOOLTIP_EDEFAULT;

	/**
	 * The default value of the '{@link #getLocalizedLabel() <em>Localized Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalizedLabel()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALIZED_LABEL_EDEFAULT = ""; //$NON-NLS-1$

	/**
	 * The default value of the '{@link #getLocalizedTooltip() <em>Localized Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalizedTooltip()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALIZED_TOOLTIP_EDEFAULT = ""; //$NON-NLS-1$

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
	protected MenuElementImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MenuPackageImpl.Literals.MENU_ELEMENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public void setLabel(String newLabel) {
		String oldLabel = label;
		label = newLabel;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.MENU_ELEMENT__LABEL, oldLabel,
					label));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public String getIconURI() {
		return iconURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public void setIconURI(String newIconURI) {
		String oldIconURI = iconURI;
		iconURI = newIconURI;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.MENU_ELEMENT__ICON_URI, oldIconURI,
					iconURI));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public void setTooltip(String newTooltip) {
		String oldTooltip = tooltip;
		tooltip = newTooltip;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.MENU_ELEMENT__TOOLTIP, oldTooltip,
					tooltip));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLocalizedLabel() {
		return LocalizationHelper.getLocalizedFeature(UiPackageImpl.Literals.UI_LABEL__LABEL, this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLocalizedTooltip() {
		return LocalizationHelper.getLocalizedFeature(UiPackageImpl.Literals.UI_LABEL__TOOLTIP, this);
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
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.MENU_ELEMENT__MNEMONICS, oldMnemonics,
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
		if (eNotificationRequired()) {
			super.updateLocalization();
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.MENU_ELEMENT__LOCALIZED_LABEL, null,
					getLocalizedLabel()));
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.MENU_ELEMENT__LOCALIZED_TOOLTIP, null,
					getLocalizedTooltip()));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case MenuPackageImpl.MENU_ELEMENT__LABEL:
			return getLabel();
		case MenuPackageImpl.MENU_ELEMENT__ICON_URI:
			return getIconURI();
		case MenuPackageImpl.MENU_ELEMENT__TOOLTIP:
			return getTooltip();
		case MenuPackageImpl.MENU_ELEMENT__LOCALIZED_LABEL:
			return getLocalizedLabel();
		case MenuPackageImpl.MENU_ELEMENT__LOCALIZED_TOOLTIP:
			return getLocalizedTooltip();
		case MenuPackageImpl.MENU_ELEMENT__MNEMONICS:
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
		case MenuPackageImpl.MENU_ELEMENT__LABEL:
			setLabel((String) newValue);
			return;
		case MenuPackageImpl.MENU_ELEMENT__ICON_URI:
			setIconURI((String) newValue);
			return;
		case MenuPackageImpl.MENU_ELEMENT__TOOLTIP:
			setTooltip((String) newValue);
			return;
		case MenuPackageImpl.MENU_ELEMENT__MNEMONICS:
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
		case MenuPackageImpl.MENU_ELEMENT__LABEL:
			setLabel(LABEL_EDEFAULT);
			return;
		case MenuPackageImpl.MENU_ELEMENT__ICON_URI:
			setIconURI(ICON_URI_EDEFAULT);
			return;
		case MenuPackageImpl.MENU_ELEMENT__TOOLTIP:
			setTooltip(TOOLTIP_EDEFAULT);
			return;
		case MenuPackageImpl.MENU_ELEMENT__MNEMONICS:
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
		case MenuPackageImpl.MENU_ELEMENT__LABEL:
			return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
		case MenuPackageImpl.MENU_ELEMENT__ICON_URI:
			return ICON_URI_EDEFAULT == null ? iconURI != null : !ICON_URI_EDEFAULT.equals(iconURI);
		case MenuPackageImpl.MENU_ELEMENT__TOOLTIP:
			return TOOLTIP_EDEFAULT == null ? tooltip != null : !TOOLTIP_EDEFAULT.equals(tooltip);
		case MenuPackageImpl.MENU_ELEMENT__LOCALIZED_LABEL:
			return LOCALIZED_LABEL_EDEFAULT == null ? getLocalizedLabel() != null
					: !LOCALIZED_LABEL_EDEFAULT.equals(getLocalizedLabel());
		case MenuPackageImpl.MENU_ELEMENT__LOCALIZED_TOOLTIP:
			return LOCALIZED_TOOLTIP_EDEFAULT == null ? getLocalizedTooltip() != null
					: !LOCALIZED_TOOLTIP_EDEFAULT.equals(getLocalizedTooltip());
		case MenuPackageImpl.MENU_ELEMENT__MNEMONICS:
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
		if (baseClass == MUILabel.class) {
			switch (derivedFeatureID) {
			case MenuPackageImpl.MENU_ELEMENT__LABEL:
				return UiPackageImpl.UI_LABEL__LABEL;
			case MenuPackageImpl.MENU_ELEMENT__ICON_URI:
				return UiPackageImpl.UI_LABEL__ICON_URI;
			case MenuPackageImpl.MENU_ELEMENT__TOOLTIP:
				return UiPackageImpl.UI_LABEL__TOOLTIP;
			case MenuPackageImpl.MENU_ELEMENT__LOCALIZED_LABEL:
				return UiPackageImpl.UI_LABEL__LOCALIZED_LABEL;
			case MenuPackageImpl.MENU_ELEMENT__LOCALIZED_TOOLTIP:
				return UiPackageImpl.UI_LABEL__LOCALIZED_TOOLTIP;
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
		if (baseClass == MUILabel.class) {
			switch (baseFeatureID) {
			case UiPackageImpl.UI_LABEL__LABEL:
				return MenuPackageImpl.MENU_ELEMENT__LABEL;
			case UiPackageImpl.UI_LABEL__ICON_URI:
				return MenuPackageImpl.MENU_ELEMENT__ICON_URI;
			case UiPackageImpl.UI_LABEL__TOOLTIP:
				return MenuPackageImpl.MENU_ELEMENT__TOOLTIP;
			case UiPackageImpl.UI_LABEL__LOCALIZED_LABEL:
				return MenuPackageImpl.MENU_ELEMENT__LOCALIZED_LABEL;
			case UiPackageImpl.UI_LABEL__LOCALIZED_TOOLTIP:
				return MenuPackageImpl.MENU_ELEMENT__LOCALIZED_TOOLTIP;
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
				return MenuPackageImpl.MENU_ELEMENT___UPDATE_LOCALIZATION;
			default:
				return super.eDerivedOperationID(baseOperationID, baseClass);
			}
		}
		if (baseClass == MUIElement.class) {
			switch (baseOperationID) {
			case UiPackageImpl.UI_ELEMENT___UPDATE_LOCALIZATION:
				return MenuPackageImpl.MENU_ELEMENT___UPDATE_LOCALIZATION;
			default:
				return super.eDerivedOperationID(baseOperationID, baseClass);
			}
		}
		if (baseClass == MUILabel.class) {
			switch (baseOperationID) {
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
		case MenuPackageImpl.MENU_ELEMENT___GET_LOCALIZED_MNEMONICS:
			return getLocalizedMnemonics();
		case MenuPackageImpl.MENU_ELEMENT___UPDATE_LOCALIZATION:
			updateLocalization();
			return null;
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
		result.append(" (label: "); //$NON-NLS-1$
		result.append(label);
		result.append(", iconURI: "); //$NON-NLS-1$
		result.append(iconURI);
		result.append(", tooltip: "); //$NON-NLS-1$
		result.append(tooltip);
		result.append(", mnemonics: "); //$NON-NLS-1$
		result.append(mnemonics);
		result.append(')');
		return result.toString();
	}

} //MenuElementImpl
