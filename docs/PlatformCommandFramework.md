Platform Command Framework
==========================

Contents
--------

*   [1 Architecture Overview](#Architecture-Overview)
*   [2 Eclipse Commands Tutorial](#Eclipse-Commands-Tutorial)
*   [3 Other Resources](#Other-Resources)
*   [4 Commands](#Commands)
    *   [4.1 Executing a command with parameters](#Executing-a-command-with-parameters)
    *   [4.2 Using an IActionDelegate to execute a command](#Using-an-IActionDelegate-to-execute-a-command)
        *   [4.2.1 Generic Command Action Delegate](#Generic-Command-Action-Delegate)
*   [5 Handlers](#Handlers)
*   [6 KeyBindings](#KeyBindings)
*   [7 Contexts](#Contexts)
*   [8 Menu Contributions](#Menu-Contributions)
*   [9 Tracing Option](#Tracing-Option)

Architecture Overview
=====================

[http://dev.eclipse.org/viewcvs/index.cgi/~checkout~/platform-ui-home/R3\_1/contributions-proposal/requestForComments\_html_m41374bdb.png](http://dev.eclipse.org/viewcvs/index.cgi/~checkout~/platform-ui-home/R3_1/contributions-proposal/requestForComments_html_m41374bdb.png)

Figure 1: High Level Architecture

Eclipse Commands Tutorial
=========================

[http://www.vogella.de/articles/EclipseCommands/article.html](http://www.vogella.de/articles/EclipseCommands/article.html) Tutorial about using Eclipse Commands by Lars Vogel

Other Resources
===============

[Commands in Action](http://blog.eclipse-tips.com/search/label/Commands) by [Prakash G.R.](http://blog.eclipse-tips.com/):

*   [Commands Part 1: Actions Vs Commands](http://blog.eclipse-tips.com/2009/01/commands-part-1-actions-vs-commands.html)
*   [Commands Part 2: Selection and Enablement of IHandlers](http://blog.eclipse-tips.com/2009/01/commands-part-2-selection-and.html)
*   [Commands Part 3: Parameters for Commands](http://blog.eclipse-tips.com/2008/12/commands-part-3-parameters-for-commands.html)
*   [Commands Part 4: Misc items ...](http://blog.eclipse-tips.com/2009/01/commands-part-4-misc-items.html)
*   [Commands Part 5: Authentication in RCP applications](http://blog.eclipse-tips.com/2009/02/commands-part-5-authentication-in-rcp.html)
*   [Commands Part 6: Toggle & Radio menu contributions](http://blog.eclipse-tips.com/2009/03/commands-part-6-toggle-radio-menu.html)
*   [Commands Part 7: Adding standard commands](http://blog.eclipse-tips.com/2009/05/commands-part-7-adding-standard.html)
*   [Keyboard accessibility thru Command Framework](http://blog.eclipse-tips.com/2009/06/keyboard-accessibility-thru-command.html)
*   [Toggle Commands the toggle other contributions](http://blog.eclipse-tips.com/2009/12/toggle-commands-toggle-other.html)

[Command Core Expressions](http://wiki.eclipse.org/Command_Core_Expressions)

[Platform Expression Framework](http://wiki.eclipse.org/Platform_Expression_Framework)

Commands
========

Commands are managed by the **org.eclipse.ui.commands** extension point and the ICommandService.

An example of using the extension point to create a command:

    <extension
           point="org.eclipse.ui.commands">
        <category
              description="Actions take at lunch time."
              id="z.ex.view.keybindings.category"
              name="Lunch">
        </category>
        <command
              categoryId="z.ex.view.keybindings.category"
              description="Go for the taco."
              id="z.ex.view.keybindings.eatTaco"
              name="Eat That Taco">
        </command>
    </extension>

You can programmatically create commands as well. From within a view:

    ICommandService cmdService = (ICommandService) getSite().getService(
        ICommandService.class);
    Category lunch = cmdService
        .getCategory("z.ex.view.keybindings.category");
    if (!lunch.isDefined()) {
      lunch.define("Lunch", "Actions take at lunch time.");
    }
    Command eatTaco = cmdService
        .getCommand("z.ex.view.keybindings.eatTaco");
    if (!eatTaco.isDefined()) {
      eatTaco.define("Eat That Taco", "Go for the taco.", lunch);
    }

Note, however, that a plugin that programmatically defines commands is responsible for cleaning them up if the plugin is ever unloaded.

Also, like IAction you can execute a command directly ... but to get the proper environment it's better to execute it through the IHandlerService. See [#Handlers](#Handlers).

  

Executing a command with parameters
-----------------------------------

When a Command specifies its parameters, it can also specify a parameter type and/or some valid values. For example, the showView command.

    <command
          name="%command.showView.name"
          description="%command.showView.description"
          categoryId="org.eclipse.ui.category.views"
          id="org.eclipse.ui.views.showView"
          defaultHandler="org.eclipse.ui.handlers.ShowViewHandler">
        <commandParameter
            id="org.eclipse.ui.views.showView.viewId"
            name="%command.showView.viewIdParameter"
            values="org.eclipse.ui.internal.registry.ViewParameterValues" />
    </command>

To execute this command, you need to create a ParameterizedCommand with a Parameterization (an instance of a parameter and its value).

     		ICommandService commandService = ...;
     		IHandlerService handlerService = ...;
     		Command showView = commandService
     				.getCommand("org.eclipse.ui.views.showView");
     		IParameter viewIdParm = showView
     				.getParameter("org.eclipse.ui.views.showView.viewId");
     
     		// the viewId parameter provides a list of valid values ... if you
     		// knew the id of the problem view, you could skip this step.
     		// This method is supposed to be used in places like the keys
     		// preference page, to allow the user to select values
     		IParameterValues parmValues = viewIdParm.getValues();
     		String viewId = null;
     		Iterator i = parmValues.getParameterValues().values().iterator();
     		while (i.hasNext()) {
     			String id = (String) i.next();
     			if (id.indexOf("ProblemView") != -1) {
     				viewId = id;
     				break;
     			}
     		}
     
     		Parameterization parm = new Parameterization(viewIdParm, viewId);
     		ParameterizedCommand parmCommand = new ParameterizedCommand(
     				showView, new Parameterization[] { parm });
     
     		handlerService.executeCommand(parmCommand, null);

This executes the showView command with the problem view id. This is done for us when declaratively specifying a keybinding.

    <key
          sequence="M2+M3+Q X"
          contextId="org.eclipse.ui.contexts.window"
          commandId="org.eclipse.ui.views.showView"
          schemeId="org.eclipse.ui.defaultAcceleratorConfiguration">
        <parameter 
            id="org.eclipse.ui.views.showView.viewId"
            value="org.eclipse.ui.views.ProblemView" />
    </key>

Using an IActionDelegate to execute a command
---------------------------------------------

In 3.1 and 3.2 there is no declarative support for a menu item to execute a command. But you can write an IActionDelegate (like GenericCommandActionDelegate) that can be used in the standard extension points (org.eclipse.ui.actionSets, org.eclipse.ui.popupMenus, org.eclipse.ui.editorActions, and org.eclipse.ui.viewActions) and use it to execute the command.

We need to do these things to wire our command to a menu item:

1.  Define an `action` in an extension point using plugin.xml markup
2.  Connect this action to our IActionDelegate instance (also in plugin.xml)
3.  Tell our IActionDelegate which command to execute (also in plugin.xml)
4.  Code the IActionDelegate class to perform the command execution. (see code example below).

For example, in the above section we saw the showView command takes one parameter, the view id. Here is how we create an Action to execute it:

    <action
      id="org.eclipse.ui.examples.actions.showOutlineView"
      label="Show View:Outline"
      menubarPath="org.eclipse.ui.examples.actions.showViewMenu/additions"
      style="push">
      <class class="org.eclipse.ui.tests.api.GenericCommandActionDelegate">
         <parameter name="commandId" 
                              value="org.eclipse.ui.views.showView"/>
         <parameter name="org.eclipse.ui.views.showView.viewId" 
                              value="org.eclipse.ui.views.ContentOutline"/>
      </class>
    </action>
    <action
      id="org.eclipse.ui.examples.actions.showBookmarkView"
      label="Show View:Bookmark"
      menubarPath="org.eclipse.ui.examples.actions.showViewMenu/additions"
      style="push">
      <class class="org.eclipse.ui.tests.api.GenericCommandActionDelegate">
          <parameter name="commandId" 
                              value="org.eclipse.ui.views.showView"/>
          <parameter name="org.eclipse.ui.views.showView.viewId" 
                              value="org.eclipse.ui.views.BookmarkView"/>
      </class>
    </action>

**Notes:**

*   For commands without parameters, you can use the class attribute short from:

    class="org.eclipse.ui.tests.api.GenericCommandActionDelegate:my.commandId"

*   your action definition looks more like a keybinding definition. You are specifying the command id and any parameters needed for that action.
*   We specifed the action using the <class/> element instead of the class attribute ... you'll get a couple of warnings, ignore them.

*   The **definitionId** that is currently in the <action/> element is for linking up legacy actions to a keybinding through a command. For example, you've had a ShowMyViewActionDelegate since 3.0, and you want to attach a keybinding to it. We don't want to use that.

*   We have just shown the plugin.xml markup to trigger a command from within an action. Since the action can be inserted into a menu, this approach allows commands to be triggered by menu items. The glue between actions and commands is a single generic IActionDelegate implementation (shown below). All of the specific behavior code is now unified in the command and its handler. We no longer need an action delegate with a specific run() method for every menu item; consequently we don't need to link ActionSets to commands with the definitionId.

**Known Issues:**

When you start up eclipse you'll get warnings about your actions not having a class attribute. It's not an error, and won't effect the action performance.

You'll also see the actions in the Uncategorized section of the keybindings page. You can bind keys to them but they won't work from the keybinding. That's OK, you should be binding keys to the command not the action.

  

### Generic Command Action Delegate

We'll need a more robust implementation, but in 3.2 your action delegate needs to look something like the class below. I've only tested this with org.eclipse.ui.actionSets, but it should work with the viewActions, editorActions, and popupMenus extension points as well. The latest version of the code lives in HEAD in the org.eclipse.ui.tests plugin: [GenericCommandActionDelegate.java](https://git.eclipse.org/c/platform/eclipse.platform.ui.git/tree/tests/org.eclipse.ui.tests/Eclipse%20UI%20Tests/org/eclipse/ui/tests/api/GenericCommandActionDelegate.java)

     /*******************************************************************************
      * Copyright (c) 2006 IBM Corporation and others.
      * All rights reserved. This program and the accompanying materials
      * are made available under the terms of the Eclipse Public License v1.0
      * which accompanies this distribution, and is available at
      * http://www.eclipse.org/legal/epl-v10.html
      *
      * Contributors:
      *     IBM Corporation - initial API and implementation
      *******************************************************************************/
     package org.eclipse.ui.tests.api;
     
     import java.util.ArrayList;
     import java.util.Iterator;
     import java.util.Map;
     
     import org.eclipse.core.commands.Command;
     import org.eclipse.core.commands.IParameter;
     import org.eclipse.core.commands.Parameterization;
     import org.eclipse.core.commands.ParameterizedCommand;
     import org.eclipse.core.commands.common.NotDefinedException;
     import org.eclipse.core.runtime.CoreException;
     import org.eclipse.core.runtime.IConfigurationElement;
     import org.eclipse.core.runtime.IExecutableExtension;
     import org.eclipse.jface.action.IAction;
     import org.eclipse.jface.viewers.ISelection;
     import org.eclipse.ui.IEditorActionDelegate;
     import org.eclipse.ui.IEditorPart;
     import org.eclipse.ui.IObjectActionDelegate;
     import org.eclipse.ui.IViewActionDelegate;
     import org.eclipse.ui.IViewPart;
     import org.eclipse.ui.IWorkbenchPart;
     import org.eclipse.ui.IWorkbenchWindow;
     import org.eclipse.ui.IWorkbenchWindowActionDelegate;
     import org.eclipse.ui.commands.ICommandService;
     import org.eclipse.ui.handlers.IHandlerService;
     
     /**
      * This action delegate can be used to specify a command with or without
      * parameters be called from an &lt;action/&gt; specified in actionSets,
      * editorActions, viewActions, or popupMenus.
      */
     public class GenericCommandActionDelegate implements
             IWorkbenchWindowActionDelegate, IViewActionDelegate,
             IEditorActionDelegate, IObjectActionDelegate, IExecutableExtension {
     
         private static final String PARM_COMMAND_ID = "commandId";
     
         private String commandId = null;
     
         private Map parameterMap = null;
     
         private ParameterizedCommand parameterizedCommand = null;
     
         private IHandlerService handlerService = null;
     
         @Override 
         public void dispose() {
             handlerService = null;
             parameterizedCommand = null;
             parameterMap = null;
         }
     
         @Override 
         public void run(IAction action) {
             if (handlerService == null) {
                 // what, no handler service ... no problem
                 return;
             }
             try {
                 if (commandId != null) {
                     handlerService.executeCommand(commandId, null);
                 } else if (parameterizedCommand != null) {
                     handlerService.executeCommand(parameterizedCommand, null);
                 }
                 // else there is no command for this delegate
             } catch (Exception e) {
                 // exceptions reduced for brevity
                 // and we won't just do a print out :-)
             }
         }
     
         /*
          * (non-Javadoc)
          * 
          * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
          *      org.eclipse.jface.viewers.ISelection)
          */
         public void selectionChanged(IAction action, ISelection selection) {
             // we don't care, handlers get their selection from the
             // ExecutionEvent application context
         }
     
         @Override 
         public void setInitializationData(IConfigurationElement config,
                 String propertyName, Object data) throws CoreException {
             String id = config.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
             // save the data until our init(*) call, where we can get
             // the services.
             if (data instanceof String) {
                 commandId = (String) data;
             } else if (data instanceof Map) {
                 parameterMap = (Map) data;
                 if (parameterMap.get(PARM_COMMAND_ID) == null) {
                     Status status = new Status(IStatus.ERROR,
                             "org.eclipse.ui.tests", "The '" + id
                                     + "' action won't work without a commandId");
                     throw new CoreException(status);
                 }
             } else {
                 Status status = new Status(
                         IStatus.ERROR,
                         "org.eclipse.ui.tests",
                         "The '"
                                 + id
                                 + "' action won't work without some initialization parameters");
                 throw new CoreException(status);
             }
         }
     
         /**
          * Build a command from the executable extension information.
          * 
          * @param commandService
          *            to get the Command object
          */
         private void createCommand(ICommandService commandService) {
             String id = (String) parameterMap.get(PARM_COMMAND_ID);
             if (id == null) {
                 return;
             }
             if (parameterMap.size() == 1) {
                 commandId = id;
                 return;
             }
             try {
                 Command cmd = commandService.getCommand(id);
                 if (!cmd.isDefined()) {
                     // command not defined? no problem ...
                     return;
                 }
                 ArrayList parameters = new ArrayList();
                 Iterator i = parameterMap.entrySet().iterator();
                 while (i.hasNext()) {
                     Map.Entry entry = (Map.Entry) i.next();
                     String parmName = (String) entry.getKey();
                     if (PARM_COMMAND_ID.equals(parmName)) {
                         continue;
                     }
                     IParameter parm = cmd.getParameter(parmName);
                     if (parm == null) {
                         // asking for a bogus parameter? No problem
                         return;
                     }
                     parameters.add(new Parameterization(parm, (String) entry.getValue()));
                 }
                 parameterizedCommand = new ParameterizedCommand(cmd,
                         (Parameterization[]) parameters
                                 .toArray(new Parameterization[parameters.size()]));
             } catch (NotDefinedException e) {
                 // command is bogus? No problem, we'll do nothing.
             }
         }
     
         @Override 
         public void init(IWorkbenchWindow window) {
             if (handlerService != null) {
                 // already initialized
                 return;
             }
     
             handlerService = (IHandlerService) window
                     .getService(IHandlerService.class);
             if (parameterMap != null) {
                 ICommandService commandService = (ICommandService) window
                         .getService(ICommandService.class);
                 createCommand(commandService);
             }
         }
     
         @Override 
         public void init(IViewPart view) {
             init(view.getSite().getWorkbenchWindow());
         }
     
         @Override 
         public void setActiveEditor(IAction action, IEditorPart targetEditor) {
             // we don't actually care about the active editor, since that
             // information is in the ExecutionEvent application context
             // but we need to make sure we're initialized.
             if (targetEditor != null) {
                 init(targetEditor.getSite().getWorkbenchWindow());
             }
         }
     
         @Override 
         public void setActivePart(IAction action, IWorkbenchPart targetPart) {
             // we don't actually care about the active part, since that
             // information is in the ExecutionEvent application context
             // but we need to make sure we're initialized.
             if (targetPart != null) {
                 init(targetPart.getSite().getWorkbenchWindow());
             }
         }
     }

Handlers
========

Handlers are managed by the **org.eclipse.ui.handlers** extension point and the IHandlerService. Many Handlers can register for a command. At any give time, either 0 or 1 handlers will be active for the command. A handler's active state and enabled state can be controlled declaratively. See [Command Core Expressions](/Command_Core_Expressions "Command Core Expressions") for a more complex description of the declarative expressions. Handlers are responsible for interpreting any optional command parameters using the ExecutionEvent parameter.

    <extension
           point="org.eclipse.ui.handlers">
        <handler
              class="z.ex.view.keybindings.handlers.TacoHandler"
              commandId="z.ex.view.keybindings.eatTaco">
           <activeWhen>
              <with variable="activeContexts">
                 <iterate operator="or">
                    <equals value="z.ex.view.keybindings.contexts.taco"/>
                 </iterate>
              </with>
           </activeWhen>
        </handler>
    </extension>

Here the handler is checking the activeContexts variable (See [org.eclipse.ui.ISources](http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/ISources.html)) and if the "taco" context is active, the handler is active.

The handler itself, **TacoHandler**, must implement IHandler but would usually be derived from the abstract base class [org.eclipse.core.commands.AbstractHandler](http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/commands/AbstractHandler.html).

You can create and activate a handler programmatically:

    IHandlerService handlerService = (IHandlerService) getSite()
        .getService(IHandlerService.class);
    IHandler handler = new AbstractHandler() {
      public Object execute(ExecutionEvent event)
              throws ExecutionException {
        System.out.println("Eat that Taco");
        return null;
      }
    };
    handlerService
        .activateHandler("z.ex.view.keybindings.eatTaco", handler);

As of 3.2 (and later releases) we should be calling the IHandlerService to run commands. We should not call the Command object execute method itself.

     handlerService.executeCommand("z.ex.view.keybindings.eatTaco", null);

In 3.1 it is still necessary to call the Command object directly since the IHandlerService didn't support executeCommand(*). But you can provide almost the same execution environment.

    Command eatTaco = cmdService
       .getCommand("z.ex.view.keybindings.eatTaco");
    eatTaco.execute(new ExecutionEvent(Collections.EMPTY_MAP, null, handlerService.getCurrentState()));

If you want the handler to evaluate an enablement expression you can do that using the expression parameter of the activateHandler() method. Here is how you need to setup your expression in order to properly work tracking selection changes:

    Expression expr = new Expression() {
        public final EvaluationResult evaluate(final IEvaluationContext context) {
            Object sel = context.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
            (return EvaluationResult.TRUE/FALSE depending on how much you like the selection)
        }
        public void collectExpressionInfo(final ExpressionInfo info) {
             // You need this to cause the expr to be evaluated on selection events
             info.markDefaultVariableAccessed();
        }
    };

KeyBindings
===========

KeyBindings are managed by the **org.eclipse.ui.bindings** extension point and the IBindingService. Keybindings cannot be updated programmatically.

    <extension
           point="org.eclipse.ui.bindings">
        <key
              commandId="z.ex.view.keybindings.eatTaco"
              contextId="z.ex.view.keybindings.contexts.taco"
              schemeId="org.eclipse.ui.defaultAcceleratorConfiguration"
              sequence="CTRL+3">
        </key>
    </extension>

A key binding is active when the context is active. A keybinding is associated with a command (with optional parameters specified by parameter id and value). If a command has a handler while the keybinding is invoked, the handler extracts the command parameters specified by the keybinding from the ExecutionEvent and invokes the appropriate action.

Contexts
========

Contexts are managed by the **org.eclipse.ui.contexts** extension point and the IContextService.

Most contexts are created by the extension point, and activated programmatically when appropriate. But you can create contexts programmatically as well. The active contexts usually form a tree, although in the case of keybindings this tree is narrowed down to a branch.

    <extension
           point="org.eclipse.ui.contexts">
        <context
              description="To allow the consumption of Tacos"
              id="z.ex.view.keybindings.contexts.taco"
              name="Mexican Food"
              parentId="org.eclipse.ui.contexts.window">
        </context>
    </extension>

For a context that was attached to a view, it would normally be activated in the view's createPartControl(*) method.

    IContextService contextService = (IContextService) getSite()
      .getService(IContextService.class);
    IContextActivation contextActivation = contextService.activateContext("z.ex.view.keybindings.contexts.taco");

You can only de-activate a context that you are responsible for activating.

Programmatically, you can create contexts:

    Context tacos = contextService
        .getContext("z.ex.view.keybindings.contexts.taco");
    if (!tacos.isDefined()) {
      tacos.define("Mexican Food", "To allow the consumption of Tacos",
          "org.eclipse.ui.contexts.window");
    }

Note, however, that a plugin that programmatically defines contexts is responsible for cleaning them up if the plugin is ever unloaded.

Menu Contributions
==================

See [Menu Contributions](/Menu_Contributions "Menu Contributions")

Tracing Option
==============

There are a couple of reasons why keybindings and commands might not work.

1.  Keybindings are in a context that is not active
2.  There is a keybinding conflict
3.  No handler is currently active for the command
4.  There is a handler conflict

To help track down the problem, you can run with debug tracing options. For example:

    org.eclipse.ui/debug=true
    org.eclipse.ui/trace/keyBindings=true
    org.eclipse.ui/trace/keyBindings.verbose=true
    org.eclipse.ui/trace/sources=true
    org.eclipse.ui/trace/handlers=true
    org.eclipse.ui/trace/handlers.verbose=true
    #org.eclipse.ui/trace/handlers.verbose.commandId=org.eclipse.ui.edit.copy
    org.eclipse.ui/trace/handlers.verbose.commandId=org.eclipse.jdt.ui.navigate.open.type
    org.eclipse.ui/trace/contexts=true
    org.eclipse.ui/trace/contexts.verbose=true
  
I put these options in a **debug.options** file and run eclipse using:

    bash$ eclipse -debug debug.options -data /opt/local/pw_workspace >debug.log 2>&1

This logs the debug output to the debug.log file. This works on windows as well:

    C:\development> eclipse33\eclipsec.exe -debug debug.options -data workspaces\pw_workspace >debug.log 2>&1

**handlers.verbose.commandId** allows you to track the information about a specific command that isn't working. org.eclipse.jdt.ui.navigate.open.type is the open type dialog (normally CTRL+SHIFT+T) and org.eclipse.ui.edit.copy (commented out) is COPY (normally CTRL+C)

