/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.advanced;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @generated
 */
public interface MAdvancedFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MAdvancedFactory INSTANCE = org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl.eINSTANCE;

	/**
	 * Returns a new object of class '<em>Placeholder</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Placeholder</em>'.
	 * @since 1.0
	 * @generated
	 */
	MPlaceholder createPlaceholder();

	/**
	 * Returns a new object of class '<em>Perspective</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Perspective</em>'.
	 * @since 1.0
	 * @generated
	 */
	MPerspective createPerspective();

	/**
	 * Returns a new object of class '<em>Perspective Stack</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Perspective Stack</em>'.
	 * @since 1.0
	 * @generated
	 */
	MPerspectiveStack createPerspectiveStack();

	/**
	 * Returns a new object of class '<em>Area</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Area</em>'.
	 * @since 1.0
	 * @generated
	 */
	MArea createArea();

} //MAdvancedFactory
