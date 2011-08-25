/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.menu.util;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MOpaqueToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MRenderedToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
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
 * @see org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl
 * @generated
 */
public class MenuSwitch<T1> extends Switch<T1> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static MenuPackageImpl modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MenuSwitch() {
		if (modelPackage == null) {
			modelPackage = MenuPackageImpl.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @parameter ePackage the package in question.
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
			case MenuPackageImpl.ITEM: {
				MItem item = (MItem)theEObject;
				T1 result = caseItem(item);
				if (result == null) result = caseUIElement(item);
				if (result == null) result = caseUILabel(item);
				if (result == null) result = caseApplicationElement(item);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.HANDLED_ITEM: {
				MHandledItem handledItem = (MHandledItem)theEObject;
				T1 result = caseHandledItem(handledItem);
				if (result == null) result = caseItem(handledItem);
				if (result == null) result = caseUIElement(handledItem);
				if (result == null) result = caseUILabel(handledItem);
				if (result == null) result = caseApplicationElement(handledItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.MENU_ELEMENT: {
				MMenuElement menuElement = (MMenuElement)theEObject;
				T1 result = caseMenuElement(menuElement);
				if (result == null) result = caseUIElement(menuElement);
				if (result == null) result = caseUILabel(menuElement);
				if (result == null) result = caseApplicationElement(menuElement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.MENU_ITEM: {
				MMenuItem menuItem = (MMenuItem)theEObject;
				T1 result = caseMenuItem(menuItem);
				if (result == null) result = caseItem(menuItem);
				if (result == null) result = caseMenuElement(menuItem);
				if (result == null) result = caseUIElement(menuItem);
				if (result == null) result = caseUILabel(menuItem);
				if (result == null) result = caseApplicationElement(menuItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.MENU_SEPARATOR: {
				MMenuSeparator menuSeparator = (MMenuSeparator)theEObject;
				T1 result = caseMenuSeparator(menuSeparator);
				if (result == null) result = caseMenuElement(menuSeparator);
				if (result == null) result = caseUIElement(menuSeparator);
				if (result == null) result = caseUILabel(menuSeparator);
				if (result == null) result = caseApplicationElement(menuSeparator);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.MENU: {
				MMenu menu = (MMenu)theEObject;
				T1 result = caseMenu(menu);
				if (result == null) result = caseMenuElement(menu);
				if (result == null) result = caseElementContainer(menu);
				if (result == null) result = caseUIElement(menu);
				if (result == null) result = caseUILabel(menu);
				if (result == null) result = caseApplicationElement(menu);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.MENU_CONTRIBUTION: {
				MMenuContribution menuContribution = (MMenuContribution)theEObject;
				T1 result = caseMenuContribution(menuContribution);
				if (result == null) result = caseElementContainer(menuContribution);
				if (result == null) result = caseUIElement(menuContribution);
				if (result == null) result = caseApplicationElement(menuContribution);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.POPUP_MENU: {
				MPopupMenu popupMenu = (MPopupMenu)theEObject;
				T1 result = casePopupMenu(popupMenu);
				if (result == null) result = caseMenu(popupMenu);
				if (result == null) result = caseContext(popupMenu);
				if (result == null) result = caseMenuElement(popupMenu);
				if (result == null) result = caseElementContainer(popupMenu);
				if (result == null) result = caseUIElement(popupMenu);
				if (result == null) result = caseUILabel(popupMenu);
				if (result == null) result = caseApplicationElement(popupMenu);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.DIRECT_MENU_ITEM: {
				MDirectMenuItem directMenuItem = (MDirectMenuItem)theEObject;
				T1 result = caseDirectMenuItem(directMenuItem);
				if (result == null) result = caseMenuItem(directMenuItem);
				if (result == null) result = caseContribution(directMenuItem);
				if (result == null) result = caseItem(directMenuItem);
				if (result == null) result = caseMenuElement(directMenuItem);
				if (result == null) result = caseUIElement(directMenuItem);
				if (result == null) result = caseUILabel(directMenuItem);
				if (result == null) result = caseApplicationElement(directMenuItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.HANDLED_MENU_ITEM: {
				MHandledMenuItem handledMenuItem = (MHandledMenuItem)theEObject;
				T1 result = caseHandledMenuItem(handledMenuItem);
				if (result == null) result = caseMenuItem(handledMenuItem);
				if (result == null) result = caseHandledItem(handledMenuItem);
				if (result == null) result = caseItem(handledMenuItem);
				if (result == null) result = caseMenuElement(handledMenuItem);
				if (result == null) result = caseUIElement(handledMenuItem);
				if (result == null) result = caseUILabel(handledMenuItem);
				if (result == null) result = caseApplicationElement(handledMenuItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.TOOL_ITEM: {
				MToolItem toolItem = (MToolItem)theEObject;
				T1 result = caseToolItem(toolItem);
				if (result == null) result = caseItem(toolItem);
				if (result == null) result = caseToolBarElement(toolItem);
				if (result == null) result = caseUIElement(toolItem);
				if (result == null) result = caseUILabel(toolItem);
				if (result == null) result = caseApplicationElement(toolItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.TOOL_BAR: {
				MToolBar toolBar = (MToolBar)theEObject;
				T1 result = caseToolBar(toolBar);
				if (result == null) result = caseElementContainer(toolBar);
				if (result == null) result = caseTrimElement(toolBar);
				if (result == null) result = caseUIElement(toolBar);
				if (result == null) result = caseApplicationElement(toolBar);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.TOOL_BAR_ELEMENT: {
				MToolBarElement toolBarElement = (MToolBarElement)theEObject;
				T1 result = caseToolBarElement(toolBarElement);
				if (result == null) result = caseUIElement(toolBarElement);
				if (result == null) result = caseApplicationElement(toolBarElement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.TOOL_CONTROL: {
				MToolControl toolControl = (MToolControl)theEObject;
				T1 result = caseToolControl(toolControl);
				if (result == null) result = caseToolBarElement(toolControl);
				if (result == null) result = caseContribution(toolControl);
				if (result == null) result = caseTrimElement(toolControl);
				if (result == null) result = caseUIElement(toolControl);
				if (result == null) result = caseApplicationElement(toolControl);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.HANDLED_TOOL_ITEM: {
				MHandledToolItem handledToolItem = (MHandledToolItem)theEObject;
				T1 result = caseHandledToolItem(handledToolItem);
				if (result == null) result = caseToolItem(handledToolItem);
				if (result == null) result = caseHandledItem(handledToolItem);
				if (result == null) result = caseItem(handledToolItem);
				if (result == null) result = caseToolBarElement(handledToolItem);
				if (result == null) result = caseUIElement(handledToolItem);
				if (result == null) result = caseUILabel(handledToolItem);
				if (result == null) result = caseApplicationElement(handledToolItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.DIRECT_TOOL_ITEM: {
				MDirectToolItem directToolItem = (MDirectToolItem)theEObject;
				T1 result = caseDirectToolItem(directToolItem);
				if (result == null) result = caseToolItem(directToolItem);
				if (result == null) result = caseContribution(directToolItem);
				if (result == null) result = caseItem(directToolItem);
				if (result == null) result = caseToolBarElement(directToolItem);
				if (result == null) result = caseUIElement(directToolItem);
				if (result == null) result = caseUILabel(directToolItem);
				if (result == null) result = caseApplicationElement(directToolItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.TOOL_BAR_SEPARATOR: {
				MToolBarSeparator toolBarSeparator = (MToolBarSeparator)theEObject;
				T1 result = caseToolBarSeparator(toolBarSeparator);
				if (result == null) result = caseToolBarElement(toolBarSeparator);
				if (result == null) result = caseUIElement(toolBarSeparator);
				if (result == null) result = caseApplicationElement(toolBarSeparator);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.MENU_CONTRIBUTIONS: {
				MMenuContributions menuContributions = (MMenuContributions)theEObject;
				T1 result = caseMenuContributions(menuContributions);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.RENDERED_MENU: {
				MRenderedMenu renderedMenu = (MRenderedMenu)theEObject;
				T1 result = caseRenderedMenu(renderedMenu);
				if (result == null) result = caseMenu(renderedMenu);
				if (result == null) result = caseMenuElement(renderedMenu);
				if (result == null) result = caseElementContainer(renderedMenu);
				if (result == null) result = caseUIElement(renderedMenu);
				if (result == null) result = caseUILabel(renderedMenu);
				if (result == null) result = caseApplicationElement(renderedMenu);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.RENDERED_TOOL_BAR: {
				MRenderedToolBar renderedToolBar = (MRenderedToolBar)theEObject;
				T1 result = caseRenderedToolBar(renderedToolBar);
				if (result == null) result = caseToolBar(renderedToolBar);
				if (result == null) result = caseElementContainer(renderedToolBar);
				if (result == null) result = caseTrimElement(renderedToolBar);
				if (result == null) result = caseUIElement(renderedToolBar);
				if (result == null) result = caseApplicationElement(renderedToolBar);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.TOOL_BAR_CONTRIBUTION: {
				MToolBarContribution toolBarContribution = (MToolBarContribution)theEObject;
				T1 result = caseToolBarContribution(toolBarContribution);
				if (result == null) result = caseElementContainer(toolBarContribution);
				if (result == null) result = caseUIElement(toolBarContribution);
				if (result == null) result = caseApplicationElement(toolBarContribution);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.TOOL_BAR_CONTRIBUTIONS: {
				MToolBarContributions toolBarContributions = (MToolBarContributions)theEObject;
				T1 result = caseToolBarContributions(toolBarContributions);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.TRIM_CONTRIBUTION: {
				MTrimContribution trimContribution = (MTrimContribution)theEObject;
				T1 result = caseTrimContribution(trimContribution);
				if (result == null) result = caseElementContainer(trimContribution);
				if (result == null) result = caseUIElement(trimContribution);
				if (result == null) result = caseApplicationElement(trimContribution);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.TRIM_CONTRIBUTIONS: {
				MTrimContributions trimContributions = (MTrimContributions)theEObject;
				T1 result = caseTrimContributions(trimContributions);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.RENDERED_MENU_ITEM: {
				MRenderedMenuItem renderedMenuItem = (MRenderedMenuItem)theEObject;
				T1 result = caseRenderedMenuItem(renderedMenuItem);
				if (result == null) result = caseMenuItem(renderedMenuItem);
				if (result == null) result = caseItem(renderedMenuItem);
				if (result == null) result = caseMenuElement(renderedMenuItem);
				if (result == null) result = caseUIElement(renderedMenuItem);
				if (result == null) result = caseUILabel(renderedMenuItem);
				if (result == null) result = caseApplicationElement(renderedMenuItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.OPAQUE_TOOL_ITEM: {
				MOpaqueToolItem opaqueToolItem = (MOpaqueToolItem)theEObject;
				T1 result = caseOpaqueToolItem(opaqueToolItem);
				if (result == null) result = caseToolItem(opaqueToolItem);
				if (result == null) result = caseItem(opaqueToolItem);
				if (result == null) result = caseToolBarElement(opaqueToolItem);
				if (result == null) result = caseUIElement(opaqueToolItem);
				if (result == null) result = caseUILabel(opaqueToolItem);
				if (result == null) result = caseApplicationElement(opaqueToolItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.OPAQUE_MENU_ITEM: {
				MOpaqueMenuItem opaqueMenuItem = (MOpaqueMenuItem)theEObject;
				T1 result = caseOpaqueMenuItem(opaqueMenuItem);
				if (result == null) result = caseMenuItem(opaqueMenuItem);
				if (result == null) result = caseItem(opaqueMenuItem);
				if (result == null) result = caseMenuElement(opaqueMenuItem);
				if (result == null) result = caseUIElement(opaqueMenuItem);
				if (result == null) result = caseUILabel(opaqueMenuItem);
				if (result == null) result = caseApplicationElement(opaqueMenuItem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.OPAQUE_MENU_SEPARATOR: {
				MOpaqueMenuSeparator opaqueMenuSeparator = (MOpaqueMenuSeparator)theEObject;
				T1 result = caseOpaqueMenuSeparator(opaqueMenuSeparator);
				if (result == null) result = caseMenuSeparator(opaqueMenuSeparator);
				if (result == null) result = caseMenuElement(opaqueMenuSeparator);
				if (result == null) result = caseUIElement(opaqueMenuSeparator);
				if (result == null) result = caseUILabel(opaqueMenuSeparator);
				if (result == null) result = caseApplicationElement(opaqueMenuSeparator);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case MenuPackageImpl.OPAQUE_MENU: {
				MOpaqueMenu opaqueMenu = (MOpaqueMenu)theEObject;
				T1 result = caseOpaqueMenu(opaqueMenu);
				if (result == null) result = caseMenu(opaqueMenu);
				if (result == null) result = caseMenuElement(opaqueMenu);
				if (result == null) result = caseElementContainer(opaqueMenu);
				if (result == null) result = caseUIElement(opaqueMenu);
				if (result == null) result = caseUILabel(opaqueMenu);
				if (result == null) result = caseApplicationElement(opaqueMenu);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
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
	public T1 caseItem(MItem object) {
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
	public T1 caseHandledItem(MHandledItem object) {
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
	public T1 caseMenuElement(MMenuElement object) {
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
	public T1 caseMenuItem(MMenuItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Separator</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Separator</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseMenuSeparator(MMenuSeparator object) {
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
	public T1 caseMenu(MMenu object) {
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
	public T1 caseMenuContribution(MMenuContribution object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Popup Menu</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Popup Menu</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 casePopupMenu(MPopupMenu object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Direct Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Direct Menu Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseDirectMenuItem(MDirectMenuItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Handled Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Handled Menu Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseHandledMenuItem(MHandledMenuItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tool Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseToolItem(MToolItem object) {
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
	public T1 caseToolBar(MToolBar object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tool Bar Element</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tool Bar Element</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseToolBarElement(MToolBarElement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tool Control</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tool Control</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseToolControl(MToolControl object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Handled Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Handled Tool Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseHandledToolItem(MHandledToolItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Direct Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Direct Tool Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseDirectToolItem(MDirectToolItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tool Bar Separator</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tool Bar Separator</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseToolBarSeparator(MToolBarSeparator object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Contributions</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Contributions</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseMenuContributions(MMenuContributions object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Rendered Menu</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Rendered Menu</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseRenderedMenu(MRenderedMenu object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Rendered Tool Bar</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Rendered Tool Bar</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseRenderedToolBar(MRenderedToolBar object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tool Bar Contribution</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tool Bar Contribution</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseToolBarContribution(MToolBarContribution object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tool Bar Contributions</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tool Bar Contributions</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseToolBarContributions(MToolBarContributions object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Trim Contribution</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Trim Contribution</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseTrimContribution(MTrimContribution object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Trim Contributions</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Trim Contributions</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseTrimContributions(MTrimContributions object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Rendered Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Rendered Menu Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseRenderedMenuItem(MRenderedMenuItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Opaque Tool Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Opaque Tool Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseOpaqueToolItem(MOpaqueToolItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Opaque Menu Item</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Opaque Menu Item</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseOpaqueMenuItem(MOpaqueMenuItem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Opaque Menu Separator</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Opaque Menu Separator</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseOpaqueMenuSeparator(MOpaqueMenuSeparator object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Opaque Menu</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Opaque Menu</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseOpaqueMenu(MOpaqueMenu object) {
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
	public T1 caseContribution(MContribution object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Trim Element</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Trim Element</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseTrimElement(MTrimElement object) {
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

} //MenuSwitch
