/*******************************************************************************
 * Copyright (c) 2011-2019 EclipseSource Muenchen GmbH and others.
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Jens Lidestrom - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal;

import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MBindingTableContainer;
import org.eclipse.e4.ui.model.application.commands.MBindings;
import org.eclipse.e4.ui.model.application.commands.MCategory;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MHandlerContainer;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.commands.MKeySequence;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptorContainer;
import org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MExpression;
import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MGenericTrimContainer;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContributions;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContributions;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContributions;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.EditingDomain;

/**
 * This class contains factory method for {@link IProperty} objects for features
 * of the e4 model classes.
 */
public class E4Properties {
	@SuppressWarnings("unchecked")
	public static IValueProperty<MApplicationElement, String> elementId(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, ApplicationPackageImpl.Literals.APPLICATION_ELEMENT__ELEMENT_ID);
	}

	@SuppressWarnings("unchecked")
	public static <E extends MUIElement> IListProperty<MElementContainer<E>, E> children(EditingDomain editingDomain) {
		return EMFEditProperties.list(editingDomain, UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
	}

	@SuppressWarnings("unchecked")
	public static <E extends MUIElement> IListProperty<MElementContainer<E>, E> children() {
		return EMFProperties.list(UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN);
	}

	@SuppressWarnings("unchecked")
	public static <E extends MUIElement> IValueProperty<MElementContainer<E>, E> selectedElement(
			EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, UiPackageImpl.Literals.ELEMENT_CONTAINER__SELECTED_ELEMENT);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MUILabel, String> label(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, UiPackageImpl.Literals.UI_LABEL__LABEL);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MUIElement, Boolean> toBeRendered(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, UiPackageImpl.Literals.UI_ELEMENT__TO_BE_RENDERED);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MUIElement, Boolean> visible(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, UiPackageImpl.Literals.UI_ELEMENT__VISIBLE);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MUIElement, MExpression> visibleWhen() {
		return EMFProperties.value(UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MUIElement, MExpression> visibleWhen(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, UiPackageImpl.Literals.UI_ELEMENT__VISIBLE_WHEN);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MCoreExpression, String> coreExpressionId(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, UiPackageImpl.Literals.CORE_EXPRESSION__CORE_EXPRESSION_ID);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MToolItem, MMenu> menu() {
		return EMFProperties.value(MenuPackageImpl.Literals.TOOL_ITEM__MENU);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MItem, ItemType> type(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, MenuPackageImpl.Literals.ITEM__TYPE);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MItem, Boolean> enabled(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, MenuPackageImpl.Literals.ITEM__ENABLED);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MItem, Boolean> selected(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, MenuPackageImpl.Literals.ITEM__SELECTED);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MUIElement, String> accessibilityPhrase(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, UiPackageImpl.Literals.UI_ELEMENT__ACCESSIBILITY_PHRASE);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MUILabel, String> tooltip(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, UiPackageImpl.Literals.UI_LABEL__TOOLTIP);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MUILabel, String> iconUri(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, UiPackageImpl.Literals.UI_LABEL__ICON_URI);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MHandledItem, MParameter> itemParameters(EditingDomain editingDomain) {
		return EMFEditProperties.list(editingDomain, MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MHandledItem, MParameter> itemParameters() {
		return EMFProperties.list(MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MHandledItem, MCommand> itemCommand(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, MenuPackageImpl.Literals.HANDLED_ITEM__COMMAND);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MHandler, MCommand> command(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, CommandsPackageImpl.Literals.HANDLER__COMMAND);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MKeyBinding, MParameter> keyBindingParameters(EditingDomain editingDomain) {
		return EMFEditProperties.list(editingDomain, CommandsPackageImpl.Literals.KEY_BINDING__PARAMETERS);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MKeySequence, String> keySequence(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, CommandsPackageImpl.Literals.KEY_SEQUENCE__KEY_SEQUENCE);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MKeyBinding, MCommand> keyBindingCommand(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, CommandsPackageImpl.Literals.KEY_BINDING__COMMAND);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MMenuContribution, String> menuParentId(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, MenuPackageImpl.Literals.MENU_CONTRIBUTION__PARENT_ID);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MMenuContribution, String> menuPositionInParent(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, MenuPackageImpl.Literals.MENU_CONTRIBUTION__POSITION_IN_PARENT);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MMenuElement, String> mnemonics(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, MenuPackageImpl.Literals.MENU_ELEMENT__MNEMONICS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MModelFragments, MModelFragment> fragments() {
		return EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__FRAGMENTS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MModelFragments, MApplicationElement> imports() {
		return EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MParameter, String> parameterValue(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, CommandsPackageImpl.Literals.PARAMETER__VALUE);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MParameter, String> parameterName(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, CommandsPackageImpl.Literals.PARAMETER__NAME);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MPartDescriptor, MMenu> partDescriptorMenus() {
		return EMFProperties.list(BasicPackageImpl.Literals.PART_DESCRIPTOR__MENUS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MHandlerContainer, MHandler> handlers() {
		return EMFProperties.list(CommandsPackageImpl.Literals.HANDLER_CONTAINER__HANDLERS);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MPartDescriptor, MToolBar> partDescriptorToolbar() {
		return EMFProperties.value(BasicPackageImpl.Literals.PART_DESCRIPTOR__TOOLBAR);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MUIElement, String> containerData(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, UiPackageImpl.Literals.UI_ELEMENT__CONTAINER_DATA);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MPartDescriptor, Boolean> closable(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, BasicPackageImpl.Literals.PART_DESCRIPTOR__CLOSEABLE);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MPartDescriptor, Boolean> allowMultiple(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, BasicPackageImpl.Literals.PART_DESCRIPTOR__ALLOW_MULTIPLE);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MPartDescriptor, String> category(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, BasicPackageImpl.Literals.PART_DESCRIPTOR__CATEGORY);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MPart, Boolean> partClosable(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain,
				org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.PART__CLOSEABLE);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MPart, MMenu> partMenus() {
		return EMFProperties
				.list(org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.PART__MENUS);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MPart, MToolBar> partToolbar() {
		return EMFProperties
				.value(org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.PART__TOOLBAR);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MGenericTile<?>, Boolean> horizontal(
			EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, UiPackageImpl.Literals.GENERIC_TILE__HORIZONTAL);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MPerspective, MWindow> perspectiveWindows() {
		return EMFProperties.list(AdvancedPackageImpl.Literals.PERSPECTIVE__WINDOWS);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MPlaceholder, MUIElement> ref(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, AdvancedPackageImpl.Literals.PLACEHOLDER__REF);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MPlaceholder, Boolean> placeholderClosable(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, AdvancedPackageImpl.Literals.PLACEHOLDER__CLOSEABLE);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MBindingTable, MBindingContext> bindingContext() {
		return EMFProperties.value(CommandsPackageImpl.Literals.BINDING_TABLE__BINDING_CONTEXT);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MBindingTableContainer, MBindingTable> bindingTables() {
		return EMFProperties.list(CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__BINDING_TABLES);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MApplication, MCommand> applicationCommands() {
		return EMFProperties.list(ApplicationPackageImpl.Literals.APPLICATION__COMMANDS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MPartDescriptorContainer, MPartDescriptor> descriptors() {
		return EMFProperties.list(BasicPackageImpl.Literals.PART_DESCRIPTOR_CONTAINER__DESCRIPTORS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MApplication, MAddon> addons() {
		return EMFProperties.list(ApplicationPackageImpl.Literals.APPLICATION__ADDONS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MMenuContributions, MMenuContribution> menuContributions() {
		return EMFProperties.list(MenuPackageImpl.Literals.MENU_CONTRIBUTIONS__MENU_CONTRIBUTIONS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MToolBarContributions, MToolBarContribution> toolBarContributions() {
		return EMFProperties.list(MenuPackageImpl.Literals.TOOL_BAR_CONTRIBUTIONS__TOOL_BAR_CONTRIBUTIONS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MTrimContributions, MTrimContribution> trimContributions() {
		return EMFProperties.list(MenuPackageImpl.Literals.TRIM_CONTRIBUTIONS__TRIM_CONTRIBUTIONS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MSnippetContainer, MUIElement> snippets() {
		return EMFProperties.list(UiPackageImpl.Literals.SNIPPET_CONTAINER__SNIPPETS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MApplication, MCategory> categories() {
		return EMFProperties.list(ApplicationPackageImpl.Literals.APPLICATION__CATEGORIES);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MBindingTableContainer, MBindingContext> rootContext() {
		return EMFProperties.list(CommandsPackageImpl.Literals.BINDING_TABLE_CONTAINER__ROOT_CONTEXT);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MModelFragment, MApplicationElement> elements() {
		return (EMFProperties.list(FragmentPackageImpl.Literals.MODEL_FRAGMENT__ELEMENTS));
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MStringModelFragment, String> parentElementId(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain,
				FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__PARENT_ELEMENT_ID);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MStringModelFragment, String> featureName(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__FEATURENAME);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MStringModelFragment, String> positionInList(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain,
				FragmentPackageImpl.Literals.STRING_MODEL_FRAGMENT__POSITION_IN_LIST);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MToolBarContribution, String> toolBarParentId(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, MenuPackageImpl.Literals.TOOL_BAR_CONTRIBUTION__PARENT_ID);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MToolBarContribution, String> toolBarPositionInParent(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain,
				MenuPackageImpl.Literals.TOOL_BAR_CONTRIBUTION__POSITION_IN_PARENT);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MContribution, String> contributionURI(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI);
	}

	@SuppressWarnings("unchecked")
	public static <E extends MUIElement> IValueProperty<MGenericTrimContainer<E>, SideValue> side(
			EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, UiPackageImpl.Literals.GENERIC_TRIM_CONTAINER__SIDE);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MTrimContribution, String> trimParentId(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, MenuPackageImpl.Literals.TRIM_CONTRIBUTION__PARENT_ID);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MTrimContribution, String> trimPositionInParent(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, MenuPackageImpl.Literals.TRIM_CONTRIBUTION__POSITION_IN_PARENT);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MTrimmedWindow, MTrimBar> windowTrimBars() {
		return EMFProperties.list(
				org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.TRIMMED_WINDOW__TRIM_BARS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MPart, MTrimBar> partTrimBars() {
		return EMFProperties
				.list(org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.PART__TRIM_BARS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MPartDescriptor, MTrimBar> partDescriptorTrimBars() {
		return EMFProperties.list(BasicPackageImpl.Literals.PART_DESCRIPTOR__TRIM_BARS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MPerspective, MTrimBar> perspectiveTrimBars() {
		return EMFProperties.list(AdvancedPackageImpl.Literals.PERSPECTIVE__TRIM_BARS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MWindow, MWindow> windowWindows() {
		return EMFProperties
				.list(org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.WINDOW__WINDOWS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MWindow, MUIElement> sharedElements() {
		return EMFProperties.list(
				org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MWindow, MUIElement> sharedElements(EditingDomain editingDomain) {
		return EMFEditProperties.list(editingDomain,
				org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.WINDOW__SHARED_ELEMENTS);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MWindow, MMenu> mainMenu() {
		return EMFProperties
				.value(org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.WINDOW__MAIN_MENU);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MWindow, Integer> windowX(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain,
				org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.WINDOW__X);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MWindow, Integer> windowY(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain,
				org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.WINDOW__Y);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MWindow, Integer> width(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain,
				org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.WINDOW__WIDTH);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MWindow, Integer> height(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain,
				org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl.Literals.WINDOW__HEIGHT);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MBindingContext, String> bindingContextName(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, CommandsPackageImpl.Literals.BINDING_CONTEXT__NAME);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MBindingContext, String> description(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, CommandsPackageImpl.Literals.BINDING_CONTEXT__DESCRIPTION);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MBindingContext, MBindingContext> bindingContextChildren() {
		return EMFProperties.list(CommandsPackageImpl.Literals.BINDING_CONTEXT__CHILDREN);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MBindings, MBindingContext> contexts() {
		return EMFProperties.list(CommandsPackageImpl.Literals.BINDINGS__BINDING_CONTEXTS);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MBindingContext, MBindingContext> bindingContextChildren(EditingDomain editingDomain) {
		return EMFEditProperties.list(editingDomain, CommandsPackageImpl.Literals.BINDING_CONTEXT__CHILDREN);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MBindingTable, MKeyBinding> bindings() {
		return EMFProperties.list(CommandsPackageImpl.Literals.BINDING_TABLE__BINDINGS);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MCategory, String> categoryName(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, CommandsPackageImpl.Literals.CATEGORY__NAME);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MCategory, String> categoryDescription(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, CommandsPackageImpl.Literals.CATEGORY__DESCRIPTION);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<MCommand, MCommandParameter> commandParameters(EditingDomain editingDomain) {
		return EMFEditProperties.list(editingDomain, CommandsPackageImpl.Literals.COMMAND__PARAMETERS);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MCommand, String> commandName(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, CommandsPackageImpl.Literals.COMMAND__COMMAND_NAME);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MCommand, String> commandDescription(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, CommandsPackageImpl.Literals.COMMAND__DESCRIPTION);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MCommand, MCategory> category() {
		return EMFProperties.value(CommandsPackageImpl.Literals.COMMAND__CATEGORY);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MCommandParameter, String> commandParameterName(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, CommandsPackageImpl.Literals.COMMAND_PARAMETER__NAME);
	}

	@SuppressWarnings("unchecked")
	public static IValueProperty<MCommandParameter, Boolean> optional(EditingDomain editingDomain) {
		return EMFEditProperties.value(editingDomain, CommandsPackageImpl.Literals.COMMAND_PARAMETER__OPTIONAL);
	}

	@SuppressWarnings("unchecked")
	public static IListProperty<Resource, EObject> resource(EditingDomain editingDomain) {
		return EMFEditProperties.resource(editingDomain);
	}
}
