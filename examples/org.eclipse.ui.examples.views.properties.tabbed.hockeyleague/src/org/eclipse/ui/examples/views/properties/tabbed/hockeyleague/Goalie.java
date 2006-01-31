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
 * A representation of the model object '<em><b>Goalie</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Goalie#getGoalieStats <em>Goalie Stats</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalie()
 * @model
 * @generated
 */
public interface Goalie extends Player {
	/**
	 * Returns the value of the '<em><b>Goalie Stats</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Goalie Stats</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Goalie Stats</em>' containment reference list.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalie_GoalieStats()
	 * @model type="org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats" containment="true"
	 * @generated
	 */
	EList getGoalieStats();

} // Goalie
