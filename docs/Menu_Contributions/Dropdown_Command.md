Menu Contributions/Dropdown Command
===================================

You can create dropdown commands using menu contributions, and then use multiple menu contributions to create the dropdown menu.

Contents
--------

*   [1 Command Definition](#Command-Definition)
*   [2 Handler](#Handler)
*   [3 Menu Contribution](#Menu-Contribution)
    *   [3.1 Toolbar declaration](#Toolbar-declaration)
    *   [3.2 Dropdown menu declaration](#Dropdown-menu-declaration)

Command Definition
------------------

Any command can be used as a toolbar dropdown ... the command itself is not aware of its dropdown rendering. If it were to need that information, 2 common ways of sharing it are:

1.  Create a parameterized command. The commands inserted in the dropdown menu would specify the parameter, and the dropdown tool item command would not.
2.  Back your handler with a model and rely on that information

This command declaration defines a parameter:

 

      <extension point="org.eclipse.ui.commands">
         <category id="z.ex.dropdown.category1" name="DropDown Examples">
         </category>
         <command categoryId="z.ex.dropdown.category1" defaultHandler="z.ex.dropdown.internal.DropDownHandler"  
                  id="z.ex.dropdown.command1" name="Drop">
            <commandParameter id="z.ex.dropdown.msg" name="Message" optional="true">
            </commandParameter>
         </command>
      </extension>
      <extension point="org.eclipse.ui.commandImages">
         <image commandId="z.ex.dropdown.command1" icon="icons/change_obj.gif">
         </image>
      </extension>
    

 

I've thrown in a default image for fun.

Handler
-------

The command example includes a default handler, which is common for simple global commands. The handler needs to check for the parameter and then do its stuff. Use org.eclipse.core.commands.AbstractHandler to use its default methods for most of the IHandler interface.

    public class DropDownHandler extends AbstractHandler {
      private static final String PARM_MSG = "z.ex.dropdown.msg";
    
      public Object execute(ExecutionEvent event) throws ExecutionException {
        String msg = event.getParameter(PARM_MSG);
        if (msg == null) {
          System.out.println("No message");
        } else {
          System.out.println("msg: " + msg);
        }
        return null;
      }
    }
    

 

As with all handlers, you can extract most of the workbench useful information out of the ExecutionEvent using HandlerUtil.

Menu Contribution
-----------------

Dropdown declarations come in two parts, the tool item declaration and a separate dropdown menu declaration.

### Toolbar declaration

You are just placing the command in the toolbar.

 

      <extension point="org.eclipse.ui.menus">
         <menuContribution locationURI="toolbar:org.eclipse.ui.main.toolbar?after=additions">
            <toolbar id="z.ex.dropdown.toolbar2">
               <command commandId="z.ex.dropdown.command1" id="z.ex.dropdown.toolbar.command1" style="pulldown" 
                        tooltip="Send them a message">
               </command>
            </toolbar>
         </menuContribution>
      </extension>
    

 

You can use an existing toolbar id or create a new toolbar to hold the command (as this example did).

### Dropdown menu declaration

Now you can provide one or more menu contributions to the dropdown menu. Here is an example of the main one:

 

      <extension point="org.eclipse.ui.menus">
         <menuContribution locationURI="menu:z.ex.dropdown.toolbar.command1">
            <command commandId="z.ex.dropdown.command1" label="Msg - hi" style="push">
               <parameter name="z.ex.dropdown.msg" value="Hello">
               </parameter>
            </command>
            <separator name="additions" visible="false">
            </separator>
            <command commandId="z.ex.dropdown.command1" label="Msg - bye" style="push">
               <parameter name="z.ex.dropdown.msg" value="Goodbye">
               </parameter>
            </command>
         </menuContribution>
      </extension>
    

 

