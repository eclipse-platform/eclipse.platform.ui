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

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Player Stats</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getYear <em>Year</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getTeam <em>Team</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getGamesPlayedIn <em>Games Played In</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getGoals <em>Goals</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getAssists <em>Assists</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPoints <em>Points</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPlusMinus <em>Plus Minus</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPenaltyMinutes <em>Penalty Minutes</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPowerPlayGoals <em>Power Play Goals</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getShortHandedGoals <em>Short Handed Goals</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getGameWinningGoals <em>Game Winning Goals</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getShots <em>Shots</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getShotPercentage <em>Shot Percentage</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayerStats()
 * @model
 * @generated
 */
public interface PlayerStats extends EObject {
	/**
	 * Returns the value of the '<em><b>Year</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Year</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Year</em>' attribute.
	 * @see #setYear(String)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayerStats_Year()
	 * @model
	 * @generated
	 */
	String getYear();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getYear <em>Year</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Year</em>' attribute.
	 * @see #getYear()
	 * @generated
	 */
	void setYear(String value);

	/**
	 * Returns the value of the '<em><b>Team</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Team</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Team</em>' reference.
	 * @see #setTeam(Team)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayerStats_Team()
	 * @model
	 * @generated
	 */
	Team getTeam();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getTeam <em>Team</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Team</em>' reference.
	 * @see #getTeam()
	 * @generated
	 */
	void setTeam(Team value);

	/**
	 * Returns the value of the '<em><b>Games Played In</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Games Played In</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Games Played In</em>' attribute.
	 * @see #setGamesPlayedIn(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayerStats_GamesPlayedIn()
	 * @model
	 * @generated
	 */
	int getGamesPlayedIn();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getGamesPlayedIn <em>Games Played In</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Games Played In</em>' attribute.
	 * @see #getGamesPlayedIn()
	 * @generated
	 */
	void setGamesPlayedIn(int value);

	/**
	 * Returns the value of the '<em><b>Goals</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Goals</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Goals</em>' attribute.
	 * @see #setGoals(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayerStats_Goals()
	 * @model
	 * @generated
	 */
	int getGoals();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getGoals <em>Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Goals</em>' attribute.
	 * @see #getGoals()
	 * @generated
	 */
	void setGoals(int value);

	/**
	 * Returns the value of the '<em><b>Assists</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Assists</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Assists</em>' attribute.
	 * @see #setAssists(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayerStats_Assists()
	 * @model
	 * @generated
	 */
	int getAssists();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getAssists <em>Assists</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Assists</em>' attribute.
	 * @see #getAssists()
	 * @generated
	 */
	void setAssists(int value);

	/**
	 * Returns the value of the '<em><b>Points</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Points</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Points</em>' attribute.
	 * @see #setPoints(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayerStats_Points()
	 * @model
	 * @generated
	 */
	int getPoints();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPoints <em>Points</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Points</em>' attribute.
	 * @see #getPoints()
	 * @generated
	 */
	void setPoints(int value);

	/**
	 * Returns the value of the '<em><b>Plus Minus</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Plus Minus</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Plus Minus</em>' attribute.
	 * @see #setPlusMinus(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayerStats_PlusMinus()
	 * @model
	 * @generated
	 */
	int getPlusMinus();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPlusMinus <em>Plus Minus</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Plus Minus</em>' attribute.
	 * @see #getPlusMinus()
	 * @generated
	 */
	void setPlusMinus(int value);

	/**
	 * Returns the value of the '<em><b>Penalty Minutes</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Penalty Minutes</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Penalty Minutes</em>' attribute.
	 * @see #setPenaltyMinutes(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayerStats_PenaltyMinutes()
	 * @model
	 * @generated
	 */
	int getPenaltyMinutes();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPenaltyMinutes <em>Penalty Minutes</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Penalty Minutes</em>' attribute.
	 * @see #getPenaltyMinutes()
	 * @generated
	 */
	void setPenaltyMinutes(int value);

	/**
	 * Returns the value of the '<em><b>Power Play Goals</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Power Play Goals</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Power Play Goals</em>' attribute.
	 * @see #setPowerPlayGoals(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayerStats_PowerPlayGoals()
	 * @model
	 * @generated
	 */
	int getPowerPlayGoals();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPowerPlayGoals <em>Power Play Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Power Play Goals</em>' attribute.
	 * @see #getPowerPlayGoals()
	 * @generated
	 */
	void setPowerPlayGoals(int value);

	/**
	 * Returns the value of the '<em><b>Short Handed Goals</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Short Handed Goals</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Short Handed Goals</em>' attribute.
	 * @see #setShortHandedGoals(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayerStats_ShortHandedGoals()
	 * @model
	 * @generated
	 */
	int getShortHandedGoals();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getShortHandedGoals <em>Short Handed Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Short Handed Goals</em>' attribute.
	 * @see #getShortHandedGoals()
	 * @generated
	 */
	void setShortHandedGoals(int value);

	/**
	 * Returns the value of the '<em><b>Game Winning Goals</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Game Winning Goals</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Game Winning Goals</em>' attribute.
	 * @see #setGameWinningGoals(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayerStats_GameWinningGoals()
	 * @model
	 * @generated
	 */
	int getGameWinningGoals();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getGameWinningGoals <em>Game Winning Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Game Winning Goals</em>' attribute.
	 * @see #getGameWinningGoals()
	 * @generated
	 */
	void setGameWinningGoals(int value);

	/**
	 * Returns the value of the '<em><b>Shots</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Shots</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Shots</em>' attribute.
	 * @see #setShots(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayerStats_Shots()
	 * @model
	 * @generated
	 */
	int getShots();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getShots <em>Shots</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Shots</em>' attribute.
	 * @see #getShots()
	 * @generated
	 */
	void setShots(int value);

	/**
	 * Returns the value of the '<em><b>Shot Percentage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Shot Percentage</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Shot Percentage</em>' attribute.
	 * @see #setShotPercentage(float)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getPlayerStats_ShotPercentage()
	 * @model
	 * @generated
	 */
	float getShotPercentage();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getShotPercentage <em>Shot Percentage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Shot Percentage</em>' attribute.
	 * @see #getShotPercentage()
	 * @generated
	 */
	void setShotPercentage(float value);

} // PlayerStats
