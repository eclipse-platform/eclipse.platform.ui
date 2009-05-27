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
 * $Id: MSashForm.java,v 1.1 2009/02/03 14:25:34 emoffatt Exp $
 */
package org.eclipse.e4.ui.model.application;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>MSash Form</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MSashForm#getWeights <em>Weights</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMSashForm()
 * @model
 * @generated
 */
public interface MSashForm<P extends MPart<?>> extends MPart<P> {
	/**
	 * Returns the value of the '<em><b>Weights</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.Integer}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Weights</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Weights</em>' attribute list.
	 * @see org.eclipse.e4.ui.model.application.ApplicationPackage#getMSashForm_Weights()
	 * @model unique="false"
	 * @generated
	 */
	EList<Integer> getWeights();

} // MSashForm
