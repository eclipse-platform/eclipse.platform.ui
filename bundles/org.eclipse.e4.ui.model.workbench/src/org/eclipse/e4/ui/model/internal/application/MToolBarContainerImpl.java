/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *
 * $Id$
 */
package org.eclipse.e4.ui.model.internal.application;

import java.util.Collection;

import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MToolBarContainer;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>MTool Bar Container</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MToolBarContainerImpl#getToolbars <em>Toolbars</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MToolBarContainerImpl#isHorizontal <em>Horizontal</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MToolBarContainerImpl<I extends MToolBar> extends MApplicationElementImpl implements MToolBarContainer<I> {
	/**
	 * The cached value of the '{@link #getToolbars() <em>Toolbars</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getToolbars()
	 * @generated
	 * @ordered
	 */
	protected EList<I> toolbars;

	/**
	 * The default value of the '{@link #isHorizontal() <em>Horizontal</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHorizontal()
	 * @generated
	 * @ordered
	 */
	protected static final boolean HORIZONTAL_EDEFAULT = false;
	/**
	 * The flag representing the value of the '{@link #isHorizontal() <em>Horizontal</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHorizontal()
	 * @generated
	 * @ordered
	 */
	protected static final int HORIZONTAL_EFLAG = 1 << 8;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MToolBarContainerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ApplicationPackage.Literals.MTOOL_BAR_CONTAINER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<I> getToolbars() {
		if (toolbars == null) {
			toolbars = new EObjectContainmentEList<I>(MToolBar.class, this, ApplicationPackage.MTOOL_BAR_CONTAINER__TOOLBARS);
		}
		return toolbars;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isHorizontal() {
		return (eFlags & HORIZONTAL_EFLAG) != 0;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHorizontal(boolean newHorizontal) {
		boolean oldHorizontal = (eFlags & HORIZONTAL_EFLAG) != 0;
		if (newHorizontal) eFlags |= HORIZONTAL_EFLAG; else eFlags &= ~HORIZONTAL_EFLAG;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MTOOL_BAR_CONTAINER__HORIZONTAL, oldHorizontal, newHorizontal));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ApplicationPackage.MTOOL_BAR_CONTAINER__TOOLBARS:
				return ((InternalEList<?>)getToolbars()).basicRemove(otherEnd, msgs);
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
			case ApplicationPackage.MTOOL_BAR_CONTAINER__TOOLBARS:
				return getToolbars();
			case ApplicationPackage.MTOOL_BAR_CONTAINER__HORIZONTAL:
				return isHorizontal();
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
			case ApplicationPackage.MTOOL_BAR_CONTAINER__TOOLBARS:
				getToolbars().clear();
				getToolbars().addAll((Collection<? extends I>)newValue);
				return;
			case ApplicationPackage.MTOOL_BAR_CONTAINER__HORIZONTAL:
				setHorizontal((Boolean)newValue);
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
			case ApplicationPackage.MTOOL_BAR_CONTAINER__TOOLBARS:
				getToolbars().clear();
				return;
			case ApplicationPackage.MTOOL_BAR_CONTAINER__HORIZONTAL:
				setHorizontal(HORIZONTAL_EDEFAULT);
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
			case ApplicationPackage.MTOOL_BAR_CONTAINER__TOOLBARS:
				return toolbars != null && !toolbars.isEmpty();
			case ApplicationPackage.MTOOL_BAR_CONTAINER__HORIZONTAL:
				return ((eFlags & HORIZONTAL_EFLAG) != 0) != HORIZONTAL_EDEFAULT;
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
		result.append(" (horizontal: "); //$NON-NLS-1$
		result.append((eFlags & HORIZONTAL_EFLAG) != 0);
		result.append(')');
		return result.toString();
	}

} //MToolBarContainerImpl
