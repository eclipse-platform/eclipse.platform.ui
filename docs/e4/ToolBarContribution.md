# E4 ToolBarContribution

## Overview

A `ToolBarContribution` (represented by `MToolBarContribution` in code) is a model element that allows you to dynamically contribute toolbar items to existing toolbars in an Eclipse 4 application. This is the E4 equivalent of the Eclipse 3.x toolbar extension mechanism, but implemented through the declarative E4 application model.

ToolBarContributions enable modular development by allowing plugins to add toolbar items to toolbars defined elsewhere in the application model, without directly modifying the target toolbar's definition.

## Use Cases

ToolBarContributions are useful in several scenarios:

1. **Plugin Contributions**: When your plugin needs to add toolbar buttons to application-level or view-specific toolbars without modifying the core application model.

2. **Modular UI Design**: When building applications where different modules contribute their own toolbar items to a shared toolbar.

3. **Dynamic Contributions**: When toolbar items need to be added or removed based on runtime conditions using visibility expressions.

4. **Fragment-based Contributions**: When using model fragments to contribute to an existing application's toolbars from separate plugins.

5. **Context-specific Actions**: When adding toolbar items that are only relevant in specific contexts (e.g., perspective-specific or view-specific toolbars).

## Core Concepts

### ToolBar vs ToolBarContribution

- **`MToolBar`**: A concrete toolbar that is rendered in the UI. It has an `elementId` that other contributions can target.
- **`MToolBarContribution`**: A contribution descriptor that specifies:
  - Which toolbar to contribute to (via `parentId`)
  - Where in that toolbar to place the items (via `positionInParent`)
  - What toolbar items to add (via its `children` list)

### Key Attributes

#### parentId
The `elementId` of the target toolbar where items will be contributed. Common targets include:
- `org.eclipse.ui.main.toolbar` - The main application toolbar
- View-specific toolbar IDs (typically the view's part ID)
- Custom toolbar IDs defined in your application

#### positionInParent
Specifies where in the target toolbar the contributed items should appear. Format: `<modifier>=<id>`

Supported modifiers:
- `after=<id>` - Insert after the element with the given ID
- `before=<id>` - Insert before the element with the given ID  
- `first` - Insert at the beginning
- `endof=<id>` - Insert at the end of a logical group (after a separator)
- `index:<n>` - Insert at specific position (0-based)

If not specified, items are appended to the end of the toolbar.

Examples:
- `after=org.eclipse.ui.edit.save` - After the Save button
- `before=additions` - Before the "additions" separator
- `endof=file.group` - At the end of the file group

#### children
A list of `MToolBarElement` items to contribute. Common types include:
- `MHandledToolItem` - A button that invokes a command
- `MDirectToolItem` - A button with direct implementation
- `MToolBarSeparator` - A visual separator
- `MToolControl` - A custom widget/control

## Creating a ToolBarContribution

### Using the E4 Model Editor

1. **Open your fragment.e4xmi** with the E4 Model Editor (right-click → Open With → E4 Model Editor)

2. **Create a StringModelFragment** (if not already present):
   - In the Model Fragments section, click "Add"
   - Select "StringModelFragment"
   - Set `featurename` to `toolBarContributions`
   - Set `parentElementId` to your application ID (e.g., `com.example.myapp.application`)

3. **Add a ToolBarContribution**:
   - Under the StringModelFragment elements, click "Add"
   - Select "ToolBarContribution"
   - Set the `elementId` (e.g., `com.example.myapp.toolbar.contribution`)
   - Set `parentId` to the target toolbar ID (e.g., `org.eclipse.ui.main.toolbar`)
   - Set `positionInParent` if you need specific placement (e.g., `after=save`)

4. **Add Toolbar Items**:
   - Select your ToolBarContribution
   - In the Children section, click "Add"
   - Select the appropriate item type (typically `HandledToolItem`)
   - Configure the item:
     - Set `elementId` (e.g., `com.example.myapp.toolbar.myaction`)
     - Set `label` (tooltip text)
     - Set `iconURI` (e.g., `platform:/plugin/com.example.myapp/icons/myicon.png`)
     - For HandledToolItem, select or import the command to execute

### XML Example

Here's what a ToolBarContribution looks like in the fragment.e4xmi XML:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<fragment:ModelFragments xmi:version="2.0" 
    xmlns:xmi="http://www.omg.org/XMI" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:fragment="http://www.eclipse.org/ui/2010/UIModel/fragment" 
    xmlns:menu="http://www.eclipse.org/ui/2010/UIModel/application/ui/menu" 
    xmi:id="_fragmentId">
  
  <fragments xsi:type="fragment:StringModelFragment" 
      xmi:id="_contribution_fragment"
      featurename="toolBarContributions" 
      parentElementId="com.example.myapp.application">
    
    <elements xsi:type="menu:ToolBarContribution" 
        xmi:id="_toolbar_contribution"
        elementId="com.example.myapp.toolbar.contribution"
        parentId="org.eclipse.ui.main.toolbar"
        positionInParent="after=save">
      
      <children xsi:type="menu:HandledToolItem" 
          xmi:id="_custom_action"
          elementId="com.example.myapp.toolbar.customaction"
          label="Custom Action"
          tooltip="Performs a custom action"
          iconURI="platform:/plugin/com.example.myapp/icons/custom.png"
          command="_imported_command"/>
      
      <children xsi:type="menu:ToolBarSeparator" 
          xmi:id="_separator"
          elementId="com.example.myapp.toolbar.separator"/>
      
    </elements>
  </fragments>
  
  <!-- Import the command defined elsewhere -->
  <imports xsi:type="commands:Command" 
      xmi:id="_imported_command"
      elementId="com.example.myapp.commands.custom"/>
      
</fragment:ModelFragments>
```

## Example: Extending the E4 Application Template

The [Eclipse PDE E4 Application template](https://github.com/eclipse-pde/eclipse.pde/blob/master/ui/org.eclipse.pde.ui.templates/templates_3.5/E4Application/Application.e4xmi) provides a basic E4 application with a main toolbar.

Let's add a custom toolbar contribution to this application.

### Step 1: Identify the Target Toolbar

The template defines a main toolbar:
```xml
<trimBars xmi:id="..." elementId="$pluginId$.trimbar.top">
  <children xsi:type="menu:ToolBar" xmi:id="..." 
      elementId="org.eclipse.ui.main.toolbar">
    <!-- Existing toolbar items -->
  </children>
</trimBars>
```

The target `parentId` is `org.eclipse.ui.main.toolbar`.

### Step 2: Create a Fragment

Create a new plugin with a fragment.e4xmi:

1. Create a plugin project (File → New → Plug-in Project)
2. Add a dependency on `org.eclipse.e4.ui.model.workbench`
3. Create `fragment.e4xmi` in the project root
4. Register it in plugin.xml:

```xml
<extension id="fragment" point="org.eclipse.e4.workbench.model">
  <fragment uri="fragment.e4xmi"/>
</extension>
```

### Step 3: Define the Contribution

Edit fragment.e4xmi to add your toolbar contribution:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<fragment:ModelFragments xmi:version="2.0" 
    xmlns:xmi="http://www.omg.org/XMI" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:commands="http://www.eclipse.org/ui/2010/UIModel/application/commands"
    xmlns:fragment="http://www.eclipse.org/ui/2010/UIModel/fragment" 
    xmlns:menu="http://www.eclipse.org/ui/2010/UIModel/application/ui/menu">
  
  <!-- Define a command -->
  <fragments xsi:type="fragment:StringModelFragment" 
      featurename="commands" 
      parentElementId="$applicationId$">
    <elements xsi:type="commands:Command" 
        elementId="com.example.extension.commands.refresh"
        commandName="Refresh"/>
  </fragments>
  
  <!-- Add a handler -->
  <fragments xsi:type="fragment:StringModelFragment" 
      featurename="handlers" 
      parentElementId="$applicationId$">
    <elements xsi:type="commands:Handler" 
        elementId="com.example.extension.handler.refresh"
        contributionURI="bundleclass://com.example.extension/com.example.extension.handlers.RefreshHandler"
        command="_refresh_cmd"/>
  </fragments>
  
  <!-- Contribute to the toolbar -->
  <fragments xsi:type="fragment:StringModelFragment" 
      featurename="toolBarContributions" 
      parentElementId="$applicationId$">
    <elements xsi:type="menu:ToolBarContribution" 
        elementId="com.example.extension.toolbar.contribution"
        parentId="org.eclipse.ui.main.toolbar"
        positionInParent="after=$pluginId$.handleditem.trimbar.top.save">
      
      <children xsi:type="menu:ToolBarSeparator" 
          elementId="com.example.extension.toolbar.separator"/>
      
      <children xsi:type="menu:HandledToolItem" 
          elementId="com.example.extension.toolbar.refresh"
          label="Refresh"
          tooltip="Refresh the view"
          iconURI="platform:/plugin/com.example.extension/icons/refresh.png"
          command="_refresh_cmd"/>
      
    </elements>
  </fragments>
  
  <!-- Import command reference -->
  <imports xsi:type="commands:Command" 
      elementId="com.example.extension.commands.refresh"
      xmi:id="_refresh_cmd"/>
  
</fragment:ModelFragments>
```

### Step 4: Implement the Handler

Create the handler class referenced in the contribution:

```java
package com.example.extension.handlers;

import org.eclipse.e4.core.di.annotations.Execute;

public class RefreshHandler {
    
    @Execute
    public void execute() {
        // Your refresh logic here
        System.out.println("Refresh action executed!");
    }
}
```

### Step 5: Run the Application

1. Run the application with the `-clearPersistedState` flag to see model changes
2. Your toolbar contribution should appear in the main toolbar after the Save button

## Where Contributions Become Visible

ToolBarContributions are processed at application startup and merged into the target toolbar at the position specified. The contributed items become part of the toolbar's children and are rendered like any other toolbar item.

Key points about visibility:

1. **Target Must Exist**: The toolbar specified in `parentId` must exist in the application model for the contribution to be processed.

2. **Rendering**: Contributed items are rendered when the target toolbar is rendered. For the main toolbar, this is typically at application startup.

3. **View Toolbars**: When contributing to view-specific toolbars, items appear when the view is opened.

4. **Dynamic Visibility**: Individual contributed items can have `visibleWhen` expressions to control their visibility based on application state.

## Visibility Control

### Item-level Visibility

You can add a `visibleWhen` expression to individual toolbar items:

```xml
<children xsi:type="menu:HandledToolItem" 
    elementId="com.example.toolbar.conditionalitem"
    label="Conditional Item"
    command="_some_command">
  <visibleWhen xsi:type="ui:CoreExpression" 
      coreExpressionId="com.example.expression.projectSelected"/>
</children>
```

### Contribution-level Visibility

You can also add a `visibleWhen` expression to the entire ToolBarContribution:

```xml
<elements xsi:type="menu:ToolBarContribution" 
    elementId="com.example.toolbar.contribution"
    parentId="org.eclipse.ui.main.toolbar">
  <visibleWhen xsi:type="ui:CoreExpression" 
      coreExpressionId="com.example.expression.debugMode"/>
  <!-- children -->
</elements>
```

When the expression evaluates to false, none of the contributed items will be visible.

## Dynamic Contributions with Factories

For advanced scenarios, you can use a factory to dynamically generate toolbar items:

```java
public class DynamicToolBarContributionFactory {
    
    @Execute
    public void createItems(List<MToolBarElement> items, 
                           MToolBar toolbar,
                           ToolBarManagerRenderer renderer) {
        // Dynamically create toolbar items based on runtime state
        for (String action : getAvailableActions()) {
            MHandledToolItem item = MenuFactoryImpl.eINSTANCE.createHandledToolItem();
            item.setElementId("dynamic." + action);
            item.setLabel(action);
            // Configure item...
            items.add(item);
        }
    }
    
    private List<String> getAvailableActions() {
        // Return list based on runtime state
        return Arrays.asList("Action1", "Action2", "Action3");
    }
}
```

Register the factory in the ToolBarContribution's transient data using the key `ToolBarContributionFactory`.

## Common Patterns

### Pattern 1: Adding a Single Button

The simplest case - add one button to an existing toolbar:

```xml
<elements xsi:type="menu:ToolBarContribution" 
    elementId="myapp.toolbar.simple"
    parentId="org.eclipse.ui.main.toolbar">
  <children xsi:type="menu:HandledToolItem" 
      elementId="myapp.toolbar.mybutton"
      label="My Button"
      iconURI="platform:/plugin/myapp/icons/button.png"
      command="_my_command"/>
</elements>
```

### Pattern 2: Adding a Group with Separator

Add multiple related items with separators:

```xml
<elements xsi:type="menu:ToolBarContribution" 
    elementId="myapp.toolbar.group"
    parentId="org.eclipse.ui.main.toolbar">
  
  <children xsi:type="menu:ToolBarSeparator" 
      elementId="myapp.toolbar.separator.start"/>
  
  <children xsi:type="menu:HandledToolItem" 
      elementId="myapp.toolbar.item1"
      label="Item 1"
      command="_cmd1"/>
  
  <children xsi:type="menu:HandledToolItem" 
      elementId="myapp.toolbar.item2"
      label="Item 2"
      command="_cmd2"/>
  
  <children xsi:type="menu:ToolBarSeparator" 
      elementId="myapp.toolbar.separator.end"/>
</elements>
```

### Pattern 3: View-Specific Toolbar Contribution

Contribute to a specific view's toolbar:

```xml
<elements xsi:type="menu:ToolBarContribution" 
    elementId="myapp.toolbar.view.contribution"
    parentId="com.example.myview.toolbar">
  <children xsi:type="menu:HandledToolItem" 
      elementId="myapp.toolbar.view.action"
      label="View Action"
      command="_view_command"/>
</elements>
```

Note: The view must define a toolbar with the specified `elementId`.

## Best Practices

1. **Use Meaningful IDs**: Choose clear, unique element IDs that indicate purpose and origin (e.g., `com.mycompany.plugin.toolbar.contribution.export`).

2. **Position Carefully**: Use `positionInParent` to place items in logical locations. Respect existing groupings and separators.

3. **Provide Icons**: Always provide appropriate icons for toolbar items. Use scalable formats (SVG) when possible.

4. **Set Tooltips**: Use the `tooltip` attribute (or `label` for basic tooltips) to describe what the button does.

5. **Use Separators**: Group related items together and use separators to visually distinguish groups.

6. **Test Visibility**: If using `visibleWhen` expressions, thoroughly test that items appear and disappear as expected.

7. **Avoid Clutter**: Don't add too many items to a single toolbar. Consider using menus for less-frequently used actions.

8. **Handle Missing Targets Gracefully**: If the target toolbar might not exist, ensure your contribution doesn't cause errors. The framework will silently ignore contributions to non-existent toolbars.

9. **Clear Persisted State During Development**: When testing model changes, run with `-clearPersistedState` to ensure changes are picked up.

10. **Document Your Contribution Points**: If you're providing a toolbar for others to contribute to, document its ID and preferred contribution patterns.

## Troubleshooting

### Contribution Not Appearing

1. **Check parentId**: Ensure the target toolbar exists and has the correct ID
2. **Verify fragment registration**: Confirm plugin.xml registers the fragment correctly
3. **Clear persisted state**: Run with `-clearPersistedState`
4. **Check dependencies**: Ensure required plugins are available
5. **Review positioning**: Invalid `positionInParent` values may prevent contribution

### Items in Wrong Position

1. **Verify reference IDs**: The ID in `positionInParent` must match an existing element
2. **Check separator names**: Separator IDs are case-sensitive
3. **Consider order**: Multiple contributions to the same position are processed in plugin load order

### Visibility Issues

1. **Test expressions**: Ensure `visibleWhen` expressions are properly defined
2. **Check context**: Verify required context variables are available
3. **Debug rendering**: Enable E4 model debugging to see contribution processing

## Related Concepts

- **[MenuContribution](../Menu_Contributions.md)**: Similar concept for menu contributions
- **[E4 Commands](../Eclipse4_Commands.md)**: Commands that toolbar items invoke
- **[E4 Model](../Eclipse4_Model.md)**: General E4 model concepts
- **[Dependency Injection](../Eclipse4_RCP_Dependency_Injection.md)**: How handlers receive context

## References

- [Eclipse 4 Model API JavaDoc](https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fe4%2Fui%2Fmodel%2Fapplication%2Fpackage-summary.html)
- [Eclipse 4 Wiki - Modeled UI](https://wiki.eclipse.org/Eclipse4/RCP/Modeled_UI)
- [E4 Application Template Source](https://github.com/eclipse-pde/eclipse.pde/tree/master/ui/org.eclipse.pde.ui.templates/templates_3.5/E4Application)

---

*This documentation is part of the Eclipse Platform UI project and evolves with the codebase.*
