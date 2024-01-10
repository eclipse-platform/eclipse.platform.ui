/**
 * <copyright>
 * </copyright>
 *
 * $Id: XpathtestFactoryImpl.java,v 1.1 2010/11/06 13:43:10 tschindl Exp $
 */
package org.eclipse.e4.emf.xpath.test.model.xpathtest.impl;

import org.eclipse.e4.emf.xpath.test.model.xpathtest.ExtendedNode;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.Menu;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.MenuElement;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.MenuItem;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.Node;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.Root;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestFactory;
import org.eclipse.e4.emf.xpath.test.model.xpathtest.XpathtestPackage;
import org.eclipse.emf.ecore.EClass;
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
public class XpathtestFactoryImpl extends EFactoryImpl implements XpathtestFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static XpathtestFactory init() {
		try {
			XpathtestFactory theXpathtestFactory = (XpathtestFactory)EPackage.Registry.INSTANCE.getEFactory(XpathtestPackage.eNS_URI);
			if (theXpathtestFactory != null) {
				return theXpathtestFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new XpathtestFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public XpathtestFactoryImpl() {
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
			case XpathtestPackage.ROOT: return createRoot();
			case XpathtestPackage.NODE: return createNode();
			case XpathtestPackage.EXTENDED_NODE: return createExtendedNode();
			case XpathtestPackage.MENU: return createMenu();
			case XpathtestPackage.MENU_ITEM: return createMenuItem();
			case XpathtestPackage.MENU_ELEMENT: return createMenuElement();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Root createRoot() {
		RootImpl root = new RootImpl();
		return root;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Node createNode() {
		NodeImpl node = new NodeImpl();
		return node;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ExtendedNode createExtendedNode() {
		ExtendedNodeImpl extendedNode = new ExtendedNodeImpl();
		return extendedNode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Menu createMenu() {
		MenuImpl menu = new MenuImpl();
		return menu;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MenuItem createMenuItem() {
		MenuItemImpl menuItem = new MenuItemImpl();
		return menuItem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MenuElement createMenuElement() {
		MenuElementImpl menuElement = new MenuElementImpl();
		return menuElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public XpathtestPackage getXpathtestPackage() {
		return (XpathtestPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static XpathtestPackage getPackage() {
		return XpathtestPackage.eINSTANCE;
	}

} //XpathtestFactoryImpl
