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
 * A representation of the literals of the enumeration '<em><b>Weight Kind</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getWeightKind()
 * @model
 * @generated
 */
public final class WeightKind extends AbstractEnumerator {
	/**
	 * The '<em><b>Pounds</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Pounds</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #POUNDS_LITERAL
	 * @model name="pounds"
	 * @generated
	 * @ordered
	 */
	public static final int POUNDS = 0;

	/**
	 * The '<em><b>Kilograms</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Kilograms</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #KILOGRAMS_LITERAL
	 * @model name="kilograms"
	 * @generated
	 * @ordered
	 */
	public static final int KILOGRAMS = 1;

	/**
	 * The '<em><b>Pounds</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #POUNDS
	 * @generated
	 * @ordered
	 */
	public static final WeightKind POUNDS_LITERAL = new WeightKind(POUNDS, "pounds", "pounds"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>Kilograms</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #KILOGRAMS
	 * @generated
	 * @ordered
	 */
	public static final WeightKind KILOGRAMS_LITERAL = new WeightKind(KILOGRAMS, "kilograms", "kilograms"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * An array of all the '<em><b>Weight Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final WeightKind[] VALUES_ARRAY =
		new WeightKind[] {
			POUNDS_LITERAL,
			KILOGRAMS_LITERAL,
		};

	/**
	 * A public read-only list of all the '<em><b>Weight Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Weight Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static WeightKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			WeightKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Weight Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static WeightKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			WeightKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Weight Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static WeightKind get(int value) {
		switch (value) {
			case POUNDS: return POUNDS_LITERAL;
			case KILOGRAMS: return KILOGRAMS_LITERAL;
		}
		return null;
	}

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private WeightKind(int value, String name, String literal) {
		super(value, name, literal);
	}

} //WeightKind
