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
 * A representation of the literals of the enumeration '<em><b>Defence Position Kind</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getDefencePositionKind()
 * @model
 * @generated
 */
public final class DefencePositionKind extends AbstractEnumerator {
	/**
	 * The '<em><b>Left defence</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Left defence</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #LEFT_DEFENCE_LITERAL
	 * @model name="left_defence"
	 * @generated
	 * @ordered
	 */
	public static final int LEFT_DEFENCE = 0;

	/**
	 * The '<em><b>Right defence</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Right defence</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #RIGHT_DEFENCE_LITERAL
	 * @model name="right_defence"
	 * @generated
	 * @ordered
	 */
	public static final int RIGHT_DEFENCE = 1;

	/**
	 * The '<em><b>Left defence</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #LEFT_DEFENCE
	 * @generated
	 * @ordered
	 */
	public static final DefencePositionKind LEFT_DEFENCE_LITERAL = new DefencePositionKind(LEFT_DEFENCE, "left_defence", "left_defence"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>Right defence</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #RIGHT_DEFENCE
	 * @generated
	 * @ordered
	 */
	public static final DefencePositionKind RIGHT_DEFENCE_LITERAL = new DefencePositionKind(RIGHT_DEFENCE, "right_defence", "right_defence"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * An array of all the '<em><b>Defence Position Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final DefencePositionKind[] VALUES_ARRAY =
		new DefencePositionKind[] {
			LEFT_DEFENCE_LITERAL,
			RIGHT_DEFENCE_LITERAL,
		};

	/**
	 * A public read-only list of all the '<em><b>Defence Position Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Defence Position Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static DefencePositionKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			DefencePositionKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Defence Position Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static DefencePositionKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			DefencePositionKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Defence Position Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static DefencePositionKind get(int value) {
		switch (value) {
			case LEFT_DEFENCE: return LEFT_DEFENCE_LITERAL;
			case RIGHT_DEFENCE: return RIGHT_DEFENCE_LITERAL;
		}
		return null;
	}

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private DefencePositionKind(int value, String name, String literal) {
		super(value, name, literal);
	}

} //DefencePositionKind
