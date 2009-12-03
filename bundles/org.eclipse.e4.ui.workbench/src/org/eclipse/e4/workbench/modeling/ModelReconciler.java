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
import org.eclipse.e4.ui.model.application.MContribution;

public abstract class ModelReconciler {

	/**
	 * Attribute defined by MApplications (value is <code>commands</code>).
	 */
	public static final String COMMANDS_ATTNAME = "commands"; //$NON-NLS-1$

	/**
	 * Attribute defined by MContributions (value is <code>persistedState</code>).
	 */
	public static final String PERSISTEDSTATE_ATTNAME = "persistedState"; //$NON-NLS-1$

	/**
	 * Attribute defined by MHandlerContainers (value is <code>handlers</code>).
	 */
	public static final String HANDLERS_ATTNAME = "handlers"; //$NON-NLS-1$

	/**
	 * Attribute defined by MUIItems (value is <code>name</code>).
	 */
	public static final String NAME_ATTNAME = "name"; //$NON-NLS-1$

	/**
	 * Attribute defined by MUIItems (value is <code>tooltip</code>).
	 */
	public static final String TOOLTIP_ATTNAME = "tooltip"; //$NON-NLS-1$

	/**
	 * Attribute defined by MUIItems (value is <code>iconURI</code>).
	 */
	public static final String ICONURI_ATTNAME = "iconURI"; //$NON-NLS-1$

	/**
	 * Attribute defined by MUIElements (value is <code>visible</code>).
	 */
	public static final String VISIBLE_ATTNAME = "visible"; //$NON-NLS-1$

	/**
	 * Attribute defined by MUIElements (value is <code>parent</code>).
	 */
	public static final String PARENT_ATTNAME = "parent"; //$NON-NLS-1$

	/**
	 * Attribute defined by MElementContainers (value is <code>children</code>).
	 */
	public static final String CHILDREN_ATTNAME = "children"; //$NON-NLS-1$

	/**
	 * Attribute defined by MElementContainers (value is <code>activeChild</code>).
	 */
	public static final String ACTIVECHILD_ATTNAME = "activeChild"; //$NON-NLS-1$

	/**
	 * Attribute defined by MWindows (value is <code>x</code>).
	 */
	public static final String X_ATTNAME = "x"; //$NON-NLS-1$

	/**
	 * Attribute defined by MWindows (value is <code>y</code>).
	 */
	public static final String Y_ATTNAME = "y"; //$NON-NLS-1$

	/**
	 * Attribute defined by MWindows (value is <code>width</code>).
	 */
	public static final String WIDTH_ATTNAME = "width"; //$NON-NLS-1$

	/**
	 * Attribute defined by MWindows (value is <code>height</code>).
	 */
	public static final String HEIGHT_ATTNAME = "height"; //$NON-NLS-1$

	/**
	 * Attribute defined by MWindows (value is <code>mainMenu</code>).
	 */
	public static final String MAINMENU_ATTNAME = "mainMenu"; //$NON-NLS-1$

	/**
	 * Attribute defined by MCommands and MHandledItems (value is <code>commandName</code>).
	 */
	public static final String COMMANDNAME_ATTNAME = "commandName"; //$NON-NLS-1$

	/**
	 * Attribute defined by MCommands (value is <code>description</code>).
	 */
	public static final String DESCRIPTION_ATTNAME = "description"; //$NON-NLS-1$

	/**
	 * Attribute defined by MKeySequences (value is <code>keySequence</code>).
	 */
	public static final String KEYSEQUENCE_ATTNAME = "keySequence"; //$NON-NLS-1$

	/**
	 * Attribute defined by MParts (value is <code>menus</code>).
	 */
	public static final String MENUS_ATTNAME = "menus"; //$NON-NLS-1$

	/**
	 * Attribute defined by MParts (value is <code>toolbar</code>).
	 */
	public static final String TOOLBAR_ATTNAME = "toolbar"; //$NON-NLS-1$

	/**
	 * Attribute defined by MGenericTiles and MTrimContainers (value is <code>horizontal</code>).
	 */
	public static final String HORIZONTAL_ATTNAME = "horizontal"; //$NON-NLS-1$

	/**
	 * Attribute defined by MGenericTiles (value is <code>weights</code>).
	 */
	public static final String WEIGHTS_ATTNAME = "weights"; //$NON-NLS-1$

	/**
	 * Attribute defined by MTrimContainers (value is <code>side</code>).
	 */
	public static final String SIDE_ATTNAME = "side"; //$NON-NLS-1$

	/**
	 * Attribute defined by MBindingContainers (value is <code>bindings</code>).
	 */
	public static final String BINDINGS_ATTNAME = "bindings"; //$NON-NLS-1$

	/**
	 * Attribute defined by MHandlers and MKeyBindings (value is <code>command</code>).
	 */
	public static final String COMMAND_ATTNAME = "command"; //$NON-NLS-1$

	/**
	 * Attribute defined by MCommands (value is <code>parameters</code>).
	 */
	public static final String PARAMETERS_ATTNAME = "parameters"; //$NON-NLS-1$

	/**
	 * Attribute defined by MItems (value is <code>enabled</code>).
	 */
	public static final String ENABLED_ATTNAME = "enabled"; //$NON-NLS-1$

	/**
	 * Attribute defined by MItems (value is <code>selected</code>).
	 */
	public static final String SELECTED_ATTNAME = "selected"; //$NON-NLS-1$

	/**
	 * Attribute defined by MItems (value is <code>separator</code>).
	 */
	public static final String SEPARATOR_ATTNAME = "separator"; //$NON-NLS-1$

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
