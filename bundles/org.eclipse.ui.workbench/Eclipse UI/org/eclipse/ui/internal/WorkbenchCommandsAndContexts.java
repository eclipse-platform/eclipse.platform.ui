package org.eclipse.ui.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.CommandManagerEvent;
import org.eclipse.ui.commands.IActionService;
import org.eclipse.ui.commands.IActionServiceEvent;
import org.eclipse.ui.commands.IActionServiceListener;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.contexts.ContextActivationServiceEvent;
import org.eclipse.ui.contexts.ContextManagerEvent;
import org.eclipse.ui.contexts.IContextActivationServiceListener;
import org.eclipse.ui.contexts.IContextManagerListener;
import org.eclipse.ui.contexts.IWorkbenchWindowContextSupport;
import org.eclipse.ui.internal.commands.ActionService;
import org.eclipse.ui.internal.commands.CommandManager;
import org.eclipse.ui.internal.util.Util;

public class WorkbenchCommandsAndContexts {

	IActionService actionService;

	IActionServiceListener actionServiceListener =
		new IActionServiceListener() {
		public void actionServiceChanged(IActionServiceEvent actionServiceEvent) {
			updateActiveIds();
		}
	};

	Set activeContextIds = new HashSet();
	IWorkbenchPage activeWorkbenchPage;
	IActionService activeWorkbenchPageActionService;
	IWorkbenchPart activeWorkbenchPart;
	IActionService activeWorkbenchPartActionService;
	IWorkbenchWindow activeWorkbenchWindow;

	final ICommandManagerListener commandManagerListener =
		new ICommandManagerListener() {
		public final void commandManagerChanged(final CommandManagerEvent commandManagerEvent) {
			updateActiveContextIds();
		}
	};

	IContextActivationServiceListener contextActivationServiceListener =
		new IContextActivationServiceListener() {
		public void contextActivationServiceChanged(ContextActivationServiceEvent contextActivationServiceEvent) {
			updateActiveIds();
		}
	};

	final IContextManagerListener contextManagerListener =
		new IContextManagerListener() {
		public final void contextManagerChanged(final ContextManagerEvent contextManagerEvent) {
			updateActiveContextIds();
		}
	};

	IInternalPerspectiveListener internalPerspectiveListener =
		new IInternalPerspectiveListener() {
		public void perspectiveActivated(
			IWorkbenchPage workbenchPage,
			IPerspectiveDescriptor perspectiveDescriptor) {
			updateActiveIds();
		}

		public void perspectiveChanged(
			IWorkbenchPage workbenchPage,
			IPerspectiveDescriptor perspectiveDescriptor,
			String changeId) {
			updateActiveIds();
		}

		public void perspectiveClosed(
			IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
			updateActiveIds();
		}

		public void perspectiveOpened(
			IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
			updateActiveIds();
		}
	};

	IPageListener pageListener = new IPageListener() {
		public void pageActivated(IWorkbenchPage workbenchPage) {
			updateActiveIds();
		}

		public void pageClosed(IWorkbenchPage workbenchPage) {
			updateActiveIds();
		}

		public void pageOpened(IWorkbenchPage workbenchPage) {
			updateActiveIds();
		}
	};

	IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart workbenchPart) {
			updateActiveIds();
			updateActiveWorkbenchWindowMenuManager(true);
		}

		public void partBroughtToTop(IWorkbenchPart workbenchPart) {
		}

		public void partClosed(IWorkbenchPart workbenchPart) {
			updateActiveIds();
		}

		public void partDeactivated(IWorkbenchPart workbenchPart) {
			updateActiveIds();
		}

		public void partOpened(IWorkbenchPart workbenchPart) {
			updateActiveIds();
		}
	};

	IWindowListener windowListener = new IWindowListener() {
		public void windowActivated(IWorkbenchWindow workbenchWindow) {
			if (workbenchWindow != null) {
				IWorkbenchWindowContextSupport workbenchWindowContextSupport =
				(IWorkbenchWindowContextSupport) workbenchWindow.getAdapter(
						IWorkbenchWindowContextSupport.class);

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
			 			
			updateActiveIds();
			updateActiveWorkbenchWindowMenuManager(true);
		}

		public void windowClosed(IWorkbenchWindow workbenchWindow) {
			updateActiveIds();
			updateActiveWorkbenchWindowMenuManager(true);
		}

		public void windowDeactivated(IWorkbenchWindow workbenchWindow) {
			updateActiveIds();
			updateActiveWorkbenchWindowMenuManager(true);
		}

		public void windowOpened(IWorkbenchWindow workbenchWindow) {
			updateActiveIds();
			updateActiveWorkbenchWindowMenuManager(true);
		}
	};

	Workbench workbench;

	WorkbenchCommandsAndContexts(Workbench workbench) {
		this.workbench = workbench;
	}

	public IActionService getActionService() {
		if (actionService == null) {
			actionService = new ActionService();
			actionService.addActionServiceListener(actionServiceListener);
		}

		return actionService;
	}

	public void updateActiveContextIds() {
		// TODO: this should be setActiveContextIds()
		workbench.getCommandManager().setActiveActivityIds(activeContextIds);
	}

	void updateActiveIds() {
		IWorkbenchWindow activeWorkbenchWindow =
			workbench.getActiveWorkbenchWindow();

		if (activeWorkbenchWindow != null
			&& !(activeWorkbenchWindow instanceof WorkbenchWindow))
			activeWorkbenchWindow = null;

		IWorkbenchPage activeWorkbenchPage =
			activeWorkbenchWindow != null
				? activeWorkbenchWindow.getActivePage()
				: null;
		IActionService activeWorkbenchPageActionService =
			activeWorkbenchPage != null
				? ((WorkbenchPage) activeWorkbenchPage).getActionService()
				: null;
		IPartService activePartService =
			activeWorkbenchWindow != null
				? activeWorkbenchWindow.getPartService()
				: null;
		IWorkbenchPart activeWorkbenchPart =
			activePartService != null
				? activePartService.getActivePart()
				: null;
		IWorkbenchPartSite activeWorkbenchPartSite =
			activeWorkbenchPart != null ? activeWorkbenchPart.getSite() : null;
		IActionService activeWorkbenchPartActionService =
			activeWorkbenchPartSite != null
				? ((PartSite) activeWorkbenchPartSite).getActionService()
				: null;

		if (activeWorkbenchWindow != this.activeWorkbenchWindow) {
			if (this.activeWorkbenchWindow != null) {
				this.activeWorkbenchWindow.removePageListener(pageListener);
				this.activeWorkbenchWindow.getPartService().removePartListener(
					partListener);
				((WorkbenchWindow) this.activeWorkbenchWindow)
					.getPerspectiveService()
					.removePerspectiveListener(internalPerspectiveListener);
			}

			this.activeWorkbenchWindow = activeWorkbenchWindow;

			if (this.activeWorkbenchWindow != null) {
				this.activeWorkbenchWindow.addPageListener(pageListener);
				this.activeWorkbenchWindow.getPartService().addPartListener(
					partListener);
				((WorkbenchWindow) this.activeWorkbenchWindow)
					.getPerspectiveService()
					.addPerspectiveListener(internalPerspectiveListener);
			}
		}

		if (activeWorkbenchPageActionService
			!= this.activeWorkbenchPageActionService) {
			if (this.activeWorkbenchPageActionService != null)
				this
					.activeWorkbenchPageActionService
					.removeActionServiceListener(
					actionServiceListener);

			this.activeWorkbenchPage = activeWorkbenchPage;
			this.activeWorkbenchPageActionService =
				activeWorkbenchPageActionService;

			if (this.activeWorkbenchPageActionService != null)
				this.activeWorkbenchPageActionService.addActionServiceListener(
					actionServiceListener);
		}

		if (activeWorkbenchPartActionService
			!= this.activeWorkbenchPartActionService) {
			if (this.activeWorkbenchPartActionService != null)
				this
					.activeWorkbenchPartActionService
					.removeActionServiceListener(
					actionServiceListener);

			this.activeWorkbenchPart = activeWorkbenchPart;
			this.activeWorkbenchPartActionService =
				activeWorkbenchPartActionService;

			if (this.activeWorkbenchPartActionService != null)
				this.activeWorkbenchPartActionService.addActionServiceListener(
					actionServiceListener);
		}

		SortedMap actionsById = new TreeMap();
		actionsById.putAll(getActionService().getActionsById());

		if (this.activeWorkbenchWindow != null) {
			actionsById.putAll(
				((WorkbenchWindow) this.activeWorkbenchWindow)
					.getActionsForGlobalActions());
			actionsById.putAll(
				((WorkbenchWindow) this.activeWorkbenchWindow)
					.getActionsForActionSets());
		}

		if (this.activeWorkbenchPageActionService != null)
			actionsById.putAll(
				this.activeWorkbenchPageActionService.getActionsById());

		if (this.activeWorkbenchPartActionService != null)
			actionsById.putAll(
				this.activeWorkbenchPartActionService.getActionsById());

		((CommandManager) workbench.getCommandManager()).setActionsById(
			actionsById);
		
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

		Set activeContextIds = new HashSet();

		if (workbenchWindow != null) {
			IWorkbenchWindowContextSupport workbenchWindowContextSupport =
			(IWorkbenchWindowContextSupport) workbenchWindow.getAdapter(
					IWorkbenchWindowContextSupport.class);

			if (workbenchWindowContextSupport != null) {
				activeContextIds =
				workbenchWindowContextSupport
				.getContextActivationService()
				.getActiveContextIds();
			}
		}

		if (!Util.equals(this.activeContextIds, activeContextIds)) {
			this.activeContextIds = activeContextIds;
			updateActiveContextIds();
			updateActiveWorkbenchWindowMenuManager(false);
		}
	}

	public void updateActiveWorkbenchWindowMenuManager(boolean textOnly) {
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
