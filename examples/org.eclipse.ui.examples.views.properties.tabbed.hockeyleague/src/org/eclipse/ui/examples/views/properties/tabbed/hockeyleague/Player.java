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
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Player</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getBirthplace <em>Birthplace</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getNumber <em>Number</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getHeightMesurement <em>Height Mesurement</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getHeightValue <em>Height Value</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getWeightMesurement <em>Weight Mesurement</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getWeightValue <em>Weight Value</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getShot <em>Shot</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getBirthdate <em>Birthdate</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayer()
 * @model abstract="true"
 * @generated
 */
public interface Player extends HockeyleagueObject {
	/**
	 * Returns the value of the '<em><b>Birthplace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Birthplace</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Birthplace</em>' attribute.
	 * @see #setBirthplace(String)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayer_Birthplace()
	 * @model
	 * @generated
	 */
	String getBirthplace();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getBirthplace <em>Birthplace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Birthplace</em>' attribute.
	 * @see #getBirthplace()
	 * @generated
	 */
	void setBirthplace(String value);

	/**
	 * Returns the value of the '<em><b>Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Number</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Number</em>' attribute.
	 * @see #setNumber(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayer_Number()
	 * @model
	 * @generated
	 */
	int getNumber();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getNumber <em>Number</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Number</em>' attribute.
	 * @see #getNumber()
	 * @generated
	 */
	void setNumber(int value);

	/**
	 * Returns the value of the '<em><b>Height Mesurement</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HeightKind}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Height Mesurement</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Height Mesurement</em>' attribute.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HeightKind
	 * @see #setHeightMesurement(HeightKind)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayer_HeightMesurement()
	 * @model
	 * @generated
	 */
	HeightKind getHeightMesurement();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getHeightMesurement <em>Height Mesurement</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Height Mesurement</em>' attribute.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HeightKind
	 * @see #getHeightMesurement()
	 * @generated
	 */
	void setHeightMesurement(HeightKind value);

	/**
	 * Returns the value of the '<em><b>Height Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Height Value</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Height Value</em>' attribute.
	 * @see #setHeightValue(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayer_HeightValue()
	 * @model
	 * @generated
	 */
	int getHeightValue();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getHeightValue <em>Height Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Height Value</em>' attribute.
	 * @see #getHeightValue()
	 * @generated
	 */
	void setHeightValue(int value);

	/**
	 * Returns the value of the '<em><b>Weight Mesurement</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.WeightKind}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Weight Mesurement</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Weight Mesurement</em>' attribute.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.WeightKind
	 * @see #setWeightMesurement(WeightKind)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayer_WeightMesurement()
	 * @model
	 * @generated
	 */
	WeightKind getWeightMesurement();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getWeightMesurement <em>Weight Mesurement</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Weight Mesurement</em>' attribute.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.WeightKind
	 * @see #getWeightMesurement()
	 * @generated
	 */
	void setWeightMesurement(WeightKind value);

	/**
	 * Returns the value of the '<em><b>Weight Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Weight Value</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Weight Value</em>' attribute.
	 * @see #setWeightValue(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayer_WeightValue()
	 * @model
	 * @generated
	 */
	int getWeightValue();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getWeightValue <em>Weight Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Weight Value</em>' attribute.
	 * @see #getWeightValue()
	 * @generated
	 */
	void setWeightValue(int value);

	/**
	 * Returns the value of the '<em><b>Shot</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ShotKind}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Shot</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Shot</em>' attribute.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ShotKind
	 * @see #setShot(ShotKind)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayer_Shot()
	 * @model
	 * @generated
	 */
	ShotKind getShot();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getShot <em>Shot</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Shot</em>' attribute.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ShotKind
	 * @see #getShot()
	 * @generated
	 */
	void setShot(ShotKind value);

	/**
	 * Returns the value of the '<em><b>Birthdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Birthdate</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Birthdate</em>' attribute.
	 * @see #setBirthdate(String)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayer_Birthdate()
	 * @model
	 * @generated
	 */
	String getBirthdate();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getBirthdate <em>Birthdate</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Birthdate</em>' attribute.
	 * @see #getBirthdate()
	 * @generated
	 */
	void setBirthdate(String value);

} // Player
