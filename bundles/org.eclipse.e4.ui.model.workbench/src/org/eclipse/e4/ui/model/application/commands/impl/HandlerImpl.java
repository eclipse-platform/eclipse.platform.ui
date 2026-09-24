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
package org.eclipse.e4.ui.model.application.commands.impl;

import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.impl.ContributionImpl;
import org.eclipse.e4.ui.model.application.ui.MExpression;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Handler</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.impl.HandlerImpl#getCommand <em>Command</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.impl.HandlerImpl#getEnabledWhen <em>Enabled When</em>}</li>
 * </ul>
 *
 * @since 1.0
 * @generated
 */
public class HandlerImpl extends ContributionImpl implements MHandler {
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
	 * The cached value of the '{@link #getEnabledWhen() <em>Enabled When</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEnabledWhen()
	 * @generated
	 * @ordered
	 */
	protected MExpression enabledWhen;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected HandlerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return CommandsPackageImpl.Literals.HANDLER;
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, CommandsPackageImpl.HANDLER__COMMAND,
							oldCommand, command));
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
			eNotify(new ENotificationImpl(this, Notification.SET, CommandsPackageImpl.HANDLER__COMMAND, oldCommand,
					command));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MExpression getEnabledWhen() {
		return enabledWhen;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetEnabledWhen(MExpression newEnabledWhen, NotificationChain msgs) {
		MExpression oldEnabledWhen = enabledWhen;
		enabledWhen = newEnabledWhen;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					CommandsPackageImpl.HANDLER__ENABLED_WHEN, oldEnabledWhen, newEnabledWhen);
			if (msgs == null) {
				msgs = notification;
			} else {
				msgs.add(notification);
			}
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEnabledWhen(MExpression newEnabledWhen) {
		if (newEnabledWhen != enabledWhen) {
			NotificationChain msgs = null;
			if (enabledWhen != null) {
				msgs = ((InternalEObject) enabledWhen).eInverseRemove(this,
						EOPPOSITE_FEATURE_BASE - CommandsPackageImpl.HANDLER__ENABLED_WHEN, null, msgs);
			}
			if (newEnabledWhen != null) {
				msgs = ((InternalEObject) newEnabledWhen).eInverseAdd(this,
						EOPPOSITE_FEATURE_BASE - CommandsPackageImpl.HANDLER__ENABLED_WHEN, null, msgs);
			}
			msgs = basicSetEnabledWhen(newEnabledWhen, msgs);
			if (msgs != null) {
				msgs.dispatch();
			}
		} else if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, CommandsPackageImpl.HANDLER__ENABLED_WHEN,
					newEnabledWhen, newEnabledWhen));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case CommandsPackageImpl.HANDLER__ENABLED_WHEN:
			return basicSetEnabledWhen(null, msgs);
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
		case CommandsPackageImpl.HANDLER__COMMAND:
			if (resolve) {
				return getCommand();
			}
			return basicGetCommand();
		case CommandsPackageImpl.HANDLER__ENABLED_WHEN:
			return getEnabledWhen();
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
		case CommandsPackageImpl.HANDLER__COMMAND:
			setCommand((MCommand) newValue);
			return;
		case CommandsPackageImpl.HANDLER__ENABLED_WHEN:
			setEnabledWhen((MExpression) newValue);
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
		case CommandsPackageImpl.HANDLER__COMMAND:
			setCommand((MCommand) null);
			return;
		case CommandsPackageImpl.HANDLER__ENABLED_WHEN:
			setEnabledWhen((MExpression) null);
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
		case CommandsPackageImpl.HANDLER__COMMAND:
			return command != null;
		case CommandsPackageImpl.HANDLER__ENABLED_WHEN:
			return enabledWhen != null;
		default:
			return super.eIsSet(featureID);
		}
	}

} //HandlerImpl
