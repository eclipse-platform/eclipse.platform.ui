package org.eclipse.team.internal.ui.sync;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.UIConstants;
import org.eclipse.team.ui.TeamUIPlugin;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * This class provides a view for performing synchronizations
 * between the local workspace and a repository.
 */
public class SyncView extends ViewPart {
	public static final String VIEW_ID = "org.eclipse.team.ui.sync.SyncView";
	private SyncCompareInput input;
	private TreeViewer viewer;
	private Composite top;
	
	// The possible sync modes
	public static final int SYNC_NONE = 0;
	public static final int SYNC_CATCHUP = 1;
	public static final int SYNC_RELEASE = 2;
	public static final int SYNC_BOTH = 3;
	public static final int SYNC_MERGE = 4;
	
	// Titles cached for efficiency
	private final String CATCHUP_TITLE = Policy.bind("SyncView.catchupModeTitle");
	private final String RELEASE_TITLE = Policy.bind("SyncView.releaseModeTitle");
	private final String FREE_TITLE = Policy.bind("SyncView.freeModeTitle");
	
	private int currentSyncMode = SYNC_NONE;
	
	/**
	 * Action for toggling the sync mode.
	 */
	class SyncModeAction extends Action {
		// The sync mode that this action enables
		private int syncMode;
		public SyncModeAction(String title, ImageDescriptor image, int mode) {
			super(title, image);
			this.syncMode = mode;
		}
		public void run() {
			SyncView.this.setSyncMode(syncMode);
		}
	}
	
	private SyncModeAction catchupMode;
	private SyncModeAction releaseMode;
	private SyncModeAction freeMode;
	
	/**
	 * Creates a new view.
	 */
	public SyncView() {
		super();
	}

	/*
	 * @see IWorkbenchPart#createPartControl
	 */
	public void createPartControl(Composite parent) {
		top = new Composite(parent, SWT.NONE);
		
		//XXX Set the control data to be this part, so the compare 
		//frames that will eventually live in this widget hierarchy
		//have some way to access the action bars for hooking global
		//actions.  See corresponding XXX comment in CompareEditor#findActionBars
		top.setData(this);
		
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		top.setLayout(layout);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		showDefaultContents();
		
		initializeSyncModes();
	}
	
	/**
	 * Makes the history view visible in the active perspective. If there
	 * isn't a history view registered <code>null</code> is returned.
	 * Otherwise the opened view part is returned.
	 */
	public static SyncView findInActivePerspective() {
		try {
			IViewPart part = TeamUIPlugin.getActivePage().findView(VIEW_ID);
			if (part == null) {
				part = TeamUIPlugin.getActivePage().showView(VIEW_ID);
			}
			return (SyncView)part;
		} catch (PartInitException pe) {
			return null;
		}
	}
	
	/**
	 * Sets up the sync modes and the actions for switching between them.
	 */
	private void initializeSyncModes() {
		// Create the actions
		catchupMode = new SyncModeAction(
			Policy.bind("SyncView.catchupModeAction"),
			TeamUIPlugin.getPlugin().getImageDescriptor(UIConstants.IMG_SYNC_MODE_CATCHUP),
			SYNC_CATCHUP);
		catchupMode.setToolTipText(Policy.bind("SyncView.catchupModeToolTip"));
		catchupMode.setChecked(false);
		
		releaseMode = new SyncModeAction(
			Policy.bind("SyncView.releaseModeAction"),
			TeamUIPlugin.getPlugin().getImageDescriptor(UIConstants.IMG_SYNC_MODE_RELEASE),
			SYNC_RELEASE);
		releaseMode.setToolTipText(Policy.bind("SyncView.releaseModeToolTip"));
		releaseMode.setChecked(false);
		
		freeMode = new SyncModeAction(
			Policy.bind("SyncView.freeModeAction"),
			TeamUIPlugin.getPlugin().getImageDescriptor(UIConstants.IMG_SYNC_MODE_FREE),
			SYNC_BOTH);
		freeMode.setToolTipText(Policy.bind("SyncView.freeModeToolTip"));
		freeMode.setChecked(false);
	}
	
	/**
	 * Runs an operation and handles progress and exceptions.  Returns true
	 * if the operation was successful, and false if there were errors or
	 * the user canceled.
	 */
	private boolean run(IRunnableWithProgress op) {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getSite().getShell());
		try {
			dialog.run(true, true, op);
			return true;
		} catch (InvocationTargetException e) {
			Throwable throwable = e.getTargetException();
			IStatus error = null;
			if (throwable instanceof CoreException) {
				error = ((CoreException)throwable).getStatus();
			} else {
				error = new Status(IStatus.ERROR, TeamUIPlugin.ID, 1, Policy.bind("simpleInternal"), throwable);
			}
			ErrorDialog.openError(getSite().getShell(), Policy.bind("SyncView.unableSynchronize"), null, error);
			TeamUIPlugin.log(error);
		} catch (InterruptedException e) {
		}
		return false;
	}
	
	/**
	 * Asks the part to take focus within the workbench.
	 */
	public void setFocus() {
		if (top != null && !top.isDisposed()) {
			top.setFocus();
		}
	}

	/**
	 * Activates the given sync mode.
	 */
	void setSyncMode(int mode) {
		// Implement radio button behaviour
		switch (mode) {
			case SYNC_CATCHUP:
				catchupMode.setChecked(true);
				releaseMode.setChecked(false);
				freeMode.setChecked(false);
				setTitle(CATCHUP_TITLE);
				break;
			case SYNC_RELEASE:
				releaseMode.setChecked(true);
				catchupMode.setChecked(false);
				freeMode.setChecked(false);
				setTitle(RELEASE_TITLE);
				break;
			case SYNC_BOTH:
				freeMode.setChecked(true);
				releaseMode.setChecked(false);
				catchupMode.setChecked(false);
				setTitle(FREE_TITLE);
				break;
		}
		// Only update actions if there is valid input
		if (input != null && input.getDiffRoot() != null && mode != currentSyncMode) {
			currentSyncMode = mode;
			input.getViewer().syncModeChanged(mode);
			updateActions();
		}
	}
	
	/**
	 * Shows default contents for the view if there is nothing to synchronize.
	 */
	private void showDefaultContents() {
		Label label = new Label(top, SWT.WRAP);
		label.setLayoutData(new GridData(GridData.FILL_BOTH));
		label.setText(Policy.bind("SyncView.text"));
	}
	
	/**
	 * Shows synchronization information for the given resources in the sync view.
	 */
	public void showSync(IRemoteSyncElement[] trees) {
		input = new SyncCompareInput(getViewSite(), trees);
		currentSyncMode = SYNC_NONE;
		
		// Run the diff and stop if cancel or error occurred.
		if (!run(input)) return;
		
		// Check for problem message
		if (input.getMessage() != null) {
			MessageDialog.openInformation(getSite().getShell(), Policy.bind("SyncView.unableSynchronize"), input.getMessage());
			return;
		}
		
		// Remove old viewer
		Control[] oldChildren = top.getChildren();
		if (oldChildren != null) {
			for (int i = 0; i < oldChildren.length; i++) {
				oldChildren[i].dispose();
			}
		}
		// Remove actions from toolbar
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().removeAll();
		bars.getToolBarManager().update(false);
		bars.getMenuManager().removeAll();
		bars.getMenuManager().update();
		bars.updateActionBars();
		
		// Check for empty comparison
		if (input.getDiffRoot() == null) {
			MessageDialog.openInformation(getSite().getShell(), Policy.bind("nothingToSynchronize"), Policy.bind("SyncView.same"));
			showDefaultContents();
			top.layout();
			return;
		}
	
		// Show the result
		Control control = input.createContents(top);
		control.setLayoutData(new GridData(GridData.FILL_BOTH));
		/*TreeViewer viewer = input.getViewer();
		if (viewer != null) {
			Control viewerControl = viewer.getControl();
			if (viewerControl != null && !viewerControl.isDisposed()) {
				WorkbenchHelp.setHelp(viewerControl, new ViewContextComputer(this, ITeamHelpContextIds.SYNC_VIEW));
			}
		}*/
		
		top.layout();
		
		// Set the sync mode depending on user preference
		// To do: add the user preference later, just say no for now.
	//	if (TeamUIPlugin.getPlugin().getPreferenceStore().getBoolean(UIConstants.PREF_ALWAYS_IN_CATCHUP_RELEASE)) {
	//		freeMode.run();
	//	} else {
			if (input.hasIncomingChanges()) {
				catchupMode.run();
			} else {
				releaseMode.run();
			}
	//	}
		// Reveal if fast view
		try {
			TeamUIPlugin.getActivePage().showView(VIEW_ID);
		} catch (PartInitException e) {
			TeamUIPlugin.log(e.getStatus());
		}
	}
	
	/**
	 * Updates the actions for this view's action bar.
	 */
	private void updateActions() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager toolBar = bars.getToolBarManager();
		IMenuManager menu = bars.getMenuManager();
		toolBar.removeAll();
		menu.removeAll();
		
		toolBar.add(catchupMode);
		toolBar.add(releaseMode);
		toolBar.add(freeMode);
		input.getViewer().contributeToActionBars(bars);
		
		toolBar.update(false);
		menu.update(false);
		bars.updateActionBars();
	}
}