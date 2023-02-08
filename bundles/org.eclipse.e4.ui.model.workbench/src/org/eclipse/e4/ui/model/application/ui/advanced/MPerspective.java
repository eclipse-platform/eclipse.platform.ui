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
package org.eclipse.e4.ui.model.application.ui.advanced;

import java.util.List;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Perspective</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * The Persepctive defines a part of the UI presentation that can be switched independently
 * of the rest of the UI. By using Placeholders it's possible to share elements between
 * different perspectives.
 * </p>
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.MPerspective#getWindows <em>Windows</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.advanced.MPerspective#getTrimBars <em>Trim Bars</em>}</li>
 * </ul>
 *
 * @model
 * @generated
 */
public interface MPerspective
		extends MElementContainer<MPartSashContainerElement>, MUILabel, MContext, MHandlerContainer, MBindings {
	/**
	 * Returns the value of the '<em><b>Windows</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.ui.basic.MWindow}.
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
	 * @return the value of the '<em>Windows</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MWindow> getWindows();

	/**
	 * Returns the value of the '<em><b>Trim Bars</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.ui.basic.MTrimBar}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The collection of TrimBars associated with this perspective.
	 * @since 1.3
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Trim Bars</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MTrimBar> getTrimBars();

} // MPerspective
