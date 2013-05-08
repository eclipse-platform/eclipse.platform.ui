/**
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.menu;


/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * @since 1.0
 * <!-- end-user-doc -->
 * @generated
 */
public interface MMenuFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MMenuFactory INSTANCE = org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl.eINSTANCE;

	/**
	 * Returns a new object of class '<em>Separator</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Separator</em>'.
	 * @generated
	 */
	MMenuSeparator createMenuSeparator();

	/**
	 * Returns a new object of class '<em>Menu</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Menu</em>'.
	 * @generated
	 */
	MMenu createMenu();

	/**
	 * Returns a new object of class '<em>Contribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Contribution</em>'.
	 * @generated
	 */
	MMenuContribution createMenuContribution();

	/**
	 * Returns a new object of class '<em>Popup Menu</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Popup Menu</em>'.
	 * @generated
	 */
	MPopupMenu createPopupMenu();

	/**
	 * Returns a new object of class '<em>Direct Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Direct Menu Item</em>'.
	 * @generated
	 */
	MDirectMenuItem createDirectMenuItem();

	/**
	 * Returns a new object of class '<em>Handled Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Handled Menu Item</em>'.
	 * @generated
	 */
	MHandledMenuItem createHandledMenuItem();

	/**
	 * Returns a new object of class '<em>Tool Bar</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tool Bar</em>'.
	 * @generated
	 */
	MToolBar createToolBar();

	/**
	 * Returns a new object of class '<em>Tool Control</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tool Control</em>'.
	 * @generated
	 */
	MToolControl createToolControl();

	/**
	 * Returns a new object of class '<em>Handled Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Handled Tool Item</em>'.
	 * @generated
	 */
	MHandledToolItem createHandledToolItem();

	/**
	 * Returns a new object of class '<em>Direct Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Direct Tool Item</em>'.
	 * @generated
	 */
	MDirectToolItem createDirectToolItem();

	/**
	 * Returns a new object of class '<em>Tool Bar Separator</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tool Bar Separator</em>'.
	 * @generated
	 */
	MToolBarSeparator createToolBarSeparator();

	/**
	 * Returns a new object of class '<em>Rendered Menu</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Rendered Menu</em>'.
	 * @generated
	 */
	MRenderedMenu createRenderedMenu();

	/**
	 * Returns a new object of class '<em>Rendered Tool Bar</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Rendered Tool Bar</em>'.
	 * @generated
	 */
	MRenderedToolBar createRenderedToolBar();

	/**
	 * Returns a new object of class '<em>Tool Bar Contribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tool Bar Contribution</em>'.
	 * @generated
	 */
	MToolBarContribution createToolBarContribution();

	/**
	 * Returns a new object of class '<em>Trim Contribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Trim Contribution</em>'.
	 * @generated
	 */
	MTrimContribution createTrimContribution();

	/**
	 * Returns a new object of class '<em>Rendered Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Rendered Menu Item</em>'.
	 * @generated
	 */
	MRenderedMenuItem createRenderedMenuItem();

	/**
	 * Returns a new object of class '<em>Opaque Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Opaque Tool Item</em>'.
	 * @generated
	 */
	MOpaqueToolItem createOpaqueToolItem();

	/**
	 * Returns a new object of class '<em>Opaque Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Opaque Menu Item</em>'.
	 * @generated
	 */
	MOpaqueMenuItem createOpaqueMenuItem();

	/**
	 * Returns a new object of class '<em>Opaque Menu Separator</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Opaque Menu Separator</em>'.
	 * @generated
	 */
	MOpaqueMenuSeparator createOpaqueMenuSeparator();

	/**
	 * Returns a new object of class '<em>Opaque Menu</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Opaque Menu</em>'.
	 * @generated
	 */
	MOpaqueMenu createOpaqueMenu();

	/**
	 * Returns a new object of class '<em>Dynamic Menu Contribution</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Dynamic Menu Contribution</em>'.
	 * @generated
	 */
	MDynamicMenuContribution createDynamicMenuContribution();

} //MMenuFactory
