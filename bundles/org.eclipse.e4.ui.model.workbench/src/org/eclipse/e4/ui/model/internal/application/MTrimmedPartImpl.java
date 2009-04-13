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

import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MToolBarContainer;
import org.eclipse.e4.ui.model.application.MTrimmedPart;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>MTrimmed Part</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MTrimmedPartImpl#getTopTrim <em>Top Trim</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MTrimmedPartImpl#getLeftTrim <em>Left Trim</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MTrimmedPartImpl#getRightTrim <em>Right Trim</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MTrimmedPartImpl#getBottomTrim <em>Bottom Trim</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.internal.application.MTrimmedPartImpl#getClientArea <em>Client Area</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MTrimmedPartImpl<P extends MPart<?>> extends MPartImpl<P> implements MTrimmedPart<P> {
	/**
	 * The cached value of the '{@link #getTopTrim() <em>Top Trim</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTopTrim()
	 * @generated
	 * @ordered
	 */
	protected MToolBarContainer<?> topTrim;

	/**
	 * The cached value of the '{@link #getLeftTrim() <em>Left Trim</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLeftTrim()
	 * @generated
	 * @ordered
	 */
	protected MToolBarContainer<?> leftTrim;

	/**
	 * The cached value of the '{@link #getRightTrim() <em>Right Trim</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRightTrim()
	 * @generated
	 * @ordered
	 */
	protected MToolBarContainer<?> rightTrim;

	/**
	 * The cached value of the '{@link #getBottomTrim() <em>Bottom Trim</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBottomTrim()
	 * @generated
	 * @ordered
	 */
	protected MToolBarContainer<?> bottomTrim;

	/**
	 * The cached value of the '{@link #getClientArea() <em>Client Area</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getClientArea()
	 * @generated
	 * @ordered
	 */
	protected MPart<?> clientArea;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected MTrimmedPartImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ApplicationPackage.Literals.MTRIMMED_PART;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBarContainer<?> getTopTrim() {
		return topTrim;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetTopTrim(MToolBarContainer<?> newTopTrim, NotificationChain msgs) {
		MToolBarContainer<?> oldTopTrim = topTrim;
		topTrim = newTopTrim;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackage.MTRIMMED_PART__TOP_TRIM, oldTopTrim, newTopTrim);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTopTrim(MToolBarContainer<?> newTopTrim) {
		if (newTopTrim != topTrim) {
			NotificationChain msgs = null;
			if (topTrim != null)
				msgs = ((InternalEObject)topTrim).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MTRIMMED_PART__TOP_TRIM, null, msgs);
			if (newTopTrim != null)
				msgs = ((InternalEObject)newTopTrim).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MTRIMMED_PART__TOP_TRIM, null, msgs);
			msgs = basicSetTopTrim(newTopTrim, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MTRIMMED_PART__TOP_TRIM, newTopTrim, newTopTrim));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBarContainer<?> getLeftTrim() {
		return leftTrim;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetLeftTrim(MToolBarContainer<?> newLeftTrim, NotificationChain msgs) {
		MToolBarContainer<?> oldLeftTrim = leftTrim;
		leftTrim = newLeftTrim;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackage.MTRIMMED_PART__LEFT_TRIM, oldLeftTrim, newLeftTrim);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLeftTrim(MToolBarContainer<?> newLeftTrim) {
		if (newLeftTrim != leftTrim) {
			NotificationChain msgs = null;
			if (leftTrim != null)
				msgs = ((InternalEObject)leftTrim).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MTRIMMED_PART__LEFT_TRIM, null, msgs);
			if (newLeftTrim != null)
				msgs = ((InternalEObject)newLeftTrim).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MTRIMMED_PART__LEFT_TRIM, null, msgs);
			msgs = basicSetLeftTrim(newLeftTrim, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MTRIMMED_PART__LEFT_TRIM, newLeftTrim, newLeftTrim));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBarContainer<?> getRightTrim() {
		return rightTrim;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRightTrim(MToolBarContainer<?> newRightTrim, NotificationChain msgs) {
		MToolBarContainer<?> oldRightTrim = rightTrim;
		rightTrim = newRightTrim;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackage.MTRIMMED_PART__RIGHT_TRIM, oldRightTrim, newRightTrim);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRightTrim(MToolBarContainer<?> newRightTrim) {
		if (newRightTrim != rightTrim) {
			NotificationChain msgs = null;
			if (rightTrim != null)
				msgs = ((InternalEObject)rightTrim).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MTRIMMED_PART__RIGHT_TRIM, null, msgs);
			if (newRightTrim != null)
				msgs = ((InternalEObject)newRightTrim).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MTRIMMED_PART__RIGHT_TRIM, null, msgs);
			msgs = basicSetRightTrim(newRightTrim, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MTRIMMED_PART__RIGHT_TRIM, newRightTrim, newRightTrim));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBarContainer<?> getBottomTrim() {
		return bottomTrim;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetBottomTrim(MToolBarContainer<?> newBottomTrim, NotificationChain msgs) {
		MToolBarContainer<?> oldBottomTrim = bottomTrim;
		bottomTrim = newBottomTrim;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackage.MTRIMMED_PART__BOTTOM_TRIM, oldBottomTrim, newBottomTrim);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBottomTrim(MToolBarContainer<?> newBottomTrim) {
		if (newBottomTrim != bottomTrim) {
			NotificationChain msgs = null;
			if (bottomTrim != null)
				msgs = ((InternalEObject)bottomTrim).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MTRIMMED_PART__BOTTOM_TRIM, null, msgs);
			if (newBottomTrim != null)
				msgs = ((InternalEObject)newBottomTrim).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MTRIMMED_PART__BOTTOM_TRIM, null, msgs);
			msgs = basicSetBottomTrim(newBottomTrim, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MTRIMMED_PART__BOTTOM_TRIM, newBottomTrim, newBottomTrim));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPart<?> getClientArea() {
		return clientArea;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetClientArea(MPart<?> newClientArea, NotificationChain msgs) {
		MPart<?> oldClientArea = clientArea;
		clientArea = newClientArea;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplicationPackage.MTRIMMED_PART__CLIENT_AREA, oldClientArea, newClientArea);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setClientArea(MPart<?> newClientArea) {
		if (newClientArea != clientArea) {
			NotificationChain msgs = null;
			if (clientArea != null)
				msgs = ((InternalEObject)clientArea).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MTRIMMED_PART__CLIENT_AREA, null, msgs);
			if (newClientArea != null)
				msgs = ((InternalEObject)newClientArea).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplicationPackage.MTRIMMED_PART__CLIENT_AREA, null, msgs);
			msgs = basicSetClientArea(newClientArea, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackage.MTRIMMED_PART__CLIENT_AREA, newClientArea, newClientArea));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ApplicationPackage.MTRIMMED_PART__TOP_TRIM:
				return basicSetTopTrim(null, msgs);
			case ApplicationPackage.MTRIMMED_PART__LEFT_TRIM:
				return basicSetLeftTrim(null, msgs);
			case ApplicationPackage.MTRIMMED_PART__RIGHT_TRIM:
				return basicSetRightTrim(null, msgs);
			case ApplicationPackage.MTRIMMED_PART__BOTTOM_TRIM:
				return basicSetBottomTrim(null, msgs);
			case ApplicationPackage.MTRIMMED_PART__CLIENT_AREA:
				return basicSetClientArea(null, msgs);
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
			case ApplicationPackage.MTRIMMED_PART__TOP_TRIM:
				return getTopTrim();
			case ApplicationPackage.MTRIMMED_PART__LEFT_TRIM:
				return getLeftTrim();
			case ApplicationPackage.MTRIMMED_PART__RIGHT_TRIM:
				return getRightTrim();
			case ApplicationPackage.MTRIMMED_PART__BOTTOM_TRIM:
				return getBottomTrim();
			case ApplicationPackage.MTRIMMED_PART__CLIENT_AREA:
				return getClientArea();
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
			case ApplicationPackage.MTRIMMED_PART__TOP_TRIM:
				setTopTrim((MToolBarContainer<?>)newValue);
				return;
			case ApplicationPackage.MTRIMMED_PART__LEFT_TRIM:
				setLeftTrim((MToolBarContainer<?>)newValue);
				return;
			case ApplicationPackage.MTRIMMED_PART__RIGHT_TRIM:
				setRightTrim((MToolBarContainer<?>)newValue);
				return;
			case ApplicationPackage.MTRIMMED_PART__BOTTOM_TRIM:
				setBottomTrim((MToolBarContainer<?>)newValue);
				return;
			case ApplicationPackage.MTRIMMED_PART__CLIENT_AREA:
				setClientArea((MPart<?>)newValue);
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
			case ApplicationPackage.MTRIMMED_PART__TOP_TRIM:
				setTopTrim((MToolBarContainer<?>)null);
				return;
			case ApplicationPackage.MTRIMMED_PART__LEFT_TRIM:
				setLeftTrim((MToolBarContainer<?>)null);
				return;
			case ApplicationPackage.MTRIMMED_PART__RIGHT_TRIM:
				setRightTrim((MToolBarContainer<?>)null);
				return;
			case ApplicationPackage.MTRIMMED_PART__BOTTOM_TRIM:
				setBottomTrim((MToolBarContainer<?>)null);
				return;
			case ApplicationPackage.MTRIMMED_PART__CLIENT_AREA:
				setClientArea((MPart<?>)null);
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
			case ApplicationPackage.MTRIMMED_PART__TOP_TRIM:
				return topTrim != null;
			case ApplicationPackage.MTRIMMED_PART__LEFT_TRIM:
				return leftTrim != null;
			case ApplicationPackage.MTRIMMED_PART__RIGHT_TRIM:
				return rightTrim != null;
			case ApplicationPackage.MTRIMMED_PART__BOTTOM_TRIM:
				return bottomTrim != null;
			case ApplicationPackage.MTRIMMED_PART__CLIENT_AREA:
				return clientArea != null;
		}
		return super.eIsSet(featureID);
	}

} //MTrimmedPartImpl
