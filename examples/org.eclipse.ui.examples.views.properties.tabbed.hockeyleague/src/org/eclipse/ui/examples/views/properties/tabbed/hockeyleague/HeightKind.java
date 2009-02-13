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
 * A representation of the literals of the enumeration '<em><b>Height Kind</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage#getHeightKind()
 * @model
 * @generated
 */
public final class HeightKind extends AbstractEnumerator {
	/**
	 * The '<em><b>Inches</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Inches</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #INCHES_LITERAL
	 * @model name="inches"
	 * @generated
	 * @ordered
	 */
	public static final int INCHES = 0;

	/**
	 * The '<em><b>Centimeters</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>Centimeters</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #CENTIMETERS_LITERAL
	 * @model name="centimeters"
	 * @generated
	 * @ordered
	 */
	public static final int CENTIMETERS = 1;

	/**
	 * The '<em><b>Inches</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #INCHES
	 * @generated
	 * @ordered
	 */
	public static final HeightKind INCHES_LITERAL = new HeightKind(INCHES, "inches", "inches"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * The '<em><b>Centimeters</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CENTIMETERS
	 * @generated
	 * @ordered
	 */
	public static final HeightKind CENTIMETERS_LITERAL = new HeightKind(CENTIMETERS, "centimeters", "centimeters"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * An array of all the '<em><b>Height Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final HeightKind[] VALUES_ARRAY =
		new HeightKind[] {
			INCHES_LITERAL,
			CENTIMETERS_LITERAL,
		};

	/**
	 * A public read-only list of all the '<em><b>Height Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Height Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static HeightKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			HeightKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Height Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static HeightKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			HeightKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Height Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static HeightKind get(int value) {
		switch (value) {
			case INCHES: return INCHES_LITERAL;
			case CENTIMETERS: return CENTIMETERS_LITERAL;
		}
		return null;
	}

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private HeightKind(int value, String name, String literal) {
		super(value, name, literal);
	}

} //HeightKind
