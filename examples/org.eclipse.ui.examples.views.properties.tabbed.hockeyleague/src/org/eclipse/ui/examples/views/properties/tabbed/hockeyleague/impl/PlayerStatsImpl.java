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


import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Player Stats</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl#getYear <em>Year</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl#getTeam <em>Team</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl#getGamesPlayedIn <em>Games Played In</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl#getGoals <em>Goals</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl#getAssists <em>Assists</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl#getPoints <em>Points</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl#getPlusMinus <em>Plus Minus</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl#getPenaltyMinutes <em>Penalty Minutes</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl#getPowerPlayGoals <em>Power Play Goals</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl#getShortHandedGoals <em>Short Handed Goals</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl#getGameWinningGoals <em>Game Winning Goals</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl#getShots <em>Shots</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl#getShotPercentage <em>Shot Percentage</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PlayerStatsImpl extends EObjectImpl implements PlayerStats {
	/**
	 * The default value of the '{@link #getYear() <em>Year</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getYear()
	 * @generated
	 * @ordered
	 */
	protected static final String YEAR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getYear() <em>Year</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getYear()
	 * @generated
	 * @ordered
	 */
	protected String year = YEAR_EDEFAULT;

	/**
	 * The cached value of the '{@link #getTeam() <em>Team</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTeam()
	 * @generated
	 * @ordered
	 */
	protected Team team;

	/**
	 * The default value of the '{@link #getGamesPlayedIn() <em>Games Played In</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGamesPlayedIn()
	 * @generated
	 * @ordered
	 */
	protected static final int GAMES_PLAYED_IN_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getGamesPlayedIn() <em>Games Played In</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGamesPlayedIn()
	 * @generated
	 * @ordered
	 */
	protected int gamesPlayedIn = GAMES_PLAYED_IN_EDEFAULT;

	/**
	 * The default value of the '{@link #getGoals() <em>Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGoals()
	 * @generated
	 * @ordered
	 */
	protected static final int GOALS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getGoals() <em>Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGoals()
	 * @generated
	 * @ordered
	 */
	protected int goals = GOALS_EDEFAULT;

	/**
	 * The default value of the '{@link #getAssists() <em>Assists</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAssists()
	 * @generated
	 * @ordered
	 */
	protected static final int ASSISTS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getAssists() <em>Assists</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAssists()
	 * @generated
	 * @ordered
	 */
	protected int assists = ASSISTS_EDEFAULT;

	/**
	 * The default value of the '{@link #getPoints() <em>Points</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPoints()
	 * @generated
	 * @ordered
	 */
	protected static final int POINTS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getPoints() <em>Points</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPoints()
	 * @generated
	 * @ordered
	 */
	protected int points = POINTS_EDEFAULT;

	/**
	 * The default value of the '{@link #getPlusMinus() <em>Plus Minus</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPlusMinus()
	 * @generated
	 * @ordered
	 */
	protected static final int PLUS_MINUS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getPlusMinus() <em>Plus Minus</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPlusMinus()
	 * @generated
	 * @ordered
	 */
	protected int plusMinus = PLUS_MINUS_EDEFAULT;

	/**
	 * The default value of the '{@link #getPenaltyMinutes() <em>Penalty Minutes</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPenaltyMinutes()
	 * @generated
	 * @ordered
	 */
	protected static final int PENALTY_MINUTES_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getPenaltyMinutes() <em>Penalty Minutes</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPenaltyMinutes()
	 * @generated
	 * @ordered
	 */
	protected int penaltyMinutes = PENALTY_MINUTES_EDEFAULT;

	/**
	 * The default value of the '{@link #getPowerPlayGoals() <em>Power Play Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPowerPlayGoals()
	 * @generated
	 * @ordered
	 */
	protected static final int POWER_PLAY_GOALS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getPowerPlayGoals() <em>Power Play Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPowerPlayGoals()
	 * @generated
	 * @ordered
	 */
	protected int powerPlayGoals = POWER_PLAY_GOALS_EDEFAULT;

	/**
	 * The default value of the '{@link #getShortHandedGoals() <em>Short Handed Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShortHandedGoals()
	 * @generated
	 * @ordered
	 */
	protected static final int SHORT_HANDED_GOALS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getShortHandedGoals() <em>Short Handed Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShortHandedGoals()
	 * @generated
	 * @ordered
	 */
	protected int shortHandedGoals = SHORT_HANDED_GOALS_EDEFAULT;

	/**
	 * The default value of the '{@link #getGameWinningGoals() <em>Game Winning Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGameWinningGoals()
	 * @generated
	 * @ordered
	 */
	protected static final int GAME_WINNING_GOALS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getGameWinningGoals() <em>Game Winning Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGameWinningGoals()
	 * @generated
	 * @ordered
	 */
	protected int gameWinningGoals = GAME_WINNING_GOALS_EDEFAULT;

	/**
	 * The default value of the '{@link #getShots() <em>Shots</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShots()
	 * @generated
	 * @ordered
	 */
	protected static final int SHOTS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getShots() <em>Shots</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShots()
	 * @generated
	 * @ordered
	 */
	protected int shots = SHOTS_EDEFAULT;

	/**
	 * The default value of the '{@link #getShotPercentage() <em>Shot Percentage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShotPercentage()
	 * @generated
	 * @ordered
	 */
	protected static final float SHOT_PERCENTAGE_EDEFAULT = 0.0F;

	/**
	 * The cached value of the '{@link #getShotPercentage() <em>Shot Percentage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShotPercentage()
	 * @generated
	 * @ordered
	 */
	protected float shotPercentage = SHOT_PERCENTAGE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PlayerStatsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EClass eStaticClass() {
		return HockeyleaguePackage.Literals.PLAYER_STATS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getYear() {
		return year;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setYear(String newYear) {
		String oldYear = year;
		year = newYear;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER_STATS__YEAR, oldYear, year));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Team getTeam() {
		if (team != null && team.eIsProxy()) {
			InternalEObject oldTeam = (InternalEObject)team;
			team = (Team)eResolveProxy(oldTeam);
			if (team != oldTeam) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, HockeyleaguePackage.PLAYER_STATS__TEAM, oldTeam, team));
			}
		}
		return team;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Team basicGetTeam() {
		return team;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTeam(Team newTeam) {
		Team oldTeam = team;
		team = newTeam;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER_STATS__TEAM, oldTeam, team));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getGamesPlayedIn() {
		return gamesPlayedIn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setGamesPlayedIn(int newGamesPlayedIn) {
		int oldGamesPlayedIn = gamesPlayedIn;
		gamesPlayedIn = newGamesPlayedIn;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER_STATS__GAMES_PLAYED_IN, oldGamesPlayedIn, gamesPlayedIn));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getGoals() {
		return goals;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setGoals(int newGoals) {
		int oldGoals = goals;
		goals = newGoals;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER_STATS__GOALS, oldGoals, goals));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getAssists() {
		return assists;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAssists(int newAssists) {
		int oldAssists = assists;
		assists = newAssists;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER_STATS__ASSISTS, oldAssists, assists));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPoints(int newPoints) {
		int oldPoints = points;
		points = newPoints;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER_STATS__POINTS, oldPoints, points));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getPlusMinus() {
		return plusMinus;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPlusMinus(int newPlusMinus) {
		int oldPlusMinus = plusMinus;
		plusMinus = newPlusMinus;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER_STATS__PLUS_MINUS, oldPlusMinus, plusMinus));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getPenaltyMinutes() {
		return penaltyMinutes;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPenaltyMinutes(int newPenaltyMinutes) {
		int oldPenaltyMinutes = penaltyMinutes;
		penaltyMinutes = newPenaltyMinutes;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER_STATS__PENALTY_MINUTES, oldPenaltyMinutes, penaltyMinutes));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getPowerPlayGoals() {
		return powerPlayGoals;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPowerPlayGoals(int newPowerPlayGoals) {
		int oldPowerPlayGoals = powerPlayGoals;
		powerPlayGoals = newPowerPlayGoals;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER_STATS__POWER_PLAY_GOALS, oldPowerPlayGoals, powerPlayGoals));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getShortHandedGoals() {
		return shortHandedGoals;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setShortHandedGoals(int newShortHandedGoals) {
		int oldShortHandedGoals = shortHandedGoals;
		shortHandedGoals = newShortHandedGoals;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER_STATS__SHORT_HANDED_GOALS, oldShortHandedGoals, shortHandedGoals));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getGameWinningGoals() {
		return gameWinningGoals;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setGameWinningGoals(int newGameWinningGoals) {
		int oldGameWinningGoals = gameWinningGoals;
		gameWinningGoals = newGameWinningGoals;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER_STATS__GAME_WINNING_GOALS, oldGameWinningGoals, gameWinningGoals));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getShots() {
		return shots;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setShots(int newShots) {
		int oldShots = shots;
		shots = newShots;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER_STATS__SHOTS, oldShots, shots));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public float getShotPercentage() {
		return shotPercentage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setShotPercentage(float newShotPercentage) {
		float oldShotPercentage = shotPercentage;
		shotPercentage = newShotPercentage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.PLAYER_STATS__SHOT_PERCENTAGE, oldShotPercentage, shotPercentage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case HockeyleaguePackage.PLAYER_STATS__YEAR:
				return getYear();
			case HockeyleaguePackage.PLAYER_STATS__TEAM:
				if (resolve) return getTeam();
				return basicGetTeam();
			case HockeyleaguePackage.PLAYER_STATS__GAMES_PLAYED_IN:
				return new Integer(getGamesPlayedIn());
			case HockeyleaguePackage.PLAYER_STATS__GOALS:
				return new Integer(getGoals());
			case HockeyleaguePackage.PLAYER_STATS__ASSISTS:
				return new Integer(getAssists());
			case HockeyleaguePackage.PLAYER_STATS__POINTS:
				return new Integer(getPoints());
			case HockeyleaguePackage.PLAYER_STATS__PLUS_MINUS:
				return new Integer(getPlusMinus());
			case HockeyleaguePackage.PLAYER_STATS__PENALTY_MINUTES:
				return new Integer(getPenaltyMinutes());
			case HockeyleaguePackage.PLAYER_STATS__POWER_PLAY_GOALS:
				return new Integer(getPowerPlayGoals());
			case HockeyleaguePackage.PLAYER_STATS__SHORT_HANDED_GOALS:
				return new Integer(getShortHandedGoals());
			case HockeyleaguePackage.PLAYER_STATS__GAME_WINNING_GOALS:
				return new Integer(getGameWinningGoals());
			case HockeyleaguePackage.PLAYER_STATS__SHOTS:
				return new Integer(getShots());
			case HockeyleaguePackage.PLAYER_STATS__SHOT_PERCENTAGE:
				return new Float(getShotPercentage());
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
			case HockeyleaguePackage.PLAYER_STATS__YEAR:
				setYear((String)newValue);
				return;
			case HockeyleaguePackage.PLAYER_STATS__TEAM:
				setTeam((Team)newValue);
				return;
			case HockeyleaguePackage.PLAYER_STATS__GAMES_PLAYED_IN:
				setGamesPlayedIn(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.PLAYER_STATS__GOALS:
				setGoals(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.PLAYER_STATS__ASSISTS:
				setAssists(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.PLAYER_STATS__POINTS:
				setPoints(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.PLAYER_STATS__PLUS_MINUS:
				setPlusMinus(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.PLAYER_STATS__PENALTY_MINUTES:
				setPenaltyMinutes(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.PLAYER_STATS__POWER_PLAY_GOALS:
				setPowerPlayGoals(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.PLAYER_STATS__SHORT_HANDED_GOALS:
				setShortHandedGoals(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.PLAYER_STATS__GAME_WINNING_GOALS:
				setGameWinningGoals(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.PLAYER_STATS__SHOTS:
				setShots(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.PLAYER_STATS__SHOT_PERCENTAGE:
				setShotPercentage(((Float)newValue).floatValue());
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
			case HockeyleaguePackage.PLAYER_STATS__YEAR:
				setYear(YEAR_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER_STATS__TEAM:
				setTeam((Team)null);
				return;
			case HockeyleaguePackage.PLAYER_STATS__GAMES_PLAYED_IN:
				setGamesPlayedIn(GAMES_PLAYED_IN_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER_STATS__GOALS:
				setGoals(GOALS_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER_STATS__ASSISTS:
				setAssists(ASSISTS_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER_STATS__POINTS:
				setPoints(POINTS_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER_STATS__PLUS_MINUS:
				setPlusMinus(PLUS_MINUS_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER_STATS__PENALTY_MINUTES:
				setPenaltyMinutes(PENALTY_MINUTES_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER_STATS__POWER_PLAY_GOALS:
				setPowerPlayGoals(POWER_PLAY_GOALS_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER_STATS__SHORT_HANDED_GOALS:
				setShortHandedGoals(SHORT_HANDED_GOALS_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER_STATS__GAME_WINNING_GOALS:
				setGameWinningGoals(GAME_WINNING_GOALS_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER_STATS__SHOTS:
				setShots(SHOTS_EDEFAULT);
				return;
			case HockeyleaguePackage.PLAYER_STATS__SHOT_PERCENTAGE:
				setShotPercentage(SHOT_PERCENTAGE_EDEFAULT);
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
			case HockeyleaguePackage.PLAYER_STATS__YEAR:
				return YEAR_EDEFAULT == null ? year != null : !YEAR_EDEFAULT.equals(year);
			case HockeyleaguePackage.PLAYER_STATS__TEAM:
				return team != null;
			case HockeyleaguePackage.PLAYER_STATS__GAMES_PLAYED_IN:
				return gamesPlayedIn != GAMES_PLAYED_IN_EDEFAULT;
			case HockeyleaguePackage.PLAYER_STATS__GOALS:
				return goals != GOALS_EDEFAULT;
			case HockeyleaguePackage.PLAYER_STATS__ASSISTS:
				return assists != ASSISTS_EDEFAULT;
			case HockeyleaguePackage.PLAYER_STATS__POINTS:
				return points != POINTS_EDEFAULT;
			case HockeyleaguePackage.PLAYER_STATS__PLUS_MINUS:
				return plusMinus != PLUS_MINUS_EDEFAULT;
			case HockeyleaguePackage.PLAYER_STATS__PENALTY_MINUTES:
				return penaltyMinutes != PENALTY_MINUTES_EDEFAULT;
			case HockeyleaguePackage.PLAYER_STATS__POWER_PLAY_GOALS:
				return powerPlayGoals != POWER_PLAY_GOALS_EDEFAULT;
			case HockeyleaguePackage.PLAYER_STATS__SHORT_HANDED_GOALS:
				return shortHandedGoals != SHORT_HANDED_GOALS_EDEFAULT;
			case HockeyleaguePackage.PLAYER_STATS__GAME_WINNING_GOALS:
				return gameWinningGoals != GAME_WINNING_GOALS_EDEFAULT;
			case HockeyleaguePackage.PLAYER_STATS__SHOTS:
				return shots != SHOTS_EDEFAULT;
			case HockeyleaguePackage.PLAYER_STATS__SHOT_PERCENTAGE:
				return shotPercentage != SHOT_PERCENTAGE_EDEFAULT;
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
		result.append(" (year: "); //$NON-NLS-1$
		result.append(year);
		result.append(", gamesPlayedIn: "); //$NON-NLS-1$
		result.append(gamesPlayedIn);
		result.append(", goals: "); //$NON-NLS-1$
		result.append(goals);
		result.append(", assists: "); //$NON-NLS-1$
		result.append(assists);
		result.append(", points: "); //$NON-NLS-1$
		result.append(points);
		result.append(", plusMinus: "); //$NON-NLS-1$
		result.append(plusMinus);
		result.append(", penaltyMinutes: "); //$NON-NLS-1$
		result.append(penaltyMinutes);
		result.append(", powerPlayGoals: "); //$NON-NLS-1$
		result.append(powerPlayGoals);
		result.append(", shortHandedGoals: "); //$NON-NLS-1$
		result.append(shortHandedGoals);
		result.append(", gameWinningGoals: "); //$NON-NLS-1$
		result.append(gameWinningGoals);
		result.append(", shots: "); //$NON-NLS-1$
		result.append(shots);
		result.append(", shotPercentage: "); //$NON-NLS-1$
		result.append(shotPercentage);
		result.append(')');
		return result.toString();
	}

} //PlayerStatsImpl
