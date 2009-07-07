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
 * $Id: ApplicationSwitch.java,v 1.4 2009/04/13 19:47:35 emoffatt Exp $
 */
package org.eclipse.e4.ui.model.application.util;

import java.util.List;

import org.eclipse.e4.ui.model.application.*;

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
 * @see org.eclipse.e4.ui.model.application.ApplicationPackage
 * @generated
 */
public class ApplicationSwitch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ApplicationPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ApplicationSwitch() {
		if (modelPackage == null) {
			modelPackage = ApplicationPackage.eINSTANCE;
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
			case ApplicationPackage.MAPPLICATION_ELEMENT: {
				MApplicationElement mApplicationElement = (MApplicationElement)theEObject;
				T result = caseMApplicationElement(mApplicationElement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MAPPLICATION: {
				MApplication<?> mApplication = (MApplication<?>)theEObject;
				T result = caseMApplication(mApplication);
				if (result == null) result = caseMApplicationElement(mApplication);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MPART: {
				MPart<?> mPart = (MPart<?>)theEObject;
				T result = caseMPart(mPart);
				if (result == null) result = caseMApplicationElement(mPart);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MSTACK: {
				MStack mStack = (MStack)theEObject;
				T result = caseMStack(mStack);
				if (result == null) result = caseMPart(mStack);
				if (result == null) result = caseMApplicationElement(mStack);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MSASH_FORM: {
				MSashForm<?> mSashForm = (MSashForm<?>)theEObject;
				T result = caseMSashForm(mSashForm);
				if (result == null) result = caseMPart(mSashForm);
				if (result == null) result = caseMApplicationElement(mSashForm);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MCONTRIBUTED_PART: {
				MContributedPart<?> mContributedPart = (MContributedPart<?>)theEObject;
				T result = caseMContributedPart(mContributedPart);
				if (result == null) result = caseMItemPart(mContributedPart);
				if (result == null) result = caseMContribution(mContributedPart);
				if (result == null) result = caseMPart(mContributedPart);
				if (result == null) result = caseMItem(mContributedPart);
				if (result == null) result = caseMApplicationElement(mContributedPart);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MCONTRIBUTION: {
				MContribution mContribution = (MContribution)theEObject;
				T result = caseMContribution(mContribution);
				if (result == null) result = caseMApplicationElement(mContribution);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MHANDLER: {
				MHandler mHandler = (MHandler)theEObject;
				T result = caseMHandler(mHandler);
				if (result == null) result = caseMContribution(mHandler);
				if (result == null) result = caseMApplicationElement(mHandler);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MITEM: {
				MItem mItem = (MItem)theEObject;
				T result = caseMItem(mItem);
				if (result == null) result = caseMApplicationElement(mItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MHANDLED_ITEM: {
				MHandledItem mHandledItem = (MHandledItem)theEObject;
				T result = caseMHandledItem(mHandledItem);
				if (result == null) result = caseMItem(mHandledItem);
				if (result == null) result = caseMApplicationElement(mHandledItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MMENU_ITEM: {
				MMenuItem mMenuItem = (MMenuItem)theEObject;
				T result = caseMMenuItem(mMenuItem);
				if (result == null) result = caseMHandledItem(mMenuItem);
				if (result == null) result = caseMItem(mMenuItem);
				if (result == null) result = caseMApplicationElement(mMenuItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MTOOL_BAR_ITEM: {
				MToolBarItem mToolBarItem = (MToolBarItem)theEObject;
				T result = caseMToolBarItem(mToolBarItem);
				if (result == null) result = caseMHandledItem(mToolBarItem);
				if (result == null) result = caseMItem(mToolBarItem);
				if (result == null) result = caseMApplicationElement(mToolBarItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MITEM_CONTAINER: {
				MItemContainer<?> mItemContainer = (MItemContainer<?>)theEObject;
				T result = caseMItemContainer(mItemContainer);
				if (result == null) result = caseMApplicationElement(mItemContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MMENU: {
				MMenu mMenu = (MMenu)theEObject;
				T result = caseMMenu(mMenu);
				if (result == null) result = caseMItemContainer(mMenu);
				if (result == null) result = caseMApplicationElement(mMenu);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MTOOL_BAR: {
				MToolBar mToolBar = (MToolBar)theEObject;
				T result = caseMToolBar(mToolBar);
				if (result == null) result = caseMItemContainer(mToolBar);
				if (result == null) result = caseMApplicationElement(mToolBar);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MTRIMMED_PART: {
				MTrimmedPart<?> mTrimmedPart = (MTrimmedPart<?>)theEObject;
				T result = caseMTrimmedPart(mTrimmedPart);
				if (result == null) result = caseMPart(mTrimmedPart);
				if (result == null) result = caseMApplicationElement(mTrimmedPart);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MITEM_PART: {
				MItemPart<?> mItemPart = (MItemPart<?>)theEObject;
				T result = caseMItemPart(mItemPart);
				if (result == null) result = caseMPart(mItemPart);
				if (result == null) result = caseMItem(mItemPart);
				if (result == null) result = caseMApplicationElement(mItemPart);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MWINDOW: {
				MWindow<?> mWindow = (MWindow<?>)theEObject;
				T result = caseMWindow(mWindow);
				if (result == null) result = caseMItemPart(mWindow);
				if (result == null) result = caseMPart(mWindow);
				if (result == null) result = caseMItem(mWindow);
				if (result == null) result = caseMApplicationElement(mWindow);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MCOMMAND: {
				MCommand mCommand = (MCommand)theEObject;
				T result = caseMCommand(mCommand);
				if (result == null) result = caseMApplicationElement(mCommand);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MTOOL_BAR_CONTAINER: {
				MToolBarContainer mToolBarContainer = (MToolBarContainer)theEObject;
				T result = caseMToolBarContainer(mToolBarContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
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
	 * Returns the result of interpreting the object as an instance of '<em>MApplication</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MApplication</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <W extends MWindow<?>> T caseMApplication(MApplication<W> object) {
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
	 * Returns the result of interpreting the object as an instance of '<em>MStack</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MStack</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMStack(MStack object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MSash Form</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MSash Form</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <P extends MPart<?>> T caseMSashForm(MSashForm<P> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MContributed Part</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MContributed Part</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <P extends MPart<?>> T caseMContributedPart(MContributedPart<P> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MContribution</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MContribution</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMContribution(MContribution object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MHandler</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MHandler</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMHandler(MHandler object) {
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
	 * Returns the result of interpreting the object as an instance of '<em>MTool Bar Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MTool Bar Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMToolBarItem(MToolBarItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MItem Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MItem Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <I extends MItem> T caseMItemContainer(MItemContainer<I> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MMenu</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MMenu</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMMenu(MMenu object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MTool Bar</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MTool Bar</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMToolBar(MToolBar object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MTrimmed Part</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MTrimmed Part</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <P extends MPart<?>> T caseMTrimmedPart(MTrimmedPart<P> object) {
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
	 * Returns the result of interpreting the object as an instance of '<em>MCommand</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MCommand</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMCommand(MCommand object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>MTool Bar Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>MTool Bar Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMToolBarContainer(MToolBarContainer object) {
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

} //ApplicationSwitch
