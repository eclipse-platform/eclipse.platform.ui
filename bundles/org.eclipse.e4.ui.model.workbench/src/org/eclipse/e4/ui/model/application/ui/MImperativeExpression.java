/**
 * Copyright (c) 2008, 2017 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui;

import org.eclipse.e4.ui.model.application.MContribution;

/**
 * <!-- begin-user-doc --> A representation of the model object
 * '<em><b>Imperative Expression</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.e4.ui.model.application.ui.MImperativeExpression#isTracking
 * <em>Tracking</em>}</li>
 * </ul>
 *
 * @model
 * @generated
 * @since 2.0
 */
public interface MImperativeExpression extends MExpression, MContribution {

	/**
	 * Returns the value of the '<em><b>Tracking</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tracking</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tracking</em>' attribute.
	 * @see #setTracking(boolean)
	 * @model
	 * @generated
	 */
	boolean isTracking();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.MImperativeExpression#isTracking <em>Tracking</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tracking</em>' attribute.
	 * @see #isTracking()
	 * @generated
	 */
	void setTracking(boolean value);
} // MImperativeExpression
