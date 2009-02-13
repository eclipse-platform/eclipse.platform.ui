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
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleagueFactory
 * @model kind="package"
 * @generated
 */
public interface HockeyleaguePackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "hockeyleague"; //$NON-NLS-1$

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http:///org/eclipse/ui/views/properties/tabbed/examples/org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ecore"; //$NON-NLS-1$

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "org.eclipse.ui.examples.views.properties.tabbed.hockeyleague"; //$NON-NLS-1$

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	HockeyleaguePackage eINSTANCE = org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleagueObjectImpl <em>Object</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleagueObjectImpl
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getHockeyleagueObject()
	 * @generated
	 */
	int HOCKEYLEAGUE_OBJECT = 5;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HOCKEYLEAGUE_OBJECT__NAME = 0;

	/**
	 * The number of structural features of the '<em>Object</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HOCKEYLEAGUE_OBJECT_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.ArenaImpl <em>Arena</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.ArenaImpl
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getArena()
	 * @generated
	 */
	int ARENA = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARENA__NAME = HOCKEYLEAGUE_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Address</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARENA__ADDRESS = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Capacity</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARENA__CAPACITY = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Arena</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ARENA_FEATURE_COUNT = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerImpl <em>Player</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerImpl
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getPlayer()
	 * @generated
	 */
	int PLAYER = 7;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER__NAME = HOCKEYLEAGUE_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Birthplace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER__BIRTHPLACE = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER__NUMBER = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Height Mesurement</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER__HEIGHT_MESUREMENT = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Height Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER__HEIGHT_VALUE = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Weight Mesurement</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER__WEIGHT_MESUREMENT = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Weight Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER__WEIGHT_VALUE = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Shot</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER__SHOT = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Birthdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER__BIRTHDATE = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 7;

	/**
	 * The number of structural features of the '<em>Player</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_FEATURE_COUNT = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 8;

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.DefenceImpl <em>Defence</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.DefenceImpl
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getDefence()
	 * @generated
	 */
	int DEFENCE = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFENCE__NAME = PLAYER__NAME;

	/**
	 * The feature id for the '<em><b>Birthplace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFENCE__BIRTHPLACE = PLAYER__BIRTHPLACE;

	/**
	 * The feature id for the '<em><b>Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFENCE__NUMBER = PLAYER__NUMBER;

	/**
	 * The feature id for the '<em><b>Height Mesurement</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFENCE__HEIGHT_MESUREMENT = PLAYER__HEIGHT_MESUREMENT;

	/**
	 * The feature id for the '<em><b>Height Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFENCE__HEIGHT_VALUE = PLAYER__HEIGHT_VALUE;

	/**
	 * The feature id for the '<em><b>Weight Mesurement</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFENCE__WEIGHT_MESUREMENT = PLAYER__WEIGHT_MESUREMENT;

	/**
	 * The feature id for the '<em><b>Weight Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFENCE__WEIGHT_VALUE = PLAYER__WEIGHT_VALUE;

	/**
	 * The feature id for the '<em><b>Shot</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFENCE__SHOT = PLAYER__SHOT;

	/**
	 * The feature id for the '<em><b>Birthdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFENCE__BIRTHDATE = PLAYER__BIRTHDATE;

	/**
	 * The feature id for the '<em><b>Position</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFENCE__POSITION = PLAYER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Player Stats</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFENCE__PLAYER_STATS = PLAYER_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Defence</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DEFENCE_FEATURE_COUNT = PLAYER_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.ForwardImpl <em>Forward</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.ForwardImpl
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getForward()
	 * @generated
	 */
	int FORWARD = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORWARD__NAME = PLAYER__NAME;

	/**
	 * The feature id for the '<em><b>Birthplace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORWARD__BIRTHPLACE = PLAYER__BIRTHPLACE;

	/**
	 * The feature id for the '<em><b>Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORWARD__NUMBER = PLAYER__NUMBER;

	/**
	 * The feature id for the '<em><b>Height Mesurement</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORWARD__HEIGHT_MESUREMENT = PLAYER__HEIGHT_MESUREMENT;

	/**
	 * The feature id for the '<em><b>Height Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORWARD__HEIGHT_VALUE = PLAYER__HEIGHT_VALUE;

	/**
	 * The feature id for the '<em><b>Weight Mesurement</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORWARD__WEIGHT_MESUREMENT = PLAYER__WEIGHT_MESUREMENT;

	/**
	 * The feature id for the '<em><b>Weight Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORWARD__WEIGHT_VALUE = PLAYER__WEIGHT_VALUE;

	/**
	 * The feature id for the '<em><b>Shot</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORWARD__SHOT = PLAYER__SHOT;

	/**
	 * The feature id for the '<em><b>Birthdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORWARD__BIRTHDATE = PLAYER__BIRTHDATE;

	/**
	 * The feature id for the '<em><b>Position</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORWARD__POSITION = PLAYER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Player Stats</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORWARD__PLAYER_STATS = PLAYER_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Forward</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORWARD_FEATURE_COUNT = PLAYER_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieImpl <em>Goalie</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieImpl
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getGoalie()
	 * @generated
	 */
	int GOALIE = 3;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE__NAME = PLAYER__NAME;

	/**
	 * The feature id for the '<em><b>Birthplace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE__BIRTHPLACE = PLAYER__BIRTHPLACE;

	/**
	 * The feature id for the '<em><b>Number</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE__NUMBER = PLAYER__NUMBER;

	/**
	 * The feature id for the '<em><b>Height Mesurement</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE__HEIGHT_MESUREMENT = PLAYER__HEIGHT_MESUREMENT;

	/**
	 * The feature id for the '<em><b>Height Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE__HEIGHT_VALUE = PLAYER__HEIGHT_VALUE;

	/**
	 * The feature id for the '<em><b>Weight Mesurement</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE__WEIGHT_MESUREMENT = PLAYER__WEIGHT_MESUREMENT;

	/**
	 * The feature id for the '<em><b>Weight Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE__WEIGHT_VALUE = PLAYER__WEIGHT_VALUE;

	/**
	 * The feature id for the '<em><b>Shot</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE__SHOT = PLAYER__SHOT;

	/**
	 * The feature id for the '<em><b>Birthdate</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE__BIRTHDATE = PLAYER__BIRTHDATE;

	/**
	 * The feature id for the '<em><b>Goalie Stats</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE__GOALIE_STATS = PLAYER_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Goalie</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_FEATURE_COUNT = PLAYER_FEATURE_COUNT + 1;

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl <em>Goalie Stats</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getGoalieStats()
	 * @generated
	 */
	int GOALIE_STATS = 4;

	/**
	 * The feature id for the '<em><b>Year</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__YEAR = 0;

	/**
	 * The feature id for the '<em><b>Team</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__TEAM = 1;

	/**
	 * The feature id for the '<em><b>Games Played In</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__GAMES_PLAYED_IN = 2;

	/**
	 * The feature id for the '<em><b>Minutes Played In</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__MINUTES_PLAYED_IN = 3;

	/**
	 * The feature id for the '<em><b>Goals Against Average</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__GOALS_AGAINST_AVERAGE = 4;

	/**
	 * The feature id for the '<em><b>Wins</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__WINS = 5;

	/**
	 * The feature id for the '<em><b>Losses</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__LOSSES = 6;

	/**
	 * The feature id for the '<em><b>Ties</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__TIES = 7;

	/**
	 * The feature id for the '<em><b>Empty Net Goals</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__EMPTY_NET_GOALS = 8;

	/**
	 * The feature id for the '<em><b>Shutouts</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__SHUTOUTS = 9;

	/**
	 * The feature id for the '<em><b>Goals Against</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__GOALS_AGAINST = 10;

	/**
	 * The feature id for the '<em><b>Saves</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__SAVES = 11;

	/**
	 * The feature id for the '<em><b>Penalty Minutes</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__PENALTY_MINUTES = 12;

	/**
	 * The feature id for the '<em><b>Goals</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__GOALS = 13;

	/**
	 * The feature id for the '<em><b>Assists</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__ASSISTS = 14;

	/**
	 * The feature id for the '<em><b>Points</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS__POINTS = 15;

	/**
	 * The number of structural features of the '<em>Goalie Stats</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOALIE_STATS_FEATURE_COUNT = 16;

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.LeagueImpl <em>League</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.LeagueImpl
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getLeague()
	 * @generated
	 */
	int LEAGUE = 6;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEAGUE__NAME = HOCKEYLEAGUE_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Headoffice</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEAGUE__HEADOFFICE = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Teams</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEAGUE__TEAMS = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>League</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int LEAGUE_FEATURE_COUNT = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl <em>Player Stats</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getPlayerStats()
	 * @generated
	 */
	int PLAYER_STATS = 8;

	/**
	 * The feature id for the '<em><b>Year</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_STATS__YEAR = 0;

	/**
	 * The feature id for the '<em><b>Team</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_STATS__TEAM = 1;

	/**
	 * The feature id for the '<em><b>Games Played In</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_STATS__GAMES_PLAYED_IN = 2;

	/**
	 * The feature id for the '<em><b>Goals</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_STATS__GOALS = 3;

	/**
	 * The feature id for the '<em><b>Assists</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_STATS__ASSISTS = 4;

	/**
	 * The feature id for the '<em><b>Points</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_STATS__POINTS = 5;

	/**
	 * The feature id for the '<em><b>Plus Minus</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_STATS__PLUS_MINUS = 6;

	/**
	 * The feature id for the '<em><b>Penalty Minutes</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_STATS__PENALTY_MINUTES = 7;

	/**
	 * The feature id for the '<em><b>Power Play Goals</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_STATS__POWER_PLAY_GOALS = 8;

	/**
	 * The feature id for the '<em><b>Short Handed Goals</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_STATS__SHORT_HANDED_GOALS = 9;

	/**
	 * The feature id for the '<em><b>Game Winning Goals</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_STATS__GAME_WINNING_GOALS = 10;

	/**
	 * The feature id for the '<em><b>Shots</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_STATS__SHOTS = 11;

	/**
	 * The feature id for the '<em><b>Shot Percentage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_STATS__SHOT_PERCENTAGE = 12;

	/**
	 * The number of structural features of the '<em>Player Stats</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PLAYER_STATS_FEATURE_COUNT = 13;

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.TeamImpl <em>Team</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.TeamImpl
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getTeam()
	 * @generated
	 */
	int TEAM = 9;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEAM__NAME = HOCKEYLEAGUE_OBJECT__NAME;

	/**
	 * The feature id for the '<em><b>Forwards</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEAM__FORWARDS = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Defencemen</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEAM__DEFENCEMEN = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Goalies</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEAM__GOALIES = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Arena</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEAM__ARENA = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Team</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TEAM_FEATURE_COUNT = HOCKEYLEAGUE_OBJECT_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.DefencePositionKind <em>Defence Position Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.DefencePositionKind
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getDefencePositionKind()
	 * @generated
	 */
	int DEFENCE_POSITION_KIND = 10;

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ForwardPositionKind <em>Forward Position Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ForwardPositionKind
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getForwardPositionKind()
	 * @generated
	 */
	int FORWARD_POSITION_KIND = 11;

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HeightKind <em>Height Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HeightKind
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getHeightKind()
	 * @generated
	 */
	int HEIGHT_KIND = 12;

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ShotKind <em>Shot Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ShotKind
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getShotKind()
	 * @generated
	 */
	int SHOT_KIND = 13;

	/**
	 * The meta object id for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.WeightKind <em>Weight Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.WeightKind
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getWeightKind()
	 * @generated
	 */
	int WEIGHT_KIND = 14;


	/**
	 * Returns the meta object for class '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Arena <em>Arena</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Arena</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Arena
	 * @generated
	 */
	EClass getArena();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Arena#getAddress <em>Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Address</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Arena#getAddress()
	 * @see #getArena()
	 * @generated
	 */
	EAttribute getArena_Address();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Arena#getCapacity <em>Capacity</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Capacity</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Arena#getCapacity()
	 * @see #getArena()
	 * @generated
	 */
	EAttribute getArena_Capacity();

	/**
	 * Returns the meta object for class '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence <em>Defence</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Defence</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence
	 * @generated
	 */
	EClass getDefence();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence#getPosition <em>Position</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Position</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence#getPosition()
	 * @see #getDefence()
	 * @generated
	 */
	EAttribute getDefence_Position();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence#getPlayerStats <em>Player Stats</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Player Stats</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence#getPlayerStats()
	 * @see #getDefence()
	 * @generated
	 */
	EReference getDefence_PlayerStats();

	/**
	 * Returns the meta object for class '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Forward <em>Forward</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Forward</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Forward
	 * @generated
	 */
	EClass getForward();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Forward#getPosition <em>Position</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Position</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Forward#getPosition()
	 * @see #getForward()
	 * @generated
	 */
	EAttribute getForward_Position();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Forward#getPlayerStats <em>Player Stats</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Player Stats</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Forward#getPlayerStats()
	 * @see #getForward()
	 * @generated
	 */
	EReference getForward_PlayerStats();

	/**
	 * Returns the meta object for class '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Goalie <em>Goalie</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Goalie</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Goalie
	 * @generated
	 */
	EClass getGoalie();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Goalie#getGoalieStats <em>Goalie Stats</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Goalie Stats</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Goalie#getGoalieStats()
	 * @see #getGoalie()
	 * @generated
	 */
	EReference getGoalie_GoalieStats();

	/**
	 * Returns the meta object for class '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats <em>Goalie Stats</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Goalie Stats</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats
	 * @generated
	 */
	EClass getGoalieStats();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getYear <em>Year</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Year</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getYear()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_Year();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getTeam <em>Team</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Team</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getTeam()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EReference getGoalieStats_Team();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGamesPlayedIn <em>Games Played In</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Games Played In</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGamesPlayedIn()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_GamesPlayedIn();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getMinutesPlayedIn <em>Minutes Played In</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Minutes Played In</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getMinutesPlayedIn()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_MinutesPlayedIn();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGoalsAgainstAverage <em>Goals Against Average</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Goals Against Average</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGoalsAgainstAverage()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_GoalsAgainstAverage();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getWins <em>Wins</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Wins</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getWins()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_Wins();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getLosses <em>Losses</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Losses</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getLosses()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_Losses();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getTies <em>Ties</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ties</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getTies()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_Ties();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getEmptyNetGoals <em>Empty Net Goals</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Empty Net Goals</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getEmptyNetGoals()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_EmptyNetGoals();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getShutouts <em>Shutouts</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Shutouts</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getShutouts()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_Shutouts();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGoalsAgainst <em>Goals Against</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Goals Against</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGoalsAgainst()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_GoalsAgainst();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getSaves <em>Saves</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Saves</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getSaves()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_Saves();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getPenaltyMinutes <em>Penalty Minutes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Penalty Minutes</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getPenaltyMinutes()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_PenaltyMinutes();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGoals <em>Goals</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Goals</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getGoals()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_Goals();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getAssists <em>Assists</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Assists</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getAssists()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_Assists();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getPoints <em>Points</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Points</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.GoalieStats#getPoints()
	 * @see #getGoalieStats()
	 * @generated
	 */
	EAttribute getGoalieStats_Points();

	/**
	 * Returns the meta object for class '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleagueObject <em>Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Object</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleagueObject
	 * @generated
	 */
	EClass getHockeyleagueObject();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleagueObject#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleagueObject#getName()
	 * @see #getHockeyleagueObject()
	 * @generated
	 */
	EAttribute getHockeyleagueObject_Name();

	/**
	 * Returns the meta object for class '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.League <em>League</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>League</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.League
	 * @generated
	 */
	EClass getLeague();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.League#getHeadoffice <em>Headoffice</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Headoffice</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.League#getHeadoffice()
	 * @see #getLeague()
	 * @generated
	 */
	EAttribute getLeague_Headoffice();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.League#getTeams <em>Teams</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Teams</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.League#getTeams()
	 * @see #getLeague()
	 * @generated
	 */
	EReference getLeague_Teams();

	/**
	 * Returns the meta object for class '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player <em>Player</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Player</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player
	 * @generated
	 */
	EClass getPlayer();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getBirthplace <em>Birthplace</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Birthplace</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getBirthplace()
	 * @see #getPlayer()
	 * @generated
	 */
	EAttribute getPlayer_Birthplace();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getNumber <em>Number</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Number</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getNumber()
	 * @see #getPlayer()
	 * @generated
	 */
	EAttribute getPlayer_Number();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getHeightMesurement <em>Height Mesurement</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Height Mesurement</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getHeightMesurement()
	 * @see #getPlayer()
	 * @generated
	 */
	EAttribute getPlayer_HeightMesurement();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getHeightValue <em>Height Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Height Value</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getHeightValue()
	 * @see #getPlayer()
	 * @generated
	 */
	EAttribute getPlayer_HeightValue();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getWeightMesurement <em>Weight Mesurement</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Weight Mesurement</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getWeightMesurement()
	 * @see #getPlayer()
	 * @generated
	 */
	EAttribute getPlayer_WeightMesurement();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getWeightValue <em>Weight Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Weight Value</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getWeightValue()
	 * @see #getPlayer()
	 * @generated
	 */
	EAttribute getPlayer_WeightValue();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getShot <em>Shot</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Shot</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getShot()
	 * @see #getPlayer()
	 * @generated
	 */
	EAttribute getPlayer_Shot();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getBirthdate <em>Birthdate</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Birthdate</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player#getBirthdate()
	 * @see #getPlayer()
	 * @generated
	 */
	EAttribute getPlayer_Birthdate();

	/**
	 * Returns the meta object for class '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats <em>Player Stats</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Player Stats</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats
	 * @generated
	 */
	EClass getPlayerStats();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getYear <em>Year</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Year</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getYear()
	 * @see #getPlayerStats()
	 * @generated
	 */
	EAttribute getPlayerStats_Year();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getTeam <em>Team</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Team</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getTeam()
	 * @see #getPlayerStats()
	 * @generated
	 */
	EReference getPlayerStats_Team();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getGamesPlayedIn <em>Games Played In</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Games Played In</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getGamesPlayedIn()
	 * @see #getPlayerStats()
	 * @generated
	 */
	EAttribute getPlayerStats_GamesPlayedIn();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getGoals <em>Goals</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Goals</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getGoals()
	 * @see #getPlayerStats()
	 * @generated
	 */
	EAttribute getPlayerStats_Goals();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getAssists <em>Assists</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Assists</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getAssists()
	 * @see #getPlayerStats()
	 * @generated
	 */
	EAttribute getPlayerStats_Assists();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPoints <em>Points</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Points</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPoints()
	 * @see #getPlayerStats()
	 * @generated
	 */
	EAttribute getPlayerStats_Points();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPlusMinus <em>Plus Minus</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Plus Minus</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPlusMinus()
	 * @see #getPlayerStats()
	 * @generated
	 */
	EAttribute getPlayerStats_PlusMinus();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPenaltyMinutes <em>Penalty Minutes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Penalty Minutes</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPenaltyMinutes()
	 * @see #getPlayerStats()
	 * @generated
	 */
	EAttribute getPlayerStats_PenaltyMinutes();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPowerPlayGoals <em>Power Play Goals</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Power Play Goals</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getPowerPlayGoals()
	 * @see #getPlayerStats()
	 * @generated
	 */
	EAttribute getPlayerStats_PowerPlayGoals();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getShortHandedGoals <em>Short Handed Goals</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Short Handed Goals</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getShortHandedGoals()
	 * @see #getPlayerStats()
	 * @generated
	 */
	EAttribute getPlayerStats_ShortHandedGoals();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getGameWinningGoals <em>Game Winning Goals</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Game Winning Goals</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getGameWinningGoals()
	 * @see #getPlayerStats()
	 * @generated
	 */
	EAttribute getPlayerStats_GameWinningGoals();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getShots <em>Shots</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Shots</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getShots()
	 * @see #getPlayerStats()
	 * @generated
	 */
	EAttribute getPlayerStats_Shots();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getShotPercentage <em>Shot Percentage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Shot Percentage</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.PlayerStats#getShotPercentage()
	 * @see #getPlayerStats()
	 * @generated
	 */
	EAttribute getPlayerStats_ShotPercentage();

	/**
	 * Returns the meta object for class '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team <em>Team</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Team</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team
	 * @generated
	 */
	EClass getTeam();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team#getForwards <em>Forwards</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Forwards</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team#getForwards()
	 * @see #getTeam()
	 * @generated
	 */
	EReference getTeam_Forwards();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team#getDefencemen <em>Defencemen</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Defencemen</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team#getDefencemen()
	 * @see #getTeam()
	 * @generated
	 */
	EReference getTeam_Defencemen();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team#getGoalies <em>Goalies</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Goalies</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team#getGoalies()
	 * @see #getTeam()
	 * @generated
	 */
	EReference getTeam_Goalies();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team#getArena <em>Arena</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Arena</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team#getArena()
	 * @see #getTeam()
	 * @generated
	 */
	EReference getTeam_Arena();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.DefencePositionKind <em>Defence Position Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Defence Position Kind</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.DefencePositionKind
	 * @generated
	 */
	EEnum getDefencePositionKind();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ForwardPositionKind <em>Forward Position Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Forward Position Kind</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ForwardPositionKind
	 * @generated
	 */
	EEnum getForwardPositionKind();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HeightKind <em>Height Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Height Kind</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HeightKind
	 * @generated
	 */
	EEnum getHeightKind();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ShotKind <em>Shot Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Shot Kind</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ShotKind
	 * @generated
	 */
	EEnum getShotKind();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.WeightKind <em>Weight Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Weight Kind</em>'.
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.WeightKind
	 * @generated
	 */
	EEnum getWeightKind();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	HockeyleagueFactory getHockeyleagueFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.ArenaImpl <em>Arena</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.ArenaImpl
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getArena()
		 * @generated
		 */
		EClass ARENA = eINSTANCE.getArena();

		/**
		 * The meta object literal for the '<em><b>Address</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARENA__ADDRESS = eINSTANCE.getArena_Address();

		/**
		 * The meta object literal for the '<em><b>Capacity</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ARENA__CAPACITY = eINSTANCE.getArena_Capacity();

		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.DefenceImpl <em>Defence</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.DefenceImpl
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getDefence()
		 * @generated
		 */
		EClass DEFENCE = eINSTANCE.getDefence();

		/**
		 * The meta object literal for the '<em><b>Position</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DEFENCE__POSITION = eINSTANCE.getDefence_Position();

		/**
		 * The meta object literal for the '<em><b>Player Stats</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DEFENCE__PLAYER_STATS = eINSTANCE.getDefence_PlayerStats();

		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.ForwardImpl <em>Forward</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.ForwardImpl
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getForward()
		 * @generated
		 */
		EClass FORWARD = eINSTANCE.getForward();

		/**
		 * The meta object literal for the '<em><b>Position</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FORWARD__POSITION = eINSTANCE.getForward_Position();

		/**
		 * The meta object literal for the '<em><b>Player Stats</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FORWARD__PLAYER_STATS = eINSTANCE.getForward_PlayerStats();

		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieImpl <em>Goalie</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieImpl
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getGoalie()
		 * @generated
		 */
		EClass GOALIE = eINSTANCE.getGoalie();

		/**
		 * The meta object literal for the '<em><b>Goalie Stats</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GOALIE__GOALIE_STATS = eINSTANCE.getGoalie_GoalieStats();

		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl <em>Goalie Stats</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.GoalieStatsImpl
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getGoalieStats()
		 * @generated
		 */
		EClass GOALIE_STATS = eINSTANCE.getGoalieStats();

		/**
		 * The meta object literal for the '<em><b>Year</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__YEAR = eINSTANCE.getGoalieStats_Year();

		/**
		 * The meta object literal for the '<em><b>Team</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GOALIE_STATS__TEAM = eINSTANCE.getGoalieStats_Team();

		/**
		 * The meta object literal for the '<em><b>Games Played In</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__GAMES_PLAYED_IN = eINSTANCE.getGoalieStats_GamesPlayedIn();

		/**
		 * The meta object literal for the '<em><b>Minutes Played In</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__MINUTES_PLAYED_IN = eINSTANCE.getGoalieStats_MinutesPlayedIn();

		/**
		 * The meta object literal for the '<em><b>Goals Against Average</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__GOALS_AGAINST_AVERAGE = eINSTANCE.getGoalieStats_GoalsAgainstAverage();

		/**
		 * The meta object literal for the '<em><b>Wins</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__WINS = eINSTANCE.getGoalieStats_Wins();

		/**
		 * The meta object literal for the '<em><b>Losses</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__LOSSES = eINSTANCE.getGoalieStats_Losses();

		/**
		 * The meta object literal for the '<em><b>Ties</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__TIES = eINSTANCE.getGoalieStats_Ties();

		/**
		 * The meta object literal for the '<em><b>Empty Net Goals</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__EMPTY_NET_GOALS = eINSTANCE.getGoalieStats_EmptyNetGoals();

		/**
		 * The meta object literal for the '<em><b>Shutouts</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__SHUTOUTS = eINSTANCE.getGoalieStats_Shutouts();

		/**
		 * The meta object literal for the '<em><b>Goals Against</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__GOALS_AGAINST = eINSTANCE.getGoalieStats_GoalsAgainst();

		/**
		 * The meta object literal for the '<em><b>Saves</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__SAVES = eINSTANCE.getGoalieStats_Saves();

		/**
		 * The meta object literal for the '<em><b>Penalty Minutes</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__PENALTY_MINUTES = eINSTANCE.getGoalieStats_PenaltyMinutes();

		/**
		 * The meta object literal for the '<em><b>Goals</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__GOALS = eINSTANCE.getGoalieStats_Goals();

		/**
		 * The meta object literal for the '<em><b>Assists</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__ASSISTS = eINSTANCE.getGoalieStats_Assists();

		/**
		 * The meta object literal for the '<em><b>Points</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GOALIE_STATS__POINTS = eINSTANCE.getGoalieStats_Points();

		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleagueObjectImpl <em>Object</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleagueObjectImpl
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getHockeyleagueObject()
		 * @generated
		 */
		EClass HOCKEYLEAGUE_OBJECT = eINSTANCE.getHockeyleagueObject();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HOCKEYLEAGUE_OBJECT__NAME = eINSTANCE.getHockeyleagueObject_Name();

		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.LeagueImpl <em>League</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.LeagueImpl
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getLeague()
		 * @generated
		 */
		EClass LEAGUE = eINSTANCE.getLeague();

		/**
		 * The meta object literal for the '<em><b>Headoffice</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute LEAGUE__HEADOFFICE = eINSTANCE.getLeague_Headoffice();

		/**
		 * The meta object literal for the '<em><b>Teams</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference LEAGUE__TEAMS = eINSTANCE.getLeague_Teams();

		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerImpl <em>Player</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerImpl
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getPlayer()
		 * @generated
		 */
		EClass PLAYER = eINSTANCE.getPlayer();

		/**
		 * The meta object literal for the '<em><b>Birthplace</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER__BIRTHPLACE = eINSTANCE.getPlayer_Birthplace();

		/**
		 * The meta object literal for the '<em><b>Number</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER__NUMBER = eINSTANCE.getPlayer_Number();

		/**
		 * The meta object literal for the '<em><b>Height Mesurement</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER__HEIGHT_MESUREMENT = eINSTANCE.getPlayer_HeightMesurement();

		/**
		 * The meta object literal for the '<em><b>Height Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER__HEIGHT_VALUE = eINSTANCE.getPlayer_HeightValue();

		/**
		 * The meta object literal for the '<em><b>Weight Mesurement</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER__WEIGHT_MESUREMENT = eINSTANCE.getPlayer_WeightMesurement();

		/**
		 * The meta object literal for the '<em><b>Weight Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER__WEIGHT_VALUE = eINSTANCE.getPlayer_WeightValue();

		/**
		 * The meta object literal for the '<em><b>Shot</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER__SHOT = eINSTANCE.getPlayer_Shot();

		/**
		 * The meta object literal for the '<em><b>Birthdate</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER__BIRTHDATE = eINSTANCE.getPlayer_Birthdate();

		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl <em>Player Stats</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.PlayerStatsImpl
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getPlayerStats()
		 * @generated
		 */
		EClass PLAYER_STATS = eINSTANCE.getPlayerStats();

		/**
		 * The meta object literal for the '<em><b>Year</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER_STATS__YEAR = eINSTANCE.getPlayerStats_Year();

		/**
		 * The meta object literal for the '<em><b>Team</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PLAYER_STATS__TEAM = eINSTANCE.getPlayerStats_Team();

		/**
		 * The meta object literal for the '<em><b>Games Played In</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER_STATS__GAMES_PLAYED_IN = eINSTANCE.getPlayerStats_GamesPlayedIn();

		/**
		 * The meta object literal for the '<em><b>Goals</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER_STATS__GOALS = eINSTANCE.getPlayerStats_Goals();

		/**
		 * The meta object literal for the '<em><b>Assists</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER_STATS__ASSISTS = eINSTANCE.getPlayerStats_Assists();

		/**
		 * The meta object literal for the '<em><b>Points</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER_STATS__POINTS = eINSTANCE.getPlayerStats_Points();

		/**
		 * The meta object literal for the '<em><b>Plus Minus</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER_STATS__PLUS_MINUS = eINSTANCE.getPlayerStats_PlusMinus();

		/**
		 * The meta object literal for the '<em><b>Penalty Minutes</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER_STATS__PENALTY_MINUTES = eINSTANCE.getPlayerStats_PenaltyMinutes();

		/**
		 * The meta object literal for the '<em><b>Power Play Goals</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER_STATS__POWER_PLAY_GOALS = eINSTANCE.getPlayerStats_PowerPlayGoals();

		/**
		 * The meta object literal for the '<em><b>Short Handed Goals</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER_STATS__SHORT_HANDED_GOALS = eINSTANCE.getPlayerStats_ShortHandedGoals();

		/**
		 * The meta object literal for the '<em><b>Game Winning Goals</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER_STATS__GAME_WINNING_GOALS = eINSTANCE.getPlayerStats_GameWinningGoals();

		/**
		 * The meta object literal for the '<em><b>Shots</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER_STATS__SHOTS = eINSTANCE.getPlayerStats_Shots();

		/**
		 * The meta object literal for the '<em><b>Shot Percentage</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PLAYER_STATS__SHOT_PERCENTAGE = eINSTANCE.getPlayerStats_ShotPercentage();

		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.TeamImpl <em>Team</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.TeamImpl
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getTeam()
		 * @generated
		 */
		EClass TEAM = eINSTANCE.getTeam();

		/**
		 * The meta object literal for the '<em><b>Forwards</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TEAM__FORWARDS = eINSTANCE.getTeam_Forwards();

		/**
		 * The meta object literal for the '<em><b>Defencemen</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TEAM__DEFENCEMEN = eINSTANCE.getTeam_Defencemen();

		/**
		 * The meta object literal for the '<em><b>Goalies</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TEAM__GOALIES = eINSTANCE.getTeam_Goalies();

		/**
		 * The meta object literal for the '<em><b>Arena</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TEAM__ARENA = eINSTANCE.getTeam_Arena();

		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.DefencePositionKind <em>Defence Position Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.DefencePositionKind
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getDefencePositionKind()
		 * @generated
		 */
		EEnum DEFENCE_POSITION_KIND = eINSTANCE.getDefencePositionKind();

		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ForwardPositionKind <em>Forward Position Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ForwardPositionKind
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getForwardPositionKind()
		 * @generated
		 */
		EEnum FORWARD_POSITION_KIND = eINSTANCE.getForwardPositionKind();

		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HeightKind <em>Height Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HeightKind
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getHeightKind()
		 * @generated
		 */
		EEnum HEIGHT_KIND = eINSTANCE.getHeightKind();

		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ShotKind <em>Shot Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ShotKind
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getShotKind()
		 * @generated
		 */
		EEnum SHOT_KIND = eINSTANCE.getShotKind();

		/**
		 * The meta object literal for the '{@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.WeightKind <em>Weight Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.WeightKind
		 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.impl.HockeyleaguePackageImpl#getWeightKind()
		 * @generated
		 */
		EEnum WEIGHT_KIND = eINSTANCE.getWeightKind();

	}

} //HockeyleaguePackage
