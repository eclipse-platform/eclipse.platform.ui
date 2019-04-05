/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *    Olivier Prouvost <olivier.prouvost@opcoach.com> - Bug 509868
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
 * @noreference This interface is not intended to be referenced by clients.
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug 509868</a>
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.MInput#getInputURI <em>Input URI</em>}</li>
 * </ul>
 *
 * @model interface="true" abstract="true"
 * @generated
 */
@Deprecated
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
	 * @deprecated No longer used.
	 * @noreference This method is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=509868">Bug 509868</a>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Input URI</em>' attribute.
	 * @see #setInputURI(String)
	 * @model
	 * @generated
	 */
	@Deprecated
	String getInputURI();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MInput#getInputURI <em>Input URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Input URI</em>' attribute.
	 * @see #getInputURI()
	 * @deprecated See {@link org.eclipse.e4.ui.model.application.ui.MInput#getInputURI() model documentation} for details.
	 * @noreference See {@link org.eclipse.e4.ui.model.application.ui.MInput#getInputURI() model documentation} for details.
	 * @generated
	 */
	@Deprecated
	void setInputURI(String value);

} // MInput
