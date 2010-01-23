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
package org.eclipse.e4.ui.model.application.provider;


import java.util.Collection;
import java.util.List;

import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MEditor;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

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
 * This is the item provider adapter for a {@link org.eclipse.e4.ui.model.application.MEditor} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class EditorItemProvider
	extends InputItemProvider
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
	public EditorItemProvider(AdapterFactory adapterFactory) {
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

			addIdPropertyDescriptor(object);
			addStylePropertyDescriptor(object);
			addURIPropertyDescriptor(object);
			addObjectPropertyDescriptor(object);
			addPersistedStatePropertyDescriptor(object);
			addContextPropertyDescriptor(object);
			addVariablesPropertyDescriptor(object);
			addWidgetPropertyDescriptor(object);
			addRendererPropertyDescriptor(object);
			addToBeRenderedPropertyDescriptor(object);
			addOnTopPropertyDescriptor(object);
			addVisiblePropertyDescriptor(object);
			addContainerDataPropertyDescriptor(object);
			addLabelPropertyDescriptor(object);
			addIconURIPropertyDescriptor(object);
			addTooltipPropertyDescriptor(object);
			addMenusPropertyDescriptor(object);
			addToolbarPropertyDescriptor(object);
			addDirtyPropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Id feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addIdPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_ApplicationElement_id_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_ApplicationElement_id_feature", "_UI_ApplicationElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.APPLICATION_ELEMENT__ID,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Style feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addStylePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_ApplicationElement_style_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_ApplicationElement_style_feature", "_UI_ApplicationElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.APPLICATION_ELEMENT__STYLE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the URI feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addURIPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Contribution_URI_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Contribution_URI_feature", "_UI_Contribution_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.CONTRIBUTION__URI,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Object feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addObjectPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Contribution_object_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Contribution_object_feature", "_UI_Contribution_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.CONTRIBUTION__OBJECT,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Persisted State feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addPersistedStatePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Contribution_persistedState_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Contribution_persistedState_feature", "_UI_Contribution_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.CONTRIBUTION__PERSISTED_STATE,
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
				 MApplicationPackage.Literals.CONTEXT__CONTEXT,
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
				 MApplicationPackage.Literals.CONTEXT__VARIABLES,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Widget feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addWidgetPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UIElement_widget_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UIElement_widget_feature", "_UI_UIElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.UI_ELEMENT__WIDGET,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Renderer feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addRendererPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UIElement_renderer_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UIElement_renderer_feature", "_UI_UIElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.UI_ELEMENT__RENDERER,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the To Be Rendered feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addToBeRenderedPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UIElement_toBeRendered_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UIElement_toBeRendered_feature", "_UI_UIElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.UI_ELEMENT__TO_BE_RENDERED,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the On Top feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addOnTopPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UIElement_onTop_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UIElement_onTop_feature", "_UI_UIElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.UI_ELEMENT__ON_TOP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Visible feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addVisiblePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UIElement_visible_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UIElement_visible_feature", "_UI_UIElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.UI_ELEMENT__VISIBLE,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Container Data feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addContainerDataPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UIElement_containerData_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UIElement_containerData_feature", "_UI_UIElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.UI_ELEMENT__CONTAINER_DATA,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
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
				 MApplicationPackage.Literals.UI_LABEL__LABEL,
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
				 MApplicationPackage.Literals.UI_LABEL__ICON_URI,
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
				 MApplicationPackage.Literals.UI_LABEL__TOOLTIP,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Menus feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addMenusPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Part_menus_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Part_menus_feature", "_UI_Part_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.PART__MENUS,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Toolbar feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addToolbarPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Part_toolbar_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Part_toolbar_feature", "_UI_Part_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.PART__TOOLBAR,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Dirty feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addDirtyPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Dirtyable_dirty_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Dirtyable_dirty_feature", "_UI_Dirtyable_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 MApplicationPackage.Literals.DIRTYABLE__DIRTY,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
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
			childrenFeatures.add(MApplicationPackage.Literals.HANDLER_CONTAINER__HANDLERS);
			childrenFeatures.add(MApplicationPackage.Literals.BINDING_CONTAINER__BINDINGS);
			childrenFeatures.add(MApplicationPackage.Literals.PART__MENUS);
			childrenFeatures.add(MApplicationPackage.Literals.PART__TOOLBAR);
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
	 * This returns Editor.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/Editor")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		String label = ((MEditor)object).getLabel();
		return label == null || label.length() == 0 ?
			getString("_UI_Editor_type") : //$NON-NLS-1$
			getString("_UI_Editor_type") + " " + label; //$NON-NLS-1$ //$NON-NLS-2$
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

		switch (notification.getFeatureID(MEditor.class)) {
			case MApplicationPackage.EDITOR__ID:
			case MApplicationPackage.EDITOR__STYLE:
			case MApplicationPackage.EDITOR__URI:
			case MApplicationPackage.EDITOR__OBJECT:
			case MApplicationPackage.EDITOR__PERSISTED_STATE:
			case MApplicationPackage.EDITOR__CONTEXT:
			case MApplicationPackage.EDITOR__VARIABLES:
			case MApplicationPackage.EDITOR__WIDGET:
			case MApplicationPackage.EDITOR__RENDERER:
			case MApplicationPackage.EDITOR__TO_BE_RENDERED:
			case MApplicationPackage.EDITOR__ON_TOP:
			case MApplicationPackage.EDITOR__VISIBLE:
			case MApplicationPackage.EDITOR__CONTAINER_DATA:
			case MApplicationPackage.EDITOR__LABEL:
			case MApplicationPackage.EDITOR__ICON_URI:
			case MApplicationPackage.EDITOR__TOOLTIP:
			case MApplicationPackage.EDITOR__DIRTY:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case MApplicationPackage.EDITOR__HANDLERS:
			case MApplicationPackage.EDITOR__BINDINGS:
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
				(MApplicationPackage.Literals.HANDLER_CONTAINER__HANDLERS,
				 MApplicationFactory.eINSTANCE.createHandler()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.BINDING_CONTAINER__BINDINGS,
				 MApplicationFactory.eINSTANCE.createKeyBinding()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.PART__MENUS,
				 MApplicationFactory.eINSTANCE.createMenu()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.PART__MENUS,
				 MApplicationFactory.eINSTANCE.createMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.PART__MENUS,
				 MApplicationFactory.eINSTANCE.createDirectMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.PART__MENUS,
				 MApplicationFactory.eINSTANCE.createHandledMenuItem()));

		newChildDescriptors.add
			(createChildParameter
				(MApplicationPackage.Literals.PART__TOOLBAR,
				 MApplicationFactory.eINSTANCE.createToolBar()));
	}

}
