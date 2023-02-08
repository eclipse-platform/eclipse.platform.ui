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
package org.eclipse.e4.ui.model.application.ui.basic;

import java.util.List;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Window</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This is the concrete class representing a bare bones window in the UI Model. Unless
 * specifically desired it's likely better to use the TrimmedWindow instead.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getMainMenu <em>Main Menu</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getX <em>X</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getY <em>Y</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getWidth <em>Width</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getHeight <em>Height</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getWindows <em>Windows</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getSharedElements <em>Shared Elements</em>}</li>
 * </ul>
 *
 * @model
 * @generated
 */
public interface MWindow
		extends MElementContainer<MWindowElement>, MUILabel, MContext, MHandlerContainer, MBindings, MSnippetContainer {
	/**
	 * Returns the value of the '<em><b>Main Menu</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The main menu (if any) for this window.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Main Menu</em>' containment reference.
	 * @see #setMainMenu(MMenu)
	 * @model containment="true"
	 * @generated
	 */
	MMenu getMainMenu();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getMainMenu <em>Main Menu</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Main Menu</em>' containment reference.
	 * @see #getMainMenu()
	 * @generated
	 */
	void setMainMenu(MMenu value);

	/**
	 * Returns the value of the '<em><b>X</b></em>' attribute.
	 * The default value is <code>"-2147483648"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The 'X' position of this window
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>X</em>' attribute.
	 * @see #isSetX()
	 * @see #unsetX()
	 * @see #setX(int)
	 * @model default="-2147483648" unsettable="true"
	 * @generated
	 */
	int getX();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getX <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>X</em>' attribute.
	 * @see #isSetX()
	 * @see #unsetX()
	 * @see #getX()
	 * @generated
	 */
	void setX(int value);

	/**
	 * Unsets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getX <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * @since 2.2
	 * <!-- end-user-doc -->
	 * @see #isSetX()
	 * @see #getX()
	 * @see #setX(int)
	 * @generated
	 */
	void unsetX();

	/**
	 * Returns whether the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getX <em>X</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * @since 2.2
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>X</em>' attribute is set.
	 * @see #unsetX()
	 * @see #getX()
	 * @see #setX(int)
	 * @generated
	 */
	boolean isSetX();

	/**
	 * Returns the value of the '<em><b>Y</b></em>' attribute.
	 * The default value is <code>"-2147483648"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The 'Y' position of this window
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Y</em>' attribute.
	 * @see #isSetY()
	 * @see #unsetY()
	 * @see #setY(int)
	 * @model default="-2147483648" unsettable="true"
	 * @generated
	 */
	int getY();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getY <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Y</em>' attribute.
	 * @see #isSetY()
	 * @see #unsetY()
	 * @see #getY()
	 * @generated
	 */
	void setY(int value);

	/**
	 * Unsets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getY <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * @since 2.2
	 * <!-- end-user-doc -->
	 * @see #isSetY()
	 * @see #getY()
	 * @see #setY(int)
	 * @generated
	 */
	void unsetY();

	/**
	 * Returns whether the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getY <em>Y</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * @since 2.2
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Y</em>' attribute is set.
	 * @see #unsetY()
	 * @see #getY()
	 * @see #setY(int)
	 * @generated
	 */
	boolean isSetY();

	/**
	 * Returns the value of the '<em><b>Width</b></em>' attribute.
	 * The default value is <code>"-1"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The width of this window
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Width</em>' attribute.
	 * @see #isSetWidth()
	 * @see #unsetWidth()
	 * @see #setWidth(int)
	 * @model default="-1" unsettable="true"
	 * @generated
	 */
	int getWidth();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getWidth <em>Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Width</em>' attribute.
	 * @see #isSetWidth()
	 * @see #unsetWidth()
	 * @see #getWidth()
	 * @generated
	 */
	void setWidth(int value);

	/**
	 * Unsets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getWidth <em>Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * @since 2.2
	 * <!-- end-user-doc -->
	 * @see #isSetWidth()
	 * @see #getWidth()
	 * @see #setWidth(int)
	 * @generated
	 */
	void unsetWidth();

	/**
	 * Returns whether the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getWidth <em>Width</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * @since 2.2
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Width</em>' attribute is set.
	 * @see #unsetWidth()
	 * @see #getWidth()
	 * @see #setWidth(int)
	 * @generated
	 */
	boolean isSetWidth();

	/**
	 * Returns the value of the '<em><b>Height</b></em>' attribute.
	 * The default value is <code>"-1"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The heigfht of this window
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Height</em>' attribute.
	 * @see #isSetHeight()
	 * @see #unsetHeight()
	 * @see #setHeight(int)
	 * @model default="-1" unsettable="true"
	 * @generated
	 */
	int getHeight();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getHeight <em>Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Height</em>' attribute.
	 * @see #isSetHeight()
	 * @see #unsetHeight()
	 * @see #getHeight()
	 * @generated
	 */
	void setHeight(int value);

	/**
	 * Unsets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getHeight <em>Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * @since 2.2
	 * <!-- end-user-doc -->
	 * @see #isSetHeight()
	 * @see #getHeight()
	 * @see #setHeight(int)
	 * @generated
	 */
	void unsetHeight();

	/**
	 * Returns whether the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getHeight <em>Height</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * @since 2.2
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Height</em>' attribute is set.
	 * @see #unsetHeight()
	 * @see #getHeight()
	 * @see #setHeight(int)
	 * @generated
	 */
	boolean isSetHeight();

	/**
	 * Returns the value of the '<em><b>Windows</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.ui.basic.MWindow}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The collection of 'Detached' windows associated with this window.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Windows</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MWindow> getWindows();

	/**
	 * Returns the value of the '<em><b>Shared Elements</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.ui.MUIElement}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * This is the collection of UI Elements that are referenced by Placeholders, allowing
	 * the re-use of these elements in different Perspectives.
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Shared Elements</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MUIElement> getSharedElements();

} // MWindow
