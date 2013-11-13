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
package org.eclipse.e4.ui.model.application.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MBindingTableContainer;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer;
import org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MDialog;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.impl.ElementContainerImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
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
 * An implementation of the model object '<em><b>Application</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getContext <em>Context</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getVariables <em>Variables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getHandlers <em>Handlers</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getBindingTables <em>Binding Tables</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getRootContext <em>Root Context</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getDescriptors <em>Descriptors</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getBindingContexts <em>Binding Contexts</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getMenuContributions <em>Menu Contributions</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getToolBarContributions <em>Tool Bar Contributions</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getTrimContributions <em>Trim Contributions</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getSnippets <em>Snippets</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getCommands <em>Commands</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getAddons <em>Addons</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getCategories <em>Categories</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.impl.ApplicationImpl#getDialogs <em>Dialogs</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ApplicationImpl extends ElementContainerImpl<MWindow> implements MApplication {
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
	 * The cached value of the '{@link #getBindingTables() <em>Binding Tables</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindingTables()
	 * @generated
	 * @ordered
	 */
	protected EList<MBindingTable> bindingTables;

	/**
	 * The cached value of the '{@link #getRootContext() <em>Root Context</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRootContext()
	 * @generated
	 * @ordered
	 */
	protected EList<MBindingContext> rootContext;

	/**
	 * The cached value of the '{@link #getDescriptors() <em>Descriptors</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescriptors()
	 * @generated
	 * @ordered
	 */
	protected EList<MPartDescriptor> descriptors;

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
	 * The cached value of the '{@link #getMenuContributions() <em>Menu Contributions</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMenuContributions()
	 * @generated
	 * @ordered
	 */
	protected EList<MMenuContribution> menuContributions;

	/**
	 * The cached value of the '{@link #getToolBarContributions() <em>Tool Bar Contributions</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getToolBarContributions()
	 * @generated
	 * @ordered
	 */
	protected EList<MToolBarContribution> toolBarContributions;

	/**
	 * The cached value of the '{@link #getTrimContributions() <em>Trim Contributions</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTrimContributions()
	 * @generated
	 * @ordered
	 */
	protected EList<MTrimContribution> trimContributions;

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
	 * The cached value of the '{@link #getCommands() <em>Commands</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommands()
	 * @generated
	 * @ordered
	 */
	protected EList<MCommand> commands;

	/**
	 * The cached value of the '{@link #getAddons() <em>Addons</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAddons()
	 * @generated
	 * @ordered
	 */
	protected EList<MAddon> addons;

	/**
	 * The cached value of the '{@link #getCategories() <em>Categories</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCategories()
	 * @generated
	 * @ordered
	 */
	protected EList<MCategory> categories;

	/**
	 * The cached value of the '{@link #getDialogs() <em>Dialogs</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDialogs()
	 * @generated
	 * @ordered
	 */
	protected EList<MDialog> dialogs;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ApplicationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ApplicationPackageImpl.Literals.APPLICATION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * This is specialized for the more specific element type known in this context.
	 * @generated
	 */
	@Override
	public List<MWindow> getChildren() {
		if (children == null) {
			children = new EObjectContainmentWithInverseEList<MWindow>(MWindow.class, this, ApplicationPackageImpl.APPLICATION__CHILDREN, UiPackageImpl.UI_ELEMENT__PARENT) { private static final long serialVersionUID = 1L; @Override public Class<?> getInverseFeatureClass() { return MUIElement.class; } };
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
	public void setSelectedElement(MWindow newSelectedElement) {
		super.setSelectedElement(newSelectedElement);
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
			eNotify(new ENotificationImpl(this, Notification.SET, ApplicationPackageImpl.APPLICATION__CONTEXT, oldContext, context));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<String> getVariables() {
		if (variables == null) {
			variables = new EDataTypeUniqueEList<String>(String.class, this, ApplicationPackageImpl.APPLICATION__VARIABLES);
		}
		return variables;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Map<String, String> getProperties() {
		if (properties == null) {
			properties = new EcoreEMap<String,String>(ApplicationPackageImpl.Literals.STRING_TO_STRING_MAP, StringToStringMapImpl.class, this, ApplicationPackageImpl.APPLICATION__PROPERTIES);
		}
		return properties.map();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MHandler> getHandlers() {
		if (handlers == null) {
			handlers = new EObjectContainmentEList<MHandler>(MHandler.class, this, ApplicationPackageImpl.APPLICATION__HANDLERS);
		}
		return handlers;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MBindingTable> getBindingTables() {
		if (bindingTables == null) {
			bindingTables = new EObjectContainmentEList<MBindingTable>(MBindingTable.class, this, ApplicationPackageImpl.APPLICATION__BINDING_TABLES);
		}
		return bindingTables;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MBindingContext> getRootContext() {
		if (rootContext == null) {
			rootContext = new EObjectContainmentEList<MBindingContext>(MBindingContext.class, this, ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT);
		}
		return rootContext;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MPartDescriptor> getDescriptors() {
		if (descriptors == null) {
			descriptors = new EObjectContainmentEList<MPartDescriptor>(MPartDescriptor.class, this, ApplicationPackageImpl.APPLICATION__DESCRIPTORS);
		}
		return descriptors;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MBindingContext> getBindingContexts() {
		if (bindingContexts == null) {
			bindingContexts = new EObjectResolvingEList<MBindingContext>(MBindingContext.class, this, ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS);
		}
		return bindingContexts;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MMenuContribution> getMenuContributions() {
		if (menuContributions == null) {
			menuContributions = new EObjectContainmentEList<MMenuContribution>(MMenuContribution.class, this, ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS);
		}
		return menuContributions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MToolBarContribution> getToolBarContributions() {
		if (toolBarContributions == null) {
			toolBarContributions = new EObjectContainmentEList<MToolBarContribution>(MToolBarContribution.class, this, ApplicationPackageImpl.APPLICATION__TOOL_BAR_CONTRIBUTIONS);
		}
		return toolBarContributions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MTrimContribution> getTrimContributions() {
		if (trimContributions == null) {
			trimContributions = new EObjectContainmentEList<MTrimContribution>(MTrimContribution.class, this, ApplicationPackageImpl.APPLICATION__TRIM_CONTRIBUTIONS);
		}
		return trimContributions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MUIElement> getSnippets() {
		if (snippets == null) {
			snippets = new EObjectContainmentEList<MUIElement>(MUIElement.class, this, ApplicationPackageImpl.APPLICATION__SNIPPETS);
		}
		return snippets;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MCommand> getCommands() {
		if (commands == null) {
			commands = new EObjectContainmentEList<MCommand>(MCommand.class, this, ApplicationPackageImpl.APPLICATION__COMMANDS);
		}
		return commands;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MAddon> getAddons() {
		if (addons == null) {
			addons = new EObjectContainmentEList<MAddon>(MAddon.class, this, ApplicationPackageImpl.APPLICATION__ADDONS);
		}
		return addons;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MCategory> getCategories() {
		if (categories == null) {
			categories = new EObjectContainmentEList<MCategory>(MCategory.class, this, ApplicationPackageImpl.APPLICATION__CATEGORIES);
		}
		return categories;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public List<MDialog> getDialogs() {
		if (dialogs == null) {
			dialogs = new EObjectResolvingEList<MDialog>(MDialog.class, this, ApplicationPackageImpl.APPLICATION__DIALOGS);
		}
		return dialogs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ApplicationPackageImpl.APPLICATION__PROPERTIES:
				return ((InternalEList<?>)((EMap.InternalMapView<String, String>)getProperties()).eMap()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__HANDLERS:
				return ((InternalEList<?>)getHandlers()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__BINDING_TABLES:
				return ((InternalEList<?>)getBindingTables()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT:
				return ((InternalEList<?>)getRootContext()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__DESCRIPTORS:
				return ((InternalEList<?>)getDescriptors()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS:
				return ((InternalEList<?>)getMenuContributions()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__TOOL_BAR_CONTRIBUTIONS:
				return ((InternalEList<?>)getToolBarContributions()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__TRIM_CONTRIBUTIONS:
				return ((InternalEList<?>)getTrimContributions()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__SNIPPETS:
				return ((InternalEList<?>)getSnippets()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__COMMANDS:
				return ((InternalEList<?>)getCommands()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__ADDONS:
				return ((InternalEList<?>)getAddons()).basicRemove(otherEnd, msgs);
			case ApplicationPackageImpl.APPLICATION__CATEGORIES:
				return ((InternalEList<?>)getCategories()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ApplicationPackageImpl.APPLICATION__CONTEXT:
				return getContext();
			case ApplicationPackageImpl.APPLICATION__VARIABLES:
				return getVariables();
			case ApplicationPackageImpl.APPLICATION__PROPERTIES:
				if (coreType) return ((EMap.InternalMapView<String, String>)getProperties()).eMap();
				else return getProperties();
			case ApplicationPackageImpl.APPLICATION__HANDLERS:
				return getHandlers();
			case ApplicationPackageImpl.APPLICATION__BINDING_TABLES:
				return getBindingTables();
			case ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT:
				return getRootContext();
			case ApplicationPackageImpl.APPLICATION__DESCRIPTORS:
				return getDescriptors();
			case ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS:
				return getBindingContexts();
			case ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS:
				return getMenuContributions();
			case ApplicationPackageImpl.APPLICATION__TOOL_BAR_CONTRIBUTIONS:
				return getToolBarContributions();
			case ApplicationPackageImpl.APPLICATION__TRIM_CONTRIBUTIONS:
				return getTrimContributions();
			case ApplicationPackageImpl.APPLICATION__SNIPPETS:
				return getSnippets();
			case ApplicationPackageImpl.APPLICATION__COMMANDS:
				return getCommands();
			case ApplicationPackageImpl.APPLICATION__ADDONS:
				return getAddons();
			case ApplicationPackageImpl.APPLICATION__CATEGORIES:
				return getCategories();
			case ApplicationPackageImpl.APPLICATION__DIALOGS:
				return getDialogs();
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
			case ApplicationPackageImpl.APPLICATION__CONTEXT:
				setContext((IEclipseContext)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__VARIABLES:
				getVariables().clear();
				getVariables().addAll((Collection<? extends String>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__PROPERTIES:
				((EStructuralFeature.Setting)((EMap.InternalMapView<String, String>)getProperties()).eMap()).set(newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__HANDLERS:
				getHandlers().clear();
				getHandlers().addAll((Collection<? extends MHandler>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__BINDING_TABLES:
				getBindingTables().clear();
				getBindingTables().addAll((Collection<? extends MBindingTable>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT:
				getRootContext().clear();
				getRootContext().addAll((Collection<? extends MBindingContext>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__DESCRIPTORS:
				getDescriptors().clear();
				getDescriptors().addAll((Collection<? extends MPartDescriptor>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS:
				getBindingContexts().clear();
				getBindingContexts().addAll((Collection<? extends MBindingContext>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS:
				getMenuContributions().clear();
				getMenuContributions().addAll((Collection<? extends MMenuContribution>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__TOOL_BAR_CONTRIBUTIONS:
				getToolBarContributions().clear();
				getToolBarContributions().addAll((Collection<? extends MToolBarContribution>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__TRIM_CONTRIBUTIONS:
				getTrimContributions().clear();
				getTrimContributions().addAll((Collection<? extends MTrimContribution>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__SNIPPETS:
				getSnippets().clear();
				getSnippets().addAll((Collection<? extends MUIElement>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__COMMANDS:
				getCommands().clear();
				getCommands().addAll((Collection<? extends MCommand>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__ADDONS:
				getAddons().clear();
				getAddons().addAll((Collection<? extends MAddon>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__CATEGORIES:
				getCategories().clear();
				getCategories().addAll((Collection<? extends MCategory>)newValue);
				return;
			case ApplicationPackageImpl.APPLICATION__DIALOGS:
				getDialogs().clear();
				getDialogs().addAll((Collection<? extends MDialog>)newValue);
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
			case ApplicationPackageImpl.APPLICATION__CONTEXT:
				setContext(CONTEXT_EDEFAULT);
				return;
			case ApplicationPackageImpl.APPLICATION__VARIABLES:
				getVariables().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__PROPERTIES:
				getProperties().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__HANDLERS:
				getHandlers().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__BINDING_TABLES:
				getBindingTables().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT:
				getRootContext().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__DESCRIPTORS:
				getDescriptors().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS:
				getBindingContexts().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS:
				getMenuContributions().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__TOOL_BAR_CONTRIBUTIONS:
				getToolBarContributions().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__TRIM_CONTRIBUTIONS:
				getTrimContributions().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__SNIPPETS:
				getSnippets().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__COMMANDS:
				getCommands().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__ADDONS:
				getAddons().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__CATEGORIES:
				getCategories().clear();
				return;
			case ApplicationPackageImpl.APPLICATION__DIALOGS:
				getDialogs().clear();
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
			case ApplicationPackageImpl.APPLICATION__CONTEXT:
				return CONTEXT_EDEFAULT == null ? context != null : !CONTEXT_EDEFAULT.equals(context);
			case ApplicationPackageImpl.APPLICATION__VARIABLES:
				return variables != null && !variables.isEmpty();
			case ApplicationPackageImpl.APPLICATION__PROPERTIES:
				return properties != null && !properties.isEmpty();
			case ApplicationPackageImpl.APPLICATION__HANDLERS:
				return handlers != null && !handlers.isEmpty();
			case ApplicationPackageImpl.APPLICATION__BINDING_TABLES:
				return bindingTables != null && !bindingTables.isEmpty();
			case ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT:
				return rootContext != null && !rootContext.isEmpty();
			case ApplicationPackageImpl.APPLICATION__DESCRIPTORS:
				return descriptors != null && !descriptors.isEmpty();
			case ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS:
				return bindingContexts != null && !bindingContexts.isEmpty();
			case ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS:
				return menuContributions != null && !menuContributions.isEmpty();
			case ApplicationPackageImpl.APPLICATION__TOOL_BAR_CONTRIBUTIONS:
				return toolBarContributions != null && !toolBarContributions.isEmpty();
			case ApplicationPackageImpl.APPLICATION__TRIM_CONTRIBUTIONS:
				return trimContributions != null && !trimContributions.isEmpty();
			case ApplicationPackageImpl.APPLICATION__SNIPPETS:
				return snippets != null && !snippets.isEmpty();
			case ApplicationPackageImpl.APPLICATION__COMMANDS:
				return commands != null && !commands.isEmpty();
			case ApplicationPackageImpl.APPLICATION__ADDONS:
				return addons != null && !addons.isEmpty();
			case ApplicationPackageImpl.APPLICATION__CATEGORIES:
				return categories != null && !categories.isEmpty();
			case ApplicationPackageImpl.APPLICATION__DIALOGS:
				return dialogs != null && !dialogs.isEmpty();
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
		if (baseClass == MContext.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__CONTEXT: return UiPackageImpl.CONTEXT__CONTEXT;
				case ApplicationPackageImpl.APPLICATION__VARIABLES: return UiPackageImpl.CONTEXT__VARIABLES;
				case ApplicationPackageImpl.APPLICATION__PROPERTIES: return UiPackageImpl.CONTEXT__PROPERTIES;
				default: return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__HANDLERS: return CommandsPackageImpl.HANDLER_CONTAINER__HANDLERS;
				default: return -1;
			}
		}
		if (baseClass == MBindingTableContainer.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__BINDING_TABLES: return CommandsPackageImpl.BINDING_TABLE_CONTAINER__BINDING_TABLES;
				case ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT: return CommandsPackageImpl.BINDING_TABLE_CONTAINER__ROOT_CONTEXT;
				default: return -1;
			}
		}
		if (baseClass == MPartDescriptorContainer.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__DESCRIPTORS: return BasicPackageImpl.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS;
				default: return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS: return CommandsPackageImpl.BINDINGS__BINDING_CONTEXTS;
				default: return -1;
			}
		}
		if (baseClass == MMenuContributions.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS: return MenuPackageImpl.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS;
				default: return -1;
			}
		}
		if (baseClass == MToolBarContributions.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__TOOL_BAR_CONTRIBUTIONS: return MenuPackageImpl.TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS;
				default: return -1;
			}
		}
		if (baseClass == MTrimContributions.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__TRIM_CONTRIBUTIONS: return MenuPackageImpl.TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS;
				default: return -1;
			}
		}
		if (baseClass == MSnippetContainer.class) {
			switch (derivedFeatureID) {
				case ApplicationPackageImpl.APPLICATION__SNIPPETS: return UiPackageImpl.SNIPPET_CONTAINER__SNIPPETS;
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
		if (baseClass == MContext.class) {
			switch (baseFeatureID) {
				case UiPackageImpl.CONTEXT__CONTEXT: return ApplicationPackageImpl.APPLICATION__CONTEXT;
				case UiPackageImpl.CONTEXT__VARIABLES: return ApplicationPackageImpl.APPLICATION__VARIABLES;
				case UiPackageImpl.CONTEXT__PROPERTIES: return ApplicationPackageImpl.APPLICATION__PROPERTIES;
				default: return -1;
			}
		}
		if (baseClass == MHandlerContainer.class) {
			switch (baseFeatureID) {
				case CommandsPackageImpl.HANDLER_CONTAINER__HANDLERS: return ApplicationPackageImpl.APPLICATION__HANDLERS;
				default: return -1;
			}
		}
		if (baseClass == MBindingTableContainer.class) {
			switch (baseFeatureID) {
				case CommandsPackageImpl.BINDING_TABLE_CONTAINER__BINDING_TABLES: return ApplicationPackageImpl.APPLICATION__BINDING_TABLES;
				case CommandsPackageImpl.BINDING_TABLE_CONTAINER__ROOT_CONTEXT: return ApplicationPackageImpl.APPLICATION__ROOT_CONTEXT;
				default: return -1;
			}
		}
		if (baseClass == MPartDescriptorContainer.class) {
			switch (baseFeatureID) {
				case BasicPackageImpl.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS: return ApplicationPackageImpl.APPLICATION__DESCRIPTORS;
				default: return -1;
			}
		}
		if (baseClass == MBindings.class) {
			switch (baseFeatureID) {
				case CommandsPackageImpl.BINDINGS__BINDING_CONTEXTS: return ApplicationPackageImpl.APPLICATION__BINDING_CONTEXTS;
				default: return -1;
			}
		}
		if (baseClass == MMenuContributions.class) {
			switch (baseFeatureID) {
				case MenuPackageImpl.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS: return ApplicationPackageImpl.APPLICATION__MENU_CONTRIBUTIONS;
				default: return -1;
			}
		}
		if (baseClass == MToolBarContributions.class) {
			switch (baseFeatureID) {
				case MenuPackageImpl.TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS: return ApplicationPackageImpl.APPLICATION__TOOL_BAR_CONTRIBUTIONS;
				default: return -1;
			}
		}
		if (baseClass == MTrimContributions.class) {
			switch (baseFeatureID) {
				case MenuPackageImpl.TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS: return ApplicationPackageImpl.APPLICATION__TRIM_CONTRIBUTIONS;
				default: return -1;
			}
		}
		if (baseClass == MSnippetContainer.class) {
			switch (baseFeatureID) {
				case UiPackageImpl.SNIPPET_CONTAINER__SNIPPETS: return ApplicationPackageImpl.APPLICATION__SNIPPETS;
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
		result.append(" (context: "); //$NON-NLS-1$
		result.append(context);
		result.append(", variables: "); //$NON-NLS-1$
		result.append(variables);
		result.append(')');
		return result.toString();
	}

} //ApplicationImpl
