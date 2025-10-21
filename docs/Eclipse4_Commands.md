# Eclipse 4 Commands

## Introduction

In Eclipse 4 (E4), commands are executed using the **ECommandService** and **EHandlerService** which are part of the E4 dependency injection framework. This document explains how to programmatically call commands in E4 applications.

Commands in Eclipse provide a semantic abstraction for user actions. A command represents what should be done (e.g., "Save File"), while handlers contain the actual implementation code that executes when the command is invoked.

## Key Services

### ECommandService

The **ECommandService** is used to:
- Define commands programmatically
- Create `ParameterizedCommand` instances for execution
- Query defined commands
- Define command categories and parameters

### EHandlerService

The **EHandlerService** is used to:
- Execute commands
- Activate and deactivate handlers
- Check if a handler can execute (enabled state)
- Execute commands with specific contexts

## Calling Commands in E4

### Basic Command Execution

To execute a command without parameters:

```java
import jakarta.inject.Inject;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.core.commands.ParameterizedCommand;

public class MyPart {
    
    @Inject
    private ECommandService commandService;
    
    @Inject
    private EHandlerService handlerService;
    
    public void executeCommand() {
        // Create the command
        ParameterizedCommand command = commandService.createCommand(
            "com.example.mycommand", 
            null  // no parameters
        );
        
        // Execute the command
        handlerService.executeHandler(command);
    }
}
```

### Command Execution with Parameters

To execute a command with parameters, provide a `Map<String, Object>` containing the parameter values:

```java
import java.util.HashMap;
import java.util.Map;
import jakarta.inject.Inject;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.core.commands.ParameterizedCommand;

public class MyPart {
    
    @Inject
    private ECommandService commandService;
    
    @Inject
    private EHandlerService handlerService;
    
    public void showSpecificView(String viewId) {
        // Create parameter map
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("org.eclipse.ui.views.showView.viewId", viewId);
        
        // Create the parameterized command
        ParameterizedCommand command = commandService.createCommand(
            "org.eclipse.ui.views.showView", 
            parameters
        );
        
        // Execute the command
        handlerService.executeHandler(command);
    }
}
```

### Using Collections.emptyMap() for Commands Without Parameters

For commands without parameters, you can use `Collections.emptyMap()` instead of `null`:

```java
import java.util.Collections;
import jakarta.inject.Inject;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.core.commands.ParameterizedCommand;

public class MyPart {
    
    @Inject
    private ECommandService commandService;
    
    @Inject
    private EHandlerService handlerService;
    
    public void executeCommand() {
        ParameterizedCommand command = commandService.createCommand(
            "com.example.mycommand", 
            Collections.emptyMap()
        );
        
        handlerService.executeHandler(command);
    }
}
```

### Executing Commands with Return Values

Commands can return values. The `executeHandler` method returns an `Object`:

```java
public Object executeCommandWithReturnValue() {
    ParameterizedCommand command = commandService.createCommand(
        "com.example.mycommand", 
        null
    );
    
    // Execute and capture the return value
    Object result = handlerService.executeHandler(command);
    return result;
}
```

### Executing Commands in a Specific Context

You can execute a command in a specific Eclipse context by passing an `IEclipseContext`:

```java
import org.eclipse.e4.core.contexts.IEclipseContext;
import jakarta.inject.Inject;

public class MyPart {
    
    @Inject
    private ECommandService commandService;
    
    @Inject
    private EHandlerService handlerService;
    
    @Inject
    private IEclipseContext context;
    
    public void executeInContext() {
        ParameterizedCommand command = commandService.createCommand(
            "com.example.mycommand", 
            null
        );
        
        // Execute in specific context
        handlerService.executeHandler(command, context);
    }
}
```

## Dependency Injection Patterns

### Injecting Services in a Part

E4 parts receive services through dependency injection using the `@Inject` annotation:

```java
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.swt.widgets.Composite;

public class MyView {
    
    @Inject
    private ECommandService commandService;
    
    @Inject
    private EHandlerService handlerService;
    
    @PostConstruct
    public void createPartControl(Composite parent) {
        // Create UI
        // Services are automatically injected
    }
}
```

### Injecting Services in a Handler

Handlers also receive services through dependency injection in their `@Execute` method:

```java
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;

public class MyHandler {
    
    @Execute
    public void execute(ECommandService commandService, 
                       EHandlerService handlerService) {
        // Use services to execute another command
        ParameterizedCommand command = commandService.createCommand(
            "com.example.othercommand", 
            null
        );
        handlerService.executeHandler(command);
    }
}
```

## Defining Commands Programmatically

While commands are typically defined in the application model or plugin.xml, you can also define them programmatically:

```java
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import jakarta.inject.Inject;
import org.eclipse.e4.core.commands.ECommandService;

public class CommandDefiner {
    
    @Inject
    private ECommandService commandService;
    
    public void defineCommand() {
        // Define a category
        Category category = commandService.defineCategory(
            "com.example.category",
            "My Commands",
            "Description of my commands"
        );
        
        // Define a command
        Command command = commandService.defineCommand(
            "com.example.mycommand",
            "My Command",
            "Description of my command",
            category,
            null  // no parameters
        );
    }
}
```

## Complete Example

Here's a complete example showing how to execute the "Show View" command from a part:

```java
package com.example.parts;

import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class MyPart {
    
    @Inject
    private ECommandService commandService;
    
    @Inject
    private EHandlerService handlerService;
    
    @PostConstruct
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout());
        
        Button button = new Button(parent, SWT.PUSH);
        button.setText("Open Problem View");
        
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openView("org.eclipse.ui.views.ProblemView");
            }
        });
    }
    
    private void openView(String viewId) {
        // Create parameter map
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("org.eclipse.ui.views.showView.viewId", viewId);
        
        // Create the parameterized command
        ParameterizedCommand command = commandService.createCommand(
            "org.eclipse.ui.views.showView", 
            parameters
        );
        
        // Execute the command
        handlerService.executeHandler(command);
    }
}
```

## Error Handling

When executing commands, you may want to handle exceptions:

```java
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;

public void executeCommandSafely() {
    try {
        ParameterizedCommand command = commandService.createCommand(
            "com.example.mycommand", 
            null
        );
        
        handlerService.executeHandler(command);
        
    } catch (ExecutionException e) {
        // Handler threw an exception
        e.printStackTrace();
    } catch (NotDefinedException e) {
        // Command is not defined
        e.printStackTrace();
    } catch (NotEnabledException e) {
        // Command is not enabled
        e.printStackTrace();
    } catch (NotHandledException e) {
        // No handler available
        e.printStackTrace();
    }
}
```

Note: In many cases, `executeHandler` wraps exceptions and stores them in the context, so you may need to check the context for exceptions after execution.

## Common Use Cases

### Opening a Perspective

```java
public void openPerspective(String perspectiveId) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("org.eclipse.ui.perspectives.showPerspective.perspectiveId", perspectiveId);
    
    ParameterizedCommand command = commandService.createCommand(
        "org.eclipse.ui.perspectives.showPerspective", 
        parameters
    );
    
    handlerService.executeHandler(command);
}
```

### Saving the Active Editor

```java
public void saveActiveEditor() {
    ParameterizedCommand command = commandService.createCommand(
        "org.eclipse.ui.file.save", 
        null
    );
    
    handlerService.executeHandler(command);
}
```

### Refreshing a Resource

```java
public void refreshResource() {
    ParameterizedCommand command = commandService.createCommand(
        "org.eclipse.ui.file.refresh", 
        null
    );
    
    handlerService.executeHandler(command);
}
```

## Required Dependencies

To use E4 command services, ensure your `META-INF/MANIFEST.MF` includes:

```
Require-Bundle: org.eclipse.e4.core.commands,
 org.eclipse.e4.core.contexts,
 org.eclipse.e4.core.di,
 org.eclipse.core.commands
```

And import the injection packages:

```
Import-Package: jakarta.inject;version="1.0.0",
 jakarta.annotation;version="1.1.0"
```

## Differences from E3 ICommandService and IHandlerService

Key differences from Eclipse 3.x:

1. **Service Names**: `ECommandService` and `EHandlerService` (E4) vs. `ICommandService` and `IHandlerService` (E3)

2. **Method Names**: 
   - E4: `commandService.createCommand()` → `handlerService.executeHandler()`
   - E3: `commandService.getCommand()` → `handlerService.executeCommand()`

3. **Dependency Injection**: E4 services are injected via `@Inject`, E3 services obtained via `getSite().getService()`

4. **Context Handling**: E4 uses `IEclipseContext`, E3 uses `IEvaluationContext`

For migrating from E3 to E4, see the [Eclipse4 Migration Guide](Eclipse4_Migration.md).

## Additional Resources

- [Eclipse4 Migration Guide](Eclipse4_Migration.md) - Migrating from E3 to E4
- [Platform Command Framework](PlatformCommandFramework.md) - E3 command framework
- [Eclipse4 RCP FAQ](Eclipse4_RCP_FAQ.md) - Common E4 questions
- [Eclipse4 RCP Dependency Injection](Eclipse4_RCP_Dependency_Injection.md) - DI details
- [Vogella Tutorial on Commands and Handlers](https://www.vogella.com/tutorials/Eclipse4Services/article.html#command-and-handler-service)

---

*This guide is maintained as part of the Eclipse Platform UI project.*
