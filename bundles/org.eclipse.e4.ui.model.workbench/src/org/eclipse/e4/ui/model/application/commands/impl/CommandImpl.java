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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import org.eclipse.e4.ui.model.LocalizationHelper;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.impl.ApplicationElementImpl;
import org.eclipse.e4.ui.model.application.ui.MLocalizable;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
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
 * An implementation of the model object '<em><b>Command</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.impl.CommandImpl#getCommandName <em>Command Name</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.impl.CommandImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.impl.CommandImpl#getParameters <em>Parameters</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.impl.CommandImpl#getCategory <em>Category</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.impl.CommandImpl#getLocalizedCommandName <em>Localized Command Name</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.impl.CommandImpl#getLocalizedDescription <em>Localized Description</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CommandImpl extends ApplicationElementImpl implements MCommand {
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
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getParameters() <em>Parameters</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParameters()
	 * @generated
	 * @ordered
	 */
	protected EList<MCommandParameter> parameters;

	/**
	 * The cached value of the '{@link #getCategory() <em>Category</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCategory()
	 * @generated
	 * @ordered
	 */
	protected MCategory category;

	/**
	 * The default value of the '{@link #getLocalizedCommandName() <em>Localized Command Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalizedCommandName()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALIZED_COMMAND_NAME_EDEFAULT = null;

	/**
	 * The default value of the '{@link #getLocalizedDescription() <em>Localized Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalizedDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALIZED_DESCRIPTION_EDEFAULT = null;

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
		return CommandsPackageImpl.Literals.COMMAND;
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
			eNotify(new ENotificationImpl(this, Notification.SET, CommandsPackageImpl.COMMAND__COMMAND_NAME, oldCommandName, commandName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CommandsPackageImpl.COMMAND__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MCommandParameter> getParameters() {
		if (parameters == null) {
			parameters = new EObjectContainmentEList<MCommandParameter>(MCommandParameter.class, this, CommandsPackageImpl.COMMAND__PARAMETERS);
		}
		return parameters;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MCategory getCategory() {
		if (category != null && ((EObject)category).eIsProxy()) {
			InternalEObject oldCategory = (InternalEObject)category;
			category = (MCategory)eResolveProxy(oldCategory);
			if (category != oldCategory) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, CommandsPackageImpl.COMMAND__CATEGORY, oldCategory, category));
			}
		}
		return category;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MCategory basicGetCategory() {
		return category;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCategory(MCategory newCategory) {
		MCategory oldCategory = category;
		category = newCategory;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CommandsPackageImpl.COMMAND__CATEGORY, oldCategory, category));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public String getLocalizedCommandName() {
		return LocalizationHelper.getLocalizedFeature(CommandsPackageImpl.Literals.COMMAND__COMMAND_NAME, this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public String getLocalizedDescription() {
		return LocalizationHelper.getLocalizedFeature(CommandsPackageImpl.Literals.COMMAND__DESCRIPTION, this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void updateLocalization() {
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(
					this, Notification.SET, CommandsPackageImpl.COMMAND__LOCALIZED_COMMAND_NAME, null, getLocalizedCommandName()));
			eNotify(new ENotificationImpl(
					this, Notification.SET, CommandsPackageImpl.COMMAND__LOCALIZED_DESCRIPTION, null, getLocalizedDescription()));
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
			case CommandsPackageImpl.COMMAND__PARAMETERS:
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
			case CommandsPackageImpl.COMMAND__COMMAND_NAME:
				return getCommandName();
			case CommandsPackageImpl.COMMAND__DESCRIPTION:
				return getDescription();
			case CommandsPackageImpl.COMMAND__PARAMETERS:
				return getParameters();
			case CommandsPackageImpl.COMMAND__CATEGORY:
				if (resolve) return getCategory();
				return basicGetCategory();
			case CommandsPackageImpl.COMMAND__LOCALIZED_COMMAND_NAME:
				return getLocalizedCommandName();
			case CommandsPackageImpl.COMMAND__LOCALIZED_DESCRIPTION:
				return getLocalizedDescription();
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
			case CommandsPackageImpl.COMMAND__COMMAND_NAME:
				setCommandName((String)newValue);
				return;
			case CommandsPackageImpl.COMMAND__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case CommandsPackageImpl.COMMAND__PARAMETERS:
				getParameters().clear();
				getParameters().addAll((Collection<? extends MCommandParameter>)newValue);
				return;
			case CommandsPackageImpl.COMMAND__CATEGORY:
				setCategory((MCategory)newValue);
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
			case CommandsPackageImpl.COMMAND__COMMAND_NAME:
				setCommandName(COMMAND_NAME_EDEFAULT);
				return;
			case CommandsPackageImpl.COMMAND__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case CommandsPackageImpl.COMMAND__PARAMETERS:
				getParameters().clear();
				return;
			case CommandsPackageImpl.COMMAND__CATEGORY:
				setCategory((MCategory)null);
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
			case CommandsPackageImpl.COMMAND__COMMAND_NAME:
				return COMMAND_NAME_EDEFAULT == null ? commandName != null : !COMMAND_NAME_EDEFAULT.equals(commandName);
			case CommandsPackageImpl.COMMAND__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case CommandsPackageImpl.COMMAND__PARAMETERS:
				return parameters != null && !parameters.isEmpty();
			case CommandsPackageImpl.COMMAND__CATEGORY:
				return category != null;
			case CommandsPackageImpl.COMMAND__LOCALIZED_COMMAND_NAME:
				return LOCALIZED_COMMAND_NAME_EDEFAULT == null ? getLocalizedCommandName() != null : !LOCALIZED_COMMAND_NAME_EDEFAULT.equals(getLocalizedCommandName());
			case CommandsPackageImpl.COMMAND__LOCALIZED_DESCRIPTION:
				return LOCALIZED_DESCRIPTION_EDEFAULT == null ? getLocalizedDescription() != null : !LOCALIZED_DESCRIPTION_EDEFAULT.equals(getLocalizedDescription());
		}
		return super.eIsSet(featureID);
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
				case UiPackageImpl.LOCALIZABLE___UPDATE_LOCALIZATION: return CommandsPackageImpl.COMMAND___UPDATE_LOCALIZATION;
				default: return -1;
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
			case CommandsPackageImpl.COMMAND___UPDATE_LOCALIZATION:
				updateLocalization();
				return null;
		}
		return super.eInvoke(operationID, arguments);
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
		result.append(" (commandName: "); //$NON-NLS-1$
		result.append(commandName);
		result.append(", description: "); //$NON-NLS-1$
		result.append(description);
		result.append(')');
		return result.toString();
	}

} //CommandImpl
