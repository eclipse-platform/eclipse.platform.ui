

Menu Contributions/Toggle Button Command
========================================

You can create a command with a required parameter. The parameter will be passed during every execution.

Command Definition
------------------

Define a command with a state. The state id should be org.eclipse.ui.commands.toggleState.

```xml
    <command
    defaultHandler="org.eclipse.examples.BoldHandler"
    id="org.eclipse.examples.boldCommand"
    name="Bold">
      <state
    class="org.eclipse.ui.handlers.RegistryToggleState:true"
    id="org.eclipse.ui.commands.toggleState">
      </state>
    </command>
```

The state can be initialized with a default value, which reflects in the Menu

Handler
-------

The handler will receive the state. Its the responsibility of the handler to update the state of the command.

```java
    public class CheckHandler extends AbstractHandler{
     
    public Object execute(ExecutionEvent event) throws ExecutionException {
     
        Command command = event.getCommand();
        boolean oldValue = HandlerUtil.toggleCommandState(command);
        // use the old value and perform the operation
     
        return null;
      }
     
    }
```

Menu Contribution
-----------------

Then you add menu contributions as you would do with any command, except you need to set the style to toggle:

```xml
    <menuContribution
    locationURI="menu:help?after=additions">
        <command
    commandId="org.eclipse.examples.boldCommand"
    label="Bold"
    style="toggle">
        </command>
```
