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
package org.eclipse.e4.ui.model.application.impl;

import java.util.Collection;

import org.eclipse.e4.core.services.context.IEclipseContext;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MBindingContainer;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MHandler;
import org.eclipse.e4.ui.model.application.MHandlerContainer;
import org.eclipse.e4.ui.model.application.MKeyBinding;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MPSCElement;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Window</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getWidget <em>Widget</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getRenderer <em>Renderer</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#isToBeRendered <em>To Be Rendered</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#isOnTop <em>On Top</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#isVisible <em>Visible</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getActiveChild <em>Active Child</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getContext <em>Context</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getVariables <em>Variables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getHandlers <em>Handlers</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getBindings <em>Bindings</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getMainMenu <em>Main Menu</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getX <em>X</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getY <em>Y</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getWidth <em>Width</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.WindowImpl#getHeight <em>Height</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class WindowImpl extends UILabelImpl implements MWindow {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getWidget() <em>Widget</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWidget()
	 * @generated
	 * @ordered
	 */
	protected static final Object WIDGET_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getWidget() <em>Widget</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWidget()
	 * @generated
	 * @ordered
	 */
	protected Object widget = WIDGET_EDEFAULT;

	/**
	 * The default value of the '{@link #getRenderer() <em>Renderer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRenderer()
	 * @generated
	 * @ordered
	 */
	protected static final Object RENDERER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRenderer() <em>Renderer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRenderer()
	 * @generated
	 * @ordered
	 */
	protected Object renderer = RENDERER_EDEFAULT;

	/**
	 * The default value of the '{@link #isToBeRendered() <em>To Be Rendered</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isToBeRendered()
	 * @generated
	 * @ordered
	 */
	protected static final boolean TO_BE_RENDERED_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isToBeRendered() <em>To Be Rendered</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isToBeRendered()
	 * @generated
	 * @ordered
	 */
	protected boolean toBeRendered = TO_BE_RENDERED_EDEFAULT;

	/**
	 * The default value of the '{@link #isOnTop() <em>On Top</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOnTop()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ON_TOP_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isOnTop() <em>On Top</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOnTop()
	 * @generated
	 * @ordered
	 */
	protected boolean onTop = ON_TOP_EDEFAULT;

	/**
	 * The default value of the '{@link #isVisible() <em>Visible</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isVisible()
	 * @generated
	 * @ordered
	 */
	protected static final boolean VISIBLE_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isVisible() <em>Visible</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isVisible()
	 * @generated
	 * @ordered
	 */
	protected boolean visible = VISIBLE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getChildren() <em>Children</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChildren()
	 * @generated
	 * @ordered
	 */
	protected EList<MPSCElement> children;

	/**
	 * The cached value of the '{@link #getActiveChild() <em>Active Child</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getActiveChild()
	 * @generated
	 * @ordered
	 */
	protected MPSCElement activeChild;

	/**
	 * The default value of the '{@link #getContext() <em>Context</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContext()
	 * @generated
	 * @ordered
	 */
	protected static final IEclipseContext CONTEXT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getContext() <em>Context</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContext()
	 * @generated
	 * @ordered
	 */
	protected IEclipseContext context = CONTEXT_EDEFAULT;

	/**
	 * The cached value of the '{@link #getVariables() <em>Variables</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVariables()
	 * @generated
	 * @ordered
	 */
	protected EList<String> variables;

	/**
	 * The cached value of the '{@link #getHandlers() <em>Handlers</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHandlers()
	 * @generated
	 * @ordered
	 */
	protected EList<MHandler> handlers;

	/**
	 * The cached value of the '{@link #getBindings() <em>Bindings</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindings()
	 * @generated
	 * @ordered
	 */
	protected EList<MKeyBinding> bindings;

	/**
	 * The cached value of the '{@link #getMainMenu() <em>Main Menu</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMainMenu()
	 * @generated
	 * @ordered
	 */
	protected MMenu mainMenu;

	/**
	 * The default value of the '{@link #getX() <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX()
	 * @generated
	 * @ordered
	 */
	protected static final int X_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getX() <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getX()
	 * @generated
	 * @ordered
	 */
	protected int x = X_EDEFAULT;

	/**
	 * The default value of the '{@link #getY() <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY()
	 * @generated
	 * @ordered
	 */
	protected static final int Y_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getY() <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY()
	 * @generated
	 * @ordered
	 */
	protected int y = Y_EDEFAULT;

	/**
	 * The default value of the '{@link #getWidth() <em>Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWidth()
	 * @generated
	 * @ordered
	 */
	protected static final int WIDTH_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getWidth() <em>Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWidth()
	 * @generated
	 * @ordered
	 */
	protected int width = WIDTH_EDEFAULT;

	/**
	 * The default value of the '{@link #getHeight() <em>Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHeight()
	 * @generated
	 * @ordered
	 */
	protected static final int HEIGHT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getHeight() <em>Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHeight()
	 * @generated
	 * @ordered
	 */
	protected int height = HEIGHT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected WindowImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MApplicationPackage.Literals.WINDOW;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getWidget() {
		return widget;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWidget(Object newWidget) {
		Object oldWidget = widget;
		widget = newWidget;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__WIDGET, oldWidget, widget));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object getRenderer() {
		return renderer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRenderer(Object newRenderer) {
		Object oldRenderer = renderer;
		renderer = newRenderer;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__RENDERER, oldRenderer, renderer));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isToBeRendered() {
		return toBeRendered;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setToBeRendered(boolean newToBeRendered) {
		boolean oldToBeRendered = toBeRendered;
		toBeRendered = newToBeRendered;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__TO_BE_RENDERED, oldToBeRendered, toBeRendered));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isOnTop() {
		return onTop;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOnTop(boolean newOnTop) {
		boolean oldOnTop = onTop;
		onTop = newOnTop;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__ON_TOP, oldOnTop, onTop));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setVisible(boolean newVisible) {
		boolean oldVisible = visible;
		visible = newVisible;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__VISIBLE, oldVisible, visible));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public MElementContainer<MUIElement> getParent() {
		if (eContainerFeatureID() != MApplicationPackage.WINDOW__PARENT) return null;
		return (MElementContainer<MUIElement>)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetParent(MElementContainer<MUIElement> newParent, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newParent, MApplicationPackage.WINDOW__PARENT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParent(MElementContainer<MUIElement> newParent) {
		if (newParent != eInternalContainer() || (eContainerFeatureID() != MApplicationPackage.WINDOW__PARENT && newParent != null)) {
			if (EcoreUtil.isAncestor(this, (EObject)newParent))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString()); //$NON-NLS-1$
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newParent != null)
				msgs = ((InternalEObject)newParent).eInverseAdd(this, MApplicationPackage.ELEMENT_CONTAINER__CHILDREN, MElementContainer.class, msgs);
			msgs = basicSetParent(newParent, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__PARENT, newParent, newParent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MPSCElement> getChildren() {
		if (children == null) {
			children = new EObjectContainmentWithInverseEList<MPSCElement>(MUIElement.class, this, MApplicationPackage.WINDOW__CHILDREN, MApplicationPackage.UI_ELEMENT__PARENT);
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPSCElement getActiveChild() {
		if (activeChild != null && ((EObject)activeChild).eIsProxy()) {
			InternalEObject oldActiveChild = (InternalEObject)activeChild;
			activeChild = (MPSCElement)eResolveProxy(oldActiveChild);
			if (activeChild != oldActiveChild) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, MApplicationPackage.WINDOW__ACTIVE_CHILD, oldActiveChild, activeChild));
			}
		}
		return activeChild;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MPSCElement basicGetActiveChild() {
		return activeChild;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setActiveChild(MPSCElement newActiveChild) {
		MPSCElement oldActiveChild = activeChild;
		activeChild = newActiveChild;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__ACTIVE_CHILD, oldActiveChild, activeChild));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public IEclipseContext getContext() {
		return context;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setContext(IEclipseContext newContext) {
		IEclipseContext oldContext = context;
		context = newContext;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__CONTEXT, oldContext, context));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<String> getVariables() {
		if (variables == null) {
			variables = new EDataTypeUniqueEList<String>(String.class, this, MApplicationPackage.WINDOW__VARIABLES);
		}
		return variables;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MHandler> getHandlers() {
		if (handlers == null) {
			handlers = new EObjectContainmentEList<MHandler>(MHandler.class, this, MApplicationPackage.WINDOW__HANDLERS);
		}
		return handlers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<MKeyBinding> getBindings() {
		if (bindings == null) {
			bindings = new EObjectContainmentEList<MKeyBinding>(MKeyBinding.class, this, MApplicationPackage.WINDOW__BINDINGS);
		}
		return bindings;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public MMenu getMainMenu() {
		return mainMenu;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetMainMenu(MMenu newMainMenu, NotificationChain msgs) {
		MMenu oldMainMenu = mainMenu;
		mainMenu = newMainMenu;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__MAIN_MENU, oldMainMenu, newMainMenu);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMainMenu(MMenu newMainMenu) {
		if (newMainMenu != mainMenu) {
			NotificationChain msgs = null;
			if (mainMenu != null)
				msgs = ((InternalEObject)mainMenu).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MApplicationPackage.WINDOW__MAIN_MENU, null, msgs);
			if (newMainMenu != null)
				msgs = ((InternalEObject)newMainMenu).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MApplicationPackage.WINDOW__MAIN_MENU, null, msgs);
			msgs = basicSetMainMenu(newMainMenu, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__MAIN_MENU, newMainMenu, newMainMenu));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getX() {
		return x;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setX(int newX) {
		int oldX = x;
		x = newX;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__X, oldX, x));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getY() {
		return y;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setY(int newY) {
		int oldY = y;
		y = newY;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__Y, oldY, y));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWidth(int newWidth) {
		int oldWidth = width;
		width = newWidth;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__WIDTH, oldWidth, width));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setHeight(int newHeight) {
		int oldHeight = height;
		height = newHeight;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MApplicationPackage.WINDOW__HEIGHT, oldHeight, height));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case MApplicationPackage.WINDOW__PARENT:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetParent((MElementContainer<MUIElement>)otherEnd, msgs);
			case MApplicationPackage.WINDOW__CHILDREN:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getChildren()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case MApplicationPackage.WINDOW__PARENT:
				return basicSetParent(null, msgs);
			case MApplicationPackage.WINDOW__CHILDREN:
				return ((InternalEList<?>)getChildren()).basicRemove(otherEnd, msgs);
			case MApplicationPackage.WINDOW__HANDLERS:
				return ((InternalEList<?>)getHandlers()).basicRemove(otherEnd, msgs);
			case MApplicationPackage.WINDOW__BINDINGS:
				return ((InternalEList<?>)getBindings()).basicRemove(otherEnd, msgs);
			case MApplicationPackage.WINDOW__MAIN_MENU:
				return basicSetMainMenu(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case MApplicationPackage.WINDOW__PARENT:
				return eInternalContainer().eInverseRemove(this, MApplicationPackage.ELEMENT_CONTAINER__CHILDREN, MElementContainer.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MApplicationPackage.WINDOW__ID:
				return getId();
			case MApplicationPackage.WINDOW__WIDGET:
				return getWidget();
			case MApplicationPackage.WINDOW__RENDERER:
				return getRenderer();
			case MApplicationPackage.WINDOW__TO_BE_RENDERED:
				return isToBeRendered();
			case MApplicationPackage.WINDOW__ON_TOP:
				return isOnTop();
			case MApplicationPackage.WINDOW__VISIBLE:
				return isVisible();
			case MApplicationPackage.WINDOW__PARENT:
				return getParent();
			case MApplicationPackage.WINDOW__CHILDREN:
				return getChildren();
			case MApplicationPackage.WINDOW__ACTIVE_CHILD:
				if (resolve) return getActiveChild();
				return basicGetActiveChild();
			case MApplicationPackage.WINDOW__CONTEXT:
				return getContext();
			case MApplicationPackage.WINDOW__VARIABLES:
				return getVariables();
			case MApplicationPackage.WINDOW__HANDLERS:
				return getHandlers();
			case MApplicationPackage.WINDOW__BINDINGS:
				return getBindings();
			case MApplicationPackage.WINDOW__MAIN_MENU:
				return getMainMenu();
			case MApplicationPackage.WINDOW__X:
				return getX();
			case MApplicationPackage.WINDOW__Y:
				return getY();
			case MApplicationPackage.WINDOW__WIDTH:
				return getWidth();
			case MApplicationPackage.WINDOW__HEIGHT:
				return getHeight();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case MApplicationPackage.WINDOW__ID:
				setId((String)newValue);
				return;
			case MApplicationPackage.WINDOW__WIDGET:
				setWidget(newValue);
				return;
			case MApplicationPackage.WINDOW__RENDERER:
				setRenderer(newValue);
				return;
			case MApplicationPackage.WINDOW__TO_BE_RENDERED:
				setToBeRendered((Boolean)newValue);
				return;
			case MApplicationPackage.WINDOW__ON_TOP:
				setOnTop((Boolean)newValue);
				return;
			case MApplicationPackage.WINDOW__VISIBLE:
				setVisible((Boolean)newValue);
				return;
			case MApplicationPackage.WINDOW__PARENT:
				setParent((MElementContainer<MUIElement>)newValue);
				return;
			case MApplicationPackage.WINDOW__CHILDREN:
				getChildren().clear();
				getChildren().addAll((Collection<? extends MPSCElement>)newValue);
				return;
			case MApplicationPackage.WINDOW__ACTIVE_CHILD:
				setActiveChild((MPSCElement)newValue);
				return;
			case MApplicationPackage.WINDOW__CONTEXT:
				setContext((IEclipseContext)newValue);
				return;
			case MApplicationPackage.WINDOW__VARIABLES:
				getVariables().clear();
				getVariables().addAll((Collection<? extends String>)newValue);
				return;
			case MApplicationPackage.WINDOW__HANDLERS:
				getHandlers().clear();
				getHandlers().addAll((Collection<? extends MHandler>)newValue);
				return;
			case MApplicationPackage.WINDOW__BINDINGS:
				getBindings().clear();
				getBindings().addAll((Collection<? extends MKeyBinding>)newValue);
				return;
			case MApplicationPackage.WINDOW__MAIN_MENU:
				setMainMenu((MMenu)newValue);
				return;
			case MApplicationPackage.WINDOW__X:
				setX((Integer)newValue);
				return;
			case MApplicationPackage.WINDOW__Y:
				setY((Integer)newValue);
				return;
			case MApplicationPackage.WINDOW__WIDTH:
				setWidth((Integer)newValue);
				return;
			case MApplicationPackage.WINDOW__HEIGHT:
				setHeight((Integer)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case MApplicationPackage.WINDOW__ID:
				setId(ID_EDEFAULT);
				return;
			case MApplicationPackage.WINDOW__WIDGET:
				setWidget(WIDGET_EDEFAULT);
				return;
			case MApplicationPackage.WINDOW__RENDERER:
				setRenderer(RENDERER_EDEFAULT);
				return;
			case MApplicationPackage.WINDOW__TO_BE_RENDERED:
				setToBeRendered(TO_BE_RENDERED_EDEFAULT);
				return;
			case MApplicationPackage.WINDOW__ON_TOP:
				setOnTop(ON_TOP_EDEFAULT);
				return;
			case MApplicationPackage.WINDOW__VISIBLE:
				setVisible(VISIBLE_EDEFAULT);
				return;
			case MApplicationPackage.WINDOW__PARENT:
				setParent((MElementContainer<MUIElement>)null);
				return;
			case MApplicationPackage.WINDOW__CHILDREN:
				getChildren().clear();
				return;
			case MApplicationPackage.WINDOW__ACTIVE_CHILD:
				setActiveChild((MPSCElement)null);
				return;
			case MApplicationPackage.WINDOW__CONTEXT:
				setContext(CONTEXT_EDEFAULT);
				return;
			case MApplicationPackage.WINDOW__VARIABLES:
				getVariables().clear();
				return;
			case MApplicationPackage.WINDOW__HANDLERS:
				getHandlers().clear();
				return;
			case MApplicationPackage.WINDOW__BINDINGS:
				getBindings().clear();
				return;
			case MApplicationPackage.WINDOW__MAIN_MENU:
				setMainMenu((MMenu)null);
				return;
			case MApplicationPackage.WINDOW__X:
				setX(X_EDEFAULT);
				return;
			case MApplicationPackage.WINDOW__Y:
				setY(Y_EDEFAULT);
				return;
			case MApplicationPackage.WINDOW__WIDTH:
				setWidth(WIDTH_EDEFAULT);
				return;
			case MApplicationPackage.WINDOW__HEIGHT:
				setHeight(HEIGHT_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case MApplicationPackage.WINDOW__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case MApplicationPackage.WINDOW__WIDGET:
				return WIDGET_EDEFAULT == null ? widget != null : !WIDGET_EDEFAULT.equals(widget);
			case MApplicationPackage.WINDOW__RENDERER:
				return RENDERER_EDEFAULT == null ? renderer != null : !RENDERER_EDEFAULT.equals(renderer);
			case MApplicationPackage.WINDOW__TO_BE_RENDERED:
				return toBeRendered != TO_BE_RENDERED_EDEFAULT;
			case MApplicationPackage.WINDOW__ON_TOP:
				return onTop != ON_TOP_EDEFAULT;
			case MApplicationPackage.WINDOW__VISIBLE:
				return visible != VISIBLE_EDEFAULT;
			case MApplicationPackage.WINDOW__PARENT:
				return getParent() != null;
			case MApplicationPackage.WINDOW__CHILDREN:
				return children != null && !children.isEmpty();
			case MApplicationPackage.WINDOW__ACTIVE_CHILD:
				return activeChild != null;
			case MApplicationPackage.WINDOW__CONTEXT:
				return CONTEXT_EDEFAULT == null ? context != null : !CONTEXT_EDEFAULT.equals(context);
			case MApplicationPackage.WINDOW__VARIABLES:
				return variables != null && !variables.isEmpty();
			case MApplicationPackage.WINDOW__HANDLERS:
				return handlers != null && !handlers.isEmpty();
			case MApplicationPackage.WINDOW__BINDINGS:
				return bindings != null && !bindings.isEmpty();
			case MApplicationPackage.WINDOW__MAIN_MENU:
				return mainMenu != null;
			case MApplicationPackage.WINDOW__X:
				return x != X_EDEFAULT;
			case MApplicationPackage.WINDOW__Y:
				return y != Y_EDEFAULT;
			case MApplicationPackage.WINDOW__WIDTH:
				return width != WIDTH_EDEFAULT;
			case MApplicationPackage.WINDOW__HEIGHT:
				return height != HEIGHT_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == MApplicationElement.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.WINDOW__ID: return MApplicationPackage.APPLICATION_ELEMENT__ID;
				default: return -1;
			}
		}
		if (baseClass == MUIElement.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.WINDOW__WIDGET: return MApplicationPackage.UI_ELEMENT__WIDGET;
				case MApplicationPackage.WINDOW__RENDERER: return MApplicationPackage.UI_ELEMENT__RENDERER;
				case MApplicationPackage.WINDOW__TO_BE_RENDERED: return MApplicationPackage.UI_ELEMENT__TO_BE_RENDERED;
				case MApplicationPackage.WINDOW__ON_TOP: return MApplicationPackage.UI_ELEMENT__ON_TOP;
				case MApplicationPackage.WINDOW__VISIBLE: return MApplicationPackage.UI_ELEMENT__VISIBLE;
				case MApplicationPackage.WINDOW__PARENT: return MApplicationPackage.UI_ELEMENT__PARENT;
				default: return -1;
			}
		}
		if (baseClass == MElementContainer.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.WINDOW__CHILDREN: return MApplicationPackage.ELEMENT_CONTAINER__CHILDREN;
				case MApplicationPackage.WINDOW__ACTIVE_CHILD: return MApplicationPackage.ELEMENT_CONTAINER__ACTIVE_CHILD;
				default: return -1;
			}
		}
		if (baseClass == MContext.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.WINDOW__CONTEXT: return MApplicationPackage.CONTEXT__CONTEXT;
				case MApplicationPackage.WINDOW__VARIABLES: return MApplicationPackage.CONTEXT__VARIABLES;
				default: return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.WINDOW__HANDLERS: return MApplicationPackage.HANDLER_CONTAINER__HANDLERS;
				default: return -1;
			}
		}
		if (baseClass == MBindingContainer.class) {
			switch (derivedFeatureID) {
				case MApplicationPackage.WINDOW__BINDINGS: return MApplicationPackage.BINDING_CONTAINER__BINDINGS;
				default: return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == MApplicationElement.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.APPLICATION_ELEMENT__ID: return MApplicationPackage.WINDOW__ID;
				default: return -1;
			}
		}
		if (baseClass == MUIElement.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.UI_ELEMENT__WIDGET: return MApplicationPackage.WINDOW__WIDGET;
				case MApplicationPackage.UI_ELEMENT__RENDERER: return MApplicationPackage.WINDOW__RENDERER;
				case MApplicationPackage.UI_ELEMENT__TO_BE_RENDERED: return MApplicationPackage.WINDOW__TO_BE_RENDERED;
				case MApplicationPackage.UI_ELEMENT__ON_TOP: return MApplicationPackage.WINDOW__ON_TOP;
				case MApplicationPackage.UI_ELEMENT__VISIBLE: return MApplicationPackage.WINDOW__VISIBLE;
				case MApplicationPackage.UI_ELEMENT__PARENT: return MApplicationPackage.WINDOW__PARENT;
				default: return -1;
			}
		}
		if (baseClass == MElementContainer.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.ELEMENT_CONTAINER__CHILDREN: return MApplicationPackage.WINDOW__CHILDREN;
				case MApplicationPackage.ELEMENT_CONTAINER__ACTIVE_CHILD: return MApplicationPackage.WINDOW__ACTIVE_CHILD;
				default: return -1;
			}
		}
		if (baseClass == MContext.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.CONTEXT__CONTEXT: return MApplicationPackage.WINDOW__CONTEXT;
				case MApplicationPackage.CONTEXT__VARIABLES: return MApplicationPackage.WINDOW__VARIABLES;
				default: return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.HANDLER_CONTAINER__HANDLERS: return MApplicationPackage.WINDOW__HANDLERS;
				default: return -1;
			}
		}
		if (baseClass == MBindingContainer.class) {
			switch (baseFeatureID) {
				case MApplicationPackage.BINDING_CONTAINER__BINDINGS: return MApplicationPackage.WINDOW__BINDINGS;
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (id: "); //$NON-NLS-1$
		result.append(id);
		result.append(", widget: "); //$NON-NLS-1$
		result.append(widget);
		result.append(", renderer: "); //$NON-NLS-1$
		result.append(renderer);
		result.append(", toBeRendered: "); //$NON-NLS-1$
		result.append(toBeRendered);
		result.append(", onTop: "); //$NON-NLS-1$
		result.append(onTop);
		result.append(", visible: "); //$NON-NLS-1$
		result.append(visible);
		result.append(", context: "); //$NON-NLS-1$
		result.append(context);
		result.append(", variables: "); //$NON-NLS-1$
		result.append(variables);
		result.append(", x: "); //$NON-NLS-1$
		result.append(x);
		result.append(", y: "); //$NON-NLS-1$
		result.append(y);
		result.append(", width: "); //$NON-NLS-1$
		result.append(width);
		result.append(", height: "); //$NON-NLS-1$
		result.append(height);
		result.append(')');
		return result.toString();
	}

} //WindowImpl
