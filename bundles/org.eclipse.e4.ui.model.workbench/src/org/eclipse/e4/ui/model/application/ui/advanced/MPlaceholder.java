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
package org.eclipse.e4.ui.model.application.ui.advanced;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Placeholder</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * A Placeholder is a concrete class used to share elements between perspectives. The
 * elements referenced by a Placeholder generally exist in the Window's 'sharedElements'
 * list. By convention a placeholder usually shares the same elementId as the element
 * that it's referencing.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder#getRef <em>Ref</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder#isCloseable <em>Closeable</em>}</li>
 * </ul>
 *
 * @model
 * @generated
 */
public interface MPlaceholder extends MUIElement, MPartSashContainerElement, MStackElement {
	/**
	 * Returns the value of the '<em><b>Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The reference to the actual UI element that this Placeholder is acting as a proxy for.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Ref</em>' reference.
	 * @see #setRef(MUIElement)
	 * @model required="true"
	 * @generated
	 */
	MUIElement getRef();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder#getRef <em>Ref</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ref</em>' reference.
	 * @see #getRef()
	 * @generated
	 */
	void setRef(MUIElement value);

	/**
	 * Returns the value of the '<em><b>Closeable</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * Determines whether the element (usually a Part) referenced by this Placeholder can
	 * be closed by the User. This allows a Part to be closeable in one perspective but
	 * not closeable in a different one.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Closeable</em>' attribute.
	 * @see #setCloseable(boolean)
	 * @model default="false"
	 * @generated
	 */
	boolean isCloseable();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder#isCloseable <em>Closeable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Closeable</em>' attribute.
	 * @see #isCloseable()
	 * @generated
	 */
	void setCloseable(boolean value);

} // MPlaceholder
