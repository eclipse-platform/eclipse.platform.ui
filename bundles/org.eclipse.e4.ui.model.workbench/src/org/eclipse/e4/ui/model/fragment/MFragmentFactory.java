/**
 * Copyright (c) 2010, 2015 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.fragment;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @generated
 */
public interface MFragmentFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MFragmentFactory INSTANCE = org.eclipse.e4.ui.model.fragment.impl.FragmentFactoryImpl.eINSTANCE;

	/**
	 * Returns a new object of class '<em>Model Fragments</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Model Fragments</em>'.
	 * @since 1.0
	 * @generated
	 */
	MModelFragments createModelFragments();

	/**
	 * Returns a new object of class '<em>String Model Fragment</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>String Model Fragment</em>'.
	 * @since 1.0
	 * @generated
	 */
	MStringModelFragment createStringModelFragment();

} //MFragmentFactory
