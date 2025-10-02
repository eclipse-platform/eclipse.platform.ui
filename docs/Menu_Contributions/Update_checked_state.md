Menu Contributions/Update checked state
=======================================

Update the toolbar item checked state
=====================================

The active handler can update any UI elements registered against the its command. It does this by requesting the ICommandService to refresh any registered UIElements.

As a handler becomes active and implement IElementUpdater like org.eclipse.ui.tests.menus.ToggleContextHandler, the command service calls `public void updateElement(UIElement element, Map parameters)` for every UIElement registered against the command.

```java
    public class ToggleContextHandler extends AbstractHandler implements
        IElementUpdater {
      private static final String TOGGLE_ID = "toggleContext.contextId";
      // ...
      public void updateElement(UIElement element, Map parameters) {
        // the checked state depends on if we have an activation for that
        // context ID or not
        String contextId = (String) parameters.get(TOGGLE_ID);
        element.setChecked(contextActivations.get(contextId) != null);
      }
    }
```
