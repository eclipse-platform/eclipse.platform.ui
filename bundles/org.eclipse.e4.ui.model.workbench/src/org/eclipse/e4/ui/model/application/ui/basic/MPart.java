/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.basic;

import java.util.List;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Part</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This a concrete class representing the core UI functionality within a Window. It's what
 * used to be a View / Editor in Eclipse 3.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#getMenus <em>Menus</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#getToolbar <em>Toolbar</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#isCloseable <em>Closeable</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#getLocalizedDescription <em>Localized Description</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#getTrimBars <em>Trim Bars</em>}</li>
 * </ul>
 *
 * @model
 * @generated
 */
public interface MPart extends MUIElement, MPartSashContainerElement, MStackElement, MContribution, MContext, MUILabel, MHandlerContainer, MDirtyable, MBindings, MWindowElement {
	/**
	 * Returns the value of the '<em><b>Menus</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.ui.menu.MMenu}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This defines the list of the menus associated with this part. There are two specific menus
	 * that are managed by the core UI;
	 * <ul>
	 * <li>If the menu is the part's id prefixed with "menu:" then it will appear as the 
	 * drop down menu available from the view's toolbar.</li>
	 * <li>If the menu is the part's id prefixed with "popup:" then it will appear as the 
	 * ddefault context menu for this view.</li>
	 * </ul>
	 * Other menus can be added here but have to be managed by the part itsefl...
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Menus</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MMenu> getMenus();

	/**
	 * Returns the value of the '<em><b>Toolbar</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is the Toolbar associated with tihs Part (if any).
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Toolbar</em>' containment reference.
	 * @see #setToolbar(MToolBar)
	 * @model containment="true"
	 * @generated
	 */
	MToolBar getToolbar();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#getToolbar <em>Toolbar</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Toolbar</em>' containment reference.
	 * @see #getToolbar()
	 * @generated
	 */
	void setToolbar(MToolBar value);

	/**
	 * Returns the value of the '<em><b>Closeable</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * Determines whether the user is allowed to close this view.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Closeable</em>' attribute.
	 * @see #setCloseable(boolean)
	 * @model default="false"
	 * @generated
	 */
	boolean isCloseable();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#isCloseable <em>Closeable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Closeable</em>' attribute.
	 * @see #isCloseable()
	 * @generated
	 */
	void setCloseable(boolean value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The description of this Part; used when the Part appears in a list of Parts
	 * (i.e. 'Show View').
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MPart#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Localized Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Localized Description</em>' attribute.
	 * @model transient="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	String getLocalizedDescription();

	/**
	 * Returns the value of the '<em><b>Trim Bars</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.ui.basic.MTrimBar}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The collection of TrimBars associated with this part.
	 * @since 2.0
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Trim Bars</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MTrimBar> getTrimBars();

} // MPart
