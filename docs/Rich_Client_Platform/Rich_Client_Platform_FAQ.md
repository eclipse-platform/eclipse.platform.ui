Rich Client Platform/FAQ
======================== 

The following are **Frequently Asked Questions** (**FAQs**) about the Eclipse Rich Client Platform. 
For relevant tutorials, help topics, newsgroups, examples, and other resources, see the main [RCP page](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform.md).

or general Eclipse FAQs, which address many RCP issues, see [The Official Eclipse FAQs](https://wiki.eclipse.org/The_Official_Eclipse_FAQs).  
For Eclipse 4, see the [Eclipse 4 RCP FAQ](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Eclipse4_RCP_FAQ.md).

Contents
--------

*   [1 What is the Eclipse Rich Client Platform?](#What-is-the-Eclipse-Rich-Client-Platform)
*   [2 Why should I build my application on the Eclipse Rich Client Platform?](#Why-should-I-build-my-application-on-the-Eclipse-Rich-Client-Platform)
*   [3 What is included in the Rich Client Platform?](#What-is-included-in-the-Rich-Client-Platform)
*   [4 What is the disk footprint for the Rich Client Platform?](#What-is-the-disk-footprint-for-the-Rich-Client-Platform)
*   [5 Is the resources plug-in (org.eclipse.core.resources) considered part of the Rich Client Platform?](#Is-the-resources-plug-in-.28org.eclipse.core.resources.29-considered-part-of-the-Rich-Client-Platform)
*   [6 Is the IDE plug-in (org.eclipse.ui.ide) considered part of the Rich Client Platform?](#Is-the-IDE-plug-in-.28org.eclipse.ui.ide.29-considered-part-of-the-Rich-Client-Platform)
*   [7 What other Eclipse components can be used in constructing RCP applications?](#What-other-Eclipse-components-can-be-used-in-constructing-RCP-applications)
*   [8 How do I get started with RCP?](#How-do-I-get-started-with-RCP)
*   [9 What is the recommended target platform setup? Or: How can I build and run my RCP app against a different version of the Eclipse base?](#What-is-the-recommended-target-platform-setup-Or-How-can-I-build-and-run-my-RCP-app-against-a-different-version-of-the-Eclipse-base)
*   [10 How can I change the window icon in my application?](#How-can-I-change-the-window-icon-in-my-application)
*   [11 How can I change the embedded app icon in my application?](#How-can-I-change-the-embedded-app-icon-in-my-application)
*   [12 How can I change the default UI settings for the perspective bar location, fast view bar location, etc?](#How-can-I-change-the-default-UI-settings-for-the-perspective-bar-location.2C-fast-view-bar-location.2C-etc)
*   [13 How can I get action set menus to appear in the right order, between my app's main menus?](#How-can-I-get-action-set-menus-to-appear-in-the-right-order.2C-between-my-app.27s-main-menus)
*   [14 Can multiple instances of the same view be made to appear at the same time?](#Can-multiple-instances-of-the-same-view-be-made-to-appear-at-the-same-time)
*   [15 How can I deploy my RCP app?](#How-can-I-deploy-my-RCP-app)
*   [16 When I try running, nothing happens, or it complains that the application could not be found in the registry, or that other plug-ins are missing. How can I track the problem down?](#When-I-try-running.2C-nothing-happens.2C-or-it-complains-that-the-application-could-not-be-found-in-the-registry.2C-or-that-other-plug-ins-are-missing.-How-can-I-track-the-problem-down)
*   [17 My own RCP plug-ins are contributed by a feature. Why is the update manager complaining that my configuration is invalid?](#My-own-RCP-plug-ins-are-contributed-by-a-feature.-Why-is-the-update-manager-complaining-that-my-configuration-is-invalid)
*   [18 Are editors tied to the workspace resource model or to the broader notion of files?](#Are-editors-tied-to-the-workspace-resource-model-or-to-the-broader-notion-of-files)
*   [19 How can I integrate my existing Swing components into an RCP application?](#How-can-I-integrate-my-existing-Swing-components-into-an-RCP-application)
*   [20 How can I define key bindings for commands?](#How-can-I-define-key-bindings-for-commands)
*   [21 How can I get my views and editors to coordinate with each other?](#How-can-I-get-my-views-and-editors-to-coordinate-with-each-other)
*   [22 Which plug-ins are needed for the Eclipse Help system?](#Which-plug-ins-are-needed-for-the-Eclipse-Help-system)
*   [23 How can I add the Eclipse Update Manager to my application?](#How-can-I-add-the-Eclipse-Update-Manager-to-my-application)
*   [24 What is ICU4J and is it required?](#What-is-ICU4J-and-is-it-required)
*   [25 How to bundle the JRE's for Windows and for Linux in my RCP application?](#How-to-bundle-the-JRE.27s-for-Windows-and-for-Linux-in-my-RCP-application)
*   [26 How to add menu item, command and handler?](#How-to-add-menu-item.2C-command-and-handler)
*   [27 How can I show line numbers by default in my RCP application?](#How-can-I-show-line-numbers-by-default-in-my-RCP-application)

What is the Eclipse Rich Client Platform?
-----------------------------------------

While the Eclipse platform is designed to serve as an open tools platform, it is architected so that its components could be used to build just about any client application. The minimal set of plug-ins needed to build a rich client application is collectively known as the **Rich Client Platform**.

For more details, see the main [RCP page](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform.md).

Why should I build my application on the Eclipse Rich Client Platform?
----------------------------------------------------------------------

Many people that have built, or are building, RCP applications state that the main value they get from using RCP is that it allows them to quickly build a professional-looking application, with native look-and-feel, on multiple platforms, allowing them to focus on their value-add. They appreciate that the components that form RCP are of high quality, are actively maintained, and are open source. They often discover after the initial adoption of RCP that there are many other Eclipse components available for reuse (e.g. Help UI, Update Manager, Cheat Sheets, Intro, etc.). Several have also discovered that the inherent extensibility of Eclipse allows them to build not only a closed-form product, but also an open-ended platform (like the Eclipse IDE) in their own domain.

For a nice description of the benefits of RCP, see [Jeff Norris' forward on NASA/JPL's use of RCP](http://web.archive.org/web/20100307050224/eclipsercp.org/book/chapters/RCP_Foreward2.pdf) (archived link), a free excerpt from the [RCP Book](/RCP_Book "RCP Book").

See also the case studies available on the [RCP Community page](https://www.eclipse.org/community/).

What is included in the Rich Client Platform?
---------------------------------------------

The Eclipse Rich Client Platform consists of the following components:

| Component    | Description    | Plug-ins    | Documentation    |
| --- | --- | --- | --- |
| Eclipse Runtime    | Provides the foundational support for plug-ins, extension points and extensions (among other facilities).  The Eclipse runtime is built on top of the [OSGi framework](https://www.osgi.org/resources/where-to-start/). | org.eclipse.core.runtime    org.eclipse.osgi   org.eclipse.osgi.services   | Dev guide: [Runtime overview](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/runtime.htm)   Article: [Notes on the Eclipse Plug-in Architecture](http://eclipse.org/articles/Article-Plug-in-architecture/plugin_architecture.html)      |
| SWT    | The Standard Widget Toolkit. SWT is designed to provide efficient, portable access to the user-interface facilities of the operating systems on which it is implemented | org.eclipse.swt    \+ platform-specific fragments      | [Platform SWT home page](https://www.eclipse.org/swt/) |
| JFace    | A UI framework, layered on top of SWT, for handling many common UI programming tasks. | org.eclipse.jface | [JFace](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/JFace.md) |
| Workbench | The Workbench builds on top of the Runtime, SWT and JFace to provide a highly scalable, open-ended, multi-window environment for managing views, editors, perspectives (task-oriented layouts), actions, wizards, preference pages, and more.    The Workbench is sometimes called the Generic Workbench, to distinguish it from the IDE Workbench facilities defined in the org.eclipse.ui.ide plug-in.   | org.eclipse.ui    org.eclipse.ui.workbench   | Dev guide: [Plugging into the workbench](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/workbench.htm),[Dialogs and wizards](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/dialogs.htm), [Advanced workbench concepts](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/wrkAdv.htm)      |
|   Other prerequisites for the Workbench   | Support for XML expressions language, commands, and help core content model.    | org.eclipse.core.expressions    org.eclipse.core.commands   org.eclipse.help   |     |

Note that as of Eclipse 3.3M6, org.eclipse.help requires [com.ibm.icu](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform/Rich_Client_Platform_FAQ.md#What-is-ICU4J-and-is-it-required) which takes a sizeable amount of footprint (that [can be reduced](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform/Rich_Client_Platform_FAQ.md#What-is-ICU4J-and-is-it-required)). [Bug 183761](https://bugs.eclipse.org/bugs/show_bug.cgi?id=183761) has been filed to investigate removing this dependency from org.eclipse.help.

What is the disk footprint for the Rich Client Platform?
--------------------------------------------------------

As of Eclipse 3.7, the disk footprint is about 20 Meg.

Is the resources plug-in (org.eclipse.core.resources) considered part of the Rich Client Platform?
--------------------------------------------------------------------------------------------------

No. The workspace resource model provided by the org.eclipse.core.resources plug-in is not considered part of the Rich Client Platform. While this is the underlying data model for the Eclipse IDE, the RCP makes no assumptions about the underlying data model of the application being built. The data model could just as well be files in the local filesystem, a remote database, an RDF data store, or anything else. If it makes sense for the application, **org.eclipse.core.resources** can be included and used as the application's data model, but this is not required. Much effort was put into Eclipse 3.0 to remove the dependencies on **org.eclipse.core.resources** from the generic workbench. Any resource dependencies (for example, the New Project, Folder and File wizards, and the Resource Navigator, Tasks and Problems views), were considered IDE-specific and factored out into the IDE plugin (**org.eclipse.ui.ide**).

  

Is the IDE plug-in (org.eclipse.ui.ide) considered part of the Rich Client Platform?
------------------------------------------------------------------------------------

No. The **org.eclipse.ui.ide** plug-in is layered on top of the generic workbench (**org.eclipse.ui**) and adds IDE-specific views, preference pages and other extensions. The IDE uses the workspace resource model as its underlying data model. :The org.eclipse.ui.ide plug-in, and the extensions defined within it, are not part of the Rich Client Platform, but they can be used in a resource- (and workspace-)based RCP application.

Prior to Eclipse 3.3, the **org.eclipse.ui.ide** was not designed to be reused in other RCP applications because it also defined the application for the Eclipse IDE, which instantiates the generic workbench, configuring it with IDE-specific menu and toolbar items. As of Eclipse 3.3, the application definition has been moved to a new plug-in **org.eclipse.ui.ide.application**.

What other Eclipse components can be used in constructing RCP applications?
---------------------------------------------------------------------------

Here is a list of some of the reusable components in the broader Eclipse codebase that can be incorporated into RCP applications.

| Component    | Description    | Plug-ins    | Documentation    |
| --- | --- | --- | --- |
| Help    | Web-app-based Help UI, with support for dynamic content.    | org.apache.lucene    org.eclipse.help.appserver   org.eclipse.help.base   org.eclipse.help.ui   org.eclipse.help.webapp   org.eclipse.tomcat   org.eclipse.ui.forms   | Dev guide: [Plugging in help](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/help.htm)      |
| Update Manager    | Allows users to discover and install updated versions of products and extensions. | org.eclipse.update.configurator    org.eclipse.update.core   org.eclipse.update.scheduler   org.eclipse.update.ui   \+ platform-specific fragments      | Dev guide: [Updating a product or extension](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/product_update.htm)      |
| Text    | Framework for building high-function text editors.    | org.eclipse.text    org.eclipse.jface.text   org.eclipse.workbench.texteditor   | Dev guide: [Text editors and platform text](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/editors_jface.htm)      |
| Forms    | Flat look control library and multi-page editor framework (used in PDE editors).    | org.eclipse.ui.forms | Article: [Eclipse Forms: Rich UI for the Rich Client](http://www.eclipse.org/articles/Article-Forms/article.html) |
| Welcome Page (aka Intro) | Initial welcome experience and guided assistance.    | org.eclipse.ui.intro    | Dev guide: [Intro support](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/workbench_advext_intro.htm) |
| Cheat Sheets    | A Cheat Sheet guides the user through a long-running, multi-step task.    | org.eclipse.ui.cheatsheets    | Dev guide: [Cheat Sheets](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/workbench_advext_cheatsheets.htm) |
| Resources    | Workspace resource model, with managed projects, folders and files.    | org.eclipse.core.resources    | [Platform Core home page](http://www.eclipse.org/eclipse/platform-core/)   Dev guide: [Resources overview](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/resInt.htm) |
| Console | Extensible console view.    | org.eclipse.ui.console    | Javadoc: [org.eclipse.ui.console](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/console/package-summary.html), [org.eclipse.ui.console.actions](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/console/actions/package-summary.html) |
| Outline and Properties views | Outline and Properties views    | org.eclipse.ui.views    | TBD |
| Graphical Editing Framework (GEF) | Framework for building graphical editors. Includes Draw2D, a vector graphics framework.    | org.eclipse.draw2d    org.eclipse.gef      | [GEF home page](http://www.eclipse.org/gef)      |
| Eclipse Modeling Framework (EMF) and Service Data Objects (SDO) | EMF is a modeling framework and code generation facility for building tools and other applications based on a structured data model.  SDO is a framework that simplifies and unifies data application development in a service oriented architecture (SOA). | [EMF plug-in list from CVS](http://dev.eclipse.org/viewcvs/indextools.cgi/org.eclipse.emf/plugins/)    | [EMF home page](http://www.eclipse.org/emf/)   Overviews:[EMF, EMF Edit, EMF Validation ...](http://www.eclipse.org/modeling/emf/docs/#overviews), [SDO](http://www-106.ibm.com/developerworks/java/library/j-sdo/) |

How do I get started with RCP?
------------------------------

The [Eclipse RCP tutorials](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform.md) are a good starting points. 
See also the [examples](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform.md#Examples) and the [suggested help topics](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform.md#Help-Topics).

What is the recommended target platform setup? Or: How can I build and run my RCP app against a different version of the Eclipse base?
--------------------------------------------------------------------------------------------------------------------------------------

With the default setup of the Eclipse SDK, plug-ins are developed against the same plug-in configuration used by the IDE itself. 
However, it is possible to configure the IDE to build, run and deploy against a different version of the eclipse base, via the Plug-in Development > Target Platform preference page. 
Configuring the target platform is highly recommended in order to avoid introducing unwanted dependencies on IDE plug-ins into your RCP app.

For more details, see the ["Target Platform Preferences"](http://help.eclipse.org/ganymede/topic/org.eclipse.pde.doc.user/guide/tools/preference_pages/target_platform.htm) help topic in the PDE Guide. 
The tutorial in the [RCP Book](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform/Rich_Client_Platform_Book.md) also walks you through this process.

For a recommended setup, use the following steps. We assume the Eclipse SDK (aka the IDE) is already installed, e.g. in c:\\eclipse.

1.  Go to the [Platform downloads page](http://download.eclipse.org/eclipse/downloads/).
2.  Pick the build you want to use as your RCP target (e.g. the 3.2 M6 milestone build).
3.  In the RCP SDK section (not the Eclipse SDK, Platform SDK, or RCP Binary sections) download the RCP SDK for your platform. This contains just the base RCP plug-ins for that platform.
4.  Extract it to a different directory than the IDE's (e.g. c:\\eclipse-RCP-SDK).
5.  Optionally, if you want to deploy to other platforms, download the Delta Pack (the link is at the bottom of the RCP SDK section). This contains the platform-specific plug-ins for all platforms. Extract it to the same location as the RCP SDK (say OK to any prompts to overwrite files).
6.  Run the IDE (e.g. c:\\eclipse\\eclipse.exe).
7.  Go to Window > Preferences > Plugin-Development > Target Platform, and configure the location to be the eclipse subdirectory of the RCP SDK install (e.g. c:\\eclipse-RCP-SDK\\eclipse). Note: if you type or paste the path, you'll need to press the Reload button.
8.  The list of plug-ins on the Plug-ins tab should update to show the RCP SDK plug-ins; other plug-ins like those for JDT and PDE should not appear.

Now, any plug-in projects in your workspace will build and run against the RCP SDK plug-ins rather than the IDE's.

If you are developing against the same version as the Eclipse SDK, there is new support in since 3.2 M5 that simplifies setting up the Target Platform for RCP development, and makes it easier to incrementally include other plug-ins from the SDK build.

1.  Go to Window > Preferences > Plugin-Development > Target Platform.
2.  In the "Pre-defined Targets" section at the bottom, choose one of the "Base RCP" entries, e.g. "Base RCP (with Source)" and press the "Load Target" button.
3.  The list of plug-ins on the Plug-ins tab still shows all SDK plug-ins, but only the RCP base plug-ins are checked; other plug-ins like those for JDT and PDE should not be checked. Only checked plug-ins constitute your target. Unchecked plug-ins are ignored.

PDE currently supports two versions back for plug-in development. That is, with Eclipse 3.2 you can develop 3.0 plugins and launch 3.0 systems, but not 2.1 or earlier.

How can I change the window icon in my application?
---------------------------------------------------

[Define a product](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/product_def.htm) via the [products extension point](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/product_def_extpt.htm) and specify the `windowImages` property to refer to two image files, a 16x16 one and a 32x32 one.

It is best to specify both, since a 16x16 icon is typically used in the window trim, and a 32x32 icon is typically used in the OS's application switcher (e.g. Alt+Tab on Windows). If only one is specified, it is scaled up or down as needed, which can result in poor quality.

For example, the [Browser Example](/RCP_Browser_Example "RCP Browser Example") has the following in its plugin.xml:

    <extension point="org.eclipse.core.runtime.products" id="product">
      <product
        name="%productName"
        application="org.eclipse.ui.examples.rcp.browser.app">
        <property
          name="windowImages"
          value="icons/eclipse.gif,icons/eclipse32.gif"/>
        ...
      </product>
    </extension>

For more details, see the [Branding Your Application](http://eclipse.org/articles/Article-Branding/branding-your-application.html) article.

How can I change the embedded app icon in my application?
---------------------------------------------------------

This can be customized via the product file which is used for the export.

How can I change the default UI settings for the perspective bar location, fast view bar location, etc?
-------------------------------------------------------------------------------------------------------

Several UI settings such as the perspective bar location, fast view bar location, traditional vs. curvy tabs, etc., are controlled by preferences on the UI plug-in. These have default values defined by the generic workbench. However, the product can override these default values using the product preference customization mechanism.

[Define a product](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/product_def.htm) via the [products extension point](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/product_def_extpt.htm) and add the following property:

    <property
      name="preferenceCustomization"
      value="plugin_customization.ini"/>

Then create a file called `plugin_customization.ini`, in the same directory as the `plugin.xml` file, with contents of the form:

    <pluginId>/<preferenceName>=<preferenceValue>

For example, to show the perspective bar and fast view bar on the left, and to use curvy tabs, add the following to the `plugin_customization.ini` file:

    org.eclipse.ui/DOCK_PERSPECTIVE_BAR=left
    org.eclipse.ui/SHOW_TEXT_ON_PERSPECTIVE_BAR=false
    org.eclipse.ui/initialFastViewBarLocation=left
    org.eclipse.ui/SHOW_TRADITIONAL_STYLE_TABS=false
    
For a list of public preferences available on the UI plug-in and their valid values, see the interface [org.eclipse.ui.IWorkbenchPreferenceConstants](http://help.eclipse.org/galileo/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/IWorkbenchPreferenceConstants.html).

For more details, see the [Branding Your Application](http://eclipse.org/articles/Article-Branding/branding-your-application.html) article and the [Customizing a product](http://help.eclipse.org/galileo/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/product_configproduct.htm) section in Help.

How can I get action set menus to appear in the right order, between my app's main menus?
-----------------------------------------------------------------------------------------

When adding main menus to the menu manager in your WorkbenchAdvisor's fillActionBars method, add an "additions" group marker where you'd like action sets to appear.

    menuBar.add(fileMenu);
    menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    menuBar.add(helpMenu);

Can multiple instances of the same view be made to appear at the same time?
---------------------------------------------------------------------------

Yes. See IWorkbenchPage.showView(String primaryId, String secondaryId, int mode).

The <view> element in the plugin.xml must also specify allowMultiple="true".

Be sure to use a different `secondaryId` for each instance, otherwise `showView` will find any existing view with the same primaryId and secondaryId rather than showing a new one.

To pass instance-specific data to the view, you will need to cast the resulting IViewPart down to the concrete view class and call your own setData method.

Note that views with a secondaryId will not match placeholders specifying just the primaryId. In a perspective factory, placeholders can be added for multi-instance views using the format `primaryId + ':' + secondaryId`, where '*' wildcards are supported.

How can I deploy my RCP app?
----------------------------

The currently best way to deploy your RCP app is to use [Tycho](https://github.com/eclipse-tycho/tycho). 
See [Tycho tutorial](https://www.vogella.com/tutorials/EclipseTycho/article.html) for an example.


When I try running, nothing happens, or it complains that the application could not be found in the registry, or that other plug-ins are missing. How can I track the problem down?
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Try running first from within Eclipse using the Runtime Workbench (3.0 and 3.0.1) or Eclipse Application (3.1) launch configuration (Run > Debug...). Ensure that the application's plug-in(s) and all its prerequisites are selected in the Plug-ins tab. The easiest way is to select "Choose plug-ins and fragments to launch from the list", press Deselect All, check off the application's plug-in(s), and press Add Required Plug-ins. In 3.1, there is also a Validate Plug-in Set button to check that all prerequisites have been satisfied, without having to launch first. On the Main tab, be sure that the correct product or application is selected (using a product is preferred -- see the [Branding Your Application](http://eclipse.org/articles/Article-Branding/branding-your-application.html) article).

When running a deployed RCP application (not running from within Eclipse), ensure that the config.ini file in the configuration directory points to the correct product or application extension via the eclipse.product or eclipse.application entry (using a product is preferred -- see the [Branding Your Application](http://eclipse.org/articles/Article-Branding/branding-your-application.html) article). Either all plug-ins need to be specified in the osgi.bundles entry of the config.ini, or the **org.eclipse.update.configurator** plug-in should be included to discover all available plug-ins the first time the application is run.

If eclipse fails silently, look in the configuration and/or workspace directories for a .log file. If you use the eclipse.exe launcher (or equivalent on other platforms) it will tell you where to find any relevant log file.

Try adding -consolelog, -debug and -clean to the command line (as program arguments, not VM arguments). For example, to run the browser example with an explicitly specified product:


    d:\j2sdk1.4.2_01\bin\java org.eclipse.core.launcher.Main -product org.eclipse.ui.examples.rcp.browser.product -consolelog -clean -debug

or

    eclipse -vm d:\j2sdk1.4.2_01\bin\java -product org.eclipse.ui.examples.rcp.browser.product -consolelog -clean -debug

-consolelog causes any log entries to be sent to the console as well (to get a console window, be sure to use java as the VM instead of javaw).

-debug causes Eclipse to log extra information about plug-in dependency problems (see [here](https://bugs.eclipse.org/bugs/show_bug.cgi?id=75648) for more background).

-clean forces Eclipse to re-read all the plugin.xml files rather than using its cached representation of the plug-in registry.

While these options are helpful for debugging, note that there is a performance penalty for -debug and -clean, so it is not recommended that they be used in the final product.

For other troubleshooting hints, see the **Troubleshooting** section of the [RCP Tutorial, part 1](http://eclipse.org/articles/Article-RCP-1/tutorial1.html).

My own RCP plug-ins are contributed by a feature. Why is the update manager complaining that my configuration is invalid?
-------------------------------------------------------------------------------------------------------------------------

If you're using a feature only for the plug-ins you write, the update manager does not check dependencies on "orphan" plug-ins (i.e. plug-ins not contributed by a feature) so the configuration appears invalid. You will need to either:

*   include all the plug-ins (yours and the RCP plug-ins) into your feature, or
*   create another feature for the RCP plug-ins.

Are editors tied to the workspace resource model or to the broader notion of files?
-----------------------------------------------------------------------------------

No. 
The concept of an editor in the workbench and the corresponding types (IEditorPart, EditorPart, IEditorInput) are not tied to the workspace resource model, or even to the notion of files (whether in the workspace or the file system). 
Editors can be used for any kind of model, and can be textual or graphical.

The Text component provides support for text editors. 
See the entry for the Text component in the list of optional components above. 
See also the [RCP text editor example](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform.md#Examples).

How can I integrate my existing Swing components into an RCP application?
-------------------------------------------------------------------------

See [this SWT FAQ entry](http://www.eclipse.org/swt/faq.php#swinginswt). Note, however, that the SWT_AWT bridge does not currently work on all platforms, e.g. Mac ([bug 67384](https://bugs.eclipse.org/bugs/show_bug.cgi?id=67384)).

Also take a look at [SwingWT](http://swingwt.sourceforge.net/), an SWT-based implementation of the Swing API.

How can I define key bindings for commands?
-------------------------------------------

As of 3.3, the preferred means of binding keys is to use commands, handlers, and contexts. 
See [Platform Command Framework#KeyBindings](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/PlatformCommandFramework.md#KeyBindings) for the extension points.

There are some wrinkles for the RCP case. 
See [Keybindings for Eclipse Commands](https://www.vogella.com/tutorials/EclipseCommands/article.html) for a tutorial.

The key binding system is designed with the general idea that it is under the control of the user, not the program. 
If you want to control key bindings in an RCP application, you have to decide whether to expose the preference system or whether to be inflexible.

To begin with, note that each org.eclipse.ui.binding is a member of a scheme. Unless you interact with the preference system, or replace the BindingService with your own class, you must put all your bindings in org.eclipse.ui.defaultAcceleratorConfiguration. If you put them in some other scheme, you will find that here is no simple API to activate your scheme.

The other obscure detail you will want is context management. If you want some commands (and thus bindings) to be active only in some parts, you will want to associate a context with your part. In createPartControl, write something like:

    	IContextService contextService = (IContextService) getSite()
    		.getService(IContextService.class);
    	contextService.activateContext(CONTEXT_ID);

At the lowest level, you can obtain the IBindingService has no 'set' functions, and BindingService is a final class in an internal package. So, if you want to get complete control, you would have to create your own implementation of IBindingService that wraps the standards one.

How can I get my views and editors to coordinate with each other?
-----------------------------------------------------------------

You can also track part activation and other lifecycle using [IPartService](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/IPartService.html), [IPartListener](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/IPartListener.html) and [IPartListener2](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/IPartListener2.html).

To track part lifecycle from within an existing view or editor, use the part service on the part's containing IWorkbenchPage:

    getSite().getPage().addPartListener(listener);

From outside the page (e.g. from an action added to the window in the ActionBarAdvisor), use the part service on the IWorkbenchWindow:

    IWorkbenchWindow window = actionBarAdvisor.getActionBarConfigurer().getWindowConfigurer().getWindow();
    window.getPartService().addPartListener(listener);

Be sure to remove the part listener in the appropriate dispose method.

Which plug-ins are needed for the Eclipse Help system?
------------------------------------------------------

See ["What other Eclipse components can be used in constructing RCP applications?"](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform/Rich_Client_Platform_FAQ.md#What-other-Eclipse-components-can-be-used-in-constructing-RCP-applications). 
Be sure to include the **org.eclipse.tomcat** plug-in since the dependency on it is indirect. 
The **org.eclipse.help.ui** plug-in requires the **org.eclipse.help.appserver** plug-in, which defines the **org.eclipse.help.appserver.server** extension point. 
The **org.eclipse.tomcat** plug-in adds an extension to this extension point. 
So although **org.eclipse.tomcat** is required, it's not found by adding all prerequisites of **org.eclipse.help.ui**, and needs to be added manually.

See also the ["Plugging in help"](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/help.htm) help topic.

See also the "Adding Help" chapter in the [RCP Book](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform/Rich_Client_Platform_Book.md).

How can I add the Eclipse Update Manager to my application?
-----------------------------------------------------------

See section 9 of the ["Developing Eclipse Rich Client Applications" tutorial](http://www.eclipsecon.org/2005/presentations/EclipseCon2005_Tutorial8.pdf) from EclipseCon 2005.

See also the ["Updating a product or extension"](http://help.eclipse.org/help31/topic/org.eclipse.platform.doc.isv/guide/product_update.htm) help topic.

See also the "Adding Update" chapter of the [RCP Book](/RCP_Book "RCP Book").

What is ICU4J and is it required?
---------------------------------

ICU4J is a set of Java libraries that provides more comprehensive support for Unicode, software globalization, and internationalization. 
In order to provide this functionality to the Eclipse community, ICU4J was added to the Eclipse platform in 3.2. 
You will see it in the build as a plug-in named com.ibm.icu.

The ICU4J plug-in has a non-negligable footprint of ~3M, which is a significant fraction of the RCP base footprint. 
If reduced footprint is more important for your application than the enhancements provided by ICU4J, it can be replaced with a plug-in that is about 100KB in size and that simply calls through to the java.* packages (default JDK implementation) of the most commonly used classes and APIs in ICU4J.

How to bundle the JRE's for Windows and for Linux in my RCP application?
------------------------------------------------------------------------

I edited the build.properties of my product's "Feature", and I put the lines bellow:

    bin.includes = feature.xml
    root.linux.gtk.x86=jre_linux/
    root.linux.gtk.x86.permissions.755=jre_linux/
    root.win32.win32.x86=jre_win/
    
The second line `root.linux.gtk.x86=jre_linux/`, tells the builder to take the contents of the "jre_linux/" directory and copy it to the root of my distribution.

The third line `root.linux.gtk.x86.permissions.755=jre_linux/` applies the chmod 755 over all the files under jre_linux/ after they are copied to the destination directory. Without it the java executable cannot be run, since it is chmod'ed to 644 (no execution permission).

Note: The above permissions line didn't work for me. Maybe this is because root.linux.gtk.x86 has already been set to jre_linux/ in the line before? When I change it to

     root.linux.gtk.x86.permissions.755=jre/bin/java
    
(only) the jre/bin/java executable is being set +x. Could someone check this, please?

TIP: the contents of the `jre_linux/` directory is a single directory called `jre`. Inside this directory are the jre's subdirectories "bin", "lib", etc. Calling it "jre" is extremely important because the launcher executable, at the absence of the `-vm path\_to\_java_executable` option will search by default for the java executable at the directory `./jre/bin`, being this directory relative from the directory where the executable was installed.

Conclusion: With this configuration the "jre/" directory tree is copied to the root of my distribution and the java executable at jre/bin/java is run by default, even if my system has other java's on its $PATH. And it wasn't necessary to set any "-vm path" to the launcher.

More options for the build.properties file can be found [here](http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.pde.doc.user/reference/pde_feature_generating_build.htm)

How to add menu item, command and handler?
------------------------------------------

*   Add dependency: MANIFEST.MF -> Dependencies tab -> Add -> _org.eclipse.ui_
*   Add extension point org.eclipse.ui.menus: plugin.xml -> Extension -> Add -> _org.eclipse.ui.menus_
    *   Right-click -> New -> menuContribution
        *   Enter locationURI: menu:file
            *   Right click -> New -> command
            *   Enter commandId: _**sampleCommand**_
            *   Enter label: Sample Menu Item
*   Add extension point org.eclipse.ui.commands: plugin.xml -> Extensions -> Add -> _org.eclipse.ui.commands_
    *   Right-click -> New -> command
        *   Enter id: _**sampleCommand**_
        *   Enter label: Sample Command
*   Add extension point org.eclipse.ui.handlers: plugin.xml -> Extensions -> Add -> _org.eclipse.ui.handlers_
    *   Right-click -> New -> handler
        *   Enter commandId: _**sampleCommand**_
        *   Enter class: sample.SampleHandler
        *   Click class link and create class
        *   Provide sample implementation of the handler class implementing _org.eclipse.core.commands.IHandler_ or extending _org.eclipse.core.commands.AbstractHandler_

MANIFEST.MF

     Manifest-Version: 1.0
     Bundle-ManifestVersion: 2
     Bundle-Name: Sample Handler
     Bundle-SymbolicName: sample;singleton:=true
     Bundle-Version: 1.0.0.qualifier
     Bundle-Vendor: sample
     Bundle-RequiredExecutionEnvironment: JavaSE-1.6
     Require-Bundle: org.eclipse.ui
    

plugin.xml

    <?xml version="1.0" encoding="UTF-8"?>
    <?eclipse version="3.4"?>
    <plugin>
       <extension
             point="org.eclipse.ui.menus">
          <menuContribution
                locationURI="menu:file">
             <command
                   commandId="sampleCommand"
                   label="Sample Menu Item"
                   style="push">
             </command>
          </menuContribution>
       </extension>
       <extension
             point="org.eclipse.ui.commands">
          <command
                id="sampleCommand"
                name="Sample Command">
          </command>
       </extension>
       <extension
             point="org.eclipse.ui.handlers">
          <handler
                class="sample.SampleHandler"
                commandId="sampleCommand">
          </handler>
       </extension>
    </plugin>

Handler – sample implementation showing a message

    package sample;
     
    import org.eclipse.core.commands.AbstractHandler;
    import org.eclipse.core.commands.ExecutionEvent;
    import org.eclipse.core.commands.ExecutionException;
    import org.eclipse.jface.dialogs.MessageDialog;
    import org.eclipse.swt.widgets.Display;
     
    public class SampleHandler extends AbstractHandler {
     
    	@Override
    	public Object execute(ExecutionEvent event) throws ExecutionException {
    		MessageDialog.openInformation(Display.getDefault().getActiveShell(),
    				"Sample Handler", "Sample Handler");
    		return null;
    	}
    }

How can I show line numbers by default in my RCP application?
-------------------------------------------------------------

Add the following line to your plugin_customization.ini file:

    org.eclipse.ui.editors/lineNumberRuler=true

