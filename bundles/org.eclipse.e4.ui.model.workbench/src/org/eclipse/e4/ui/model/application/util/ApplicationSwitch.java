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
 * $Id: ApplicationSwitch.java,v 1.1 2008/11/11 18:19:12 bbokowski Exp $
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
			case ApplicationPackage.APPLICATION_ELEMENT: {
				ApplicationElement applicationElement = (ApplicationElement)theEObject;
				T result = caseApplicationElement(applicationElement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.APPLICATION: {
				Application<?> application = (Application<?>)theEObject;
				T result = caseApplication(application);
				if (result == null) result = caseApplicationElement(application);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.PART: {
				Part<?> part = (Part<?>)theEObject;
				T result = casePart(part);
				if (result == null) result = caseApplicationElement(part);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.STACK: {
				Stack stack = (Stack)theEObject;
				T result = caseStack(stack);
				if (result == null) result = casePart(stack);
				if (result == null) result = caseApplicationElement(stack);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.SASH_FORM: {
				SashForm<?> sashForm = (SashForm<?>)theEObject;
				T result = caseSashForm(sashForm);
				if (result == null) result = casePart(sashForm);
				if (result == null) result = caseApplicationElement(sashForm);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.CONTRIBUTED_PART: {
				ContributedPart<?> contributedPart = (ContributedPart<?>)theEObject;
				T result = caseContributedPart(contributedPart);
				if (result == null) result = caseItemPart(contributedPart);
				if (result == null) result = caseContribution(contributedPart);
				if (result == null) result = casePart(contributedPart);
				if (result == null) result = caseItem(contributedPart);
				if (result == null) result = caseApplicationElement(contributedPart);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.CONTRIBUTION: {
				Contribution contribution = (Contribution)theEObject;
				T result = caseContribution(contribution);
				if (result == null) result = caseApplicationElement(contribution);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.HANDLER: {
				Handler handler = (Handler)theEObject;
				T result = caseHandler(handler);
				if (result == null) result = caseContribution(handler);
				if (result == null) result = caseApplicationElement(handler);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.ITEM: {
				Item item = (Item)theEObject;
				T result = caseItem(item);
				if (result == null) result = caseApplicationElement(item);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.HANDLED_ITEM: {
				HandledItem handledItem = (HandledItem)theEObject;
				T result = caseHandledItem(handledItem);
				if (result == null) result = caseItem(handledItem);
				if (result == null) result = caseApplicationElement(handledItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MENU_ITEM: {
				MenuItem menuItem = (MenuItem)theEObject;
				T result = caseMenuItem(menuItem);
				if (result == null) result = caseHandledItem(menuItem);
				if (result == null) result = caseItem(menuItem);
				if (result == null) result = caseApplicationElement(menuItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.TOOL_BAR_ITEM: {
				ToolBarItem toolBarItem = (ToolBarItem)theEObject;
				T result = caseToolBarItem(toolBarItem);
				if (result == null) result = caseHandledItem(toolBarItem);
				if (result == null) result = caseItem(toolBarItem);
				if (result == null) result = caseApplicationElement(toolBarItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.ITEM_CONTAINER: {
				ItemContainer<?> itemContainer = (ItemContainer<?>)theEObject;
				T result = caseItemContainer(itemContainer);
				if (result == null) result = caseApplicationElement(itemContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.MENU: {
				Menu menu = (Menu)theEObject;
				T result = caseMenu(menu);
				if (result == null) result = caseItemContainer(menu);
				if (result == null) result = caseApplicationElement(menu);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.TOOL_BAR: {
				ToolBar toolBar = (ToolBar)theEObject;
				T result = caseToolBar(toolBar);
				if (result == null) result = caseItemContainer(toolBar);
				if (result == null) result = caseApplicationElement(toolBar);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.TRIM: {
				Trim trim = (Trim)theEObject;
				T result = caseTrim(trim);
				if (result == null) result = caseApplicationElement(trim);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.ITEM_PART: {
				ItemPart<?> itemPart = (ItemPart<?>)theEObject;
				T result = caseItemPart(itemPart);
				if (result == null) result = casePart(itemPart);
				if (result == null) result = caseItem(itemPart);
				if (result == null) result = caseApplicationElement(itemPart);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.WINDOW: {
				Window<?> window = (Window<?>)theEObject;
				T result = caseWindow(window);
				if (result == null) result = caseItemPart(window);
				if (result == null) result = casePart(window);
				if (result == null) result = caseItem(window);
				if (result == null) result = caseApplicationElement(window);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplicationPackage.COMMAND: {
				Command command = (Command)theEObject;
				T result = caseCommand(command);
				if (result == null) result = caseApplicationElement(command);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
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
	public T caseApplicationElement(ApplicationElement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Application</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Application</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <W extends Window<?>> T caseApplication(Application<W> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Part</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Part</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <P extends Part<?>> T casePart(Part<P> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Stack</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Stack</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseStack(Stack object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Sash Form</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Sash Form</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <P extends Part<?>> T caseSashForm(SashForm<P> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Contributed Part</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Contributed Part</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <P extends Part<?>> T caseContributedPart(ContributedPart<P> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Contribution</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Contribution</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseContribution(Contribution object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Handler</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Handler</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHandler(Handler object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseItem(Item object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Handled Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Handled Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHandledItem(HandledItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Menu Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMenuItem(MenuItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tool Bar Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tool Bar Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseToolBarItem(ToolBarItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Item Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Item Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <I extends Item> T caseItemContainer(ItemContainer<I> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Menu</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Menu</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseMenu(Menu object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tool Bar</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tool Bar</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseToolBar(ToolBar object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Trim</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Trim</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTrim(Trim object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Item Part</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Item Part</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <P extends Part<?>> T caseItemPart(ItemPart<P> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Window</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Window</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <P extends Part<?>> T caseWindow(Window<P> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Command</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Command</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCommand(Command object) {
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
