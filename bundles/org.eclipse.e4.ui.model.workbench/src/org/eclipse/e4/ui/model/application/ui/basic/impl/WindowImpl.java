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
package org.eclipse.e4.ui.model.application.ui.basic.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.LocalizationHelper;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.impl.StringToStringMapImpl;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MLocalizable;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.model.application.ui.impl.ElementContainerImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Window</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getLabel <em>Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getIconURI <em>Icon URI</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getTooltip <em>Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getLocalizedLabel <em>Localized Label</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getLocalizedTooltip <em>Localized Tooltip</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getContext <em>Context</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getVariables <em>Variables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getHandlers <em>Handlers</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getBindingContexts <em>Binding Contexts</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getSnippets <em>Snippets</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getMainMenu <em>Main Menu</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getX <em>X</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getY <em>Y</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getWidth <em>Width</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getHeight <em>Height</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getWindows <em>Windows</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.impl.WindowImpl#getSharedElements <em>Shared Elements</em>}</li>
 * </ul>
 *
 * @since 1.0
 * @generated
 */
public class WindowImpl extends ElementContainerImpl<MWindowElement> implements MWindow {
	/**
	 * The default value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected static final String LABEL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLabel() <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLabel()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected String label = LABEL_EDEFAULT;

	/**
	 * The default value of the '{@link #getIconURI() <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIconURI()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected static final String ICON_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getIconURI() <em>Icon URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIconURI()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected String iconURI = ICON_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getTooltip() <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTooltip()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected static final String TOOLTIP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTooltip() <em>Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTooltip()
	 * @since 1.0
	 * @generated
	 * @ordered
	 */
	protected String tooltip = TOOLTIP_EDEFAULT;

	/**
	 * The default value of the '{@link #getLocalizedLabel() <em>Localized Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalizedLabel()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALIZED_LABEL_EDEFAULT = ""; //$NON-NLS-1$

	/**
	 * The default value of the '{@link #getLocalizedTooltip() <em>Localized Tooltip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocalizedTooltip()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALIZED_TOOLTIP_EDEFAULT = ""; //$NON-NLS-1$

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
	 * The cached value of the '{@link #getProperties() <em>Properties</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProperties()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, String> properties;

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
	 * The cached value of the '{@link #getBindingContexts() <em>Binding Contexts</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindingContexts()
	 * @generated
	 * @ordered
	 */
	protected EList<MBindingContext> bindingContexts;

	/**
	 * The cached value of the '{@link #getSnippets() <em>Snippets</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSnippets()
	 * @generated
	 * @ordered
	 */
	protected EList<MUIElement> snippets;

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
	protected static final int X_EDEFAULT = -2147483648;

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
	 * This is true if the X attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean xESet;

	/**
	 * The default value of the '{@link #getY() <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getY()
	 * @generated
	 * @ordered
	 */
	protected static final int Y_EDEFAULT = -2147483648;

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
	 * This is true if the Y attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean yESet;

	/**
	 * The default value of the '{@link #getWidth() <em>Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWidth()
	 * @generated
	 * @ordered
	 */
	protected static final int WIDTH_EDEFAULT = -1;

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
	 * This is true if the Width attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean widthESet;

	/**
	 * The default value of the '{@link #getHeight() <em>Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHeight()
	 * @generated
	 * @ordered
	 */
	protected static final int HEIGHT_EDEFAULT = -1;

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
	 * This is true if the Height attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean heightESet;

	/**
	 * The cached value of the '{@link #getWindows() <em>Windows</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWindows()
	 * @generated
	 * @ordered
	 */
	protected EList<MWindow> windows;

	/**
	 * The cached value of the '{@link #getSharedElements() <em>Shared Elements</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSharedElements()
	 * @generated
	 * @ordered
	 */
	protected EList<MUIElement> sharedElements;

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
		return BasicPackageImpl.Literals.WINDOW;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * This is specialized for the more specific element type known in this context.
	 * @generated
	 */
	@Override
	public List<MWindowElement> getChildren() {
		if (children == null) {
			children = new EObjectContainmentWithInverseEList<>(MWindowElement.class, this,
					BasicPackageImpl.WINDOW__CHILDREN, UiPackageImpl.UI_ELEMENT__PARENT) {
				private static final long serialVersionUID = 1L;

				@Override
				public Class<?> getInverseFeatureClass() {
					return MUIElement.class;
				}
			};
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * This is specialized for the more specific type known in this context.
	 * @generated
	 */
	@Override
	public void setSelectedElement(MWindowElement newSelectedElement) {
		super.setSelectedElement(newSelectedElement);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public void setLabel(String newLabel) {
		String oldLabel = label;
		label = newLabel;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.WINDOW__LABEL, oldLabel, label));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public String getIconURI() {
		return iconURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public void setIconURI(String newIconURI) {
		String oldIconURI = iconURI;
		iconURI = newIconURI;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.WINDOW__ICON_URI, oldIconURI,
					iconURI));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.0
	 * @generated
	 */
	@Override
	public void setTooltip(String newTooltip) {
		String oldTooltip = tooltip;
		tooltip = newTooltip;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.WINDOW__TOOLTIP, oldTooltip,
					tooltip));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLocalizedLabel() {
		return LocalizationHelper.getLocalizedFeature(UiPackageImpl.Literals.UI_LABEL__LABEL, this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLocalizedTooltip() {
		return LocalizationHelper.getLocalizedFeature(UiPackageImpl.Literals.UI_LABEL__TOOLTIP, this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IEclipseContext getContext() {
		return context;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setContext(IEclipseContext newContext) {
		IEclipseContext oldContext = context;
		context = newContext;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.WINDOW__CONTEXT, oldContext,
					context));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<String> getVariables() {
		if (variables == null) {
			variables = new EDataTypeUniqueEList<>(String.class, this, BasicPackageImpl.WINDOW__VARIABLES);
		}
		return variables;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Map<String, String> getProperties() {
		if (properties == null) {
			properties = new EcoreEMap<>(ApplicationPackageImpl.Literals.STRING_TO_STRING_MAP,
					StringToStringMapImpl.class, this, BasicPackageImpl.WINDOW__PROPERTIES);
		}
		return properties.map();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<MHandler> getHandlers() {
		if (handlers == null) {
			handlers = new EObjectContainmentEList<>(MHandler.class, this, BasicPackageImpl.WINDOW__HANDLERS);
		}
		return handlers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<MBindingContext> getBindingContexts() {
		if (bindingContexts == null) {
			bindingContexts = new EObjectResolvingEList<>(MBindingContext.class, this,
					BasicPackageImpl.WINDOW__BINDING_CONTEXTS);
		}
		return bindingContexts;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<MUIElement> getSnippets() {
		if (snippets == null) {
			snippets = new EObjectContainmentEList<>(MUIElement.class, this,
					BasicPackageImpl.WINDOW__SNIPPETS);
		}
		return snippets;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
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
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
					BasicPackageImpl.WINDOW__MAIN_MENU, oldMainMenu, newMainMenu);
			if (msgs == null) {
				msgs = notification;
			} else {
				msgs.add(notification);
			}
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMainMenu(MMenu newMainMenu) {
		if (newMainMenu != mainMenu) {
			NotificationChain msgs = null;
			if (mainMenu != null) {
				msgs = ((InternalEObject) mainMenu).eInverseRemove(this,
						EOPPOSITE_FEATURE_BASE - BasicPackageImpl.WINDOW__MAIN_MENU, null, msgs);
			}
			if (newMainMenu != null) {
				msgs = ((InternalEObject) newMainMenu).eInverseAdd(this,
						EOPPOSITE_FEATURE_BASE - BasicPackageImpl.WINDOW__MAIN_MENU, null, msgs);
			}
			msgs = basicSetMainMenu(newMainMenu, msgs);
			if (msgs != null) {
				msgs.dispatch();
			}
		} else if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.WINDOW__MAIN_MENU, newMainMenu,
					newMainMenu));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getX() {
		return x;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setX(int newX) {
		int oldX = x;
		x = newX;
		boolean oldXESet = xESet;
		xESet = true;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.WINDOW__X, oldX, x, !oldXESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetX() {
		int oldX = x;
		boolean oldXESet = xESet;
		x = X_EDEFAULT;
		xESet = false;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.UNSET, BasicPackageImpl.WINDOW__X, oldX, X_EDEFAULT,
					oldXESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetX() {
		return xESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getY() {
		return y;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setY(int newY) {
		int oldY = y;
		y = newY;
		boolean oldYESet = yESet;
		yESet = true;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.WINDOW__Y, oldY, y, !oldYESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetY() {
		int oldY = y;
		boolean oldYESet = yESet;
		y = Y_EDEFAULT;
		yESet = false;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.UNSET, BasicPackageImpl.WINDOW__Y, oldY, Y_EDEFAULT,
					oldYESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetY() {
		return yESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getWidth() {
		return width;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setWidth(int newWidth) {
		int oldWidth = width;
		width = newWidth;
		boolean oldWidthESet = widthESet;
		widthESet = true;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.WINDOW__WIDTH, oldWidth, width,
					!oldWidthESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetWidth() {
		int oldWidth = width;
		boolean oldWidthESet = widthESet;
		width = WIDTH_EDEFAULT;
		widthESet = false;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.UNSET, BasicPackageImpl.WINDOW__WIDTH, oldWidth,
					WIDTH_EDEFAULT, oldWidthESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetWidth() {
		return widthESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getHeight() {
		return height;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setHeight(int newHeight) {
		int oldHeight = height;
		height = newHeight;
		boolean oldHeightESet = heightESet;
		heightESet = true;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.WINDOW__HEIGHT, oldHeight, height,
					!oldHeightESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetHeight() {
		int oldHeight = height;
		boolean oldHeightESet = heightESet;
		height = HEIGHT_EDEFAULT;
		heightESet = false;
		if (eNotificationRequired()) {
			eNotify(new ENotificationImpl(this, Notification.UNSET, BasicPackageImpl.WINDOW__HEIGHT, oldHeight,
					HEIGHT_EDEFAULT, oldHeightESet));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetHeight() {
		return heightESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<MWindow> getWindows() {
		if (windows == null) {
			windows = new EObjectContainmentEList<>(MWindow.class, this, BasicPackageImpl.WINDOW__WINDOWS);
		}
		return windows;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<MUIElement> getSharedElements() {
		if (sharedElements == null) {
			sharedElements = new EObjectContainmentEList<>(MUIElement.class, this,
					BasicPackageImpl.WINDOW__SHARED_ELEMENTS);
		}
		return sharedElements;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @since 1.1
	 * @generated
	 */
	@Override
	public void updateLocalization() {
		if (eNotificationRequired()) {
			super.updateLocalization();
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.WINDOW__LOCALIZED_LABEL, null,
					getLocalizedLabel()));
			eNotify(new ENotificationImpl(this, Notification.SET, BasicPackageImpl.WINDOW__LOCALIZED_TOOLTIP, null,
					getLocalizedTooltip()));
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case BasicPackageImpl.WINDOW__PROPERTIES:
			return ((InternalEList<?>) ((EMap.InternalMapView<String, String>) getProperties()).eMap())
					.basicRemove(otherEnd, msgs);
		case BasicPackageImpl.WINDOW__HANDLERS:
			return ((InternalEList<?>) getHandlers()).basicRemove(otherEnd, msgs);
		case BasicPackageImpl.WINDOW__SNIPPETS:
			return ((InternalEList<?>) getSnippets()).basicRemove(otherEnd, msgs);
		case BasicPackageImpl.WINDOW__MAIN_MENU:
			return basicSetMainMenu(null, msgs);
		case BasicPackageImpl.WINDOW__WINDOWS:
			return ((InternalEList<?>) getWindows()).basicRemove(otherEnd, msgs);
		case BasicPackageImpl.WINDOW__SHARED_ELEMENTS:
			return ((InternalEList<?>) getSharedElements()).basicRemove(otherEnd, msgs);
		default:
			return super.eInverseRemove(otherEnd, featureID, msgs);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case BasicPackageImpl.WINDOW__LABEL:
			return getLabel();
		case BasicPackageImpl.WINDOW__ICON_URI:
			return getIconURI();
		case BasicPackageImpl.WINDOW__TOOLTIP:
			return getTooltip();
		case BasicPackageImpl.WINDOW__LOCALIZED_LABEL:
			return getLocalizedLabel();
		case BasicPackageImpl.WINDOW__LOCALIZED_TOOLTIP:
			return getLocalizedTooltip();
		case BasicPackageImpl.WINDOW__CONTEXT:
			return getContext();
		case BasicPackageImpl.WINDOW__VARIABLES:
			return getVariables();
		case BasicPackageImpl.WINDOW__PROPERTIES:
			if (coreType) {
				return ((EMap.InternalMapView<String, String>) getProperties()).eMap();
			} else {
				return getProperties();
			}
		case BasicPackageImpl.WINDOW__HANDLERS:
			return getHandlers();
		case BasicPackageImpl.WINDOW__BINDING_CONTEXTS:
			return getBindingContexts();
		case BasicPackageImpl.WINDOW__SNIPPETS:
			return getSnippets();
		case BasicPackageImpl.WINDOW__MAIN_MENU:
			return getMainMenu();
		case BasicPackageImpl.WINDOW__X:
			return getX();
		case BasicPackageImpl.WINDOW__Y:
			return getY();
		case BasicPackageImpl.WINDOW__WIDTH:
			return getWidth();
		case BasicPackageImpl.WINDOW__HEIGHT:
			return getHeight();
		case BasicPackageImpl.WINDOW__WINDOWS:
			return getWindows();
		case BasicPackageImpl.WINDOW__SHARED_ELEMENTS:
			return getSharedElements();
		default:
			return super.eGet(featureID, resolve, coreType);
		}
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
		case BasicPackageImpl.WINDOW__LABEL:
			setLabel((String) newValue);
			return;
		case BasicPackageImpl.WINDOW__ICON_URI:
			setIconURI((String) newValue);
			return;
		case BasicPackageImpl.WINDOW__TOOLTIP:
			setTooltip((String) newValue);
			return;
		case BasicPackageImpl.WINDOW__CONTEXT:
			setContext((IEclipseContext) newValue);
			return;
		case BasicPackageImpl.WINDOW__VARIABLES:
			getVariables().clear();
			getVariables().addAll((Collection<? extends String>) newValue);
			return;
		case BasicPackageImpl.WINDOW__PROPERTIES:
			((EStructuralFeature.Setting) ((EMap.InternalMapView<String, String>) getProperties()).eMap())
					.set(newValue);
			return;
		case BasicPackageImpl.WINDOW__HANDLERS:
			getHandlers().clear();
			getHandlers().addAll((Collection<? extends MHandler>) newValue);
			return;
		case BasicPackageImpl.WINDOW__BINDING_CONTEXTS:
			getBindingContexts().clear();
			getBindingContexts().addAll((Collection<? extends MBindingContext>) newValue);
			return;
		case BasicPackageImpl.WINDOW__SNIPPETS:
			getSnippets().clear();
			getSnippets().addAll((Collection<? extends MUIElement>) newValue);
			return;
		case BasicPackageImpl.WINDOW__MAIN_MENU:
			setMainMenu((MMenu) newValue);
			return;
		case BasicPackageImpl.WINDOW__X:
			setX((Integer) newValue);
			return;
		case BasicPackageImpl.WINDOW__Y:
			setY((Integer) newValue);
			return;
		case BasicPackageImpl.WINDOW__WIDTH:
			setWidth((Integer) newValue);
			return;
		case BasicPackageImpl.WINDOW__HEIGHT:
			setHeight((Integer) newValue);
			return;
		case BasicPackageImpl.WINDOW__WINDOWS:
			getWindows().clear();
			getWindows().addAll((Collection<? extends MWindow>) newValue);
			return;
		case BasicPackageImpl.WINDOW__SHARED_ELEMENTS:
			getSharedElements().clear();
			getSharedElements().addAll((Collection<? extends MUIElement>) newValue);
			return;
		default:
			super.eSet(featureID, newValue);
			return;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case BasicPackageImpl.WINDOW__LABEL:
			setLabel(LABEL_EDEFAULT);
			return;
		case BasicPackageImpl.WINDOW__ICON_URI:
			setIconURI(ICON_URI_EDEFAULT);
			return;
		case BasicPackageImpl.WINDOW__TOOLTIP:
			setTooltip(TOOLTIP_EDEFAULT);
			return;
		case BasicPackageImpl.WINDOW__CONTEXT:
			setContext(CONTEXT_EDEFAULT);
			return;
		case BasicPackageImpl.WINDOW__VARIABLES:
			getVariables().clear();
			return;
		case BasicPackageImpl.WINDOW__PROPERTIES:
			getProperties().clear();
			return;
		case BasicPackageImpl.WINDOW__HANDLERS:
			getHandlers().clear();
			return;
		case BasicPackageImpl.WINDOW__BINDING_CONTEXTS:
			getBindingContexts().clear();
			return;
		case BasicPackageImpl.WINDOW__SNIPPETS:
			getSnippets().clear();
			return;
		case BasicPackageImpl.WINDOW__MAIN_MENU:
			setMainMenu((MMenu) null);
			return;
		case BasicPackageImpl.WINDOW__X:
			unsetX();
			return;
		case BasicPackageImpl.WINDOW__Y:
			unsetY();
			return;
		case BasicPackageImpl.WINDOW__WIDTH:
			unsetWidth();
			return;
		case BasicPackageImpl.WINDOW__HEIGHT:
			unsetHeight();
			return;
		case BasicPackageImpl.WINDOW__WINDOWS:
			getWindows().clear();
			return;
		case BasicPackageImpl.WINDOW__SHARED_ELEMENTS:
			getSharedElements().clear();
			return;
		default:
			super.eUnset(featureID);
			return;
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case BasicPackageImpl.WINDOW__LABEL:
			return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
		case BasicPackageImpl.WINDOW__ICON_URI:
			return ICON_URI_EDEFAULT == null ? iconURI != null : !ICON_URI_EDEFAULT.equals(iconURI);
		case BasicPackageImpl.WINDOW__TOOLTIP:
			return TOOLTIP_EDEFAULT == null ? tooltip != null : !TOOLTIP_EDEFAULT.equals(tooltip);
		case BasicPackageImpl.WINDOW__LOCALIZED_LABEL:
			return LOCALIZED_LABEL_EDEFAULT == null ? getLocalizedLabel() != null
					: !LOCALIZED_LABEL_EDEFAULT.equals(getLocalizedLabel());
		case BasicPackageImpl.WINDOW__LOCALIZED_TOOLTIP:
			return LOCALIZED_TOOLTIP_EDEFAULT == null ? getLocalizedTooltip() != null
					: !LOCALIZED_TOOLTIP_EDEFAULT.equals(getLocalizedTooltip());
		case BasicPackageImpl.WINDOW__CONTEXT:
			return CONTEXT_EDEFAULT == null ? context != null : !CONTEXT_EDEFAULT.equals(context);
		case BasicPackageImpl.WINDOW__VARIABLES:
			return variables != null && !variables.isEmpty();
		case BasicPackageImpl.WINDOW__PROPERTIES:
			return properties != null && !properties.isEmpty();
		case BasicPackageImpl.WINDOW__HANDLERS:
			return handlers != null && !handlers.isEmpty();
		case BasicPackageImpl.WINDOW__BINDING_CONTEXTS:
			return bindingContexts != null && !bindingContexts.isEmpty();
		case BasicPackageImpl.WINDOW__SNIPPETS:
			return snippets != null && !snippets.isEmpty();
		case BasicPackageImpl.WINDOW__MAIN_MENU:
			return mainMenu != null;
		case BasicPackageImpl.WINDOW__X:
			return isSetX();
		case BasicPackageImpl.WINDOW__Y:
			return isSetY();
		case BasicPackageImpl.WINDOW__WIDTH:
			return isSetWidth();
		case BasicPackageImpl.WINDOW__HEIGHT:
			return isSetHeight();
		case BasicPackageImpl.WINDOW__WINDOWS:
			return windows != null && !windows.isEmpty();
		case BasicPackageImpl.WINDOW__SHARED_ELEMENTS:
			return sharedElements != null && !sharedElements.isEmpty();
		default:
			return super.eIsSet(featureID);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == MUILabel.class) {
			switch (derivedFeatureID) {
			case BasicPackageImpl.WINDOW__LABEL:
				return UiPackageImpl.UI_LABEL__LABEL;
			case BasicPackageImpl.WINDOW__ICON_URI:
				return UiPackageImpl.UI_LABEL__ICON_URI;
			case BasicPackageImpl.WINDOW__TOOLTIP:
				return UiPackageImpl.UI_LABEL__TOOLTIP;
			case BasicPackageImpl.WINDOW__LOCALIZED_LABEL:
				return UiPackageImpl.UI_LABEL__LOCALIZED_LABEL;
			case BasicPackageImpl.WINDOW__LOCALIZED_TOOLTIP:
				return UiPackageImpl.UI_LABEL__LOCALIZED_TOOLTIP;
			default:
				return -1;
			}
		}
		if (baseClass == MContext.class) {
			switch (derivedFeatureID) {
			case BasicPackageImpl.WINDOW__CONTEXT:
				return UiPackageImpl.CONTEXT__CONTEXT;
			case BasicPackageImpl.WINDOW__VARIABLES:
				return UiPackageImpl.CONTEXT__VARIABLES;
			case BasicPackageImpl.WINDOW__PROPERTIES:
				return UiPackageImpl.CONTEXT__PROPERTIES;
			default:
				return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (derivedFeatureID) {
			case BasicPackageImpl.WINDOW__HANDLERS:
				return CommandsPackageImpl.HANDLER_CONTAINER__HANDLERS;
			default:
				return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (derivedFeatureID) {
			case BasicPackageImpl.WINDOW__BINDING_CONTEXTS:
				return CommandsPackageImpl.BINDINGS__BINDING_CONTEXTS;
			default:
				return -1;
			}
		}
		if (baseClass == MSnippetContainer.class) {
			switch (derivedFeatureID) {
			case BasicPackageImpl.WINDOW__SNIPPETS:
				return UiPackageImpl.SNIPPET_CONTAINER__SNIPPETS;
			default:
				return -1;
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
		if (baseClass == MUILabel.class) {
			switch (baseFeatureID) {
			case UiPackageImpl.UI_LABEL__LABEL:
				return BasicPackageImpl.WINDOW__LABEL;
			case UiPackageImpl.UI_LABEL__ICON_URI:
				return BasicPackageImpl.WINDOW__ICON_URI;
			case UiPackageImpl.UI_LABEL__TOOLTIP:
				return BasicPackageImpl.WINDOW__TOOLTIP;
			case UiPackageImpl.UI_LABEL__LOCALIZED_LABEL:
				return BasicPackageImpl.WINDOW__LOCALIZED_LABEL;
			case UiPackageImpl.UI_LABEL__LOCALIZED_TOOLTIP:
				return BasicPackageImpl.WINDOW__LOCALIZED_TOOLTIP;
			default:
				return -1;
			}
		}
		if (baseClass == MContext.class) {
			switch (baseFeatureID) {
			case UiPackageImpl.CONTEXT__CONTEXT:
				return BasicPackageImpl.WINDOW__CONTEXT;
			case UiPackageImpl.CONTEXT__VARIABLES:
				return BasicPackageImpl.WINDOW__VARIABLES;
			case UiPackageImpl.CONTEXT__PROPERTIES:
				return BasicPackageImpl.WINDOW__PROPERTIES;
			default:
				return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (baseFeatureID) {
			case CommandsPackageImpl.HANDLER_CONTAINER__HANDLERS:
				return BasicPackageImpl.WINDOW__HANDLERS;
			default:
				return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (baseFeatureID) {
			case CommandsPackageImpl.BINDINGS__BINDING_CONTEXTS:
				return BasicPackageImpl.WINDOW__BINDING_CONTEXTS;
			default:
				return -1;
			}
		}
		if (baseClass == MSnippetContainer.class) {
			switch (baseFeatureID) {
			case UiPackageImpl.SNIPPET_CONTAINER__SNIPPETS:
				return BasicPackageImpl.WINDOW__SNIPPETS;
			default:
				return -1;
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
	public int eDerivedOperationID(int baseOperationID, Class<?> baseClass) {
		if (baseClass == MLocalizable.class) {
			switch (baseOperationID) {
			case UiPackageImpl.LOCALIZABLE___UPDATE_LOCALIZATION:
				return BasicPackageImpl.WINDOW___UPDATE_LOCALIZATION;
			default:
				return super.eDerivedOperationID(baseOperationID, baseClass);
			}
		}
		if (baseClass == MUIElement.class) {
			switch (baseOperationID) {
			case UiPackageImpl.UI_ELEMENT___UPDATE_LOCALIZATION:
				return BasicPackageImpl.WINDOW___UPDATE_LOCALIZATION;
			default:
				return super.eDerivedOperationID(baseOperationID, baseClass);
			}
		}
		if (baseClass == MUILabel.class) {
			switch (baseOperationID) {
			default:
				return -1;
			}
		}
		if (baseClass == MContext.class) {
			switch (baseOperationID) {
			default:
				return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (baseOperationID) {
			default:
				return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (baseOperationID) {
			default:
				return -1;
			}
		}
		if (baseClass == MSnippetContainer.class) {
			switch (baseOperationID) {
			default:
				return -1;
			}
		}
		return super.eDerivedOperationID(baseOperationID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
		case BasicPackageImpl.WINDOW___UPDATE_LOCALIZATION:
			updateLocalization();
			return null;
		default:
			return super.eInvoke(operationID, arguments);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) {
			return super.toString();
		}

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (label: "); //$NON-NLS-1$
		result.append(label);
		result.append(", iconURI: "); //$NON-NLS-1$
		result.append(iconURI);
		result.append(", tooltip: "); //$NON-NLS-1$
		result.append(tooltip);
		result.append(", context: "); //$NON-NLS-1$
		result.append(context);
		result.append(", variables: "); //$NON-NLS-1$
		result.append(variables);
		result.append(", x: "); //$NON-NLS-1$
		if (xESet) {
			result.append(x);
		}
		else {
			result.append("<unset>"); //$NON-NLS-1$
		}
		result.append(", y: "); //$NON-NLS-1$
		if (yESet) {
			result.append(y);
		}
		else {
			result.append("<unset>"); //$NON-NLS-1$
		}
		result.append(", width: "); //$NON-NLS-1$
		if (widthESet) {
			result.append(width);
		}
		else {
			result.append("<unset>"); //$NON-NLS-1$
		}
		result.append(", height: "); //$NON-NLS-1$
		if (heightESet) {
			result.append(height);
		}
		else {
			result.append("<unset>"); //$NON-NLS-1$
		}
		result.append(')');
		return result.toString();
	}

} //WindowImpl
