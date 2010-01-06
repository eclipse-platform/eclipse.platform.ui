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
package org.eclipse.e4.workbench.ui;

public class UIEvents {
	public static final String TOPIC_SEP = "/"; //$NON-NLS-1$
	public static final String ALL_SUB_TOPICS = "*"; //$NON-NLS-1$
	public static final String UITopicBase = "org/eclipse/e4/ui/model"; //$NON-NLS-1$

	public static interface EventTypes {
		public static final String CREATE = "CREATE"; //$NON-NLS-1$
		public static final String SET = "SET"; //$NON-NLS-1$
		public static final String ADD = "ADD"; //$NON-NLS-1$
		public static final String REMOVE = "REMOVE"; //$NON-NLS-1$
	}

	public static interface EventTags {
		public static final String ELEMENT = "ChangedElement"; //$NON-NLS-1$
		public static final String WIDGET = "Widget"; //$NON-NLS-1$
		public static final String TYPE = "EventType"; //$NON-NLS-1$
		public static final String ATTNAME = "AttName"; //$NON-NLS-1$
		public static final String OLD_VALUE = "OldValue"; //$NON-NLS-1$
		public static final String NEW_VALUE = "NewValue"; //$NON-NLS-1$
	}

	public static interface Application {
		public static final String TOPIC = UITopicBase + "/application/Application"; //$NON-NLS-1$
		public static final String COMMANDS = "commands"; //$NON-NLS-1$
	}

	public static interface ApplicationElement {
		public static final String TOPIC = UITopicBase + "/application/ApplicationElement"; //$NON-NLS-1$
		public static final String ID = "id"; //$NON-NLS-1$
		public static final String STYLE = "style"; //$NON-NLS-1$
	}

	public static interface BindingContainer {
		public static final String TOPIC = UITopicBase + "/application/BindingContainer"; //$NON-NLS-1$
		public static final String BINDINGS = "bindings"; //$NON-NLS-1$
	}

	public static interface Command {
		public static final String TOPIC = UITopicBase + "/application/Command"; //$NON-NLS-1$
		public static final String COMMANDNAME = "commandName"; //$NON-NLS-1$
		public static final String DESCRIPTION = "description"; //$NON-NLS-1$
		public static final String PARAMETERS = "parameters"; //$NON-NLS-1$
	}

	public static interface CommandParameter {
		public static final String TOPIC = UITopicBase + "/application/CommandParameter"; //$NON-NLS-1$
		public static final String NAME = "name"; //$NON-NLS-1$
		public static final String OPTIONAL = "optional"; //$NON-NLS-1$
		public static final String TYPEID = "typeId"; //$NON-NLS-1$
	}

	public static interface Context {
		public static final String TOPIC = UITopicBase + "/application/Context"; //$NON-NLS-1$
		public static final String CONTEXT = "context"; //$NON-NLS-1$
		public static final String VARIABLES = "variables"; //$NON-NLS-1$
	}

	public static interface Contribution {
		public static final String TOPIC = UITopicBase + "/application/Contribution"; //$NON-NLS-1$
		public static final String URI = "URI"; //$NON-NLS-1$
		public static final String OBJECT = "object"; //$NON-NLS-1$
		public static final String PERSISTEDSTATE = "persistedState"; //$NON-NLS-1$
	}

	public static interface Dirtyable {
		public static final String TOPIC = UITopicBase + "/application/Dirtyable"; //$NON-NLS-1$
		public static final String DIRTY = "dirty"; //$NON-NLS-1$
	}

	public static interface ElementContainer {
		public static final String TOPIC = UITopicBase + "/application/ElementContainer"; //$NON-NLS-1$
		public static final String ACTIVECHILD = "activeChild"; //$NON-NLS-1$
		public static final String CHILDREN = "children"; //$NON-NLS-1$
	}

	public static interface GenericTile {
		public static final String TOPIC = UITopicBase + "/application/GenericTile"; //$NON-NLS-1$
		public static final String HORIZONTAL = "horizontal"; //$NON-NLS-1$
	}

	public static interface HandledItem {
		public static final String TOPIC = UITopicBase + "/application/HandledItem"; //$NON-NLS-1$
		public static final String COMMAND = "command"; //$NON-NLS-1$
		public static final String PARAMETERS = "parameters"; //$NON-NLS-1$
		public static final String WBCOMMAND = "wbCommand"; //$NON-NLS-1$
	}

	public static interface Handler {
		public static final String TOPIC = UITopicBase + "/application/Handler"; //$NON-NLS-1$
		public static final String COMMAND = "command"; //$NON-NLS-1$
	}

	public static interface HandlerContainer {
		public static final String TOPIC = UITopicBase + "/application/HandlerContainer"; //$NON-NLS-1$
		public static final String HANDLERS = "handlers"; //$NON-NLS-1$
	}

	public static interface Input {
		public static final String TOPIC = UITopicBase + "/application/Input"; //$NON-NLS-1$
		public static final String INPUTURI = "inputURI"; //$NON-NLS-1$
	}

	public static interface Item {
		public static final String TOPIC = UITopicBase + "/application/Item"; //$NON-NLS-1$
		public static final String ENABLED = "enabled"; //$NON-NLS-1$
		public static final String SELECTED = "selected"; //$NON-NLS-1$
		public static final String TYPE = "type"; //$NON-NLS-1$
	}

	public static interface KeyBinding {
		public static final String TOPIC = UITopicBase + "/application/KeyBinding"; //$NON-NLS-1$
		public static final String COMMAND = "command"; //$NON-NLS-1$
		public static final String PARAMETERS = "parameters"; //$NON-NLS-1$
	}

	public static interface KeySequence {
		public static final String TOPIC = UITopicBase + "/application/KeySequence"; //$NON-NLS-1$
		public static final String KEYSEQUENCE = "keySequence"; //$NON-NLS-1$
	}

	public static interface ModelComponent {
		public static final String TOPIC = UITopicBase + "/application/ModelComponent"; //$NON-NLS-1$
		public static final String CHILDREN = "children"; //$NON-NLS-1$
		public static final String COMMANDS = "commands"; //$NON-NLS-1$
		public static final String HANDLERS = "handlers"; //$NON-NLS-1$
		public static final String PARENTID = "parentID"; //$NON-NLS-1$
		public static final String POSITIONINPARENT = "positionInParent"; //$NON-NLS-1$
	}

	public static interface ModelComponents {
		public static final String TOPIC = UITopicBase + "/application/ModelComponents"; //$NON-NLS-1$
		public static final String COMPONENTS = "components"; //$NON-NLS-1$
	}

	public static interface Parameter {
		public static final String TOPIC = UITopicBase + "/application/Parameter"; //$NON-NLS-1$
		public static final String TAG = "tag"; //$NON-NLS-1$
		public static final String VALUE = "value"; //$NON-NLS-1$
	}

	public static interface Part {
		public static final String TOPIC = UITopicBase + "/application/Part"; //$NON-NLS-1$
		public static final String MENUS = "menus"; //$NON-NLS-1$
		public static final String TOOLBAR = "toolbar"; //$NON-NLS-1$
	}

	public static interface Placeholder {
		public static final String TOPIC = UITopicBase + "/application/Placeholder"; //$NON-NLS-1$
		public static final String REF = "ref"; //$NON-NLS-1$
	}

	public static interface TrimContainer {
		public static final String TOPIC = UITopicBase + "/application/TrimContainer"; //$NON-NLS-1$
		public static final String SIDE = "side"; //$NON-NLS-1$
	}

	public static interface UIElement {
		public static final String TOPIC = UITopicBase + "/application/UIElement"; //$NON-NLS-1$
		public static final String CONTAINERDATA = "containerData"; //$NON-NLS-1$
		public static final String ONTOP = "onTop"; //$NON-NLS-1$
		public static final String PARENT = "parent"; //$NON-NLS-1$
		public static final String RENDERER = "renderer"; //$NON-NLS-1$
		public static final String TOBERENDERED = "toBeRendered"; //$NON-NLS-1$
		public static final String VISIBLE = "visible"; //$NON-NLS-1$
		public static final String WIDGET = "widget"; //$NON-NLS-1$
	}

	public static interface UILabel {
		public static final String TOPIC = UITopicBase + "/application/UILabel"; //$NON-NLS-1$
		public static final String ICONURI = "iconURI"; //$NON-NLS-1$
		public static final String LABEL = "label"; //$NON-NLS-1$
		public static final String TOOLTIP = "tooltip"; //$NON-NLS-1$
	}

	public static interface Window {
		public static final String TOPIC = UITopicBase + "/application/Window"; //$NON-NLS-1$
		public static final String HEIGHT = "height"; //$NON-NLS-1$
		public static final String MAINMENU = "mainMenu"; //$NON-NLS-1$
		public static final String WIDTH = "width"; //$NON-NLS-1$
		public static final String X = "x"; //$NON-NLS-1$
		public static final String Y = "y"; //$NON-NLS-1$
	}

	public static String buildTopic(String topic) {
		return topic + TOPIC_SEP + ALL_SUB_TOPICS;
	}

	public static String buildTopic(String topic, String attrName) {
		return topic + TOPIC_SEP + attrName + TOPIC_SEP + ALL_SUB_TOPICS;
	}

	public static String buildTopic(String topic, String attrName, String eventType) {
		return topic + TOPIC_SEP + attrName + TOPIC_SEP + eventType;
	}
}
