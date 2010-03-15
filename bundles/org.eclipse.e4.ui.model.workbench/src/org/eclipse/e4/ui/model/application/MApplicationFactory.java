/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.model.application.MApplicationPackage
 * @generated
 */
public interface MApplicationFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MApplicationFactory eINSTANCE = org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Item</em>'.
	 * @generated
	 */
	MItem createItem();

	/**
	 * Returns a new object of class '<em>Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Menu Item</em>'.
	 * @generated
	 */
	MMenuItem createMenuItem();

	/**
	 * Returns a new object of class '<em>Direct Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Direct Menu Item</em>'.
	 * @generated
	 */
	MDirectMenuItem createDirectMenuItem();

	/**
	 * Returns a new object of class '<em>Menu</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Menu</em>'.
	 * @generated
	 */
	MMenu createMenu();

	/**
	 * Returns a new object of class '<em>Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tool Item</em>'.
	 * @generated
	 */
	MToolItem createToolItem();

	/**
	 * Returns a new object of class '<em>Direct Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Direct Tool Item</em>'.
	 * @generated
	 */
	MDirectToolItem createDirectToolItem();

	/**
	 * Returns a new object of class '<em>Tool Bar</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tool Bar</em>'.
	 * @generated
	 */
	MToolBar createToolBar();

	/**
	 * Returns a new object of class '<em>Application</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Application</em>'.
	 * @generated
	 */
	MApplication createApplication();

	/**
	 * Returns a new object of class '<em>Part</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Part</em>'.
	 * @generated
	 */
	MPart createPart();

	/**
	 * Returns a new object of class '<em>Input Part</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Input Part</em>'.
	 * @generated
	 */
	MInputPart createInputPart();

	/**
	 * Returns a new object of class '<em>Part Descriptor</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Part Descriptor</em>'.
	 * @generated
	 */
	MPartDescriptor createPartDescriptor();

	/**
	 * Returns a new object of class '<em>Part Descriptor Container</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Part Descriptor Container</em>'.
	 * @generated
	 */
	MPartDescriptorContainer createPartDescriptorContainer();

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
	 * Returns a new object of class '<em>Model Components</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Model Components</em>'.
	 * @generated
	 */
	MModelComponents createModelComponents();

	/**
	 * Returns a new object of class '<em>Model Component</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Model Component</em>'.
	 * @generated
	 */
	MModelComponent createModelComponent();

	/**
	 * Returns a new object of class '<em>Binding Context</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Binding Context</em>'.
	 * @generated
	 */
	MBindingContext createBindingContext();

	/**
	 * Returns a new object of class '<em>Binding Table</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Binding Table</em>'.
	 * @generated
	 */
	MBindingTable createBindingTable();

	/**
	 * Returns a new object of class '<em>Command</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Command</em>'.
	 * @generated
	 */
	MCommand createCommand();

	/**
	 * Returns a new object of class '<em>Command Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Command Parameter</em>'.
	 * @generated
	 */
	MCommandParameter createCommandParameter();

	/**
	 * Returns a new object of class '<em>Handler</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Handler</em>'.
	 * @generated
	 */
	MHandler createHandler();

	/**
	 * Returns a new object of class '<em>Handled Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Handled Item</em>'.
	 * @generated
	 */
	MHandledItem createHandledItem();

	/**
	 * Returns a new object of class '<em>Handled Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Handled Menu Item</em>'.
	 * @generated
	 */
	MHandledMenuItem createHandledMenuItem();

	/**
	 * Returns a new object of class '<em>Handled Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Handled Tool Item</em>'.
	 * @generated
	 */
	MHandledToolItem createHandledToolItem();

	/**
	 * Returns a new object of class '<em>Key Binding</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Key Binding</em>'.
	 * @generated
	 */
	MKeyBinding createKeyBinding();

	/**
	 * Returns a new object of class '<em>Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Parameter</em>'.
	 * @generated
	 */
	MParameter createParameter();

	/**
	 * Returns a new object of class '<em>Window Trim</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Window Trim</em>'.
	 * @generated
	 */
	MWindowTrim createWindowTrim();

	/**
	 * Returns a new object of class '<em>Placeholder</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Placeholder</em>'.
	 * @generated
	 */
	MPlaceholder createPlaceholder();

	/**
	 * Returns a new object of class '<em>Perspective</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Perspective</em>'.
	 * @generated
	 */
	MPerspective createPerspective();

	/**
	 * Returns a new object of class '<em>Perspective Stack</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Perspective Stack</em>'.
	 * @generated
	 */
	MPerspectiveStack createPerspectiveStack();

	/**
	 * Returns a new object of class '<em>Test Harness</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Test Harness</em>'.
	 * @generated
	 */
	MTestHarness createTestHarness();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	MApplicationPackage getApplicationPackage();

} //MApplicationFactory
