Eclipse4/RCP/FAQ
================

Contents
--------

*   [1 Adopting the Eclipse 4 Application Platform](#Adopting-the-Eclipse-4-Application-Platform)
    *   [1.1 How do the Eclipse 3.x and 4.x programming models differ?](#how-do-the-eclipse-3x-and-4x-programming-models-differ)
        *   [1.1.1 Handlers](#Handlers)
        *   [1.1.2 Parts](#Parts)
        *   [1.1.3 Lazy Instantiantion](#Lazy-Instantiantion)
    *   [1.2 How would I accomplish X in Eclipse 4?](#How-would-I-accomplish-X-in-Eclipse-4)
        *   [1.2.1 Accessing the status line](#Accessing-the-status-line)
        *   [1.2.2 Associating help context with a control](#Associating-help-context-with-a-control)
        *   [1.2.3 Handling errors and exceptions](#Handling-errors-and-exceptions)
        *   [1.2.4 Accessing preference values](#Accessing-preference-values)
        *   [1.2.5 How to I find ressource leaks in plug-ins](#How-to-find-resource-leaks)
*   [2 The E4 Model](#The-E4-Model)
    *   [2.1 What is an _xmi:id_? How is it different from the _elementId_?](#what-is-an-xmiid-how-is-it-different-from-the-elementid)
    *   [2.2 How do I reference an object defined in another .e4xmi?](#how-do-i-reference-an-object-defined-in-another-e4xmi)
    *   [2.3 Are identifiers (elementId) supposed to be unique?](#are-identifiers-elementid-supposed-to-be-unique)
    *   [2.4 How do I use MPlaceholders?](#How-do-I-use-MPlaceholders)
    *   [2.5 How do I create an MPart from an MPartDescriptor?](#How-do-I-create-an-MPart-from-an-MPartDescriptor)
*   [3 Problems on Configuration, Start-Up, and Shutdown](#Problems-on-Configuration.2C-Start-Up.2C-and-Shutdown)
    *   [3.1 Why won't my application start?](#Why-won.27t-my-application-start)
    *   [3.2 I modified my App.e4xmi/fragment.e4xmi but the changes aren't being loaded. Why?](#i-modified-my-appe4xmifragmente4xmi-but-the-changes-arent-being-loaded-why)
    *   [3.3 How can I prevent my workbench model from being saved on exit?](#How-can-I-prevent-my-workbench-model-from-being-saved-on-exit)
*   [4 Dependency Injection & Contexts](#dependency-injection--contexts)
    *   [4.1 Why aren't my @Inject-able/@PostConstruct methods being injected?](#why-arent-my-inject-ablepostconstruct-methods-being-injected)
        *   [4.1.1 Cause #1: Mismatched Annotations](#cause-1-mismatched-annotations)
        *   [4.1.2 Cause #2: Unresolvable Injections](#cause-2-unresolvable-injections)
    *   [4.2 What services are available for injection?](#What-services-are-available-for-injection)
    *   [4.3 How can I override a provided object?](#How-can-I-override-a-provided-object)
    *   [4.4 How do I provide singleton objects?](#How-do-I-provide-singleton-objects)
    *   [4.5 Why am I getting a new instance of an object?](#Why-am-I-getting-a-new-instance-of-an-object)
    *   [4.6 Why is my widget/part not displaying? Why am I getting a new Shell?](#Why-is-my-widget.2Fpart-not-displaying-Why-am-I-getting-a-new-Shell)
    *   [4.7 Why am I being injected with _null_?](#Why-am-I-being-injected-with-null)
    *   [4.8 Why aren't my parts being injected with my value set from my bundle activator?](#Why-aren.27t-my-parts-being-injected-with-my-value-set-from-my-bundle-activator)
    *   [4.9 What is the difference between IEclipseContext#set and IEclipseContext#modify?](#what-is-the-difference-between-ieclipsecontextset-and-ieclipsecontextmodify)
    *   [4.10 Why aren't my _@EventTopic_ or _@UIEventTopic_ methods being called?](#why-arent-my-eventtopic-or-uieventtopic-methods-being-called)
*   [5 Commands and Handlers](#Commands-and-Handlers)
    *   [5.1 Why aren't my handler fields being re-injected?](#why-arent-my-handler-fields-being-re-injected)
    *   [5.2 Why is my parameterized handler not triggered?](#Why-is-my-parameterized-handler-not-triggered)
    *   [5.3 Why does org.eclipse.core.commands.Command's isEnabled() and getHandler() not work?](#why-does-orgeclipsecorecommandscommands-isenabled-and-gethandler-not-work)
*   [6 UI](#UI)
    *   [6.1 How do I enable Drag N Drop (DND) of parts?](#how-do-i-enable-drag-n-drop-dnd-of-parts)
    *   [6.2 Why are my CSS theming not taking effect?](#Why-are-my-CSS-theming-not-taking-effect)
    *   [6.3 Why is my part's selection never set as the active selection?](#why-is-my-parts-selection-never-set-as-the-active-selection)
*   [7 Customizing and Controlling the Platform](#Customizing-and-Controlling-the-Platform)
    *   [7.1 How do I provide my own prompt-to-save when closing a part?](#How-do-I-provide-my-own-prompt-to-save-when-closing-a-part)

Adopting the Eclipse 4 Application Platform
-------------------------------------------

### How do the Eclipse 3.x and 4.x programming models differ?

Conceptually, the models aren't very different. The Eclipse 4 programming model is strongly influenced by the Eclipse 3.x model, but rectifies some of the mistakes that were only realized in hindsight. If you are a proficient Eclipse 3.x RCP developer, then most concepts and approaches will be fairly familiar.

#### Handlers

With E3.x, the handlers were in a flat global namespace. The handler service used activeWhen expressions to choose the most specific handler for the current situation (e.g., active when the activePartId = xxx).

With E4.x, handlers can be installed on parts, windows, as well as globally on the MApplication. Handler look up starts from the active part and proceeds upwards. So many of the uses for the activeWhen expressions disappeared. Enablement expressions are now handled by the handler class itself through @CanExecute methods.

#### Parts

Eclipse 4 has no formal notion of an 'editor', though one could be defined by virtue of the EMF-based model. Eclipse 4 instead distinguishes between a Part and an InputPart. An input part has a URI to provide the input and can also be marked as dirty.

#### Lazy Instantiantion

Most classes referenced by model objects are immediately instantiated on the rendering of the model. This means that bundles contributing to the UI will be invariably activated on startup.

  

### How would I accomplish X in Eclipse 4?

The following snippets show how to access various services from pure E4 components (created using injection). These snippets cannot be used directly from Eclipse 3.x parts using the E4 Compatibility Layer as these parts are not injected.

**Accessing the status line**

| Eclipse 3.x | Eclipse 4.0 |
| --- | --- |
|  <pre>getViewSite()<br>   .getActionsBars()<br>      .getStatusLineManager()<br>         .setMessage(msg);</pre> | <pre>@Inject<br>IStatusLineManager statusLine;<br>...<br>statusLine.setMessage(msg);</pre>|

**Associating help context with a control**

| Eclipse 3.x | Eclipse 4.0 |
| --- | --- |
|  <pre>getSite()<br>   .getWorkbenchWindow()<br>      .getWorkbench()<br>         .getHelpSystem().setHelp(<br>         viewer.getControl(), some_id)</pre> | <pre>@Inject<br>IWorkbenchHelpSystem helpSystem;<br>...<br>helpSystem.setHelp(<br>viewer.getControl(), some_id);</pre>|

**Handling errors and exceptions**

| Eclipse 3.x | Eclipse 4.0 |
| --- | --- |
| <pre>try {<br>   ...<br>} catch (Exception ex) {<br>   IStatus status = new Status(<br>      IStatus.ERROR, "plugin-id",<br>      "Error while ...", ex);<br>   StatusManager.getManager()<br>      .handle(status, StatusManager.SHOW);<br>}</pre>|<pre>@Inject<br>StatusReporter statusReporter;<br>...<br>try{<br>   ...<br>} catch (Exception ex) {<br>   statusReporter.show("Error while ...", ex);<br>}|

**Accessing preference values**
| Eclipse 3.x | Eclipse 4.0 |
| --- | --- |
| <pre>IPreferenceStore store =<br>   IDEWorkbenchPlugin.getDefault()<br>      .getPreferenceStore();<br>boolean saveBeforeBuild = store<br>   .getBoolean(SAVE\_BEFORE\_BUILD);</pre> | <pre>@Inject @Preference(SAVE\_BEFORE\_BUILD)<br>boolean saveBeforeBuild;</pre>  |
| <pre>IPreferenceStore store =<br>   IDEWorkbenchPlugin.getDefault()<br>      .getPreferenceStore();<br>store.putBoolean(SAVE_BEFORE_BUILD, false);</pre>|  <pre>@Inject @Preference<br>IEclipsePreferences prefs;<br>...<br>prefs.setBoolean(SAVE\_BEFORE\_BUILD, false);</pre> |


#### How to find resource leaks

SWT provides a way to trace resource leaks and report them.
For example, if you create an image you you do not dispose it, this is a resource leak, as the native handler for this resource is not released.
If you see the no more handlers exception, you should check for resource leaks.

To active the SWT tracing for resource leaks, add the following as VM parameter to your application.

-Dorg.eclipse.swt.graphics.Resource.reportNonDisposed=true

The E4 Model
------------

### What is an _xmi:id_? How is it different from the _elementId_?

if you look at the contents of an .e4xmi file, you'll see that every object has a unique _xmi:id_ attribute. The _xmi:id_ are unique identifiers are internal to EMF and used for resolving model-level references between objects, similar to a memory reference. The _elementId_s are Eclipse 4 identifiers and serve to provide a well-known name to an object.

Unlike EMF's _xmi:id_ identifiers, which are expected by EMF to be unique, Eclipse 4's _elementId_s do not _necessarily_ have to be unique. For example, it may make sense for every part stack containing editors to have elementId _editor-stack_ (though we might instead recommend using a tag). Some _elementId_s are expected to be unique for particular uses. For example, an _MCommand_s _elementId_ serves as the unique command identifier.

In the following example, notice the (anonymous) handler has a reference to the command instance. The command instance's _elementId_ is used as the Eclipse command identifier.

      <handlers xmi:id="_385TQr5EEeGzleFI7lW1Fg"
          contributorURI="platform:/plugin/org.eclipse.platform"
          contributionURI="bundleclass://org.eclipse.e4.ui.workbench/org.eclipse.e4.ui.internal.workbench.ExitHandler"
          command="_385TTr5EEeGzleFI7lW1Fg"/>
      <commands xmi:id="_385TTr5EEeGzleFI7lW1Fg"
          elementId="e4.exit" 
          contributorURI="platform:/plugin/org.eclipse.platform"
          commandName="%command.name.exit"
          description=""/>

### How do I reference an object defined in another .e4xmi?

Referencing an object defined elsewhere is often necessary, such as to provide an MHandler for a particular MCommand. To reference an object defined in a different .e4xmi you need an _import_, basically creating an alias that is replaced at load-time to the xmi:id of the imported object.

Ensure the object to be imported has a unique elementId, and then do the following steps in the Model Editor:

1.  Open the fragment.e4xmi
2.  Select the "Imports" section from the overview pane on the left.
3.  In the details pane, on the right, select the type of object to be imported and click the "Add" button. This adds and opens the new import in the details pane.
4.  Select the "Default" tab ({bug|384500})
5.  Provide the elementId of the desired object

Note: only model fragments can reference objects defined elsewhere. You cannot have an Application.e4xmi reference an object defined in a fragment.e4xmi.

### Are identifiers (elementId) supposed to be unique?

It depends on the context in which the elementIds are being used. In practice, searches of the model are performed within some scope such as for a particular type of object (e.g., all MBindingContexts) or within some object graph (e.g., the children of an MPerspective).

The E4 model does not require that elementIds be unique. Otherwise every "File" menu in different windows would require a different identifier, which would be very annoying. But each MCommand defined on an MApplication is _expected_ to have a unique identifier. (Note: command-identifier uniqueness is not actually enforced, but could lead to unexpected UI behaviours since there will be multiple possible command objects.) If you are attempting to import an object into a fragment, then it's important that the elementIds are unique for that type.

  

### How do I use MPlaceholders?

### How do I create an MPart from an MPartDescriptor?

Use the EPartService.

Problems on Configuration, Start-Up, and Shutdown
-------------------------------------------------

### Why won't my application start?

E4AP products require having the following plugins:

*   org.apache.felix.scr (> 4.7 Oxygen) or org.eclipse.equinox.ds (4.0 - 4.6) (must be started)
*   org.eclipse.equinox.event (must be started)

Note that org.eclipse.equinox.ds and org.eclipse.equinox.event _must_ be explicitly started. In your product file, you should have a section like the following:

      <configurations>
         <plugin id="org.eclipse.core.runtime" autoStart="true" startLevel="2" />
         <plugin id="org.eclipse.equinox.ds" autoStart="true" startLevel="3" />
         <plugin id="org.eclipse.equinox.event" autoStart="true" startLevel="3" />
      </configurations>
    
or

      <configurations>
         <plugin id="org.eclipse.core.runtime" autoStart="true" startLevel="2" />
         <plugin id="org.apache.felix.scr" autoStart="true" startLevel="3" />
         <plugin id="org.eclipse.equinox.event" autoStart="true" startLevel="3" />
      </configurations>
    
Failure to set the auto-start levels usually manifest as runtime errors like

      Unable to acquire application service. Ensure that the org.eclipse.core.runtime bundle is resolved and started (see config.ini)
    
See also [\[1\]](https://www.eclipse.org/eclipse/news/4.7/platform_isv.php#equinox-ds-felix-scr)

### I modified my App.e4xmi/fragment.e4xmi but the changes aren't being loaded. Why?

The E4 workbench persists the current model on shutdown and, if found, restores that model on startup. Any changes in the Application.e4xmi and previously-loaded fragments will be ignored. If you're debugging, use the -persistState false option to prevent the model from being persisted on shutdown.

### How can I prevent my workbench model from being saved on exit?

You can prevent the workbench from being persisted on shutdown by launching with "-persistState false" or setting the system property "persistState=false".

You can prevent the persisted workbench from being loaded on startup by launching with "-clearPersistedState" or setting the property "clearPersistedState=true"

Dependency Injection & Contexts
-------------------------------

### Why aren't my @Inject-able/@PostConstruct methods being injected?

There are typically two reasons why injection fails.

#### Cause #1: Mismatched Annotations

Note: As of Eclipse Neon (4.6), the advice, to place a package-version on javax.annotation, is no longer required. See [bug 463292](https://bugs.eclipse.org/bugs/show_bug.cgi?id=463292) for details.  

  
Ensure your bundles use Import-Package with a package version to pull in the standard annotations rather than a Require-Bundle on the javax.annotation bundle.

    Import-Package: javax.annotation; version="1.1.0"

Basically the injector is resolving to a different PostConstruct class from your code. You can try "packages javax.annotation" from the OSGi console to see if your bundle is bound to a different package version than org.eclipse.e4.core.di. The reasons behind this are complex (see [bug 348155](https://bugs.eclipse.org/bugs/show_bug.cgi?id=348155) for a long discussion about the issue and the problems with various solutions. A workaround was committed for Kepler SR2 for bundles that require the org.eclipse.core.runtime bundle (see [bug 424274](https://bugs.eclipse.org/bugs/show_bug.cgi?id=424274)). The real solution is for the OSGi Framework to annotate the VM definitions with their respective versions ([bug 348630](https://bugs.eclipse.org/bugs/show_bug.cgi?id=348630)), but it's a hard problem.

  

#### Cause #2: Unresolvable Injections

The injector attempts to resolve objects in the context. If an object cannot be resolved in the context, and it's not marked as @Optional, then the method will not be injected. The injector does not normally log when such situations occur as it is an expected occurrence.

Two approaches to help diagnose such problems:

*   enable tracing on org.eclipse.e4.core.di for debug/injector; as of 4.6 M4, the injector will warn of found annotation mismatches ([bug 482136](https://bugs.eclipse.org/bugs/show_bug.cgi?id=482136));
*   put an exception breakpoint on InjectionException.

### What services are available for injection?

See the [list of provided services](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Eclipse4_RCP_EAS_List_of_All_Provided_Services.md)

### How can I override a provided object?

FIXME For example, to provide an alternative StatusReporter or Logger

### How do I provide singleton objects?

Typical E4AP applications have a single injector, accessible through _org.eclipse.e4.core.di.InjectorFactory#getDefault()_. Within this injector, any class or interface annotated with the _javax.inject.Singleton_ will be treated as a singleton instance.

Another approach is to use a _IContextFunction_ that checks and sets a value in the top context.

FIXME: Can the injector also be configured to bind @Singleton to a particular class?

### Why am I getting a new instance of an object?

\[NB: the injector's behaviour changed as of 4.2M7. After M7, objects are only created if annotated with @Creatable.\]

The injector attempts to resolve objects in the context. If they are not found in the context, but the class exists, then the injector will instantiate and return a new instance _providing_ that its injectable dependencies can be resolved.

This behaviour can be a bit confusing, so let's walk through a somewhat subtle example that frequently causes confusion to new developers with E4 and DI. Consider an E4 RCP app with two MParts, OverviewPart and DetailPart. Since the OverviewPart provides an overview of the contents shown by DetailPart, it needs to get ahold of the DetailPart. A first attempt at writing OverviewPart and DetailPart might be:

    public class OverviewPart {
       @Inject private Composite detail;
       @Inject private DetailPart detail; 
     
       @PostConstruct private void init() { /* ... */ }
    }
     
    public class DetailPart {
       @Inject private Composite detail;
     
       @PostConstruct private void init() { /* ... */ }
    }

If you try to run with this code, it seems to work — but somehow the OverviewPart and DetailPart receive the same Composite! What's wrong?

There are several problems in the code above:

1.  Objects are resolved through the context ancestry, and never through the context tree. Since MParts are siblings, an MPart will never be resolved through injection alone.
2.  Despite their names, neither OverviewPart nor DetailPart are actually MParts. They are the _contributed objects_ of an MPart that implement the behaviour. These contributed object are not available through the injection context.
3.  Even if contributed objects could be injected, there could be several instances in the model. Consider the Eclipse IDE with multiple windows with the _Outline_ view in each.

So how did the code above seem to work? It works because of a subtle feature of the Dependency Injector called _instance autogeneration_ (also supported by Guice). When trying to inject a field or method argument of type T, our DI tries to resolve an object of type T using the provided context. If an object of type T cannot be found, it examines the type T: if it is a concrete class, and either has a 0-argument constructor or a constructor marked with @Inject, it will _autogenerate_ an instance using the provided context.

So the flow looks something like this.

1.  MPart(OverviewPart) is to be rendered. A new IEclipseContext is created hanging off of the MPart(OverviewPart)'s parent.
    1.  DI is requested to create OverviewPart, using the IEclipseContext for MPart(OverviewPart)
    2.  OverviewPart's constructor was called.
    3.  The OverviewPart instance's fields were examined for injection
        1.  DI found the field for DetailPart. It tried to resolve that type in the MPart(OverviewPart)'s context, but nothing was found.
            1.  DI then looked to see if DetailPart was concrete and either had a 0-argument constructor or an @Injectable constructor; it found a 0-argument constructor. DI then created an instance of DetailPart and _began injecting this new DetailPart using MPart(OverviewPart)'s context_. Note that DI did not create a new context for this object!
            2.  DI looked to see if this new DetailPart object had any injectable fields.
                1.  DI found a field of type Composite. DI checked in MPart(OverviewPart)'s context for Composite — and found an instance. But this instance was the Composite for OverviewPart. The field was injected.
            3.  DI then looked for methods of the new object to be injected.
    4.  DI looked for any methods in OverviewPart to be injected.
2.  The OverviewPart object is returned.

(The correct solution is to use the EModelService to find the detail part.)

### Why is my widget/part not displaying? Why am I getting a new Shell?

This type of problem is another symptom of the [DI autogeneration issue](#Why-am-I-getting-a-new-instance-of-an-object), and usually occurs with code like the following:

    class ShowDialogHandler {
     
       @Execute
       private void showDialog(Shell shell) {
          dialog = new Dialog(shell, ...);
       }
    }

As there is no Shell in the DI context, but [Shell](http://help.eclipse.org/indigo/topic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/swt/widgets/Shell.html) has a 0-argument constructor, the DI will create a new Shell for the purpose of injection.

The fix is to annotate the Shell request with "@Named(ACTIVE_SHELL)" to fetch the shell for the active window, as in:

       @Execute
       private void showDialog(@Named(ACTIVE_SHELL) Shell shell) { ... }

This value is set in the context for each window.

### Why am I being injected with _null_?

Typically null values are only injected when an argument or field is marked as @Optional. But a null value will be injected if the value has previously been explicitly set to null in the context.

For example, the valueChanged() method will be injected with null:

    @PostConstruct
    public void init(IEclipseContext context) {
       context.set("value", null);
    }
     
    @Inject
    public void valueChanged(@Named("value") String selection) {
       // ...
    }

### Why aren't my parts being injected with my value set from my bundle activator?

The context obtained using `EclipseContextFactory.getServiceContext(bundleContext)` is completely dissociated from the context created for an application in `E4Application`. If you want to populate a context such that a part can be injected, you either need to use an addon or an `IContextFunction`.

### What is the difference between IEclipseContext#set and IEclipseContext#modify?

    public class MyPart {
        @Inject IEclipseContext context;
     
        ...
     
        private void repositoryChanged(IRepository repository) {
            //  set the variable "repository" in this part's context: the value is only visible to
            // this part and any children
            context.set("repository", repository);
     
            // search up the context stack to see if the variable exists in one of the the context's 
            // ancestors; otherwise it does a set in the specified context
            context.modify("repository", repository);
        }
     
        ...
    }

A placeholder can be made for _#modify_ with _IEclipseContext#declareModifiable()_ or a _<variable>_ declaration in an _Application.e4xmi_.

### Why aren't my _@EventTopic_ or _@UIEventTopic_ methods being called?

This problem usually occurs in testing, where an object is created explictly via injection that would normally be created by the Eclipse 4 framework (e.g., an addon). The event registrations are created when the object is injected and _are removed_ when the object is GC'd. You must ensure your object reference are held somewhere.

Commands and Handlers
---------------------

### Why aren't my handler fields being re-injected?

Handler instances are singleton-ish — that is, only a single instance is created within the Eclipse-4 workbench — and the handler may actually be invoked by different threads in parallel. So the handler instance can’t be re-injected since that may result in field clobbering. Only the method arguments of the @CanExecute and @Execute arguments are injected, since they can't be clobbered with parallel invocations.

### Why is my parameterized handler not triggered?

When binding a command to a UI element (e.g., an _MHandledToolItem_ or _MHandledMenuItem_), the binding must provide a parameter for each of the parameters defined by the command. The attribute names used (as of 4.2M3) for establishing a correspondance between command parameters and handler arguments can lead to some confusion.

The parameters to a command are specified as instances of _MCommandParameter_. The identifier for each parameter, used for matching, is taken from the _elementId_ attribute. The _name_ attribute is a descriptive label for the parameter.

The confusion arises as each _binding_ parameter (an instance of _MParameter_) also have an _elementId_, but it is the _name_ attribute that is used for matching against the command parameters. The binding parameter's _elementId_ merely serves to identify that parameter instance within the model.

For example, consider defining a command to perform a CSS theme switch, where the theme identifier is provided as a command parameter _themeId_. We might configure the command (programmatically) as follows:

    MCommand switchThemeCommand = MCommandsFactory.INSTANCE.createCommand();
    // set the unique command id
    switchThemeCommand.setElementId("command.switchTheme");   
    MCommandParameter themeId = MCommandsFactory.INSTANCE.createCommandParameter();
    themeId.setElementId("themeId");
    themeId.setName("The Theme Identifier");
    themeId.setOptional(false);
    switchThemeCommand.getParameters().add(themeId);
    // make the command known
    app.getCommands().add(switchThemeCommand);

To configure a menu item to trigger a theme switch:

    // somehow find the MCommand definition
    MCommand switchThemeCommand = helper.findCommand("command.switchTheme");
    MMenu themeMenu = ...;
    for(ITheme theme : engine.getThemes()) {
       MHandledMenuItem menuItem = MMenuFactory.INSTANCE.createHandledMenuItem();
       menuItem.setLabel(theme.getLabel());
       menuItem.setCommand(switchThemeCommand);
       menuItem.setContributorURI("platform:/plugin/bundle-name/class-name");
       MParameter parameter = MCommandsFactory.INSTANCE.createParameter();
       // set the identifier for the corresponding command parameter
       parameter.setName("themeId");
       parameter.setValue(themeIdentifier);
       menuItem.getParameters().add(parameter);
       themeMenu.getChildren().add(menuItem);
    }

  

### Why does org.eclipse.core.commands.Command's isEnabled() and getHandler() not work?

`Command`'s `isEnabled()` and `getHandler()` and are specific to Eclipse 3.x based API and are not supported in Eclipse 4. Hence they will always return false or null in Eclipse 4 applications. Applications should use the `EHandlerService` to query against a command.

UI
--

### How do I enable Drag N Drop (DND) of parts?

The DND addon is found in the org.eclipse.e4.ui.workbench.addons.swt plugin. However it requires the compatibility layer and is not available for native E4AP applications.

### Why are my CSS theming not taking effect?

Assuming that you are using the CSS theming through the 'org.eclipse.e4.ui.css.swt.theme _extension point:_

*   Ensure the org.eclipse.e4.ui.css.swt.theme bundle is included with its prereqs
*   Verify that a plugin.xml defines one or more themes on the _org.eclipse.e4.ui.css.swt.theme_ extension point
*   Verify that your product specifies a "cssTheme" property, or is passed "-cssTheme" on the command-line, with a valid theme-id
*   Verify that your product specifies the _applicationCSSResources_ property, a URI prefix to the location containing your CSS resources
    *   A common symptom: your switch-theme handler cannot be injected as _IThemeEngine_ could not be resolved
*   Verify that your CSS files have no errors: add a breakpoint to _org.eclipse.e4.ui.css.core.exceptions.UnsupportedClassCSSPropertyException_, _org.eclipse.e4.ui.css.core.exceptions.DOMExceptionImpl_, and _org.w3c.css.sac.CSSException_

### Why is my part's selection never set as the active selection?

You must ensure that your part implements a method annotated with @Focus, and this method must set the SWT focus to one of your created widgets. Setting the focus to that widget is an essential part for triggering the change of the active selection.

We generally recommend that your part create a container composite in your constructor or @PostConstruct, and this container is the perfect widget to use for your @Focus as Composite.setFocus() traverses its children to restore focus to its child widget that last had focus. You should _not_ use the parent composite provided to your part's constructor/@PostConstruct, though as this composite is (1) not under your control, and (2) may contain other UI elements that should not receive the focus.

Customizing and Controlling the Platform
----------------------------------------

### How do I provide my own prompt-to-save when closing a part?

Put your own custom org.eclipse.e4.ui.workbench.modeling.ISaveHandler implementation somewhere in the part's context hierarchy. Note that this handler is only called if the part is already marked as dirty.

