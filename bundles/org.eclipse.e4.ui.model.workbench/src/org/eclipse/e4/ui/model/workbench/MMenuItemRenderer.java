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

import org.eclipse.e4.ui.model.application.MMenuItem;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>MMenu Item Renderer</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.workbench.MMenuItemRenderer#getRenderer <em>Renderer</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.e4.ui.model.workbench.WorkbenchPackage#getMMenuItemRenderer()
 * @model
 * @generated
 */
public interface MMenuItemRenderer extends MMenuItem {
	/**
	 * Returns the value of the '<em><b>Renderer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Renderer</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Renderer</em>' attribute.
	 * @see #setRenderer(Object)
	 * @see org.eclipse.e4.ui.model.workbench.WorkbenchPackage#getMMenuItemRenderer_Renderer()
	 * @model dataType="org.eclipse.e4.ui.model.workbench.IContributionItem" transient="true"
	 * @generated
	 */
	Object getRenderer();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.workbench.MMenuItemRenderer#getRenderer <em>Renderer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Renderer</em>' attribute.
	 * @see #getRenderer()
	 * @generated
	 */
	void setRenderer(Object value);

} // MMenuItemRenderer
