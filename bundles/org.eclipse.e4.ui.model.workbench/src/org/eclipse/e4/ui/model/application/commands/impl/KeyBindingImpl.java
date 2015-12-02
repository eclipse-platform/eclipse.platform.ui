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
package org.eclipse.e4.ui.model.application.commands.impl;

import java.util.Collection;
import java.util.List;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MKeySequence;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.impl.ApplicationElementImpl;
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
 * An implementation of the model object '<em><b>Key Binding</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.impl.KeyBindingImpl#getKeySequence <em>Key Sequence</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.impl.KeyBindingImpl#getCommand <em>Command</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.impl.KeyBindingImpl#getParameters <em>Parameters</em>}</li>
 * </ul>
 *
 * @generated
 */
public class KeyBindingImpl extends ApplicationElementImpl implements MKeyBinding {
	/**
	 * The default value of the '{@link #getKeySequence() <em>Key Sequence</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKeySequence()
	 * @generated
	 * @ordered
	 */
	protected static final String KEY_SEQUENCE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getKeySequence() <em>Key Sequence</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKeySequence()
	 * @generated
	 * @ordered
	 */
	protected String keySequence = KEY_SEQUENCE_EDEFAULT;

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
	protected KeyBindingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return CommandsPackageImpl.Literals.KEY_BINDING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getKeySequence() {
		return keySequence;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setKeySequence(String newKeySequence) {
		String oldKeySequence = keySequence;
		keySequence = newKeySequence;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CommandsPackageImpl.KEY_BINDING__KEY_SEQUENCE, oldKeySequence, keySequence));
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, CommandsPackageImpl.KEY_BINDING__COMMAND, oldCommand, command));
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
			eNotify(new ENotificationImpl(this, Notification.SET, CommandsPackageImpl.KEY_BINDING__COMMAND, oldCommand, command));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MParameter> getParameters() {
		if (parameters == null) {
			parameters = new EObjectContainmentEList<MParameter>(MParameter.class, this, CommandsPackageImpl.KEY_BINDING__PARAMETERS);
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
			case CommandsPackageImpl.KEY_BINDING__PARAMETERS:
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
			case CommandsPackageImpl.KEY_BINDING__KEY_SEQUENCE:
				return getKeySequence();
			case CommandsPackageImpl.KEY_BINDING__COMMAND:
				if (resolve) return getCommand();
				return basicGetCommand();
			case CommandsPackageImpl.KEY_BINDING__PARAMETERS:
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
			case CommandsPackageImpl.KEY_BINDING__KEY_SEQUENCE:
				setKeySequence((String)newValue);
				return;
			case CommandsPackageImpl.KEY_BINDING__COMMAND:
				setCommand((MCommand)newValue);
				return;
			case CommandsPackageImpl.KEY_BINDING__PARAMETERS:
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
			case CommandsPackageImpl.KEY_BINDING__KEY_SEQUENCE:
				setKeySequence(KEY_SEQUENCE_EDEFAULT);
				return;
			case CommandsPackageImpl.KEY_BINDING__COMMAND:
				setCommand((MCommand)null);
				return;
			case CommandsPackageImpl.KEY_BINDING__PARAMETERS:
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
			case CommandsPackageImpl.KEY_BINDING__KEY_SEQUENCE:
				return KEY_SEQUENCE_EDEFAULT == null ? keySequence != null : !KEY_SEQUENCE_EDEFAULT.equals(keySequence);
			case CommandsPackageImpl.KEY_BINDING__COMMAND:
				return command != null;
			case CommandsPackageImpl.KEY_BINDING__PARAMETERS:
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
		if (baseClass == MKeySequence.class) {
			switch (derivedFeatureID) {
				case CommandsPackageImpl.KEY_BINDING__KEY_SEQUENCE: return CommandsPackageImpl.KEY_SEQUENCE__KEY_SEQUENCE;
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
		if (baseClass == MKeySequence.class) {
			switch (baseFeatureID) {
				case CommandsPackageImpl.KEY_SEQUENCE__KEY_SEQUENCE: return CommandsPackageImpl.KEY_BINDING__KEY_SEQUENCE;
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
		result.append(" (keySequence: "); //$NON-NLS-1$
		result.append(keySequence);
		result.append(')');
		return result.toString();
	}

} //KeyBindingImpl
