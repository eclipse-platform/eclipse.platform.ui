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
 * A representation of the model object '<em><b>Goalie Stats</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getYear <em>Year</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getTeam <em>Team</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGamesPlayedIn <em>Games Played In</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getMinutesPlayedIn <em>Minutes Played In</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGoalsAgainstAverage <em>Goals Against Average</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getWins <em>Wins</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getLosses <em>Losses</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getTies <em>Ties</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getEmptyNetGoals <em>Empty Net Goals</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getShutouts <em>Shutouts</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGoalsAgainst <em>Goals Against</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getSaves <em>Saves</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getPenaltyMinutes <em>Penalty Minutes</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGoals <em>Goals</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getAssists <em>Assists</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getPoints <em>Points</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats()
 * @model
 * @generated
 */
public interface GoalieStats extends EObject {
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
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_Year()
	 * @model
	 * @generated
	 */
	String getYear();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getYear <em>Year</em>}' attribute.
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
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_Team()
	 * @model
	 * @generated
	 */
	Team getTeam();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getTeam <em>Team</em>}' reference.
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
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_GamesPlayedIn()
	 * @model
	 * @generated
	 */
	int getGamesPlayedIn();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGamesPlayedIn <em>Games Played In</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Games Played In</em>' attribute.
	 * @see #getGamesPlayedIn()
	 * @generated
	 */
	void setGamesPlayedIn(int value);

	/**
	 * Returns the value of the '<em><b>Minutes Played In</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Minutes Played In</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Minutes Played In</em>' attribute.
	 * @see #setMinutesPlayedIn(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_MinutesPlayedIn()
	 * @model
	 * @generated
	 */
	int getMinutesPlayedIn();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getMinutesPlayedIn <em>Minutes Played In</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Minutes Played In</em>' attribute.
	 * @see #getMinutesPlayedIn()
	 * @generated
	 */
	void setMinutesPlayedIn(int value);

	/**
	 * Returns the value of the '<em><b>Goals Against Average</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Goals Against Average</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Goals Against Average</em>' attribute.
	 * @see #setGoalsAgainstAverage(float)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_GoalsAgainstAverage()
	 * @model
	 * @generated
	 */
	float getGoalsAgainstAverage();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGoalsAgainstAverage <em>Goals Against Average</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Goals Against Average</em>' attribute.
	 * @see #getGoalsAgainstAverage()
	 * @generated
	 */
	void setGoalsAgainstAverage(float value);

	/**
	 * Returns the value of the '<em><b>Wins</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Wins</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Wins</em>' attribute.
	 * @see #setWins(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_Wins()
	 * @model
	 * @generated
	 */
	int getWins();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getWins <em>Wins</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Wins</em>' attribute.
	 * @see #getWins()
	 * @generated
	 */
	void setWins(int value);

	/**
	 * Returns the value of the '<em><b>Losses</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Losses</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Losses</em>' attribute.
	 * @see #setLosses(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_Losses()
	 * @model
	 * @generated
	 */
	int getLosses();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getLosses <em>Losses</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Losses</em>' attribute.
	 * @see #getLosses()
	 * @generated
	 */
	void setLosses(int value);

	/**
	 * Returns the value of the '<em><b>Ties</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ties</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ties</em>' attribute.
	 * @see #setTies(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_Ties()
	 * @model
	 * @generated
	 */
	int getTies();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getTies <em>Ties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ties</em>' attribute.
	 * @see #getTies()
	 * @generated
	 */
	void setTies(int value);

	/**
	 * Returns the value of the '<em><b>Empty Net Goals</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Empty Net Goals</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Empty Net Goals</em>' attribute.
	 * @see #setEmptyNetGoals(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_EmptyNetGoals()
	 * @model
	 * @generated
	 */
	int getEmptyNetGoals();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getEmptyNetGoals <em>Empty Net Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Empty Net Goals</em>' attribute.
	 * @see #getEmptyNetGoals()
	 * @generated
	 */
	void setEmptyNetGoals(int value);

	/**
	 * Returns the value of the '<em><b>Shutouts</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Shutouts</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Shutouts</em>' attribute.
	 * @see #setShutouts(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_Shutouts()
	 * @model
	 * @generated
	 */
	int getShutouts();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getShutouts <em>Shutouts</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Shutouts</em>' attribute.
	 * @see #getShutouts()
	 * @generated
	 */
	void setShutouts(int value);

	/**
	 * Returns the value of the '<em><b>Goals Against</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Goals Against</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Goals Against</em>' attribute.
	 * @see #setGoalsAgainst(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_GoalsAgainst()
	 * @model
	 * @generated
	 */
	int getGoalsAgainst();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGoalsAgainst <em>Goals Against</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Goals Against</em>' attribute.
	 * @see #getGoalsAgainst()
	 * @generated
	 */
	void setGoalsAgainst(int value);

	/**
	 * Returns the value of the '<em><b>Saves</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Saves</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Saves</em>' attribute.
	 * @see #setSaves(int)
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_Saves()
	 * @model
	 * @generated
	 */
	int getSaves();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getSaves <em>Saves</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Saves</em>' attribute.
	 * @see #getSaves()
	 * @generated
	 */
	void setSaves(int value);

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
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_PenaltyMinutes()
	 * @model
	 * @generated
	 */
	int getPenaltyMinutes();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getPenaltyMinutes <em>Penalty Minutes</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Penalty Minutes</em>' attribute.
	 * @see #getPenaltyMinutes()
	 * @generated
	 */
	void setPenaltyMinutes(int value);

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
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_Goals()
	 * @model
	 * @generated
	 */
	int getGoals();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGoals <em>Goals</em>}' attribute.
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
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_Assists()
	 * @model
	 * @generated
	 */
	int getAssists();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getAssists <em>Assists</em>}' attribute.
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
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getGoalieStats_Points()
	 * @model
	 * @generated
	 */
	int getPoints();

	/**
	 * Sets the value of the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getPoints <em>Points</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Points</em>' attribute.
	 * @see #getPoints()
	 * @generated
	 */
	void setPoints(int value);

} // GoalieStats
