/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.menu.impl;

import org.eclipse.e4.ui.model.LocalizationHelper;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.impl.UIElementImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.emf.common.notify.Notification;
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
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ItemImpl#getLabel <em>Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ItemImpl#getIconURI <em>Icon URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ItemImpl#getTooltip <em>Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ItemImpl#getLocalizedLabel <em>Localized Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ItemImpl#getLocalizedTooltip <em>Localized Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ItemImpl#isEnabled <em>Enabled</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ItemImpl#isSelected <em>Selected</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.ItemImpl#getType <em>Type</em>}</li>
 * </ul>
 *
 * @generated
 */
public abstract class ItemImpl extends UIElementImpl implements MItem {
	/**
	 * The default value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @generated
	 * @ordered
	 */
	protected static final String LABEL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @generated
	 * @ordered
	 */
	protected String label = LABEL_EDEFAULT;

	/**
	 * The default value of the '{@link #getIconURI() <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIconURI()
	 * @generated
	 * @ordered
	 */
	protected static final String ICON_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getIconURI() <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIconURI()
	 * @generated
	 * @ordered
	 */
	protected String iconURI = ICON_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getTooltip() <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTooltip()
	 * @generated
	 * @ordered
	 */
	protected static final String TOOLTIP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTooltip() <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTooltip()
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
	 * The default value of the '{@link #isEnabled() <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isEnabled()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ENABLED_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isEnabled() <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isEnabled()
	 * @generated
	 * @ordered
	 */
	protected boolean enabled = ENABLED_EDEFAULT;

	/**
	 * The default value of the '{@link #isSelected() <em>Selected</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSelected()
	 * @generated
	 * @ordered
	 */
	protected static final boolean SELECTED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isSelected() <em>Selected</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSelected()
	 * @generated
	 * @ordered
	 */
	protected boolean selected = SELECTED_EDEFAULT;

	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final ItemType TYPE_EDEFAULT = ItemType.PUSH;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected ItemType type = TYPE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ItemImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MenuPackageImpl.Literals.ITEM;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLabel(String newLabel) {
		String oldLabel = label;
		label = newLabel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.ITEM__LABEL, oldLabel, label));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getIconURI() {
		return iconURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setIconURI(String newIconURI) {
		String oldIconURI = iconURI;
		iconURI = newIconURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.ITEM__ICON_URI, oldIconURI, iconURI));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTooltip(String newTooltip) {
		String oldTooltip = tooltip;
		tooltip = newTooltip;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.ITEM__TOOLTIP, oldTooltip, tooltip));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setEnabled(boolean newEnabled) {
		boolean oldEnabled = enabled;
		enabled = newEnabled;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.ITEM__ENABLED, oldEnabled, enabled));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSelected(boolean newSelected) {
		boolean oldSelected = selected;
		selected = newSelected;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.ITEM__SELECTED, oldSelected, selected));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ItemType getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(ItemType newType) {
		ItemType oldType = type;
		type = newType == null ? TYPE_EDEFAULT : newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.ITEM__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	@Override
	public void updateLocalization() {
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(
					this, Notification.SET, MenuPackageImpl.ITEM__LOCALIZED_LABEL, null, getLocalizedLabel()));
			eNotify(new ENotificationImpl(
					this, Notification.SET, MenuPackageImpl.ITEM__LOCALIZED_TOOLTIP, null, getLocalizedTooltip()));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public String getLocalizedLabel() {
		return LocalizationHelper.getLocalizedLabel(this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public String getLocalizedTooltip() {
		return LocalizationHelper.getLocalizedTooltip(this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MenuPackageImpl.ITEM__LABEL:
				return getLabel();
			case MenuPackageImpl.ITEM__ICON_URI:
				return getIconURI();
			case MenuPackageImpl.ITEM__TOOLTIP:
				return getTooltip();
			case MenuPackageImpl.ITEM__LOCALIZED_LABEL:
				return getLocalizedLabel();
			case MenuPackageImpl.ITEM__LOCALIZED_TOOLTIP:
				return getLocalizedTooltip();
			case MenuPackageImpl.ITEM__ENABLED:
				return isEnabled();
			case MenuPackageImpl.ITEM__SELECTED:
				return isSelected();
			case MenuPackageImpl.ITEM__TYPE:
				return getType();
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
			case MenuPackageImpl.ITEM__LABEL:
				setLabel((String)newValue);
				return;
			case MenuPackageImpl.ITEM__ICON_URI:
				setIconURI((String)newValue);
				return;
			case MenuPackageImpl.ITEM__TOOLTIP:
				setTooltip((String)newValue);
				return;
			case MenuPackageImpl.ITEM__ENABLED:
				setEnabled((Boolean)newValue);
				return;
			case MenuPackageImpl.ITEM__SELECTED:
				setSelected((Boolean)newValue);
				return;
			case MenuPackageImpl.ITEM__TYPE:
				setType((ItemType)newValue);
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
			case MenuPackageImpl.ITEM__LABEL:
				setLabel(LABEL_EDEFAULT);
				return;
			case MenuPackageImpl.ITEM__ICON_URI:
				setIconURI(ICON_URI_EDEFAULT);
				return;
			case MenuPackageImpl.ITEM__TOOLTIP:
				setTooltip(TOOLTIP_EDEFAULT);
				return;
			case MenuPackageImpl.ITEM__ENABLED:
				setEnabled(ENABLED_EDEFAULT);
				return;
			case MenuPackageImpl.ITEM__SELECTED:
				setSelected(SELECTED_EDEFAULT);
				return;
			case MenuPackageImpl.ITEM__TYPE:
				setType(TYPE_EDEFAULT);
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
			case MenuPackageImpl.ITEM__LABEL:
				return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
			case MenuPackageImpl.ITEM__ICON_URI:
				return ICON_URI_EDEFAULT == null ? iconURI != null : !ICON_URI_EDEFAULT.equals(iconURI);
			case MenuPackageImpl.ITEM__TOOLTIP:
				return TOOLTIP_EDEFAULT == null ? tooltip != null : !TOOLTIP_EDEFAULT.equals(tooltip);
			case MenuPackageImpl.ITEM__LOCALIZED_LABEL:
				return LOCALIZED_LABEL_EDEFAULT == null ? getLocalizedLabel() != null : !LOCALIZED_LABEL_EDEFAULT.equals(getLocalizedLabel());
			case MenuPackageImpl.ITEM__LOCALIZED_TOOLTIP:
				return LOCALIZED_TOOLTIP_EDEFAULT == null ? getLocalizedTooltip() != null : !LOCALIZED_TOOLTIP_EDEFAULT.equals(getLocalizedTooltip());
			case MenuPackageImpl.ITEM__ENABLED:
				return enabled != ENABLED_EDEFAULT;
			case MenuPackageImpl.ITEM__SELECTED:
				return selected != SELECTED_EDEFAULT;
			case MenuPackageImpl.ITEM__TYPE:
				return type != TYPE_EDEFAULT;
		}
		return super.eIsSet(featureID);
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
				case MenuPackageImpl.ITEM__LABEL: return UiPackageImpl.UI_LABEL__LABEL;
				case MenuPackageImpl.ITEM__ICON_URI: return UiPackageImpl.UI_LABEL__ICON_URI;
				case MenuPackageImpl.ITEM__TOOLTIP: return UiPackageImpl.UI_LABEL__TOOLTIP;
				case MenuPackageImpl.ITEM__LOCALIZED_LABEL: return UiPackageImpl.UI_LABEL__LOCALIZED_LABEL;
				case MenuPackageImpl.ITEM__LOCALIZED_TOOLTIP: return UiPackageImpl.UI_LABEL__LOCALIZED_TOOLTIP;
				default: return -1;
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
				case UiPackageImpl.UI_LABEL__LABEL: return MenuPackageImpl.ITEM__LABEL;
				case UiPackageImpl.UI_LABEL__ICON_URI: return MenuPackageImpl.ITEM__ICON_URI;
				case UiPackageImpl.UI_LABEL__TOOLTIP: return MenuPackageImpl.ITEM__TOOLTIP;
				case UiPackageImpl.UI_LABEL__LOCALIZED_LABEL: return MenuPackageImpl.ITEM__LOCALIZED_LABEL;
				case UiPackageImpl.UI_LABEL__LOCALIZED_TOOLTIP: return MenuPackageImpl.ITEM__LOCALIZED_TOOLTIP;
				default: return -1;
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
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (label: "); //$NON-NLS-1$
		result.append(label);
		result.append(", iconURI: "); //$NON-NLS-1$
		result.append(iconURI);
		result.append(", tooltip: "); //$NON-NLS-1$
		result.append(tooltip);
		result.append(", enabled: "); //$NON-NLS-1$
		result.append(enabled);
		result.append(", selected: "); //$NON-NLS-1$
		result.append(selected);
		result.append(", type: "); //$NON-NLS-1$
		result.append(type);
		result.append(')');
		return result.toString();
	}

} //ItemImpl
