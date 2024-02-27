Eclipse4/RCP/EAS/List of All Provided Services
==============================================

This page provides a listing of the services and other data values that can be injected or obtained from a [context](https://github.com/eclipse-platform/eclipse.platform.ui/blob/master/docs/Eclipse4_RCP_Contexts.md).

Contents
--------

*   [1 Application Context](#Application-Context)
    *   [1.1 Application Parameters](#Application-Parameters)
    *   [1.2 Services](#Services)
    *   [1.3 Runtime Data](#Runtime-Data)
*   [2 Top Level Window Context](#Top-Level-Window-Context)
    *   [2.1 Model Info](#Model-Info)
    *   [2.2 Services](#Services-2)
    *   [2.3 Runtime Data](#Runtime-Data-2)
*   [3 Part Context](#Part-Context)
    *   [3.1 Model Info](#Model-Info-2)
    *   [3.2 Services](#Services-3)
*   [4 Handler Execution](#Handler-Execution)
*   [5 Eclipse 3.x Compatibility Layer](#Eclipse-3x-Compatibility-Layer)
    *   [5.1 Application Context](#Application-Context-2)
        *   [5.1.1 Application Parameters](#Application-Parameters-2)
        *   [5.1.2 Model Info](#Model-Info-3)
        *   [5.1.3 Services](#Services-4)
        *   [5.1.4 Runtime Data](#Runtime-Data-3)
    *   [5.2 Top Level Window Context](#Top-Level-Window-Context-2)
        *   [5.2.1 Model Info](#Model-Info-4)
        *   [5.2.2 Services](#Services-5)
        *   [5.2.3 Runtime Data](#Runtime-Data-4)
    *   [5.3 Part Context](#Part-Context-2)
        *   [5.3.1 Model Info](#Model-Info-5)
        *   [5.3.2 Services](#Services-6)

Application Context
-------------------

### Application Parameters

*   applicationCSS (E4Workbench#CSS\_URI\_ARG)
*   applicationCSSResources (E4Workbench#CSS\_RESOURCE\_URI_ARG)
*   applicationXMI (E4Workbench#XMI\_URI\_ARG)
*   clearPersistedState (E4Workbench#CLEAR\_PERSISTED\_STATE)
*   deltaRestore (E4Workbench#DELTA_RESTORE)
*   cssTheme (E4Application#THEME_ID)
*   initialWorkbenchModelURI (E4Workbench#INITIAL\_WORKBENCH\_MODEL_URI)
*   instanceLocation (E4Workbench#INSTANCE_LOCATION)
*   persistState (E4Workbench#PERSIST_STATE)

### Services

*   org.eclipse.core.databinding.observable.Realm
*   org.eclipse.core.runtime.dynamichelpers.IExtensionTracker
*   org.eclipse.core.runtime.IExtensionRegistry
*   org.eclipse.core.runtime.Platform
*   org.eclipse.e4.core.commands.ECommandService
*   org.eclipse.e4.core.commands.EHandlerService
*   org.eclipse.e4.core.services.adapter.Adapter
*   org.eclipse.e4.core.services.events.IEventBroker
*   org.eclipse.e4.core.services.log.Logger
*   org.eclipse.e4.core.services.translation.TranslationService
*   org.eclipse.e4.ui.css.swt.theme.IThemeEngine
*   org.eclipse.e4.ui.services.IStylingEngine
*   org.eclipse.e4.ui.workbench.IPresentationEngine
*   org.eclipse.e4.ui.workbench.IResourceUtilities
*   org.eclipse.e4.ui.workbench.modeling.EModelService
*   org.eclipse.equinox.app.IApplicationContext
*   org.eclipse.core.runtime.preferences.IEclipsePreferences (requires @Preference annotation)
*   org.eclipse.ui.ISharedImages
*   org.eclipse.ui.progress.IProgressService
*   org.eclipse.e4.ui.services.help.EHelpService

### Runtime Data

*   activePart (IServiceConstants#ACTIVE_PART)
*   org.eclipse.e4.core.locale (TranslationService#LOCALE)
*   org.eclipse.e4.ui.model.application.MApplication
*   selection (ESelectionService#SELECTION)
*   org.eclipse.swt.widgets.Display

Top Level Window Context
------------------------

### Model Info

When a context is created for any MContext element all of its implemented interfaces are added to its context

*   org.eclipse.e4.ui.model.application.commands.MBindings
*   org.eclipse.e4.ui.model.application.commands.MHandlerContainer
*   org.eclipse.e4.ui.model.application.MApplicationElement
*   org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow
*   org.eclipse.e4.ui.model.application.ui.basic.MWindow
*   org.eclipse.e4.ui.model.application.ui.MContext
*   org.eclipse.e4.ui.model.application.ui.MElementContainer
*   org.eclipse.e4.ui.model.application.ui.MUIElement
*   org.eclipse.e4.ui.model.application.ui.MUILabel

### Services

*   org.eclipse.e4.ui.workbench.modeling.ESelectionService
*   org.eclipse.e4.ui.workbench.modeling.ISaveHandler
*   org.eclipse.e4.ui.workbench.modeling.EPartService

### Runtime Data

*   activePart (IServiceCOnstants#ACTIVE_PART)
*   selection (ESelectionService#SELECTION)

Part Context
------------

### Model Info

*   org.eclipse.e4.ui.model.application.commands.MBindings
*   org.eclipse.e4.ui.model.application.commands.MHandlerContainer
*   org.eclipse.e4.ui.model.application.MApplicationElement
*   org.eclipse.e4.ui.model.application.MContribution
*   org.eclipse.e4.ui.model.application.ui.basic.MPart
*   org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement
*   org.eclipse.e4.ui.model.application.ui.basic.MStackElement
*   org.eclipse.e4.ui.model.application.ui.basic.MWindowElement
*   org.eclipse.e4.ui.model.application.ui.MContext
*   org.eclipse.e4.ui.model.application.ui.MDirtyable
*   org.eclipse.e4.ui.model.application.ui.MUIElement
*   org.eclipse.e4.ui.model.application.ui.MUILabel

### Services

*   org.eclipse.e4.ui.workbench.modeling.EPartService

Handler Execution
-----------------

*   org.eclipse.swt.Event: the SWT event that triggered the handler (if any)
*   org.eclipse.core.commands.ParameterizedCommand
*   the named parameters as defined by the command

  

Eclipse 3.x Compatibility Layer
===============================

Application Context
-------------------

### Application Parameters

### Model Info

*   org.eclipse.ui.IWorkbench

### Services

*   org.eclipse.core.runtime.Platform
*   org.eclipse.core.commands.CommandManager
*   org.eclipse.core.commands.contexts.ContextManager
*   org.eclipse.core.runtime.dynamichelpers.IExtensionTracker
*   org.eclipse.ui.activities.IWorkbenchActivitySupport
*   org.eclipse.ui.progress.IProgressService
*   org.eclipse.ui.commands.ICommandService
*   org.eclipse.ui.commands.ICommandImageService
*   org.eclipse.jface.preference.PreferenceManager
*   org.eclipse.ui.keys.IBindingService
*   org.eclipse.ui.model.IContributionService
*   org.eclipse.ui.menus.IMenuService
*   org.eclipse.ui.services.IEvaluationService
*   org.eclipse.ui.services.ISourceProviderService
*   org.eclipse.ui.swt.IFocusService
*   org.eclipse.ui.ISaveablesLifecycleListener
*   org.eclipse.jface.bindings.BindingManager
*   org.eclipse.ui.internal.services.IWorkbenchLocationService

  

### Runtime Data

*   ISources.ACTIVE\_WORKBENCH\_WINDOW_NAME ("activeWorkbenchWindow")
*   ISources.ACTIVE\_WORKBENCH\_WINDOW\_SHELL\_NAME ("activeWorkbenchWindowShell")
*   org.eclipse.e4.ui.workbench.IPresentationEngine.ANIMATIONS_ENABLED ("Animations Enabled")

Top Level Window Context
------------------------

### Model Info

*   org.eclipse.ui.IWorkbenchWindow
*   org.eclipse.ui.IWorkbenchPage

### Services

*   org.eclipse.ui.IPageService
*   org.eclipse.ui.IPartService
*   org.eclipse.ui.ISelectionService

### Runtime Data

Part Context
------------

### Model Info

*   The corresponding org.eclipse.ui.IViewPart or org.eclipse.ui.IEditorPart
*   org.eclipse.ui.IWorkbenchPartSite
*   The corresponding org.eclipse.ui.internal.ViewReference or org.eclipse.ui.internal.EditorReference

### Services

*   org.eclipse.ui.contexts.IContextService
*   org.eclipse.ui.handlers.IHandlerService
*   org.eclipse.ui.progress.IWorkbenchSiteProgressService
*   org.eclipse.ui.dnd.IDragAndDropService (editors only)

