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
import org.eclipse.e4.ui.model.application.provider.ContributionItemProvider;
import org.eclipse.e4.ui.model.application.provider.UIElementsEditPlugin;
import org.eclipse.e4.ui.model.application.ui.MUiFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
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
 * This is the item provider adapter for a {@link org.eclipse.e4.ui.model.application.ui.basic.MPart} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class PartItemProvider
	extends ContributionItemProvider
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
	public PartItemProvider(AdapterFactory adapterFactory) {
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

			addContextPropertyDescriptor(object);
			addVariablesPropertyDescriptor(object);
			addToBeRenderedPropertyDescriptor(object);
			addOnTopPropertyDescriptor(object);
			addVisiblePropertyDescriptor(object);
			addContainerDataPropertyDescriptor(object);
			addCurSharedRefPropertyDescriptor(object);
			addAccessibilityPhrasePropertyDescriptor(object);
			addLabelPropertyDescriptor(object);
			addIconURIPropertyDescriptor(object);
			addTooltipPropertyDescriptor(object);
			addDirtyPropertyDescriptor(object);
			addBindingContextsPropertyDescriptor(object);
			addCloseablePropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
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
				 UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED,
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
				 UiPackageImpl.Literals.UI_ELEMENT__ON_TOP,
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
				 UiPackageImpl.Literals.UI_ELEMENT__VISIBLE,
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
				 UiPackageImpl.Literals.UI_ELEMENT__CONTAINER_DATA,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Cur Shared Ref feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCurSharedRefPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UIElement_curSharedRef_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UIElement_curSharedRef_feature", "_UI_UIElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 UiPackageImpl.Literals.UI_ELEMENT__CUR_SHARED_REF,
				 true,
				 false,
				 true,
				 null,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Accessibility Phrase feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addAccessibilityPhrasePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_UIElement_accessibilityPhrase_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_UIElement_accessibilityPhrase_feature", "_UI_UIElement_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 UiPackageImpl.Literals.UI_ELEMENT__ACCESSIBILITY_PHRASE,
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
				 UiPackageImpl.Literals.DIRTYABLE__DIRTY,
				 true,
				 false,
				 false,
				 ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE,
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
				 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
				 null,
				 null));
	}

	/**
	 * This adds a property descriptor for the Closeable feature.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void addCloseablePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add
			(createItemPropertyDescriptor
				(((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
				 getResourceLocator(),
				 getString("_UI_Part_closeable_feature"), //$NON-NLS-1$
				 getString("_UI_PropertyDescriptor_description", "_UI_Part_closeable_feature", "_UI_Part_type"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				 BasicPackageImpl.Literals.PART__CLOSEABLE,
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
			childrenFeatures.add(UiPackageImpl.Literals.CONTEXT__PROPERTIES);
			childrenFeatures.add(UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN);
			childrenFeatures.add(CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS);
			childrenFeatures.add(BasicPackageImpl.Literals.PART__MENUS);
			childrenFeatures.add(BasicPackageImpl.Literals.PART__TOOLBAR);
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
	 * This returns Part.gif.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/Part")); //$NON-NLS-1$
	}

	/**
	 * This returns the label text for the adapted class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public String getText(Object object) {
		String label = ((MPart)object).getContributionURI();
		return label == null || label.length() == 0 ?
			getString("_UI_Part_type") : //$NON-NLS-1$
			getString("_UI_Part_type") + " -> " + label; //$NON-NLS-1$ //$NON-NLS-2$
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

		switch (notification.getFeatureID(MPart.class)) {
			case BasicPackageImpl.PART__CONTEXT:
			case BasicPackageImpl.PART__VARIABLES:
			case BasicPackageImpl.PART__WIDGET:
			case BasicPackageImpl.PART__RENDERER:
			case BasicPackageImpl.PART__TO_BE_RENDERED:
			case BasicPackageImpl.PART__ON_TOP:
			case BasicPackageImpl.PART__VISIBLE:
			case BasicPackageImpl.PART__CONTAINER_DATA:
			case BasicPackageImpl.PART__ACCESSIBILITY_PHRASE:
			case BasicPackageImpl.PART__LABEL:
			case BasicPackageImpl.PART__ICON_URI:
			case BasicPackageImpl.PART__TOOLTIP:
			case BasicPackageImpl.PART__DIRTY:
			case BasicPackageImpl.PART__BINDING_CONTEXTS:
			case BasicPackageImpl.PART__CLOSEABLE:
				fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
				return;
			case BasicPackageImpl.PART__PROPERTIES:
			case BasicPackageImpl.PART__VISIBLE_WHEN:
			case BasicPackageImpl.PART__HANDLERS:
			case BasicPackageImpl.PART__MENUS:
			case BasicPackageImpl.PART__TOOLBAR:
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
				(UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN,
				 MUiFactory.INSTANCE.createCoreExpression()));

		newChildDescriptors.add
			(createChildParameter
				(CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS,
				 MCommandsFactory.INSTANCE.createHandler()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.PART__MENUS,
				 MMenuFactory.INSTANCE.createMenu()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.PART__MENUS,
				 MMenuFactory.INSTANCE.createPopupMenu()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.PART__MENUS,
				 MMenuFactory.INSTANCE.createRenderedMenu()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.PART__TOOLBAR,
				 MMenuFactory.INSTANCE.createToolBar()));

		newChildDescriptors.add
			(createChildParameter
				(BasicPackageImpl.Literals.PART__TOOLBAR,
				 MMenuFactory.INSTANCE.createRenderedToolBar()));
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
