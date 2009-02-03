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
package org.eclipse.e4.ui.model.workbench.util;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MItem;
import org.eclipse.e4.ui.model.application.MItemPart;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MWindow;

import org.eclipse.e4.ui.model.workbench.*;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.model.workbench.WorkbenchPackage
 * @generated
 */
public class WorkbenchAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static WorkbenchPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WorkbenchAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = WorkbenchPackage.eINSTANCE;
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
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected WorkbenchSwitch<Adapter> modelSwitch =
		new WorkbenchSwitch<Adapter>() {
			@Override
			public Adapter caseMWorkbenchWindow(MWorkbenchWindow object) {
				return createMWorkbenchWindowAdapter();
			}
			@Override
			public <P extends MPart<?>> Adapter caseMProxyPart(MProxyPart<P> object) {
				return createMProxyPartAdapter();
			}
			@Override
			public <P extends MPart<?>> Adapter caseMPerspective(MPerspective<P> object) {
				return createMPerspectiveAdapter();
			}
			@Override
			public Adapter caseMWorkbench(MWorkbench object) {
				return createMWorkbenchAdapter();
			}
			@Override
			public Adapter caseMApplicationElement(MApplicationElement object) {
				return createMApplicationElementAdapter();
			}
			@Override
			public <P extends MPart<?>> Adapter caseMPart(MPart<P> object) {
				return createMPartAdapter();
			}
			@Override
			public Adapter caseMItem(MItem object) {
				return createMItemAdapter();
			}
			@Override
			public <P extends MPart<?>> Adapter caseMItemPart(MItemPart<P> object) {
				return createMItemPartAdapter();
			}
			@Override
			public <P extends MPart<?>> Adapter caseMWindow(MWindow<P> object) {
				return createMWindowAdapter();
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
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.workbench.MWorkbenchWindow <em>MWorkbench Window</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.workbench.MWorkbenchWindow
	 * @generated
	 */
	public Adapter createMWorkbenchWindowAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.workbench.MProxyPart <em>MProxy Part</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.workbench.MProxyPart
	 * @generated
	 */
	public Adapter createMProxyPartAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.workbench.MPerspective <em>MPerspective</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.workbench.MPerspective
	 * @generated
	 */
	public Adapter createMPerspectiveAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.workbench.MWorkbench <em>MWorkbench</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.workbench.MWorkbench
	 * @generated
	 */
	public Adapter createMWorkbenchAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.MApplicationElement <em>MApplication Element</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.MApplicationElement
	 * @generated
	 */
	public Adapter createMApplicationElementAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.MPart <em>MPart</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.MPart
	 * @generated
	 */
	public Adapter createMPartAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.MItem <em>MItem</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.MItem
	 * @generated
	 */
	public Adapter createMItemAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.MItemPart <em>MItem Part</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.MItemPart
	 * @generated
	 */
	public Adapter createMItemPartAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.e4.ui.model.application.MWindow <em>MWindow</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.e4.ui.model.application.MWindow
	 * @generated
	 */
	public Adapter createMWindowAdapter() {
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

} //WorkbenchAdapterFactory
