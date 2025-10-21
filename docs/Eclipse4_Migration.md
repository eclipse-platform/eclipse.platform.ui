# Eclipse 4 Migration Guide

## Introduction

This guide helps you migrate your Eclipse 3.x (E3) RCP application to the Eclipse 4 (E4) application model. It assumes you have already set up a basic E4 application structure and now need guidance on how to gradually upgrade existing E3 components to their E4 counterparts.

For a broader understanding of E4 concepts, see the [Eclipse4 RCP FAQ](Eclipse4_RCP_FAQ.md).

## Prerequisites

Before you begin migrating individual components, ensure you have:

1. **A Model Fragment**: All E4 contributions are made through model fragments. If you haven't created one yet, see [Creating Model Fragments](#creating-model-fragments).

2. **Required Dependencies**: Your MANIFEST.MF should include appropriate E4 dependencies:
   ```
   Require-Bundle: org.eclipse.e4.ui.model.workbench,
    org.eclipse.e4.ui.workbench,
    org.eclipse.e4.core.di,
    org.eclipse.e4.ui.di,
    org.eclipse.e4.core.contexts
   ```

3. **Annotation Support**: For dependency injection to work, add to your MANIFEST.MF:
   ```
   Import-Package: jakarta.annotation;version="1.1.0",
    jakarta.inject;version="1.0.0"
   ```

4. **Preserve Element IDs**: When migrating from E3 to E4, always use the same IDs! The E3 command/view/handler ID should match the E4 elementId to ensure compatibility and avoid breaking existing configurations.

### Creating Model Fragments

Model fragments allow you to contribute UI elements to the E4 application model. To create a model fragment:

1. **Create the fragment file**: Create a `fragment.e4xmi` file in your bundle's root or in a `model/` folder.

2. **Register in plugin.xml**: Add an extension to your plugin.xml:
   ```xml
   <extension
         id="fragment"
         point="org.eclipse.e4.workbench.model">
      <fragment
            uri="fragment.e4xmi">
      </fragment>
   </extension>
   ```

3. **Edit with E4 Model Editor**: Open the fragment.e4xmi with the "Eclipse 4 Model Editor" (right-click → Open With → E4 Model Editor).

The fragment structure typically looks like:
```xml
<?xml version="1.0" encoding="ASCII"?>
<fragment:ModelFragments xmi:version="2.0" 
    xmlns:xmi="http://www.omg.org/XMI" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:commands="http://www.eclipse.org/ui/2010/UIModel/application/commands" 
    xmlns:fragment="http://www.eclipse.org/ui/2010/UIModel/fragment">
  <!-- fragments go here -->
</fragment:ModelFragments>
```

For more details, see [Eclipse4 Model](Eclipse4_Model.md).

## Migrate a Command

Commands define semantic actions that can be triggered by various UI elements (menu items, toolbar buttons, key bindings).

### E3 Approach

In E3, commands are defined in plugin.xml using the `org.eclipse.ui.commands` extension point:

```xml
<extension point="org.eclipse.ui.commands">
   <category
         id="com.example.category"
         name="My Commands"
         description="Custom command category">
   </category>
   <command
         id="com.example.mycommand"
         name="My Command"
         description="Does something useful"
         categoryId="com.example.category">
      <commandParameter
            id="com.example.mycommand.param1"
            name="Parameter 1"
            optional="true">
      </commandParameter>
   </command>
</extension>
```

### E4 Approach

In E4, commands are defined in the application model (fragment.e4xmi) as MCommand elements:

```xml
<fragments xsi:type="fragment:StringModelFragment" 
    featurename="commands" 
    parentElementId="org.eclipse.e4.legacy.ide.application">
  <elements xsi:type="commands:Command" 
      elementId="com.example.mycommand" 
      commandName="My Command"
      description="Does something useful">
    <parameters 
        elementId="com.example.mycommand.param1"
        name="Parameter 1"
        optional="true"/>
  </elements>
</fragments>
```

**Note**: In the model editor:
- The `elementId` serves as the command identifier (equivalent to E3's `id`)
- The `commandName` is the display name

### Migration Steps

1. **Open fragment.e4xmi** in the E4 Model Editor

2. **Add a Model Fragment**:
   - In the overview pane, select "Model Fragments"
   - Click "Add" → Select "StringModelFragment"
   - Set Feature Name: `commands`
   - Set Parent Element ID: `org.eclipse.e4.legacy.ide.application` (or your application ID)

3. **Add Command**:
   - In the fragment details, click "Add" under Elements
   - Select "Command"
   - Set Element Id: Use your E3 command id (e.g., `com.example.mycommand`)
   - Set Command Name: The display name
   - Set Description: Optional description

4. **Add Parameters** (if needed):
   - Select your command
   - In the "Parameters" section, click "Add"
   - Set Element Id and Name for each parameter

5. **Remove E3 registration**: Once tested, remove the corresponding `<command>` element from your plugin.xml

6. **Update references**: Any code that references the command by ID should continue to work, as the element ID matches the E3 command ID.

## Migrate a Handler

Handlers contain the actual implementation code that executes when a command is invoked.

### E3 Approach

In E3, handlers are registered in plugin.xml using the `org.eclipse.ui.handlers` extension point:

```xml
<extension point="org.eclipse.ui.handlers">
   <handler
         commandId="com.example.mycommand"
         class="com.example.handlers.MyHandler">
      <activeWhen>
         <with variable="activePartId">
            <equals value="com.example.myview"/>
         </with>
      </activeWhen>
      <enabledWhen>
         <with variable="selection">
            <count value="1"/>
         </with>
      </enabledWhen>
   </handler>
</extension>
```

The handler class extends `AbstractHandler`:

```java
package com.example.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class MyHandler extends AbstractHandler {
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        // Implementation here
        return null;
    }
}
```

### E4 Approach

In E4, handlers are defined in the model fragment and use dependency injection. The handler class uses `@Execute` annotation:

```xml
<fragments xsi:type="fragment:StringModelFragment" 
    featurename="handlers" 
    parentElementId="org.eclipse.e4.legacy.ide.application">
  <elements xsi:type="commands:Handler" 
      elementId="com.example.myhandler"
      contributionURI="bundleclass://com.example.bundle/com.example.handlers.MyHandler"
      command="_command_xmi_id"/>
</fragments>
```

The handler class uses dependency injection:

```java
package com.example.handlers;

import jakarta.inject.Named;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

public class MyHandler {
    
    @Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
        // Implementation here
        // Dependencies are injected automatically
    }
    
    @CanExecute
    public boolean canExecute() {
        // Replaces E3's enabledWhen expressions
        return true;
    }
}
```

### Migration Steps

1. **Define the handler in the model**:
   - Open fragment.e4xmi in the E4 Model Editor
   - Add a Model Fragment with Feature Name: `handlers`
   - Set Parent Element ID: `org.eclipse.e4.legacy.ide.application` (or scope it to a specific part/window)
   - Add a Handler element
   - Set Contribution URI: `bundleclass://your.bundle.id/com.example.handlers.MyHandler`

2. **Link to command**:
   - If the command is in the same fragment, you can reference it directly
   - If it's in a different fragment/application, create an import:
     - Select "Imports" in the fragment
     - Add a Command import
     - Set the Element ID to your command's ID
   - Set the handler's Command reference to your command

3. **Convert handler class**:
   - Remove `extends AbstractHandler` and `implements IHandler`
   - Remove `execute(ExecutionEvent)` method signature
   - Add `@Execute` annotation to execution method
   - Use dependency injection instead of HandlerUtil:
     ```java
     // E3:
     IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
     ISelection selection = HandlerUtil.getCurrentSelection(event);
     
     // E4:
     @Execute
     public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) ISelection selection,
                        @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
         // Use injected values
     }
     ```

4. **Convert enablement logic**:
   
   **Option A: Use Core Expressions (Recommended for complex conditions)**
   
   If your E3 handler had an `<enabledWhen>` expression, you can migrate it to a core expression in the E4 model:
   
   - Define a core expression in plugin.xml (see [Command_Core_Expressions.md](Command_Core_Expressions.md#definitions)):
     ```xml
     <extension point="org.eclipse.core.expressions.definitions">
        <definition id="com.example.handler.enabled">
           <with variable="selection">
              <count value="1"/>
           </with>
        </definition>
     </extension>
     ```
   
   - In the E4 Model Editor, select your handler and add an "Enabled When" expression:
     - In the handler details, click "Add" in the "Enabled When" section
     - Select "Core Expression"
     - Set Core Expression Id: `com.example.handler.enabled`
   
   When a core expression is defined, it takes precedence over `@CanExecute` annotations.
   
   **Option B: Use @CanExecute annotation (Recommended for simple conditions)**
   
   For simple enablement conditions, use `@CanExecute` method in your handler class:
   ```java
   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) ISelection selection) {
       return selection != null && !selection.isEmpty();
   }
   ```

5. **Handle active context** (replacement for `activeWhen`):
   - In E4, handlers are scoped to their containing model element
   - Global handlers: Add to application level
   - Part-specific handlers: Add to the part's handler list
   - Window-specific handlers: Add to the window's handler list

6. **Remove E3 registration**: Remove the `<handler>` element from plugin.xml

7. **Update MANIFEST.MF**: Ensure you import the injection packages:
   ```
   Import-Package: jakarta.inject;version="1.0.0",
    jakarta.annotation;version="1.1.0"
   ```

### Handler Enablement Precedence

When both an `enabledWhen` core expression and a `@CanExecute` annotation are present:
1. The `enabledWhen` expression is evaluated first
2. If the expression returns false, the handler is disabled (regardless of `@CanExecute`)
3. If the expression returns true (or is not defined), the `@CanExecute` method is called

This allows you to use core expressions for complex, declarative conditions while still having programmatic control through `@CanExecute` when needed.

For more information on core expression syntax, see [Command_Core_Expressions.md](Command_Core_Expressions.md).

### Common Injection Patterns for Handlers

**Important**: Always inject dependencies in the `@Execute` and `@CanExecute` methods rather than using field injection. This ensures you always receive the most recent values from the context, which is critical for handlers that may be executed multiple times with different contexts.

```java
// Active shell
@Named(IServiceConstants.ACTIVE_SHELL) Shell shell

// Active part
@Named(IServiceConstants.ACTIVE_PART) MPart part

// Active selection
@Named(IServiceConstants.ACTIVE_SELECTION) ISelection selection

// Eclipse context
IEclipseContext context

// Part service
EPartService partService

// Model service
EModelService modelService

// Command service (for parameters)
@Execute
public void execute(@Named("parameter.name") String paramValue) {
    // paramValue contains the command parameter
}
```

## Migrate a View

Views are the primary UI containers in Eclipse applications.

### E3 Approach

In E3, views are registered in plugin.xml using the `org.eclipse.ui.views` extension point:

```xml
<extension point="org.eclipse.ui.views">
   <category
         id="com.example.category"
         name="My Views">
   </category>
   <view
         id="com.example.myview"
         name="My View"
         class="com.example.views.MyView"
         category="com.example.category"
         icon="icons/view.png"
         restorable="true"
         allowMultiple="false">
   </view>
</extension>
```

The view class extends `ViewPart`:

```java
package com.example.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class MyView extends ViewPart {
    
    public static final String ID = "com.example.myview";
    
    @Override
    public void createPartControl(Composite parent) {
        // Create UI here
    }
    
    @Override
    public void setFocus() {
        // Set focus to a control
    }
}
```

### E4 Approach

In E4, views become MPart elements defined through PartDescriptors in the model:

```xml
<fragments xsi:type="fragment:StringModelFragment" 
    featurename="descriptors" 
    parentElementId="org.eclipse.e4.legacy.ide.application">
  <elements xsi:type="basic:PartDescriptor" 
      elementId="com.example.myview"
      label="My View"
      iconURI="platform:/plugin/com.example.bundle/icons/view.png"
      contributionURI="bundleclass://com.example.bundle/com.example.views.MyView"
      category="com.example.category"
      closeable="true"
      allowMultiple="false">
    <tags>View</tags>
  </elements>
</fragments>
```

The view class uses dependency injection:

```java
package com.example.views;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.e4.ui.di.Focus;

public class MyView {
    
    @Inject
    public MyView() {
        // Constructor - dependencies can be injected here
    }
    
    @PostConstruct
    public void createPartControl(Composite parent) {
        // Create UI here - same as E3
        // parent is automatically injected
    }
    
    @Focus
    public void setFocus() {
        // Set focus to a control
    }
    
    @PreDestroy
    public void dispose() {
        // Cleanup resources
    }
}
```

### Migration Steps

1. **Create PartDescriptor in the model**:
   - Open fragment.e4xmi in E4 Model Editor
   - Add a Model Fragment with Feature Name: `descriptors`
   - Set Parent Element ID: `org.eclipse.e4.legacy.ide.application`
   - Add a PartDescriptor element
   - Set Element Id: Use your E3 view ID (e.g., `com.example.myview`)
   - Set Label: The view name
   - Set Contribution URI: `bundleclass://your.bundle.id/com.example.views.MyView`
   - Set Icon URI: `platform:/plugin/your.bundle.id/icons/view.png` (if applicable)
   - Set Category: For grouping in the Show View menu
   - Set Closeable: `true` if view can be closed
   - Set Allow Multiple: `false` typically (set `true` if multiple instances allowed)

2. **Add View tag**:
   - In the PartDescriptor details, go to "Tags" section
   - Add tag: `View` (this identifies it as a view for the Show View menu)

3. **Convert view class**:
   - Remove `extends ViewPart`
   - Change method signatures:
     ```java
     // E3:
     public void createPartControl(Composite parent)
     
     // E4:
     @PostConstruct
     public void createPartControl(Composite parent)
     ```
   - Change focus method:
     ```java
     // E3:
     public void setFocus()
     
     // E4:
     @Focus
     public void setFocus()
     ```
   - Add disposal if needed:
     ```java
     @PreDestroy
     public void dispose() {
         // Cleanup
     }
     ```

4. **Replace getSite() calls** with dependency injection:
   ```java
   // E3:
   IWorkbenchPartSite site = getSite();
   ISelectionProvider provider = site.getSelectionProvider();
   
   // E4:
   @Inject
   ESelectionService selectionService;
   
   @PostConstruct
   public void createPartControl(Composite parent) {
       // Use selectionService.setSelection() instead
   }
   ```

5. **Update other service access**:
   ```java
   // E3:
   getSite().getWorkbenchWindow().getActivePage();
   
   // E4:
   @Inject
   EPartService partService;
   ```

6. **Remove E3 registration**: Remove the `<view>` element from plugin.xml

7. **Test the view**: 
   - The view should appear in Window → Show View menu
   - It should be openable via `EPartService.showPart()`

### Common View Injection Patterns

```java
// Parent composite (for createPartControl)
@PostConstruct
public void createPartControl(Composite parent)

// Part itself
@Inject MPart part;

// Part service
@Inject EPartService partService;

// Selection service
@Inject ESelectionService selectionService;

// Menu service
@Inject EMenuService menuService;

// Receiving selection changes
@Inject
public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) 
                        @Optional ISelection selection) {
    // Reacts to selection changes
}

// Accessing part properties
@Inject MPart part;
// part.getProperties().put("key", "value");
```

## Migrate a Menu

Menus in Eclipse can be main menus, view menus, context menus, or toolbar items.

### E3 Approach

In E3, menu contributions are made through various extension points:

**Main Menu Contribution:**
```xml
<extension point="org.eclipse.ui.menus">
   <menuContribution
         locationURI="menu:org.eclipse.ui.main.menu?after=additions">
      <menu
            id="com.example.menu"
            label="My Menu"
            mnemonic="M">
         <command
               commandId="com.example.mycommand"
               style="push"
               label="My Command">
         </command>
      </menu>
   </menuContribution>
</extension>
```

**Toolbar Contribution:**
```xml
<extension point="org.eclipse.ui.menus">
   <menuContribution
         locationURI="toolbar:org.eclipse.ui.main.toolbar?after=additions">
      <toolbar id="com.example.toolbar">
         <command
               commandId="com.example.mycommand"
               icon="icons/action.png"
               tooltip="My Action"
               style="push">
         </command>
      </toolbar>
   </menuContribution>
</extension>
```

**Context Menu Contribution:**
```xml
<extension point="org.eclipse.ui.menus">
   <menuContribution
         locationURI="popup:org.eclipse.ui.popup.any?after=additions">
      <command
            commandId="com.example.mycommand"
            label="My Action">
         <visibleWhen checkEnabled="false">
            <with variable="selection">
               <count value="1"/>
            </with>
         </visibleWhen>
      </command>
   </menuContribution>
</extension>
```

### E4 Approach

In E4, menu contributions are defined in the model through menu contributions:

**Main Menu Contribution:**
```xml
<fragments xsi:type="fragment:StringModelFragment" 
    featurename="menuContributions" 
    parentElementId="org.eclipse.e4.legacy.ide.application">
  <elements xsi:type="menu:MenuContribution" 
      elementId="com.example.menucontribution"
      parentId="org.eclipse.ui.main.menu"
      positionInParent="after=additions">
    <children xsi:type="menu:Menu" 
        elementId="com.example.menu"
        label="My Menu"
        mnemonics="M">
      <children xsi:type="menu:HandledMenuItem" 
          elementId="com.example.menuitem"
          label="My Command"
          command="_command_reference"/>
    </children>
  </elements>
</fragments>
```

**Toolbar Contribution:**
```xml
<fragments xsi:type="fragment:StringModelFragment" 
    featurename="menuContributions" 
    parentElementId="org.eclipse.e4.legacy.ide.application">
  <elements xsi:type="menu:MenuContribution" 
      elementId="com.example.toolbarcontribution"
      parentId="org.eclipse.ui.main.toolbar"
      positionInParent="after=additions">
    <children xsi:type="menu:ToolBar" 
        elementId="com.example.toolbar">
      <children xsi:type="menu:HandledToolItem" 
          elementId="com.example.toolitem"
          label="My Action"
          tooltip="My Action"
          iconURI="platform:/plugin/com.example.bundle/icons/action.png"
          command="_command_reference"/>
    </children>
  </elements>
</fragments>
```

**Dynamic Menu with Visibility Expression:**
```xml
<children xsi:type="menu:HandledMenuItem" 
    elementId="com.example.menuitem"
    label="My Command"
    command="_command_reference">
  <visibleWhen xsi:type="ui:CoreExpression"
      coreExpressionId="com.example.expression.visible"/>
</children>
```

Define the expression in plugin.xml or as a core expression in the model.

### Migration Steps

#### For Main Menu Items:

1. **Create menu contribution fragment**:
   - Open fragment.e4xmi in E4 Model Editor
   - Add Model Fragment with Feature Name: `menuContributions`
   - Set Parent Element ID: `org.eclipse.e4.legacy.ide.application`
   - Add MenuContribution element
   - Set Parent ID: `org.eclipse.ui.main.menu` (for main menu)
   - Set Position In Parent: `after=additions` (or other position)

2. **Add menu structure**:
   - For a submenu: Add Menu element as a child
     - Set Element Id, Label, Mnemonics
   - For menu items: Add HandledMenuItem as children
     - Set Element Id, Label
     - Link to command (via import if necessary)

3. **Link to commands**:
   - Create command imports if commands are defined elsewhere
   - Set the Command reference on each HandledMenuItem

#### For Toolbar Items:

1. **Create toolbar contribution**:
   - Same as menu, but set Parent ID: `org.eclipse.ui.main.toolbar`
   - Add ToolBar element as child
   - Add HandledToolItem elements as toolbar children
   - Set Icon URI: `platform:/plugin/your.bundle.id/icons/icon.png`

#### For Context Menus:

1. **Identify the popup menu ID**:
   - For views: Usually the view's element ID
   - For general context menus: `org.eclipse.ui.popup.any`
   - For specific contexts: Check existing plugin.xml registrations

2. **Create popup menu contribution**:
   - Set Parent ID to the popup menu ID
   - Add HandledMenuItem elements
   - Add visibility expressions if needed

3. **Programmatic context menu** (for view-specific):
   ```java
   @PostConstruct
   public void createPartControl(Composite parent, 
                                  EMenuService menuService,
                                  MPart part) {
       // Create viewer or control
       TableViewer viewer = new TableViewer(parent);
       
       // Register context menu
       menuService.registerContextMenu(viewer.getControl(), 
                                       part.getElementId());
   }
   ```

#### For Visibility/Enablement:

1. **Using Core Expressions**:
   - Define expressions in plugin.xml:
     ```xml
     <extension point="org.eclipse.core.expressions.definitions">
        <definition id="com.example.expression.visible">
           <with variable="selection">
              <count value="1"/>
           </with>
        </definition>
     </extension>
     ```
   
2. **In the model**:
   - Add visibleWhen element to menu items
   - Reference the expression by ID

3. **Programmatic enablement** (via handler):
   ```java
   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) 
                            ISelection selection) {
       return selection != null && !selection.isEmpty();
   }
   ```

#### Final Steps:

1. **Remove E3 registrations**: Remove `<menuContribution>` elements from plugin.xml

2. **Test menu visibility and enablement**: Ensure menus appear in correct locations and respond to context

3. **Verify icons**: Check that icon URIs are correct (use `platform:/plugin/` scheme)

### Common Menu URI Patterns

For reference, common menu parent IDs:

- Main menu: `org.eclipse.ui.main.menu`
- Main toolbar: `org.eclipse.ui.main.toolbar`
- File menu: `file`
- Edit menu: `edit`
- Help menu: `help`
- Any popup: `org.eclipse.ui.popup.any`
- View-specific popup: Use the view's element ID

Position qualifiers:
- `after=additions`
- `before=additions`
- `after=someMenuId`
- `first`
- `last`

## Testing Your Migration

After migrating components, test thoroughly:

1. **Launch Configuration**: 
   - Use `-clearPersistedState` to ensure model changes are loaded
   - Use `-persistState false` during development

2. **Verify Functionality**:
   - Commands execute correctly
   - Handlers receive proper context
   - Views display and update properly
   - Menus appear in correct locations

3. **Check Injection**:
   - Enable tracing: `-Dorg.eclipse.e4.core.di.debug=true`
   - Watch for injection errors in the log

4. **Test Lifecycle**:
   - Open/close views
   - Execute commands multiple times
   - Check resource disposal (no memory leaks)

## Common Migration Pitfalls

1. **Forgot @PostConstruct**: Methods won't be called without the annotation
2. **Wrong parent ID**: Menu items won't appear if parent ID is incorrect
3. **Missing imports**: Commands/handlers from other fragments need imports
4. **Persisted state**: Use `-clearPersistedState` when testing model changes
5. **Class not injectable**: Handler/view class must have a public no-arg constructor or an @Inject constructor
6. **Shell auto-generation**: Always use `@Named(IServiceConstants.ACTIVE_SHELL)` to avoid getting a new Shell

## Additional Resources

- [Eclipse4 RCP FAQ](Eclipse4_RCP_FAQ.md) - Common questions and answers
- [Eclipse4 RCP Dependency Injection](Eclipse4_RCP_Dependency_Injection.md) - DI details
- [Eclipse4 RCP Contexts](Eclipse4_RCP_Contexts.md) - Context hierarchy
- [Eclipse4 Model](Eclipse4_Model.md) - Contributing to the E4 application model
- [Platform Command Framework](PlatformCommandFramework.md) - Command details
- [Menu Contributions](Menu_Contributions.md) - E3 menu contribution patterns

## Contributing

If you find issues with this guide or have suggestions for improvement, please contribute:
1. Open an issue at the [Eclipse Platform UI repository](https://github.com/eclipse-platform/eclipse.platform.ui)
2. Submit a pull request with improvements
3. Share your migration experiences to help others

---

*This guide is maintained as part of the Eclipse Platform UI project and evolves with the codebase.*
