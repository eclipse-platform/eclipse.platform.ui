/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.modeling;

import java.util.Collection;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;

/**
 * @noreference This class is not intended to be referenced by clients.
 * @since 1.0
 */
public abstract class ModelReconciler {

	/**
	 * Attribute defined by MApplicationElements (value is <code>elementId</code>).
	 */
	public static final String APPLICATIONELEMENT_ELEMENTID_ATTNAME = ApplicationPackageImpl.eINSTANCE
			.getApplicationElement_ElementId().getName();

	/**
	 * Attribute defined by MApplicationElements (value is <code>tags</code>).
	 */
	public static final String APPLICATIONELEMENT_TAGS_ATTNAME = ApplicationPackageImpl.eINSTANCE
			.getApplicationElement_Tags().getName();

	/**
	 * Attribute defined by MApplicationElements (value is <code>clonableSnippets</code>).
	 */
	public static final String SNIPPETCONTAINER_SNIPPETS_ATTNAME = UiPackageImpl.eINSTANCE
			.getSnippetContainer_Snippets().getName();

	/**
	 * Attribute defined by MApplications (value is <code>commands</code>).
	 */
	public static final String APPLICATION_COMMANDS_ATTNAME = ApplicationPackageImpl.eINSTANCE
			.getApplication_Commands().getName();

	/**
	 * Attribute defined by MApplications (value is <code>addons</code>).
	 */
	public static final String APPLICATION_ADDONS_ATTNAME = ApplicationPackageImpl.eINSTANCE
			.getApplication_Addons().getName();

	/**
	 * Attribute defined by MContexts (value is <code>properties</code>).
	 */
	public static final String CONTEXT_PROPERTIES_ATTNAME = UiPackageImpl.eINSTANCE
			.getContext_Properties().getName();

	/**
	 * Attribute defined by MApplicationElements (value is <code>persistedState</code>).
	 */
	public static final String APPLICATIONELEMENT_PERSISTEDSTATE_ATTNAME = ApplicationPackageImpl.eINSTANCE
			.getApplicationElement_PersistedState().getName();

	/**
	 * Attribute defined by MContributions (value is <code>contributionURI</code>).
	 */
	public static final String CONTRIBUTION_URI_ATTNAME = ApplicationPackageImpl.eINSTANCE
			.getContribution_ContributionURI().getName();

	/**
	 * Attribute defined by MHandlerContainers (value is <code>handlers</code>).
	 */
	public static final String HANDLERCONTAINER_HANDLERS_ATTNAME = CommandsPackageImpl.eINSTANCE
			.getHandlerContainer_Handlers().getName();

	/**
	 * Attribute defined by MUILabels (value is <code>label</code>).
	 */
	public static final String UILABEL_LABEL_ATTNAME = UiPackageImpl.eINSTANCE.getUILabel_Label()
			.getName();

	/**
	 * Attribute defined by MUILabels (value is <code>tooltip</code>).
	 */
	public static final String UILABEL_TOOLTIP_ATTNAME = UiPackageImpl.eINSTANCE
			.getUILabel_Tooltip().getName();

	/**
	 * Attribute defined by MUILabels (value is <code>iconURI</code>).
	 */
	public static final String UILABEL_ICONURI_ATTNAME = UiPackageImpl.eINSTANCE
			.getUILabel_IconURI().getName();

	/**
	 * Attribute defined by MUIElements (value is <code>toBeRendered</code>).
	 */
	public static final String UIELEMENT_TOBERENDERED_ATTNAME = UiPackageImpl.eINSTANCE
			.getUIElement_ToBeRendered().getName();

	/**
	 * Attribute defined by MUIElements (value is <code>visible</code>).
	 */
	public static final String UIELEMENT_VISIBLE_ATTNAME = UiPackageImpl.eINSTANCE
			.getUIElement_Visible().getName();

	/**
	 * Attribute defined by MUIElements (value is <code>parent</code>).
	 */
	public static final String UIELEMENT_PARENT_ATTNAME = UiPackageImpl.eINSTANCE
			.getUIElement_Parent().getName();

	/**
	 * Attribute defined by MUIElements (value is <code>containerData</code>).
	 */
	public static final String UIELEMENT_CONTAINERDATA_ATTNAME = UiPackageImpl.eINSTANCE
			.getUIElement_ContainerData().getName();

	/**
	 * Attribute defined by MUIElements (value is <code>visibleWhen</code>).
	 */
	public static final String UIELEMENT_VISIBLEWHEN_ATTNAME = UiPackageImpl.eINSTANCE
			.getUIElement_VisibleWhen().getName();

	/**
	 * Attribute defined by MElementContainers (value is <code>children</code>).
	 */
	public static final String ELEMENTCONTAINER_CHILDREN_ATTNAME = UiPackageImpl.eINSTANCE
			.getElementContainer_Children().getName();

	/**
	 * Attribute defined by MElementContainers (value is <code>selectedElement</code>).
	 */
	public static final String ELEMENTCONTAINER_SELECTEDELEMENT_ATTNAME = UiPackageImpl.eINSTANCE
			.getElementContainer_SelectedElement().getName();

	/**
	 * Attribute defined by MWindows (value is <code>x</code>).
	 */
	public static final String WINDOW_X_ATTNAME = BasicPackageImpl.eINSTANCE.getWindow_X()
			.getName();

	/**
	 * Attribute defined by MWindows (value is <code>y</code>).
	 */
	public static final String WINDOW_Y_ATTNAME = BasicPackageImpl.eINSTANCE.getWindow_Y()
			.getName();

	/**
	 * Attribute defined by MWindows (value is <code>width</code>).
	 */
	public static final String WINDOW_WIDTH_ATTNAME = BasicPackageImpl.eINSTANCE.getWindow_Width()
			.getName();

	/**
	 * Attribute defined by MWindows (value is <code>height</code>).
	 */
	public static final String WINDOW_HEIGHT_ATTNAME = BasicPackageImpl.eINSTANCE
			.getWindow_Height().getName();

	/**
	 * Attribute defined by MWindows (value is <code>mainMenu</code>).
	 */
	public static final String WINDOW_MAINMENU_ATTNAME = BasicPackageImpl.eINSTANCE
			.getWindow_MainMenu().getName();

	/**
	 * Attribute defined by MWindows (value is <code>sharedElements</code>).
	 */
	public static final String WINDOW_SHAREDELEMENTS_ATTNAME = BasicPackageImpl.eINSTANCE
			.getWindow_SharedElements().getName();

	/**
	 * Attribute defined by MTrimmedWindows (value is <code>trimBars</code>).
	 */
	public static final String TRIMMEDWINDOW_TRIMBARS_ATTNAME = BasicPackageImpl.eINSTANCE
			.getTrimmedWindow_TrimBars().getName();

	/**
	 * Attribute defined by MCommands (value is <code>commandName</code>).
	 */
	public static final String COMMAND_COMMANDNAME_ATTNAME = CommandsPackageImpl.eINSTANCE
			.getCommand_CommandName().getName();

	/**
	 * Attribute defined by MCommands (value is <code>description</code>).
	 */
	public static final String COMMAND_DESCRIPTION_ATTNAME = CommandsPackageImpl.eINSTANCE
			.getCommand_Description().getName();

	/**
	 * Attribute defined by MCommands (value is <code>parameters</code>).
	 */
	public static final String COMMAND_PARAMETERS_ATTNAME = CommandsPackageImpl.eINSTANCE
			.getCommand_Parameters().getName();

	/**
	 * Attribute defined by MKeySequences (value is <code>keySequence</code>).
	 */
	public static final String KEYSEQUENCE_KEYSEQUENCE_ATTNAME = CommandsPackageImpl.eINSTANCE
			.getKeySequence_KeySequence().getName();

	/**
	 * Attribute defined by MParts (value is <code>closeable</code>).
	 */
	public static final String PART_CLOSEABLE_ATTNAME = BasicPackageImpl.eINSTANCE
			.getPart_Closeable().getName();

	/**
	 * Attribute defined by MParts (value is <code>menus</code>).
	 */
	public static final String PART_MENUS_ATTNAME = BasicPackageImpl.eINSTANCE.getPart_Menus()
			.getName();

	/**
	 * Attribute defined by MParts (value is <code>toolbar</code>).
	 */
	public static final String PART_TOOLBAR_ATTNAME = BasicPackageImpl.eINSTANCE.getPart_Toolbar()
			.getName();

	/**
	 * Attribute defined by MInputs (value is <code>inputURI</code>).
	 */
	public static final String INPUT_INPUTURI_ATTNAME = UiPackageImpl.eINSTANCE.getInput_InputURI()
			.getName();

	/**
	 * Attribute defined by MGenericTiles (value is <code>horizontal</code>).
	 */
	public static final String GENERICTILE_HORIZONTAL_ATTNAME = UiPackageImpl.eINSTANCE
			.getGenericTile_Horizontal().getName();

	/**
	 * Attribute defined by MGenericTrimContainers (value is <code>side</code>).
	 */
	public static final String GENERICTRIMCONTAINER_SIDE_ATTNAME = UiPackageImpl.eINSTANCE
			.getGenericTrimContainer_Side().getName();

	/**
	 * Attribute defined by MBindingContainers (value is <code>rootContext</code>).
	 */
	public static final String BINDINGCONTAINER_ROOTCONTEXT_ATTNAME = CommandsPackageImpl.eINSTANCE
			.getBindingTableContainer_RootContext().getName();

	/**
	 * Attribute defined by MBindingContainers (value is <code>bindingTables</code>).
	 */
	public static final String BINDINGCONTAINER_BINDINGTABLES_ATTNAME = CommandsPackageImpl.eINSTANCE
			.getBindingTableContainer_BindingTables().getName();

	/**
	 * Attribute defined by MBindingTables (value is <code>bindings</code>).
	 */
	public static final String BINDINGTABLE_BINDINGS_ATTNAME = CommandsPackageImpl.eINSTANCE
			.getBindingTable_Bindings().getName();

	/**
	 * Attribute defined by MBindingTables (value is <code>bindingContext</code>).
	 */
	public static final String BINDINGTABLE_BINDINGCONTEXT_ATTNAME = CommandsPackageImpl.eINSTANCE
			.getBindingTable_BindingContext().getName();

	/**
	 * Attribute defined by MBindings (value is <code>bindingContexts</code>).
	 */
	public static final String BINDINGS_BINDINGCONTEXTS_ATTNAME = CommandsPackageImpl.eINSTANCE
			.getBindings_BindingContexts().getName();

	/**
	 * Attribute defined by MHandlers (value is <code>command</code>).
	 */
	public static final String HANDLER_COMMAND_ATTNAME = CommandsPackageImpl.eINSTANCE
			.getHandler_Command().getName();

	/**
	 * Attribute defined by MHandledItems (value is <code>command</code>).
	 */
	public static final String HANDLEDITEM_COMMAND_ATTNAME = MenuPackageImpl.eINSTANCE
			.getHandledItem_Command().getName();

	/**
	 * Attribute defined by MHandledItems (value is <code>parameters</code>).
	 */
	public static final String HANDLEDITEM_PARAMETERS_ATTNAME = MenuPackageImpl.eINSTANCE
			.getHandledItem_Parameters().getName();

	/**
	 * Attribute defined by MKeyBindings (value is <code>command</code>).
	 */
	public static final String KEYBINDING_COMMAND_ATTNAME = CommandsPackageImpl.eINSTANCE
			.getKeyBinding_Command().getName();

	/**
	 * Attribute defined by MItems (value is <code>enabled</code>).
	 */
	public static final String ITEM_ENABLED_ATTNAME = MenuPackageImpl.eINSTANCE.getItem_Enabled()
			.getName();

	/**
	 * Attribute defined by MItems (value is <code>selected</code>).
	 */
	public static final String ITEM_SELECTED_ATTNAME = MenuPackageImpl.eINSTANCE.getItem_Selected()
			.getName();

	/**
	 * Attribute defined by MItems (value is <code>type</code>).
	 */
	public static final String ITEM_TYPE_ATTNAME = MenuPackageImpl.eINSTANCE.getItem_Type()
			.getName();

	/**
	 * Attribute defined by MMenuItems (value is <code>mnemonics</code>).
	 */
	public static final String MENUITEM_MNEMONICS_ATTNAME = MenuPackageImpl.eINSTANCE
			.getMenuElement_Mnemonics().getName();

	/**
	 * Attribute defined by MTrimContribution (value is <code>parentId</code>).
	 */
	public static final String TRIMCONTRIBUTION_PARENTID_ATTNAME = MenuPackageImpl.eINSTANCE
			.getTrimContribution_ParentId().getName();

	/**
	 * Attribute defined by MTrimContribution (value is <code>positionInParent</code>).
	 */
	public static final String TRIMCONTRIBUTION_POSITIONINPARENT_ATTNAME = MenuPackageImpl.eINSTANCE
			.getTrimContribution_PositionInParent().getName();

	/**
	 * Attribute defined by MTrimContributions (value is <code>trimContributions</code>).
	 */
	public static final String TRIMCONTRIBUTIONS_TRIMCONTRIBUTIONS_ATTNAME = MenuPackageImpl.eINSTANCE
			.getTrimContributions_TrimContributions().getName();

	/**
	 * Attribute defined by MToolBarContribution (value is <code>parentId</code>).
	 */
	public static final String TOOLBARCONTRIBUTION_PARENTID_ATTNAME = MenuPackageImpl.eINSTANCE
			.getToolBarContribution_ParentId().getName();

	/**
	 * Attribute defined by MToolBarContribution (value is <code>positionInParent</code>).
	 */
	public static final String TOOLBARCONTRIBUTION_POSITIONINPARENT_ATTNAME = MenuPackageImpl.eINSTANCE
			.getToolBarContribution_PositionInParent().getName();

	/**
	 * Attribute defined by MToolBarContributions (value is <code>toolBarContributions</code>).
	 */
	public static final String TOOLBARCONTRIBUTIONS_TOOLBARCONTRIBUTIONS_ATTNAME = MenuPackageImpl.eINSTANCE
			.getToolBarContributions_ToolBarContributions().getName();

	/**
	 * Attribute defined by MMenuContributions (value is <code>menuContributions</code>).
	 */
	public static final String MENUCONTRIBUTIONS_MENUCONTRIBUTIONS_ATTNAME = MenuPackageImpl.eINSTANCE
			.getMenuContributions_MenuContributions().getName();

	/**
	 * Attribute defined by MMenuContribution (value is <code>positionInParent</code>).
	 */
	public static final String MENUCONTRIBUTION_POSITIONINPARENT_ATTNAME = MenuPackageImpl.eINSTANCE
			.getMenuContribution_PositionInParent().getName();

	/**
	 * Attribute defined by MMenuContribution (value is <code>parentID</code>).
	 */
	public static final String MENUCONTRIBUTION_PARENTID_ATTNAME = MenuPackageImpl.eINSTANCE
			.getMenuContribution_ParentId().getName();

	/**
	 * Attribute defined by MPartDescriptor (value is <code>allowMultiple</code>).
	 */
	public static final String PARTDESCRIPTOR_ALLOWMULTIPLE_ATTNAME = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE
			.getPartDescriptor_AllowMultiple().getName();

	/**
	 * Attribute defined by MPartDescriptor (value is <code>category</code>).
	 */
	public static final String PARTDESCRIPTOR_CATEGORY_ATTNAME = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE
			.getPartDescriptor_Category().getName();

	/**
	 * Attribute defined by MPartDescriptorContainers (value is <code>descriptors</code>).
	 */
	public static final String PARTDESCRIPTORCONTAINER_DESCRIPTORS_ATTNAME = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE
			.getPartDescriptorContainer_Descriptors().getName();

	/**
	 * Attribute defined by MPartDescriptor (value is <code>menus</code>).
	 */
	public static final String PARTDESCRIPTOR_MENUS_ATTNAME = org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE
			.getPartDescriptor_Menus().getName();

	/**
	 * Attribute defined by MPlaceholders (value is <code>ref</code>).
	 */
	public static final String PLACEHOLDER_REF_NAME = AdvancedPackageImpl.eINSTANCE
			.getPlaceholder_Ref().getName();

	/**
	 * Attribute defined by MParameters (value is <code>name</code>).
	 */
	public static final String PARAMETER_NAME_ATTNAME = CommandsPackageImpl.eINSTANCE
			.getParameter_Name().getName();

	/**
	 * Attribute defined by MParameters (value is <code>value</code>).
	 */
	public static final String PARAMETER_VALUE_ATTNAME = CommandsPackageImpl.eINSTANCE
			.getParameter_Value().getName();

	/**
	 * Attribute defined by MCoreExpressions (value is <code>coreExpressionId</code>).
	 */
	public static final String COREEXPRESSION_COREEXPRESSIONID_ATTNAME = UiPackageImpl.eINSTANCE
			.getCoreExpression_CoreExpressionId().getName();

	/**
	 * Attribute defined by MPerspective (value is <code>windows</code>).
	 */
	public static final String PERSPECTIVE_WINDOWS_ATTNAME = AdvancedPackageImpl.eINSTANCE
			.getPerspective_Windows().getName();

	/**
	 * Begin recording changes on the specified object. All changes contained within child elements
	 * of the object will also be recorded. When the desired changes have been captured,
	 * {@link #serialize()} should be called.
	 *
	 * @param object
	 *            the object to monitor changes for, must not be <code>null</code>
	 */
	public abstract void recordChanges(Object object);

	/**
	 * Serializes all the changes that have been captured since the last call to
	 * {@link #recordChanges(Object)} and returns an object that can be used later with
	 * {@link #constructDeltas(Object, Object)}.
	 *
	 * @return a serialization of all the changes that have been made to the model since the last
	 *         call to <code>recordChanges(Object)</code>
	 */
	public abstract Object serialize();

	/**
	 * Analyzes the model and its serialized state and constructs a collection of deltas between the
	 * two.
	 *
	 * @param model
	 *            the object to apply changes to
	 * @param serializedState
	 *            an object that was returned from {@link #serialize()}
	 * @return a collection of operations that can be applied to alter the model to the state it was
	 *         in due to the serialized delta changes
	 */
	public abstract Collection<ModelDelta> constructDeltas(Object model, Object serializedState);
}
