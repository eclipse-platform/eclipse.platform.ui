package org.eclipse.e4.ui.examples.css.rcp;

/**
 * Interface defining the application's command IDs.
 * Key bindings can be defined for specific commands.
 * To associate an action with a command, use IAction.setActionDefinitionId(commandId).
 *
 * @see org.eclipse.jface.action.IAction#setActionDefinitionId(String)
 */
public interface ICommandIds {

    public static final String CMD_OPEN = "org.eclipse.e4.ui.examples.css.rcp.open";
    public static final String CMD_MARK_AS_READ = "org.eclipse.e4.ui.examples.css.rcp.markAsRead";
    
}
