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
package org.eclipse.e4.ui.model.application.ui;

import java.util.List;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Element Container</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This is the base for the two different types of containment used in the model;
 * 'Stacks' (where only one element would be visible at a time) and 'Tiles' (where 
 * all the ele elements are visible at the same time.
 * </p><p>
 * All containers define the type of element that they are to contain. By design this is
 * always a single type. Where different concrete types are to be contained within the
 * same container they all both mix in a container-specific type. For example both
 * MParts and MPlaceholders are valid children for an MPartStack so they both mix in
 * 'StackElement' (which is an empty stub used only to constran the stack's types.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
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
	 * It is bidirectional and its opposite is '{@link org.eclipse.e4.ui.model.application.ui.MUIElement#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is the list of contained elements in this container. All elements must be of type <T>.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list.
	 * @see org.eclipse.e4.ui.model.application.ui.MUIElement#getParent
	 * @model opposite="parent" containment="true"
	 * @generated
	 */
	List<T> getChildren();

	/**
	 * Returns the value of the '<em><b>Selected Element</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This field contains the reference to the currently 'selected' element within a container.
	 * Note that the element must not only be in the container's children list but must also be
	 * visible in the presentation ("toBeRendered' == true).
	 * </p>
	 * <!-- end-model-doc -->
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
