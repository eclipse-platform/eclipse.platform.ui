/**
 * Copyright (c) 2021 EclipseSource GmbH and others.
 *  
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *  
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:   
 *     EclipseSource GmbH - initial API and implementation
 */
package org.eclipse.e4.ui.workbench.internal.persistence;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.workbench.internal.persistence.IPersistencePackage
 * @generated
 */
public interface IPersistenceFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	IPersistenceFactory eINSTANCE = org.eclipse.e4.ui.workbench.internal.persistence.impl.PersistenceFactory.init();

	/**
	 * Returns a new object of class '<em>Workbench State</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Workbench State</em>'.
	 * @generated
	 */
	IWorkbenchState createWorkbenchState();

	/**
	 * Returns a new object of class '<em>Part Memento</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Part Memento</em>'.
	 * @generated
	 */
	IPartMemento createPartMemento();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	IPersistencePackage getPersistencePackage();

} //IPersistenceFactory
