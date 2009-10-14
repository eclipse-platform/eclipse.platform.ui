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

import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MCommand;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Command</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.CommandImpl#getCommandURI <em>Command URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.CommandImpl#getImpl <em>Impl</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.CommandImpl#getArgs <em>Args</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.CommandImpl#getCommandName <em>Command Name</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CommandImpl extends ApplicationElementImpl implements MCommand {
	/**
	 * The default value of the '{@link #getCommandURI() <em>Command URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommandURI()
	 * @generated
	 * @ordered
	 */
	protected static final String COMMAND_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCommandURI() <em>Command URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommandURI()
	 * @generated
	 * @ordered
	 */
	protected String commandURI = COMMAND_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getImpl() <em>Impl</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImpl()
	 * @generated
	 * @ordered
	 */
	protected static final Object IMPL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getImpl() <em>Impl</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImpl()
	 * @generated
	 * @ordered
	 */
	protected Object impl = IMPL_EDEFAULT;

	/**
	 * The cached value of the '{@link #getArgs() <em>Args</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArgs()
	 * @generated
	 * @ordered
	 */
	protected EList<String> args;

	/**
	 * The default value of the '{@link #getCommandName() <em>Command Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommandName()
	 * @generated
	 * @ordered
	 */
	protected static final String COMMAND_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCommandName() <em>Command Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommandName()
	 * @generated
	 * @ordered
	 */
	protected String commandName = COMMAND_NAME_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CommandImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MApplicationPackage.Literals.COMMAND;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCommandURI() {
		return commandURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCommandURI(String newCommandURI) {
		String oldCommandURI = commandURI;
		commandURI = newCommandURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.COMMAND__COMMAND_URI, oldCommandURI, commandURI));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getImpl() {
		return impl;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setImpl(Object newImpl) {
		Object oldImpl = impl;
		impl = newImpl;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.COMMAND__IMPL, oldImpl, impl));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getArgs() {
		if (args == null) {
			args = new EDataTypeUniqueEList<String>(String.class, this, MApplicationPackage.COMMAND__ARGS);
		}
		return args;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCommandName() {
		return commandName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCommandName(String newCommandName) {
		String oldCommandName = commandName;
		commandName = newCommandName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.COMMAND__COMMAND_NAME, oldCommandName, commandName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MApplicationPackage.COMMAND__COMMAND_URI:
				return getCommandURI();
			case MApplicationPackage.COMMAND__IMPL:
				return getImpl();
			case MApplicationPackage.COMMAND__ARGS:
				return getArgs();
			case MApplicationPackage.COMMAND__COMMAND_NAME:
				return getCommandName();
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
			case MApplicationPackage.COMMAND__COMMAND_URI:
				setCommandURI((String)newValue);
				return;
			case MApplicationPackage.COMMAND__IMPL:
				setImpl(newValue);
				return;
			case MApplicationPackage.COMMAND__ARGS:
				getArgs().clear();
				getArgs().addAll((Collection<? extends String>)newValue);
				return;
			case MApplicationPackage.COMMAND__COMMAND_NAME:
				setCommandName((String)newValue);
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
			case MApplicationPackage.COMMAND__COMMAND_URI:
				setCommandURI(COMMAND_URI_EDEFAULT);
				return;
			case MApplicationPackage.COMMAND__IMPL:
				setImpl(IMPL_EDEFAULT);
				return;
			case MApplicationPackage.COMMAND__ARGS:
				getArgs().clear();
				return;
			case MApplicationPackage.COMMAND__COMMAND_NAME:
				setCommandName(COMMAND_NAME_EDEFAULT);
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
			case MApplicationPackage.COMMAND__COMMAND_URI:
				return COMMAND_URI_EDEFAULT == null ? commandURI != null : !COMMAND_URI_EDEFAULT.equals(commandURI);
			case MApplicationPackage.COMMAND__IMPL:
				return IMPL_EDEFAULT == null ? impl != null : !IMPL_EDEFAULT.equals(impl);
			case MApplicationPackage.COMMAND__ARGS:
				return args != null && !args.isEmpty();
			case MApplicationPackage.COMMAND__COMMAND_NAME:
				return COMMAND_NAME_EDEFAULT == null ? commandName != null : !COMMAND_NAME_EDEFAULT.equals(commandName);
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
		result.append(" (commandURI: "); //$NON-NLS-1$
		result.append(commandURI);
		result.append(", impl: "); //$NON-NLS-1$
		result.append(impl);
		result.append(", args: "); //$NON-NLS-1$
		result.append(args);
		result.append(", commandName: "); //$NON-NLS-1$
		result.append(commandName);
		result.append(')');
		return result.toString();
	}

} //CommandImpl
