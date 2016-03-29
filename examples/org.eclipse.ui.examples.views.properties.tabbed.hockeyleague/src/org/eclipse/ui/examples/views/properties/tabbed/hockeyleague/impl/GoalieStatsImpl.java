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
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Goalie Stats</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getYear <em>Year</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getTeam <em>Team</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getGamesPlayedIn <em>Games Played In</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getMinutesPlayedIn <em>Minutes Played In</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getGoalsAgainstAverage <em>Goals Against Average</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getWins <em>Wins</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getLosses <em>Losses</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getTies <em>Ties</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getEmptyNetGoals <em>Empty Net Goals</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getShutouts <em>Shutouts</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getGoalsAgainst <em>Goals Against</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getSaves <em>Saves</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getPenaltyMinutes <em>Penalty Minutes</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getGoals <em>Goals</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getAssists <em>Assists</em>}</li>
 *   <li>{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl#getPoints <em>Points</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class GoalieStatsImpl extends EObjectImpl implements GoalieStats {
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
	 * The default value of the '{@link #getMinutesPlayedIn() <em>Minutes Played In</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMinutesPlayedIn()
	 * @generated
	 * @ordered
	 */
	protected static final int MINUTES_PLAYED_IN_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getMinutesPlayedIn() <em>Minutes Played In</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMinutesPlayedIn()
	 * @generated
	 * @ordered
	 */
	protected int minutesPlayedIn = MINUTES_PLAYED_IN_EDEFAULT;

	/**
	 * The default value of the '{@link #getGoalsAgainstAverage() <em>Goals Against Average</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGoalsAgainstAverage()
	 * @generated
	 * @ordered
	 */
	protected static final float GOALS_AGAINST_AVERAGE_EDEFAULT = 0.0F;

	/**
	 * The cached value of the '{@link #getGoalsAgainstAverage() <em>Goals Against Average</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGoalsAgainstAverage()
	 * @generated
	 * @ordered
	 */
	protected float goalsAgainstAverage = GOALS_AGAINST_AVERAGE_EDEFAULT;

	/**
	 * The default value of the '{@link #getWins() <em>Wins</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWins()
	 * @generated
	 * @ordered
	 */
	protected static final int WINS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getWins() <em>Wins</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWins()
	 * @generated
	 * @ordered
	 */
	protected int wins = WINS_EDEFAULT;

	/**
	 * The default value of the '{@link #getLosses() <em>Losses</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLosses()
	 * @generated
	 * @ordered
	 */
	protected static final int LOSSES_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getLosses() <em>Losses</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLosses()
	 * @generated
	 * @ordered
	 */
	protected int losses = LOSSES_EDEFAULT;

	/**
	 * The default value of the '{@link #getTies() <em>Ties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTies()
	 * @generated
	 * @ordered
	 */
	protected static final int TIES_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getTies() <em>Ties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTies()
	 * @generated
	 * @ordered
	 */
	protected int ties = TIES_EDEFAULT;

	/**
	 * The default value of the '{@link #getEmptyNetGoals() <em>Empty Net Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEmptyNetGoals()
	 * @generated
	 * @ordered
	 */
	protected static final int EMPTY_NET_GOALS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getEmptyNetGoals() <em>Empty Net Goals</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEmptyNetGoals()
	 * @generated
	 * @ordered
	 */
	protected int emptyNetGoals = EMPTY_NET_GOALS_EDEFAULT;

	/**
	 * The default value of the '{@link #getShutouts() <em>Shutouts</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShutouts()
	 * @generated
	 * @ordered
	 */
	protected static final int SHUTOUTS_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getShutouts() <em>Shutouts</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getShutouts()
	 * @generated
	 * @ordered
	 */
	protected int shutouts = SHUTOUTS_EDEFAULT;

	/**
	 * The default value of the '{@link #getGoalsAgainst() <em>Goals Against</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGoalsAgainst()
	 * @generated
	 * @ordered
	 */
	protected static final int GOALS_AGAINST_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getGoalsAgainst() <em>Goals Against</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGoalsAgainst()
	 * @generated
	 * @ordered
	 */
	protected int goalsAgainst = GOALS_AGAINST_EDEFAULT;

	/**
	 * The default value of the '{@link #getSaves() <em>Saves</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSaves()
	 * @generated
	 * @ordered
	 */
	protected static final int SAVES_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getSaves() <em>Saves</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSaves()
	 * @generated
	 * @ordered
	 */
	protected int saves = SAVES_EDEFAULT;

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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GoalieStatsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EClass eStaticClass() {
		return HockeyleaguePackage.Literals.GOALIE_STATS;
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
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__YEAR, oldYear, year));
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
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, HockeyleaguePackage.GOALIE_STATS__TEAM, oldTeam, team));
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
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__TEAM, oldTeam, team));
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
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__GAMES_PLAYED_IN, oldGamesPlayedIn, gamesPlayedIn));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getMinutesPlayedIn() {
		return minutesPlayedIn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMinutesPlayedIn(int newMinutesPlayedIn) {
		int oldMinutesPlayedIn = minutesPlayedIn;
		minutesPlayedIn = newMinutesPlayedIn;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__MINUTES_PLAYED_IN, oldMinutesPlayedIn, minutesPlayedIn));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public float getGoalsAgainstAverage() {
		return goalsAgainstAverage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setGoalsAgainstAverage(float newGoalsAgainstAverage) {
		float oldGoalsAgainstAverage = goalsAgainstAverage;
		goalsAgainstAverage = newGoalsAgainstAverage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__GOALS_AGAINST_AVERAGE, oldGoalsAgainstAverage, goalsAgainstAverage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getWins() {
		return wins;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWins(int newWins) {
		int oldWins = wins;
		wins = newWins;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__WINS, oldWins, wins));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getLosses() {
		return losses;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLosses(int newLosses) {
		int oldLosses = losses;
		losses = newLosses;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__LOSSES, oldLosses, losses));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getTies() {
		return ties;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTies(int newTies) {
		int oldTies = ties;
		ties = newTies;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__TIES, oldTies, ties));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getEmptyNetGoals() {
		return emptyNetGoals;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setEmptyNetGoals(int newEmptyNetGoals) {
		int oldEmptyNetGoals = emptyNetGoals;
		emptyNetGoals = newEmptyNetGoals;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__EMPTY_NET_GOALS, oldEmptyNetGoals, emptyNetGoals));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getShutouts() {
		return shutouts;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setShutouts(int newShutouts) {
		int oldShutouts = shutouts;
		shutouts = newShutouts;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__SHUTOUTS, oldShutouts, shutouts));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getGoalsAgainst() {
		return goalsAgainst;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setGoalsAgainst(int newGoalsAgainst) {
		int oldGoalsAgainst = goalsAgainst;
		goalsAgainst = newGoalsAgainst;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__GOALS_AGAINST, oldGoalsAgainst, goalsAgainst));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getSaves() {
		return saves;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSaves(int newSaves) {
		int oldSaves = saves;
		saves = newSaves;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__SAVES, oldSaves, saves));
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
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__PENALTY_MINUTES, oldPenaltyMinutes, penaltyMinutes));
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
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__GOALS, oldGoals, goals));
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
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__ASSISTS, oldAssists, assists));
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
			eNotify(new ENotificationImpl(this, Notification.SET, HockeyleaguePackage.GOALIE_STATS__POINTS, oldPoints, points));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case HockeyleaguePackage.GOALIE_STATS__YEAR:
				return getYear();
			case HockeyleaguePackage.GOALIE_STATS__TEAM:
				if (resolve) return getTeam();
				return basicGetTeam();
			case HockeyleaguePackage.GOALIE_STATS__GAMES_PLAYED_IN:
				return Integer.valueOf(getGamesPlayedIn());
			case HockeyleaguePackage.GOALIE_STATS__MINUTES_PLAYED_IN:
				return Integer.valueOf(getMinutesPlayedIn());
			case HockeyleaguePackage.GOALIE_STATS__GOALS_AGAINST_AVERAGE:
				return new Float(getGoalsAgainstAverage());
			case HockeyleaguePackage.GOALIE_STATS__WINS:
				return Integer.valueOf(getWins());
			case HockeyleaguePackage.GOALIE_STATS__LOSSES:
				return Integer.valueOf(getLosses());
			case HockeyleaguePackage.GOALIE_STATS__TIES:
				return Integer.valueOf(getTies());
			case HockeyleaguePackage.GOALIE_STATS__EMPTY_NET_GOALS:
				return Integer.valueOf(getEmptyNetGoals());
			case HockeyleaguePackage.GOALIE_STATS__SHUTOUTS:
				return Integer.valueOf(getShutouts());
			case HockeyleaguePackage.GOALIE_STATS__GOALS_AGAINST:
				return Integer.valueOf(getGoalsAgainst());
			case HockeyleaguePackage.GOALIE_STATS__SAVES:
				return Integer.valueOf(getSaves());
			case HockeyleaguePackage.GOALIE_STATS__PENALTY_MINUTES:
				return Integer.valueOf(getPenaltyMinutes());
			case HockeyleaguePackage.GOALIE_STATS__GOALS:
				return Integer.valueOf(getGoals());
			case HockeyleaguePackage.GOALIE_STATS__ASSISTS:
				return Integer.valueOf(getAssists());
			case HockeyleaguePackage.GOALIE_STATS__POINTS:
				return Integer.valueOf(getPoints());
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
			case HockeyleaguePackage.GOALIE_STATS__YEAR:
				setYear((String)newValue);
				return;
			case HockeyleaguePackage.GOALIE_STATS__TEAM:
				setTeam((Team)newValue);
				return;
			case HockeyleaguePackage.GOALIE_STATS__GAMES_PLAYED_IN:
				setGamesPlayedIn(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.GOALIE_STATS__MINUTES_PLAYED_IN:
				setMinutesPlayedIn(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.GOALIE_STATS__GOALS_AGAINST_AVERAGE:
				setGoalsAgainstAverage(((Float)newValue).floatValue());
				return;
			case HockeyleaguePackage.GOALIE_STATS__WINS:
				setWins(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.GOALIE_STATS__LOSSES:
				setLosses(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.GOALIE_STATS__TIES:
				setTies(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.GOALIE_STATS__EMPTY_NET_GOALS:
				setEmptyNetGoals(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.GOALIE_STATS__SHUTOUTS:
				setShutouts(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.GOALIE_STATS__GOALS_AGAINST:
				setGoalsAgainst(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.GOALIE_STATS__SAVES:
				setSaves(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.GOALIE_STATS__PENALTY_MINUTES:
				setPenaltyMinutes(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.GOALIE_STATS__GOALS:
				setGoals(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.GOALIE_STATS__ASSISTS:
				setAssists(((Integer)newValue).intValue());
				return;
			case HockeyleaguePackage.GOALIE_STATS__POINTS:
				setPoints(((Integer)newValue).intValue());
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
			case HockeyleaguePackage.GOALIE_STATS__YEAR:
				setYear(YEAR_EDEFAULT);
				return;
			case HockeyleaguePackage.GOALIE_STATS__TEAM:
				setTeam((Team)null);
				return;
			case HockeyleaguePackage.GOALIE_STATS__GAMES_PLAYED_IN:
				setGamesPlayedIn(GAMES_PLAYED_IN_EDEFAULT);
				return;
			case HockeyleaguePackage.GOALIE_STATS__MINUTES_PLAYED_IN:
				setMinutesPlayedIn(MINUTES_PLAYED_IN_EDEFAULT);
				return;
			case HockeyleaguePackage.GOALIE_STATS__GOALS_AGAINST_AVERAGE:
				setGoalsAgainstAverage(GOALS_AGAINST_AVERAGE_EDEFAULT);
				return;
			case HockeyleaguePackage.GOALIE_STATS__WINS:
				setWins(WINS_EDEFAULT);
				return;
			case HockeyleaguePackage.GOALIE_STATS__LOSSES:
				setLosses(LOSSES_EDEFAULT);
				return;
			case HockeyleaguePackage.GOALIE_STATS__TIES:
				setTies(TIES_EDEFAULT);
				return;
			case HockeyleaguePackage.GOALIE_STATS__EMPTY_NET_GOALS:
				setEmptyNetGoals(EMPTY_NET_GOALS_EDEFAULT);
				return;
			case HockeyleaguePackage.GOALIE_STATS__SHUTOUTS:
				setShutouts(SHUTOUTS_EDEFAULT);
				return;
			case HockeyleaguePackage.GOALIE_STATS__GOALS_AGAINST:
				setGoalsAgainst(GOALS_AGAINST_EDEFAULT);
				return;
			case HockeyleaguePackage.GOALIE_STATS__SAVES:
				setSaves(SAVES_EDEFAULT);
				return;
			case HockeyleaguePackage.GOALIE_STATS__PENALTY_MINUTES:
				setPenaltyMinutes(PENALTY_MINUTES_EDEFAULT);
				return;
			case HockeyleaguePackage.GOALIE_STATS__GOALS:
				setGoals(GOALS_EDEFAULT);
				return;
			case HockeyleaguePackage.GOALIE_STATS__ASSISTS:
				setAssists(ASSISTS_EDEFAULT);
				return;
			case HockeyleaguePackage.GOALIE_STATS__POINTS:
				setPoints(POINTS_EDEFAULT);
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
			case HockeyleaguePackage.GOALIE_STATS__YEAR:
				return YEAR_EDEFAULT == null ? year != null : !YEAR_EDEFAULT.equals(year);
			case HockeyleaguePackage.GOALIE_STATS__TEAM:
				return team != null;
			case HockeyleaguePackage.GOALIE_STATS__GAMES_PLAYED_IN:
				return gamesPlayedIn != GAMES_PLAYED_IN_EDEFAULT;
			case HockeyleaguePackage.GOALIE_STATS__MINUTES_PLAYED_IN:
				return minutesPlayedIn != MINUTES_PLAYED_IN_EDEFAULT;
			case HockeyleaguePackage.GOALIE_STATS__GOALS_AGAINST_AVERAGE:
				return goalsAgainstAverage != GOALS_AGAINST_AVERAGE_EDEFAULT;
			case HockeyleaguePackage.GOALIE_STATS__WINS:
				return wins != WINS_EDEFAULT;
			case HockeyleaguePackage.GOALIE_STATS__LOSSES:
				return losses != LOSSES_EDEFAULT;
			case HockeyleaguePackage.GOALIE_STATS__TIES:
				return ties != TIES_EDEFAULT;
			case HockeyleaguePackage.GOALIE_STATS__EMPTY_NET_GOALS:
				return emptyNetGoals != EMPTY_NET_GOALS_EDEFAULT;
			case HockeyleaguePackage.GOALIE_STATS__SHUTOUTS:
				return shutouts != SHUTOUTS_EDEFAULT;
			case HockeyleaguePackage.GOALIE_STATS__GOALS_AGAINST:
				return goalsAgainst != GOALS_AGAINST_EDEFAULT;
			case HockeyleaguePackage.GOALIE_STATS__SAVES:
				return saves != SAVES_EDEFAULT;
			case HockeyleaguePackage.GOALIE_STATS__PENALTY_MINUTES:
				return penaltyMinutes != PENALTY_MINUTES_EDEFAULT;
			case HockeyleaguePackage.GOALIE_STATS__GOALS:
				return goals != GOALS_EDEFAULT;
			case HockeyleaguePackage.GOALIE_STATS__ASSISTS:
				return assists != ASSISTS_EDEFAULT;
			case HockeyleaguePackage.GOALIE_STATS__POINTS:
				return points != POINTS_EDEFAULT;
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
		result.append(", minutesPlayedIn: "); //$NON-NLS-1$
		result.append(minutesPlayedIn);
		result.append(", goalsAgainstAverage: "); //$NON-NLS-1$
		result.append(goalsAgainstAverage);
		result.append(", wins: "); //$NON-NLS-1$
		result.append(wins);
		result.append(", losses: "); //$NON-NLS-1$
		result.append(losses);
		result.append(", ties: "); //$NON-NLS-1$
		result.append(ties);
		result.append(", emptyNetGoals: "); //$NON-NLS-1$
		result.append(emptyNetGoals);
		result.append(", shutouts: "); //$NON-NLS-1$
		result.append(shutouts);
		result.append(", goalsAgainst: "); //$NON-NLS-1$
		result.append(goalsAgainst);
		result.append(", saves: "); //$NON-NLS-1$
		result.append(saves);
		result.append(", penaltyMinutes: "); //$NON-NLS-1$
		result.append(penaltyMinutes);
		result.append(", goals: "); //$NON-NLS-1$
		result.append(goals);
		result.append(", assists: "); //$NON-NLS-1$
		result.append(assists);
		result.append(", points: "); //$NON-NLS-1$
		result.append(points);
		result.append(')');
		return result.toString();
	}

} //GoalieStatsImpl
