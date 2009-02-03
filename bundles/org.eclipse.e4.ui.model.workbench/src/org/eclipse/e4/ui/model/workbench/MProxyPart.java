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
package org.eclipse.e4.ui.model.workbench;

import org.eclipse.e4.ui.model.application.MPart;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>MProxy Part</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.workbench.MProxyPart#getPart <em>Part</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.workbench.WorkbenchPackage#getMProxyPart()
 * @model abstract="true"
 * @generated
 */
public interface MProxyPart<P extends MPart<?>> extends MPart<P> {
	/**
	 * Returns the value of the '<em><b>Part</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Part</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Part</em>' reference.
	 * @see #setPart(MPart)
	 * @see org.eclipse.e4.ui.model.workbench.WorkbenchPackage#getMProxyPart_Part()
	 * @model
	 * @generated
	 */
	MPart<?> getPart();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.workbench.MProxyPart#getPart <em>Part</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Part</em>' reference.
	 * @see #getPart()
	 * @generated
	 */
	void setPart(MPart<?> value);

} // MProxyPart
