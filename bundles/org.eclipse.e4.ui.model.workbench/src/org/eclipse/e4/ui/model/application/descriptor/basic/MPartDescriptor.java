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
 */
package org.eclipse.e4.ui.model.application.descriptor.basic;

import java.util.List;
import java.util.Map;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
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
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isAllowMultiple <em>Allow Multiple</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getCategory <em>Category</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getMenus <em>Menus</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getToolbar <em>Toolbar</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isCloseable <em>Closeable</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable <em>Dirtyable</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getContributionURI <em>Contribution URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getLocalizedDescription <em>Localized Description</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getVariables <em>Variables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#getTrimBars <em>Trim Bars</em>}</li>
 * </ul>
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
	 * There are two specific menus that are managed by the core UI:
	 * </p>
	 * <ul>
	 * <li>If the menu is the part's id prefixed with "menu:" then it will appear as the
	 * drop down menu available from the view's toolbar.</li>
	 * <li>If the menu is the part's id prefixed with "popup:" then it will appear as the
	 * default context menu for this view.</li>
	 * </ul>
	 * <p>
	 * Other menus can be added here but have to be managed by the part itself...
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
	 * Dirty -&gt; Save cycle. At best this is a hint since all Parts are inherently dirtyable.
	 * </p>
	 * @deprecated dirtyable is managed by part.
	 * @noreference This method is not intended to be referenced by clients.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=530887">Bug 530887</a>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Dirtyable</em>' attribute.
	 * @see #setDirtyable(boolean)
	 * @model
	 * @generated
	 */
	@Deprecated
	boolean isDirtyable();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable <em>Dirtyable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dirtyable</em>' attribute.
	 * @see #isDirtyable()
	 * @deprecated See {@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable() model documentation} for details.
	 * @noreference See {@link org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor#isDirtyable() model documentation} for details.
	 * @generated
	 */
	@Deprecated
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
	 * Returns the value of the '<em><b>Localized Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Localized Description</em>' attribute.
	 * @model transient="true" changeable="false" volatile="true" derived="true"
	 * @generated
	 */
	String getLocalizedDescription();

	/**
	 * Returns the value of the '<em><b>Variables</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
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
	 * @return the value of the '<em>Variables</em>' attribute list.
	 * @model ordered="false"
	 * @generated
	 */
	List<String> getVariables();

	/**
	 * Returns the value of the '<em><b>Properties</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link java.lang.String},
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
	 * @return the value of the '<em>Properties</em>' map.
	 * @model mapType="org.eclipse.e4.ui.model.application.StringToStringMap&lt;org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EString&gt;"
	 * @generated
	 */
	Map<String, String> getProperties();

	/**
	 * Returns the value of the '<em><b>Trim Bars</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.ui.basic.MTrimBar}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The collection of TrimBars associated with the part represented by this descriptor.
	 * @since 1.3
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Trim Bars</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MTrimBar> getTrimBars();

} // MPartDescriptor
