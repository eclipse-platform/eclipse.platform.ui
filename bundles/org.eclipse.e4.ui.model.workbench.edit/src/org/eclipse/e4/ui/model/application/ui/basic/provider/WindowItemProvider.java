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
package org.eclipse.e4.ui.model.application.ui.basic.provider;


import java.util.Collection;
import java.util.List;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.provider.UIElementsEditPlugin;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.provider.ElementContainerItemProvider;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ViewerNotification;

/**
 * This is the item provider adapter for a {@link org.eclipse.e4.ui.model.application.ui.basic.MWindow} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class WindowItemProvider
	extends ElementContainerItemProvider
	implements
		IEditingDomainItemProvider,
		IStructuredItemContentProvider,
		ITreeItemContentProvider,
		IItemLabelProvider,
		IItemPropertySource {
	/**
	 * This constructs an instance from a factory and a notifier.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WindowItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	/**
	 * This returns the property descriptors for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
		if (itemPropertyDescriptors == null) {
			super.getPropertyDescriptors(object);

			addLabelPropertyDescriptor(object);
			addIconURIPropertyDescriptor(object);
			addTooltipPropertyDescriptor(object);
			addContextPropertyDescriptor(object);
			addVariablesPropertyDescriptor(object);
			addBindingContextsPropertyDescriptor(object);
			addXPropertyDescriptor(object);
			addYPropertyDescriptor(object);
			addWidthPropertyDescriptor(object);
			addHeightPropertyDescriptor(object);
			addSharedElementsPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Label feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addLabelPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UILabel_label_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UILabel_label_feature", "_UI_UILabel_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 UiPackageImpl.Literals.UI_LABEL__LABEL,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Icon URI feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addIconURIPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UILabel_iconURI_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UILabel_iconURI_feature", "_UI_UILabel_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 UiPackageImpl.Literals.UI_LABEL__ICON_URI,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Tooltip feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addTooltipPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UILabel_tooltip_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UILabel_tooltip_feature", "_UI_UILabel_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 UiPackageImpl.Literals.UI_LABEL__TOOLTIP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Context feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addContextPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Context_context_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Context_context_feature", "_UI_Context_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 UiPackageImpl.Literals.CONTEXT__CONTEXT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Variables feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addVariablesPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Context_variables_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Context_variables_feature", "_UI_Context_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 UiPackageImpl.Literals.CONTEXT__VARIABLES,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Binding Contexts feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addBindingContextsPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Bindings_bindingContexts_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Bindings_bindingContexts_feature", "_UI_Bindings_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 CommandsPackageImpl.Literals.BINDINGS__BINDING_CONTEXTS,
				 true,
				 false,
				 false,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the X feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addXPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Window_x_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Window_x_feature", "_UI_Window_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 BasicPackageImpl.Literals.WINDOW__X,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Y feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addYPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Window_y_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Window_y_feature", "_UI_Window_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 BasicPackageImpl.Literals.WINDOW__Y,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Width feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addWidthPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Window_width_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Window_width_feature", "_UI_Window_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 BasicPackageImpl.Literals.WINDOW__WIDTH,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Height feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addHeightPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Window_height_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Window_height_feature", "_UI_Window_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 BasicPackageImpl.Literals.WINDOW__HEIGHT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.INTEGRAL_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Shared Elements feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addSharedElementsPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Window_sharedElements_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Window_sharedElements_feature", "_UI_Window_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 true,
				 false,
				 false,
				 null,
				 null,
				 null));
	}

	/**
	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
	 * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
	 * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
		if (childrenFeatures == null) {
			super.getChildrenFeatures(object);
			childrenFeatures.add(UiPackageImpl.Literals.CONTEXT__PROPERTIES);
			childrenFeatures.add(CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS);
			childrenFeatures.add(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS);
			childrenFeatures.add(BasicPackageImpl.Literals.WINDOW__MAIN_MENU);
			childrenFeatures.add(BasicPackageImpl.Literals.WINDOW__WINDOWS);
			childrenFeatures.add(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS);
		}
		return childrenFeatures;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EStructuralFeature getChildFeature(Object object, Object child) {
		// Check the type of the specified child object and return the proper feature to use for
		// adding (see {@link AddCommand}) it as a child.

		return super.getChildFeature(object, child);
	}

	/**
	 * This returns Window.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/Window")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public String getText(Object object) {
		String label = ((MWindow)object).getLabel();
		return label == null || label.length() == 0 ?
			getString("_UI_Window_type") : //$NON-NLS-1$
			getString("_UI_Window_type") + " - " + label; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * This handles model notifications by calling {@link #updateChildren} to update any cached
	 * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void notifyChanged(Notification notification) {
		updateChildren(notification);

		switch (notification.getFeatureID(MWindow.class)) {
			case BasicPackageImpl.WINDOW__LABEL:
			case BasicPackageImpl.WINDOW__ICON_URI:
			case BasicPackageImpl.WINDOW__TOOLTIP:
			case BasicPackageImpl.WINDOW__CONTEXT:
			case BasicPackageImpl.WINDOW__VARIABLES:
			case BasicPackageImpl.WINDOW__BINDING_CONTEXTS:
			case BasicPackageImpl.WINDOW__X:
			case BasicPackageImpl.WINDOW__Y:
			case BasicPackageImpl.WINDOW__WIDTH:
			case BasicPackageImpl.WINDOW__HEIGHT:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case BasicPackageImpl.WINDOW__PROPERTIES:
			case BasicPackageImpl.WINDOW__HANDLERS:
			case BasicPackageImpl.WINDOW__SNIPPETS:
			case BasicPackageImpl.WINDOW__MAIN_MENU:
			case BasicPackageImpl.WINDOW__WINDOWS:
			case BasicPackageImpl.WINDOW__SHARED_ELEMENTS:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
				return;
		}
		super.notifyChanged(notification);
	}

	/**
	 * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
	 * that can be created under this object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
		super.collectNewChildDescriptors(newChildDescriptors, object);

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.CONTEXT__PROPERTIES,
				 ((EFactory)MApplicationFactory.INSTANCE).create(ApplicationPackageImpl.Literals.STRING_TO_STRING_MAP)));

		newChildDescriptors.add
			(createChildParameter
				(CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS,
				 MCommandsFactory.INSTANCE.createHandler()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MBasicFactory.INSTANCE.createPart()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MBasicFactory.INSTANCE.createInputPart()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MBasicFactory.INSTANCE.createPartStack()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MBasicFactory.INSTANCE.createPartSashContainer()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MBasicFactory.INSTANCE.createWindow()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MBasicFactory.INSTANCE.createTrimmedWindow()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MBasicFactory.INSTANCE.createTrimBar()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MApplicationFactory.INSTANCE.createApplication()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createMenuSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createMenu()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createMenuContribution()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createPopupMenu()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createDirectMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createHandledMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createToolControl()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createHandledToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createDirectToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createToolBarSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createRenderedMenu()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createRenderedToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createToolBarContribution()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createTrimContribution()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createRenderedMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createOpaqueToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createOpaqueMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createOpaqueMenuSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MMenuFactory.INSTANCE.createOpaqueMenu()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MAdvancedFactory.INSTANCE.createPlaceholder()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MAdvancedFactory.INSTANCE.createPerspective()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MAdvancedFactory.INSTANCE.createPerspectiveStack()));

		newChildDescriptors.add
			(createChildParameter
				(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS,
				 MAdvancedFactory.INSTANCE.createArea()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__MAIN_MENU,
				 MMenuFactory.INSTANCE.createMenu()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__MAIN_MENU,
				 MMenuFactory.INSTANCE.createPopupMenu()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__MAIN_MENU,
				 MMenuFactory.INSTANCE.createRenderedMenu()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__MAIN_MENU,
				 MMenuFactory.INSTANCE.createOpaqueMenu()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__WINDOWS,
				 MBasicFactory.INSTANCE.createWindow()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__WINDOWS,
				 MBasicFactory.INSTANCE.createTrimmedWindow()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MBasicFactory.INSTANCE.createPart()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MBasicFactory.INSTANCE.createInputPart()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MBasicFactory.INSTANCE.createPartStack()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MBasicFactory.INSTANCE.createPartSashContainer()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MBasicFactory.INSTANCE.createWindow()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MBasicFactory.INSTANCE.createTrimmedWindow()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MBasicFactory.INSTANCE.createTrimBar()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MApplicationFactory.INSTANCE.createApplication()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createMenuSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createMenu()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createMenuContribution()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createPopupMenu()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createDirectMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createHandledMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createToolControl()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createHandledToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createDirectToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createToolBarSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createRenderedMenu()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createRenderedToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createToolBarContribution()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createTrimContribution()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createRenderedMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createOpaqueToolItem()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createOpaqueMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createOpaqueMenuSeparator()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MMenuFactory.INSTANCE.createOpaqueMenu()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MAdvancedFactory.INSTANCE.createPlaceholder()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MAdvancedFactory.INSTANCE.createPerspective()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MAdvancedFactory.INSTANCE.createPerspectiveStack()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS,
				 MAdvancedFactory.INSTANCE.createArea()));
	}

	/**
	 * This returns the label text for {@link org.eclipse.emf.edit.command.CreateChildCommand}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCreateChildText(Object owner, Object feature, Object child, Collection<?> selection) {
		Object childFeature = feature;
		Object childObject = child;

		boolean qualify =
			childFeature == ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__PERSISTED_STATE ||
			childFeature == UiPackageImpl.Literals.CONTEXT__PROPERTIES ||
			childFeature == UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN ||
			childFeature == UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS ||
			childFeature == BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS ||
			childFeature == BasicPackageImpl.Literals.WINDOW__WINDOWS ||
			childFeature == BasicPackageImpl.Literals.WINDOW__MAIN_MENU;

		if (qualify) {
			return getString
				("_UI_CreateChild_text2", //$NON-NLS-1$
				 new Object[] { getTypeText(childObject), getFeatureText(childFeature), getTypeText(owner) });
		}
		return super.getCreateChildText(owner, feature, child, selection);
	}

	/**
	 * Return the resource locator for this item provider's resources.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return UIElementsEditPlugin.INSTANCE;
	}

}
