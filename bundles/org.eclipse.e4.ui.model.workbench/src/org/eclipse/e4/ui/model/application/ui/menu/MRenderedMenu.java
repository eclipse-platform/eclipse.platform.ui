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
package org.eclipse.e4.ui.model.application.ui.menu;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Rendered Menu</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Do no use! This class will be removed at the beginning of Luna (4.4) development.
 * @deprecated Use MMenu
 * @noreference
 * @since 1.0
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenu#getContributionManager <em>Contribution Manager</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 */
public interface MRenderedMenu extends MMenu {
	/**
	 * Returns the value of the '<em><b>Contribution Manager</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * <strong>Developers</strong>:
	 * Add more detailed documentation by editing this comment in 
	 * org.eclipse.ui.model.workbench/model/UIElements.ecore. 
	 * There is a GenModel/documentation node under each type and attribute.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Contribution Manager</em>' attribute.
	 * @see #setContributionManager(Object)
	 * @model transient="true"
	 * @generated
	 */
	Object getContributionManager();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenu#getContributionManager <em>Contribution Manager</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Contribution Manager</em>' attribute.
	 * @see #getContributionManager()
	 * @generated
	 */
	void setContributionManager(Object value);

} // MRenderedMenu
