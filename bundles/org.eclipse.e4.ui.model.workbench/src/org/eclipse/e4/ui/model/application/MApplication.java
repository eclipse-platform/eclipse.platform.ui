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

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>MApplication</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MApplication#getWindows <em>Windows</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MApplication#getCommand <em>Command</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMApplication()
 * @model
 * @generated
 */
public interface MApplication<W extends MWindow<?>> extends MApplicationElement {
	/**
	 * Returns the value of the '<em><b>Windows</b></em>' containment reference list.
	 * The list contents are of type {@link W}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Windows</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Windows</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMApplication_Windows()
	 * @model containment="true" required="true"
	 * @generated
	 */
	EList<W> getWindows();

	/**
	 * Returns the value of the '<em><b>Command</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.MCommand}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Command</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Command</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMApplication_Command()
	 * @model containment="true" required="true"
	 * @generated
	 */
	EList<MCommand> getCommand();

} // MApplication
