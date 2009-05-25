/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl;


import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Arena;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Forward;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Goalie;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Team</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.TeamImpl#getForwards <em>Forwards</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.TeamImpl#getDefencemen <em>Defencemen</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.TeamImpl#getGoalies <em>Goalies</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.TeamImpl#getArena <em>Arena</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TeamImpl extends HockeyleagueObjectImpl implements Team {
	/**
	 * The cached value of the '{@link #getForwards() <em>Forwards</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getForwards()
	 * @generated
	 * @ordered
	 */
	protected EList forwards;

	/**
	 * The cached value of the '{@link #getDefencemen() <em>Defencemen</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDefencemen()
	 * @generated
	 * @ordered
	 */
	protected EList defencemen;

	/**
	 * The cached value of the '{@link #getGoalies() <em>Goalies</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGoalies()
	 * @generated
	 * @ordered
	 */
	protected EList goalies;

	/**
	 * The cached value of the '{@link #getArena() <em>Arena</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getArena()
	 * @generated
	 * @ordered
	 */
	protected Arena arena;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TeamImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EClass eStaticClass() {
		return HockeyleaguePackage.Literals.TEAM;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList getForwards() {
		if (forwards == null) {
			forwards = new EObjectContainmentEList(Forward.class, this, HockeyleaguePackage.TEAM__FORWARDS);
		}
		return forwards;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList getDefencemen() {
		if (defencemen == null) {
			defencemen = new EObjectContainmentEList(Defence.class, this, HockeyleaguePackage.TEAM__DEFENCEMEN);
		}
		return defencemen;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList getGoalies() {
		if (goalies == null) {
			goalies = new EObjectContainmentEList(Goalie.class, this, HockeyleaguePackage.TEAM__GOALIES);
		}
		return goalies;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Arena getArena() {
		return arena;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetArena(Arena newArena, NotificationChain msgs) {
		Arena oldArena = arena;
		arena = newArena;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.TEAM__ARENA, oldArena, newArena);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setArena(Arena newArena) {
		if (newArena != arena) {
			NotificationChain msgs = null;
			if (arena != null)
				msgs = ((InternalEObject)arena).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - HockeyleaguePackage.TEAM__ARENA, null, msgs);
			if (newArena != null)
				msgs = ((InternalEObject)newArena).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - HockeyleaguePackage.TEAM__ARENA, null, msgs);
			msgs = basicSetArena(newArena, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.TEAM__ARENA, newArena, newArena));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case HockeyleaguePackage.TEAM__FORWARDS:
				return ((InternalEList)getForwards()).basicRemove(otherEnd, msgs);
			case HockeyleaguePackage.TEAM__DEFENCEMEN:
				return ((InternalEList)getDefencemen()).basicRemove(otherEnd, msgs);
			case HockeyleaguePackage.TEAM__GOALIES:
				return ((InternalEList)getGoalies()).basicRemove(otherEnd, msgs);
			case HockeyleaguePackage.TEAM__ARENA:
				return basicSetArena(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case HockeyleaguePackage.TEAM__FORWARDS:
				return getForwards();
			case HockeyleaguePackage.TEAM__DEFENCEMEN:
				return getDefencemen();
			case HockeyleaguePackage.TEAM__GOALIES:
				return getGoalies();
			case HockeyleaguePackage.TEAM__ARENA:
				return getArena();
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
			case HockeyleaguePackage.TEAM__FORWARDS:
				getForwards().clear();
				getForwards().addAll((Collection)newValue);
				return;
			case HockeyleaguePackage.TEAM__DEFENCEMEN:
				getDefencemen().clear();
				getDefencemen().addAll((Collection)newValue);
				return;
			case HockeyleaguePackage.TEAM__GOALIES:
				getGoalies().clear();
				getGoalies().addAll((Collection)newValue);
				return;
			case HockeyleaguePackage.TEAM__ARENA:
				setArena((Arena)newValue);
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
			case HockeyleaguePackage.TEAM__FORWARDS:
				getForwards().clear();
				return;
			case HockeyleaguePackage.TEAM__DEFENCEMEN:
				getDefencemen().clear();
				return;
			case HockeyleaguePackage.TEAM__GOALIES:
				getGoalies().clear();
				return;
			case HockeyleaguePackage.TEAM__ARENA:
				setArena((Arena)null);
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
			case HockeyleaguePackage.TEAM__FORWARDS:
				return forwards != null && !forwards.isEmpty();
			case HockeyleaguePackage.TEAM__DEFENCEMEN:
				return defencemen != null && !defencemen.isEmpty();
			case HockeyleaguePackage.TEAM__GOALIES:
				return goalies != null && !goalies.isEmpty();
			case HockeyleaguePackage.TEAM__ARENA:
				return arena != null;
		}
		return super.eIsSet(featureID);
	}

} //TeamImpl
