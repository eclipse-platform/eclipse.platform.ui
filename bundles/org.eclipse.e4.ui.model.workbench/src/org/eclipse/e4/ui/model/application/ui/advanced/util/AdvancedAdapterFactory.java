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
package org.eclipse.e4.ui.model.application.ui.advanced.util;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MLocalizable;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl
 * @generated
 */
public class AdvancedAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static AdvancedPackageImpl modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AdvancedAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = AdvancedPackageImpl.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject) object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AdvancedSwitch<Adapter> modelSwitch = new AdvancedSwitch<>() {
		@Override
		public Adapter casePlaceholder(MPlaceholder object) {
			return createPlaceholderAdapter();
		}

		@Override
		public Adapter casePerspective(MPerspective object) {
			return createPerspectiveAdapter();
		}

		@Override
		public Adapter casePerspectiveStack(MPerspectiveStack object) {
			return createPerspectiveStackAdapter();
		}

		@Override
		public Adapter caseArea(MArea object) {
			return createAreaAdapter();
		}

		@Override
		public Adapter caseApplicationElement(MApplicationElement object) {
			return createApplicationElementAdapter();
		}

		@Override
		public Adapter caseLocalizable(MLocalizable object) {
			return createLocalizableAdapter();
		}

		@Override
		public Adapter caseUIElement(MUIElement object) {
			return createUIElementAdapter();
		}

		@Override
		public Adapter casePartSashContainerElement(MPartSashContainerElement object) {
			return createPartSashContainerElementAdapter();
		}

		@Override
		public Adapter caseStackElement(MStackElement object) {
			return createStackElementAdapter();
		}

		@Override
		public <T extends MUIElement> Adapter caseElementContainer(MElementContainer<T> object) {
			return createElementContainerAdapter();
		}

		@Override
		public Adapter caseUILabel(MUILabel object) {
			return createUILabelAdapter();
		}

		@Override
		public Adapter caseContext(MContext object) {
			return createContextAdapter();
		}

		@Override
		public Adapter caseHandlerContainer(MHandlerContainer object) {
			return createHandlerContainerAdapter();
		}

		@Override
		public Adapter caseBindings(MBindings object) {
			return createBindingsAdapter();
		}

		@Override
		public <T extends MUIElement> Adapter caseGenericStack(MGenericStack<T> object) {
			return createGenericStackAdapter();
		}

		@Override
		public Adapter caseWindowElement(MWindowElement object) {
			return createWindowElementAdapter();
		}

		@Override
		public <T extends MUIElement> Adapter caseGenericTile(MGenericTile<T> object) {
			return createGenericTileAdapter();
		}

		@Override
		public Adapter casePartSashContainer(MPartSashContainer object) {
			return createPartSashContainerAdapter();
		}

		@Override
		public Adapter defaultCase(EObject object) {
			return createEObjectAdapter();
		}
	};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject) target);
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder <em>Placeholder</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder
	 * @since 1.0
	 * @generated
	 */
	public Adapter createPlaceholderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.advanced.MPerspective <em>Perspective</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.MPerspective
	 * @since 1.0
	 * @generated
	 */
	public Adapter createPerspectiveAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack <em>Perspective Stack</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack
	 * @since 1.0
	 * @generated
	 */
	public Adapter createPerspectiveStackAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.advanced.MArea <em>Area</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.advanced.MArea
	 * @since 1.0
	 * @generated
	 */
	public Adapter createAreaAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.MApplicationElement <em>Element</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.MApplicationElement
	 * @since 1.0
	 * @generated
	 */
	public Adapter createApplicationElementAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.MLocalizable <em>Localizable</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.MLocalizable
	 * @since 1.1
	 * @generated
	 */
	public Adapter createLocalizableAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.MUIElement <em>UI Element</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.MUIElement
	 * @since 1.0
	 * @generated
	 */
	public Adapter createUIElementAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement <em>Part Sash Container Element</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement
	 * @since 1.0
	 * @generated
	 */
	public Adapter createPartSashContainerElementAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.basic.MStackElement <em>Stack Element</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MStackElement
	 * @since 1.0
	 * @generated
	 */
	public Adapter createStackElementAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.MElementContainer <em>Element Container</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.MElementContainer
	 * @since 1.0
	 * @generated
	 */
	public Adapter createElementContainerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.MUILabel <em>UI Label</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.MUILabel
	 * @since 1.0
	 * @generated
	 */
	public Adapter createUILabelAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.MContext <em>Context</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.MContext
	 * @since 1.0
	 * @generated
	 */
	public Adapter createContextAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.commands.MHandlerContainer <em>Handler Container</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.commands.MHandlerContainer
	 * @since 1.0
	 * @generated
	 */
	public Adapter createHandlerContainerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.commands.MBindings <em>Bindings</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.commands.MBindings
	 * @since 1.0
	 * @generated
	 */
	public Adapter createBindingsAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.MGenericStack <em>Generic Stack</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.MGenericStack
	 * @since 1.0
	 * @generated
	 */
	public Adapter createGenericStackAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.basic.MWindowElement <em>Window Element</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MWindowElement
	 * @since 1.0
	 * @generated
	 */
	public Adapter createWindowElementAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.MGenericTile <em>Generic Tile</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.MGenericTile
	 * @since 1.0
	 * @generated
	 */
	public Adapter createGenericTileAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer <em>Part Sash Container</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer
	 * @since 1.0
	 * @generated
	 */
	public Adapter createPartSashContainerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //AdvancedAdapterFactory
