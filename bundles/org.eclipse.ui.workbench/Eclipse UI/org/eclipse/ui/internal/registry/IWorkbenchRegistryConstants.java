/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dan Rubel <dan_rubel@instantiations.com>
 *     - Fix for bug 11490 - define hidden view (placeholder for view) in plugin.xml     
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

/**
 * Interface containing various registry constants (tag and attribute names).
 * 
 * @since 3.1
 */
public interface IWorkbenchRegistryConstants {

/* ***** Common constants ***** */
	
	/**
	 * Id attribute.  Value <code>id</code>.
	 */
	public static final String ATT_ID = "id"; //$NON-NLS-1$
    
    /**
     * Name attribute.  Value <code>name</code>.
     */
    public static final String ATT_NAME = "name"; //$NON-NLS-1$
    
    /**
     * Icon attribute.  Value <code>icon</code>.
     */
    public static final String ATT_ICON = "icon"; //$NON-NLS-1$
    
    /**
     * Value attribute.  Value <code>value</code>.
     */
    public static final String ATT_VALUE = "value"; //$NON-NLS-1$

	/**
	 * Class attribute.  Value <code>class</code>.
	 */
	public static final String ATT_CLASS = "class"; //$NON-NLS-1$
    
    /**
     * Accelerator attribute.  Value <code>accelerator</code>.
     */
    public static final String ATT_ACCELERATOR = "accelerator"; //$NON-NLS-1$
	
    /**
     * Perspective default attribute.  Value <code>default</code>.
     */
    public static final String ATT_DEFAULT = "default";//$NON-NLS-1$

	/**
	 * Description element.  Value <code>description</code>.
	 */
    public static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	
	/**
	 * Product id attribute.  Value <code>productId</code>.
	 */
	public static final String ATT_PRODUCTID = "productId"; //$NON-NLS-1$
    
    /**
     * Category tag.  Value <code>category</code>.
     */
    public static final String TAG_CATEGORY = "category";//$NON-NLS-1$
    
    /**
     * Target attribute.  Value <code>targetID</code>.
     */
    public static final String ATT_TARGET_ID = "targetID";//$NON-NLS-1$
    
    /**
     * Category id attribute.  Value <code>categoryId</code>.
     */
    public static final String ATT_CATEGORY_ID = "categoryId"; //$NON-NLS-1$
	
/* ***** org.eclipse.ui.activitySupport constants ***** */
	
	/**
	 * Category image binding tag.  Value <code>categoryImageBinding</code>.
	 */
	public static final String TAG_CATEGORY_IMAGE_BINDING = "categoryImageBinding"; //$NON-NLS-1$

	/**
	 * Activity image binding tag.  Value <code>activityImageBindingw</code>.
	 */
	public static final String TAG_ACTIVITY_IMAGE_BINDING = "activityImageBinding"; //$NON-NLS-1$
	
	/**
	 * Advisor tag.  Value <code>triggerPointAdvisor</code>.
	 */
	public static final String TAG_TRIGGERPOINTADVISOR = "triggerPointAdvisor"; //$NON-NLS-1$

	/**
	 * Advisor id attribute.  Value <code>triggerPointAdvisorId</code>.
	 */
	public static final String ATT_ADVISORID = "triggerPointAdvisorId"; //$NON-NLS-1$
	
	/**
	 * Advisor to product binding element.  Value <code>triggerPointAdvisorProductBinding</code>. 
	 */
	public static final String TAG_ADVISORPRODUCTBINDING = "triggerPointAdvisorProductBinding"; //$NON-NLS-1$
    
    /**
     * Trigger point tag.  Value <code>triggerPoint</code>.
     */
    public static final String TAG_TRIGGERPOINT = "triggerPoint"; //$NON-NLS-1$
    
    /**
     * Trigger point hint tag.  Value <code>hint</code>.
     */
    public static final String TAG_HINT = "hint"; //$NON-NLS-1$


/* ***** org.eclipse.ui.views constants ***** */
    
    /**
     * View tag.  Value <code>view</code>.
     */
    public static final String TAG_VIEW = "view";//$NON-NLS-1$

    /**
     * Sticky view tag.  Value <code>stickyView</code>.
     */
    public static final String TAG_STICKYVIEW = "stickyView";//$NON-NLS-1$

    /**
     * Sticky view location attribute.  Value <code>location</code>.
     */
    public static final String ATT_LOCATION = "location"; //$NON-NLS-1$

    /**
     * Sticky view closable attribute.  Value <code>closable</code>.
     */
    public static final String ATT_CLOSEABLE = "closeable"; //$NON-NLS-1$    

    /**
     * Sticky view moveable attribute.  Value <code>moveable</code>.
     */
    public static final String ATT_MOVEABLE = "moveable"; //$NON-NLS-1$

    /**
     * View ratio attribute.  Value <code>ratio</code>.
     */
    public static final String ATT_RATIO = "ratio"; //$NON-NLS-1$

    /**
     * View ratio attribute.  Value <code>fastViewWidthRatio</code>.
     */
    public static final String ATT_FAST_VIEW_WIDTH_RATIO = "fastViewWidthRatio"; //$NON-NLS-1$

    /**
     * View multiple attribute.  Value <code>allowMultiple</code>.
     */
    public static final String ATT_MULTIPLE = "allowMultiple"; //$NON-NLS-1$

    /**
     * View parent category attribute.  Value <code>parentCategory</code>.
     */
    public static final String ATT_PARENT = "parentCategory"; //$NON-NLS-1$
    
/* ***** org.eclipse.ui.perspectives constants ***** */

    /**
     * Perspective singleton attribute.  Value <code>singleton</code>.
     */
    public static final String ATT_SINGLETON = "singleton";//$NON-NLS-1$

    /**
     * Perspective fixed attribute.  Value <code>fixed</code>.
     */
    public static final String ATT_FIXED = "fixed";//$NON-NLS-1$
    
    /**
     * Perspective tag.  Value <code>perspective</code>.
     */
    public static final String TAG_PERSPECTIVE = "perspective";//$NON-NLS-1$

/* ***** org.eclipse.ui.editors constants ***** */
    
    /**
     * Editor tag.  Value <code>editor</code>.
     */
    public static final String TAG_EDITOR = "editor";//$NON-NLS-1$
    
    /**
     * Editor contributor class attribute.  Value <code>contributorClass</code>.
     */
    public static final String ATT_EDITOR_CONTRIBUTOR = "contributorClass"; //$NON-NLS-1$

    /**
     * Editor command attribute.  Value <code>command</code>.
     */
    public static final String ATT_COMMAND = "command";//$NON-NLS-1$

    /**
     * Editor launcher attribute.  Value <code>launcher</code>.
     */
    public static final String ATT_LAUNCHER = "launcher";//$NON-NLS-1$

    /**
     * Editor extensions attribute.  Value <code>extensions</code>.
     */
    public static final String ATT_EXTENSIONS = "extensions";//$NON-NLS-1$

    /**
     * Editor filenames attribute.  Value <code>filenames</code>.
     */
    public static final String ATT_FILENAMES = "filenames";//$NON-NLS-1$
	
	/**
	 * Editor content type binding tag.  Value <code>contentTypeBinding</code>.
	 */
	public static final String TAG_CONTENT_TYPE_BINDING = "contentTypeBinding"; //$NON-NLS-1$

	/**
	 * Editor content type id binding attribute.  Value <code>contentTypeId</code>.
	 */
	public static final String ATT_CONTENT_TYPE_ID = "contentTypeId"; //$NON-NLS-1$
    
    /**
     * Editor management strategy attribute.  Value <code>matchingStrategy</code>.
     */
    public static final String ATT_MATCHING_STRATEGY = "matchingStrategy"; //$NON-NLS-1$
    
/* ***** org.eclipse.ui.browserSupport constants ***** */
    
    /**
     * Browser support tag.  Value <code>support</code>.
     */
    public static final String TAG_SUPPORT = "support"; //$NON-NLS-1$
    
    
/* ***** org.eclipse.ui.workingSets constants ***** */
    
    /**
     * Working set tag.  Value <code>workingSet</code>.
     */
    public static final String TAG_WORKING_SET = "workingSet"; //$NON-NLS-1$
    
/* ***** org.eclipse.ui.newWizards, importWizards, exportWizards constants ***** */ 
    
    /**
     * Wizard tag.  Value <code>wizard</code>.
     */
    public final static String TAG_WIZARD = "wizard";//$NON-NLS-1$

    /**
     * Help url attribute.  Value <code>helpHref</code>.
     */
    public final static String ATT_HELP_HREF = "helpHref"; //$NON-NLS-1$

    /**
     * Description image attribute.  Value <code>descriptionImage</code>.
     */
    public final static String ATT_DESCRIPTION_IMAGE = "descriptionImage"; //$NON-NLS-1$

    /**
     * Project attribute.  Value <code>project</code>.
     */
    // @issue project-specific attribute and behavior
    public final static String ATT_PROJECT = "project";//$NON-NLS-1$
    
    /**
     * Primary wizard tag.  Value <code>primaryWizard</code>.
     */
    public final static String TAG_PRIMARYWIZARD = "primaryWizard"; //$NON-NLS-1$
    
    /**
     * Attribute that specifies whether a wizard is immediately capable of finishing.  Value <code>canFinishEarly</code>.
     */
    public final static String ATT_CAN_FINISH_EARLY = "canFinishEarly"; //$NON-NLS-1$
    
    /**
     * Attribute that specifies whether a wizard has any pages.  Value <code>hasPages</code>.
     */
    public final static String ATT_HAS_PAGES = "hasPages"; //$NON-NLS-1$
    
/* ***** org.eclipse.ui.actionSetPartAssociations ***** */    

    /**
     * Part association tag.  Value <code>actionSetPartAssociation</code>.
     */
    public static final String TAG_ACTION_SET_ASSOCIATION = "actionSetPartAssociation";//$NON-NLS-1$

    /**
     * Part tag.  Value <code>part</code>.
     */
    public static final String TAG_PART = "part";//$NON-NLS-1$

/* ***** org.eclipse.ui.actionSets ***** */    
    
    /**
     * Action set tag.  Value <code>actionSet</code>.
     */
    public static final String TAG_ACTION_SET = "actionSet";//$NON-NLS-1$
    
/* ***** org.eclipse.ui.decorators ***** */    

    /**
     * Lightweight decorator tag.  Value <code>lightweight</code>.
     */
    public static final String ATT_LIGHTWEIGHT = "lightweight"; //$NON-NLS-1$
    
/* ***** org.eclipse.ui.popupMenus ***** */        

    /**
     * Object class attribute.  Value <code>objectClass</code>.
     */
    public final static String ATT_OBJECTCLASS = "objectClass";//$NON-NLS-1$

    /**
     * Object contribution tag.  Value <code>objectContribution</code>.
     */
    public final static String TAG_OBJECT_CONTRIBUTION = "objectContribution";//$NON-NLS-1$
    
    /**
     * Viewer contribution tag.  Value <code>viewerContribution</code>.
     */
    public static final String TAG_CONTRIBUTION_TYPE = "viewerContribution"; //$NON-NLS-1$

/* ***** org.eclipse.ui.perspectiveExtensions ***** */
    
    /**
     * Perspective extension tag.  Value <code>perspectiveExtension</code>.
     */
    public static final String TAG_EXTENSION = "perspectiveExtension";//$NON-NLS-1$

    /**
     * Wizard shortcut tag.  Value <code>newWizardShortcut</code>.
     */
    public static final String TAG_WIZARD_SHORTCUT = "newWizardShortcut";//$NON-NLS-1$

    /**
     * View shortcut tag.  Value <code>viewShortcut</code>.
     */
    public static final String TAG_VIEW_SHORTCUT = "viewShortcut";//$NON-NLS-1$

    /**
     * Perspective shortcut tag.  Value <code>perspectiveShortcut</code>.
     */
    public static final String TAG_PERSP_SHORTCUT = "perspectiveShortcut";//$NON-NLS-1$

    /**
     * Show in part tag.  Value <code>showInPart</code>.
     */
    public static final String TAG_SHOW_IN_PART = "showInPart";//$NON-NLS-1$

    /**
     * Relative attribute.  Value <code>relative</code>.
     */
    public static final String ATT_RELATIVE = "relative";//$NON-NLS-1$

    /**
     * Relationship attribute.  Value <code>relationship</code>.
     */
    public static final String ATT_RELATIONSHIP = "relationship";//$NON-NLS-1$


    /**
     * Visible attribute.  Value <code>visible</code>.
     */
    // ATT_VISIBLE added by dan_rubel@instantiations.com  
    public static final String ATT_VISIBLE = "visible";//$NON-NLS-1$

    /**
     * Standalone attribute.  Value <code>standalone</code>.
     */
    public static final String ATT_STANDALONE = "standalone";//$NON-NLS-1$

    /**
     * Show title attribute.  Value <code>showTitle</code>.
     */
    public static final String ATT_SHOW_TITLE = "showTitle";//$NON-NLS-1$
    
/* ***** Shared action constants ***** */    

    /**
     * Menu tag.  Value <code>menu</code>.
     */
    public static final String TAG_MENU = "menu"; //$NON-NLS-1$

    /**
     * Action tag.  Value <code>action</code>.
     */
    public static final String TAG_ACTION = "action"; //$NON-NLS-1$

    /**
     * Separator tag.  Value <code>separator</code>.
     */
    public static final String TAG_SEPARATOR = "separator"; //$NON-NLS-1$

    /**
     * Group marker tag.  Value <code>groupMarker</code>.
     */
    public static final String TAG_GROUP_MARKER = "groupMarker"; //$NON-NLS-1$

    /**
     * Filter tag.  Value <code>filter</code>.
     */
    public static final String TAG_FILTER = "filter"; //$NON-NLS-1$

    /**
     * Visibility tag.  Value <code>visibility</code>.
     */
    public static final String TAG_VISIBILITY = "visibility"; //$NON-NLS-1$

    /**
     * Enablement tag.  Value <code>enablement</code>.
     */
    public static final String TAG_ENABLEMENT = "enablement"; //$NON-NLS-1$

    /**
     * Selectiont tag.  Value <code>selection</code>.
     */
    public static final String TAG_SELECTION = "selection"; //$NON-NLS-1$

    /**
     * Label attribute.  Value <code>label</code>.
     */
    public static final String ATT_LABEL = "label"; //$NON-NLS-1$

    /**
     * Enables-for attribute.  Value <code>enablesFor</code>.
     */
    public static final String ATT_ENABLES_FOR = "enablesFor"; //$NON-NLS-1$

    /**
     * Path attribute.  Value <code>path</code>.
     */
    public static final String ATT_PATH = "path"; //$NON-NLS-1$
    
    /**
     * Adaptable attribute.  Value <code>adaptable</code>.
     */
    public static final String ATT_ADAPTABLE = "adaptable"; //$NON-NLS-1$
    
    /**
     * Name filter attribute.  Value <code>nameFilter</code>.
     */
    public static final String ATT_NAME_FILTER = "nameFilter"; //$NON-NLS-1$
    
/* ***** org.eclipse.ui.preferenceTransfer ***** */    

    /**
     * Transfer tag.  Value <code>transfer</code>.
     */
    public final static String TAG_TRANSFER = "transfer";//$NON-NLS-1$

    /**
     * Mapping tag.  Value <code>mapping</code>.
     */
    public static final String TAG_MAPPING = "mapping"; //$NON-NLS-1$

    /**
     * Entry tag.  Value <code>entry</code>.
     */
    public static final String TAG_ENTRY = "entry"; //$NON-NLS-1$

    /**
     * Scope attribute.  Value <code>scope</code>.
     */
    public static final String ATT_SCOPE = "scope"; //$NON-NLS-1$

    /**
     * Node attribute.  Value <code>node</code>.
     */
    public static final String ATT_NODE = "node"; //$NON-NLS-1$

    /**
     * Keys attribute.  Value <code>keys</code>.
     */
    public static final String ATT_KEY = "key"; //$NON-NLS-1$
    
/* ***** org.eclipse.ui.themes constants ***** */    

    /**
     * Presentation id attribute.  Value <code>presentationId</code>.
     */
    public static final String ATT_PRESENTATIONID = "presentationId"; //$NON-NLS-1$

    /**
     * Defaults-to attribute.  Value <code>defaultsTo</code>.
     */
    public static final String ATT_DEFAULTS_TO = "defaultsTo"; //$NON-NLS-1$

    /**
     * Parent id attribute.  Value <code>parentId</code>.
     */
    public static final String ATT_PARENT_ID = "parentId"; //$NON-NLS-1$

    /**
     * Is-editable attribute.  Value <code>isEditable</code>.
     */
    public static final String ATT_IS_EDITABLE = "isEditable"; //$NON-NLS-1$

    /**
     * Operating system attribute.  Value <code>os</code>.
     */
    public static final String ATT_OS = "os"; //$NON-NLS-1$

    /**
     * Windowing system attribute.  Value <code>ws</code>.
     */
    public static final String ATT_WS = "ws"; //$NON-NLS-1$

    /**
     * Color factory attribute.  Value <code>colorFactory</code>.
     */
    public static final String ATT_COLORFACTORY = "colorFactory"; //$NON-NLS-1$

    /**
     * Category presentation tag.  Value <code>categoryPresentationBinding</code>.
     */
    public static final String TAG_CATEGORYPRESENTATIONBINDING = "categoryPresentationBinding"; //$NON-NLS-1$

    /**
     * Element category tag.  Value <code>themeElementCategory</code>.
     */
    public static final String TAG_CATEGORYDEFINITION = "themeElementCategory"; //$NON-NLS-1$

    /**
     * Color definition tag.  Value <code>colorDefinition</code>.
     */
    public static final String TAG_COLORDEFINITION = "colorDefinition"; //$NON-NLS-1$

    /**
     * Color override tag.  Value <code>colorOverride</code>.
     */
    public static final String TAG_COLOROVERRIDE = "colorOverride"; //$NON-NLS-1$    

    /**
     * Color value tag.  Value <code>colorValue</code>.
     */
    public static final String TAG_COLORVALUE = "colorValue"; //$NON-NLS-1$

    /***
     * Font definition tag.  Value <code>fontDefinition</code>.
     */
    public static final String TAG_FONTDEFINITION = "fontDefinition"; //$NON-NLS-1$

    /**
     * Font override tag.  Value <code>fontOverride</code>.
     */
    public static final String TAG_FONTOVERRIDE = "fontOverride"; //$NON-NLS-1$

    /**
     * Font value tag.  Value <code>fontValue</code>.
     */
    public static final String TAG_FONTVALUE = "fontValue"; //$NON-NLS-1$

    /**
     * Data tag.  Value <code>data</code>.
     */
    public static final String TAG_DATA = "data"; //$NON-NLS-1$

    /**
     * Theme tag.  Value <code>theme</code>.
     */
    public static final String TAG_THEME = "theme";//$NON-NLS-1$
    
/* ***** org.eclipse.ui.actionSets constants ***** */   

    /**
     * Action definition id attribute.  Value <code>definitionId</code>.
     */
    public static final String ATT_DEFINITION_ID = "definitionId";//$NON-NLS-1$

    /**
     * Help context id attribute.  Value <code>helpContextId</code>.
     */
    public static final String ATT_HELP_CONTEXT_ID = "helpContextId";//$NON-NLS-1$

    /**
     * Action style attribute.  Value <code>style</code>.
     */
    public static final String ATT_STYLE = "style";//$NON-NLS-1$

    /**
     * Action state attribute.  Value <code>state</code>.
     */
    public static final String ATT_STATE = "state";//$NON-NLS-1$

    /**
     * Tooltip attribute.  Value <code>tooltip</code>.
     */
    public static final String ATT_TOOLTIP = "tooltip";//$NON-NLS-1$

    /**
     * Menubar path attribute.  Value <code>menubarPath</code>.
     */
    public static final String ATT_MENUBAR_PATH = "menubarPath";//$NON-NLS-1$

    /**
     * Toolbar path attribute.  Value <code>toolbarPath</code>.
     */
    public static final String ATT_TOOLBAR_PATH = "toolbarPath";//$NON-NLS-1$

    /**
     * Hover icon attribute.  Value <code>hoverIcon</code>.
     */
    public static final String ATT_HOVERICON = "hoverIcon";//$NON-NLS-1$

    /**
     * Disabled icon attribute.  Value <code>disabledIcon</code>.
     */
    public static final String ATT_DISABLEDICON = "disabledIcon";//$NON-NLS-1$

    /**
     * Retarget attribute.  Value <code>retarget</code>.
     */
    public static final String ATT_RETARGET = "retarget";//$NON-NLS-1$

    /**
     * Allow label update attribute.  Value <code>allowLabelUpdate</code>.
     */
    public static final String ATT_ALLOW_LABEL_UPDATE = "allowLabelUpdate";//$NON-NLS-1$
}
