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


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Arena;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.DefencePositionKind;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Forward;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ForwardPositionKind;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Goalie;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HeightKind;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleagueFactory;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleagueObject;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.League;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ShotKind;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.WeightKind;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class HockeyleaguePackageImpl extends EPackageImpl implements HockeyleaguePackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass arenaEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass defenceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass forwardEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass goalieEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass goalieStatsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass hockeyleagueObjectEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass leagueEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass playerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass playerStatsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass teamEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum defencePositionKindEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum forwardPositionKindEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum heightKindEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum shotKindEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum weightKindEEnum = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private HockeyleaguePackageImpl() {
		super(eNS_URI, HockeyleagueFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this
	 * model, and for any others upon which it depends.  Simple
	 * dependencies are satisfied by calling this method on all
	 * dependent packages before doing anything else.  This method drives
	 * initialization for interdependent packages directly, in parallel
	 * with this package, itself.
	 * <p>Of this package and its interdependencies, all packages which
	 * have not yet been registered by their URI values are first created
	 * and registered.  The packages are then initialized in two steps:
	 * meta-model objects for all of the packages are created before any
	 * are initialized, since one package's meta-model objects may refer to
	 * those of another.
	 * <p>Invocation of this method will not affect any packages that have
	 * already been initialized.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static HockeyleaguePackage init() {
		if (isInited) return (HockeyleaguePackage)EPackage.Registry.INSTANCE.getEPackage(HockeyleaguePackage.eNS_URI);

		// Obtain or create and register package
		HockeyleaguePackageImpl theHockeyleaguePackage = (HockeyleaguePackageImpl)(EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof HockeyleaguePackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new HockeyleaguePackageImpl());

		isInited = true;

		// Create package meta-data objects
		theHockeyleaguePackage.createPackageContents();

		// Initialize created meta-data
		theHockeyleaguePackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theHockeyleaguePackage.freeze();

		return theHockeyleaguePackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getArena() {
		return arenaEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getArena_Address() {
		return (EAttribute)arenaEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getArena_Capacity() {
		return (EAttribute)arenaEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getDefence() {
		return defenceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getDefence_Position() {
		return (EAttribute)defenceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getDefence_PlayerStats() {
		return (EReference)defenceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getForward() {
		return forwardEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getForward_Position() {
		return (EAttribute)forwardEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getForward_PlayerStats() {
		return (EReference)forwardEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getGoalie() {
		return goalieEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getGoalie_GoalieStats() {
		return (EReference)goalieEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getGoalieStats() {
		return goalieStatsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_Year() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getGoalieStats_Team() {
		return (EReference)goalieStatsEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_GamesPlayedIn() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_MinutesPlayedIn() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_GoalsAgainstAverage() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_Wins() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_Losses() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_Ties() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_EmptyNetGoals() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_Shutouts() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_GoalsAgainst() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_Saves() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_PenaltyMinutes() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_Goals() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_Assists() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getGoalieStats_Points() {
		return (EAttribute)goalieStatsEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getHockeyleagueObject() {
		return hockeyleagueObjectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getHockeyleagueObject_Name() {
		return (EAttribute)hockeyleagueObjectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getLeague() {
		return leagueEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getLeague_Headoffice() {
		return (EAttribute)leagueEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getLeague_Teams() {
		return (EReference)leagueEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPlayer() {
		return playerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayer_Birthplace() {
		return (EAttribute)playerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayer_Number() {
		return (EAttribute)playerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayer_HeightMesurement() {
		return (EAttribute)playerEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayer_HeightValue() {
		return (EAttribute)playerEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayer_WeightMesurement() {
		return (EAttribute)playerEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayer_WeightValue() {
		return (EAttribute)playerEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayer_Shot() {
		return (EAttribute)playerEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayer_Birthdate() {
		return (EAttribute)playerEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPlayerStats() {
		return playerStatsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayerStats_Year() {
		return (EAttribute)playerStatsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getPlayerStats_Team() {
		return (EReference)playerStatsEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayerStats_GamesPlayedIn() {
		return (EAttribute)playerStatsEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayerStats_Goals() {
		return (EAttribute)playerStatsEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayerStats_Assists() {
		return (EAttribute)playerStatsEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayerStats_Points() {
		return (EAttribute)playerStatsEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayerStats_PlusMinus() {
		return (EAttribute)playerStatsEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayerStats_PenaltyMinutes() {
		return (EAttribute)playerStatsEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayerStats_PowerPlayGoals() {
		return (EAttribute)playerStatsEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayerStats_ShortHandedGoals() {
		return (EAttribute)playerStatsEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayerStats_GameWinningGoals() {
		return (EAttribute)playerStatsEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayerStats_Shots() {
		return (EAttribute)playerStatsEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPlayerStats_ShotPercentage() {
		return (EAttribute)playerStatsEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTeam() {
		return teamEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTeam_Forwards() {
		return (EReference)teamEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTeam_Defencemen() {
		return (EReference)teamEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTeam_Goalies() {
		return (EReference)teamEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTeam_Arena() {
		return (EReference)teamEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getDefencePositionKind() {
		return defencePositionKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getForwardPositionKind() {
		return forwardPositionKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getHeightKind() {
		return heightKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getShotKind() {
		return shotKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getWeightKind() {
		return weightKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public HockeyleagueFactory getHockeyleagueFactory() {
		return (HockeyleagueFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		arenaEClass = createEClass(ARENA);
		createEAttribute(arenaEClass, ARENA__ADDRESS);
		createEAttribute(arenaEClass, ARENA__CAPACITY);

		defenceEClass = createEClass(DEFENCE);
		createEAttribute(defenceEClass, DEFENCE__POSITION);
		createEReference(defenceEClass, DEFENCE__PLAYER_STATS);

		forwardEClass = createEClass(FORWARD);
		createEAttribute(forwardEClass, FORWARD__POSITION);
		createEReference(forwardEClass, FORWARD__PLAYER_STATS);

		goalieEClass = createEClass(GOALIE);
		createEReference(goalieEClass, GOALIE__GOALIE_STATS);

		goalieStatsEClass = createEClass(GOALIE_STATS);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__YEAR);
		createEReference(goalieStatsEClass, GOALIE_STATS__TEAM);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__GAMES_PLAYED_IN);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__MINUTES_PLAYED_IN);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__GOALS_AGAINST_AVERAGE);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__WINS);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__LOSSES);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__TIES);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__EMPTY_NET_GOALS);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__SHUTOUTS);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__GOALS_AGAINST);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__SAVES);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__PENALTY_MINUTES);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__GOALS);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__ASSISTS);
		createEAttribute(goalieStatsEClass, GOALIE_STATS__POINTS);

		hockeyleagueObjectEClass = createEClass(HOCKEYLEAGUE_OBJECT);
		createEAttribute(hockeyleagueObjectEClass, HOCKEYLEAGUE_OBJECT__NAME);

		leagueEClass = createEClass(LEAGUE);
		createEAttribute(leagueEClass, LEAGUE__HEADOFFICE);
		createEReference(leagueEClass, LEAGUE__TEAMS);

		playerEClass = createEClass(PLAYER);
		createEAttribute(playerEClass, PLAYER__BIRTHPLACE);
		createEAttribute(playerEClass, PLAYER__NUMBER);
		createEAttribute(playerEClass, PLAYER__HEIGHT_MESUREMENT);
		createEAttribute(playerEClass, PLAYER__HEIGHT_VALUE);
		createEAttribute(playerEClass, PLAYER__WEIGHT_MESUREMENT);
		createEAttribute(playerEClass, PLAYER__WEIGHT_VALUE);
		createEAttribute(playerEClass, PLAYER__SHOT);
		createEAttribute(playerEClass, PLAYER__BIRTHDATE);

		playerStatsEClass = createEClass(PLAYER_STATS);
		createEAttribute(playerStatsEClass, PLAYER_STATS__YEAR);
		createEReference(playerStatsEClass, PLAYER_STATS__TEAM);
		createEAttribute(playerStatsEClass, PLAYER_STATS__GAMES_PLAYED_IN);
		createEAttribute(playerStatsEClass, PLAYER_STATS__GOALS);
		createEAttribute(playerStatsEClass, PLAYER_STATS__ASSISTS);
		createEAttribute(playerStatsEClass, PLAYER_STATS__POINTS);
		createEAttribute(playerStatsEClass, PLAYER_STATS__PLUS_MINUS);
		createEAttribute(playerStatsEClass, PLAYER_STATS__PENALTY_MINUTES);
		createEAttribute(playerStatsEClass, PLAYER_STATS__POWER_PLAY_GOALS);
		createEAttribute(playerStatsEClass, PLAYER_STATS__SHORT_HANDED_GOALS);
		createEAttribute(playerStatsEClass, PLAYER_STATS__GAME_WINNING_GOALS);
		createEAttribute(playerStatsEClass, PLAYER_STATS__SHOTS);
		createEAttribute(playerStatsEClass, PLAYER_STATS__SHOT_PERCENTAGE);

		teamEClass = createEClass(TEAM);
		createEReference(teamEClass, TEAM__FORWARDS);
		createEReference(teamEClass, TEAM__DEFENCEMEN);
		createEReference(teamEClass, TEAM__GOALIES);
		createEReference(teamEClass, TEAM__ARENA);

		// Create enums
		defencePositionKindEEnum = createEEnum(DEFENCE_POSITION_KIND);
		forwardPositionKindEEnum = createEEnum(FORWARD_POSITION_KIND);
		heightKindEEnum = createEEnum(HEIGHT_KIND);
		shotKindEEnum = createEEnum(SHOT_KIND);
		weightKindEEnum = createEEnum(WEIGHT_KIND);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Add supertypes to classes
		arenaEClass.getESuperTypes().add(this.getHockeyleagueObject());
		defenceEClass.getESuperTypes().add(this.getPlayer());
		forwardEClass.getESuperTypes().add(this.getPlayer());
		goalieEClass.getESuperTypes().add(this.getPlayer());
		leagueEClass.getESuperTypes().add(this.getHockeyleagueObject());
		playerEClass.getESuperTypes().add(this.getHockeyleagueObject());
		teamEClass.getESuperTypes().add(this.getHockeyleagueObject());

		// Initialize classes and features; add operations and parameters
		initEClass(arenaEClass, Arena.class, "Arena", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getArena_Address(), ecorePackage.getEString(), "address", null, 0, 1, Arena.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getArena_Capacity(), ecorePackage.getEInt(), "capacity", null, 0, 1, Arena.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(defenceEClass, Defence.class, "Defence", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getDefence_Position(), this.getDefencePositionKind(), "position", null, 0, 1, Defence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getDefence_PlayerStats(), this.getPlayerStats(), null, "playerStats", null, 0, -1, Defence.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(forwardEClass, Forward.class, "Forward", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getForward_Position(), this.getForwardPositionKind(), "position", null, 0, 1, Forward.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getForward_PlayerStats(), this.getPlayerStats(), null, "playerStats", null, 0, -1, Forward.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(goalieEClass, Goalie.class, "Goalie", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getGoalie_GoalieStats(), this.getGoalieStats(), null, "goalieStats", null, 0, -1, Goalie.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(goalieStatsEClass, GoalieStats.class, "GoalieStats", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getGoalieStats_Year(), ecorePackage.getEString(), "year", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getGoalieStats_Team(), this.getTeam(), null, "team", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGoalieStats_GamesPlayedIn(), ecorePackage.getEInt(), "gamesPlayedIn", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGoalieStats_MinutesPlayedIn(), ecorePackage.getEInt(), "minutesPlayedIn", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGoalieStats_GoalsAgainstAverage(), ecorePackage.getEFloat(), "goalsAgainstAverage", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGoalieStats_Wins(), ecorePackage.getEInt(), "wins", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGoalieStats_Losses(), ecorePackage.getEInt(), "losses", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGoalieStats_Ties(), ecorePackage.getEInt(), "ties", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGoalieStats_EmptyNetGoals(), ecorePackage.getEInt(), "emptyNetGoals", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGoalieStats_Shutouts(), ecorePackage.getEInt(), "shutouts", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGoalieStats_GoalsAgainst(), ecorePackage.getEInt(), "goalsAgainst", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGoalieStats_Saves(), ecorePackage.getEInt(), "saves", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGoalieStats_PenaltyMinutes(), ecorePackage.getEInt(), "penaltyMinutes", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGoalieStats_Goals(), ecorePackage.getEInt(), "goals", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGoalieStats_Assists(), ecorePackage.getEInt(), "assists", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getGoalieStats_Points(), ecorePackage.getEInt(), "points", null, 0, 1, GoalieStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(hockeyleagueObjectEClass, HockeyleagueObject.class, "HockeyleagueObject", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getHockeyleagueObject_Name(), ecorePackage.getEString(), "name", null, 0, 1, HockeyleagueObject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(leagueEClass, League.class, "League", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getLeague_Headoffice(), ecorePackage.getEString(), "headoffice", null, 0, 1, League.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getLeague_Teams(), this.getTeam(), null, "teams", null, 0, -1, League.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(playerEClass, Player.class, "Player", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getPlayer_Birthplace(), ecorePackage.getEString(), "birthplace", null, 0, 1, Player.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayer_Number(), ecorePackage.getEInt(), "number", null, 0, 1, Player.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayer_HeightMesurement(), this.getHeightKind(), "heightMesurement", null, 0, 1, Player.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayer_HeightValue(), ecorePackage.getEInt(), "heightValue", null, 0, 1, Player.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayer_WeightMesurement(), this.getWeightKind(), "weightMesurement", null, 0, 1, Player.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayer_WeightValue(), ecorePackage.getEInt(), "weightValue", null, 0, 1, Player.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayer_Shot(), this.getShotKind(), "shot", null, 0, 1, Player.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayer_Birthdate(), ecorePackage.getEString(), "birthdate", null, 0, 1, Player.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(playerStatsEClass, PlayerStats.class, "PlayerStats", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEAttribute(getPlayerStats_Year(), ecorePackage.getEString(), "year", null, 0, 1, PlayerStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getPlayerStats_Team(), this.getTeam(), null, "team", null, 0, 1, PlayerStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayerStats_GamesPlayedIn(), ecorePackage.getEInt(), "gamesPlayedIn", null, 0, 1, PlayerStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayerStats_Goals(), ecorePackage.getEInt(), "goals", null, 0, 1, PlayerStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayerStats_Assists(), ecorePackage.getEInt(), "assists", null, 0, 1, PlayerStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayerStats_Points(), ecorePackage.getEInt(), "points", null, 0, 1, PlayerStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayerStats_PlusMinus(), ecorePackage.getEInt(), "plusMinus", null, 0, 1, PlayerStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayerStats_PenaltyMinutes(), ecorePackage.getEInt(), "penaltyMinutes", null, 0, 1, PlayerStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayerStats_PowerPlayGoals(), ecorePackage.getEInt(), "powerPlayGoals", null, 0, 1, PlayerStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayerStats_ShortHandedGoals(), ecorePackage.getEInt(), "shortHandedGoals", null, 0, 1, PlayerStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayerStats_GameWinningGoals(), ecorePackage.getEInt(), "gameWinningGoals", null, 0, 1, PlayerStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayerStats_Shots(), ecorePackage.getEInt(), "shots", null, 0, 1, PlayerStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEAttribute(getPlayerStats_ShotPercentage(), ecorePackage.getEFloat(), "shotPercentage", null, 0, 1, PlayerStats.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		initEClass(teamEClass, Team.class, "Team", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS); //$NON-NLS-1$
		initEReference(getTeam_Forwards(), this.getForward(), null, "forwards", null, 0, -1, Team.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getTeam_Defencemen(), this.getDefence(), null, "defencemen", null, 0, -1, Team.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getTeam_Goalies(), this.getGoalie(), null, "goalies", null, 0, -1, Team.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$
		initEReference(getTeam_Arena(), this.getArena(), null, "arena", null, 0, 1, Team.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED); //$NON-NLS-1$

		// Initialize enums and add enum literals
		initEEnum(defencePositionKindEEnum, DefencePositionKind.class, "DefencePositionKind"); //$NON-NLS-1$
		addEEnumLiteral(defencePositionKindEEnum, DefencePositionKind.LEFT_DEFENCE_LITERAL);
		addEEnumLiteral(defencePositionKindEEnum, DefencePositionKind.RIGHT_DEFENCE_LITERAL);

		initEEnum(forwardPositionKindEEnum, ForwardPositionKind.class, "ForwardPositionKind"); //$NON-NLS-1$
		addEEnumLiteral(forwardPositionKindEEnum, ForwardPositionKind.LEFT_WING_LITERAL);
		addEEnumLiteral(forwardPositionKindEEnum, ForwardPositionKind.RIGHT_WING_LITERAL);
		addEEnumLiteral(forwardPositionKindEEnum, ForwardPositionKind.CENTER_LITERAL);

		initEEnum(heightKindEEnum, HeightKind.class, "HeightKind"); //$NON-NLS-1$
		addEEnumLiteral(heightKindEEnum, HeightKind.INCHES_LITERAL);
		addEEnumLiteral(heightKindEEnum, HeightKind.CENTIMETERS_LITERAL);

		initEEnum(shotKindEEnum, ShotKind.class, "ShotKind"); //$NON-NLS-1$
		addEEnumLiteral(shotKindEEnum, ShotKind.LEFT_LITERAL);
		addEEnumLiteral(shotKindEEnum, ShotKind.RIGHT_LITERAL);

		initEEnum(weightKindEEnum, WeightKind.class, "WeightKind"); //$NON-NLS-1$
		addEEnumLiteral(weightKindEEnum, WeightKind.POUNDS_LITERAL);
		addEEnumLiteral(weightKindEEnum, WeightKind.KILOGRAMS_LITERAL);

		// Create resource
		createResource(eNS_URI);
	}

} //HockeyleaguePackageImpl
