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
package org.eclipse.e4.ui.model.application.ui.impl;

import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Core Expression</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.impl.CoreExpressionImpl#getCoreExpressionId <em>Core Expression Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.impl.CoreExpressionImpl#getCoreExpression <em>Core Expression</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class CoreExpressionImpl extends ExpressionImpl implements MCoreExpression {
	/**
	 * The default value of the '{@link #getCoreExpressionId() <em>Core Expression Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCoreExpressionId()
	 * @generated
	 * @ordered
	 */
	protected static final String CORE_EXPRESSION_ID_EDEFAULT = ""; //$NON-NLS-1$

	/**
	 * The cached value of the '{@link #getCoreExpressionId() <em>Core Expression Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCoreExpressionId()
	 * @generated
	 * @ordered
	 */
	protected String coreExpressionId = CORE_EXPRESSION_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getCoreExpression() <em>Core Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCoreExpression()
	 * @generated
	 * @ordered
	 */
	protected static final Object CORE_EXPRESSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCoreExpression() <em>Core Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCoreExpression()
	 * @generated
	 * @ordered
	 */
	protected Object coreExpression = CORE_EXPRESSION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CoreExpressionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return UiPackageImpl.Literals.CORE_EXPRESSION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCoreExpressionId() {
		return coreExpressionId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCoreExpressionId(String newCoreExpressionId) {
		String oldCoreExpressionId = coreExpressionId;
		coreExpressionId = newCoreExpressionId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, UiPackageImpl.CORE_EXPRESSION__CORE_EXPRESSION_ID, oldCoreExpressionId, coreExpressionId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getCoreExpression() {
		return coreExpression;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCoreExpression(Object newCoreExpression) {
		Object oldCoreExpression = coreExpression;
		coreExpression = newCoreExpression;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, UiPackageImpl.CORE_EXPRESSION__CORE_EXPRESSION, oldCoreExpression, coreExpression));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case UiPackageImpl.CORE_EXPRESSION__CORE_EXPRESSION_ID:
				return getCoreExpressionId();
			case UiPackageImpl.CORE_EXPRESSION__CORE_EXPRESSION:
				return getCoreExpression();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case UiPackageImpl.CORE_EXPRESSION__CORE_EXPRESSION_ID:
				setCoreExpressionId((String)newValue);
				return;
			case UiPackageImpl.CORE_EXPRESSION__CORE_EXPRESSION:
				setCoreExpression(newValue);
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
			case UiPackageImpl.CORE_EXPRESSION__CORE_EXPRESSION_ID:
				setCoreExpressionId(CORE_EXPRESSION_ID_EDEFAULT);
				return;
			case UiPackageImpl.CORE_EXPRESSION__CORE_EXPRESSION:
				setCoreExpression(CORE_EXPRESSION_EDEFAULT);
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
			case UiPackageImpl.CORE_EXPRESSION__CORE_EXPRESSION_ID:
				return CORE_EXPRESSION_ID_EDEFAULT == null ? coreExpressionId != null : !CORE_EXPRESSION_ID_EDEFAULT.equals(coreExpressionId);
			case UiPackageImpl.CORE_EXPRESSION__CORE_EXPRESSION:
				return CORE_EXPRESSION_EDEFAULT == null ? coreExpression != null : !CORE_EXPRESSION_EDEFAULT.equals(coreExpression);
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
		result.append(" (coreExpressionId: "); //$NON-NLS-1$
		result.append(coreExpressionId);
		result.append(", coreExpression: "); //$NON-NLS-1$
		result.append(coreExpression);
		result.append(')');
		return result.toString();
	}

} //CoreExpressionImpl
