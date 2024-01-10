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

import java.util.Collection;
import java.util.List;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
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
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.HandledToolItemImpl#getCommand <em>Command</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.HandledToolItemImpl#getWbCommand <em>Wb Command</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.impl.HandledToolItemImpl#getParameters <em>Parameters</em>}</li>
 * </ul>
 *
 * @since 1.0
 * @generated
 */
public class HandledToolItemImpl extends ToolItemImpl implements MHandledToolItem {
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
	 * @noreference
	 * @generated
	 * @ordered
	 */
	protected static final ParameterizedCommand WB_COMMAND_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getWbCommand() <em>Wb Command</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWbCommand()
	 * @noreference
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
		return MenuPackageImpl.Literals.HANDLED_TOOL_ITEM;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MCommand getCommand() {
		if (command != null && ((EObject) command).eIsProxy()) {
			InternalEObject oldCommand = (InternalEObject) command;
			command = (MCommand) eResolveProxy(oldCommand);
			if (command != oldCommand) {
				if (eNotificationRequired()) {
					eNotify(new ENotificationImpl(this, Notification.RESOLVE,
							MenuPackageImpl.HANDLED_TOOL_ITEM__COMMAND, oldCommand, command));
				}
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
	@Override
	public void setCommand(MCommand newCommand) {
		MCommand oldCommand = command;
		command = newCommand;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.HANDLED_TOOL_ITEM__COMMAND,
					oldCommand, command));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @noreference
	 * @generated
	 */
	@Override
	public ParameterizedCommand getWbCommand() {
		return wbCommand;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @noreference
	 * @generated
	 */
	@Override
	public void setWbCommand(ParameterizedCommand newWbCommand) {
		ParameterizedCommand oldWbCommand = wbCommand;
		wbCommand = newWbCommand;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, MenuPackageImpl.HANDLED_TOOL_ITEM__WB_COMMAND,
					oldWbCommand, wbCommand));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<MParameter> getParameters() {
		if (parameters == null) {
			parameters = new EObjectContainmentEList<>(MParameter.class, this,
					MenuPackageImpl.HANDLED_TOOL_ITEM__PARAMETERS);
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
		case MenuPackageImpl.HANDLED_TOOL_ITEM__PARAMETERS:
			return ((InternalEList<?>) getParameters()).basicRemove(otherEnd, msgs);
		default:
			return super.eInverseRemove(otherEnd, featureID, msgs);
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
		case MenuPackageImpl.HANDLED_TOOL_ITEM__COMMAND:
			if (resolve) {
				return getCommand();
			}
			return basicGetCommand();
		case MenuPackageImpl.HANDLED_TOOL_ITEM__WB_COMMAND:
			return getWbCommand();
		case MenuPackageImpl.HANDLED_TOOL_ITEM__PARAMETERS:
			return getParameters();
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
		case MenuPackageImpl.HANDLED_TOOL_ITEM__COMMAND:
			setCommand((MCommand) newValue);
			return;
		case MenuPackageImpl.HANDLED_TOOL_ITEM__WB_COMMAND:
			setWbCommand((ParameterizedCommand) newValue);
			return;
		case MenuPackageImpl.HANDLED_TOOL_ITEM__PARAMETERS:
			getParameters().clear();
			getParameters().addAll((Collection<? extends MParameter>) newValue);
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
		case MenuPackageImpl.HANDLED_TOOL_ITEM__COMMAND:
			setCommand((MCommand) null);
			return;
		case MenuPackageImpl.HANDLED_TOOL_ITEM__WB_COMMAND:
			setWbCommand(WB_COMMAND_EDEFAULT);
			return;
		case MenuPackageImpl.HANDLED_TOOL_ITEM__PARAMETERS:
			getParameters().clear();
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
		case MenuPackageImpl.HANDLED_TOOL_ITEM__COMMAND:
			return command != null;
		case MenuPackageImpl.HANDLED_TOOL_ITEM__WB_COMMAND:
			return WB_COMMAND_EDEFAULT == null ? wbCommand != null : !WB_COMMAND_EDEFAULT.equals(wbCommand);
		case MenuPackageImpl.HANDLED_TOOL_ITEM__PARAMETERS:
			return parameters != null && !parameters.isEmpty();
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
		if (baseClass == MHandledItem.class) {
			switch (derivedFeatureID) {
			case MenuPackageImpl.HANDLED_TOOL_ITEM__COMMAND:
				return MenuPackageImpl.HANDLED_ITEM__COMMAND;
			case MenuPackageImpl.HANDLED_TOOL_ITEM__WB_COMMAND:
				return MenuPackageImpl.HANDLED_ITEM__WB_COMMAND;
			case MenuPackageImpl.HANDLED_TOOL_ITEM__PARAMETERS:
				return MenuPackageImpl.HANDLED_ITEM__PARAMETERS;
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
		if (baseClass == MHandledItem.class) {
			switch (baseFeatureID) {
			case MenuPackageImpl.HANDLED_ITEM__COMMAND:
				return MenuPackageImpl.HANDLED_TOOL_ITEM__COMMAND;
			case MenuPackageImpl.HANDLED_ITEM__WB_COMMAND:
				return MenuPackageImpl.HANDLED_TOOL_ITEM__WB_COMMAND;
			case MenuPackageImpl.HANDLED_ITEM__PARAMETERS:
				return MenuPackageImpl.HANDLED_TOOL_ITEM__PARAMETERS;
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
	public String toString() {
		if (eIsProxy()) {
			return super.toString();
		}

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (wbCommand: "); //$NON-NLS-1$
		result.append(wbCommand);
		result.append(')');
		return result.toString();
	}

} //HandledToolItemImpl
