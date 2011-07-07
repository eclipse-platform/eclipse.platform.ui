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
package org.eclipse.e4.ui.model.application.ui.menu;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Rendered Tool Bar</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.MRenderedToolBar#getContributionManager <em>Contribution Manager</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 */
public interface MRenderedToolBar extends MToolBar {
	/**
	 * Returns the value of the '<em><b>Contribution Manager</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Contribution Manager</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Contribution Manager</em>' attribute.
	 * @see #setContributionManager(Object)
	 * @model transient="true"
	 * @generated
	 */
	Object getContributionManager();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.menu.MRenderedToolBar#getContributionManager <em>Contribution Manager</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Contribution Manager</em>' attribute.
	 * @see #getContributionManager()
	 * @generated
	 */
	void setContributionManager(Object value);

} // MRenderedToolBar
