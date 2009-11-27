package org.eclipse.e4.workbench.ui;

public class UIEvents {
	public static final String TOPIC_SEP = "/"; //$NON-NLS-1$
	public static final String ALL_ATTRIBUTES = "*"; //$NON-NLS-1$
	public static final String UITopicBase = "org/eclipse/e4/ui/model"; //$NON-NLS-1$

	public static interface EventTypes {
		public static final String ALL = "*"; //$NON-NLS-1$
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

	public static interface ApplicationElement {
		public static final String TOPIC = UITopicBase + "/application/ApplicationElement"; //$NON-NLS-1$
		public static final String ID = "id"; //$NON-NLS-1$
		public static final String ID_TOPIC = TOPIC + "/id"; //$NON-NLS-1$
	}

	public static interface Contribution {
		public static final String TOPIC = UITopicBase + "/application/Contribution"; //$NON-NLS-1$
		public static final String URI = "URI"; //$NON-NLS-1$
		public static final String URI_TOPIC = TOPIC + "/URI"; //$NON-NLS-1$
		public static final String OBJECT = "object"; //$NON-NLS-1$
		public static final String OBJECT_TOPIC = TOPIC + "/object"; //$NON-NLS-1$
		public static final String PERSISTEDSTATE = "persistedState"; //$NON-NLS-1$
		public static final String PERSISTEDSTATE_TOPIC = TOPIC + "/persistedState"; //$NON-NLS-1$
	}

	public static interface Command {
		public static final String TOPIC = UITopicBase + "/application/Command"; //$NON-NLS-1$
		public static final String COMMANDNAME = "commandName"; //$NON-NLS-1$
		public static final String COMMANDNAME_TOPIC = TOPIC + "/commandName"; //$NON-NLS-1$
		public static final String DESCRIPTION = "description"; //$NON-NLS-1$
		public static final String DESCRIPTION_TOPIC = TOPIC + "/description"; //$NON-NLS-1$
		public static final String PARAMETERS = "parameters"; //$NON-NLS-1$
		public static final String PARAMETERS_TOPIC = TOPIC + "/parameters"; //$NON-NLS-1$
	}

	public static interface CommandParameter {
		public static final String TOPIC = UITopicBase + "/application/CommandParameter"; //$NON-NLS-1$
		public static final String NAME = "name"; //$NON-NLS-1$
		public static final String NAME_TOPIC = TOPIC + "/name"; //$NON-NLS-1$
		public static final String TYPEID = "typeId"; //$NON-NLS-1$
		public static final String TYPEID_TOPIC = TOPIC + "/typeId"; //$NON-NLS-1$
		public static final String OPTIONAL = "optional"; //$NON-NLS-1$
		public static final String OPTIONAL_TOPIC = TOPIC + "/optional"; //$NON-NLS-1$
	}

	public static interface Dirtyable {
		public static final String TOPIC = UITopicBase + "/application/Dirtyable"; //$NON-NLS-1$
		public static final String DIRTY = "dirty"; //$NON-NLS-1$
		public static final String DIRTY_TOPIC = TOPIC + "/dirty"; //$NON-NLS-1$
	}

	public static interface Handler {
		public static final String TOPIC = UITopicBase + "/application/Handler"; //$NON-NLS-1$
		public static final String COMMAND = "command"; //$NON-NLS-1$
		public static final String COMMAND_TOPIC = TOPIC + "/command"; //$NON-NLS-1$
	}

	public static interface HandlerContainer {
		public static final String TOPIC = UITopicBase + "/application/HandlerContainer"; //$NON-NLS-1$
		public static final String HANDLERS = "handlers"; //$NON-NLS-1$
		public static final String HANDLERS_TOPIC = TOPIC + "/handlers"; //$NON-NLS-1$
	}

	public static interface Input {
		public static final String TOPIC = UITopicBase + "/application/Input"; //$NON-NLS-1$
		public static final String INPUTURI = "inputURI"; //$NON-NLS-1$
		public static final String INPUTURI_TOPIC = TOPIC + "/inputURI"; //$NON-NLS-1$
	}

	public static interface Parameter {
		public static final String TOPIC = UITopicBase + "/application/Parameter"; //$NON-NLS-1$
		public static final String TAG = "tag"; //$NON-NLS-1$
		public static final String TAG_TOPIC = TOPIC + "/tag"; //$NON-NLS-1$
		public static final String VALUE = "value"; //$NON-NLS-1$
		public static final String VALUE_TOPIC = TOPIC + "/value"; //$NON-NLS-1$
	}

	public static interface UIItem {
		public static final String TOPIC = UITopicBase + "/application/UIItem"; //$NON-NLS-1$
		public static final String NAME = "name"; //$NON-NLS-1$
		public static final String NAME_TOPIC = TOPIC + "/name"; //$NON-NLS-1$
		public static final String ICONURI = "iconURI"; //$NON-NLS-1$
		public static final String ICONURI_TOPIC = TOPIC + "/iconURI"; //$NON-NLS-1$
		public static final String TOOLTIP = "tooltip"; //$NON-NLS-1$
		public static final String TOOLTIP_TOPIC = TOPIC + "/tooltip"; //$NON-NLS-1$
	}

	public static interface UIElement {
		public static final String TOPIC = UITopicBase + "/application/UIElement"; //$NON-NLS-1$
		public static final String WIDGET = "widget"; //$NON-NLS-1$
		public static final String WIDGET_TOPIC = TOPIC + "/widget"; //$NON-NLS-1$
		public static final String FACTORY = "factory"; //$NON-NLS-1$
		public static final String FACTORY_TOPIC = TOPIC + "/factory"; //$NON-NLS-1$
		public static final String VISIBLE = "visible"; //$NON-NLS-1$
		public static final String VISIBLE_TOPIC = TOPIC + "/visible"; //$NON-NLS-1$
		public static final String PARENT = "parent"; //$NON-NLS-1$
		public static final String PARENT_TOPIC = TOPIC + "/parent"; //$NON-NLS-1$
	}

	public static interface Context {
		public static final String TOPIC = UITopicBase + "/application/Context"; //$NON-NLS-1$
		public static final String CONTEXT = "context"; //$NON-NLS-1$
		public static final String CONTEXT_TOPIC = TOPIC + "/context"; //$NON-NLS-1$
		public static final String VARIABLES = "variables"; //$NON-NLS-1$
		public static final String VARIABLES_TOPIC = TOPIC + "/variables"; //$NON-NLS-1$
	}

	public static interface KeySequence {
		public static final String TOPIC = UITopicBase + "/application/KeySequence"; //$NON-NLS-1$
		public static final String KEYSEQUENCE = "keySequence"; //$NON-NLS-1$
		public static final String KEYSEQUENCE_TOPIC = TOPIC + "/keySequence"; //$NON-NLS-1$
	}

	public static interface ElementContainer {
		public static final String TOPIC = UITopicBase + "/application/ElementContainer"; //$NON-NLS-1$
		public static final String CHILDREN = "children"; //$NON-NLS-1$
		public static final String CHILDREN_TOPIC = TOPIC + "/children"; //$NON-NLS-1$
		public static final String ACTIVECHILD = "activeChild"; //$NON-NLS-1$
		public static final String ACTIVECHILD_TOPIC = TOPIC + "/activeChild"; //$NON-NLS-1$
	}

	public static interface GenericTile {
		public static final String TOPIC = UITopicBase + "/application/GenericTile"; //$NON-NLS-1$
		public static final String WEIGHTS = "weights"; //$NON-NLS-1$
		public static final String WEIGHTS_TOPIC = TOPIC + "/weights"; //$NON-NLS-1$
		public static final String HORIZONTAL = "horizontal"; //$NON-NLS-1$
		public static final String HORIZONTAL_TOPIC = TOPIC + "/horizontal"; //$NON-NLS-1$
	}

	public static interface TrimContainer {
		public static final String TOPIC = UITopicBase + "/application/TrimContainer"; //$NON-NLS-1$
		public static final String HORIZONTAL = "horizontal"; //$NON-NLS-1$
		public static final String HORIZONTAL_TOPIC = TOPIC + "/horizontal"; //$NON-NLS-1$
		public static final String SIDE = "side"; //$NON-NLS-1$
		public static final String SIDE_TOPIC = TOPIC + "/side"; //$NON-NLS-1$
	}

	public static interface Application {
		public static final String TOPIC = UITopicBase + "/application/Application"; //$NON-NLS-1$
		public static final String COMMANDS = "commands"; //$NON-NLS-1$
		public static final String COMMANDS_TOPIC = TOPIC + "/commands"; //$NON-NLS-1$
	}

	public static interface Item {
		public static final String TOPIC = UITopicBase + "/application/Item"; //$NON-NLS-1$
		public static final String ENABLED = "enabled"; //$NON-NLS-1$
		public static final String ENABLED_TOPIC = TOPIC + "/enabled"; //$NON-NLS-1$
		public static final String SELECTED = "selected"; //$NON-NLS-1$
		public static final String SELECTED_TOPIC = TOPIC + "/selected"; //$NON-NLS-1$
		public static final String SEPARATOR = "separator"; //$NON-NLS-1$
		public static final String SEPARATOR_TOPIC = TOPIC + "/separator"; //$NON-NLS-1$
	}

	public static interface HandledItem {
		public static final String TOPIC = UITopicBase + "/application/HandledItem"; //$NON-NLS-1$
		public static final String COMMAND = "command"; //$NON-NLS-1$
		public static final String COMMAND_TOPIC = TOPIC + "/command"; //$NON-NLS-1$
		public static final String WBCOMMAND = "wbCommand"; //$NON-NLS-1$
		public static final String WBCOMMAND_TOPIC = TOPIC + "/wbCommand"; //$NON-NLS-1$
		public static final String PARAMETERS = "parameters"; //$NON-NLS-1$
		public static final String PARAMETERS_TOPIC = TOPIC + "/parameters"; //$NON-NLS-1$
	}

	public static interface Part {
		public static final String TOPIC = UITopicBase + "/application/Part"; //$NON-NLS-1$
		public static final String MENUS = "menus"; //$NON-NLS-1$
		public static final String MENUS_TOPIC = TOPIC + "/menus"; //$NON-NLS-1$
		public static final String TOOLBAR = "toolbar"; //$NON-NLS-1$
		public static final String TOOLBAR_TOPIC = TOPIC + "/toolbar"; //$NON-NLS-1$
	}

	public static interface Window {
		public static final String TOPIC = UITopicBase + "/application/Window"; //$NON-NLS-1$
		public static final String MAINMENU = "mainMenu"; //$NON-NLS-1$
		public static final String MAINMENU_TOPIC = TOPIC + "/mainMenu"; //$NON-NLS-1$
		public static final String X = "x"; //$NON-NLS-1$
		public static final String X_TOPIC = TOPIC + "/x"; //$NON-NLS-1$
		public static final String Y = "y"; //$NON-NLS-1$
		public static final String Y_TOPIC = TOPIC + "/y"; //$NON-NLS-1$
		public static final String WIDTH = "width"; //$NON-NLS-1$
		public static final String WIDTH_TOPIC = TOPIC + "/width"; //$NON-NLS-1$
		public static final String HEIGHT = "height"; //$NON-NLS-1$
		public static final String HEIGHT_TOPIC = TOPIC + "/height"; //$NON-NLS-1$
	}

	public static interface KeyBinding {
		public static final String TOPIC = UITopicBase + "/application/KeyBinding"; //$NON-NLS-1$
		public static final String COMMAND = "command"; //$NON-NLS-1$
		public static final String COMMAND_TOPIC = TOPIC + "/command"; //$NON-NLS-1$
		public static final String PARAMETERS = "parameters"; //$NON-NLS-1$
		public static final String PARAMETERS_TOPIC = TOPIC + "/parameters"; //$NON-NLS-1$
	}

	public static interface BindingContainer {
		public static final String TOPIC = UITopicBase + "/application/BindingContainer"; //$NON-NLS-1$
		public static final String BINDINGS = "bindings"; //$NON-NLS-1$
		public static final String BINDINGS_TOPIC = TOPIC + "/bindings"; //$NON-NLS-1$
	}

	public static interface EditorStack {
		public static final String TOPIC = UITopicBase + "/application/EditorStack"; //$NON-NLS-1$
		public static final String INPUTURI = "inputURI"; //$NON-NLS-1$
		public static final String INPUTURI_TOPIC = TOPIC + "/inputURI"; //$NON-NLS-1$
	}

	public static interface IDEWindow {
		public static final String TOPIC = UITopicBase + "/application/IDEWindow"; //$NON-NLS-1$
		public static final String MAINMENU = "mainMenu"; //$NON-NLS-1$
		public static final String MAINMENU_TOPIC = TOPIC + "/mainMenu"; //$NON-NLS-1$
	}

	public static String buildTopic(String topic, String type) {
		return topic + TOPIC_SEP + type;
	}

	private UIEvents() {
	}
}
