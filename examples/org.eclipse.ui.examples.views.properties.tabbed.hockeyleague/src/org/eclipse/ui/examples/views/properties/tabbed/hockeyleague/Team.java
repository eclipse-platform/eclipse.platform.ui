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

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Team</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team#getForwards <em>Forwards</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team#getDefencemen <em>Defencemen</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team#getGoalies <em>Goalies</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team#getArena <em>Arena</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getTeam()
 * @model
 * @generated
 */
public interface Team extends HockeyleagueObject {
	/**
	 * Returns the value of the '<em><b>Forwards</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Forward}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Forwards</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Forwards</em>' containment reference list.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getTeam_Forwards()
	 * @model type="org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Forward" containment="true"
	 * @generated
	 */
	EList getForwards();

	/**
	 * Returns the value of the '<em><b>Defencemen</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Defencemen</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Defencemen</em>' containment reference list.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getTeam_Defencemen()
	 * @model type="org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence" containment="true"
	 * @generated
	 */
	EList getDefencemen();

	/**
	 * Returns the value of the '<em><b>Goalies</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Goalie}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Goalies</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Goalies</em>' containment reference list.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getTeam_Goalies()
	 * @model type="org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Goalie" containment="true"
	 * @generated
	 */
	EList getGoalies();

	/**
	 * Returns the value of the '<em><b>Arena</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Arena</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Arena</em>' containment reference.
	 * @see #setArena(Arena)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getTeam_Arena()
	 * @model containment="true"
	 * @generated
	 */
	Arena getArena();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team#getArena <em>Arena</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Arena</em>' containment reference.
	 * @see #getArena()
	 * @generated
	 */
	void setArena(Arena value);

} // Team
