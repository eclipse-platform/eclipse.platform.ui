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
package org.eclipse.e4.ui.model.application.descriptor.basic;

import java.util.List;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Part Descriptor</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This element represents a template from which an MPart can be created on demand.
 * The collection of PartDescriptors owned by the Application represents the contributed
 * parts and is used in the e4 version of 'Show View'...
 * </p>
 * @since 1.0
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isAllowMultiple <em>Allow Multiple</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getCategory <em>Category</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getMenus <em>Menus</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getToolbar <em>Toolbar</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isCloseable <em>Closeable</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable <em>Dirtyable</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getContributionURI <em>Contribution URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getDescription <em>Description</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 */
public interface MPartDescriptor extends MApplicationElement, MUILabel, MHandlerContainer, MBindings {
	/**
	 * Returns the value of the '<em><b>Allow Multiple</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * Determines whether or not the part represented by this descriptot can have multiple
	 * instances with a given window.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Allow Multiple</em>' attribute.
	 * @see #setAllowMultiple(boolean)
	 * @model
	 * @generated
	 */
	boolean isAllowMultiple();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isAllowMultiple <em>Allow Multiple</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Allow Multiple</em>' attribute.
	 * @see #isAllowMultiple()
	 * @generated
	 */
	void setAllowMultiple(boolean value);

	/**
	 * Returns the value of the '<em><b>Category</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The category that the view represented by this descriptor belongs to.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Category</em>' attribute.
	 * @see #setCategory(String)
	 * @model
	 * @generated
	 */
	String getCategory();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getCategory <em>Category</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Category</em>' attribute.
	 * @see #getCategory()
	 * @generated
	 */
	void setCategory(String value);

	/**
	 * Returns the value of the '<em><b>Menus</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.ui.menu.MMenu}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This defines the list of the menus associated with the part represented by this descriptor. 
	 * There are two specific menus that are managed by the core UI;
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
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getToolbar <em>Toolbar</em>}' containment reference.
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
	 * Defines whether instances of views created from this descriptor are closeable by the
	 * User.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Closeable</em>' attribute.
	 * @see #setCloseable(boolean)
	 * @model default="false"
	 * @generated
	 */
	boolean isCloseable();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isCloseable <em>Closeable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Closeable</em>' attribute.
	 * @see #isCloseable()
	 * @generated
	 */
	void setCloseable(boolean value);

	/**
	 * Returns the value of the '<em><b>Dirtyable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * Determines whether Parts generated from this template can participate in the
	 * Dirty -> Save cycle. At best this is a hint since all Parts are inherently dirtyable.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Dirtyable</em>' attribute.
	 * @see #setDirtyable(boolean)
	 * @model
	 * @generated
	 */
	boolean isDirtyable();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable <em>Dirtyable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dirtyable</em>' attribute.
	 * @see #isDirtyable()
	 * @generated
	 */
	void setDirtyable(boolean value);

	/**
	 * Returns the value of the '<em><b>Contribution URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The fully qualified path to the class implementing the behavior of the Part.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Contribution URI</em>' attribute.
	 * @see #setContributionURI(String)
	 * @model
	 * @generated
	 */
	String getContributionURI();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getContributionURI <em>Contribution URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Contribution URI</em>' attribute.
	 * @see #getContributionURI()
	 * @generated
	 */
	void setContributionURI(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The description of this Part.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * A method that will return the translated description.
	 * </p>
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getLocalizedDescription();

} // MPartDescriptor
