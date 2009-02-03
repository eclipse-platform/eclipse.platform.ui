/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *
 * $Id$
 */
package org.eclipse.e4.ui.model.application;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.model.application.ApplicationPackage
 * @generated
 */
public interface ApplicationFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ApplicationFactory eINSTANCE = org.eclipse.e4.ui.model.internal.application.ApplicationFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>MApplication</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MApplication</em>'.
	 * @generated
	 */
	<W extends MWindow<?>> MApplication<W> createMApplication();

	/**
	 * Returns a new object of class '<em>MPart</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MPart</em>'.
	 * @generated
	 */
	<P extends MPart<?>> MPart<P> createMPart();

	/**
	 * Returns a new object of class '<em>MStack</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MStack</em>'.
	 * @generated
	 */
	MStack createMStack();

	/**
	 * Returns a new object of class '<em>MSash Form</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MSash Form</em>'.
	 * @generated
	 */
	<P extends MPart<?>> MSashForm<P> createMSashForm();

	/**
	 * Returns a new object of class '<em>MContributed Part</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MContributed Part</em>'.
	 * @generated
	 */
	<P extends MPart<?>> MContributedPart<P> createMContributedPart();

	/**
	 * Returns a new object of class '<em>MHandler</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MHandler</em>'.
	 * @generated
	 */
	MHandler createMHandler();

	/**
	 * Returns a new object of class '<em>MHandled Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MHandled Item</em>'.
	 * @generated
	 */
	MHandledItem createMHandledItem();

	/**
	 * Returns a new object of class '<em>MMenu Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MMenu Item</em>'.
	 * @generated
	 */
	MMenuItem createMMenuItem();

	/**
	 * Returns a new object of class '<em>MTool Bar Item</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MTool Bar Item</em>'.
	 * @generated
	 */
	MToolBarItem createMToolBarItem();

	/**
	 * Returns a new object of class '<em>MMenu</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MMenu</em>'.
	 * @generated
	 */
	MMenu createMMenu();

	/**
	 * Returns a new object of class '<em>MTool Bar</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MTool Bar</em>'.
	 * @generated
	 */
	MToolBar createMToolBar();

	/**
	 * Returns a new object of class '<em>MTrim</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MTrim</em>'.
	 * @generated
	 */
	MTrim createMTrim();

	/**
	 * Returns a new object of class '<em>MItem Part</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MItem Part</em>'.
	 * @generated
	 */
	<P extends MPart<?>> MItemPart<P> createMItemPart();

	/**
	 * Returns a new object of class '<em>MWindow</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MWindow</em>'.
	 * @generated
	 */
	<P extends MPart<?>> MWindow<P> createMWindow();

	/**
	 * Returns a new object of class '<em>MCommand</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MCommand</em>'.
	 * @generated
	 */
	MCommand createMCommand();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ApplicationPackage getApplicationPackage();

} //ApplicationFactory
