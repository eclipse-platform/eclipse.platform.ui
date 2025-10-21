# Eclipse 4 Model

## Contributing to the E4 Application Model

This document provides guidance on contributing to the Eclipse 4 application model through model fragments and extensions.

## Model Fragments

Model fragments allow you to contribute UI elements to the E4 application model declaratively. All E4 contributions are made through model fragments rather than traditional Eclipse 3.x extension points.

### Creating a Model Fragment

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

### Fragment Structure

A basic fragment structure looks like:

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

### Types of Contributions

Model fragments can contribute various elements:

- **Commands**: Define semantic actions
- **Handlers**: Implement command behavior
- **Parts (Views/Editors)**: UI containers via PartDescriptors
- **Menu Contributions**: Menus, toolbars, and context menus
- **Key Bindings**: Keyboard shortcuts
- **Addons**: Application lifecycle hooks

### String Model Fragments

The most common fragment type is `StringModelFragment`, which targets a specific feature of a parent element:

```xml
<fragments xsi:type="fragment:StringModelFragment" 
    featurename="commands" 
    parentElementId="org.eclipse.e4.legacy.ide.application">
  <elements xsi:type="commands:Command" 
      elementId="com.example.mycommand" 
      commandName="My Command"/>
</fragments>
```

Key attributes:
- **featurename**: The model feature to contribute to (e.g., `commands`, `handlers`, `descriptors`, `menuContributions`)
- **parentElementId**: The ID of the parent element (typically your application ID or `org.eclipse.e4.legacy.ide.application`)

### Importing Model Elements

To reference elements defined in other fragments or the application model:

1. Select "Imports" in the fragment editor
2. Add the appropriate import type (Command, Part, etc.)
3. Set the Element ID to match the target element's ID

**Note**: Only fragments can import elements from the application model or other fragments. Application models cannot import from fragments.

### Element IDs

- Element IDs (`elementId`) are used to identify model elements
- Unlike EMF's internal `xmi:id`, element IDs should be human-readable
- For certain elements (like Commands), element IDs must be unique
- When migrating from E3, always preserve the original IDs

### Model Editor Tips

- Use the E4 Model Editor (included in Eclipse IDE for RCP development)
- The editor provides validation and helps ensure proper model structure
- Changes to fragments require `-clearPersistedState` flag during development to see effects

## Application Model

The application model (`Application.e4xmi`) defines the core structure of your E4 application:

- **Windows**: Top-level containers
- **Perspectives**: Layouts of parts
- **Part Stacks**: Containers for parts (views/editors)
- **Trim Bars**: Toolbars and status bars
- **Handlers**: Global command handlers
- **Bindings**: Key bindings
- **Snippets**: Reusable UI fragments

## Additional Resources

- [Eclipse4 RCP FAQ](Eclipse4_RCP_FAQ.md) - Common questions about E4
- [Eclipse4 Migration Guide](Eclipse4_Migration.md) - Migrating from E3 to E4
- [Eclipse Wiki - Contributing to the Model](https://wiki.eclipse.org/Eclipse4/RCP/Modeled_UI/Contributing_to_the_Model) - Detailed Wiki article

## Best Practices

1. **Use meaningful element IDs**: Choose descriptive IDs that clearly indicate purpose
2. **Keep fragments focused**: Create separate fragments for different contribution types
3. **Document your model**: Add descriptions to help others understand your contributions
4. **Test with -clearPersistedState**: Model changes only apply when state is cleared
5. **Preserve E3 IDs when migrating**: Maintain compatibility by keeping the same IDs

---

*This guide is maintained as part of the Eclipse Platform UI project and evolves with the codebase.*
