package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.CommandHandlerServiceEvent;
import org.eclipse.ui.commands.CommandManagerEvent;
import org.eclipse.ui.commands.ICommandHandlerServiceListener;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.commands.IWorkbenchWindowCommandSupport;
import org.eclipse.ui.contexts.ContextActivationServiceEvent;
import org.eclipse.ui.contexts.ContextManagerEvent;
import org.eclipse.ui.contexts.IContextActivationServiceListener;
import org.eclipse.ui.contexts.IContextManagerListener;
import org.eclipse.ui.contexts.IWorkbenchWindowContextSupport;
import org.eclipse.ui.internal.commands.CommandManager;

public class WorkbenchCommandsAndContexts {
	final ICommandHandlerServiceListener commandHandlerServiceListener =
		new ICommandHandlerServiceListener() {
		public void commandHandlerServiceChanged(CommandHandlerServiceEvent commandHandlerServiceEvent) {
			WorkbenchCommandsAndContexts.this.commandHandlerServiceChanged();
		}
	};
	final ICommandManagerListener commandManagerListener =
		new ICommandManagerListener() {
		public final void commandManagerChanged(final CommandManagerEvent commandManagerEvent) {
			updateActiveWorkbenchWindowMenuManager(false);
		}
	};
	final IContextActivationServiceListener contextActivationServiceListener =
		new IContextActivationServiceListener() {
		public void contextActivationServiceChanged(ContextActivationServiceEvent contextActivationServiceEvent) {
			WorkbenchCommandsAndContexts.this.contextActivationServiceChanged();
		}
	};
	final IContextManagerListener contextManagerListener =
		new IContextManagerListener() {
		public final void contextManagerChanged(final ContextManagerEvent contextManagerEvent) {
			updateActiveWorkbenchWindowMenuManager(false);
		}
	};
	final IWindowListener windowListener = new IWindowListener() {
		public void windowActivated(IWorkbenchWindow workbenchWindow) {
			if (workbenchWindow != null) {
				IWorkbenchWindowCommandSupport workbenchWindowCommandSupport =
				(IWorkbenchWindowCommandSupport) workbenchWindow.getCommandSupport();

				/* TODO: this should be looked at.
				* right now, its how the the active workbench window is monitored
				* for additions and removals of commands without part change.
				* there should be a corresponding remove to save computation time,
				* although the update* methods should work in terms of the real
				* active window at all times anyways. 
				*/  
				if (workbenchWindowCommandSupport != null)
					workbenchWindowCommandSupport.getCommandHandlerService().addCommandHandlerServiceListener(commandHandlerServiceListener);	

				IWorkbenchWindowContextSupport workbenchWindowContextSupport =
				(IWorkbenchWindowContextSupport) workbenchWindow.getContextSupport();

				/* TODO: this should be looked at.
				* right now, its how the the active workbench window is monitored
				* for additions and removals of contexts without part change.
				* there should be a corresponding remove to save computation time,
				* although the update* methods should work in terms of the real
				* active window at all times anyways. 
				*/  
				if (workbenchWindowContextSupport != null)
					workbenchWindowContextSupport.getContextActivationService().addContextActivationServiceListener(contextActivationServiceListener);	
			}
			 			
			commandHandlerServiceChanged();
			contextActivationServiceChanged();
			updateActiveWorkbenchWindowMenuManager(true);
		}

		public void windowClosed(IWorkbenchWindow workbenchWindow) {
			commandHandlerServiceChanged();
			contextActivationServiceChanged();
			updateActiveWorkbenchWindowMenuManager(true);
		}

		public void windowDeactivated(IWorkbenchWindow workbenchWindow) {
			commandHandlerServiceChanged();
			contextActivationServiceChanged();
			updateActiveWorkbenchWindowMenuManager(true);
		}

		public void windowOpened(IWorkbenchWindow workbenchWindow) {
			commandHandlerServiceChanged();
			contextActivationServiceChanged();
			updateActiveWorkbenchWindowMenuManager(true);
		}
	};
	Workbench workbench;

	WorkbenchCommandsAndContexts(Workbench workbench) {
		this.workbench = workbench;
	}

	public void commandHandlerServiceChanged() {
		//System.out.println("commandHandlerServiceChanged()");				
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		Map handlersByCommandId = new HashMap();
		
		if (workbenchWindow != null) {
			IWorkbenchWindowCommandSupport workbenchWindowCommandSupport =
				(IWorkbenchWindowCommandSupport) workbenchWindow.getCommandSupport();

			if (workbenchWindowCommandSupport != null) {
				handlersByCommandId =
					workbenchWindowCommandSupport
					.getCommandHandlerService()
					.getHandlersByCommandId();
			}
		}

		((CommandManager) workbench.getCommandSupport().getCommandManager()).setActionsById(handlersByCommandId);
	}
	
	public void contextActivationServiceChanged() {
		//System.out.println("contextActivationServiceChanged()");		
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		Set activeContextIds = new HashSet();
		
		if (workbenchWindow != null) {
			IWorkbenchWindowContextSupport workbenchWindowContextSupport =
				(IWorkbenchWindowContextSupport) workbenchWindow.getContextSupport();

			if (workbenchWindowContextSupport != null) {
				activeContextIds =
					workbenchWindowContextSupport
					.getContextActivationService()
					.getActiveContextIds();
			}
		}

		workbench.getCommandSupport().getCommandManager().setActiveActivityIds(activeContextIds);
	}

	public void updateActiveWorkbenchWindowMenuManager(boolean textOnly) {
		//System.out.println("updateActiveWorkbenchWindowMenuManager(" + textOnly + ")");
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

		if (workbenchWindow instanceof WorkbenchWindow) {
			MenuManager menuManager =
				((WorkbenchWindow) workbenchWindow).getMenuManager();
			
			if (textOnly)
				menuManager.update(IAction.TEXT);
			else 
				menuManager.updateAll(true);	
		}
	}
}
