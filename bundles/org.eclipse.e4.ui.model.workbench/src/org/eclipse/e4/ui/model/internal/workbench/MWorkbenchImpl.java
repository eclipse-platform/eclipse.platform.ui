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

import org.eclipse.e4.ui.model.workbench.MWorkbench;
import org.eclipse.e4.ui.model.workbench.MWorkbenchWindow;
import org.eclipse.e4.ui.model.workbench.WorkbenchPackage;

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
 * An implementation of the model object '<em><b>MWorkbench</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.internal.workbench.MWorkbenchImpl#getWbWindows <em>Wb Windows</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.workbench.MWorkbenchImpl#getCurWBW <em>Cur WBW</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MWorkbenchImpl extends FlatEObjectImpl implements MWorkbench {
	/**
	 * The cached value of the '{@link #getWbWindows() <em>Wb Windows</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWbWindows()
	 * @generated
	 * @ordered
	 */
	protected EList<MWorkbenchWindow> wbWindows;

	/**
	 * The cached value of the '{@link #getCurWBW() <em>Cur WBW</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCurWBW()
	 * @generated
	 * @ordered
	 */
	protected MWorkbenchWindow curWBW;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MWorkbenchImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return WorkbenchPackage.Literals.MWORKBENCH;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MWorkbenchWindow> getWbWindows() {
		if (wbWindows == null) {
			wbWindows = new EObjectContainmentEList<MWorkbenchWindow>(MWorkbenchWindow.class, this, WorkbenchPackage.MWORKBENCH__WB_WINDOWS);
		}
		return wbWindows;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MWorkbenchWindow getCurWBW() {
		if (curWBW != null && curWBW.eIsProxy()) {
			InternalEObject oldCurWBW = (InternalEObject)curWBW;
			curWBW = (MWorkbenchWindow)eResolveProxy(oldCurWBW);
			if (curWBW != oldCurWBW) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, WorkbenchPackage.MWORKBENCH__CUR_WBW, oldCurWBW, curWBW));
			}
		}
		return curWBW;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MWorkbenchWindow basicGetCurWBW() {
		return curWBW;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCurWBW(MWorkbenchWindow newCurWBW) {
		MWorkbenchWindow oldCurWBW = curWBW;
		curWBW = newCurWBW;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WorkbenchPackage.MWORKBENCH__CUR_WBW, oldCurWBW, curWBW));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case WorkbenchPackage.MWORKBENCH__WB_WINDOWS:
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
			case WorkbenchPackage.MWORKBENCH__WB_WINDOWS:
				return getWbWindows();
			case WorkbenchPackage.MWORKBENCH__CUR_WBW:
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
			case WorkbenchPackage.MWORKBENCH__WB_WINDOWS:
				getWbWindows().clear();
				getWbWindows().addAll((Collection<? extends MWorkbenchWindow>)newValue);
				return;
			case WorkbenchPackage.MWORKBENCH__CUR_WBW:
				setCurWBW((MWorkbenchWindow)newValue);
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
			case WorkbenchPackage.MWORKBENCH__WB_WINDOWS:
				getWbWindows().clear();
				return;
			case WorkbenchPackage.MWORKBENCH__CUR_WBW:
				setCurWBW((MWorkbenchWindow)null);
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
			case WorkbenchPackage.MWORKBENCH__WB_WINDOWS:
				return wbWindows != null && !wbWindows.isEmpty();
			case WorkbenchPackage.MWORKBENCH__CUR_WBW:
				return curWBW != null;
		}
		return super.eIsSet(featureID);
	}

} //MWorkbenchImpl
