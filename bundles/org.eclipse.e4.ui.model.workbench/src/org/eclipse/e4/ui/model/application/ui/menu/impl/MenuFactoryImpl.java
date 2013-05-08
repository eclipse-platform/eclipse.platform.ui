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
package org.eclipse.e4.ui.model.application.ui.menu.impl;

import org.eclipse.e4.ui.model.application.ui.menu.*;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
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
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class MenuFactoryImpl extends EFactoryImpl implements MMenuFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final MenuFactoryImpl eINSTANCE = init();

	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static MenuFactoryImpl init() {
		try {
			MenuFactoryImpl theMenuFactory = (MenuFactoryImpl)EPackage.Registry.INSTANCE.getEFactory(MenuPackageImpl.eNS_URI);
			if (theMenuFactory != null) {
				return theMenuFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new MenuFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MenuFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case MenuPackageImpl.MENU_SEPARATOR: return (EObject)createMenuSeparator();
			case MenuPackageImpl.MENU: return (EObject)createMenu();
			case MenuPackageImpl.MENU_CONTRIBUTION: return (EObject)createMenuContribution();
			case MenuPackageImpl.POPUP_MENU: return (EObject)createPopupMenu();
			case MenuPackageImpl.DIRECT_MENU_ITEM: return (EObject)createDirectMenuItem();
			case MenuPackageImpl.HANDLED_MENU_ITEM: return (EObject)createHandledMenuItem();
			case MenuPackageImpl.TOOL_BAR: return (EObject)createToolBar();
			case MenuPackageImpl.TOOL_CONTROL: return (EObject)createToolControl();
			case MenuPackageImpl.HANDLED_TOOL_ITEM: return (EObject)createHandledToolItem();
			case MenuPackageImpl.DIRECT_TOOL_ITEM: return (EObject)createDirectToolItem();
			case MenuPackageImpl.TOOL_BAR_SEPARATOR: return (EObject)createToolBarSeparator();
			case MenuPackageImpl.RENDERED_MENU: return (EObject)createRenderedMenu();
			case MenuPackageImpl.RENDERED_TOOL_BAR: return (EObject)createRenderedToolBar();
			case MenuPackageImpl.TOOL_BAR_CONTRIBUTION: return (EObject)createToolBarContribution();
			case MenuPackageImpl.TRIM_CONTRIBUTION: return (EObject)createTrimContribution();
			case MenuPackageImpl.RENDERED_MENU_ITEM: return (EObject)createRenderedMenuItem();
			case MenuPackageImpl.OPAQUE_TOOL_ITEM: return (EObject)createOpaqueToolItem();
			case MenuPackageImpl.OPAQUE_MENU_ITEM: return (EObject)createOpaqueMenuItem();
			case MenuPackageImpl.OPAQUE_MENU_SEPARATOR: return (EObject)createOpaqueMenuSeparator();
			case MenuPackageImpl.OPAQUE_MENU: return (EObject)createOpaqueMenu();
			case MenuPackageImpl.DYNAMIC_MENU_CONTRIBUTION: return (EObject)createDynamicMenuContribution();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case MenuPackageImpl.ITEM_TYPE:
				return createItemTypeFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case MenuPackageImpl.ITEM_TYPE:
				return convertItemTypeToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MMenuSeparator createMenuSeparator() {
		MenuSeparatorImpl menuSeparator = new MenuSeparatorImpl();
		return menuSeparator;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MMenu createMenu() {
		MenuImpl menu = new MenuImpl();
		return menu;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MMenuContribution createMenuContribution() {
		MenuContributionImpl menuContribution = new MenuContributionImpl();
		return menuContribution;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPopupMenu createPopupMenu() {
		PopupMenuImpl popupMenu = new PopupMenuImpl();
		return popupMenu;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDirectMenuItem createDirectMenuItem() {
		DirectMenuItemImpl directMenuItem = new DirectMenuItemImpl();
		return directMenuItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MHandledMenuItem createHandledMenuItem() {
		HandledMenuItemImpl handledMenuItem = new HandledMenuItemImpl();
		return handledMenuItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBar createToolBar() {
		ToolBarImpl toolBar = new ToolBarImpl();
		return toolBar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolControl createToolControl() {
		ToolControlImpl toolControl = new ToolControlImpl();
		return toolControl;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MHandledToolItem createHandledToolItem() {
		HandledToolItemImpl handledToolItem = new HandledToolItemImpl();
		return handledToolItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDirectToolItem createDirectToolItem() {
		DirectToolItemImpl directToolItem = new DirectToolItemImpl();
		return directToolItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBarSeparator createToolBarSeparator() {
		ToolBarSeparatorImpl toolBarSeparator = new ToolBarSeparatorImpl();
		return toolBarSeparator;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MRenderedMenu createRenderedMenu() {
		RenderedMenuImpl renderedMenu = new RenderedMenuImpl();
		return renderedMenu;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MRenderedToolBar createRenderedToolBar() {
		RenderedToolBarImpl renderedToolBar = new RenderedToolBarImpl();
		return renderedToolBar;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MToolBarContribution createToolBarContribution() {
		ToolBarContributionImpl toolBarContribution = new ToolBarContributionImpl();
		return toolBarContribution;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MTrimContribution createTrimContribution() {
		TrimContributionImpl trimContribution = new TrimContributionImpl();
		return trimContribution;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MRenderedMenuItem createRenderedMenuItem() {
		RenderedMenuItemImpl renderedMenuItem = new RenderedMenuItemImpl();
		return renderedMenuItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MOpaqueToolItem createOpaqueToolItem() {
		OpaqueToolItemImpl opaqueToolItem = new OpaqueToolItemImpl();
		return opaqueToolItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MOpaqueMenuItem createOpaqueMenuItem() {
		OpaqueMenuItemImpl opaqueMenuItem = new OpaqueMenuItemImpl();
		return opaqueMenuItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MOpaqueMenuSeparator createOpaqueMenuSeparator() {
		OpaqueMenuSeparatorImpl opaqueMenuSeparator = new OpaqueMenuSeparatorImpl();
		return opaqueMenuSeparator;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MOpaqueMenu createOpaqueMenu() {
		OpaqueMenuImpl opaqueMenu = new OpaqueMenuImpl();
		return opaqueMenu;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MDynamicMenuContribution createDynamicMenuContribution() {
		DynamicMenuContributionImpl dynamicMenuContribution = new DynamicMenuContributionImpl();
		return dynamicMenuContribution;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ItemType createItemTypeFromString(EDataType eDataType, String initialValue) {
		ItemType result = ItemType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertItemTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MenuPackageImpl getMenuPackage() {
		return (MenuPackageImpl)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static MenuPackageImpl getPackage() {
		return MenuPackageImpl.eINSTANCE;
	}

} //MenuFactoryImpl
