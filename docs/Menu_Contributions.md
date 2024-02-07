

Menu Contributions
==================

Placement examples that describe the proposed new way of placing menu items for **3.3**. Please contribute comments and suggestions in the discussion area or on [Bug 154130 -KeyBindings- Finish re-work of commands and key bindings](https://bugs.eclipse.org/bugs/show_bug.cgi?id=154130).

Contents
--------

*   [1 Placement and visibility](#Placement-and-visibility)
*   [2 Example Matrix](#Example-Matrix)
*   [3 Menu XML](#Menu-XML)
    *   [3.1 Declarative menus - some constraints](#Declarative-menus---some-constraints)
    *   [3.2 Menu URIs](#Menu-URIs)
        *   [3.2.1 menu:](#menu)
        *   [3.2.2 toolbar:](#toolbar)
        *   [3.2.3 popup:](#popup)
    *   [3.3 Using Expressions in <visibleWhen/>](#Uusing-expressions-in-visiblewhen)
    *   [3.4 Ideas that were considered but not implemented](#Ideas-that-were-considered-but-not-implemented)
        *   [3.4.1 Menu - JSR198](#Menu---JSR198)
        *   [3.4.2 Menu - XUL](#Menu---XUL)
        *   [3.4.3 Expression Templates original suggestion](#Expression-Templates-original-suggestion)
        *   [3.4.4 Another Expression Alternative: Specify Context at Extension Level](#Another-Expression-Alternative-Specify-Context-at-Extension-Level)
*   [4 Updating the menu and toolbar appearance](#Updating-the-menu-and-toolbar-appearance)
    *   [4.1 UIElements represent each UI visible instance of a command](#UIElements-represent-each-UI-visible-instance-of-a-command)
    *   [4.2 State associated with the command is propogated to UI visible elements](#State-associated-with-the-command-is-propogated-to-UI-visible-elements)


Placement and visibility
========================

The 4 extension points that deal with menus now org.eclipse.ui.actionSets, org.eclipse.ui.viewActions, org.eclipse.ui.editorActions, and org.eclipse.ui.popupMenus specify both menu placement and their visibility criteria. 
In the new menu mechanism they are separate concepts, placement and visibility.

Example Matrix
==============

A (hopefully) growing list of menu contribution examples.

| Example | comments |
| --- | --- |
| [Menu Contributions/Dropdown Command](/Menu_Contributions/Dropdown_Command "Menu Contributions/Dropdown Command") | Dropdown tool items can have their menus filled in using menu contributions |
| [Menu Contributions/Problems View Example](/Menu_Contributions/Problems_View_Example "Menu Contributions/Problems View Example") | An example showing how the Problems View might be converted |
| [Menu Contributions/Populating a dynamic submenu](/Menu_Contributions/Populating_a_dynamic_submenu "Menu Contributions/Populating a dynamic submenu") | A menu contribution to populate a Problems View dynamic submenu |
| [Menu Contributions/Toggle Mark Occurrences](/Menu_Contributions/Toggle_Mark_Occurrences "Menu Contributions/Toggle Mark Occurrences") | Placing the toggle mark occurrences button |
| [Menu Contributions/Toggle Button Command](/Menu_Contributions/Toggle_Button_Command "Menu Contributions/Toggle Button Command") | Contribute a toggle state menu item thru commands |
| [Menu Contributions/Radio Button Command](/Menu_Contributions/Radio_Button_Command "Menu Contributions/Radio Button Command") | Similar to updating toggle state, you can create radio buttons using menu contributions |
| [Menu Contributions/Update checked state](/Menu_Contributions/Update_checked_state "Menu Contributions/Update checked state") | The active handler can update the checked state (and other attributes) of its button |
| [Menu Contributions/Search Menu](/Menu_Contributions/Search_Menu "Menu Contributions/Search Menu") | Adding the Java Search options to the Search menu |
| [Menu Contributions/IFile objectContribution](/Menu_Contributions/IFile_objectContribution "Menu Contributions/IFile objectContribution") | A menu contribution for context menus when the selection is an IFile |
| [Menu Contributions/TextEditor viewerContribution](/Menu_Contributions/TextEditor_viewerContribution "Menu Contributions/TextEditor viewerContribution") | A menu contribution for the text editor context menu |
| [Menu Contributions/Widget in a toolbar](/Menu_Contributions/Widget_in_a_toolbar "Menu Contributions/Widget in a toolbar") | A menu contribution adding a control into the main toolbar |
| [Menu Contributions/RCP removes the Project menu](/Menu_Contributions/RCP_removes_the_Project_menu "Menu Contributions/RCP removes the Project menu") | An RCP application removes the Project menu. Note: this will probably not be in 3.3 |
| [Menu Contributions/Workbench wizard contribution](/Menu_Contributions/Workbench_wizard_contribution "Menu Contributions/Workbench wizard contribution") | Contributing workbench wizards to Menu |

Menu XML
========

Declarative information ... this needs to be cleaned up.

### Declarative menus - some constraints

Some constraints on the system:

1.  Identifiers (id) for `<menu/>` elements must be globally unique.
2.  Identifiers (id) for `<command/>` elements must be globally unique if they are specified.
3.  You can reference a `<menu/>` by id.
4.  If you are just creating menu items for your commands, you can leave them with only a command id. You don't have to specify an item id.
5.  You can reference a `<command/>` for placement options (after, before, etc.) by id.
6.  `<separator/>` ids only have to be unique within that menu level. This is changed to name instead of id in **3.3M5**.
7.  You can provide a `<command/>` label attribute. If none is provided, it will take the command name.
8.  In this design the item contains most of the same rendering information that `<action/>` did.
9.  `<menu/>` and `<command/>` can have `<visibleWhen/>` clauses. If a menu's `<visibleWhen/>` evaluates to false, we will never ask the items contained in that menu.
10.  All of the displayable attributes are translatable.
11.  The mnemonic is specified as you place your `<command/>` elements in their respective menus, since it is possible that the same command might need a different mnemonic depending on which menu it is placed. Also, when defaulting to command names, they don't contain any mnemonic information.

Menus cannot be re-used, and so they have an intrinsic id value. Separators are unique within one menu level, so they also contain their name.

### Menu URIs

For location placement we need a path and placement modifier, and to specify how the paths are built. 
First pass we are going to look at URIs.

*   `<scheme>:<menu-id>[?<placement-modifier>]`

scheme is about how to interpret the URI path. 
For example, `menu`, `toolbar`, `popup`, `status` (although status may be deprecated).

#### menu:

For `menu:` valid root ids will be any viewId for that view's menu, and **org.eclipse.ui.main.menu** for the main menu. 
Then specify the id of the menu this contribution applies to. 
The placement modifier helps position the menu contribution. ex: `after=<id>`, where `<id>` can be a separator name, menu id, or item id. 
An example of a path: `menu:org.eclipse.search.menu?after=contextMenuActionsGroup`

Since menu ids must be unique, you can specify your menu location relative to an existing id: `menu:org.eclipse.search.menu?after=contextMenuActionsGroup`

#### toolbar:

For `toolbar:` valid root ids will be any viewId for that view's toolbar, **org.eclipse.ui.main.toolbar** for the main toolbar, and any toolbar id that is contained in the main toolbar. 
Toolbars can support **invisible** separators. Toolbars in the main toolbar (technically a coolbar) can have ids as well as separators, but only one level. For example: `toolbar:org.eclipse.ui.edit.text.actionSet.presentation?after=Presentation`

In this example, **Presentation** is an invisible separator in the **org.eclipse.ui.edit.text.actionSet.presentation** toolbar.

The use of **org.eclipse.ui.main.toolbar** might change if all "main" toolbars have ids anyway, so the only options for interpretting the toolbar root is 1) the view toolbar or 2) an IDed main toolbar.

#### popup:

For `popup:` valid root ids are any registered context id (which defaults to the part id if no context menu id was given at registration time) and **org.eclipse.ui.popup.any** for all registered context menus. 
For example, to add to the default Text Editor context menu: `popup:#TextEditorContext?after=additions`

Popup submenus are treated like menu submenus, except the form continues to be `popup:submenuId`.

There will be constants defined for the ids that the eclipse workbench provides, probably in `org.eclipse.ui.menus.MenuUtil`.

### Using Expressions in `<visibleWhen/>`

In **3.3M6** an org.eclipse.core.expressions.definitions extension point was added. 
Used to define a [core expression](/Platform_Expression_Framework "Platform Expression Framework"), the definition can then be referenced from other locations.

    <extension point="org.eclipse.core.expressions.definitions">
      <definition id="com.example.context">
        <with variable="activeContexts">
           <iterate operator="or">
             <equals value="org.eclipse.ui.contexts.actionSet"/>
           </iterate>
        </with>
      </definition>
    </extension>

This can be called in a core expression like activeWhen, enabledWhen, visibleWhen, etc using the reference element:

    <reference definitionId="com.example.context"/>

### Ideas that were considered but not implemented

These ideas were considered but not implemented.

#### Menu - JSR198

**Note:** for novelty purposes only.

For comparison, there is a JSR describing how IDEs can contribute menus. 
Below is a sample for 2 items:

*   org.eclipse.ui.views.problems.sorting.item from menu:org.eclipse.ui.views.ProblemView
*   org.eclipse.ui.views.problems.resolveMarker.item from popup:org.eclipse.ui.views.ProblemView

````
    <menu-hook>
      <actions>
        <action id="org.eclipse.ui.views.problems.sorting.item">
          <label>Sorting...</label>
          <mnemonic>S</mnemonic>
          <tooltip>Change the Sort order</tooltip>
          <invoke-class>org.eclipse.ui.views.problems.sorting</invoke-class>
        </action>
        <action id="org.eclipse.ui.views.problems.resolveMarker.item">
          <label>Quick Fix</label>
          <mnemonic>Q</mnemonic>
          <iconpath>$nl$/icons/full/elcl16/smartmode_co.gif</iconpath>
          <invoke-class>org.eclipse.jdt.ui.edit.text.java.correction.assist.proposals</invoke-class>
          <update-class>org.eclipse.jdt.ui.edit.text.java.correction.assist.proposals</update-class>
        </action>
      </actions>
      <menus>
        <menubar id="org.eclipse.ui.views.ProblemView">
          <menu id="org.eclipse.ui.views.ProblemView">
            <section id="problem.view.section">
              <command action-ref="org.eclipse.ui.views.problems.sorting.item" />
              <menu id="org.eclipse.ui.views.problems.groupBy.menu">
                <label>Group By</label>
                <mnemonic>G</mnemonic>
              </menu>
            </section>
          </menu>
        </menubar>
        <popup id="org.eclipse.ui.views.ProblemView">
          <section id="group.resolve">
            <command action-ref="org.eclipse.ui.views.problems.resolveMarker.item" />
          </section>
        </popup>
      </menus>
    </menu-hook>
````

Some thoughts:

*   the actions can only specify one icon
*   the actions can't \*quite\* link to our commands
*   the menus can't specify dynamic submenus

#### Menu - XUL

**Note:** for novelty purposes only.

  
For comparison, with Mozilla everywhere there is the probability eclipse will include xulrunner. 
Menu definitions that are consistent with XUL look like:

    <keyset>
      <key id="paste-key" modifiers="accel" key="V" />
    </keyset>
    <menubar id="org.eclipse.ui.views.ProblemView">
      <menupopup id="org.eclipse.ui.views.ProblemView">
        <menuitem id="org.eclipse.ui.views.problems.sorting.item"
            accesskey="S"
            key="paste-key"
            label="Sorting..."
            oncommand="invokeCommand('org.eclipse.ui.views.problems.sorting')" />
        <menu id="org.eclipse.ui.views.problems.groupBy.menu"
            label="Group By"
            accesskey="G">
          <menupopup id="groupby.popup">
            <!-- this is where submenu items would go -->
          </menupopup>
        </menu>
      </menupopup>
    </menubar>

XUL supports everything as a flavour of a DOM, and javascripting can drive your buttons to perform commands. 
I suspect the scripting would allow you to dynamically update menus (dynamic menus) on popup, depending on what events the DOM would report to you.

  

#### Expression Templates original suggestion

You can see that the `<activeWhen/>`, `<enabledWhen/>`, and probably the `<visibleWhen/>` are likely to be replicated over and over again. 
A possible option is some kind of expression template markup ... either in its own extension or supported by our UI extensions that can use core expressions.

Here's an example of using expression templates in its own extension point.

    <extension point="org.eclipse.core.expression.templates">
      <expression id="isPartActive">
        <parameter id="partId" />
        <with variable="activePartId">
          <equals value="$partId" />
        </with>
      </expression>
      <expression id="isActionSetActive">
        <parameter id="actionSetId" />
        <with variable="activeContexts">
          <iterator operator="or">
            <equals value="$actionSetId" />
          </iterator>
        </with>
      </expression>
      <expression id="isContextActive">
        <parameter id="contextId" />
        <with variable="activeContexts">
          <iterator operator="or">
            <equals value="$contextId" />
          </iterator>
        </with>
      </expression>
      <expression id="isSelectionAvailable">
        <not>
          <count value="0" />
        </not>
      </expression>
    </extension>

This could be used to simplify the handler definitions:

    <extension point="org.eclipse.ui.handlers">
      <handler commandId="org.eclipse.ui.edit.copy"
          class="org.eclipse.ui.views.markers.internal.CopyMarkerHandler">
        <enabledWhen>
          <evaluate ref="isSelectionAvailable" />
        </enabledWhen>
        <activeWhen>
          <evaluate ref="isPartActive">
            <parameter id="partId" value="org.eclipse.ui.views.ProblemView" />
          </evaluate>
        </activeWhen>
      </handler>
    </extension>

If we allow recursive template definitions, that would allow you to specify the concrete expression once and then reference it throughout your view.

    <extension point="org.eclipse.core.expression.templates">
      <expression id="isProblemViewActive">
        <evaluate ref="isPartActive">
          <parameter id="partId" value="org.eclipse.ui.views.ProblemView" />
        </evaluate>
      </expression>
    </extension>
    <extension point="org.eclipse.ui.handlers">
      <handler commandId="org.eclipse.ui.edit.copy"
          class="org.eclipse.ui.views.markers.internal.CopyMarkerHandler">
        <enabledWhen>
          <evaluate ref="isSelectionAvailable" />
        </enabledWhen>
        <activeWhen>
          <evaluate ref="isProblemViewActive" />
        </activeWhen>
      </handler>
    </extension>

This reduces the handler definition even more.

  
A similar option to reuse expressions as much as possible without turning them into their own procedural language would be to allow global definitions and then reuse them. 
No parameters and no expression composition:

    <extension point="org.eclipse.core.expression.templates">
      <expression id="isProblemViewActive">
        <with variable="activePartId">
          <equals value="org.eclipse.ui.views.ProblemView" />
        </with>
      </expression>
      <expression id="isSelectionAvailable">
        <not>
          <count value="0" />
        </not>
      </expression>
    </extension>
    <extension point="org.eclipse.ui.handlers">
      <handler commandId="org.eclipse.ui.edit.copy"
          class="org.eclipse.ui.views.markers.internal.CopyMarkerHandler">
        <enabledWhen ref="isSelectionAvailable" />
        <activeWhen ref="isProblemViewActive" />
      </handler>
    </extension>

#### Another Expression Alternative: Specify Context at Extension Level

Since `enabledWhen` and `activeWhen` specify context and the simple way to specify context in XML is enclosure, how about scoping context to the extension point rather than the handler:

    <extension point="org.eclipse.ui.handlers">
      <enabledWhen>  <!-- context of all  handlers in this extension -->
        <not>
          <count value="0" />
        </not>
      </enabledWhen>
      <activeWhen>
        <with variable="activePartId">
          <equals value="org.eclipse.ui.views.ProblemView" />
        </with>
      </activeWhen>
      <handler commandId="org.eclipse.ui.edit.copy"
          class="org.eclipse.ui.views.markers.internal.CopyMarkerHandler" />
      <handler commandId="org.eclipse.ui.edit.paste"
          class="org.eclipse.ui.views.markers.internal.PasteMarkerHandler" />
      <handler commandId="org.eclipse.ui.edit.delete"
          class="org.eclipse.ui.views.markers.internal.RemoveMarkerHandler" />
      <handler commandId="org.eclipse.jdt.ui.edit.text.java.correction.assist.proposals"
          class="org.eclipse.ui.views.markers.internal.ResolveMarkerHandler" />
      <handler commandId="org.eclipse.ui.edit.selectAll"
          class="org.eclipse.ui.views.markers.internal.SelectAllMarkersHandler" />
      <handler commandId="org.eclipse.ui.file.properties"
          class="org.eclipse.ui.views.markers.internal.ProblemPropertiesHandler" />
    </extension>

This gives compact markup without inventing a new language. 
Elements nested in the handler element could override the extension-wide settings.

Updating the menu and toolbar appearance
========================================

It was suggested in 3.2 that state on the command could be used to implement the old contribution story behaviours:

1.  changing label text and tooltips
2.  changing icons
3.  changing enablement
4.  setting the item state (like checked state)

In 3.3 the enablement is tied to the command, and for the other behaviours we've decided to go with UIElements approach.

UIElements represent each UI visible instance of a command
----------------------------------------------------------

The command service keeps a list of registered UI elements, which can be updated by the active handler. 
The checked state can be updated through UIElement#setChecked(boolean); (note that updateElement below is from IElementUpdater):

    private boolean isChecked() {
        return getStore().getBoolean(
                PreferenceConstants.EDITOR_MARK_OCCURRENCES);
    }
     
    public void updateElement(UIElement element, Map parameters) {
        element.setChecked(isChecked());
    }

When the toggle handler runs, it can request that any UI elements have their appearance updated from its execute(*) method:

    ICommandService service = (ICommandService) serviceLocator
            .getService(ICommandService.class);
    service.refreshElements(IJavaEditorActionDefinitionIds.TOGGLE_MARK_OCCURRENCES, null);

State associated with the command is propogated to UI visible elements
----------------------------------------------------------------------

First define the toggle mark occurrences command. 
Pretty straight forward, although it needs a "STYLE" state since it can be toggled. 
To allow handlers to update the label for the menu/toolbar items, we also add the "NAME" state.

    <extension point="org.eclipse.ui.commands">
      <command categoryId="org.eclipse.jdt.ui.category.source"
          description="%jdt.ui.ToggleMarkOccurrences.description"
          id="org.eclipse.jdt.ui.edit.text.java.toggleMarkOccurrences"
          name="%jdt.ui.ToggleMarkOccurrences.name">
        <state id="NAME" class="org.eclipse.jface.menus.TextState" />
        <state id="STYLE" class="org.eclipse.jface.commands.ToggleState:true" />
      </command>
    </extension>

