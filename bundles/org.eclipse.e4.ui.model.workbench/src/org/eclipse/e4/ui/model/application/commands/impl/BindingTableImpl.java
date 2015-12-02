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
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
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
 * An implementation of the model object '<em><b>Binding Table</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.impl.BindingTableImpl#getBindings <em>Bindings</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.commands.impl.BindingTableImpl#getBindingContext <em>Binding Context</em>}</li>
 * </ul>
 *
 * @generated
 */
public class BindingTableImpl extends ApplicationElementImpl implements MBindingTable {
	/**
	 * The cached value of the '{@link #getBindings() <em>Bindings</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindings()
	 * @generated
	 * @ordered
	 */
	protected EList<MKeyBinding> bindings;

	/**
	 * The cached value of the '{@link #getBindingContext() <em>Binding Context</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindingContext()
	 * @generated
	 * @ordered
	 */
	protected MBindingContext bindingContext;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BindingTableImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return CommandsPackageImpl.Literals.BINDING_TABLE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MKeyBinding> getBindings() {
		if (bindings == null) {
			bindings = new EObjectContainmentEList<MKeyBinding>(MKeyBinding.class, this, CommandsPackageImpl.BINDING_TABLE__BINDINGS);
		}
		return bindings;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MBindingContext getBindingContext() {
		if (bindingContext != null && ((EObject)bindingContext).eIsProxy()) {
			InternalEObject oldBindingContext = (InternalEObject)bindingContext;
			bindingContext = (MBindingContext)eResolveProxy(oldBindingContext);
			if (bindingContext != oldBindingContext) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, CommandsPackageImpl.BINDING_TABLE__BINDING_CONTEXT, oldBindingContext, bindingContext));
			}
		}
		return bindingContext;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MBindingContext basicGetBindingContext() {
		return bindingContext;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBindingContext(MBindingContext newBindingContext) {
		MBindingContext oldBindingContext = bindingContext;
		bindingContext = newBindingContext;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, CommandsPackageImpl.BINDING_TABLE__BINDING_CONTEXT, oldBindingContext, bindingContext));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case CommandsPackageImpl.BINDING_TABLE__BINDINGS:
				return ((InternalEList<?>)getBindings()).basicRemove(otherEnd, msgs);
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
			case CommandsPackageImpl.BINDING_TABLE__BINDINGS:
				return getBindings();
			case CommandsPackageImpl.BINDING_TABLE__BINDING_CONTEXT:
				if (resolve) return getBindingContext();
				return basicGetBindingContext();
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
			case CommandsPackageImpl.BINDING_TABLE__BINDINGS:
				getBindings().clear();
				getBindings().addAll((Collection<? extends MKeyBinding>)newValue);
				return;
			case CommandsPackageImpl.BINDING_TABLE__BINDING_CONTEXT:
				setBindingContext((MBindingContext)newValue);
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
			case CommandsPackageImpl.BINDING_TABLE__BINDINGS:
				getBindings().clear();
				return;
			case CommandsPackageImpl.BINDING_TABLE__BINDING_CONTEXT:
				setBindingContext((MBindingContext)null);
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
			case CommandsPackageImpl.BINDING_TABLE__BINDINGS:
				return bindings != null && !bindings.isEmpty();
			case CommandsPackageImpl.BINDING_TABLE__BINDING_CONTEXT:
				return bindingContext != null;
		}
		return super.eIsSet(featureID);
	}

} //BindingTableImpl
