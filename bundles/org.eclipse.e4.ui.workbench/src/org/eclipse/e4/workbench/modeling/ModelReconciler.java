/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MContribution;

public abstract class ModelReconciler {

	/**
	 * Attribute defined by MApplicationElements (value is <code>id</code>).
	 */
	public static final String APPLICATIONELEMENT_ID_ATTNAME = MApplicationPackage.eINSTANCE
			.getApplicationElement_Id().getName();

	/**
	 * Attribute defined by MApplications (value is <code>commands</code>).
	 */
	public static final String APPLICATION_COMMANDS_ATTNAME = MApplicationPackage.eINSTANCE
			.getApplication_Commands().getName();

	/**
	 * Attribute defined by MContributions (value is <code>persistedState</code>).
	 */
	public static final String CONTRIBUTION_PERSISTEDSTATE_ATTNAME = MApplicationPackage.eINSTANCE
			.getContribution_PersistedState().getName();

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
	 * Attribute defined by MElementContainers (value is <code>children</code>).
	 */
	public static final String ELEMENTCONTAINER_CHILDREN_ATTNAME = MApplicationPackage.eINSTANCE
			.getElementContainer_Children().getName();

	/**
	 * Attribute defined by MElementContainers (value is <code>activeChild</code>).
	 */
	public static final String ELEMENTCONTAINER_ACTIVECHILD_ATTNAME = MApplicationPackage.eINSTANCE
			.getElementContainer_ActiveChild().getName();

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
	 * Attribute defined by MGenericTiles (value is <code>horizontal</code>).
	 * 
	 * @see #TRIMCONTAINER_HORIZONTAL_ATTNAME
	 */
	public static final String GENERICTILE_HORIZONTAL_ATTNAME = MApplicationPackage.eINSTANCE
			.getGenericTile_Horizontal().getName();

	/**
	 * Attribute defined by MGenericTiles (value is <code>weights</code>).
	 */
	public static final String GENERICTILE_WEIGHTS_ATTNAME = MApplicationPackage.eINSTANCE
			.getGenericTile_Weights().getName();

	/**
	 * Attribute defined by MTrimContainers (value is <code>horizontal</code>).
	 * 
	 * @see #GENERICTILE_HORIZONTAL_ATTNAME
	 */
	public static final String TRIMCONTAINER_HORIZONTAL_ATTNAME = MApplicationPackage.eINSTANCE
			.getTrimContainer_Horizontal().getName();

	/**
	 * Attribute defined by MTrimContainers (value is <code>side</code>).
	 */
	public static final String TRIMCONTAINER_SIDE_ATTNAME = MApplicationPackage.eINSTANCE
			.getTrimContainer_Side().getName();

	/**
	 * Attribute defined by MBindingContainers (value is <code>bindings</code>).
	 */
	public static final String BINDINGCONTAINER_BINDINGS_ATTNAME = MApplicationPackage.eINSTANCE
			.getBindingContainer_Bindings().getName();

	/**
	 * Attribute defined by MHandlers (value is <code>command</code>).
	 */
	public static final String HANDLER_COMMAND_ATTNAME = MApplicationPackage.eINSTANCE
			.getHandler_Command().getName();

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
	 * Attribute defined by MItems (value is <code>separator</code>).
	 */
	public static final String ITEM_SEPARATOR_ATTNAME = MApplicationPackage.eINSTANCE
			.getItem_Separator().getName();

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

	protected String getModelId(Object object) {
		String id = null;

		if (object instanceof MApplicationElement) {
			id = ((MApplicationElement) object).getId();
			if (id != null) {
				return id;
			}
		}

		if (object instanceof MContribution) {
			id = ((MContribution) object).getURI();
			if (id != null) {
				return id;
			}
		}

		return null;
	}
}
