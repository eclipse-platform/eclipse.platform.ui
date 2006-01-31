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
 * A representation of the model object '<em><b>Defence</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence#getPosition <em>Position</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence#getPlayerStats <em>Player Stats</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getDefence()
 * @model
 * @generated
 */
public interface Defence extends Player {
	/**
	 * Returns the value of the '<em><b>Position</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.DefencePositionKind}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Position</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Position</em>' attribute.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.DefencePositionKind
	 * @see #setPosition(DefencePositionKind)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getDefence_Position()
	 * @model
	 * @generated
	 */
	DefencePositionKind getPosition();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence#getPosition <em>Position</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Position</em>' attribute.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.DefencePositionKind
	 * @see #getPosition()
	 * @generated
	 */
	void setPosition(DefencePositionKind value);

	/**
	 * Returns the value of the '<em><b>Player Stats</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Player Stats</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Player Stats</em>' containment reference list.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getDefence_PlayerStats()
	 * @model type="org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats" containment="true"
	 * @generated
	 */
	EList getPlayerStats();

} // Defence
