package org.eclipse.ui.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

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
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.commands.CommandManagerEvent;
import org.eclipse.ui.commands.IActionService;
import org.eclipse.ui.commands.IActionServiceEvent;
import org.eclipse.ui.commands.IActionServiceListener;
import org.eclipse.ui.commands.ICommandManagerListener;
import org.eclipse.ui.contexts.IContextActivationService;
import org.eclipse.ui.contexts.IContextActivationServiceEvent;
import org.eclipse.ui.contexts.IContextActivationServiceListener;

import org.eclipse.ui.internal.commands.ActionService;
import org.eclipse.ui.internal.commands.CommandManager;
import org.eclipse.ui.internal.contexts.ContextActivationService;
import org.eclipse.ui.internal.util.Util;

/**
 * TODO javadoc
 * 
 * @since 3.0
 */
public class WorkbenchActivitiesCommandsAndRoles {

	IActionService actionService;

	IActionServiceListener actionServiceListener = new IActionServiceListener() {
		public void actionServiceChanged(IActionServiceEvent actionServiceEvent) {
			updateActiveCommandIdsAndActiveActivityIds();
		}
	};

	Set activeActivityIds = new HashSet();
	//IActionService activeWorkbenchWindowActionService;
	//IContextActivationService activeWorkbenchWindowContextActivationService;

	IWorkbenchPage activeWorkbenchPage;
	IActionService activeWorkbenchPageActionService;
	IContextActivationService activeWorkbenchPageContextActivationService;

	IWorkbenchPart activeWorkbenchPart;
	IActionService activeWorkbenchPartActionService;
	IContextActivationService activeWorkbenchPartContextActivationService;

	IWorkbenchWindow activeWorkbenchWindow;

	final IActivityManagerListener activityManagerListener = new IActivityManagerListener() {
		public final void activityManagerChanged(final ActivityManagerEvent activityManagerEvent) {
			updateActiveActivityIds();
		}
	};

	final ICommandManagerListener commandManagerListener = new ICommandManagerListener() {
		public final void commandManagerChanged(final CommandManagerEvent commandManagerEvent) {
			updateActiveActivityIds();
		}
	};
	IContextActivationService contextActivationService;

	IContextActivationServiceListener contextActivationServiceListener =
		new IContextActivationServiceListener() {
		public void contextActivationServiceChanged(IContextActivationServiceEvent contextActivationServiceEvent) {
			updateActiveCommandIdsAndActiveActivityIds();
		}
	};

	IInternalPerspectiveListener internalPerspectiveListener = new IInternalPerspectiveListener() {
		public void perspectiveActivated(
			IWorkbenchPage workbenchPage,
			IPerspectiveDescriptor perspectiveDescriptor) {
			updateActiveCommandIdsAndActiveActivityIds();
		}

		public void perspectiveChanged(
			IWorkbenchPage workbenchPage,
			IPerspectiveDescriptor perspectiveDescriptor,
			String changeId) {
			updateActiveCommandIdsAndActiveActivityIds();
		}

		public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			updateActiveCommandIdsAndActiveActivityIds();
		}

		public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			updateActiveCommandIdsAndActiveActivityIds();
		}
	};

	IPageListener pageListener = new IPageListener() {
		public void pageActivated(IWorkbenchPage workbenchPage) {
			updateActiveCommandIdsAndActiveActivityIds();
		}

		public void pageClosed(IWorkbenchPage workbenchPage) {
			updateActiveCommandIdsAndActiveActivityIds();
		}

		public void pageOpened(IWorkbenchPage workbenchPage) {
			updateActiveCommandIdsAndActiveActivityIds();
		}
	};

	IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart workbenchPart) {
			updateActiveCommandIdsAndActiveActivityIds();
			updateActiveWorkbenchWindowMenuManager();
		}

		public void partBroughtToTop(IWorkbenchPart workbenchPart) {
		}

		public void partClosed(IWorkbenchPart workbenchPart) {
			updateActiveCommandIdsAndActiveActivityIds();
		}

		public void partDeactivated(IWorkbenchPart workbenchPart) {
			updateActiveCommandIdsAndActiveActivityIds();
		}

		public void partOpened(IWorkbenchPart workbenchPart) {
			updateActiveCommandIdsAndActiveActivityIds();
		}
	};

	IWindowListener windowListener = new IWindowListener() {
		public void windowActivated(IWorkbenchWindow workbenchWindow) {
			updateActiveCommandIdsAndActiveActivityIds();
			updateActiveWorkbenchWindowMenuManager();
		}

		public void windowClosed(IWorkbenchWindow workbenchWindow) {
			updateActiveCommandIdsAndActiveActivityIds();
			updateActiveWorkbenchWindowMenuManager();
		}

		public void windowDeactivated(IWorkbenchWindow workbenchWindow) {
			updateActiveCommandIdsAndActiveActivityIds();
			updateActiveWorkbenchWindowMenuManager();
		}

		public void windowOpened(IWorkbenchWindow workbenchWindow) {
			updateActiveCommandIdsAndActiveActivityIds();
			updateActiveWorkbenchWindowMenuManager();
		}
	};

	Workbench workbench;

	WorkbenchActivitiesCommandsAndRoles(Workbench workbench) {
		this.workbench = workbench;
	}

	public IActionService getActionService() {
		if (actionService == null) {
			actionService = new ActionService();
			actionService.addActionServiceListener(actionServiceListener);
		}

		return actionService;
	}

	public IContextActivationService getContextActivationService() {
		if (contextActivationService == null) {
			contextActivationService = new ContextActivationService();
			contextActivationService.addContextActivationServiceListener(
				contextActivationServiceListener);
		}

		return contextActivationService;
	}

	public void updateActiveActivityIds() {
		workbench.getCommandManager().setActiveActivityIds(activeActivityIds);
	}

	void updateActiveCommandIdsAndActiveActivityIds() {
		IWorkbenchWindow currentWorkbenchWindow = workbench.getActiveWorkbenchWindow();

		if (currentWorkbenchWindow != null && !(currentWorkbenchWindow instanceof WorkbenchWindow))
			currentWorkbenchWindow = null;

		//IActionService activeWorkbenchWindowActionService =
		// activeWorkbenchWindow != null ? ((WorkbenchWindow)
		// activeWorkbenchWindow).getActionService() : null;
		//IContextActivationService
		// activeWorkbenchWindowContextActivationService =
		// activeWorkbenchWindow != null ? ((WorkbenchWindow)
		// activeWorkbenchWindow).getContextActivationService() : null;

		IWorkbenchPage currentWorkbenchPage =
			currentWorkbenchWindow != null ? currentWorkbenchWindow.getActivePage() : null;

		IActionService currentWorkbenchPageActionService =
			currentWorkbenchPage != null
				? ((WorkbenchPage) currentWorkbenchPage).getActionService()
				: null;

		IContextActivationService currentWorkbenchPageContextActivationService =
			currentWorkbenchPage != null
				? ((WorkbenchPage) currentWorkbenchPage).getContextActivationService()
				: null;

		IPartService activePartService =
			currentWorkbenchWindow != null ? currentWorkbenchWindow.getPartService() : null;
		IWorkbenchPart currentWorkbenchPart =
			activePartService != null ? activePartService.getActivePart() : null;
		IWorkbenchPartSite activeWorkbenchPartSite =
			currentWorkbenchPart != null ? currentWorkbenchPart.getSite() : null;

		IActionService currentWorkbenchPartActionService =
			activeWorkbenchPartSite != null
				? ((PartSite) activeWorkbenchPartSite).getActionService()
				: null;
		IContextActivationService currentWorkbenchPartContextActivationService =
			activeWorkbenchPartSite != null
				? ((PartSite) activeWorkbenchPartSite).getContextActivationService()
				: null;

		if (currentWorkbenchWindow != this.activeWorkbenchWindow) {
			if (this.activeWorkbenchWindow != null) {
				this.activeWorkbenchWindow.removePageListener(pageListener);
				this.activeWorkbenchWindow.getPartService().removePartListener(partListener);
				((WorkbenchWindow) this.activeWorkbenchWindow)
					.getPerspectiveService()
					.removePerspectiveListener(
					internalPerspectiveListener);
			}

			this.activeWorkbenchWindow = currentWorkbenchWindow;

			if (this.activeWorkbenchWindow != null) {
				this.activeWorkbenchWindow.addPageListener(pageListener);
				this.activeWorkbenchWindow.getPartService().addPartListener(partListener);
				((WorkbenchWindow) this.activeWorkbenchWindow)
					.getPerspectiveService()
					.addPerspectiveListener(
					internalPerspectiveListener);
			}
		}

		/*
		 * if (activeWorkbenchWindowActionService !=
		 * this.activeWorkbenchWindowActionService) { if
		 * (this.activeWorkbenchWindowActionService != null)
		 * this.activeWorkbenchWindowActionService.removeActionServiceListener(actionServiceListener);
		 * this.activeWorkbenchWindow = activeWorkbenchWindow;
		 * this.activeWorkbenchWindowActionService =
		 * activeWorkbenchWindowActionService; if
		 * (this.activeWorkbenchWindowActionService != null)
		 * this.activeWorkbenchWindowActionService.addActionServiceListener(actionServiceListener); }
		 */

		if (currentWorkbenchPageActionService != this.activeWorkbenchPageActionService) {
			if (this.activeWorkbenchPageActionService != null)
				this.activeWorkbenchPageActionService.removeActionServiceListener(
					actionServiceListener);

			this.activeWorkbenchPage = currentWorkbenchPage;
			this.activeWorkbenchPageActionService = currentWorkbenchPageActionService;

			if (this.activeWorkbenchPageActionService != null)
				this.activeWorkbenchPageActionService.addActionServiceListener(
					actionServiceListener);
		}

		if (currentWorkbenchPartActionService != this.activeWorkbenchPartActionService) {
			if (this.activeWorkbenchPartActionService != null)
				this.activeWorkbenchPartActionService.removeActionServiceListener(
					actionServiceListener);

			this.activeWorkbenchPart = currentWorkbenchPart;
			this.activeWorkbenchPartActionService = currentWorkbenchPartActionService;

			if (this.activeWorkbenchPartActionService != null)
				this.activeWorkbenchPartActionService.addActionServiceListener(
					actionServiceListener);
		}

		SortedMap actionsById = new TreeMap();
		actionsById.putAll(getActionService().getActionsById());

		//if (this.activeWorkbenchWindowActionService != null)
		//	actionsById.putAll(this.activeWorkbenchWindowActionService.getActionsById());

		if (this.activeWorkbenchWindow != null) {
			actionsById.putAll(
				((WorkbenchWindow) this.activeWorkbenchWindow).getActionsForGlobalActions());
			actionsById.putAll(
				((WorkbenchWindow) this.activeWorkbenchWindow).getActionsForActionSets());
		}

		if (this.activeWorkbenchPageActionService != null)
			actionsById.putAll(this.activeWorkbenchPageActionService.getActionsById());

		if (this.activeWorkbenchPartActionService != null)
			actionsById.putAll(this.activeWorkbenchPartActionService.getActionsById());

		((CommandManager) workbench.getCommandManager()).setActionsById(actionsById);

		/*
		 * if (activeWorkbenchWindowContextActivationService !=
		 * this.activeWorkbenchWindowContextActivationService) { if
		 * (this.activeWorkbenchWindowContextActivationService != null)
		 * this.activeWorkbenchWindowContextActivationService.removeContextActivationServiceListener(contextActivationServiceListener);
		 * this.activeWorkbenchWindow = activeWorkbenchWindow;
		 * this.activeWorkbenchWindowContextActivationService =
		 * activeWorkbenchWindowContextActivationService; if
		 * (this.activeWorkbenchWindowContextActivationService != null)
		 * this.activeWorkbenchWindowContextActivationService.addContextActivationServiceListener(contextActivationServiceListener); }
		 */

		if (currentWorkbenchPageContextActivationService
			!= this.activeWorkbenchPageContextActivationService) {
			if (this.activeWorkbenchPageContextActivationService != null)
				this
					.activeWorkbenchPageContextActivationService
					.removeContextActivationServiceListener(
					contextActivationServiceListener);

			this.activeWorkbenchPage = currentWorkbenchPage;
			this.activeWorkbenchPageContextActivationService =
				currentWorkbenchPageContextActivationService;

			if (this.activeWorkbenchPageContextActivationService != null)
				this
					.activeWorkbenchPageContextActivationService
					.addContextActivationServiceListener(
					contextActivationServiceListener);
		}

		if (currentWorkbenchPartContextActivationService
			!= this.activeWorkbenchPartContextActivationService) {
			if (this.activeWorkbenchPartContextActivationService != null)
				this
					.activeWorkbenchPartContextActivationService
					.removeContextActivationServiceListener(
					contextActivationServiceListener);

			this.activeWorkbenchPart = currentWorkbenchPart;
			this.activeWorkbenchPartContextActivationService =
				currentWorkbenchPartContextActivationService;

			if (this.activeWorkbenchPartContextActivationService != null)
				this
					.activeWorkbenchPartContextActivationService
					.addContextActivationServiceListener(
					contextActivationServiceListener);
		}

		SortedSet activeContextIds = new TreeSet();
		activeContextIds.addAll(getContextActivationService().getActiveContextIds());

		//if (this.activeWorkbenchWindowContextActivationService != null)
		//	activeContextIds.addAll(this.activeWorkbenchWindowContextActivationService.getActiveContextIds());

		if (this.activeWorkbenchPageContextActivationService != null)
			activeContextIds.addAll(
				this.activeWorkbenchPageContextActivationService.getActiveContextIds());

		if (this.activeWorkbenchPartContextActivationService != null)
			activeContextIds.addAll(
				this.activeWorkbenchPartContextActivationService.getActiveContextIds());

		Set currentActivityIds = new HashSet(activeContextIds);

		if (!Util.equals(this.activeActivityIds, currentActivityIds)) {
			this.activeActivityIds = currentActivityIds;

			updateActiveActivityIds();

			IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

			if (workbenchWindow instanceof WorkbenchWindow) {
				MenuManager menuManager = ((WorkbenchWindow) workbenchWindow).getMenuManager();
				menuManager.updateAll(true);
			}
		}
	}

	public void updateActiveWorkbenchWindowMenuManager() {
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

		if (workbenchWindow instanceof WorkbenchWindow) {
			MenuManager menuManager = ((WorkbenchWindow) workbenchWindow).getMenuManager();
			menuManager.update(IAction.TEXT);
		}
	}
}
