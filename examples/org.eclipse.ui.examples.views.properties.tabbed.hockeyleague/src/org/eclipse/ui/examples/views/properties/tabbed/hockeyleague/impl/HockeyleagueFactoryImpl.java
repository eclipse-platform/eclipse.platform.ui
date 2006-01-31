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


import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class HockeyleagueFactoryImpl extends EFactoryImpl implements HockeyleagueFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static HockeyleagueFactory init() {
		try {
			HockeyleagueFactory theHockeyleagueFactory = (HockeyleagueFactory)EPackage.Registry.INSTANCE.getEFactory("http:///org/eclipse/ui/views/properties/tabbed/examples/org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ecore"); //$NON-NLS-1$ 
			if (theHockeyleagueFactory != null) {
				return theHockeyleagueFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new HockeyleagueFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public HockeyleagueFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case HockeyleaguePackage.ARENA: return createArena();
			case HockeyleaguePackage.DEFENCE: return createDefence();
			case HockeyleaguePackage.FORWARD: return createForward();
			case HockeyleaguePackage.GOALIE: return createGoalie();
			case HockeyleaguePackage.GOALIE_STATS: return createGoalieStats();
			case HockeyleaguePackage.LEAGUE: return createLeague();
			case HockeyleaguePackage.PLAYER_STATS: return createPlayerStats();
			case HockeyleaguePackage.TEAM: return createTeam();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case HockeyleaguePackage.DEFENCE_POSITION_KIND:
				return createDefencePositionKindFromString(eDataType, initialValue);
			case HockeyleaguePackage.FORWARD_POSITION_KIND:
				return createForwardPositionKindFromString(eDataType, initialValue);
			case HockeyleaguePackage.HEIGHT_KIND:
				return createHeightKindFromString(eDataType, initialValue);
			case HockeyleaguePackage.SHOT_KIND:
				return createShotKindFromString(eDataType, initialValue);
			case HockeyleaguePackage.WEIGHT_KIND:
				return createWeightKindFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case HockeyleaguePackage.DEFENCE_POSITION_KIND:
				return convertDefencePositionKindToString(eDataType, instanceValue);
			case HockeyleaguePackage.FORWARD_POSITION_KIND:
				return convertForwardPositionKindToString(eDataType, instanceValue);
			case HockeyleaguePackage.HEIGHT_KIND:
				return convertHeightKindToString(eDataType, instanceValue);
			case HockeyleaguePackage.SHOT_KIND:
				return convertShotKindToString(eDataType, instanceValue);
			case HockeyleaguePackage.WEIGHT_KIND:
				return convertWeightKindToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Arena createArena() {
		ArenaImpl arena = new ArenaImpl();
		return arena;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Defence createDefence() {
		DefenceImpl defence = new DefenceImpl();
		return defence;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Forward createForward() {
		ForwardImpl forward = new ForwardImpl();
		return forward;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Goalie createGoalie() {
		GoalieImpl goalie = new GoalieImpl();
		return goalie;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GoalieStats createGoalieStats() {
		GoalieStatsImpl goalieStats = new GoalieStatsImpl();
		return goalieStats;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public League createLeague() {
		LeagueImpl league = new LeagueImpl();
		return league;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PlayerStats createPlayerStats() {
		PlayerStatsImpl playerStats = new PlayerStatsImpl();
		return playerStats;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Team createTeam() {
		TeamImpl team = new TeamImpl();
		return team;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DefencePositionKind createDefencePositionKindFromString(EDataType eDataType, String initialValue) {
		DefencePositionKind result = DefencePositionKind.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertDefencePositionKindToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ForwardPositionKind createForwardPositionKindFromString(EDataType eDataType, String initialValue) {
		ForwardPositionKind result = ForwardPositionKind.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertForwardPositionKindToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public HeightKind createHeightKindFromString(EDataType eDataType, String initialValue) {
		HeightKind result = HeightKind.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertHeightKindToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ShotKind createShotKindFromString(EDataType eDataType, String initialValue) {
		ShotKind result = ShotKind.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertShotKindToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WeightKind createWeightKindFromString(EDataType eDataType, String initialValue) {
		WeightKind result = WeightKind.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertWeightKindToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public HockeyleaguePackage getHockeyleaguePackage() {
		return (HockeyleaguePackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	public static HockeyleaguePackage getPackage() {
		return HockeyleaguePackage.eINSTANCE;
	}

} //HockeyleagueFactoryImpl
