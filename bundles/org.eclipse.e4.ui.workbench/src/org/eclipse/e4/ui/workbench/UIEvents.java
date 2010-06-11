/*******************************************************************************
 * Copyright (c) 2009,2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.workbench;

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

	public static interface BindingContext {
		public static final String TOPIC = UITopicBase + "/commands/BindingContext"; //$NON-NLS-1$
		public static final String CHILDREN = "children"; //$NON-NLS-1$
		public static final String DESCRIPTION = "description"; //$NON-NLS-1$
		public static final String NAME = "name"; //$NON-NLS-1$
	}

	public static interface BindingTable {
		public static final String TOPIC = UITopicBase + "/commands/BindingTable"; //$NON-NLS-1$
		public static final String BINDINGCONTEXTID = "bindingContextId"; //$NON-NLS-1$
		public static final String BINDINGS = "bindings"; //$NON-NLS-1$
	}

	public static interface BindingTableContainer {
		public static final String TOPIC = UITopicBase + "/commands/BindingTableContainer"; //$NON-NLS-1$
		public static final String BINDINGTABLES = "bindingTables"; //$NON-NLS-1$
		public static final String ROOTCONTEXT = "rootContext"; //$NON-NLS-1$
	}

	public static interface Bindings {
		public static final String TOPIC = UITopicBase + "/commands/Bindings"; //$NON-NLS-1$
		public static final String BINDINGCONTEXTS = "bindingContexts"; //$NON-NLS-1$
	}

	public static interface Command {
		public static final String TOPIC = UITopicBase + "/commands/Command"; //$NON-NLS-1$
		public static final String COMMANDNAME = "commandName"; //$NON-NLS-1$
		public static final String DESCRIPTION = "description"; //$NON-NLS-1$
		public static final String PARAMETERS = "parameters"; //$NON-NLS-1$
	}

	public static interface CommandParameter {
		public static final String TOPIC = UITopicBase + "/commands/CommandParameter"; //$NON-NLS-1$
		public static final String NAME = "name"; //$NON-NLS-1$
		public static final String OPTIONAL = "optional"; //$NON-NLS-1$
		public static final String TYPEID = "typeId"; //$NON-NLS-1$
	}

	public static interface Handler {
		public static final String TOPIC = UITopicBase + "/commands/Handler"; //$NON-NLS-1$
		public static final String COMMAND = "command"; //$NON-NLS-1$
	}

	public static interface HandlerContainer {
		public static final String TOPIC = UITopicBase + "/commands/HandlerContainer"; //$NON-NLS-1$
		public static final String HANDLERS = "handlers"; //$NON-NLS-1$
	}

	public static interface KeyBinding {
		public static final String TOPIC = UITopicBase + "/commands/KeyBinding"; //$NON-NLS-1$
		public static final String COMMAND = "command"; //$NON-NLS-1$
		public static final String PARAMETERS = "parameters"; //$NON-NLS-1$
	}

	public static interface KeySequence {
		public static final String TOPIC = UITopicBase + "/commands/KeySequence"; //$NON-NLS-1$
		public static final String KEYSEQUENCE = "keySequence"; //$NON-NLS-1$
	}

	public static interface Parameter {
		public static final String TOPIC = UITopicBase + "/commands/Parameter"; //$NON-NLS-1$
		public static final String NAME = "name"; //$NON-NLS-1$
		public static final String VALUE = "value"; //$NON-NLS-1$
	}

	public static interface PartDescriptor {
		public static final String TOPIC = UITopicBase + "/basic/PartDescriptor"; //$NON-NLS-1$
		public static final String ALLOWMULTIPLE = "allowMultiple"; //$NON-NLS-1$
		public static final String CATEGORY = "category"; //$NON-NLS-1$
		public static final String CLOSEABLE = "closeable"; //$NON-NLS-1$
		public static final String CONTRIBUTIONURI = "contributionURI"; //$NON-NLS-1$
		public static final String DIRTYABLE = "dirtyable"; //$NON-NLS-1$
		public static final String MENUS = "menus"; //$NON-NLS-1$
		public static final String TOOLBAR = "toolbar"; //$NON-NLS-1$
	}

	public static interface PartDescriptorContainer {
		public static final String TOPIC = UITopicBase + "/basic/PartDescriptorContainer"; //$NON-NLS-1$
		public static final String DESCRIPTORS = "descriptors"; //$NON-NLS-1$
	}

	public static interface Application {
		public static final String TOPIC = UITopicBase + "/application/Application"; //$NON-NLS-1$
		public static final String ADDONS = "addons"; //$NON-NLS-1$
		public static final String COMMANDS = "commands"; //$NON-NLS-1$
	}

	public static interface ApplicationElement {
		public static final String TOPIC = UITopicBase + "/application/ApplicationElement"; //$NON-NLS-1$
		public static final String ELEMENTID = "elementId"; //$NON-NLS-1$
		public static final String TAGS = "tags"; //$NON-NLS-1$
	}

	public static interface Contribution {
		public static final String TOPIC = UITopicBase + "/application/Contribution"; //$NON-NLS-1$
		public static final String CONTRIBUTIONURI = "contributionURI"; //$NON-NLS-1$
		public static final String OBJECT = "object"; //$NON-NLS-1$
		public static final String PERSISTEDSTATE = "persistedState"; //$NON-NLS-1$
	}

	public static interface ModelComponent {
		public static final String TOPIC = UITopicBase + "/application/ModelComponent"; //$NON-NLS-1$
		public static final String BINDINGS = "bindings"; //$NON-NLS-1$
		public static final String CHILDREN = "children"; //$NON-NLS-1$
		public static final String COMMANDS = "commands"; //$NON-NLS-1$
		public static final String PARENTID = "parentID"; //$NON-NLS-1$
		public static final String POSITIONINPARENT = "positionInParent"; //$NON-NLS-1$
		public static final String PROCESSOR = "processor"; //$NON-NLS-1$
	}

	public static interface ModelComponents {
		public static final String TOPIC = UITopicBase + "/application/ModelComponents"; //$NON-NLS-1$
		public static final String COMPONENTS = "components"; //$NON-NLS-1$
		public static final String IMPORTS = "imports"; //$NON-NLS-1$
	}

	public static interface StringToStringMap {
		public static final String TOPIC = UITopicBase + "/application/StringToStringMap"; //$NON-NLS-1$
		public static final String KEY = "key"; //$NON-NLS-1$
		public static final String VALUE = "value"; //$NON-NLS-1$
	}

	public static interface Perspective {
		public static final String TOPIC = UITopicBase + "/advanced/Perspective"; //$NON-NLS-1$
		public static final String WINDOWS = "windows"; //$NON-NLS-1$
	}

	public static interface Placeholder {
		public static final String TOPIC = UITopicBase + "/advanced/Placeholder"; //$NON-NLS-1$
		public static final String REF = "ref"; //$NON-NLS-1$
	}

	public static interface Part {
		public static final String TOPIC = UITopicBase + "/basic/Part"; //$NON-NLS-1$
		public static final String CLOSEABLE = "closeable"; //$NON-NLS-1$
		public static final String MENUS = "menus"; //$NON-NLS-1$
		public static final String TOOLBAR = "toolbar"; //$NON-NLS-1$
	}

	public static interface TrimmedWindow {
		public static final String TOPIC = UITopicBase + "/basic/TrimmedWindow"; //$NON-NLS-1$
		public static final String TRIMBARS = "trimBars"; //$NON-NLS-1$
	}

	public static interface Window {
		public static final String TOPIC = UITopicBase + "/basic/Window"; //$NON-NLS-1$
		public static final String HEIGHT = "height"; //$NON-NLS-1$
		public static final String MAINMENU = "mainMenu"; //$NON-NLS-1$
		public static final String SHAREDELEMENTS = "sharedElements"; //$NON-NLS-1$
		public static final String WIDTH = "width"; //$NON-NLS-1$
		public static final String WINDOWS = "windows"; //$NON-NLS-1$
		public static final String X = "x"; //$NON-NLS-1$
		public static final String Y = "y"; //$NON-NLS-1$
	}

	public static interface Context {
		public static final String TOPIC = UITopicBase + "/ui/Context"; //$NON-NLS-1$
		public static final String CONTEXT = "context"; //$NON-NLS-1$
		public static final String PROPERTIES = "properties"; //$NON-NLS-1$
		public static final String VARIABLES = "variables"; //$NON-NLS-1$
	}

	public static interface CoreExpression {
		public static final String TOPIC = UITopicBase + "/ui/CoreExpression"; //$NON-NLS-1$
		public static final String COREEXPRESSION = "coreExpression"; //$NON-NLS-1$
		public static final String COREEXPRESSIONID = "coreExpressionId"; //$NON-NLS-1$
	}

	public static interface Dirtyable {
		public static final String TOPIC = UITopicBase + "/ui/Dirtyable"; //$NON-NLS-1$
		public static final String DIRTY = "dirty"; //$NON-NLS-1$
	}

	public static interface ElementContainer {
		public static final String TOPIC = UITopicBase + "/ui/ElementContainer"; //$NON-NLS-1$
		public static final String CHILDREN = "children"; //$NON-NLS-1$
		public static final String SELECTEDELEMENT = "selectedElement"; //$NON-NLS-1$
	}

	public static interface GenericTile {
		public static final String TOPIC = UITopicBase + "/ui/GenericTile"; //$NON-NLS-1$
		public static final String HORIZONTAL = "horizontal"; //$NON-NLS-1$
	}

	public static interface GenericTrimContainer {
		public static final String TOPIC = UITopicBase + "/ui/GenericTrimContainer"; //$NON-NLS-1$
		public static final String SIDE = "side"; //$NON-NLS-1$
	}

	public static interface Input {
		public static final String TOPIC = UITopicBase + "/ui/Input"; //$NON-NLS-1$
		public static final String INPUTURI = "inputURI"; //$NON-NLS-1$
	}

	public static interface UIElement {
		public static final String TOPIC = UITopicBase + "/ui/UIElement"; //$NON-NLS-1$
		public static final String CONTAINERDATA = "containerData"; //$NON-NLS-1$
		public static final String CURSHAREDREF = "curSharedRef"; //$NON-NLS-1$
		public static final String ONTOP = "onTop"; //$NON-NLS-1$
		public static final String PARENT = "parent"; //$NON-NLS-1$
		public static final String RENDERER = "renderer"; //$NON-NLS-1$
		public static final String TOBERENDERED = "toBeRendered"; //$NON-NLS-1$
		public static final String VISIBLE = "visible"; //$NON-NLS-1$
		public static final String VISIBLEWHEN = "visibleWhen"; //$NON-NLS-1$
		public static final String WIDGET = "widget"; //$NON-NLS-1$
	}

	public static interface UILabel {
		public static final String TOPIC = UITopicBase + "/ui/UILabel"; //$NON-NLS-1$
		public static final String ICONURI = "iconURI"; //$NON-NLS-1$
		public static final String LABEL = "label"; //$NON-NLS-1$
		public static final String TOOLTIP = "tooltip"; //$NON-NLS-1$
	}

	public static interface HandledItem {
		public static final String TOPIC = UITopicBase + "/menu/HandledItem"; //$NON-NLS-1$
		public static final String COMMAND = "command"; //$NON-NLS-1$
		public static final String PARAMETERS = "parameters"; //$NON-NLS-1$
		public static final String WBCOMMAND = "wbCommand"; //$NON-NLS-1$
	}

	public static interface Item {
		public static final String TOPIC = UITopicBase + "/menu/Item"; //$NON-NLS-1$
		public static final String ENABLED = "enabled"; //$NON-NLS-1$
		public static final String SELECTED = "selected"; //$NON-NLS-1$
		public static final String TYPE = "type"; //$NON-NLS-1$
	}

	public static interface Menu {
		public static final String TOPIC = UITopicBase + "/menu/Menu"; //$NON-NLS-1$
		public static final String ENABLED = "enabled"; //$NON-NLS-1$
	}

	public static interface MenuContribution {
		public static final String TOPIC = UITopicBase + "/menu/MenuContribution"; //$NON-NLS-1$
		public static final String PARENTID = "parentID"; //$NON-NLS-1$
		public static final String POSITIONINPARENT = "positionInParent"; //$NON-NLS-1$
	}

	public static interface MenuContributions {
		public static final String TOPIC = UITopicBase + "/menu/MenuContributions"; //$NON-NLS-1$
		public static final String MENUCONTRIBUTIONS = "menuContributions"; //$NON-NLS-1$
	}

	public static interface MenuItem {
		public static final String TOPIC = UITopicBase + "/menu/MenuItem"; //$NON-NLS-1$
		public static final String MNEMONICS = "mnemonics"; //$NON-NLS-1$
	}

	public static interface RenderedMenu {
		public static final String TOPIC = UITopicBase + "/menu/RenderedMenu"; //$NON-NLS-1$
		public static final String CONTRIBUTIONMANAGER = "contributionManager"; //$NON-NLS-1$
	}

	public static interface RenderedToolBar {
		public static final String TOPIC = UITopicBase + "/menu/RenderedToolBar"; //$NON-NLS-1$
		public static final String CONTRIBUTIONMANAGER = "contributionManager"; //$NON-NLS-1$
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
