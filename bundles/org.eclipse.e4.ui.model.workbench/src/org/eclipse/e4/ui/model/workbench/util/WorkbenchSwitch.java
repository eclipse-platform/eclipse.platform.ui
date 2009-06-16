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
 * $Id: WorkbenchSwitch.java,v 1.3 2009/02/03 14:25:37 emoffatt Exp $
 */
package org.eclipse.e4.ui.model.workbench.util;

import java.util.List;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MHandledItem;
import org.eclipse.e4.ui.model.application.MItem;
import org.eclipse.e4.ui.model.application.MItemPart;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MWindow;

import org.eclipse.e4.ui.model.workbench.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.eclipse.e4.ui.model.workbench.WorkbenchPackage
 * @generated
 */
public class WorkbenchSwitch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static WorkbenchPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WorkbenchSwitch() {
		if (modelPackage == null) {
			modelPackage = WorkbenchPackage.eINSTANCE;
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	public T doSwitch(EObject theEObject) {
		return doSwitch(theEObject.eClass(), theEObject);
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected T doSwitch(EClass theEClass, EObject theEObject) {
		if (theEClass.eContainer() == modelPackage) {
			return doSwitch(theEClass.getClassifierID(), theEObject);
		}
		else {
			List<EClass> eSuperTypes = theEClass.getESuperTypes();
			return
				eSuperTypes.isEmpty() ?
					defaultCase(theEObject) :
					doSwitch(eSuperTypes.get(0), theEObject);
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case WorkbenchPackage.MWORKBENCH_WINDOW: {
				MWorkbenchWindow mWorkbenchWindow = (MWorkbenchWindow)theEObject;
				T result = caseMWorkbenchWindow(mWorkbenchWindow);
				if (result == null) result = caseMWindow(mWorkbenchWindow);
				if (result == null) result = caseMItemPart(mWorkbenchWindow);
				if (result == null) result = caseMPart(mWorkbenchWindow);
				if (result == null) result = caseMItem(mWorkbenchWindow);
				if (result == null) result = caseMApplicationElement(mWorkbenchWindow);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case WorkbenchPackage.MPROXY_PART: {
				MProxyPart<?> mProxyPart = (MProxyPart<?>)theEObject;
				T result = caseMProxyPart(mProxyPart);
				if (result == null) result = caseMPart(mProxyPart);
				if (result == null) result = caseMApplicationElement(mProxyPart);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case WorkbenchPackage.MPERSPECTIVE: {
				MPerspective<?> mPerspective = (MPerspective<?>)theEObject;
				T result = caseMPerspective(mPerspective);
				if (result == null) result = caseMItemPart(mPerspective);
				if (result == null) result = caseMPart(mPerspective);
				if (result == null) result = caseMItem(mPerspective);
				if (result == null) result = caseMApplicationElement(mPerspective);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case WorkbenchPackage.MWORKBENCH: {
				MWorkbench mWorkbench = (MWorkbench)theEObject;
				T result = caseMWorkbench(mWorkbench);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case WorkbenchPackage.MMENU_ITEM_RENDERER: {
				MMenuItemRenderer mMenuItemRenderer = (MMenuItemRenderer)theEObject;
				T result = caseMMenuItemRenderer(mMenuItemRenderer);
				if (result == null) result = caseMMenuItem(mMenuItemRenderer);
				if (result == null) result = caseMHandledItem(mMenuItemRenderer);
				if (result == null) result = caseMItem(mMenuItemRenderer);
				if (result == null) result = caseMApplicationElement(mMenuItemRenderer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MWorkbench Window</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MWorkbench Window</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMWorkbenchWindow(MWorkbenchWindow object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MProxy Part</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MProxy Part</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <P extends MPart<?>> T caseMProxyPart(MProxyPart<P> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MPerspective</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MPerspective</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <P extends MPart<?>> T caseMPerspective(MPerspective<P> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MWorkbench</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MWorkbench</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMWorkbench(MWorkbench object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MMenu Item Renderer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MMenu Item Renderer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMMenuItemRenderer(MMenuItemRenderer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MApplication Element</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MApplication Element</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMApplicationElement(MApplicationElement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MPart</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MPart</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <P extends MPart<?>> T caseMPart(MPart<P> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MItem</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MItem</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMItem(MItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MItem Part</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MItem Part</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <P extends MPart<?>> T caseMItemPart(MItemPart<P> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MWindow</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MWindow</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <P extends MPart<?>> T caseMWindow(MWindow<P> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MHandled Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MHandled Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMHandledItem(MHandledItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MMenu Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MMenu Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMMenuItem(MMenuItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	public T defaultCase(EObject object) {
		return null;
	}

} //WorkbenchSwitch
