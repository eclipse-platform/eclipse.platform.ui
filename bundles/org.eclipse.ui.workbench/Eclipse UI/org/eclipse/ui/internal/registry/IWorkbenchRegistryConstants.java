/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dan Rubel <dan_rubel@instantiations.com>
 *     - Fix for bug 11490 - define hidden view (placeholder for view) in plugin.xml    
 *     Markus Alexander Kuppe, Versant Corporation - bug #215797
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.ui.PlatformUI;

/**
 * Interface containing various registry constants (tag and attribute names).
 * 
 * @since 3.1
 */
public interface IWorkbenchRegistryConstants {

	/**
	 * Accelerator attribute. Value <code>accelerator</code>.
	 */
	public static String ATT_ACCELERATOR = "accelerator"; //$NON-NLS-1$

	/**
	 * Adaptable attribute. Value <code>adaptable</code>.
	 */
	public static String ATT_ADAPTABLE = "adaptable"; //$NON-NLS-1$

	/**
	 * Advisor id attribute. Value <code>triggerPointAdvisorId</code>.
	 */
	public static String ATT_ADVISORID = "triggerPointAdvisorId"; //$NON-NLS-1$

	/**
	 * Allow label update attribute. Value <code>allowLabelUpdate</code>.
	 */
	public static String ATT_ALLOW_LABEL_UPDATE = "allowLabelUpdate";//$NON-NLS-1$

	/**
	 * View multiple attribute. Value <code>allowMultiple</code>.
	 */
	public static String ATT_ALLOW_MULTIPLE = "allowMultiple"; //$NON-NLS-1$

	/**
	 * Attribute that specifies whether a view gets restored upon workbench restart. Value <code>restorable</code>.
	 */
	public static String ATT_RESTORABLE = "restorable"; //$NON-NLS-1$
	
	/**
	 * Attribute that specifies whether a wizard is immediately capable of
	 * finishing. Value <code>canFinishEarly</code>.
	 */
	public static String ATT_CAN_FINISH_EARLY = "canFinishEarly"; //$NON-NLS-1$

	/**
	 * The name of the category attribute, which appears on a command
	 * definition.
	 */
	public static String ATT_CATEGORY = "category"; //$NON-NLS-1$

	/**
	 * Category id attribute. Value <code>categoryId</code>.
	 */
	public static String ATT_CATEGORY_ID = "categoryId"; //$NON-NLS-1$
	
	/**
	 * The name of the attribute storing checkEnabled for the visibleWhen
	 * element. Value <code>checkEnabled</code>.
	 */
	public static String ATT_CHECK_ENABLED = "checkEnabled"; //$NON-NLS-1$

	/**
	 * Class attribute. Value <code>class</code>.
	 */
	public static String ATT_CLASS = "class"; //$NON-NLS-1$

	/**
	 * Sticky view closable attribute. Value <code>closable</code>.
	 */
	public static String ATT_CLOSEABLE = "closeable"; //$NON-NLS-1$    

	/**
	 * Color factory attribute. Value <code>colorFactory</code>.
	 */
	public static String ATT_COLORFACTORY = "colorFactory"; //$NON-NLS-1$

	/**
	 * Editor command attribute. Value <code>command</code>.
	 */
	public static String ATT_COMMAND = "command";//$NON-NLS-1$

	/**
	 * The name of the attribute storing the command id.
	 */
	public static String ATT_COMMAND_ID = "commandId"; //$NON-NLS-1$

	/**
	 * The name of the configuration attribute storing the scheme id for a
	 * binding.
	 */
	public static String ATT_CONFIGURATION = "configuration"; //$NON-NLS-1$

	/**
	 * Intro content detector class attribute (optional). Value <code>contentDetector</code>.
	 */
	public static String ATT_CONTENT_DETECTOR = "contentDetector"; //$NON-NLS-1$
	
	/**
	 * Editor content type id binding attribute. Value
	 * <code>contentTypeId</code>.
	 */
	public static String ATT_CONTENT_TYPE_ID = "contentTypeId"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the context id for a binding.
	 */
	public static String ATT_CONTEXT_ID = "contextId"; //$NON-NLS-1$

	/**
	 * Editor contributor class attribute. Value <code>contributorClass</code>.
	 */
	public static String ATT_CONTRIBUTOR_CLASS = "contributorClass"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the AbstractParameterValueConverter for
	 * a commandParameterType.
	 */
	public static String ATT_CONVERTER = "converter"; //$NON-NLS-1$

	/**
	 * Perspective default attribute. Value <code>default</code>.
	 */
	public static String ATT_DEFAULT = "default";//$NON-NLS-1$

	/**
	 * The name of the default handler attribute, which appears on a command
	 * definition.
	 */
	public static String ATT_DEFAULT_HANDLER = "defaultHandler"; //$NON-NLS-1$

	/**
	 * Defaults-to attribute. Value <code>defaultsTo</code>.
	 */
	public static String ATT_DEFAULTS_TO = "defaultsTo"; //$NON-NLS-1$

	/**
	 * Action definition id attribute. Value <code>definitionId</code>.
	 */
	public static String ATT_DEFINITION_ID = "definitionId";//$NON-NLS-1$

	/**
	 * The name of the description attribute, which appears on named handle
	 * objects.
	 */
	public static String ATT_DESCRIPTION = "description"; //$NON-NLS-1$

	/**
	 * Description image attribute. Value <code>descriptionImage</code>.
	 */
	public static String ATT_DESCRIPTION_IMAGE = "descriptionImage"; //$NON-NLS-1$

	/**
	 * Disabled icon attribute. Value <code>disabledIcon</code>.
	 */
	public static String ATT_DISABLEDICON = "disabledIcon";//$NON-NLS-1$

	/**
	 * Enables-for attribute. Value <code>enablesFor</code>.
	 */
	public static String ATT_ENABLES_FOR = "enablesFor"; //$NON-NLS-1$

	/**
	 * Editor extensions attribute. Value <code>extensions</code>.
	 */
	public static String ATT_EXTENSIONS = "extensions";//$NON-NLS-1$

	/**
	 * View ratio attribute. Value <code>fastViewWidthRatio</code>.
	 */
	public static String ATT_FAST_VIEW_WIDTH_RATIO = "fastViewWidthRatio"; //$NON-NLS-1$

	/**
	 * Editor filenames attribute. Value <code>filenames</code>.
	 */
	public static String ATT_FILENAMES = "filenames";//$NON-NLS-1$

	/**
	 * Trim fill major attribute. Value <code>fillMajor</code>.
	 */
	public static String ATT_FILL_MAJOR = "fillMajor";//$NON-NLS-1$

	/**
	 * Trim fill minor attribute. Value <code>fillMinor</code>.
	 */
	public static String ATT_FILL_MINOR = "fillMinor";//$NON-NLS-1$

	/**
	 * Perspective fixed attribute. Value <code>fixed</code>.
	 */
	public static String ATT_FIXED = "fixed";//$NON-NLS-1$

	/**
	 * Attribute that specifies whether a wizard has any pages. Value
	 * <code>hasPages</code>.
	 */
	public static String ATT_HAS_PAGES = "hasPages"; //$NON-NLS-1$

	/**
	 * Help context id attribute. Value <code>helpContextId</code>.
	 */
	public static String ATT_HELP_CONTEXT_ID = "helpContextId";//$NON-NLS-1$

	/**
	 * Help url attribute. Value <code>helpHref</code>.
	 */
	public static String ATT_HELP_HREF = "helpHref"; //$NON-NLS-1$

	/**
	 * Hover icon attribute. Value <code>hoverIcon</code>.
	 */
	public static String ATT_HOVERICON = "hoverIcon";//$NON-NLS-1$

	/**
	 * Icon attribute. Value <code>icon</code>.
	 */
	public static String ATT_ICON = "icon"; //$NON-NLS-1$

	/**
	 * Id attribute. Value <code>id</code>.
	 */
	public static String ATT_ID = "id"; //$NON-NLS-1$

	/**
	 * The name of the image style attribute, which is used on location elements
	 * in the menus extension point.
	 */
	public static String ATT_IMAGE_STYLE = "imageStyle"; //$NON-NLS-1$

	/**
	 * Is-editable attribute. Value <code>isEditable</code>.
	 */
	public static String ATT_IS_EDITABLE = "isEditable"; //$NON-NLS-1$

	/**
	 * Keys attribute. Value <code>keys</code>.
	 */
	public static String ATT_KEY = "key"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the identifier for the active key
	 * configuration identifier. This provides legacy support for the
	 * <code>activeKeyConfiguration</code> element in the commands extension
	 * point.
	 */
	public static String ATT_KEY_CONFIGURATION_ID = "keyConfigurationId"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the trigger sequence for a binding.
	 * This is called a 'keySequence' for legacy reasons.
	 */
	public static String ATT_KEY_SEQUENCE = "keySequence"; //$NON-NLS-1$

	/**
	 * Label attribute. Value <code>label</code>.
	 */
	public static String ATT_LABEL = "label"; //$NON-NLS-1$

	/**
	 * Editor launcher attribute. Value <code>launcher</code>.
	 */
	public static String ATT_LAUNCHER = "launcher";//$NON-NLS-1$

	/**
	 * Lightweight decorator tag. Value <code>lightweight</code>.
	 */
	public static String ATT_LIGHTWEIGHT = "lightweight"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the locale for a binding.
	 */
	public static String ATT_LOCALE = "locale"; //$NON-NLS-1$

	/**
	 * Sticky view location attribute. Value <code>location</code>.
	 */
	public static String ATT_LOCATION = "location"; //$NON-NLS-1$

	/**
	 * Editor management strategy attribute. Value <code>matchingStrategy</code>.
	 */
	public static String ATT_MATCHING_STRATEGY = "matchingStrategy"; //$NON-NLS-1$

	/**
	 * The name of the menu identifier attribute, which appears on items.
	 */
	public static String ATT_MENU_ID = "menuId"; //$NON-NLS-1$

	/**
	 * Menubar path attribute. Value <code>menubarPath</code>.
	 */
	public static String ATT_MENUBAR_PATH = "menubarPath";//$NON-NLS-1$

	/**
	 * The name of the mnemonic attribute, which appears on locations.
	 */
	public static String ATT_MNEMONIC = "mnemonic"; //$NON-NLS-1$

	/**
	 * The name of the minimized attribute, which appears
	 * when adding a view in a perspectiveExtension.
	 */
	public static String ATT_MINIMIZED = "minimized"; //$NON-NLS-1$

	/**
	 * Sticky view moveable attribute. Value <code>moveable</code>.
	 */
	public static String ATT_MOVEABLE = "moveable"; //$NON-NLS-1$

	/**
	 * Name attribute. Value <code>name</code>.
	 */
	public static String ATT_NAME = "name"; //$NON-NLS-1$

	/**
	 * Name filter attribute. Value <code>nameFilter</code>.
	 */
	public static String ATT_NAME_FILTER = "nameFilter"; //$NON-NLS-1$

	/**
	 * Node attribute. Value <code>node</code>.
	 */
	public static String ATT_NODE = "node"; //$NON-NLS-1$

	/**
	 * Object class attribute. Value <code>objectClass</code>.
	 */
	public static String ATT_OBJECTCLASS = "objectClass";//$NON-NLS-1$

	/**
	 * The name of the optional attribute, which appears on parameter
	 * definitions.
	 */
	public static String ATT_OPTIONAL = "optional"; //$NON-NLS-1$

	/**
	 * Operating system attribute. Value <code>os</code>.
	 */
	public static String ATT_OS = "os"; //$NON-NLS-1$

	/**
	 * The name of the deprecated parent attribute, which appears on scheme
	 * definitions.
	 */
	public static String ATT_PARENT = "parent"; //$NON-NLS-1$

	/**
	 * View parent category attribute. Value <code>parentCategory</code>.
	 */
	public static String ATT_PARENT_CATEGORY = "parentCategory"; //$NON-NLS-1$

	/**
	 * Parent id attribute. Value <code>parentId</code>.
	 */
	public static String ATT_PARENT_ID = "parentId"; //$NON-NLS-1$

	/**
	 * The name of the deprecated parent scope attribute, which appears on
	 * contexts definitions.
	 */
	public static String ATT_PARENT_SCOPE = "parentScope"; //$NON-NLS-1$

	/**
	 * Path attribute. Value <code>path</code>.
	 */
	public static String ATT_PATH = "path"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the platform for a binding.
	 */
	public static String ATT_PLATFORM = "platform"; //$NON-NLS-1$

	/**
	 * The name of the position attribute, which appears on order elements.
	 */
	public static String ATT_POSITION = "position"; //$NON-NLS-1$

	/**
	 * Presentation id attribute. Value <code>presentationId</code>.
	 */
	public static String ATT_PRESENTATIONID = "presentationId"; //$NON-NLS-1$

	/**
	 * Product id attribute. Value <code>productId</code>.
	 */
	public static String ATT_PRODUCTID = "productId"; //$NON-NLS-1$

	/**
	 * Project attribute. Value <code>project</code>.
	 */
	// @issue project-specific attribute and behavior
	public static String ATT_PROJECT = "project";//$NON-NLS-1$	/**

	/**
	 * The name of the pulldown attribute, which indicates whether the class is
	 * a pulldown delegate.
	 */
	public static String ATT_PULLDOWN = "pulldown"; //$NON-NLS-1$

	/**
	 * View ratio attribute. Value <code>ratio</code>.
	 */
	public static String ATT_RATIO = "ratio"; //$NON-NLS-1$

	/**
	 * Relationship attribute. Value <code>relationship</code>.
	 */
	public static String ATT_RELATIONSHIP = "relationship";//$NON-NLS-1$

	/**
	 * Relative attribute. Value <code>relative</code>.
	 */
	public static String ATT_RELATIVE = "relative";//$NON-NLS-1$

	/**
	 * The name of the relativeTo attribute, which appears on order elements.
	 */
	public static String ATT_RELATIVE_TO = "relativeTo"; //$NON-NLS-1$

	/**
	 * Retarget attribute. Value <code>retarget</code>.
	 */
	public static String ATT_RETARGET = "retarget";//$NON-NLS-1$

	/**
	 * The name of the returnTypeId attribute, which appears on command
	 * elements.
	 */
	public static String ATT_RETURN_TYPE_ID = "returnTypeId"; //$NON-NLS-1$

	/**
	 * The name of the attribute storing the identifier for the active scheme.
	 * This is called a 'keyConfigurationId' for legacy reasons.
	 */
	public static String ATT_SCHEME_ID = "schemeId"; //$NON-NLS-1$

	/**
	 * Scope attribute. Value <code>scope</code>.
	 */
	public static String ATT_SCOPE = "scope"; //$NON-NLS-1$

	/**
	 * The name of the separatorsVisible attribute, which appears on group
	 * elements.
	 */
	public static String ATT_SEPARATORS_VISIBLE = "separatorsVisible"; //$NON-NLS-1$

	/**
	 * The name of the sequence attribute for a key binding.
	 */
	public static String ATT_SEQUENCE = "sequence"; //$NON-NLS-1$

	/**
	 * Show title attribute. Value <code>showTitle</code>.
	 */
	public static String ATT_SHOW_TITLE = "showTitle";//$NON-NLS-1$

	/**
	 * Perspective singleton attribute. Value <code>singleton</code>.
	 */
	public static String ATT_SINGLETON = "singleton";//$NON-NLS-1$
	
	/**
	 * Splash id attribute.  Value <code>splashId</code>.
	 * 
	 * @since 3.3
	 */
	public static String ATT_SPLASH_ID = "splashId"; //$NON-NLS-1$

	/**
	 * Standalone attribute. Value <code>standalone</code>.
	 */
	public static String ATT_STANDALONE = "standalone";//$NON-NLS-1$

	/**
	 * Action state attribute. Value <code>state</code>.
	 */
	public static String ATT_STATE = "state";//$NON-NLS-1$

	/**
	 * The name of the string attribute (key sequence) for a binding in the
	 * commands extension point.
	 */
	public static String ATT_STRING = "string"; //$NON-NLS-1$

	/**
	 * Action style attribute. Value <code>style</code>.
	 */
	public static String ATT_STYLE = "style";//$NON-NLS-1$

	/**
	 * Target attribute. Value <code>targetID</code>.
	 */
	public static String ATT_TARGET_ID = "targetID";//$NON-NLS-1$

	/**
	 * Toolbar path attribute. Value <code>toolbarPath</code>.
	 */
	public static String ATT_TOOLBAR_PATH = "toolbarPath";//$NON-NLS-1$

	/**
	 * Tooltip attribute. Value <code>tooltip</code>.
	 */
	public static String ATT_TOOLTIP = "tooltip";//$NON-NLS-1$

	/**
	 * The name of the type attribute, which appears on bar elements and
	 * commandParameterType elments.
	 */
	public static String ATT_TYPE = "type"; //$NON-NLS-1$

	/**
	 * The name of the typeId attribute, which appears on commandParameter
	 * elements.
	 */
	public static String ATT_TYPE_ID = "typeId"; //$NON-NLS-1$

	/**
	 * Value attribute. Value <code>value</code>.
	 */
	public static String ATT_VALUE = "value"; //$NON-NLS-1$

	/**
	 * Visible attribute. Value <code>visible</code>.
	 */
	// ATT_VISIBLE added by dan_rubel@instantiations.com
	public static String ATT_VISIBLE = "visible";//$NON-NLS-1$

	/**
	 * Windowing system attribute. Value <code>ws</code>.
	 */
	public static String ATT_WS = "ws"; //$NON-NLS-1$

	/**
	 * The prefix that all auto-generated identifiers start with. This makes the
	 * identifier recognizable as auto-generated, and further helps ensure that
	 * it does not conflict with existing identifiers.
	 */
	public static String AUTOGENERATED_PREFIX = "AUTOGEN:::"; //$NON-NLS-1$

	/**
	 * The legacy extension point (2.1.x and earlier) for specifying a key
	 * binding scheme.
	 * 
	 * @since 3.1.1
	 */
	public static String PL_ACCELERATOR_CONFIGURATIONS = "acceleratorConfigurations"; //$NON-NLS-1$

	/**
	 * The legacy extension point (2.1.x and earlier) for specifying a context.
	 * 
	 * @since 3.1.1
	 */
	public static String PL_ACCELERATOR_SCOPES = "acceleratorScopes"; //$NON-NLS-1$

	/**
	 * The legacy extension point (2.1.x and earlier) for specifying a command.
	 * 
	 * @since 3.1.1
	 */
	public static String PL_ACTION_DEFINITIONS = "actionDefinitions"; //$NON-NLS-1$

	public static String PL_ACTION_SET_PART_ASSOCIATIONS = "actionSetPartAssociations"; //$NON-NLS-1$

	public static String PL_ACTION_SETS = "actionSets"; //$NON-NLS-1$

	public static String PL_ACTIVITIES = "activities"; //$NON-NLS-1$

	public static String PL_ACTIVITYSUPPORT = "activitySupport"; //$NON-NLS-1$

	/**
	 * The extension point (3.1 and later) for specifying bindings, such as
	 * keyboard shortcuts.
	 * 
	 * @since 3.1.1
	 */
	public static String PL_BINDINGS = "bindings"; //$NON-NLS-1$

	public static String PL_BROWSER_SUPPORT = "browserSupport"; //$NON-NLS-1$

	public static String PL_COLOR_DEFINITIONS = "colorDefinitions"; //$NON-NLS-1$

	/**
	 * The extension point (3.2 and later) for associating images with commands.
	 * 
	 * @since 3.2
	 */
	public static String PL_COMMAND_IMAGES = "commandImages"; //$NON-NLS-1$

	/**
	 * The extension point (2.1.x and later) for specifying a command. A lot of
	 * other things have appeared first in this extension point and then been
	 * moved to their own extension point.
	 * 
	 * @since 3.1.1
	 */
	public static String PL_COMMANDS = "commands"; //$NON-NLS-1$

	/**
	 * The extension point (3.0 and later) for specifying a context.
	 * 
	 * @since 3.1.1
	 */
	public static String PL_CONTEXTS = "contexts"; //$NON-NLS-1$

	public static String PL_DECORATORS = "decorators"; //$NON-NLS-1$

	public static String PL_DROP_ACTIONS = "dropActions"; //$NON-NLS-1$

	public static String PL_EDITOR = "editors"; //$NON-NLS-1$

	public static String PL_EDITOR_ACTIONS = "editorActions"; //$NON-NLS-1$

	public static String PL_ELEMENT_FACTORY = "elementFactories"; //$NON-NLS-1$

	/**
	 * The extension point for encoding definitions.
	 */
	public static String PL_ENCODINGS = "encodings"; //$NON-NLS-1$

	public static String PL_EXPORT = "exportWizards"; //$NON-NLS-1$

	public static String PL_FONT_DEFINITIONS = "fontDefinitions"; //$NON-NLS-1$

	/**
	 * The extension point (3.1 and later) for specifying handlers.
	 * 
	 * @since 3.1.1
	 */
	public static String PL_HANDLERS = "handlers"; //$NON-NLS-1$

	public static String PL_HELPSUPPORT = "helpSupport"; //$NON-NLS-1$

	public static String PL_IMPORT = "importWizards"; //$NON-NLS-1$

	public static String PL_INTRO = "intro"; //$NON-NLS-1$

	/**
	 * The extension point for keyword definitions.
	 * 
	 * @since 3.1
	 */
	public static String PL_KEYWORDS = "keywords"; //$NON-NLS-1$

	/**
	 * The extension point (3.2 and later) for specifying menu contributions.
	 * 
	 * @since 3.2
	 */
	public static String PL_MENUS = "menus"; //$NON-NLS-1$

	/**
	 * The extension point (3.3 and later) for specifying menu contributions.
	 * 
	 * @since 3.3
	 */
	public static String PL_MENU_CONTRIBUTION = "menuContribution"; //$NON-NLS-1$

	public static String PL_NEW = "newWizards"; //$NON-NLS-1$

	public static String PL_PERSPECTIVE_EXTENSIONS = "perspectiveExtensions"; //$NON-NLS-1$

	public static String PL_PERSPECTIVES = "perspectives"; //$NON-NLS-1$

	public static String PL_POPUP_MENU = "popupMenus"; //$NON-NLS-1$

	public static String PL_PREFERENCE_TRANSFER = "preferenceTransfer"; //$NON-NLS-1$

	public static String PL_PREFERENCES = "preferencePages"; //$NON-NLS-1$

	public static String PL_PRESENTATION_FACTORIES = "presentationFactories"; //$NON-NLS-1$

	public static String PL_PROPERTY_PAGES = "propertyPages"; //$NON-NLS-1$

	public static String PL_STARTUP = "startup"; //$NON-NLS-1$
	
	/**
	 * @since 3.3
	 */
	public static String PL_SPLASH_HANDLERS = "splashHandlers"; //$NON-NLS-1$

	public static String PL_SYSTEM_SUMMARY_SECTIONS = "systemSummarySections"; //$NON-NLS-1$

	public static String PL_THEMES = "themes"; //$NON-NLS-1$

	public static String PL_VIEW_ACTIONS = "viewActions"; //$NON-NLS-1$

	public static String PL_VIEWS = "views"; //$NON-NLS-1$

	public static String PL_WORKINGSETS = "workingSets"; //$NON-NLS-1$

	/**
	 * The name of the deprecated accelerator configurations extension point.
	 */
	public static String EXTENSION_ACCELERATOR_CONFIGURATIONS = PlatformUI.PLUGIN_ID
			+ '.' + PL_ACCELERATOR_CONFIGURATIONS;

	/**
	 * The name of the accelerator scopes extension point.
	 */
	public static String EXTENSION_ACCELERATOR_SCOPES = PlatformUI.PLUGIN_ID
			+ '.' + PL_ACCELERATOR_SCOPES;

	/**
	 * The name of the action definitions extension point.
	 */
	public static String EXTENSION_ACTION_DEFINITIONS = PlatformUI.PLUGIN_ID
			+ '.' + PL_ACTION_DEFINITIONS;

	/**
	 * The name of the <code>org.eclipse.ui.actionSets</code> extension point.
	 */
	public static String EXTENSION_ACTION_SETS = PlatformUI.PLUGIN_ID
			+ '.' + IWorkbenchRegistryConstants.PL_ACTION_SETS;

	/**
	 * The name of the bindings extension point.
	 */
	public static String EXTENSION_BINDINGS = PlatformUI.PLUGIN_ID + '.'
			+ PL_BINDINGS;

	/**
	 * The name of the commands extension point.
	 */
	public static String EXTENSION_COMMAND_IMAGES = PlatformUI.PLUGIN_ID
			+ '.' + PL_COMMAND_IMAGES;

	/**
	 * The name of the commands extension point, and the name of the key for the
	 * commands preferences.
	 */
	public static String EXTENSION_COMMANDS = PlatformUI.PLUGIN_ID + '.'
			+ PL_COMMANDS;

	/**
	 * The name of the contexts extension point.
	 */
	public static String EXTENSION_CONTEXTS = PlatformUI.PLUGIN_ID + '.'
			+ PL_CONTEXTS;

	/**
	 * The name of the <code>org.eclipse.ui.editorActions</code> extension
	 * point.
	 */
	public static String EXTENSION_EDITOR_ACTIONS = PlatformUI.PLUGIN_ID
			+ '.' + PL_EDITOR_ACTIONS;

	/**
	 * The name of the commands extension point.
	 */
	public static String EXTENSION_HANDLERS = PlatformUI.PLUGIN_ID + '.'
			+ PL_HANDLERS;

	/**
	 * The name of the <code>org.eclipse.ui.menus</code> extension point.
	 */
	public static String EXTENSION_MENUS = PlatformUI.PLUGIN_ID + '.'
			+ PL_MENUS;

	/**
	 * The name of the <code>org.eclipse.ui.menus2</code> extension point.
	 */
	public static String COMMON_MENU_ADDITIONS = PlatformUI.PLUGIN_ID + '.'
			+ PL_MENUS + '2';

	/**
	 * The name of the <code>org.eclipse.ui.popupMenus</code> extension point.
	 */
	public static String EXTENSION_POPUP_MENUS = PlatformUI.PLUGIN_ID
			+ '.' + PL_POPUP_MENU;

	/**
	 * The name of the <code>org.eclipse.ui.viewActions</code> extension
	 * point.
	 */
	public static String EXTENSION_VIEW_ACTIONS = PlatformUI.PLUGIN_ID
			+ '.' + PL_VIEW_ACTIONS;

	/**
	 * The constant for the position attribute corresponding to
	 * {@link SOrder#POSITION_AFTER}.
	 */
	public static String POSITION_AFTER = "after"; //$NON-NLS-1$

	/**
	 * The constant for the position attribute corresponding to
	 * {@link SOrder#POSITION_BEFORE}.
	 */
	public static String POSITION_BEFORE = "before"; //$NON-NLS-1$

	/**
	 * The constant for the position attribute corresponding to
	 * {@link SOrder#POSITION_END}.
	 */
	public static String POSITION_END = "end"; //$NON-NLS-1$

	/**
	 * The constant for the position attribute corresponding to
	 * {@link SOrder#POSITION_START}.
	 */
	public static String POSITION_START = "start"; //$NON-NLS-1$

	/**
	 * The action style for drop-down menus.
	 */
	public static String STYLE_PULLDOWN = "pulldown"; //$NON-NLS-1$

	/**
	 * The action style for radio buttons.
	 */
	public static String STYLE_RADIO = "radio"; //$NON-NLS-1$

	/**
	 * The action style for check boxes.
	 */
	public static String STYLE_TOGGLE = "toggle"; //$NON-NLS-1$

	/**
	 * The name of the deprecated accelerator configuration element. This
	 * element was used in 2.1.x and earlier to define groups of what are now
	 * called schemes.
	 */
	public static String TAG_ACCELERATOR_CONFIGURATION = "acceleratorConfiguration"; //$NON-NLS-1$

	/**
	 * The name of the element storing a deprecated accelerator scope.
	 */
	public static String TAG_ACCELERATOR_SCOPE = "acceleratorScope"; //$NON-NLS-1$

	/**
	 * Action tag. Value <code>action</code>.
	 */
	public static String TAG_ACTION = "action"; //$NON-NLS-1$

	/**
	 * The name of the element storing an action definition. This element only
	 * existed in
	 */
	public static String TAG_ACTION_DEFINITION = "actionDefinition"; //$NON-NLS-1$

	/**
	 * Action set tag. Value <code>actionSet</code>.
	 */
	public static String TAG_ACTION_SET = "actionSet";//$NON-NLS-1$

	/**
	 * Part association tag. Value <code>actionSetPartAssociation</code>.
	 */
	public static String TAG_ACTION_SET_PART_ASSOCIATION = "actionSetPartAssociation";//$NON-NLS-1$

	/**
	 * The name of the element storing the active key configuration from the
	 * commands extension point.
	 */
	public static String TAG_ACTIVE_KEY_CONFIGURATION = "activeKeyConfiguration"; //$NON-NLS-1$

	/**
	 * The name of the active when element, which appears on a handler
	 * definition.
	 */
	public static String TAG_ACTIVE_WHEN = "activeWhen"; //$NON-NLS-1$

	/**
	 * Activity image binding tag. Value <code>activityImageBindingw</code>.
	 */
	public static String TAG_ACTIVITY_IMAGE_BINDING = "activityImageBinding"; //$NON-NLS-1$

	/**
	 * Advisor to product binding element. Value
	 * <code>triggerPointAdvisorProductBinding</code>.
	 */
	public static String TAG_ADVISORPRODUCTBINDING = "triggerPointAdvisorProductBinding"; //$NON-NLS-1$

	/**
	 * The name of the bar element, which appears in a location definition.
	 */
	public static String TAG_BAR = "bar"; //$NON-NLS-1$

	/**
	 * Category tag. Value <code>category</code>.
	 */
	public static String TAG_CATEGORY = "category";//$NON-NLS-1$

	/**
	 * Category image binding tag. Value <code>categoryImageBinding</code>.
	 */
	public static String TAG_CATEGORY_IMAGE_BINDING = "categoryImageBinding"; //$NON-NLS-1$

	/**
	 * Element category tag. Value <code>themeElementCategory</code>.
	 */
	public static String TAG_CATEGORYDEFINITION = "themeElementCategory"; //$NON-NLS-1$

	/**
	 * Category presentation tag. Value <code>categoryPresentationBinding</code>.
	 */
	public static String TAG_CATEGORYPRESENTATIONBINDING = "categoryPresentationBinding"; //$NON-NLS-1$

	/**
	 * The name of the class element, which appears on an executable extension.
	 */
	public static String TAG_CLASS = ATT_CLASS;

	/**
	 * Color definition tag. Value <code>colorDefinition</code>.
	 */
	public static String TAG_COLORDEFINITION = "colorDefinition"; //$NON-NLS-1$

	/**
	 * Color override tag. Value <code>colorOverride</code>.
	 */
	public static String TAG_COLOROVERRIDE = "colorOverride"; //$NON-NLS-1$    

	/**
	 * Color value tag. Value <code>colorValue</code>.
	 */
	public static String TAG_COLORVALUE = "colorValue"; //$NON-NLS-1$

	/**
	 * The name of the element storing a command.
	 */
	public static String TAG_COMMAND = "command"; //$NON-NLS-1$

	/**
	 * The name of the element storing a parameter.
	 */
	public static String TAG_COMMAND_PARAMETER = "commandParameter"; //$NON-NLS-1$

	/**
	 * The name of the element storing a parameter type.
	 */
	public static String TAG_COMMAND_PARAMETER_TYPE = "commandParameterType"; //$NON-NLS-1$

	/**
	 * Editor content type binding tag. Value <code>contentTypeBinding</code>.
	 */
	public static String TAG_CONTENT_TYPE_BINDING = "contentTypeBinding"; //$NON-NLS-1$

	/**
	 * The name of the element storing a context.
	 */
	public static String TAG_CONTEXT = "context"; //$NON-NLS-1$

	/**
	 * Data tag. Value <code>data</code>.
	 */
	public static String TAG_DATA = "data"; //$NON-NLS-1$

	/**
	 * The name of the default handler element, which appears on a command
	 * definition.
	 */
	public static String TAG_DEFAULT_HANDLER = ATT_DEFAULT_HANDLER;

	/**
	 * Description element. Value <code>description</code>.
	 */
	public static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$

	/**
	 * The name of the dynamic menu element, which appears in a group or menu
	 * definition.
	 */
	public static String TAG_DYNAMIC = "dynamic"; //$NON-NLS-1$

	/**
	 * Editor tag. Value <code>editor</code>.
	 */
	public static String TAG_EDITOR = "editor";//$NON-NLS-1$

	/**
	 * The name of the deprecated editorContribution element. This is used for
	 * contributing actions to the top-level menus and tool bars when particular
	 * editors are visible.
	 */
	public static String TAG_EDITOR_CONTRIBUTION = "editorContribution"; //$NON-NLS-1$

	/**
	 * The name of the enabled when element, which appears on a handler
	 * definition.
	 */
	public static String TAG_ENABLED_WHEN = "enabledWhen"; //$NON-NLS-1$

	/**
	 * Enablement tag. Value <code>enablement</code>.
	 */
	public static String TAG_ENABLEMENT = "enablement"; //$NON-NLS-1$

	/**
	 * Entry tag. Value <code>entry</code>.
	 */
	public static String TAG_ENTRY = "entry"; //$NON-NLS-1$

	/**
	 * Filter tag. Value <code>filter</code>.
	 */
	public static String TAG_FILTER = "filter"; //$NON-NLS-1$

	/***************************************************************************
	 * Font definition tag. Value <code>fontDefinition</code>.
	 */
	public static String TAG_FONTDEFINITION = "fontDefinition"; //$NON-NLS-1$

	/**
	 * Font override tag. Value <code>fontOverride</code>.
	 */
	public static String TAG_FONTOVERRIDE = "fontOverride"; //$NON-NLS-1$

	/**
	 * Font value tag. Value <code>fontValue</code>.
	 */
	public static String TAG_FONTVALUE = "fontValue"; //$NON-NLS-1$

	/**
	 * The name of the element storing a group.
	 */
	public static String TAG_GROUP = "group"; //$NON-NLS-1$ 

	/**
	 * Group marker tag. Value <code>groupMarker</code>.
	 */
	public static String TAG_GROUP_MARKER = "groupMarker"; //$NON-NLS-1$

	/**
	 * The name of the element storing a handler.
	 */
	public static String TAG_HANDLER = "handler"; //$NON-NLS-1$

	/**
	 * The name of the element storing a handler submission.
	 */
	public static String TAG_HANDLER_SUBMISSION = "handlerSubmission"; //$NON-NLS-1$

	/**
	 * Trigger point hint tag. Value <code>hint</code>.
	 */
	public static String TAG_HINT = "hint"; //$NON-NLS-1$

	/**
	 * The name of the element storing an image.
	 */
	public static String TAG_IMAGE = "image"; //$NON-NLS-1$

	/**
	 * The name of the element storing a key binding.
	 */
	public static String TAG_KEY = "key"; //$NON-NLS-1$

	/**
	 * The name of the key binding element in the commands extension point.
	 */
	public static String TAG_KEY_BINDING = "keyBinding"; //$NON-NLS-1$

	/**
	 * The name of the deprecated key configuration element in the commands
	 * extension point. This element has been replaced with the scheme element
	 * in the bindings extension point.
	 */
	public static String TAG_KEY_CONFIGURATION = "keyConfiguration"; //$NON-NLS-1$

	/**
	 * The name of the element storing a location.
	 */
	public static String TAG_LOCATION = "location"; //$NON-NLS-1$

	/**
	 * The name of the element defining the insertion point for menu
	 * additions.
	 * 
	 * @since 3.3
	 */
	public static String TAG_LOCATION_URI = "locationURI"; //$NON-NLS-1$

	/**
	 * The name of the element storing trim layout info for a widget.
	 */
	public static String TAG_LAYOUT = "layout"; //$NON-NLS-1$

	/**
	 * Mapping tag. Value <code>mapping</code>.
	 */
	public static String TAG_MAPPING = "mapping"; //$NON-NLS-1$

	/**
	 * Menu tag. Value <code>menu</code>.
	 */
	public static String TAG_MENU = "menu"; //$NON-NLS-1$

	/**
	 * Wizard shortcut tag. Value <code>newWizardShortcut</code>.
	 */
	public static String TAG_NEW_WIZARD_SHORTCUT = "newWizardShortcut";//$NON-NLS-1$

	/**
	 * Object contribution tag. Value <code>objectContribution</code>.
	 */
	public static String TAG_OBJECT_CONTRIBUTION = "objectContribution";//$NON-NLS-1$

	/**
	 * The name of the element storing the ordering information.
	 */
	public static String TAG_ORDER = "order"; //$NON-NLS-1$

	/**
	 * The name of the element storing a parameter.
	 */
	public static String TAG_PARAMETER = "parameter"; //$NON-NLS-1$

	/**
	 * Part tag. Value <code>part</code>.
	 */
	public static String TAG_PART = "part";//$NON-NLS-1$

	/**
	 * Perspective shortcut tag. Value <code>perspectiveShortcut</code>.
	 */
	public static String TAG_PERSP_SHORTCUT = "perspectiveShortcut";//$NON-NLS-1$

	/**
	 * Perspective tag. Value <code>perspective</code>.
	 */
	public static String TAG_PERSPECTIVE = "perspective";//$NON-NLS-1$

	/**
	 * Perspective extension tag. Value <code>perspectiveExtension</code>.
	 */
	public static String TAG_PERSPECTIVE_EXTENSION = "perspectiveExtension";//$NON-NLS-1$

	/**
	 * Primary wizard tag. Value <code>primaryWizard</code>.
	 */
	public static String TAG_PRIMARYWIZARD = "primaryWizard"; //$NON-NLS-1$

	/**
	 * The name of the element storing the a menu element reference.
	 */
	public static String TAG_REFERENCE = "reference"; //$NON-NLS-1$

	/**
	 * The name of the scheme element in the bindings extension point.
	 */
	public static String TAG_SCHEME = "scheme"; //$NON-NLS-1$

	/**
	 * The name of the element storing a deprecated scope.
	 */
	public static String TAG_SCOPE = "scope"; //$NON-NLS-1$

	/**
	 * Selectiont tag. Value <code>selection</code>.
	 */
	public static String TAG_SELECTION = "selection"; //$NON-NLS-1$

	/**
	 * Separator tag. Value <code>separator</code>.
	 */
	public static String TAG_SEPARATOR = "separator"; //$NON-NLS-1$
	
	
	/**
	 * Tag for the settings transfer entry.
	 */
	public static String TAG_SETTINGS_TRANSFER = "settingsTransfer"; //$NON-NLS-1$

	/**
	 * Show in part tag. Value <code>showInPart</code>.
	 */
	public static String TAG_SHOW_IN_PART = "showInPart";//$NON-NLS-1$

	/**
	 * The name of the element storing some state.
	 */
	public static String TAG_STATE = "state"; //$NON-NLS-1$
	
	
	/**
	 * The name of the element describing splash handlers. Value
	 * <code>splashHandler</code>.
	 * @since 3.3
	 */
	public static String TAG_SPLASH_HANDLER = "splashHandler"; //$NON-NLS-1$
	

	/**
	 * The name of the element describing splash handler product bindings. Value
	 * <code>splashHandlerProductBinding</code>.
	 * @since 3.3
	 */
	public static String TAG_SPLASH_HANDLER_PRODUCT_BINDING = "splashHandlerProductBinding"; //$NON-NLS-1$
	
	/**
	 * Sticky view tag. Value <code>stickyView</code>.
	 */
	public static String TAG_STICKYVIEW = "stickyView";//$NON-NLS-1$

	/**
	 * Browser support tag. Value <code>support</code>.
	 */
	public static String TAG_SUPPORT = "support"; //$NON-NLS-1$

	/**
	 * Theme tag. Value <code>theme</code>.
	 */
	public static String TAG_THEME = "theme";//$NON-NLS-1$

	/**
	 * Transfer tag. Value <code>transfer</code>.
	 */
	public static String TAG_TRANSFER = "transfer";//$NON-NLS-1$

	/**
	 * Trigger point tag. Value <code>triggerPoint</code>.
	 */
	public static String TAG_TRIGGERPOINT = "triggerPoint"; //$NON-NLS-1$

	/**
	 * Advisor tag. Value <code>triggerPointAdvisor</code>.
	 */
	public static String TAG_TRIGGERPOINTADVISOR = "triggerPointAdvisor"; //$NON-NLS-1$

	/**
	 * View tag. Value <code>view</code>.
	 */
	public static String TAG_VIEW = "view";//$NON-NLS-1$

	/**
	 * View shortcut tag. Value <code>viewShortcut</code>.
	 */
	public static String TAG_VIEW_SHORTCUT = "viewShortcut";//$NON-NLS-1$

	/**
	 * The name of the element storing a view contribution.
	 */
	public static String TAG_VIEW_CONTRIBUTION = "viewContribution"; //$NON-NLS-1$

	/**
	 * Viewer contribution tag. Value <code>viewerContribution</code>.
	 */
	public static String TAG_VIEWER_CONTRIBUTION = "viewerContribution"; //$NON-NLS-1$

	/**
	 * Visibility tag. Value <code>visibility</code>.
	 */
	public static String TAG_VISIBILITY = "visibility"; //$NON-NLS-1$

	/**
	 * The name of the element storing the visible when condition.
	 */
	public static String TAG_VISIBLE_WHEN = "visibleWhen"; //$NON-NLS-1$

	/**
	 * The name of the element storing a widget.
	 */
	public static String TAG_WIDGET = "widget"; //$NON-NLS-1$

	/**
	 * The name of the element storing a control hosted in a ToolBar.
	 */
	public static String TAG_CONTROL = "control"; //$NON-NLS-1$

	/**
	 * Wizard tag. Value <code>wizard</code>.
	 */
	public static String TAG_WIZARD = "wizard";//$NON-NLS-1$

	/**
	 * Working set tag. Value <code>workingSet</code>.
	 */
	public static String TAG_WORKING_SET = "workingSet"; //$NON-NLS-1$

	/**
	 * The type of reference which refers to a group.
	 */
	public static String TYPE_GROUP = "group"; //$NON-NLS-1$

	/**
	 * The type of reference which refers to an item.
	 */
	public static String TYPE_ITEM = "item"; //$NON-NLS-1$

	/**
	 * The type of reference which refers to an menu.
	 */
	public static String TYPE_MENU = "menu"; //$NON-NLS-1$

	/**
	 * The type of reference which refers to the widget.
	 */
	public static String TYPE_WIDGET = "widget"; //$NON-NLS-1$

	public static String TAG_TOOLBAR = "toolbar"; //$NON-NLS-1$
	
	public static String TAG_SERVICE_FACTORY = "serviceFactory"; //$NON-NLS-1$
	
	public static String TAG_SERVICE = "service"; //$NON-NLS-1$

	public static final String ATTR_FACTORY_CLASS = "factoryClass"; //$NON-NLS-1$

	public static final String ATTR_SERVICE_CLASS = "serviceClass"; //$NON-NLS-1$
	
	public static final String TAG_SOURCE_PROVIDER = "sourceProvider"; //$NON-NLS-1$

	public static final String ATTR_PROVIDER = "provider"; //$NON-NLS-1$

	public static final String TAG_VARIABLE = "variable"; //$NON-NLS-1$

	public static final String ATT_PRIORITY_LEVEL = "priorityLevel"; //$NON-NLS-1$
	
	public static final String ATT_MODE = "mode"; //$NON-NLS-1$
}
