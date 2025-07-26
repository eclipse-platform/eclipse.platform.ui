Menu Contributions/Radio Button Command
=======================================


Eclipse 3.5 or later
====================

You can create a command with a required parameter. The parameter will be passed during every execution.

Command Definition
------------------

Define a command with a state and a parameter. The state id should be org.eclipse.ui.commands.radioState and the parameter id should be org.eclipse.ui.commands.radioStateParameter.

    <command
    defaultHandler="com.example.RadioHandler"
    id="z.ex.dropdown.internal.RadioHandler"
    name="Radio Example">
      <commandParameter
    id="org.eclipse.ui.commands.radioStateParameter"
    name="Radio Name"
    optional="false">
      </commandParameter>
      <state
    class="org.eclipse.ui.handlers.RadioState:Moe"
    id="org.eclipse.ui.commands.radioState">
      </state>
    </command>

Alternatively, the state can be initialized with a default value, which will be checked initially in the Menu. Persistence and default value can be set by parameters for org.eclipse.ui.handlers.RadioState class. The above state section would then be replaced by the following.

    <state 
    id="org.eclipse.ui.commands.radioState"> 
      <class 
    class="org.eclipse.ui.handlers.RadioState"> 
          <parameter 
    name="default" 
    value="Moe"> 
          </parameter> 
          <parameter 
    name="persisted" 
    value="false"> 
          </parameter> 
      </class> 
    </state>

  

Handler
-------

The handler will receive the parameter. It can then update its model (in my example my **model** is a local variable, but that might not be appropriate in command that can have multiple handlers).

    package com.example;
     
    public class RadioHandler extends AbstractHandler{
     
    public Object execute(ExecutionEvent event) throws ExecutionException {
     
        if(HandlerUtil.matchesRadioState(event))
            return null; // we are already in the updated state - do nothing
     
        String currentState = event.getParameter(RadioState.PARAMETER_ID);
     
        // do whatever having "currentState" implies
     
        // and finally update the current state
        HandlerUtil.updateRadioState(event.getCommand(), currentState);
     
        return null;  
      }
     
    }

Menu Contribution
-----------------

Then you add menu contributions with the specific parameters that you want:

    <menuContribution
    locationURI="menu:help?after=additions">
        <separator
    name="z.ex.dropdown.menu.separator1"
    visible="true">
        </separator>
        <command
    commandId="z.ex.dropdown.radio"
    id="z.ex.dropdown.menu.radio1"
    label="Moe"
    style="radio">
            <parameter
    name="org.eclipse.ui.commands.radioStateParameter"
    value="Moe">
            </parameter>
        </command>
        <command
    commandId="z.ex.dropdown.radio"
    id="z.ex.dropdown.menu.radio2"
    label="Larry"
    style="radio">
            <parameter
    name="org.eclipse.ui.commands.radioStateParameter"
    value="Larry">
            </parameter>
        </command>
        <command
    commandId="z.ex.dropdown.radio"
    id="z.ex.dropdown.menu.radio3"
    label="Curly"
    style="radio">
            <parameter
    name="org.eclipse.ui.commands.radioStateParameter"
    value="Curly">
            </parameter>
        </command>
        <separator
    name="z.ex.dropdown.menu.separator2"
    visible="true">
        </separator>
    </menuContribution>

Eclipse 3.4 or earlier
======================

You can create a command with a required parameter. The parameter will be passed during every execution.

Command Definition
------------------

You want to create a command that will be executed with a paramter. The parameter in this example matches which of the radio buttons is selected.

    <command
    categoryId="org.eclipse.ui.category.help"
    defaultHandler="z.ex.dropdown.internal.RadioHandler"
    id="z.ex.dropdown.radio"
    name="Radio Example">
        <commandParameter
    id="z.ex.dropdown.radio.info"
    name="Radio Name"
    optional="false">
        </commandParameter>
    </command>

Handler
-------

The handler will receive the parameter. It can then update its model (in my example my **model** is a local variable, but that might not be appropriate in command that can have multiple handlers).

    package com.example.handlers.internal;
     
    import java.util.Map;
    import org.eclipse.core.commands.AbstractHandler;
    import org.eclipse.core.commands.ExecutionEvent;
    import org.eclipse.core.commands.ExecutionException;
    import org.eclipse.ui.commands.ICommandService;
    import org.eclipse.ui.commands.IElementUpdater;
    import org.eclipse.ui.handlers.HandlerUtil;
    import org.eclipse.ui.menus.UIElement;
     
    public class RadioHandler extends AbstractHandler implements IElementUpdater {
     
    private static final String PARM_INFO = "z.ex.dropdown.radio.info";
    private String fCurrentValue;
     
    public Object execute(ExecutionEvent event) throws ExecutionException {
      String parm = event.getParameter(PARM_INFO);
      if (parm.equals(fCurrentValue)) {
        return null; // in theory, we're already in the correct state
      }
     
      // do whatever having "parm" active implies
      fCurrentValue = parm;
     
     
      // update our radio button states ... get the service from
      // a place that's most appropriate
      ICommandService service = (ICommandService) HandlerUtil
          .getActiveWorkbenchWindowChecked(event).getService(
              ICommandService.class);
      service.refreshElements(event.getCommand().getId(), null);
      return null;
    }
     
    public void updateElement(UIElement element, Map parameters) {
      String parm = (String) parameters.get(PARM_INFO);
      if (parm != null) {
        if (fCurrentValue != null && fCurrentValue.equals(parm)) {
          element.setChecked(true);
        } else {
          element.setChecked(false);
        }
      }
    }
    }

Menu Contribution
-----------------

Then you add menu contributions with the specific parameters that you want:

    <menuContribution
    locationURI="menu:help?after=additions">
        <separator
    name="z.ex.dropdown.menu.separator1"
    visible="true">
        </separator>
        <command
    commandId="z.ex.dropdown.radio"
    id="z.ex.dropdown.menu.radio1"
    label="Moe"
    style="radio">
            <parameter
    name="z.ex.dropdown.radio.info"
    value="Moe">
            </parameter>
        </command>
        <command
    commandId="z.ex.dropdown.radio"
    id="z.ex.dropdown.menu.radio2"
    label="Larry"
    style="radio">
            <parameter
    name="z.ex.dropdown.radio.info"
    value="Larry">
            </parameter>
        </command>
        <command
    commandId="z.ex.dropdown.radio"
    id="z.ex.dropdown.menu.radio3"
    label="Curly"
    style="radio">
            <parameter
    name="z.ex.dropdown.radio.info"
    value="Curly">
            </parameter>
        </command>
        <separator
    name="z.ex.dropdown.menu.separator2"
    visible="true">
        </separator>
    </menuContribution>

Initializing the Handler
------------------------

It may happen that your radio menu contributions are not initialized the first time the menu is displayed. This is because at this time, your Handler might not yet have been instantiated (this is due to Eclipse's lazy loading policy). If this is the case, you can enforce the instantiation of your Handler within the Activator of your plug-in. Just add the following code to the start(BundleContext) method:

    UIJob job = new UIJob("InitCommandsWorkaround") {
     
        public IStatus runInUIThread(@SuppressWarnings("unused") IProgressMonitor monitor) {
     
            ICommandService commandService = (ICommandService) PlatformUI
                .getWorkbench().getActiveWorkbenchWindow().getService(
                    ICommandService.class);
            Command command = commandService.getCommand("z.ex.dropdown.radio");
            command.isEnabled();
            return new Status(IStatus.OK,
                "my.plugin.id",
                "Init commands workaround performed successfully");
        }
     
    };
    job.schedule();

