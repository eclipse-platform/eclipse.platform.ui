/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.basic;


/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-user-doc -->
 * @generated
 */
public interface MBasicFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MBasicFactory INSTANCE = org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl.eINSTANCE;

	/**
	 * Returns a new object of class '<em>Part</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Part</em>'.
	 * @generated
	 */
	MPart createPart();

	/**
	 * Returns a new object of class '<em>Composite Part</em>'.
	 * <!-- begin-user-doc -->
	 * @since 1.1
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Composite Part</em>'.
	 * @generated
	 */
	MCompositePart createCompositePart();

	/**
	 * Returns a new object of class '<em>Input Part</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Input Part</em>'.
	 * @generated
	 */
	MInputPart createInputPart();

	/**
	 * Returns a new object of class '<em>Part Stack</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Part Stack</em>'.
	 * @generated
	 */
	MPartStack createPartStack();

	/**
	 * Returns a new object of class '<em>Part Sash Container</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Part Sash Container</em>'.
	 * @generated
	 */
	MPartSashContainer createPartSashContainer();

	/**
	 * Returns a new object of class '<em>Window</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Window</em>'.
	 * @generated
	 */
	MWindow createWindow();

	/**
	 * Returns a new object of class '<em>Trimmed Window</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Trimmed Window</em>'.
	 * @generated
	 */
	MTrimmedWindow createTrimmedWindow();

	/**
	 * Returns a new object of class '<em>Trim Bar</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Trim Bar</em>'.
	 * @generated
	 */
	MTrimBar createTrimBar();

	/**
	 * Returns a new object of class '<em>Dialog</em>'.
	 * <!-- begin-user-doc -->
	 * @since 1.1
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Dialog</em>'.
	 * @generated
	 */
	MDialog createDialog();

	/**
	 * Returns a new object of class '<em>Wizard Dialog</em>'.
	 * <!-- begin-user-doc -->
	 * @since 1.1
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Wizard Dialog</em>'.
	 * @generated
	 */
	MWizardDialog createWizardDialog();

} //MBasicFactory
