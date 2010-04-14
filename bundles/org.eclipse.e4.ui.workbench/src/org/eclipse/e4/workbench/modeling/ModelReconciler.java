/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench.modeling;

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MApplicationPackage;

public abstract class ModelReconciler {

	/**
	 * Attribute defined by MApplicationElements (value is <code>id</code>).
	 */
	public static final String APPLICATIONELEMENT_ID_ATTNAME = MApplicationPackage.eINSTANCE
			.getApplicationElement_Id().getName();

	/**
	 * Attribute defined by MApplicationElements (value is <code>tags</code>).
	 */
	public static final String APPLICATIONELEMENT_TAGS_ATTNAME = MApplicationPackage.eINSTANCE
			.getApplicationElement_Tags().getName();

	/**
	 * Attribute defined by MApplications (value is <code>commands</code>).
	 */
	public static final String APPLICATION_COMMANDS_ATTNAME = MApplicationPackage.eINSTANCE
			.getApplication_Commands().getName();

	/**
	 * Attribute defined by MContexts (value is <code>properties</code>).
	 */
	public static final String CONTEXT_PROPERTIES_ATTNAME = MApplicationPackage.eINSTANCE
			.getContext_Properties().getName();

	/**
	 * Attribute defined by MContributions (value is <code>persistedState</code>).
	 */
	public static final String CONTRIBUTION_PERSISTEDSTATE_ATTNAME = MApplicationPackage.eINSTANCE
			.getContribution_PersistedState().getName();

	/**
	 * Attribute defined by MContributions (value is <code>uri</code>).
	 */
	public static final String CONTRIBUTION_URI_ATTNAME = MApplicationPackage.eINSTANCE
			.getContribution_URI().getName();

	/**
	 * Attribute defined by MHandlerContainers (value is <code>handlers</code>).
	 */
	public static final String HANDLERCONTAINER_HANDLERS_ATTNAME = MApplicationPackage.eINSTANCE
			.getHandlerContainer_Handlers().getName();

	/**
	 * Attribute defined by MUILabels (value is <code>label</code>).
	 */
	public static final String UILABEL_LABEL_ATTNAME = MApplicationPackage.eINSTANCE
			.getUILabel_Label().getName();

	/**
	 * Attribute defined by MUILabels (value is <code>tooltip</code>).
	 */
	public static final String UILABEL_TOOLTIP_ATTNAME = MApplicationPackage.eINSTANCE
			.getUILabel_Tooltip().getName();

	/**
	 * Attribute defined by MUILabels (value is <code>iconURI</code>).
	 */
	public static final String UILABEL_ICONURI_ATTNAME = MApplicationPackage.eINSTANCE
			.getUILabel_IconURI().getName();

	/**
	 * Attribute defined by MUIElements (value is <code>toBeRendered</code>).
	 */
	public static final String UIELEMENT_TOBERENDERED_ATTNAME = MApplicationPackage.eINSTANCE
			.getUIElement_ToBeRendered().getName();

	/**
	 * Attribute defined by MUIElements (value is <code>visible</code>).
	 */
	public static final String UIELEMENT_VISIBLE_ATTNAME = MApplicationPackage.eINSTANCE
			.getUIElement_Visible().getName();

	/**
	 * Attribute defined by MUIElements (value is <code>parent</code>).
	 */
	public static final String UIELEMENT_PARENT_ATTNAME = MApplicationPackage.eINSTANCE
			.getUIElement_Parent().getName();

	/**
	 * Attribute defined by MUIElements (value is <code>parent</code>).
	 */
	public static final String UIELEMENT_CONTAINERDATA_ATTNAME = MApplicationPackage.eINSTANCE
			.getUIElement_ContainerData().getName();

	/**
	 * Attribute defined by MElementContainers (value is <code>children</code>).
	 */
	public static final String ELEMENTCONTAINER_CHILDREN_ATTNAME = MApplicationPackage.eINSTANCE
			.getElementContainer_Children().getName();

	/**
	 * Attribute defined by MElementContainers (value is <code>selectedElement</code>).
	 */
	public static final String ELEMENTCONTAINER_SELECTEDELEMENT_ATTNAME = MApplicationPackage.eINSTANCE
			.getElementContainer_SelectedElement().getName();

	/**
	 * Attribute defined by MWindows (value is <code>x</code>).
	 */
	public static final String WINDOW_X_ATTNAME = MApplicationPackage.eINSTANCE.getWindow_X()
			.getName();

	/**
	 * Attribute defined by MWindows (value is <code>y</code>).
	 */
	public static final String WINDOW_Y_ATTNAME = MApplicationPackage.eINSTANCE.getWindow_Y()
			.getName();

	/**
	 * Attribute defined by MWindows (value is <code>width</code>).
	 */
	public static final String WINDOW_WIDTH_ATTNAME = MApplicationPackage.eINSTANCE
			.getWindow_Width().getName();

	/**
	 * Attribute defined by MWindows (value is <code>height</code>).
	 */
	public static final String WINDOW_HEIGHT_ATTNAME = MApplicationPackage.eINSTANCE
			.getWindow_Height().getName();

	/**
	 * Attribute defined by MWindows (value is <code>mainMenu</code>).
	 */
	public static final String WINDOW_MAINMENU_ATTNAME = MApplicationPackage.eINSTANCE
			.getWindow_MainMenu().getName();

	/**
	 * Attribute defined by MCommands (value is <code>commandName</code>).
	 */
	public static final String COMMAND_COMMANDNAME_ATTNAME = MApplicationPackage.eINSTANCE
			.getCommand_CommandName().getName();

	/**
	 * Attribute defined by MCommands (value is <code>description</code>).
	 */
	public static final String COMMAND_DESCRIPTION_ATTNAME = MApplicationPackage.eINSTANCE
			.getCommand_Description().getName();

	/**
	 * Attribute defined by MCommands (value is <code>parameters</code>).
	 */
	public static final String COMMAND_PARAMETERS_ATTNAME = MApplicationPackage.eINSTANCE
			.getCommand_Parameters().getName();

	/**
	 * Attribute defined by MKeySequences (value is <code>keySequence</code>).
	 */
	public static final String KEYSEQUENCE_KEYSEQUENCE_ATTNAME = MApplicationPackage.eINSTANCE
			.getKeySequence_KeySequence().getName();

	/**
	 * Attribute defined by MParts (value is <code>closeable</code>).
	 */
	public static final String PART_CLOSEABLE_ATTNAME = MApplicationPackage.eINSTANCE
			.getPart_Closeable().getName();

	/**
	 * Attribute defined by MParts (value is <code>menus</code>).
	 */
	public static final String PART_MENUS_ATTNAME = MApplicationPackage.eINSTANCE.getPart_Menus()
			.getName();

	/**
	 * Attribute defined by MParts (value is <code>toolbar</code>).
	 */
	public static final String PART_TOOLBAR_ATTNAME = MApplicationPackage.eINSTANCE
			.getPart_Toolbar().getName();

	/**
	 * Attribute defined by MInputs (value is <code>inputURI</code>).
	 */
	public static final String INPUT_INPUTURI_ATTNAME = MApplicationPackage.eINSTANCE
			.getInput_InputURI().getName();

	/**
	 * Attribute defined by MGenericTiles (value is <code>horizontal</code>).
	 */
	public static final String GENERICTILE_HORIZONTAL_ATTNAME = MApplicationPackage.eINSTANCE
			.getGenericTile_Horizontal().getName();

	/**
	 * Attribute defined by MTrimContainers (value is <code>side</code>).
	 */
	public static final String TRIMCONTAINER_SIDE_ATTNAME = MApplicationPackage.eINSTANCE
			.getTrimContainer_Side().getName();

	/**
	 * Attribute defined by MBindingContainers (value is <code>rootContext</code>).
	 */
	public static final String BINDINGCONTAINER_ROOTCONTEXT_ATTNAME = MApplicationPackage.eINSTANCE
			.getBindingContainer_RootContext().getName();

	/**
	 * Attribute defined by MBindingContainers (value is <code>bindingTables</code>).
	 */
	public static final String BINDINGCONTAINER_BINDINGTABLES_ATTNAME = MApplicationPackage.eINSTANCE
			.getBindingContainer_BindingTables().getName();

	/**
	 * Attribute defined by MBindingTables (value is <code>bindings</code>).
	 */
	public static final String BINDINGTABLES_BINDINGS_ATTNAME = MApplicationPackage.eINSTANCE
			.getBindingTable_Bindings().getName();

	/**
	 * Attribute defined by MBindingTables (value is <code>bindingContextId</code>).
	 */
	public static final String BINDINGTABLES_BINDINGCONTEXTID_ATTNAME = MApplicationPackage.eINSTANCE
			.getBindingTable_BindingContextId().getName();

	/**
	 * Attribute defined by MHandlers (value is <code>command</code>).
	 */
	public static final String HANDLER_COMMAND_ATTNAME = MApplicationPackage.eINSTANCE
			.getHandler_Command().getName();

	/**
	 * Attribute defined by MHandledItems (value is <code>command</code>).
	 */
	public static final String HANDLEDITEM_COMMAND_ATTNAME = MApplicationPackage.eINSTANCE
			.getHandledItem_Command().getName();

	/**
	 * Attribute defined by MKeyBindings (value is <code>command</code>).
	 */
	public static final String KEYBINDING_COMMAND_ATTNAME = MApplicationPackage.eINSTANCE
			.getKeyBinding_Command().getName();

	/**
	 * Attribute defined by MItems (value is <code>enabled</code>).
	 */
	public static final String ITEM_ENABLED_ATTNAME = MApplicationPackage.eINSTANCE
			.getItem_Enabled().getName();

	/**
	 * Attribute defined by MItems (value is <code>selected</code>).
	 */
	public static final String ITEM_SELECTED_ATTNAME = MApplicationPackage.eINSTANCE
			.getItem_Selected().getName();

	/**
	 * Attribute defined by MItems (value is <code>type</code>).
	 */
	public static final String ITEM_TYPE_ATTNAME = MApplicationPackage.eINSTANCE.getItem_Type()
			.getName();

	/**
	 * Attribute defined by MPartDescriptor (value is <code>allowMultiple</code>).
	 */
	public static final String PARTDESCRIPTOR_ALLOWMULTIPLE_ATTNAME = MApplicationPackage.eINSTANCE
			.getPartDescriptor_AllowMultiple().getName();

	/**
	 * Attribute defined by MPartDescriptor (value is <code>category</code>).
	 */
	public static final String PARTDESCRIPTOR_CATEGORY_ATTNAME = MApplicationPackage.eINSTANCE
			.getPartDescriptor_Category().getName();

	/**
	 * Attribute defined by MPartDescriptorContainers (value is <code>descriptors</code>).
	 */
	public static final String PARTDESCRIPTORCONTAINER_DESCRIPTORS_ATTNAME = MApplicationPackage.eINSTANCE
			.getPartDescriptorContainer_Descriptors().getName();

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
