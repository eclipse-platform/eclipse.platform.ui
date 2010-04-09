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
package org.eclipse.e4.ui.model.application.ui;

import java.util.List;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Element Container</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MElementContainer#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MElementContainer#getSelectedElement <em>Selected Element</em>}</li>
 * </ul>
 * </p>
 *
 * @model abstract="true"
 * @generated
 */
public interface MElementContainer<T extends MUIElement> extends MUIElement {
	/**
	 * Returns the value of the '<em><b>Children</b></em>' containment reference list.
	 * The list contents are of type {@link T}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Children</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.ui.MUIElement#getParent
	 * @model opposite="parent" containment="true"
	 * @generated
	 */
	List<T> getChildren();

	/**
	 * Returns the value of the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Selected Element</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Selected Element</em>' reference.
	 * @see #setSelectedElement(MUIElement)
	 * @model
	 * @generated
	 */
	T getSelectedElement();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MElementContainer#getSelectedElement <em>Selected Element</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Selected Element</em>' reference.
	 * @see #getSelectedElement()
	 * @generated
	 */
	void setSelectedElement(T value);

} // MElementContainer
