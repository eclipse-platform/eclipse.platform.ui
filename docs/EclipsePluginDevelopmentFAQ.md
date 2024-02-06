

Eclipse Plug-in Development FAQ
===============================

This page is a collection of FAQs that is intended to help a developer write Eclipse plug-ins.

This FAQ is intended to be complimentary to the [RCP FAQ](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Rich_Client_Platform/Rich_Client_Platform_FAQ.md), the [Eclipse 4 RCP FAQ](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Eclipse4_RCP_FAQ.md) and the [FAQ](https://wiki.eclipse.org/The_Official_Eclipse_FAQs) pages. 

Contents
--------

*   [1 General Development](#General-Development)
    *   [1.1 How do I find a class from Eclipse?](#How-do-I-find-a-class-from-Eclipse.3F)
    *   [1.2 I see these $NON-NLS-1$ tags all over the place when I'm browsing Eclipse's source code? What do they mean?](#I-see-these-.24NON-NLS-1.24-tags-all-over-the-place-when-I.27m-browsing-Eclipse.27s-source-code.3F-What-do-they-mean.3F)
    *   [1.3 I need help debugging my plug-in...](#I-need-help-debugging-my-plug-in...)
    *   [1.4 I'm using third party jar files and my plug-in is not working](#I.27m-using-third-party-jar-files-and-my-plug-in-is-not-working)
    *   [1.5 What is the IAdaptable interface?](#What-is-the-IAdaptable-interface.3F)
    *   [1.6 How do I read from a file that I've included in my bundle/plug-in?](#How-do-I-read-from-a-file-that-I.27ve-included-in-my-bundle.2Fplug-in.3F)
    *   [1.7 Where do I find the javadoc for the Eclipse API locally? I don't always want to load stuff up in a browser.](#Where-do-I-find-the-javadoc-for-the-Eclipse-API-locally.3F-I-don.27t-always-want-to-load-stuff-up-in-a-browser.)
    *   [1.8 A plug-in in my 'Eclipse Application' launch configuration is listed as being "out of sync", what should I do?](#A-plug-in-in-my-.27Eclipse-Application.27-launch-configuration-is-listed-as-being-.22out-of-sync.22.2C-what-should-I-do.3F)
    *   [1.9 How do I include native libraries in my bundle?](#How-do-I-include-native-libraries-in-my-bundle.3F)
*   [2 User Interface](#User-Interface)
    *   [2.1 There's a view / editor that I want to model. How do I find out what its source looks like and how it was designed?](#There.27s-a-view-.2F-editor-that-I-want-to-model.-How-do-I-find-out-what-its-source-looks-like-and-how-it-was-designed.3F)
    *   [2.2 There's a preference / property page that I want to model. How do I find out what its source looks like and how it was designed?](#There.27s-a-preference-.2F-property-page-that-I-want-to-model.-How-do-I-find-out-what-its-source-looks-like-and-how-it-was-designed.3F)
    *   [2.3 There's a window / dialog / popup that I want to model. How do I find out what its source looks like and how it was designed?](#There.27s-a-window-.2F-dialog-.2F-popup-that-I-want-to-model.-How-do-I-find-out-what-its-source-looks-like-and-how-it-was-designed.3F)
    *   [2.4 There's a wizard page that I want to model. How do I find out what its source looks like and how it was designed?](#There.27s-a-wizard-page-that-I-want-to-model.-How-do-I-find-out-what-its-source-looks-like-and-how-it-was-designed.3F)
    *   [2.5 How can I leverage the 'Outline' view?](#How-can-I-leverage-the-.27Outline.27-view.3F)
    *   [2.6 How can I show the perspective bar in my RCP application?](#How-can-I-show-the-perspective-bar-in-my-RCP-application.3F)
    *   [2.7 How do I get the perspective bar to show on the top right corner?](#How-do-I-get-the-perspective-bar-to-show-on-the-top-right-corner.3F)
    *   [2.8 How do I warn the user that a workbench part that is not currently visible has changed?](#How-do-I-warn-the-user-that-a-workbench-part-that-is-not-currently-visible-has-changed.3F)
    *   [2.9 How can I make use of the workbench's browser capabilities?](#How-can-I-make-use-of-the-workbench.27s-browser-capabilities.3F)
    *   [2.10 How do I retrieve the id of a preference page?](#How-do-I-retrieve-the-id-of-a-preference-page.3F)
    *   [2.11 How do I ask my decorator to decorate items?](#How-do-I-ask-my-decorator-to-decorate-items.3F)
    *   [2.12 How do I get the icon associated with a file or content type?](#How-do-I-get-the-icon-associated-with-a-file-or-content-type.3F)
    *   [2.13 How do I set the selection of an editor or view?](#How-do-I-set-the-selection-of-an-editor-or-view.3F)
    *   [2.14 How do I get the selection of an editor or view?](#How-do-I-get-the-selection-of-an-editor-or-view.3F)
    *   [2.15 How do I get progress feedback in the status bar in my RCP application?](#How-do-I-get-progress-feedback-in-the-status-bar-in-my-RCP-application.3F)
    *   [2.16 How do I make a New / Import / Export Wizard appear in the context menu of the Project Explorer?](#How-do-I-make-a-New-.2F-Import-.2F-Export-Wizard-appear-in-the-context-menu-of-the-Project-Explorer.3F)
    *   [2.17 How do I show a message dialogue for exceptions and log them?](#How-do-I-show-a-message-dialogue-for-exceptions-and-log-them.3F)
    *   [2.18 How do I launch a dialogue from a non-ui thread and get a return value](#How-do-I-launch-a-dialogue-from-a-non-ui-thread-and-get-a-return-value)
    *   [2.19 How do I make a title area dialogue with radio buttons?](#How-do-I-make-a-title-area-dialogue-with-radio-buttons.3F)
*   [3 Editors](#Editors)
    *   [3.1 How do I add those rectangles in my source editor like what JDT does for parameter names during code completion?](#How-do-I-add-those-rectangles-in-my-source-editor-like-what-JDT-does-for-parameter-names-during-code-completion.3F)
    *   [3.2 How do I implement a 'Quick Outline' for my editor?](#How-do-I-implement-a-.27Quick-Outline.27-for-my-editor.3F)
    *   [3.3 How do I get an editor's StyledText widget?](#How-do-I-get-an-editor.27s-StyledText-widget.3F)
    *   [3.4 How can I get the IDocument from an editor?](#How-can-I-get-the-IDocument-from-an-editor.3F)
    *   [3.5 How do I get an IFile given an IEditorPart or IEditorInput?](#How-do-I-get-an-IFile-given-an-IEditorPart-or-IEditorInput.3F)
    *   [3.6 How do I hide the tabs of a MultiPageEditorPart if it only has one page?](#How-do-I-hide-the-tabs-of-a-MultiPageEditorPart-if-it-only-has-one-page.3F)
    *   [3.7 How do I change the editor that is being opened when a marker has been opened?](#How-do-I-change-the-editor-that-is-being-opened-when-a-marker-has-been-opened.3F)
    *   [3.8 How can I make my editor respond to a user opening a marker?](#How-can-I-make-my-editor-respond-to-a-user-opening-a-marker.3F)
    *   [3.9 Why does the workbench keep opening a new editor every time I open a marker?](#Why-does-the-workbench-keep-opening-a-new-editor-every-time-I-open-a-marker.3F)
    *   [3.10 How should I let my editor know that its syntax colours have changed?](#How-should-I-let-my-editor-know-that-its-syntax-colours-have-changed.3F)
    *   [3.11 How do I close one/all of my editors upon workbench shutdown so that it won't appear upon workbench restart?](#How-do-I-close-one.2Fall-of-my-editors-upon-workbench-shutdown-so-that-it-won.27t-appear-upon-workbench-restart.3F)
    *   [3.12 How do I prevent a particular editor from being restored on the next workbench startup?](#How-do-I-prevent-a-particular-editor-from-being-restored-on-the-next-workbench-startup.3F)
*   [4 Debug](#Debug)
    *   [4.1 How do I invoke a process and have its output managed by the 'Console' view?](#How-do-I-invoke-a-process-and-have-its-output-managed-by-the-.27Console.27-view.3F)
    *   [4.2 How do I associate my executed process with its command line counterpart?](#How-do-I-associate-my-executed-process-with-its-command-line-counterpart.3F)
    *   [4.3 How do I capture the output of my launched application like the 'Console' view?](#How-do-I-capture-the-output-of-my-launched-application-like-the-.27Console.27-view.3F)
    *   [4.4 How do I run Eclipse launch configurations programmatically?](#How-do-I-run-Eclipse-launch-configurations-programmatically.3F)
*   [5 Release](#Release)
    *   [5.1 How do I make a p2 repository?](#How-do-I-make-a-p2-repository.3F)
    *   [5.2 How do I add files to the root of the installation directory?](#How-do-I-add-files-to-the-root-of-the-installation-directory.3F)

General Development
-------------------

### How do I find a class from Eclipse?



You  need to set up your workspace so that all Eclipse plug-ins are found by the Java search engine. This can be accomplished by loading all the Eclipse plug-ins into your workspace, but this quickly results in a cluttered workspace in which it is difficult to find your own projects. There are two easier approaches to adding Eclipse plug-ins to the Java search engine's index.

**Option 1**

In Eclipse 3.5 (Galileo) or later

*   Open the Plug-in Development Preference Page by going to **Window > Preferences > Plug-in Development**.
*   Check the box marked **Include all plug-ins from target in Java search**.

**Option 2**

*   Activate the 'Plug-ins' view by going to **Window > Show View > Other > PDE > Plug-ins**.
*   Select all plug-ins in the view.
*   From the context menu, select **Add to Java Search**.

Once you have done this, switch back to the Java perspective and use **Navigate > Open Type** (or press Ctrl + Shift + T) and start typing the name of the class or interface you are looking for. You will now be able to quickly open an editor on any Java type in the Eclipse Platform. If you are searching for the plug-in that contains that class, you will find that information in the bottom of the 'Open Type' dialog. Please note that a class that's in package x.y.z is not guaranteed to be in plug-in x.y.z, it may be contributed by another plug-in.

In case you are curious, this works by creating in your workspace a Java project called External Plug-in Libraries. This project will have all the Eclipse plug-ins you selected on its build path, which ensures that they will be consulted by the Java search engine when searching for and opening Java types. You can use a similar technique to add other Java libraries to the search index. Simply add the JARs you want to be able to search to the build path of any Java project in your workspace, and they will automatically be included in the search.



### I see these $NON-NLS-1$ tags all over the place when I'm browsing Eclipse's source code? What do they mean?

They are meant to mark a string literal as not needing to be externalized (as in, translated / localized). You will often see something like...

    if (string.equals("")) { //$NON-NLS-1$
        // do stuff
    }

...this would be a scenario where a string wouldn't need to be localized because it is a string for the code to "manipulate", per se, over it being part of a message to the user at the UI level.

### I need help debugging my plug-in...

Are you getting errors like "Unhandled loop event exception" messages in your console with nothing useful after it? Make sure you have [-consoleLog](http://wiki.eclipse.org/Graphical_Eclipse_FAQs#I_get_an_unhandled_event_loop_exception_in_my_console._What_gives.3F) as a **Program Argument** in your launch configuration. 

### I'm using third party jar files and my plug-in is not working

Did you add those jars to your project's **Java Build Path**? Do not use that project property page when developing plug-ins. You have two options.

**Option 1: turn the jars into plug-ins** Use New > Project > Plug-in Development > Plug-in from existing JAR archive. That will turn one or more jar files into a single jar plug-in. For something like log4j you can then set up Buddy-Classloading, etc.

Prior to 3.2.1, you had to make modifications to the build.properties file. See [bug 146042](https://bugs.eclipse.org/bugs/show_bug.cgi?id=146042) (RCP export has problems with required plug-ins).

**Option 2: include the jars in a plug-in**

1.  Use Import>File System to import the jar files into your plug-in project, say in the <project>/lib directory.
2.  Use "Add..." to add the jars to the classpath section of the PDE Editor>Runtime tab.
3.  Use "New..." to add "." library back (with no quotes, of course). Some versions of eclipse automatically do this for you.
4.  Make sure your binary build exports the new jar files on the PDE Editor>Build tab.
5.  Save
6.  On the project, use the popup menu>PDE Tools>Update Classpath to correctly add the jars to the eclipse project classpath.
7.  Export any packages that you need to using the PDE Editor>Runtime tab
8.  Save

**Common mistakes for option 2**

One common mistake is to forget to add the '.' to the classpath after adding your library. If you fail to do this, your plug-in will crash with a message like this: 

           java.lang.ClassNotFoundException: lib.foo.Bar
           at org.eclipse.osgi.internal.loader.BundleLoader.findClassInternal(BundleLoader.java:513)
           at org.eclipse.osgi.internal.loader.BundleLoader.findClass(BundleLoader.java:429)
           at org.eclipse.osgi.internal.loader.BundleLoader.findClass(BundleLoader.java:417)
           at org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader.loadClass(DefaultClassLoader.java:107)
           at java.lang.ClassLoader.loadClass(Unknown Source)
           ...
    

 

Example of META-INF/MANIFEST.MF:

           // lib.foo MANIFEST.MF
           Manifest-Version: 1.0
           Bundle-ManifestVersion: 2
           Bundle-Name: Foo
           Bundle-SymbolicName: lib.foo;singleton:=true
           Bundle-Version: 1.0.0.qualifier
           Bundle-Activator: lib.foo.Activator
           Bundle-ActivationPolicy: lazy
           Bundle-RequiredExecutionEnvironment: JavaSE-1.6
           Import-Package: org.osgi.framework;version="1.6.0"
           Export-Package: lib.foo
           Bundle-ClassPath: lib/someRandomDep.jar  <--- WRONG
           Bundle-ClassPath: .,lib/someRandomDep.jar  <--- CORRECT
    

 

Also check whether your build.properties file now lists the '.' and the 'lib'-directory (or whatever you named it) in 'bin.includes':

           bin.includes = META-INF/,\
                  lib/,\
                  .
                  
Check out [bug 108781](https://bugs.eclipse.org/bugs/show_bug.cgi?id=108781).

It talks about how adding a 3rd party jar removes the default "." classpath, and the need to add it back.

Also, Eclipse can handle jars within jars. It expands them into a temporary location during runtime.

### What is the [IAdaptable](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/runtime/IAdaptable.html) interface?

The articles below may be of your interest.

*   [Adapters](http://www.eclipse.org/articles/article.php?file=Article-Adapters/index.html)

### How do I read from a file that I've included in my bundle/plug-in?

The [FileLocator](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/runtime/FileLocator.html) class should be able to do most of the things that you want. It can open up a java.io.InputStream as well as provide a java.io.File. 
You should keep in mind that the java.io.File approach is not going to work if your bundle is packaged as a jar file. To get a reference to your bundle's [Bundle](http://www.osgi.org/javadoc/r4/org/osgi/framework/Bundle.html) instance, you can use [Platform](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/runtime/Platform.html)'s [getBundle(String)](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/runtime/Platform.html#getBundle(java.lang.String)) method. Alternatively, if your bundle's activator subclasses [Plugin](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/runtime/Plugin.html) or [AbstractUIPlugin](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/plugin/AbstractUIPlugin.html), then you can just call [getBundle()](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/runtime/Plugin.html#getBundle()) from it directly. If your activator simply implements the [BundleActivator](http://www.osgi.org/javadoc/r4v41/org/osgi/framework/BundleActivator.html) interface, then from your implementation of the [start(BundleContext)](http://www.osgi.org/javadoc/r4v41/org/osgi/framework/BundleActivator.html#start(org.osgi.framework.BundleContext)) method, you can just call [getBundle()](http://www.osgi.org/javadoc/r4v41/org/osgi/framework/BundleContext.html#getBundle()) on the passed in [BundleContext](http://www.osgi.org/javadoc/r4v41/org/osgi/framework/BundleContext.html) to store the Bundle instance in a field for retrieval later. You can also query for Bundle instances from the [PackageAdmin](http://www.osgi.org/javadoc/r4v41/org/osgi/service/packageadmin/PackageAdmin.html) service.

    // your BundleActivator implementation will probably look something
    // like the following
    public class Activator implements BundleActivator {
      private static Activator instance;
     
      private Bundle bundle;
     
      public void start(BundleContext context) throws Exception {
        instance = this;
        bundle = context.getBundle();
      }
     
      public void stop(BundleContext context) throws Exception {
        instance = null;
      }
     
      public static Activator getDefault() {
        return instance;
      }
     
      public Bundle getBundle() {
        return bundle;
      }
    }
     
    // code to retrieve an java.io.InputStream
    InputStream inputStream = FileLocator.openStream(
        Activator.getDefault().getBundle(), new Path("resources/setup.xml"), false);

### Where do I find the javadoc for the Eclipse API locally? I don't always want to load stuff up in a browser.

You will already have the javadocs for the Eclipse Platform if you installed the Eclipse SDK. The HTML files are stored in the org.eclipse.platform.doc.isv jar file located in your `eclipse/plugins` folder. Likewise, you can find JDT APIs in their org.eclipse.jdt.doc.isv jar file and so on.

### A plug-in in my 'Eclipse Application' launch configuration is listed as being "out of sync", what should I do?

Open the Problems View, click on the small downwards triangle in the views menu and select 'Group by'=Type. Look through the Plugin Problems, fixing anything which has to do with the "Out Of Sync" Plugin.

Another known workaround to this problem is to remove your workspace's `.metadata/org.eclipse.pde.core` folder. Cleaning and reloading the target platform does not appear to fix this problem.

### How do I include native libraries in my bundle?

When you want to use native libraries within your bundle using custom libraries, you can either tell the user to set the appropriate system environment path or include those libraries in the plug-in.

If you wish to include the natives in your bundle, continue reading.

Edit your MANIFEST.MF manually by adding the following lines

    Bundle-NativeCode: 
     /libs/yourlib.dll; 
     /libs/someotherlib.dll; 
     osname=win32; processor=x86

Afterwards, explicitly load all libraries manually using System.loadLibrary().

    ArrayList<String> list = new ArrayList<String>();
    list.add("yourlib");
    list.add("someotherlib");
    for (String lib : list) {
    	System.loadLibrary(lib);
    }

**Caution:** If you get an UnsatisfiedLinkError when you load a library which depends on a library which hasn't been loaded yet. To overcome this automatically, you can iterate over a list of the required libraries, and throw all out which could be loaded, until the list is empty. Like this, you can compute the correct ordering for loading your libraries.

    /**
     * Iterate through the list of libraries and remove all libraries from the list
     * that could be loaded. Repeat this step until all libraries are loaded.
     * The Reason: You can only load a library, if all dependent libraries have
     * already been loaded. So you need to know the correct ordering from the
     * leafs to the root. Load the leafs first!
     */
    ArrayList<String> toRemove;
    int i = 10; //cancel after 10 iterations. You probably forgot to mention a library to load.
    while (!list.isEmpty() && (i > 0)) {
    	toRemove = new ArrayList<String>();
    	int j = 0;
    	while (j < list.size() - 1) {
    		try {
    			System.loadLibrary(list.get(j));
    			toRemove.add(list.get(j));
    			System.out.println("loaded library " + j + ": " + list.get(j));
    		} catch (Throwable e) {
    //					System.out.println(e.getMessage());
    //					System.out.println("error loading lib: " + list.get(j));
    		}
    		j++;
    	}
    	for (String lib : toRemove) {
    		//System.out.println("removing lib : " + lib);
    		list.remove(lib);
    	}
    	i--;
    }

Please note that for debugging, e.getMessage() may give you more information for debugging. It can assist you if you mistyped some of the library names for instance.

You should not use this code for production but to compute the correct ordering of your libraries!

User Interface
--------------

### There's a view / editor that I want to model. How do I find out what its source looks like and how it was designed?

Views and editors generally extend [ViewPart](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/part/ViewPart.html) and [EditorPart](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/part/EditorPart.html) respectively. Placing a breakpoint in its constructor when you show the view or editor or invoking the "Plug-in Spy" with the Alt+Shift+F1 keybinding will tell you what the name of that class is. From there, you can inspect the class's code to see how it works. In the case of the user interface elements, you should look at its implementation of the [createPartControl(Composite)](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/part/WorkbenchPart.html#createPartControl(org.eclipse.swt.widgets.Composite)) method.

If you cannot get "Plug-in Spy" to run, you may not have [PDE](/PDE "PDE") installed.

### There's a preference / property page that I want to model. How do I find out what its source looks like and how it was designed?

Put a breakpoint in the constructors of the [PreferencePage](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/preference/PreferencePage.html) / [PropertyPage](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/dialogs/PropertyPage.html) class. Open the preferences / properties dialog, and then select the page you are interested about. Now you can identify which class is constructing that page based on the stack trace.

You can also invoke the "Plug-in Spy" with the Alt+Shift+F1 keybinding to retrieve information about the page that your mouse is currently hovering over. If you cannot get "Plug-in Spy" to run, you may not have [PDE](/PDE "PDE") installed.

### There's a window / dialog / popup that I want to model. How do I find out what its source looks like and how it was designed?

There are two usual suspects, an SWT [Shell](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/swt/widgets/Shell.html) or a JFace [Window](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/window/Window.html). Generally, most developers subclass's JFace's [Dialog](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/dialogs/Dialog.html) class (which is a subclass of Window) for their dialog needs. So you should first try and put a breakpoint in Window's [open()](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/window/Window.html#open()) method and see if the window you're trying to model stops at that breakpoint when it has been opened (shown). If not, try Shell's [open()](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/swt/widgets/Shell.html#open()) or [setVisible(boolean)](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/swt/widgets/Shell.html#setVisible(boolean)) methods.

You can also invoke the "Plug-in Spy" with the Alt+Shift+F1 keybinding to retrieve information about the window that your mouse is currently hovering over. If you cannot get "Plug-in Spy" to run, you may not have [PDE](/PDE "PDE") installed.

Wayne's [blog post](http://dev.eclipse.org/blogs/wayne/2008/06/18/does-anybody-know-how-such-and-such-is-coded/) may be of help to you.

### There's a wizard page that I want to model. How do I find out what its source looks like and how it was designed?

Wizard pages usually extend the [WizardPage](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/wizard/WizardPage.html) class. Putting a breakpoint in its constructor will help you identify the class that is creating that page.

You can also invoke the "Plug-in Spy" with the Alt+Shift+F1 keybinding to retrieve information about the wizard that your mouse is currently hovering over. If you cannot get "Plug-in Spy" to run, you may not have [PDE](/PDE "PDE") installed.

### How can I leverage the 'Outline' view?

In your [IWorkbenchPart](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/IWorkbenchPart.html)'s implementation, you should override the [getAdapter(Class)](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/runtime/IAdaptable.html#getAdapter(java.lang.Class)) method and return your own implementation of [IContentOutlinePage](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/views/contentoutline/IContentOutlinePage.html) or you can choose to subclass [ContentOutlinePage](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/views/contentoutline/ContentOutlinePage.html).

    public Object getAdapter(Class adapter) {
        if (adapter.equals(IContentOutlinePage.class)) {
            return page;
        }
        // do NOT forget this line
        return super.getAdapter(adapter);
    }

### How can I show the perspective bar in my RCP application?

In your concrete subclass of [WorkbenchWindowAdvsior](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/application/WorkbenchWindowAdvisor.html), you should override its [preWindowOpen()](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/application/WorkbenchWindowAdvisor.html#preWindowOpen()) method and then call [setShowPerspectiveBar(boolean)](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/application/IWorkbenchWindowConfigurer.html#setShowPerspectiveBar(boolean)) on your [IWorkbenchWindowConfigurer](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/application/IWorkbenchWindowConfigurer.html) (which you retrieve by invoking [getWindowConfigurer()](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/application/WorkbenchWindowAdvisor.html#getWindowConfigurer())).

### How do I get the perspective bar to show on the top right corner?

The code below will demonstrate this.

PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.DOCK\_PERSPECTIVE\_BAR, IWorkbenchPreferenceConstants.TOP_RIGHT);

    PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR, IWorkbenchPreferenceConstants.TOP_RIGHT);

### How do I warn the user that a workbench part that is not currently visible has changed?

From your [WorkbenchPart](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/part/WorkbenchPart.html) subclass, you can use the code below. Please note that the code below currently only works on views. For notification support in editors, please see [bug 86221](https://bugs.eclipse.org/bugs/show_bug.cgi?id=86221).

    IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) part.getSite().getService(IWorkbenchSiteProgressService.class);
    // notify the user by turning the workbench part's title bold
    service.warnOfContentChange();

### How can I make use of the workbench's browser capabilities?

To leverage the workbench's browser capabilities, you will have to interact with the [IWorkbenchBrowserSupport](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/browser/IWorkbenchBrowserSupport.html) class. The code below will show you how to retrieve an implementation of this interface and open a website with the external browser:

    try {
        IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
        browserSupport.getExternalBrowser().openURL(new URL("http://www.eclipse.org"));
    } catch (PartInitException e) {
        // handle the exception
    }

### How do I retrieve the id of a preference page?

You can try the following code below:

    public String getId(IPreferencePage page) {
        PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
        List list = pm.getElements(PreferenceManager.PRE_ORDER);
     
        for (int i = 0; i < list.size(); i++) {
            PreferenceNode node = (PreferenceNode) list.get(i);
            IPreferencePage p = node.getPage();
     
            if (p == page) {
                return node.getId();
            }
        }
        return null;
    }

### How do I ask my decorator to decorate items?

You can try the following code below:

    PlatformUI.getWorkbench().getDecoratorManager().update("com.mycompany.product.ui.decoratorId");

### How do I get the icon associated with a file or content type?

You can try the following code below:

    IContentType contentType = IDE.getContentType(file);
    ImageDescriptor imageDescriptor =
        PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(file.getName(), contentType);

### How do I set the selection of an editor or view?

You can retrieve the selection provider from a workbench part from its site.

    IWorkbenchPartSite site = workbenchPart.getSite();
    ISelectionProvider provider = site.getSelectionProvider();
    // this can be null if the workbench part hasn't set one, better safe than sorry
    if (provider != null) {
        provider.setSelection(...);
    }

### How do I get the selection of an editor or view?

You can retrieve the selection from the ISelectionService or get it directly from the selection provider of the part from its site.

    ISelectionService selectionService = (ISelectionService) serviceLocator.get(ISelectionService.class);
    ISelection selection = selectionService.getSelection(partId);
    /* ... */

    IWorkbenchPartSite site = workbenchPart.getSite();
    ISelectionProvider provider = site.getSelectionProvider();
    // this can be null if the workbench part hasn't set one, better safe than sorry
    if (provider != null) {
        ISelection selection = provider.getSelection();
        /* ... */
    }

### How do I get progress feedback in the status bar in my RCP application?

Try adding the following piece of code in your [WorkbenchWindowAdvisor](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/application/WorkbenchWindowAdvisor.html)'s [preWindow()](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/application/WorkbenchWindowAdvisor.html#preWindowOpen%28%29) implementation:

    public void preWindowOpen() {
        getWindowConfigurer().setShowProgressIndicator(true);
    }

### How do I make a New / Import / Export Wizard appear in the context menu of the Project Explorer?

Add an extension to the extension point [org.eclipse.ui.navigator.navigatorContent](http://help.eclipse.org/helios/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/extension-points/org_eclipse_ui_navigator_navigatorContent.html) with a commonWizard element that points to the ID of your wizard.

### How do I show a message dialogue for exceptions and log them?

Often when catching exceptions, it's puzzling as to what one is suppose to do with them.

The following code solves this dilemma. 
It is recomended to put this into your activator.

It produces a message box as such:

![OpenErrorPerfPlugin.png](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/OpenErrorPerfPlugin.png)

The details show the stack trace.

It then logs the caught exception in the error-logging framework.

To use the code, call it as following:

             try {
                throw new EmptyStackException();
            }
            catch (EmptyStackException ex) {
                openError(ex, "End of the world is near");
            }
     
    //       ....
     
         /**
         * Log the given exception and display the message/reason in an error
         * message box. (From org.eclipse.linuxtools.packagekit.ui.Activator)
         *
         * @param ex the given exception to display
         * @since 2.0
         */
        public void openError(Exception ex, final String title) {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
     
            final String message = ex.getMessage();
            final String formattedMessage = PLUGIN_ID + " : " + message; //$NON-NLS-1$
            final Status status = new Status(IStatus.ERROR, PLUGIN_ID, formattedMessage, new Throwable(writer.toString()));
     
            getLog().log(status);
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    ErrorDialog.openError(Display.getDefault().getActiveShell(),
                            title, message, status);
                }
            });
        }

(It gets the default instance and calls the method).

(It is safe to call this from any thread, as this opens the dialogue from the U.I thread)

An example of this can be found in: (note, this is internal, avoid referencing. Instead copy the code into your plugin.)

org.eclipse.linuxtools.internal.perf.PerfPlugin.openError(Exception, String)

    org.eclipse.linuxtools.internal.perf.PerfPlugin.openError(Exception, String)

### How do I launch a dialogue from a non-ui thread and get a return value

In a U.I thread, you typically have access to a shell. In a background process, you usually don't and if you try to get it you get an exception.

The way around this is to create a runnable object and make the UI thread run it. Also, you use 'syncExec' (as oppose to asyncExec), which forces the current code to wait until the runnable has completed execution.

Example source code:

            //define some final variable outside the runnable. This should have a getter/setter method. 
            final AtomicBoolean userChoice = new AtomicBoolean(false);
     
            //To generate U.I, we make sure to call the U.I thread,
            //otherwise we get an U.I exception.
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                @Override
                public void run() {
     
                    Shell parent = PlatformUI.getWorkbench().getDisplay().getActiveShell();  //get shell. 
                    boolean okPressed = MessageDialog.openConfirm(parent, "prof err",
                            "Flag is not set in options. Would you like to add/rebuild?");
     
                    if (okPressed) {
                        userChoice.set(true);
                    } else
                        userChoice.set(false);
                    }
            });
     
            //Retrieve the value that the runnable changed. 
            return userChoice.get();

  

### How do I make a title area dialogue with radio buttons?

An example of how you can extend the title area dialogue to suit your needs is shown in \[[title area dialogue with radio buttons](https://wiki.eclipse.org/Eclipse_Plug-in_Development_FAQ/TitleAreaDialogWithRadioButtons)\]

Editors
-------

### How do I add those rectangles in my source editor like what JDT does for parameter names during code completion?

To achieve this, you will need to use a [LinkedModeModel](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/text/link/LinkedModeModel.html) and a [LinkedModeUI](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/text/link/LinkedModeUI.html). Open the 'Call Hierarchy' on either of the two constructors to find out how to use those two classes.

### How do I implement a 'Quick Outline' for my editor?

JDT's implementing class is named 'org.eclipse.jdt.internal.ui.text.JavaOutlineInformationControl', you should take a look at that.

### How do I get an editor's [StyledText](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/swt/custom/StyledText.html) widget?

Since you cannot access the editor's [ITextViewer](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/text/ITextViewer.html), you will have to try using the code below.

    StyledText text = (StyledText) editor.getAdapter(Control.class);

### How can I get the [IDocument](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/text/IDocument.html) from an editor?

Assuming the editor adapts to the [ITextEditor](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/texteditor/ITextEditor.html) interface, you can try the code below.

    ITextEditor editor = (ITextEditor) editorPart.getAdapter(ITextEditor.class):
    if (editor != null) {
      IDocumentProvider provider = editor.getDocumentProvider();
      IDocument document = provider.getDocument(editor.getEditorInput());
    }

This code should work on most text editors.

### How do I get an [IFile](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/resources/IFile.html) given an [IEditorPart](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/IEditorPart.html) or [IEditorInput](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/IEditorInput.html)?

The code below will demonstrate how to do this.

    IFile file = (IFile) editorPart.getEditorInput().getAdapter(IFile.class);
    if (file != null) {
        // do stuff
    }

Note that [IFile](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/resources/IFile.html)s are meant to represent files within the workspace and will not work if the file that has been opened is not contained within the workspace. Instead, a [FileStoreEditorInput](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/ide/FileStoreEditorInput.html) is usually passed into the editor when the editor is opening a file outside the workspace.

### How do I hide the tabs of a [MultiPageEditorPart](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/part/MultiPageEditorPart.html) if it only has one page?

Adding the code below into your MultiPageEditorPart's subclass should do the trick.

    protected void createPages() {
        super.createPages();
        if (getPageCount() == 1) {
            Composite container = getContainer();
            if (container instanceof CTabFolder) {
                ((CTabFolder) container).setTabHeight(0);
            }
        }
    }

### How do I change the editor that is being opened when a marker has been opened?

You can associate the string ID of your editor onto your marker with [IMarker](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/resources/IMarker.html)'s [setAttribute(String, Object)](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/resources/IMarker.html#setAttribute(java.lang.String,%20java.lang.Object)) method by using the [EDITOR\_ID\_ATTR](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/ide/IDE.html#EDITOR_ID_ATTR) string constant defined in the [IDE](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/ide/IDE.html) class as the attribute name.

    marker.setAttribute(IDE.EDITOR_ID_ATTR, "com.example.xyz.editorID");

### How can I make my editor respond to a user opening a marker?

When a marker has been opened, the Eclipse Platform tries to help the user out via the [IGotoMarker](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/ide/IGotoMarker.html) interface. Your editor should either implement the interface or respond to this class by returning an implementation via the [getAdapter(Class)](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/runtime/IAdaptable.html#getAdapter(java.lang.Class)) method.

    public Object getAdapter(Class adapter) {
        if (adapter.equals(IGotoMarker.class)) {
            return gotoMarker;
        }
        return super.getAdapter(adapter);
    }

IGotoMarker's [gotoMarker(IMarker)](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/ide/IGotoMarker.html#gotoMarker(org.eclipse.core.resources.IMarker)) method will be called accordingly on the corresponding interface implementation and it is in that method implementation that you can react to a user opening a marker.

### Why does the workbench keep opening a new editor every time I open a marker?

Are you using a custom [IEditorInput](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/IEditorInput.html) implementation for your editor? You should override Object's equals(Object) method to return true if your custom implementation is equal to another IEditorInput.

_Clients implementing this editor input interface should override Object.equals(Object) to answer true for two inputs that are the same. The IWorbenchPage.openEditor APIs are dependent on this to find an editor with the same input._

### How should I let my editor know that its syntax colours have changed?

You should return true when you receive the proper notifications through [AbstractTextEditor](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/texteditor/AbstractTextEditor.html)'s [affectsTextPresentation(PropertyChangeEvent)](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/texteditor/AbstractTextEditor.html#affectsTextPresentation(org.eclipse.jface.util.PropertyChangeEvent)) method.

### How do I close one/all of my editors upon workbench shutdown so that it won't appear upon workbench restart?

The snippet below will close all Editors in the workbench when you close the eclipse application.

    IWorkbench workbench = PlatformUI.getWorkbench();
    final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
     
    workbench.addWorkbenchListener( new IWorkbenchListener()
    {
        public boolean preShutdown( IWorkbench workbench, boolean forced )
        {                            
            activePage.closeEditors( activePage.getEditorReferences(), true);
            return true;
        }
     
        public void postShutdown( IWorkbench workbench )
        {
     
        }
    });

  
The example below shows how to close an editor that is programmatically opened.

    IWorkbench workbench = PlatformUI.getWorkbench();
    final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
     
    final IEditorPart editorPart = IDE.openEditorOnFileStore( activePage, fileStore );
     
    workbench.addWorkbenchListener( new IWorkbenchListener() {
        public boolean preShutdown( IWorkbench workbench, boolean forced )     {                            
            activePage.closeEditor(editorPart, true);
            return true;
        }
     
        public void postShutdown( IWorkbench workbench )
        {
     
        }
    });



### How do I prevent a particular editor from being restored on the next workbench startup?

In your [IEditorInput](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/IEditorInput.html) implementation, you can return `null` for [getPersistable()](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/IEditorInput.html#getPersistable()) or `false` for [exists()](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/IEditorInput.html#exists()).

Debug
-----

### How do I invoke a process and have its output managed by the 'Console' view?

Use [DebugPlugin](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/DebugPlugin.html)'s [newProcess(ILaunch, Process, String)](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/DebugPlugin.html#newProcess(org.eclipse.debug.core.ILaunch,%20java.lang.Process,%20java.lang.String)) or [newProcess(ILaunch, Process, String, Map)](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/DebugPlugin.html#newProcess(org.eclipse.debug.core.ILaunch,%20java.lang.Process,%20java.lang.String,%20java.util.Map)) method. You will probably be calling this in your [ILaunchConfigurationDelegate](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/model/ILaunchConfigurationDelegate.html) implementation.

### How do I associate my executed process with its command line counterpart?

Your [IProcess](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/model/IProcess.html) implementation must return a valid string that corresponds to the [IProcess.ATTR_CMDLINE](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/model/IProcess.html#ATTR_CMDLINE) attribute. The sample code below will demonstrate how this is done with the [DebugPlugin](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/DebugPlugin.html)'s [newProcess(ILaunch, Process, String, Map)](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/DebugPlugin.html#newProcess(org.eclipse.debug.core.ILaunch,%20java.lang.Process,%20java.lang.String,%20java.util.Map)) method.

    String commandLine = "/usr/bin/make";
    Map attributes = new HashMap();
    attributes.put(IProcess.ATTR_CMDLINE, commandLine);
    Process process = Runtime.getRuntime().exec(commandLine);
    // this assumes that 'launch' is a non-null reference to an ILaunch implementation
    DebugPlugin.newProcess(launch, process, "make", attributes);

### How do I capture the output of my launched application like the 'Console' view?

If the underlying [IProcess](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/model/IProcess.html) allows for the retrieval of its [IStreamsProxy](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/model/IStreamsProxy.html), you can retrieve a corresponding [IStreamMonitor](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/model/IStreamMonitor.html) and attach an [IStreamListener](http://help.eclipse.org/stable/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/IStreamListener.html) onto it to monitor changes to the stream.

The sample code below will demonstrate how to read the [InputStream](http://java.sun.com/j2se/1.4.2/docs/api/java/io/InputStream.html) of an executed process:

    String commandLine = "/usr/bin/make";
    Process process = Runtime.getRuntime().exec(commandLine);
    IProcess iProcess = DebugPlugin.newProcess(launch, process, "make", attributes);
    iProcess.getStreamsProxy().getOutputStreamMonitor().addListener(new IStreamListener(){
        public void streamAppended (String text, IStreamMonitor monitor){
           //TODO: As per user requirement. 
        }
    });

### How do I run Eclipse launch configurations programmatically?

Let us say you want to contribute to the PackageExplorer and run a CompilationUnit using a context menu. This is how you would run a Java Application using a dynamic launch configuration.

First, an [ILaunchConfiguration](http://help.eclipse.org/galileo/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/ILaunchConfiguration.html) is created by using its [ILaunchConfigurationType](http://help.eclipse.org/galileo/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/ILaunchConfigurationType.html). Already existing types can be obtained via the [ILaunchManager](http://help.eclipse.org/galileo/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/debug/core/ILaunchManager.html):

    ILaunchConfigurationType javaType = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
    ILaunchConfigurationWorkingCopy config = javaType.newInstance(null, name);

Then you need so set each attribute to complete your launch configuration. Check out [JavaRuntime](http://help.eclipse.org/ganymede/index.jsp?topic=/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/launching/JavaRuntime.html) for further methods, e.g. variable entries.

    List<String> classpath = new ArrayList<String>();
    classpath.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path("/project.web/src/main/java")).getMemento());
    classpath.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path("/lib/dev-2.0.0.jar")).getMemento());
    classpath.add(JavaRuntime.newRuntimeContainerClasspathEntry(new Path( JavaRuntime.JRE_CONTAINER ), IRuntimeClasspathEntry.STANDARD_CLASSES, project).getMemento());
    classpath.add(new DefaultProjectClasspathEntry(project).getMemento());
    config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
     
    config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
     
    List<String> resourceTypes = new ArrayList<String>();
    List<String> resourcePaths = new ArrayList<String>();
    resourcePaths.add("/project.web");
    resourceTypes.add("4");
    config.setAttribute(LaunchConfiguration.ATTR_MAPPED_RESOURCE_TYPES, resourceTypes);
    config.setAttribute(LaunchConfiguration.ATTR_MAPPED_RESOURCE_PATHS, resourcePaths);
     
    config.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
     
    config.setAttribute(JavaMainTab.ATTR_CONSIDER_INHERITED_MAIN, true);
    config.setAttribute(JavaMainTab.ATTR_INCLUDE_EXTERNAL_JARS, true);
     
    config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "project.Main");
    config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "-startupUrl index.html");
    config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "project.web");
    config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Xmx256M");

Now you can either run it directly in Eclipse...

    DebugUITools.launch(config, ILaunchManager.RUN_MODE);

...or save this configuration, which is great for verifying the attributes.

    config.doSave();



JDT has support for launching Java programs. First, add the following plug-ins to your dependent list:


*   org.eclipse.debug.core
*   org.eclipse.jdt.core
*   org.eclipse.jdt.launching

With those plug-ins added to your dependent plug-in list, your Java program can be launched using the JDT in two ways. In the first approach, an IVMRunner uses the currently installed VM, sets up its classpath, and asks the VM runner to run the program: 

       void launch(IJavaProject proj, String main) {
          IVMInstall vm = JavaRuntime.getVMInstall(proj);
          if (vm == null) vm = JavaRuntime.getDefaultVMInstall();
          IVMRunner vmr = vm.getVMRunner(ILaunchManager.RUN_MODE);
          String[] cp = JavaRuntime.
             computeDefaultRuntimeClassPath(proj);
          VMRunnerConfiguration config = 
             new VMRunnerConfiguration(main, cp);
          ILaunch launch = new Launch(null, 
             ILaunchManager.RUN_MODE, null);
          vmr.run(config, launch, null);
       }
    

The second approach is to create a new launch configuration, save it, and run it. The cfg parameter to this method is the name of the launch configuration to use:


       void launch(IJavaProject proj, String cfg, String main) {
          DebugPlugin plugin = DebugPlugin.getDefault();
          ILaunchManager lm = plugin.getLaunchManager();
          ILaunchConfigurationType t = lm.getLaunchConfigurationType(
            IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
          ILaunchConfigurationWorkingCopy wc = t.newInstance(
            null, cfg);
          wc.setAttribute(
            IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, 
            proj.getElementName());
          wc.setAttribute(
            IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, 
            main);
          ILaunchConfiguration config = wc.doSave();   
          config.launch(ILaunchManager.RUN_MODE, null);
       }
    



More information is available at **Help > Help Contents > JDT Plug-in Developer Guide** \> JDT Debug > Running Java code**.**


Release
-------

### How do I make a p2 repository?

This is not the only way to create a repository, and it may not be the best way, but it worked for me. YMMV.

*   Initially, create a project in your workspace called "UpdateSite".
    *   If you choose a different name, some of the following steps will need modification.
*   Export the updated plugins and features:
    *   Select the features you want to release in the Package Explorer view.
    *   Choose File -> Export from the menu.
    *   Plug-In Development -> Deployable Features, click Next
    *   Select features to update
    *   Destination Directory should be the UpdateSite project in your workspace.
    *   Turn off "generate repository metadata"
    *   Click Finish
*   Edit your category.xml and add any new or updated features to it, in the correct categories.
*   Generate the artifacts.xml file using the ant build.xml file below with target="artifacts"
*   Generate the content.jar with categories:
*   Launch an eclipse application with the following parameters:
    *   -application org.eclipse.equinox.p2.publisher.CategoryPublisher
    *   -consolelog
    *   -repositoryName "My Update Site"
    *   -metadataRepository file:${project_loc:UpdateSite}
    *   -categoryDefinition file:${project_loc:UpdateSite}/category.xml
    *   -categoryQualifier
    *   -compress

Here is the Ant build file for generating artifacts.xml

    <?xml version="1.0" encoding="UTF-8"?>
    <project name="project" default="artifacts">
      <target name="artifacts">
        <delete file="artifacts.xml"/>
        <delete file="content.xml"/>
        <delete file="artifacts.jar"/>
        <delete file="content.jar"/>
        <p2.publish.featuresAndBundles 
          repository="file:${basedir}" 
          repositoryname="My Update Site"
          source="${basedir}" 
          compress="true" />
      </target>
    </project>

### How do I add files to the root of the installation directory?

You can include [\[1\]](http://help.eclipse.org/indigo/index.jsp?topic=/org.eclipse.pde.doc.user/tasks/pde_rootfiles.htm) with your build.

PDE/Build will take the feature root files and generate p2 artifacts with touchpoint stuff to do the right thing at install time.

