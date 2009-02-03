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
package org.eclipse.e4.ui.model.workbench;

import org.eclipse.e4.ui.model.application.MPart;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.model.workbench.WorkbenchPackage
 * @generated
 */
public interface WorkbenchFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	WorkbenchFactory eINSTANCE = org.eclipse.e4.ui.model.internal.workbench.WorkbenchFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>MWorkbench Window</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MWorkbench Window</em>'.
	 * @generated
	 */
	MWorkbenchWindow createMWorkbenchWindow();

	/**
	 * Returns a new object of class '<em>MPerspective</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MPerspective</em>'.
	 * @generated
	 */
	<P extends MPart<?>> MPerspective<P> createMPerspective();

	/**
	 * Returns a new object of class '<em>MWorkbench</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>MWorkbench</em>'.
	 * @generated
	 */
	MWorkbench createMWorkbench();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	WorkbenchPackage getWorkbenchPackage();

} //WorkbenchFactory
