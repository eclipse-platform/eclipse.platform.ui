/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl;


import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HeightKind;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ShotKind;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.WeightKind;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Player</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerImpl#getBirthplace <em>Birthplace</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerImpl#getNumber <em>Number</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerImpl#getHeightMesurement <em>Height Mesurement</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerImpl#getHeightValue <em>Height Value</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerImpl#getWeightMesurement <em>Weight Mesurement</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerImpl#getWeightValue <em>Weight Value</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerImpl#getShot <em>Shot</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerImpl#getBirthdate <em>Birthdate</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class PlayerImpl extends HockeyleagueObjectImpl implements Player {
	/**
	 * The default value of the '{@link #getBirthplace() <em>Birthplace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBirthplace()
	 * @generated
	 * @ordered
	 */
	protected static final String BIRTHPLACE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBirthplace() <em>Birthplace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBirthplace()
	 * @generated
	 * @ordered
	 */
	protected String birthplace = BIRTHPLACE_EDEFAULT;

	/**
	 * The default value of the '{@link #getNumber() <em>Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumber()
	 * @generated
	 * @ordered
	 */
	protected static final int NUMBER_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getNumber() <em>Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNumber()
	 * @generated
	 * @ordered
	 */
	protected int number = NUMBER_EDEFAULT;

	/**
	 * The default value of the '{@link #getHeightMesurement() <em>Height Mesurement</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHeightMesurement()
	 * @generated
	 * @ordered
	 */
	protected static final HeightKind HEIGHT_MESUREMENT_EDEFAULT = HeightKind.INCHES_LITERAL;

	/**
	 * The cached value of the '{@link #getHeightMesurement() <em>Height Mesurement</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHeightMesurement()
	 * @generated
	 * @ordered
	 */
	protected HeightKind heightMesurement = HEIGHT_MESUREMENT_EDEFAULT;

	/**
	 * The default value of the '{@link #getHeightValue() <em>Height Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHeightValue()
	 * @generated
	 * @ordered
	 */
	protected static final int HEIGHT_VALUE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getHeightValue() <em>Height Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHeightValue()
	 * @generated
	 * @ordered
	 */
	protected int heightValue = HEIGHT_VALUE_EDEFAULT;

	/**
	 * The default value of the '{@link #getWeightMesurement() <em>Weight Mesurement</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWeightMesurement()
	 * @generated
	 * @ordered
	 */
	protected static final WeightKind WEIGHT_MESUREMENT_EDEFAULT = WeightKind.POUNDS_LITERAL;

	/**
	 * The cached value of the '{@link #getWeightMesurement() <em>Weight Mesurement</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWeightMesurement()
	 * @generated
	 * @ordered
	 */
	protected WeightKind weightMesurement = WEIGHT_MESUREMENT_EDEFAULT;

	/**
	 * The default value of the '{@link #getWeightValue() <em>Weight Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWeightValue()
	 * @generated
	 * @ordered
	 */
	protected static final int WEIGHT_VALUE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getWeightValue() <em>Weight Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWeightValue()
	 * @generated
	 * @ordered
	 */
	protected int weightValue = WEIGHT_VALUE_EDEFAULT;

	/**
	 * The default value of the '{@link #getShot() <em>Shot</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShot()
	 * @generated
	 * @ordered
	 */
	protected static final ShotKind SHOT_EDEFAULT = ShotKind.LEFT_LITERAL;

	/**
	 * The cached value of the '{@link #getShot() <em>Shot</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShot()
	 * @generated
	 * @ordered
	 */
	protected ShotKind shot = SHOT_EDEFAULT;

	/**
	 * The default value of the '{@link #getBirthdate() <em>Birthdate</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBirthdate()
	 * @generated
	 * @ordered
	 */
	protected static final String BIRTHDATE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBirthdate() <em>Birthdate</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBirthdate()
	 * @generated
	 * @ordered
	 */
	protected String birthdate = BIRTHDATE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PlayerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EClass eStaticClass() {
		return HockeyleaguePackage.Literals.PLAYER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getBirthplace() {
		return birthplace;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBirthplace(String newBirthplace) {
		String oldBirthplace = birthplace;
		birthplace = newBirthplace;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER__BIRTHPLACE, oldBirthplace, birthplace));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNumber(int newNumber) {
		int oldNumber = number;
		number = newNumber;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER__NUMBER, oldNumber, number));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public HeightKind getHeightMesurement() {
		return heightMesurement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHeightMesurement(HeightKind newHeightMesurement) {
		HeightKind oldHeightMesurement = heightMesurement;
		heightMesurement = newHeightMesurement == null ? HEIGHT_MESUREMENT_EDEFAULT : newHeightMesurement;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER__HEIGHT_MESUREMENT, oldHeightMesurement, heightMesurement));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getHeightValue() {
		return heightValue;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHeightValue(int newHeightValue) {
		int oldHeightValue = heightValue;
		heightValue = newHeightValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER__HEIGHT_VALUE, oldHeightValue, heightValue));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WeightKind getWeightMesurement() {
		return weightMesurement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWeightMesurement(WeightKind newWeightMesurement) {
		WeightKind oldWeightMesurement = weightMesurement;
		weightMesurement = newWeightMesurement == null ? WEIGHT_MESUREMENT_EDEFAULT : newWeightMesurement;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER__WEIGHT_MESUREMENT, oldWeightMesurement, weightMesurement));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getWeightValue() {
		return weightValue;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWeightValue(int newWeightValue) {
		int oldWeightValue = weightValue;
		weightValue = newWeightValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER__WEIGHT_VALUE, oldWeightValue, weightValue));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ShotKind getShot() {
		return shot;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setShot(ShotKind newShot) {
		ShotKind oldShot = shot;
		shot = newShot == null ? SHOT_EDEFAULT : newShot;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER__SHOT, oldShot, shot));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getBirthdate() {
		return birthdate;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBirthdate(String newBirthdate) {
		String oldBirthdate = birthdate;
		birthdate = newBirthdate;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER__BIRTHDATE, oldBirthdate, birthdate));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case HockeyleaguePackage.PLAYER__BIRTHPLACE:
				return getBirthplace();
			case HockeyleaguePackage.PLAYER__NUMBER:
				return new Integer(getNumber());
			case HockeyleaguePackage.PLAYER__HEIGHT_MESUREMENT:
				return getHeightMesurement();
			case HockeyleaguePackage.PLAYER__HEIGHT_VALUE:
				return new Integer(getHeightValue());
			case HockeyleaguePackage.PLAYER__WEIGHT_MESUREMENT:
				return getWeightMesurement();
			case HockeyleaguePackage.PLAYER__WEIGHT_VALUE:
				return new Integer(getWeightValue());
			case HockeyleaguePackage.PLAYER__SHOT:
				return getShot();
			case HockeyleaguePackage.PLAYER__BIRTHDATE:
				return getBirthdate();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case HockeyleaguePackage.PLAYER__BIRTHPLACE:
				setBirthplace((String)newValue);
				return;
			case HockeyleaguePackage.PLAYER__NUMBER:
				setNumber(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.PLAYER__HEIGHT_MESUREMENT:
				setHeightMesurement((HeightKind)newValue);
				return;
			case HockeyleaguePackage.PLAYER__HEIGHT_VALUE:
				setHeightValue(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.PLAYER__WEIGHT_MESUREMENT:
				setWeightMesurement((WeightKind)newValue);
				return;
			case HockeyleaguePackage.PLAYER__WEIGHT_VALUE:
				setWeightValue(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.PLAYER__SHOT:
				setShot((ShotKind)newValue);
				return;
			case HockeyleaguePackage.PLAYER__BIRTHDATE:
				setBirthdate((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void eUnset(int featureID) {
		switch (featureID) {
			case HockeyleaguePackage.PLAYER__BIRTHPLACE:
				setBirthplace(BIRTHPLACE_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER__NUMBER:
				setNumber(NUMBER_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER__HEIGHT_MESUREMENT:
				setHeightMesurement(HEIGHT_MESUREMENT_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER__HEIGHT_VALUE:
				setHeightValue(HEIGHT_VALUE_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER__WEIGHT_MESUREMENT:
				setWeightMesurement(WEIGHT_MESUREMENT_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER__WEIGHT_VALUE:
				setWeightValue(WEIGHT_VALUE_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER__SHOT:
				setShot(SHOT_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER__BIRTHDATE:
				setBirthdate(BIRTHDATE_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case HockeyleaguePackage.PLAYER__BIRTHPLACE:
				return BIRTHPLACE_EDEFAULT == null ? birthplace != null : !BIRTHPLACE_EDEFAULT.equals(birthplace);
			case HockeyleaguePackage.PLAYER__NUMBER:
				return number != NUMBER_EDEFAULT;
			case HockeyleaguePackage.PLAYER__HEIGHT_MESUREMENT:
				return heightMesurement != HEIGHT_MESUREMENT_EDEFAULT;
			case HockeyleaguePackage.PLAYER__HEIGHT_VALUE:
				return heightValue != HEIGHT_VALUE_EDEFAULT;
			case HockeyleaguePackage.PLAYER__WEIGHT_MESUREMENT:
				return weightMesurement != WEIGHT_MESUREMENT_EDEFAULT;
			case HockeyleaguePackage.PLAYER__WEIGHT_VALUE:
				return weightValue != WEIGHT_VALUE_EDEFAULT;
			case HockeyleaguePackage.PLAYER__SHOT:
				return shot != SHOT_EDEFAULT;
			case HockeyleaguePackage.PLAYER__BIRTHDATE:
				return BIRTHDATE_EDEFAULT == null ? birthdate != null : !BIRTHDATE_EDEFAULT.equals(birthdate);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (birthplace: "); //$NON-NLS-1$
		result.append(birthplace);
		result.append(", number: "); //$NON-NLS-1$
		result.append(number);
		result.append(", heightMesurement: "); //$NON-NLS-1$
		result.append(heightMesurement);
		result.append(", heightValue: "); //$NON-NLS-1$
		result.append(heightValue);
		result.append(", weightMesurement: "); //$NON-NLS-1$
		result.append(weightMesurement);
		result.append(", weightValue: "); //$NON-NLS-1$
		result.append(weightValue);
		result.append(", shot: "); //$NON-NLS-1$
		result.append(shot);
		result.append(", birthdate: "); //$NON-NLS-1$
		result.append(birthdate);
		result.append(')');
		return result.toString();
	}

} //PlayerImpl
