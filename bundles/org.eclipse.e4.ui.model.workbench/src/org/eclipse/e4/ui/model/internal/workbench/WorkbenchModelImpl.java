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
package org.eclipse.e4.ui.model.internal.workbench;

import java.util.Collection;

import org.eclipse.e4.ui.model.workbench.WorkbenchModel;
import org.eclipse.e4.ui.model.workbench.WorkbenchPackage;
import org.eclipse.e4.ui.model.workbench.WorkbenchWindow;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.FlatEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.internal.workbench.WorkbenchModelImpl#getWbWindows <em>Wb Windows</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.workbench.WorkbenchModelImpl#getCurWBW <em>Cur WBW</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class WorkbenchModelImpl extends FlatEObjectImpl implements WorkbenchModel {
	/**
	 * The cached value of the '{@link #getWbWindows() <em>Wb Windows</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWbWindows()
	 * @generated
	 * @ordered
	 */
	protected EList<WorkbenchWindow> wbWindows;

	/**
	 * The cached value of the '{@link #getCurWBW() <em>Cur WBW</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCurWBW()
	 * @generated
	 * @ordered
	 */
	protected WorkbenchWindow curWBW;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected WorkbenchModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return WorkbenchPackage.Literals.WORKBENCH_MODEL;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<WorkbenchWindow> getWbWindows() {
		if (wbWindows == null) {
			wbWindows = new EObjectContainmentEList<WorkbenchWindow>(WorkbenchWindow.class, this, WorkbenchPackage.WORKBENCH_MODEL__WB_WINDOWS);
		}
		return wbWindows;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WorkbenchWindow getCurWBW() {
		if (curWBW != null && curWBW.eIsProxy()) {
			InternalEObject oldCurWBW = (InternalEObject)curWBW;
			curWBW = (WorkbenchWindow)eResolveProxy(oldCurWBW);
			if (curWBW != oldCurWBW) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, WorkbenchPackage.WORKBENCH_MODEL__CUR_WBW, oldCurWBW, curWBW));
			}
		}
		return curWBW;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WorkbenchWindow basicGetCurWBW() {
		return curWBW;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCurWBW(WorkbenchWindow newCurWBW) {
		WorkbenchWindow oldCurWBW = curWBW;
		curWBW = newCurWBW;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WorkbenchPackage.WORKBENCH_MODEL__CUR_WBW, oldCurWBW, curWBW));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case WorkbenchPackage.WORKBENCH_MODEL__WB_WINDOWS:
				return ((InternalEList<?>)getWbWindows()).basicRemove(otherEnd, msgs);
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
			case WorkbenchPackage.WORKBENCH_MODEL__WB_WINDOWS:
				return getWbWindows();
			case WorkbenchPackage.WORKBENCH_MODEL__CUR_WBW:
				if (resolve) return getCurWBW();
				return basicGetCurWBW();
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
			case WorkbenchPackage.WORKBENCH_MODEL__WB_WINDOWS:
				getWbWindows().clear();
				getWbWindows().addAll((Collection<? extends WorkbenchWindow>)newValue);
				return;
			case WorkbenchPackage.WORKBENCH_MODEL__CUR_WBW:
				setCurWBW((WorkbenchWindow)newValue);
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
			case WorkbenchPackage.WORKBENCH_MODEL__WB_WINDOWS:
				getWbWindows().clear();
				return;
			case WorkbenchPackage.WORKBENCH_MODEL__CUR_WBW:
				setCurWBW((WorkbenchWindow)null);
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
			case WorkbenchPackage.WORKBENCH_MODEL__WB_WINDOWS:
				return wbWindows != null && !wbWindows.isEmpty();
			case WorkbenchPackage.WORKBENCH_MODEL__CUR_WBW:
				return curWBW != null;
		}
		return super.eIsSet(featureID);
	}

} //WorkbenchModelImpl
