/*
 * Created on Jul 21, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.internal.commands.old;

/**
 * @author cmclaren
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Command2 {

	/*
	private ICommandEvent commandEvent;
	private List commandListeners;
	private CommandManager commandManager;
	private String id;

	Command(CommandManager commandManager, String id) {
		super();
		this.commandManager = commandManager;
		this.id = id;
	}

	public void addCommandListener(ICommandListener commandListener)
		throws IllegalArgumentException {
		if (commandListener == null)
			throw new IllegalArgumentException();
		
		if (commandListeners == null)
			commandListeners = new ArrayList();
		
		if (!commandListeners.contains(commandListener))
			commandListeners.add(commandListener);
	}

	public ICommandDelegate getCommandHandler()
		throws NotDelegatedException {
		SortedMap commandHandlersById = commandManager.getCommandHandlersById();
		ICommandDelegate commandHandler = (ICommandDelegate) commandHandlersById.get(id);
		
		if (commandHandlersById.containsKey(id))
			return commandHandler;
		else 		
			throw new NotDelegatedException();
	}

	public String getDescription() 
		throws NotDefinedException {
		CommandElement commandElement = (CommandElement) commandManager.getCommandElement(id);

		if (commandElement != null && commandManager.getDefinedCommandIds().contains(id))
			return commandElement.getDescription();
		else 
			throw new NotDefinedException();
	}

	public SortedSet getContextBindings() {
		return Util.EMPTY_SORTED_SET;
	}

	public SortedSet getGestureBindings() {
		return Util.EMPTY_SORTED_SET;
	}
		
	public String getId() {
		return id;
	}

	public SortedSet getImageBindings() {
		return Util.EMPTY_SORTED_SET;		
	}

	public SortedSet getKeyBindings() {
		return Util.EMPTY_SORTED_SET;		
	}

	public String getName() 
		throws NotDefinedException {
		CommandElement commandElement = (CommandElement) commandManager.getCommandElement(id);

		if (commandElement != null && commandManager.getDefinedCommandIds().contains(id))
			return commandElement.getName();
		else 
			throw new NotDefinedException();
	}

	public String getParentId() 
		throws NotDefinedException {
		CommandElement commandElement = (CommandElement) commandManager.getCommandElement(id);

		if (commandElement != null && commandManager.getDefinedCommandIds().contains(id))
			return commandElement.getParentId();
		else 
			throw new NotDefinedException();
	}

	public String getPluginId() 
		throws NotDefinedException {
		CommandElement commandElement = (CommandElement) commandManager.getCommandElement(id);

		if (commandElement != null && commandManager.getDefinedCommandIds().contains(id))
			return commandElement.getPluginId();
		else 
			throw new NotDefinedException();
	}

	public boolean isDefined() {
		return commandManager.getCommandElement(id) != null && commandManager.getDefinedCommandIds().contains(id);
	}

	public boolean isHandled() {
		return commandManager.getCommandHandlersById().containsKey(id);
	}

	public void removeCommandListener(ICommandListener commandListener)
		throws IllegalArgumentException {
		if (commandListener == null)
			throw new IllegalArgumentException();

		if (commandListeners != null) {
			commandListeners.remove(commandListener);
			
			if (commandListeners.isEmpty())
				commandListeners = null;
		}
	}
	
	void fireCommandChanged() {
		if (commandListeners != null) {
			// TODO copying to avoid ConcurrentModificationException
			Iterator iterator = new ArrayList(commandListeners).iterator();
			
			if (iterator.hasNext()) {
				if (commandEvent == null)
					commandEvent = new CommandEvent(this);
				
				while (iterator.hasNext())	
					((ICommandListener) iterator.next()).commandChanged(commandEvent);
			}							
		}			
	}
	*/		
}
