Platform UI Command Design
==========================

Starting point for menu and toolbar placement of commands in 3.3.
Please contribute comments and suggestions in the discussion area or on [Bug 154130 -KeyBindings- Finish re-work of commands and key bindings](https://bugs.eclipse.org/bugs/show_bug.cgi?id=154130).
Here is a page with concrete example cases: [Menu Item Placement Examples](./Menu_Contributions.md)


Current Framework
=================

See [Platform Command Framework](PlatformCommandFramework.md)

Menus and ToolBars
==================

Menu and toolbar placement is managed by 4 extension points, and through programmatic contributions at a number of locations: IActionBars, IViewSite, IEditorSite, EditorActionBarContributor ... more to follow

I'm not sure of an appropriate way to wrap [org.eclipse.ui.IActionDelegate](https://help.eclipse.org/latest/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/IActionDelegate.html). It is the base class and provides 2 methods to all of the **I*ActionDelegates**.
```java
	public void run(IAction action);
	public void selectionChanged(IAction action, ISelection selection);
```
`run(*)` is the execution method, so that is pretty straight forward.
The `selectionChanged(*)` method is called as the workbench selection changes, often times it updates the IAction enablement ... but moving forward there is no IAction enablement.
However, an IHandler can be a selection listener and update its own enablement state directly.

The current action delegate proxy, ActionDelegateHandlerProxy, creates a bogus IAction. It allows the action delegates to continue working, but it is disconnected from any state.

Of course, there is also IActionDelegate2 :-)

Managing menus through the suggested **org.eclipse.ui.menus** extension maps one menu item to one [org.eclipse.core.commands.ParameterizedCommand](https://help.eclipse.org/latest/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/commands/ParameterizedCommand.html). This contains both the command and appropriate parameters needed to execute it.



Programmatic Contributions and Delegates
----------------------------------------

Contributing menus through IActionBars, EditorActionBarContributor, IWorkbenchPartSite#registerContextMenu(*), etc



### I*ActionDelegate

Each of the IActionDelegates has a slightly different initialization interface. With each execution the IHandler is provided an ExecutionEvent that contains the application context, which will allow the handler to retrieve the information of interest.

Creating an equivalent IHandler for IWorkbenchWindowActionDelegate that has access to the window is straightforward. ex:


```java
	public class SampleAction extends AbstractHandler {
		public Object execute(ExecutionEvent event) throws ExecutionException {
			IWorkbenchWindow window = null;
			ISelection selection = null;

			Object appContextObj = event.getApplicationContext();
			if (appContextObj instanceof IEvaluationContext) {
				IEvaluationContext appContext = (IEvaluationContext) appContextObj;
				window = (IWorkbenchWindow) appContext
						.getVariable(ISources.ACTIVE\_WORKBENCH\_WINDOW_NAME);
				selection = (ISelection) appContext
						.getVariable(ISources.ACTIVE\_CURRENT\_SELECTION_NAME);
			}
			if (window != null) {
				MessageDialog.openInformation(window.getShell(), "Editor Plug-in",
						"Hello, Eclipse world");
			}
			return null;
		}
	}


At the moment, a wrapper for an existing **I*ActionDelegate** is the ActionDelegateHandlerProxy.

Similarly, an IEditorActionDelegate equivalent (same applies to IViewActionDelegate, except it would use the active part) can access the active editor:

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart activeEditor = null;
		ISelection selection = null;
		Object appContextObj = event.getApplicationContext();
		if (appContextObj instanceof IEvaluationContext) {
			IEvaluationContext appContext = (IEvaluationContext) appContextObj;
			activeEditor = (IEditorPart) appContext
					.getVariable(ISources.ACTIVE\_EDITOR\_NAME);
			selection = (ISelection) appContext
					.getVariable(ISources.ACTIVE\_CURRENT\_SELECTION_NAME);
		}
		// ... execute the event.
		return null;
	}


Also note that IHandlers are not handed an IAction, but the IHandler can return its own **isEnabled()** state directly. For Handlers that want to programmatically report their enablement change, they must remember to fire an event.

	public class SampleEnabledHandler extends AbstractHandler {
		private boolean enabled = true;

		private void setEnabled(boolean b) {
			if (enabled != b) {
				enabled = b;
				HandlerEvent handlerEvent = new HandlerEvent(this, true, false);
				fireHandlerChanged(handlerEvent);
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
		 */
		public boolean isEnabled() {
			return enabled;
		}

		public Object execute(ExecutionEvent event) throws ExecutionException {
			IEditorPart activeEditor = null;
			ISelection selection = null;
			Object appContextObj = event.getApplicationContext();
			if (appContextObj instanceof IEvaluationContext) {
				IEvaluationContext appContext = (IEvaluationContext) appContextObj;
				activeEditor = (IEditorPart) appContext
						.getVariable(ISources.ACTIVE\_EDITOR\_NAME);
				selection = (ISelection) appContext
						.getVariable(ISources.ACTIVE\_CURRENT\_SELECTION_NAME);
			}
			// ... execute the event.
			return null;
		}
	}

### IObjectActionDelegate

For the behaviour part of an IObjectActionDelegate, it can simply get the active part on execution similar to the IEditorActionDelegate with `appContext.getVariable(ISources.ACTIVE\_PART\_NAME)`.

Handlers are the implementation of behaviour, and so there is no one to one equivalent of IObjectActionDelegate#setActivePart(IAction action, IWorkbenchPart targetPart). The menu item as displayed in the popup menu can declare visibleWhen in its XML to help control its visibility, but will not be notified just before it is about to show.

### EditorActionBars

These are the editor specific IActionBars.
They add some functionality, but most specifically the ability to add two IEditorActionBarContributors.
The editor action bar contributor and the extension action bar contributor (which is an ExternalContributor provided by the EditorActionBuilder).

One of the advantages of EditorActionBars is the lifecycle of the actions that are added.
They're created when the first editor of that type is loaded and exist until the last editor of that type is closed.

### PluginActionBuilder subclasses

**PluginActionBuilder** is the base class for reading the 4 current action extension points. The subclasses are:

*   PluginActionSetBuilder - for org.eclipse.ui.actionSets, it creates an ActionSetContribution and the ActionDescriptors with ActionDescriptor.T\_WORKBENCH or ActionDescriptor.T\_WORKBENCH_PULLDOWN. It fills in each PluginActionSet provided by the ActionSetRegistry. Each PluginActionSet provides the IActionBars.
*   EditorActionBuilder - for org.eclipse.ui.editorActions, it creates an EditorContribution and ActionDescriptors with ActionDescriptor.T_EDITOR. The EditorContributions are added to an ExternalContributor. These are added to the [EditorActionBars](#EditorActionBars).
*   ViewActionBuilder - for org.eclipse.ui.viewActions, it creates a BasicContribution and the ActionDescriptors with ActionDescriptor.T_VIEW. The BasicContributions are added to the IViewSite IActionBars.
*   ViewerActionBuilder - this is part of the org.eclipse.ui.popupMenus extension point. This is used after IPartSite#registerContextMenu(\*) creates a PopupMenuExtender. It creates a ViewerContribution and stores ActionDescriptors with ActionDescriptor.T\_EDITOR or ActionDescriptor.T\_VIEW. It's contributed to the popup menu by PopupMenuExtender#menuAboutToShow(\*) - viewerContributions are called "static" contributions.
*   ObjectActionContributor - also for org.eclipse.ui.popupMenus, it stores the ActionDescriptions of type ActionDescriptor.T_POPUP in an ObjectContribution. This goes back to the ObjectActionContributorManager, which is called into from PopupMenuExtender#menuAboutToShow(\*).



org.eclipse.ui.actionSets
-------------------------

Action Sets are visible in the main menu and coolbar.
Their visibility can be updated by the user using **Customize Perspective**.
Here is a sample actionSet distributed with Eclipse SDK.

	<extension
	      point="org.eclipse.ui.actionSets">
	   <actionSet
	         id="z.ex.editor.actionSet"
	         label="Sample Action Set"
	         visible="true">
	      <menu
	            id="sampleMenu"
	            label="Sample &amp;Menu">
	         <separator
	               name="sampleGroup">
	         </separator>
	      </menu>
	      <action
	            class="z.ex.editor.actions.SampleAction"
	            icon="icons/sample.gif"
	            id="z.ex.editor.actions.SampleAction"
	            label="&amp;Sample Action"
	            menubarPath="sampleMenu/sampleGroup"
	            toolbarPath="sampleGroup"
	            tooltip="Hello, Eclipse world">
	      </action>
	   </actionSet>
	</extension>

The `<actionSet/>` element defines the group of elements that can be shown or hidden.
The `<menu/>` elements create menus and groups.
The `<action/>` elements define individual "actions" ... they contain rendering information (label, icon), menu placement (menubarPath, toolbarPath) and behaviour (the class attribute, in this case an IWorkbenchWindowActionDelegate).

org.eclipse.ui.editorActions
----------------------------

Editor Actions appear in the main menu and coolbar as long as an editor of that type is the active editor.
Using the org.eclipse.ui.actionSetPartAssociation extension with an org.eclipse.ui.actionSet works similarly, except all editor actions are IEditorActionDelegates instead of IWorkbenchWindowActionDelegates.

Here is an action example adapted as an editor action:

	<extension
	      point="org.eclipse.ui.editorActions">
	   <editorContribution
	         id="z.ex.editor.actions"
	         targetID="z.ex.view.keybindings.editors.XMLEditor">
	      <menu
	            id="sampleMenu"
	            label="Sample &amp;Menu">
	         <separator
	               name="sampleGroup">
	         </separator>
	      </menu>
	      <action
	            class="z.ex.view.keybindings.actions.SampleEditorAction"
	            icon="icons/sample.gif"
	            id="z.ex.view.keybindings.actions.SampleEditorAction"
	            label="Sample &amp;Editor Action"
	            menubarPath="sampleMenu/sampleGroup"
	            toolbarPath="sampleGroup"
	            tooltip="Hello, Eclipse world">
	      </action>
	   </editorContribution>
	</extension>


The `<editorContribution/>` element ties the editor action to a specific editor type.
Other than that, it is almost identical to org.eclipse.ui.actionSets.

org.eclipse.ui.viewActions
--------------------------

View actions are placed in the view menu or view toolbar, but the extension point looks almost identical to org.eclipse.ui.editorActions.
The delegate for views is IViewActionDelegate.

	<extension
	      point="org.eclipse.ui.viewActions">
	   <viewContribution
	         id="z.ex.view.keybindings.viewAction"
	         targetID="z.ex.view.keybindings.views.SampleView">
	       <menu
	            id="sampleMenu"
	            label="Sample &amp;Menu">
	         <separator
	               name="sampleGroup">
	         </separator>
	      </menu>
	      <action
	            class="z.ex.view.keybindings.actions.SampleViewAction"
	            icon="icons/sample.gif"
	            id="z.ex.view.keybindings.actions.SampleViewAction"
	            label="Sample &amp;View Action"
	            menubarPath="sampleMenu/sampleGroup"
	            toolbarPath="sampleGroup"
	            tooltip="Hello, Eclipse world">
	      </action>
	   </viewContribution>
	</extension>

Here, the Sample Menu will show up in the view menu, the dropdown from the top of the view's CTabFolder.

org.eclipse.ui.popupMenus
-------------------------

Popup menu contributions are actions contributed to the various popup menus in eclipse. They take 2 forms. A <viewerContribution/> contributes actions to a popup in a view or an editor. An <objectContribution/> contributes actions to any popup, as long as the selected object matches its enablement criteria.

Here is an example of each:

	<extension
	      point="org.eclipse.ui.popupMenus">
	   <objectContribution
	         id="z.ex.popup.objectContribution"
	         objectClass="org.eclipse.ui.handlers.IHandlerActivation">
	      <action
	            class="z.ex.view.keybindings.actions.SampleContributionAction"
	            icon="icons/sample.gif"
	            id="z.ex.view.keybindings.actions.SampleContributionAction"
	            label="Sample &Contribution Action"
	            menubarPath="additions"
	            tooltip="Hello, Eclipse world">
	      </action>
	   </objectContribution>
	   <viewerContribution
	         id="z.ex.popup.viewerContribution"
	         targetID="z.ex.view.keybindings.editors.XMLEditor">
	      <action
	            class="z.ex.view.keybindings.actions.SampleViewerAction"
	            icon="icons/sample.gif"
	            id="z.ex.view.keybindings.actions.SampleViewerAction"
	            label="Sample V&iewer Action"
	            menubarPath="#Ruler"
	            tooltip="Hello, Eclipse world">
	      </action>
	   </viewerContribution>
	</extension>

Framework Enhancements for 3.3
==============================

Quick list of code issues to be addressed in 3.3.

Issues to Address
-----------------

### Issue 101 - PluginActions disconnected from Handlers

Menus and toolbars point to PluginActions, but the keybindings have an ActionDelegateHandlerProxy that is disconnected from the original action. See [Bug 151612 -KeyBindings- keybinding not enabled even though actions appear enabled in menus](https://bugs.eclipse.org/bugs/show_bug.cgi?id=151612)

Does this become an issue if we replace our menuing abstraction?

### Issue 102 - Commands implementation of label changing

Commands are an abstraction of behaviour, an have a lot in common with RetargetableActions. Certain kinds of RetargetableActions provide the ability to switch the labels, like switching **Undo** to **Redo**. This can be accomodated by adding text state to the command:

	<extension
	      point="org.eclipse.ui.commands">
	   <command
	         description="A test case for a command with state"
	         id="org.eclipse.ui.tests.commandWithState"
	         name="Command Wtih State">
	      <state
	            class="org.eclipse.jface.menus.TextState"
	            id="NAME"/>
	   </command>
	</extension>

Currently, command states that have meaning are in [org.eclipse.jface.menus.IMenuStateIds](https://help.eclipse.org/latest/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/menus/IMenuStateIds.html) and [org.eclipse.core.commands.INamedHandleStateIds](https://help.eclipse.org/latest/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/commands/INamedHandleStateIds.html).

The NAME state can be updated, and also supports state change notification. Our menu and/or toolbar rendering would have to listen for changes to properties and states associated with commands?

States are also the proposed way to handle information like toggled state (for check boxes or toggle buttons) and radio state (for a group of radio buttons).



### Issue 103 - Menu items map to ParameterizedCommands

Just like a keybinding, each menu item or toolbar button would map to a ParameterizedCommand. If there was no parameters involved, then just containing the command id would be acceptable. For example, if you were creating menu items for _Show Console View_ and _Show Problems View_ each menu item would map to the ParameterizedCommand.

`IHandlerService#executeCommand(String commandId, Event event)` can execute a normal command with a selection event, and `IHandlerSerivce#executeCommand(ParameterizedCommand command, Event event)` does the same with a parameterized command.

### Issue 104 - Dynamic menus need a good story

Dynamic menus need to work correctly for menus and menu items. They also need to create the drop-downs for toolbar items.

If the top part of a dynamic menu is declarative, then it can be provided without loading the plugin. Then a callback could be specified.

It seems right now that dynamic menus are mostly used to populate pull-down actions.

The appropriate IAction returns an IMenuCreator. This means that it must be instanitated inside the IAction.

Menu Proposal 1
---------------

Replace the entire thing with something really simple (haha).
This should most closely resemble the design described in the original RFC in [Historical Information Section](#Historical-Information).

Menu placements would be controlled by a tree of data.

*   File (menu)
    *   New (menu)
        *   (group: new.wizard)
        *   New Wizard (item)
    *   Open File... (item)
    *   (group: file.close)
    *   Close (item)
    *   Close All (item)
*   Edit (menu)
    *   Undo (item)
    *   Redo (item)

All nodes in the tree would have visibility and enablement state. Enablement for the menu node would be tied to its Command.

The each menu item would be able to specify some order constraints.

Dynamic menus would have a simpler form. They would also be included in the declarative extension point (as much as possible) [See Issue 104](#Issue-104---Dynamic-menus-need-a-good-story).

There would be a product level visibility (similar to the way Customize Perspective works for ActionSets).
Would we also provide product level ability to customize other prospective menu item attributes? For example, a product level way to override menu order?


This would mean an out-and-out replacement of most of the internal classes to do with PluginActions and the ActionSetRegistry.
For RCP support, we would continue to allow MenuManager and contribution items to be added, but those structures would need to be used to populate the tree structures. This is hard :-)


The new system would have to manage the interaction between 4 properties: Enabled, Active, Visible, Showing. Enabled is the state of the command (from the active handler), Active is the state of the handler, Visible is the state of the menu item, and Showing (and internal state) describes if the menu item can currently be seen by the user. Showing is a new concept designed to help with lazy loading and lazy creation of sub-menus. It's not in the current menu behaviour.

We would have to provide a visibility engine of somekind that would update the menu item structure visibility as state changed.

From an menu visibility/enablement point of view, Enabled is the state that matters.
If no handler is active, Enabled will be false. If a handler is active, the Enabled will be the handler Enabled state.

Menu Proposal 2
---------------

We need a relatively straight forward model to represent our menu and toolbar structures.
But JFace already provides decent coverage for that using MenuManager, ToolBarManager, and IContributionItems.
As part of providing our rendering, we would be changing from ActionContributionItems that are currently used to some kind of CommandContributionItem.

IActions would still be used (mostly), but really our IAction would be the focal point of the rendering data model.
For example, we'll provide a `CommandBridgeAction`.
Knowledge of the command will allow the `CommandBridgeAction` to provide label, description, icons (from the ICommandImageService).
Its `runWithEvent(*)` method will simply be `handlerService.executeCommand("commandId", event)`.

As an alternative option, we could skip IActions and just use the CommandContributionItem.
Why build an IAction as the intermediary when the CommandContributionItem can fetch things like the label, icon, etc for us.


We would still have to deal with [Issue 104](#Issue-104---Dynamic-menus-need-a-good-story) and improve dynamic menu creation.

Perhaps IContributionManagerOverrides could be enhanced and used as the _product override_ mechanism.
There would have to be some API to update and list the product level list of overrides.

We would get rid of the PluginAction and ActionSet code.

We would need to provide a "visibility" engine to update the visibility of contribution items or IActions as the sources changed.

This still needs a good story for replacing EditorActionBarContributor and the IActionBar stuff ... providing the same functionality in an alternate place.
I don't think providing the functionality is too much of a problem, but finding the correct place for it will require more thought.



### Proposal "A" (Eric's World...;-):

The basic idea is to provide a single menu/action/command(+binding)/handler 'best practices' path.
We can do this by re-using the existing extension points; tweaking as necessary to provide complete functionality and deprecating EP's that are no longer necessary.

1.  Move the current 'ActionSet.action' and 'ActionSet.menu' out from under the ActionSet extension point and make them their own EPs. This allows the definition of actions and menu structure independent of any particular action set and/or each other.
2.  Standardize on core expressions for vis and enablement states. It turns out that this mechanism is already capable of emulating many behaviours that can currently be accessed through the use of specialized EPs.
3.  Allow for the definition of the IWorkbenchWindowPulldownDelegate2 for any sub-menu to handle the dynamic menus case.
4.  Extend the 'menuBarPath' & 'tooBarPath' to allow for new conventions specific to siting an action anywhere within the workbench. This should allow us to deprecate View/Editor Actions -and- ObjectContributions.

*   EditorContributions:
	*   `editor: <editorId>[/groupPath]`

*   ViewContributions:
	*   `view-context:<viewId>[/groupPath]` - Site it in the given view's context menu
	*   `view-chevron:<viewId>[/groupPath]` - Site it in the given view's chevron menu \[in the given group\]

*   "any context menu" - Replacement for the current ObjectContribution mechanism. The 'visibleWhen' core expression can determine whether the selection context is appropriate...



#### Proposal "A" - item 3 - Dynamic Menu interface

The menu extension can declaratively specify that the menu item is a dynamic menu, and provide a dynamic menu callback class that implements `IDynamicMenu`. When the menu item is about to show, the callback class will be handed an `IMenuCollection`, which contains a modifiable list of menu elements. As an example:

	public interface IDynamicMenu {
		/**
		 * Called just before the given menu is about to show. This allows the
		 * implementor of this interface to modify the list of menu elements before
		 * the menu is actually shown.
		 *
		 * @param menu
		 *            The menu that is about to show. This value is never
		 *            null.
		 */
		public void aboutToShow(IMenuCollection menu);
	}

And the `IMenuCollection` allows the modification of the menu that's about to show.
Assume that `MenuElement` is the new flavour of `IContributionItem`/`IContributionManager`

	public interface IMenuCollection {
		/**
		 * Appends a menu element to the end of the collection.
		 *
		 * @param element
		 *            The element to append. Must not be null, and
		 *            must be of the appropriate type for the type of collection.
		 */
		public void add(MenuElement element);

		/**
		 * Adds a menu element at the given index.
		 *
		 * @param index
		 *            The index at which to insert.
		 * @param element
		 *            The element to append. Must not be null, and
		 *            must be of the appropriate type for the type of collection.
		 */
		public void add(int index, MenuElement element);

		/**
		 * Removes all elements from the collection.
		 */
		public void clear();

		/**
		 * Gets the element at a given index.
		 *
		 * @param index
		 *            The index at which to retrieve the element.
		 * @return The element at the index.
		 */
		public MenuElement get(int index);

		/**
		 * Removes the element at a given index.
		 *
		 * @param index
		 *            The index at which to remove the element.
		 * @return The element that has been removed.
		 */
		public MenuElement remove(int index);

		/**
		 * Removes the given menu element, if it exists.
		 *
		 * @param element
		 *            The element to remove.
		 * @return true if the object was removed; false if it could not be found.
		 */
		public boolean remove(MenuElement element);

		/**
		 * Returns the number of elements in the collection.
		 *
		 * @return The size of the collection.
		 */
		public int size();
	}

This is a change from the `IMenuCreator` interface, which directly references an SWT Control or SWT Menu.

### Menu Proposal 2 UseCases

Visibility controls if the user can see the menu item in the menubar or toolbar or context menu.
There are 3 levels of visibility that are checked in order:

1.  Overrides
2.  Visibility expression like <visibleWhen/> or the Expression used in IMenuService#contribute(*)
3.  Programmatic visibility property 1

If the check at a level returns false, the item won't be visible. If it returns true the next level of visibility is evaluated.

\[1\] I'm not sure if a programmatic level of visibility is warranted. The counter example is enablement, which can be changed through Overrides, the `<enabledWhen/>` expression on the handler, or programmatically from the handler isEnabled() method.

#### Menu Placement Locations

We'll support 7 **root** types to start with:

1.  SBar.MENU

    The main menu.

2.  SBar.TOOLBAR

    The main toolbar.

3.  SBar.VIEW_MENU

    A view menu. The first path segment will be the view id.

4.  SBar.VIEW_TOOLBAR

    A view toolbar. The first path segment will be the view id.

5.  SBar.CONTEXT_MENU

    A context menu. The first path segment will be the context menu id, or `org.eclipse.ui.menus.context.any` to apply to all context menus

6.  SBar.TRIM

    Contribute a piece of trim. This is already implemented

7.  SBar.STATUS

    Contribute information to the status line manager ... I'm not sure about this one, is it no longer necessary since they can contribute trim?


For describing menu locations as strings, we have a couple of options.

**Option 1: URI**

All of our paths can be generalized to a URI that looks like `type://id/path`.

*   The main File menu extention group marker: `menu://org.eclipse.ui.menu.main/file/file.ext`
*   The save toolbar: `toolbar://org.eclipse.ui.toolbar.main/save.group`
*   The resource navigator menu additions group marker: `menu://org.eclipse.ui.views.ResourceNavigator/additions`
*   The resource navigator toolbar: `toolbar://org.eclipse.ui.views.ResourceNavigator`
*   The text editor context menu additions group marker: `popup://#EditorContext/additions`
*   An object contribution additions group marker: `popup://org.eclipse.ui.menu.any/additions`

**Option 2: A rooted Path**

Just use paths of the form `/type/id/path`. They'll be IPath elements within eclipse.

*   The main File menu extention group marker: `/menu/org.eclipse.ui.menu.main/file/file.ext`
*   The save toolbar: `/toolbar/org.eclipse.ui.toolbar.main/save.group`
*   The resource navigator menu additions group marker: `/menu/org.eclipse.ui.views.ResourceNavigator/additions`
*   The resource navigator toolbar: `/toolbar/org.eclipse.ui.views.ResourceNavigator`
*   The text editor context menu additions group marker: `/popup/#EditorContext/additions`
*   An object contribution additions group marker: `/popup/org.eclipse.ui.menu.any/additions`


The usecases are being moved to [Menu Item Placement Examples](./Menu_Contributions.md)

Menu Proposal 3
---------------

In this proposal we don't seek to address most of what the RFC is talking about.
Leave dynamic menus alone. The current extension points are fine, they just need to be backed by a single implementation.

It would involve cleaning up some of the workbench code, and wiring up the underlying framework like in [Issue 101](#Issue-101---PluginActions-disconnected-from-Handlers).

We would also go through all of the programmatic code in the workbench and make sure we're creating IActions backed by commands.

Historical Information
======================

There are discussions in a number of places:

*   [Bug 20298 -Contributions- updating: MenuManager remains disabled after adding items to it](https://bugs.eclipse.org/bugs/show_bug.cgi?id=20298)
*   [Bug 26593 -Contributions- (dynamic) Support for showing and hiding dynamic menus](https://bugs.eclipse.org/bugs/show_bug.cgi?id=26593)
*   [Bug 27019 -Contributions- updating: Hide or disable submenu if empty](https://bugs.eclipse.org/bugs/show_bug.cgi?id=27019)
*   [Bug 30423 -Contributions- updating: MenuManager#updateMenuItem() incorrect](https://bugs.eclipse.org/bugs/show_bug.cgi?id=30423)
*   [Bug 36968 -Contributions- Improve action contributions](https://bugs.eclipse.org/bugs/show_bug.cgi?id=36968)
*   [Bug 80725 -Contributions- -RCP- Allow action sets to be shown when no perspective open](https://bugs.eclipse.org/bugs/show_bug.cgi?id=80725)



Original Requirements
---------------------

1.  Provide a single concept for contributing to the workbench. Right now, there are two distinct ontologies: actions and contribution items; and commands and handlers.
2.  Support the addition and removal of plug-ins.
3.  Separate model and behaviour from visual presentation. Adhere more closely to the Model-View-Controller pattern. Model and user interface separation.
4.  Extensibility. Every group of items in the user interface (e.g., menu, tool bar, etc.) should be extensible – both in structure and content.
5.  Universal keyboard shortcuts. A user should be able to add a keyboard shortcut to any item that appears in the user interface (e.g., menu item, tool item, menu, etc.).
6.  Separation of structure and content. The structure of the menus (e.g., groups) should be defined independently from the items.
7.  No implicit declarations of structure or content. Everything should be explicit.
8.  Fine-grained control over visibility.
9.  More intelligent updating of elements within the user interface. Support for lazy updating for elements that are not showing within the user interface. This lazy updating should be handled automatically – without the elements needing to understand whether they are showing.
10.  Improved control over menu definition and item ordering. This will affect the “Search” and “Run” menus.
11.  The selection should be capable of overriding the behaviour of a user action. For example, if a Java element is selected in the Resource Navigator, a rename should be a refactoring rename.
12.  Address the difficulty in determining the keyboard shortcuts to show for context menu items.
13.  Support dynamic entries in top-level menus. For example, the recently opened files in the “File” menu should be possible using only public API.
14.  There should be an easy way to define the default behaviour in response to a user action (i.e., default handler for a command).
15.  Provide localized control of the model, view and controller elements talked about in this proposal. This includes such concepts as automatic addition/removal as parts are become active/inactive, and automatic removal as parts are destroyed.
16.  Allow the same user interface element to be placed in multiple locations. Reduce duplication in the syntax, and try to reduce memory usage.
17.  Provide facilities for finding and triggering elements within the user interface. This is intended to provide better support for the welcome facilities, cheat sheets, macros and scripting.
18.  JFace must not lose functionality. Everything that can be accomplished with JFace must still be possible in JFace, even if the API changes radically. Similarly, everything that can be accomplished with the workbench must still be possible in the workbench.
19.  Contribute all of the workbench and IDE model, view and controller elements using the API from this proposal. Everything that the workbench and IDE can do should be possible for third-party plug-ins as well.
20.  Contributing arbitrary controls (e.g., combo boxes) to Eclipse, where appropriate.

Rational
--------

The Eclipse Platform has always provided a mechanism for contributing items to the menus and tool bars in Eclipse.
This mechanism has – up until now – been based on instances of IAction.

Actions suffered from a few key deficiencies.
First of all, the interaction with the application model (e.g., the handling of the run method) was tightly coupled with its presentation elements (e.g., icon, label, etc.). Also, there was no easy way to provide user-configurable keyboard shortcuts. Actions were not initially designed with a way to identify two actions as sharing the same semantic behaviour. To further confuse matters, there were action delegates. Action delegates were not actions, but could handle action behaviour in some circumstances.

Actions were defined in XML using several extension points. This XML syntax had several problems. First of all, there were too many extension points, which made the syntax hard to learn and caused maintenance problems. Features added to one extension point, would have to be copied into other extension points. Ultimately, what ended up happening is that for any given feature, it was possible that only a subset of the extension points would actually support it (e.g., dynamic menus). Partly due to this and partly due to the tight coupling mentioned above, this lead to an overly verbose syntax containing duplicate XML elements. If an action was required in a view menu and in a context menu, then the XML would need to be copied and contributed to two different extension points. This also led to multiple instances of the action in memory.

Aside from these main points, there are handful of other significant problems we hope to address – either directly or indirectly. These include dynamic menus, ordering of contribution items, performance problems, and better macro and instrumentation support.

