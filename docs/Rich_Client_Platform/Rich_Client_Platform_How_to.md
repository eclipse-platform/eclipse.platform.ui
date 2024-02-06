Rich Client Platform/How-to
===========================

This page is a checklist to get started in building an Eclipse RCP application. It is intended to keep as minimal as possible.

This was tested on Eclipse 3.5 (Galileo).

Contents
--------

*   [1 IDE](#IDE)
*   [2 Target](#Target)
*   [3 Plug-in](#Plug-in)
*   [4 View](#View)
*   [5 Perspective](#Perspective)
*   [6 Editor](#Editor)
*   [7 Debug](#Debug)
*   [8 Command](#Command)
*   [9 Menu](#Menu)
*   [10 Popup menu](#Popup-menu)
*   [11 Toolbar](#Toolbar)
*   [12 View Toolbar](#View-Toolbar)
*   [13 Key Binding](#Key-Binding)
*   [14 Product](#Product)
*   [15 Feature](#Feature)
*   [16 Localization](#Localization)
    *   [16.1 plugin.xml](#plugin.xml)
    *   [16.2 RCP](#RCP)
    *   [16.3 Strings](#Strings)
*   [17 Preferences](#Preferences)
*   [18 Update](#Update)
*   [19 Help](#Help)
*   [20 Build](#Build)
*   [21 Customization](#Customization)
*   [22 Status Line](#Status-Line)
*   [23 Extension Point](#Extension-Point)
*   [24 Progress](#Progress)

IDE
---

[Eclipse Download site](http://www.eclipse.org/downloads/)

Link "Eclipse for RCP/Plug-in Developers"

Download and unzip: eclipse-rcp-galileo-*.zip

Launch Eclipse

Open PDE (Plug-in Development Environment) perspective: Window > Open Perspective > Other > Plug-in Development

Target
------

Target Platform is used to run the applications/plugins you build with Eclipse RCP. 
Although it is possible to run your applications/plugins using your Eclipse IDE installation, this is not recommended.

Create directory _target_ in a folder of your choice.

[The Eclipse Project Downloads](http://download.eclipse.org/eclipse/downloads)

Download and unzip in _target_: eclipse-RCP-SDK-*.zip, eclipse-*-delta-pack.zip

*   Delta pack is needed for building to other platforms, or for automated [build](#Build).
*   If Help or Update feature is needed, either replace RCP-SDK with platform-SDK, or download individual features from [here](http://www.eclipse.org/platform/)
*   eclipse-RCP-SDK-*.zip is needed rather than eclipse-RCP-*.zip else extensions won't work in plug-in editor.

Window > Preferences > Plug-in Development > Target Platform > Browse > _target_/eclipse > OK > Reload

Plug-in
-------

File > New > Project > Plug-in Project > Next > Project name > Next > Rich Client Application > Yes > Next > Hello RCP > Next > Add branding > Finish

plugin.xml > Overview > Testing > Launch

View
----

plugin.xml > Extensions > Add > org.eclipse.ui.views > right-click > New > View > id, class

Perspective
-----------

plugin.xml > Extensions > org.eclipse.ui.perspectives > perspective > class

     public void createInitialLayout (IPageLayout layout) {
       layout.addView ("view_id", IPageLayout.LEFT, 0.5f, layout.getEditorArea ());
       layout.getViewLayout ("view_id").setCloseable (false);
     }
    
WorkbenchWindowAdvisor.preWindowOpen:

     configurer.setShowPerspectiveBar (true);
     configurer.setShowCoolBar (true);

Editor
------

plugin.xml > Extensions > Add > org.eclipse.ui.editors > id, class, icon

To open editor:

     IWorkbenchPage page;
     IEditorInput input;
     page = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();
     // else if in ViewPart // page = getSite ().getPage ();
     page.openEditor (input, "editor_id");
    
EditorPart.init:

     setSite (site);
     setInput (input);

Debug
-----

In case something goes wrong:

Run > Run Configurations > Main > Clear workspace

Run > Run Configurations > Arguments > Program arguments > -consoleLog

Run > Run Configurations > Plug-ins > Validate plug-ins automatically

Run > Run Configurations > Plug-ins > Add Required Plug-ins

Command
-------

plugin.xml > Extensions > org.eclipse.ui.commands > right-click > New > command > defaultHandler > Superclass > org.eclipse.core.commands.AbstractHandler

Menu
----

plugin.xml > Extensions > org.eclipse.ui.menus > right-click > New > menuContribution > locationURI = menu:org.eclipse.ui.main.menu > right-click > New > menu > right-click > New > command > commandId > Browse

Popup menu
----------

plugin.xml > Extensions > org.eclipse.ui.menus > right-click > New > menuContribution > locationURI = popup:_view_id_ \> right-click > New > command > commandId > Browse

ViewPart.createPartControl:

     Viewer viewer;
     MenuManager menuManager = new MenuManager ();
     Menu menu = menuManager.createContextMenu (viewer.getControl ());
     viewer.getControl ().setMenu (menu);
     getSite ().registerContextMenu (menuManager, viewer);
    
Toolbar
-------

plugin.xml > Extensions > org.eclipse.ui.menus > right-click > New > menuContribution > locationURI = toolbar:org.eclipse.ui.main.toolbar > right-click > New > toolbar > right-click > New > command > commandId > Browse

WorkbenchWindowAdvisor.preWindowOpen:

     configurer.setShowCoolBar (true);

View Toolbar
------------

plugin.xml > Extensions > org.eclipse.ui.menus > right-click > New > menuContribution > locationURI = toolbar:_view_id_ \> right-click > New > command > commandId > Browse

Key Binding
-----------

plugin.xml > Extensions > org.eclipse.ui.commands > right-click > New > keyBinding

*   keyConfigurationId = org.eclipse.ui.defaultAcceleratorConfiguration
*   commandId (there is no Browse button)
*   keySequence = M1+A (this is Ctrl-A)

Product
-------

Creating a product is required for branding and automatic [build](#Build).

File > New > Product Configuration > File name > _product_name_.product > Initialize the file content:

*   Use an existing product (_plugin_id_.product) if applying (if "Add branding" was checked)
*   Use a launch configuration (_plugin_id_.application) if applying (if application was launched)

If no existing product: _product_name_.product > Overview > Specify the product identifier > New > Defining plug-in  
_product_name_.product > Overview > Testing > Synchronize (updates plugin.xml)  
_product_name_.product > Overview > Testing > Launch  
_product_name_.product > Overview > Eclipse Product export wizard

Feature
-------

Creating a feature is required for [update](#Update).

File > New > Project > Feature Project > Next > Project name > _feature_id_  
feature.xml > Plug-ins > Add > _plugin_id_

_product_name_.product > Overview > The product configuration is based on > Features  
_product_name_.product > Configuration > Add > org.eclipse.rcp  
_product_name_.product > Configuration > Add > _feature_id_

Localization
------------

### plugin.xml

plugin.xml > Overview > Exporting > Externalize Strings Wizard

plugin.properties:

     key=Default value

Create for each language _xx_ (ISO 639 language code) a file plugin__xx_.properties

plugin__xx_.properties:

     key=Translated value
    
Add this line in MANIFEST.MF:

     Bundle-Localization: plugin

Alternately (tested under Helios 3.6), you can run the Externalize Strings Wizard on plugin.xml (and any source files that need it too), then run PDE Tools: Internationalize on the project. This creates a project fragment (of the same name with ".nl1" suffixed) that contains the to-be-localized files.

Move the various .properties files from that project fragment to the source project (e.g. move "myproject.nl1/plugin\_fr.properties" to "myproject/plugin\_fr.properties", and likewise for any .properties files in subfolders such as OSGI-INF or src). You should then have essentially nothing left in the project fragment, so you can delete it.

Now install the Resource Bundle Editor (RBE) plug-in (from [here](http://sourceforge.net/projects/eclipse-rbe/)) by unzipping its plugins directory into your eclipse/plugins directory (restart Eclipse once this is done). Go to Window: Preferences: General: Editors: File Associations, and assign RBE as the default editor for .properties files.

You will now be able to open the .properties files that need localization and edit them accordingly. By keeping matching .properties files co-located, RBE is able to display the original (English) and target language string values side by side, which makes translation less of a chore. RBE is being integrated into Eclipse's Babel project, so it will eventually no longer be necessary to move the .properties files as described earlier: you'll be able to edit them right in the project fragments. Project fragments are nice if you do not want to ship your project with all the various localizations in it (strings, icons, images, help files, etc.), but would rather ship each localization separately.

If you do not use RBE, be aware that .properties files _must_ use ISO 8859-1 character encoding, which makes some Unicode strings painful to deal with; RBE handles this conversion transparently for you.

### RCP

[Download Eclipse](http://download.eclipse.org/technology/babel/babel_language_packs/)

Unzip BabelLanguagePack-eclipse-*.zip into _target_

Window > Preferences > Plug-in Development > Target Platform > Reload

Run > Run Configurations > Plug-ins > Add Required Plug-ins

### Strings

Source > Externalize Strings > Use Eclipse's string externalization

Preferences
-----------

If no need for preference scopes

See [User Settings FAQ](https://eclipse.dev/eclipse/platform-core/documents/user_settings/faq.html)

Get preference value:

     Activator.getDefault ().getPluginPreferences ().getString ("preference_id");
    
Preference page: plugin.xml > Extensions > Add > org.eclipse.ui.preferencePages > right-click > New > page > class > Superclass > FieldEditorPreferencePage

FieldEditorPreferencePage:

     protected IPreferenceStore doGetPreferenceStore () {
       return Activator.getDefault ().getPreferenceStore ();
     }
     protected void createFieldEditors () {
       addField (new StringFieldEditor ("preference_id", "label", getFieldEditorParent ()));
     }
    

Command org.eclipse.ui.window.preferences

Update
------

See also [feature](#Feature)

File > New > Project > Update Site Project

site.xml > Archives > URL, Description

site.xml > Site Map > Add Feature > _feature_id_

site.xml > Site Map > Build All

Copy to URL: site.xml, plugins, features

feature.xml > Overview > Update Site URL

feature.xml > Included Features > Add > org.eclipse.rcp

feature.xml > Plug-ins > Add > org.eclipse.core.net, org.eclipse.equinox.security, org.eclipse.ui.forms, org.eclipse.update.core, org.eclipse.update.ui

plugin.xml > Dependencies > Add > org.eclipse.update.core, org.eclipse.update.ui

Run > Run Configurations > Plug-ins > Add Required Plug-ins

Add command IHandler

IHandler.execute:

     BusyIndicator.showWhile (HandlerUtil.getActiveShell (event).getDisplay (),
     new Runnable () {
       public void run () {
         String label = Platform.getResourceBundle (Platform.getBundle (
         "org.eclipse.update.ui")).getString ("actionSets.softwareUpdates.label");
         UpdateManagerUI.openInstaller (HandlerUtil.getActiveShell (event),
         new UpdateJob (label, false, false));
       }
     });
    
WorkbenchWindowAdvisor.preWindowOpen:

     configurer.setShowProgressIndicator (true);

Test with exported product, not with product launched from IDE

Help
----

Add command org.eclipse.ui.help.helpContents

plugin.xml > Dependencies > Add > org.eclipse.help.ui, org.eclipse.help.webapp

plugin.xml > Extensions > Add > org.eclipse.help.toc > Available templates > Help Content

_product_name_.product > Configuration > Add > org.eclipse.help

build.properties > Binary build > check html, icons, toc.xml, etc.

ActionBarAdvisor.makeActions:

     IAction helpAction = ActionFactory.HELP_CONTENTS.create (window);
     register (helpAction);
    

Build
-----

Automatic (headless) build

File > New > Project > General > Project

Copy build.properties and customTargets.xml from: eclipse/plugins/org.eclipse.pde.build_*/templates/headless-build

build.properties (edit with properties file editor):

     product=/plugin_id/product_name.product
     archivePrefix
     configs=win32,win32,x86 & linux,gtk,x86 (*,*,* does not work)
     buildDirectory=${builder}/build
     base=path_to_target
    

customTargets.xml:

     <target name="preSetup">
       <copy todir="${buildDirectory}/plugins/plugin_id">
         <fileset dir="${builder}/path_to_plugin" />
       </copy>
       <copy todir="${buildDirectory}/features/feature_id">
         <fileset dir="${builder}/path_to_feature" />
       </copy>
     </target>
    

_product_name_.product > Program Launcher > Launcher Name

java -jar eclipse/plugins/org.eclipse.equinox.launcher_*.jar -application org.eclipse.ant.core.antRunner -buildfile eclipse/plugins/org.eclipse.pde.build_*/scripts/productBuild/productBuild.xml

Customization
-------------

Create file plugin_customization.ini in plug-in

plugin_customization.ini:

     org.eclipse.ui/DOCK_PERSPECTIVE_BAR=topRight
     org.eclipse.ui/SHOW_TRADITIONAL_STYLE_TABS=false
    
Status Line
-----------

WorkbenchWindowAdvisor.preWindowOpen:

     getWindowConfigurer ().setShowStatusLine (true);
 

ViewPart:

     getViewSite ().getActionBars ().getStatusLineManager ().setMessage ("message");
 

Extension Point
---------------

Plug-in with extension point (_plugin_id_):

plugin.xml > Extension Points > Add > ID=_point_id_, Name

_point_id_.exsd > Definition > New Element > Name=_element_name_

_element_name_ \> New Attribute > Name=_class_name_, Type=java, Implements=_package_name_._InterfaceName_ \> Implements

_point_id_.exsd > Definition > extension > New Choice > right-click > New > _element_name_

plugin.xml > Runtime > Exported Packages > Add > _package_name_

Activator.start (for example):

     IConfigurationElement[] elements = Platform.getExtensionRegistry ().getConfigurationElementsFor ("plugin_id.point_id");
     for (IConfigurationElement e : elements) {
       Object o = e.createExecutableExtension ("class_name");
       if (o instanceof InterfaceName) {
       }
     }
    

Plug-in with extension:

MANIFEST.MF > Dependencies > Required Plug-ins > _plugin with extension point_

MANIFEST.MF > Extensions > Add > _plugin\_id.point\_id_ \> right-click > New > _element_name_ \> _class_name_

Add plug-in with extension to run configuration.

Progress
--------

     Job job = new Job ("Job name") {
       public IStatus run (IProgressMonitor monitor) {
         final int duration = 10;
         monitor.beginTask ("Task message", duration);
         try {
           for (int i = 0; i < duration; i++) {
             if (monitor.isCanceled ())
               return Status.CANCEL_STATUS;
             monitor.subTask ("Subtask message");
             // ...
             monitor.worked (1);
           }
         }
         finally {
           monitor.done ();
         }
         return Status.OK_STATUS;
       }
     }
     job.schedule ();
    

WorkbenchWindowAdvisor.preWindowOpen:

     IWorkbenchWindowConfigurer configurer = getWindowConfigurer ();
     configurer.setShowProgressIndicator (true);

