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
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getMainMenu <em>Main Menu</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getX <em>X</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getY <em>Y</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getWidth <em>Width</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getHeight <em>Height</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getWindows <em>Windows</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getSharedElements <em>Shared Elements</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 */
public interface MWindow extends MElementContainer<MWindowElement>, MUILabel, MContext, MHandlerContainer, MBindings, MSnippetContainer {
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
	 * @see #setX(int)
	 * @model default="-2147483648"
	 * @generated
	 */
	int getX();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getX <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>X</em>' attribute.
	 * @see #getX()
	 * @generated
	 */
	void setX(int value);

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
	 * @see #setY(int)
	 * @model default="-2147483648"
	 * @generated
	 */
	int getY();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getY <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Y</em>' attribute.
	 * @see #getY()
	 * @generated
	 */
	void setY(int value);

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
	 * @see #setWidth(int)
	 * @model default="-1"
	 * @generated
	 */
	int getWidth();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getWidth <em>Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Width</em>' attribute.
	 * @see #getWidth()
	 * @generated
	 */
	void setWidth(int value);

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
	 * @see #setHeight(int)
	 * @model default="-1"
	 * @generated
	 */
	int getHeight();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindow#getHeight <em>Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Height</em>' attribute.
	 * @see #getHeight()
	 * @generated
	 */
	void setHeight(int value);

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
