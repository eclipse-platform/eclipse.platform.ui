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


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Input</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This class should be mixed into UI elements such as InputParts that need to
 * reference an external resource (files...).
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @deprecated No longer used
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MInput#getInputURI <em>Input URI</em>}</li>
 * </ul>
 * </p>
 *
 * @model interface="true" abstract="true"
 * @generated
 */
public interface MInput {
	/**
	 * Returns the value of the '<em><b>Input URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The specification of the particular resource's location or other meta information.
	 * The format of this field will be interpreted by the class using it (i.e. a Part).
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Input URI</em>' attribute.
	 * @see #setInputURI(String)
	 * @model
	 * @generated
	 */
	String getInputURI();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MInput#getInputURI <em>Input URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Input URI</em>' attribute.
	 * @see #getInputURI()
	 * @generated
	 */
	void setInputURI(String value);

} // MInput
