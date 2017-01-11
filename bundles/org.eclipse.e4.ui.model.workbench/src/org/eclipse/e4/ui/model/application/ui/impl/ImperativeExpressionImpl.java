/**
 * Copyright (c) 2008, 2017 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.impl;

import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MImperativeExpression;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Imperative Expression</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.impl.ImperativeExpressionImpl#getContributionURI <em>Contribution URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.impl.ImperativeExpressionImpl#getObject <em>Object</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.impl.ImperativeExpressionImpl#isTracking <em>Tracking</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ImperativeExpressionImpl extends ExpressionImpl implements MImperativeExpression {
	/**
	 * The default value of the '{@link #getContributionURI() <em>Contribution URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContributionURI()
	 * @generated
	 * @ordered
	 */
	protected static final String CONTRIBUTION_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getContributionURI() <em>Contribution URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContributionURI()
	 * @generated
	 * @ordered
	 */
	protected String contributionURI = CONTRIBUTION_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getObject() <em>Object</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObject()
	 * @generated
	 * @ordered
	 */
	protected static final Object OBJECT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getObject() <em>Object</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObject()
	 * @generated
	 * @ordered
	 */
	protected Object object = OBJECT_EDEFAULT;

	/**
	 * The default value of the '{@link #isTracking() <em>Tracking</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isTracking()
	 * @generated
	 * @ordered
	 */
	protected static final boolean TRACKING_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isTracking() <em>Tracking</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isTracking()
	 * @generated
	 * @ordered
	 */
	protected boolean tracking = TRACKING_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ImperativeExpressionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return UiPackageImpl.Literals.IMPERATIVE_EXPRESSION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getContributionURI() {
		return contributionURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setContributionURI(String newContributionURI) {
		String oldContributionURI = contributionURI;
		contributionURI = newContributionURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, UiPackageImpl.IMPERATIVE_EXPRESSION__CONTRIBUTION_URI, oldContributionURI, contributionURI));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setObject(Object newObject) {
		Object oldObject = object;
		object = newObject;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, UiPackageImpl.IMPERATIVE_EXPRESSION__OBJECT, oldObject, object));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isTracking() {
		return tracking;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTracking(boolean newTracking) {
		boolean oldTracking = tracking;
		tracking = newTracking;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, UiPackageImpl.IMPERATIVE_EXPRESSION__TRACKING, oldTracking, tracking));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case UiPackageImpl.IMPERATIVE_EXPRESSION__CONTRIBUTION_URI:
			return getContributionURI();
		case UiPackageImpl.IMPERATIVE_EXPRESSION__OBJECT:
			return getObject();
		case UiPackageImpl.IMPERATIVE_EXPRESSION__TRACKING:
			return isTracking();
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
		case UiPackageImpl.IMPERATIVE_EXPRESSION__CONTRIBUTION_URI:
			setContributionURI((String)newValue);
			return;
		case UiPackageImpl.IMPERATIVE_EXPRESSION__OBJECT:
			setObject(newValue);
			return;
		case UiPackageImpl.IMPERATIVE_EXPRESSION__TRACKING:
			setTracking((Boolean)newValue);
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
		case UiPackageImpl.IMPERATIVE_EXPRESSION__CONTRIBUTION_URI:
			setContributionURI(CONTRIBUTION_URI_EDEFAULT);
			return;
		case UiPackageImpl.IMPERATIVE_EXPRESSION__OBJECT:
			setObject(OBJECT_EDEFAULT);
			return;
		case UiPackageImpl.IMPERATIVE_EXPRESSION__TRACKING:
			setTracking(TRACKING_EDEFAULT);
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
		case UiPackageImpl.IMPERATIVE_EXPRESSION__CONTRIBUTION_URI:
			return CONTRIBUTION_URI_EDEFAULT == null ? contributionURI != null : !CONTRIBUTION_URI_EDEFAULT.equals(contributionURI);
		case UiPackageImpl.IMPERATIVE_EXPRESSION__OBJECT:
			return OBJECT_EDEFAULT == null ? object != null : !OBJECT_EDEFAULT.equals(object);
		case UiPackageImpl.IMPERATIVE_EXPRESSION__TRACKING:
			return tracking != TRACKING_EDEFAULT;
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
		if (baseClass == MContribution.class) {
			switch (derivedFeatureID) {
			case UiPackageImpl.IMPERATIVE_EXPRESSION__CONTRIBUTION_URI: return ApplicationPackageImpl.CONTRIBUTION__CONTRIBUTION_URI;
			case UiPackageImpl.IMPERATIVE_EXPRESSION__OBJECT: return ApplicationPackageImpl.CONTRIBUTION__OBJECT;
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
		if (baseClass == MContribution.class) {
			switch (baseFeatureID) {
			case ApplicationPackageImpl.CONTRIBUTION__CONTRIBUTION_URI: return UiPackageImpl.IMPERATIVE_EXPRESSION__CONTRIBUTION_URI;
			case ApplicationPackageImpl.CONTRIBUTION__OBJECT: return UiPackageImpl.IMPERATIVE_EXPRESSION__OBJECT;
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
		result.append(" (contributionURI: "); //$NON-NLS-1$
		result.append(contributionURI);
		result.append(", object: "); //$NON-NLS-1$
		result.append(object);
		result.append(", tracking: "); //$NON-NLS-1$
		result.append(tracking);
		result.append(')');
		return result.toString();
	}

} //ImperativeExpressionImpl
