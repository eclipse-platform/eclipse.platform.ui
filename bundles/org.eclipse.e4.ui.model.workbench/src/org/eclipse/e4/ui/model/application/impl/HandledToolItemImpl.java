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
package org.eclipse.e4.ui.model.application.impl;

import java.util.Collection;

import org.eclipse.core.commands.ParameterizedCommand;

import org.eclipse.e4.ui.model.application.ItemType;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MHandledItem;
import org.eclipse.e4.ui.model.application.MHandledToolItem;
import org.eclipse.e4.ui.model.application.MItem;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MParameter;
import org.eclipse.e4.ui.model.application.MToolItem;
import org.eclipse.e4.ui.model.application.MUILabel;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Handled Tool Item</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.HandledToolItemImpl#getLabel <em>Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.HandledToolItemImpl#getIconURI <em>Icon URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.HandledToolItemImpl#getTooltip <em>Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.HandledToolItemImpl#isEnabled <em>Enabled</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.HandledToolItemImpl#isSelected <em>Selected</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.HandledToolItemImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.HandledToolItemImpl#getCommand <em>Command</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.HandledToolItemImpl#getWbCommand <em>Wb Command</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.HandledToolItemImpl#getParameters <em>Parameters</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class HandledToolItemImpl extends ElementContainerImpl<MMenuItem> implements MHandledToolItem {
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
	 * The cached value of the '{@link #getCommand() <em>Command</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommand()
	 * @generated
	 * @ordered
	 */
	protected MCommand command;

	/**
	 * The default value of the '{@link #getWbCommand() <em>Wb Command</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWbCommand()
	 * @generated
	 * @ordered
	 */
	protected static final ParameterizedCommand WB_COMMAND_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getWbCommand() <em>Wb Command</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWbCommand()
	 * @generated
	 * @ordered
	 */
	protected ParameterizedCommand wbCommand = WB_COMMAND_EDEFAULT;

	/**
	 * The cached value of the '{@link #getParameters() <em>Parameters</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParameters()
	 * @generated
	 * @ordered
	 */
	protected EList<MParameter> parameters;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected HandledToolItemImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MApplicationPackage.Literals.HANDLED_TOOL_ITEM;
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.HANDLED_TOOL_ITEM__LABEL, oldLabel, label));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.HANDLED_TOOL_ITEM__ICON_URI, oldIconURI, iconURI));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.HANDLED_TOOL_ITEM__TOOLTIP, oldTooltip, tooltip));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.HANDLED_TOOL_ITEM__ENABLED, oldEnabled, enabled));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.HANDLED_TOOL_ITEM__SELECTED, oldSelected, selected));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.HANDLED_TOOL_ITEM__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MCommand getCommand() {
		if (command != null && ((EObject)command).eIsProxy()) {
			InternalEObject oldCommand = (InternalEObject)command;
			command = (MCommand)eResolveProxy(oldCommand);
			if (command != oldCommand) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, MApplicationPackage.HANDLED_TOOL_ITEM__COMMAND, oldCommand, command));
			}
		}
		return command;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MCommand basicGetCommand() {
		return command;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCommand(MCommand newCommand) {
		MCommand oldCommand = command;
		command = newCommand;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.HANDLED_TOOL_ITEM__COMMAND, oldCommand, command));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ParameterizedCommand getWbCommand() {
		return wbCommand;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWbCommand(ParameterizedCommand newWbCommand) {
		ParameterizedCommand oldWbCommand = wbCommand;
		wbCommand = newWbCommand;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.HANDLED_TOOL_ITEM__WB_COMMAND, oldWbCommand, wbCommand));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MParameter> getParameters() {
		if (parameters == null) {
			parameters = new EObjectContainmentEList<MParameter>(MParameter.class, this, MApplicationPackage.HANDLED_TOOL_ITEM__PARAMETERS);
		}
		return parameters;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case MApplicationPackage.HANDLED_TOOL_ITEM__PARAMETERS:
				return ((InternalEList<?>)getParameters()).basicRemove(otherEnd, msgs);
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
			case MApplicationPackage.HANDLED_TOOL_ITEM__LABEL:
				return getLabel();
			case MApplicationPackage.HANDLED_TOOL_ITEM__ICON_URI:
				return getIconURI();
			case MApplicationPackage.HANDLED_TOOL_ITEM__TOOLTIP:
				return getTooltip();
			case MApplicationPackage.HANDLED_TOOL_ITEM__ENABLED:
				return isEnabled();
			case MApplicationPackage.HANDLED_TOOL_ITEM__SELECTED:
				return isSelected();
			case MApplicationPackage.HANDLED_TOOL_ITEM__TYPE:
				return getType();
			case MApplicationPackage.HANDLED_TOOL_ITEM__COMMAND:
				if (resolve) return getCommand();
				return basicGetCommand();
			case MApplicationPackage.HANDLED_TOOL_ITEM__WB_COMMAND:
				return getWbCommand();
			case MApplicationPackage.HANDLED_TOOL_ITEM__PARAMETERS:
				return getParameters();
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
			case MApplicationPackage.HANDLED_TOOL_ITEM__LABEL:
				setLabel((String)newValue);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__ICON_URI:
				setIconURI((String)newValue);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__TOOLTIP:
				setTooltip((String)newValue);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__ENABLED:
				setEnabled((Boolean)newValue);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__SELECTED:
				setSelected((Boolean)newValue);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__TYPE:
				setType((ItemType)newValue);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__COMMAND:
				setCommand((MCommand)newValue);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__WB_COMMAND:
				setWbCommand((ParameterizedCommand)newValue);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__PARAMETERS:
				getParameters().clear();
				getParameters().addAll((Collection<? extends MParameter>)newValue);
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
			case MApplicationPackage.HANDLED_TOOL_ITEM__LABEL:
				setLabel(LABEL_EDEFAULT);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__ICON_URI:
				setIconURI(ICON_URI_EDEFAULT);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__TOOLTIP:
				setTooltip(TOOLTIP_EDEFAULT);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__ENABLED:
				setEnabled(ENABLED_EDEFAULT);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__SELECTED:
				setSelected(SELECTED_EDEFAULT);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__COMMAND:
				setCommand((MCommand)null);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__WB_COMMAND:
				setWbCommand(WB_COMMAND_EDEFAULT);
				return;
			case MApplicationPackage.HANDLED_TOOL_ITEM__PARAMETERS:
				getParameters().clear();
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
			case MApplicationPackage.HANDLED_TOOL_ITEM__LABEL:
				return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
			case MApplicationPackage.HANDLED_TOOL_ITEM__ICON_URI:
				return ICON_URI_EDEFAULT == null ? iconURI != null : !ICON_URI_EDEFAULT.equals(iconURI);
			case MApplicationPackage.HANDLED_TOOL_ITEM__TOOLTIP:
				return TOOLTIP_EDEFAULT == null ? tooltip != null : !TOOLTIP_EDEFAULT.equals(tooltip);
			case MApplicationPackage.HANDLED_TOOL_ITEM__ENABLED:
				return enabled != ENABLED_EDEFAULT;
			case MApplicationPackage.HANDLED_TOOL_ITEM__SELECTED:
				return selected != SELECTED_EDEFAULT;
			case MApplicationPackage.HANDLED_TOOL_ITEM__TYPE:
				return type != TYPE_EDEFAULT;
			case MApplicationPackage.HANDLED_TOOL_ITEM__COMMAND:
				return command != null;
			case MApplicationPackage.HANDLED_TOOL_ITEM__WB_COMMAND:
				return WB_COMMAND_EDEFAULT == null ? wbCommand != null : !WB_COMMAND_EDEFAULT.equals(wbCommand);
			case MApplicationPackage.HANDLED_TOOL_ITEM__PARAMETERS:
				return parameters != null && !parameters.isEmpty();
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
				case MApplicationPackage.HANDLED_TOOL_ITEM__LABEL: return MApplicationPackage.UI_LABEL__LABEL;
				case MApplicationPackage.HANDLED_TOOL_ITEM__ICON_URI: return MApplicationPackage.UI_LABEL__ICON_URI;
				case MApplicationPackage.HANDLED_TOOL_ITEM__TOOLTIP: return MApplicationPackage.UI_LABEL__TOOLTIP;
				default: return -1;
			}
		}
		if (baseClass == MItem.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.HANDLED_TOOL_ITEM__ENABLED: return MApplicationPackage.ITEM__ENABLED;
				case MApplicationPackage.HANDLED_TOOL_ITEM__SELECTED: return MApplicationPackage.ITEM__SELECTED;
				case MApplicationPackage.HANDLED_TOOL_ITEM__TYPE: return MApplicationPackage.ITEM__TYPE;
				default: return -1;
			}
		}
		if (baseClass == MHandledItem.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.HANDLED_TOOL_ITEM__COMMAND: return MApplicationPackage.HANDLED_ITEM__COMMAND;
				case MApplicationPackage.HANDLED_TOOL_ITEM__WB_COMMAND: return MApplicationPackage.HANDLED_ITEM__WB_COMMAND;
				case MApplicationPackage.HANDLED_TOOL_ITEM__PARAMETERS: return MApplicationPackage.HANDLED_ITEM__PARAMETERS;
				default: return -1;
			}
		}
		if (baseClass == MToolItem.class) {
			switch (derivedFeatureID) {
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
				case MApplicationPackage.UI_LABEL__LABEL: return MApplicationPackage.HANDLED_TOOL_ITEM__LABEL;
				case MApplicationPackage.UI_LABEL__ICON_URI: return MApplicationPackage.HANDLED_TOOL_ITEM__ICON_URI;
				case MApplicationPackage.UI_LABEL__TOOLTIP: return MApplicationPackage.HANDLED_TOOL_ITEM__TOOLTIP;
				default: return -1;
			}
		}
		if (baseClass == MItem.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.ITEM__ENABLED: return MApplicationPackage.HANDLED_TOOL_ITEM__ENABLED;
				case MApplicationPackage.ITEM__SELECTED: return MApplicationPackage.HANDLED_TOOL_ITEM__SELECTED;
				case MApplicationPackage.ITEM__TYPE: return MApplicationPackage.HANDLED_TOOL_ITEM__TYPE;
				default: return -1;
			}
		}
		if (baseClass == MHandledItem.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.HANDLED_ITEM__COMMAND: return MApplicationPackage.HANDLED_TOOL_ITEM__COMMAND;
				case MApplicationPackage.HANDLED_ITEM__WB_COMMAND: return MApplicationPackage.HANDLED_TOOL_ITEM__WB_COMMAND;
				case MApplicationPackage.HANDLED_ITEM__PARAMETERS: return MApplicationPackage.HANDLED_TOOL_ITEM__PARAMETERS;
				default: return -1;
			}
		}
		if (baseClass == MToolItem.class) {
			switch (baseFeatureID) {
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
		result.append(", wbCommand: "); //$NON-NLS-1$
		result.append(wbCommand);
		result.append(')');
		return result.toString();
	}

} //HandledToolItemImpl
