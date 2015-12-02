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
package org.eclipse.e4.ui.model.application.ui.util;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.*;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Switch;

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
 * @see org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl
 * @generated
 */
public class UiSwitch<T1> extends Switch<T1> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static UiPackageImpl modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public UiSwitch() {
		if (modelPackage == null) {
			modelPackage = UiPackageImpl.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T1 doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case UiPackageImpl.CONTEXT: {
				MContext context = (MContext)theEObject;
				T1 result = caseContext(context);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case UiPackageImpl.DIRTYABLE: {
				MDirtyable dirtyable = (MDirtyable)theEObject;
				T1 result = caseDirtyable(dirtyable);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case UiPackageImpl.INPUT: {
				MInput input = (MInput)theEObject;
				T1 result = caseInput(input);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case UiPackageImpl.UI_ELEMENT: {
				MUIElement uiElement = (MUIElement)theEObject;
				T1 result = caseUIElement(uiElement);
				if (result == null) result = caseApplicationElement(uiElement);
				if (result == null) result = caseLocalizable(uiElement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case UiPackageImpl.ELEMENT_CONTAINER: {
				MElementContainer<?> elementContainer = (MElementContainer<?>)theEObject;
				T1 result = caseElementContainer(elementContainer);
				if (result == null) result = caseUIElement(elementContainer);
				if (result == null) result = caseApplicationElement(elementContainer);
				if (result == null) result = caseLocalizable(elementContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case UiPackageImpl.UI_LABEL: {
				MUILabel uiLabel = (MUILabel)theEObject;
				T1 result = caseUILabel(uiLabel);
				if (result == null) result = caseLocalizable(uiLabel);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case UiPackageImpl.GENERIC_STACK: {
				MGenericStack<?> genericStack = (MGenericStack<?>)theEObject;
				T1 result = caseGenericStack(genericStack);
				if (result == null) result = caseElementContainer(genericStack);
				if (result == null) result = caseUIElement(genericStack);
				if (result == null) result = caseApplicationElement(genericStack);
				if (result == null) result = caseLocalizable(genericStack);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case UiPackageImpl.GENERIC_TILE: {
				MGenericTile<?> genericTile = (MGenericTile<?>)theEObject;
				T1 result = caseGenericTile(genericTile);
				if (result == null) result = caseElementContainer(genericTile);
				if (result == null) result = caseUIElement(genericTile);
				if (result == null) result = caseApplicationElement(genericTile);
				if (result == null) result = caseLocalizable(genericTile);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case UiPackageImpl.GENERIC_TRIM_CONTAINER: {
				MGenericTrimContainer<?> genericTrimContainer = (MGenericTrimContainer<?>)theEObject;
				T1 result = caseGenericTrimContainer(genericTrimContainer);
				if (result == null) result = caseElementContainer(genericTrimContainer);
				if (result == null) result = caseUIElement(genericTrimContainer);
				if (result == null) result = caseApplicationElement(genericTrimContainer);
				if (result == null) result = caseLocalizable(genericTrimContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case UiPackageImpl.EXPRESSION: {
				MExpression expression = (MExpression)theEObject;
				T1 result = caseExpression(expression);
				if (result == null) result = caseApplicationElement(expression);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case UiPackageImpl.CORE_EXPRESSION: {
				MCoreExpression coreExpression = (MCoreExpression)theEObject;
				T1 result = caseCoreExpression(coreExpression);
				if (result == null) result = caseExpression(coreExpression);
				if (result == null) result = caseApplicationElement(coreExpression);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case UiPackageImpl.SNIPPET_CONTAINER: {
				MSnippetContainer snippetContainer = (MSnippetContainer)theEObject;
				T1 result = caseSnippetContainer(snippetContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case UiPackageImpl.LOCALIZABLE: {
				MLocalizable localizable = (MLocalizable)theEObject;
				T1 result = caseLocalizable(localizable);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Context</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Context</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseContext(MContext object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Dirtyable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Dirtyable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseDirtyable(MDirtyable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Input</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Input</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseInput(MInput object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>UI Element</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>UI Element</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseUIElement(MUIElement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>UI Label</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>UI Label</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseUILabel(MUILabel object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Element Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Element Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T extends MUIElement> T1 caseElementContainer(MElementContainer<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Generic Stack</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Generic Stack</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T extends MUIElement> T1 caseGenericStack(MGenericStack<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Generic Tile</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Generic Tile</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T extends MUIElement> T1 caseGenericTile(MGenericTile<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Generic Trim Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Generic Trim Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T extends MUIElement> T1 caseGenericTrimContainer(MGenericTrimContainer<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Expression</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Expression</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseExpression(MExpression object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Core Expression</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Core Expression</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseCoreExpression(MCoreExpression object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Snippet Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Snippet Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseSnippetContainer(MSnippetContainer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Localizable</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Localizable</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseLocalizable(MLocalizable object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Element</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Element</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseApplicationElement(MApplicationElement object) {
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
	@Override
	public T1 defaultCase(EObject object) {
		return null;
	}

} //UiSwitch
