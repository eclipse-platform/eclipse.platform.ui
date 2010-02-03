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

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MKeyBinding;
import org.eclipse.e4.ui.model.application.MParameter;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Key Binding</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.KeyBindingImpl#getKeySequence <em>Key Sequence</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.KeyBindingImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.KeyBindingImpl#getTags <em>Tags</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.KeyBindingImpl#getCommand <em>Command</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.KeyBindingImpl#getParameters <em>Parameters</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class KeyBindingImpl extends EObjectImpl implements MKeyBinding {
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
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getTags() <em>Tags</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTags()
	 * @generated
	 * @ordered
	 */
	protected static final String TAGS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTags() <em>Tags</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTags()
	 * @generated
	 * @ordered
	 */
	protected String tags = TAGS_EDEFAULT;

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
		return MApplicationPackage.Literals.KEY_BINDING;
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.KEY_BINDING__KEY_SEQUENCE, oldKeySequence, keySequence));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.KEY_BINDING__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getTags() {
		return tags;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTags(String newTags) {
		String oldTags = tags;
		tags = newTags;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.KEY_BINDING__TAGS, oldTags, tags));
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, MApplicationPackage.KEY_BINDING__COMMAND, oldCommand, command));
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
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.KEY_BINDING__COMMAND, oldCommand, command));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MParameter> getParameters() {
		if (parameters == null) {
			parameters = new EObjectContainmentEList<MParameter>(MParameter.class, this, MApplicationPackage.KEY_BINDING__PARAMETERS);
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
			case MApplicationPackage.KEY_BINDING__PARAMETERS:
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
			case MApplicationPackage.KEY_BINDING__KEY_SEQUENCE:
				return getKeySequence();
			case MApplicationPackage.KEY_BINDING__ID:
				return getId();
			case MApplicationPackage.KEY_BINDING__TAGS:
				return getTags();
			case MApplicationPackage.KEY_BINDING__COMMAND:
				if (resolve) return getCommand();
				return basicGetCommand();
			case MApplicationPackage.KEY_BINDING__PARAMETERS:
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
			case MApplicationPackage.KEY_BINDING__KEY_SEQUENCE:
				setKeySequence((String)newValue);
				return;
			case MApplicationPackage.KEY_BINDING__ID:
				setId((String)newValue);
				return;
			case MApplicationPackage.KEY_BINDING__TAGS:
				setTags((String)newValue);
				return;
			case MApplicationPackage.KEY_BINDING__COMMAND:
				setCommand((MCommand)newValue);
				return;
			case MApplicationPackage.KEY_BINDING__PARAMETERS:
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
			case MApplicationPackage.KEY_BINDING__KEY_SEQUENCE:
				setKeySequence(KEY_SEQUENCE_EDEFAULT);
				return;
			case MApplicationPackage.KEY_BINDING__ID:
				setId(ID_EDEFAULT);
				return;
			case MApplicationPackage.KEY_BINDING__TAGS:
				setTags(TAGS_EDEFAULT);
				return;
			case MApplicationPackage.KEY_BINDING__COMMAND:
				setCommand((MCommand)null);
				return;
			case MApplicationPackage.KEY_BINDING__PARAMETERS:
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
			case MApplicationPackage.KEY_BINDING__KEY_SEQUENCE:
				return KEY_SEQUENCE_EDEFAULT == null ? keySequence != null : !KEY_SEQUENCE_EDEFAULT.equals(keySequence);
			case MApplicationPackage.KEY_BINDING__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case MApplicationPackage.KEY_BINDING__TAGS:
				return TAGS_EDEFAULT == null ? tags != null : !TAGS_EDEFAULT.equals(tags);
			case MApplicationPackage.KEY_BINDING__COMMAND:
				return command != null;
			case MApplicationPackage.KEY_BINDING__PARAMETERS:
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
		if (baseClass == MApplicationElement.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.KEY_BINDING__ID: return MApplicationPackage.APPLICATION_ELEMENT__ID;
				case MApplicationPackage.KEY_BINDING__TAGS: return MApplicationPackage.APPLICATION_ELEMENT__TAGS;
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
		if (baseClass == MApplicationElement.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.APPLICATION_ELEMENT__ID: return MApplicationPackage.KEY_BINDING__ID;
				case MApplicationPackage.APPLICATION_ELEMENT__TAGS: return MApplicationPackage.KEY_BINDING__TAGS;
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
		result.append(", id: "); //$NON-NLS-1$
		result.append(id);
		result.append(", tags: "); //$NON-NLS-1$
		result.append(tags);
		result.append(')');
		return result.toString();
	}

} //KeyBindingImpl
