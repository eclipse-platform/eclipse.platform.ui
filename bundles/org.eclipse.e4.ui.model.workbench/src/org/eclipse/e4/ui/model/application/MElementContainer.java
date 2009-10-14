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

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Element Container</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MElementContainer#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.MElementContainer#getActiveChild <em>Active Child</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getElementContainer()
 * @model abstract="true"
 * @generated
 */
public interface MElementContainer<T extends MUIElement> extends MUIElement {
	/**
	 * Returns the value of the '<em><b>Children</b></em>' containment reference list.
	 * The list contents are of type {@link T}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.e4.ui.model.application.MUIElement#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Children</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getElementContainer_Children()
	 * @see org.eclipse.e4.ui.model.application.MUIElement#getParent
	 * @model opposite="parent" containment="true"
	 * @generated
	 */
	EList<T> getChildren();

	/**
	 * Returns the value of the '<em><b>Active Child</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Active Child</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Active Child</em>' reference.
	 * @see #setActiveChild(MUIElement)
	 * @see org.eclipse.e4.ui.model.application.MApplicationPackage#getElementContainer_ActiveChild()
	 * @model
	 * @generated
	 */
	T getActiveChild();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.MElementContainer#getActiveChild <em>Active Child</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Active Child</em>' reference.
	 * @see #getActiveChild()
	 * @generated
	 */
	void setActiveChild(T value);

} // MElementContainer
