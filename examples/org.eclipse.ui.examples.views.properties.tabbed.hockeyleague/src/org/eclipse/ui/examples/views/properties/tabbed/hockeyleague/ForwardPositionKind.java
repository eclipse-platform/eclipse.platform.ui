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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.AbstractEnumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Forward Position Kind</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getForwardPositionKind()
 * @model
 * @generated
 */
public final class ForwardPositionKind extends AbstractEnumerator {
	/**
	 * The '<em><b>Left wing</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Left wing</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #LEFT_WING_LITERAL
	 * @model name="left_wing"
	 * @generated
	 * @ordered
	 */
	public static final int LEFT_WING = 0;

	/**
	 * The '<em><b>Right wing</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Right wing</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #RIGHT_WING_LITERAL
	 * @model name="right_wing"
	 * @generated
	 * @ordered
	 */
	public static final int RIGHT_WING = 1;

	/**
	 * The '<em><b>Center</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Center</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #CENTER_LITERAL
	 * @model name="center"
	 * @generated
	 * @ordered
	 */
	public static final int CENTER = 2;

	/**
	 * The '<em><b>Left wing</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #LEFT_WING
	 * @generated
	 * @ordered
	 */
	public static final ForwardPositionKind LEFT_WING_LITERAL = new ForwardPositionKind(LEFT_WING, "left_wing", "left_wing"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>Right wing</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #RIGHT_WING
	 * @generated
	 * @ordered
	 */
	public static final ForwardPositionKind RIGHT_WING_LITERAL = new ForwardPositionKind(RIGHT_WING, "right_wing", "right_wing"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>Center</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CENTER
	 * @generated
	 * @ordered
	 */
	public static final ForwardPositionKind CENTER_LITERAL = new ForwardPositionKind(CENTER, "center", "center"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * An array of all the '<em><b>Forward Position Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final ForwardPositionKind[] VALUES_ARRAY =
		new ForwardPositionKind[] {
			LEFT_WING_LITERAL,
			RIGHT_WING_LITERAL,
			CENTER_LITERAL,
		};

	/**
	 * A public read-only list of all the '<em><b>Forward Position Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Forward Position Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ForwardPositionKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ForwardPositionKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Forward Position Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ForwardPositionKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ForwardPositionKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Forward Position Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ForwardPositionKind get(int value) {
		switch (value) {
			case LEFT_WING: return LEFT_WING_LITERAL;
			case RIGHT_WING: return RIGHT_WING_LITERAL;
			case CENTER: return CENTER_LITERAL;
		}
		return null;
	}

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private ForwardPositionKind(int value, String name, String literal) {
		super(value, name, literal);
	}

} //ForwardPositionKind
