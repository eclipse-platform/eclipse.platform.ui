package org.eclipse.team.internal.ui.target;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.target.ISiteListener;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.internal.ui.ConfigureProjectWizard;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

public class SiteExplorerView extends ViewPart implements ISiteListener {

	public static final String VIEW_ID = "org.eclipse.team.ui.target.SiteExplorerView"; //$NON-NLS-1$

	// The tree viewer
	private TreeViewer viewer;
	
	// The root
	private SiteRootsElement root;
	
	// The view's actions
	private Action addSiteAction;

	/**
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		root = new SiteRootsElement();
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new SiteLazyContentProvider());
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		getSite().setSelectionProvider(viewer);
		viewer.setInput(root);
		viewer.setSorter(new SiteViewSorter());
		viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.F5) {
					refresh();
				}
			}
		});
		TargetManager.addSiteListener(this);
		initalizeToolbarActions();
	}

	private void initalizeToolbarActions() {
		final Shell shell = viewer.getTree().getShell();
		// Create actions
		
		// Refresh (toolbar)
		addSiteAction = new Action("Add a Site", TeamImages.getImageDescriptor(ISharedImages.IMG_SITE_ELEMENT)) {
			public void run() {
				ConfigureProjectWizard wizard = new ConfigureTargetWizard();
				wizard.init(null, null);
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.open();
			}
		};
		addSiteAction.setToolTipText("Add a Site");

		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager tbm = bars.getToolBarManager();
		tbm.add(addSiteAction);
		tbm.update(false);
	}
	
	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}
	
	/**
	 * @see ISiteListener#siteAdded(Site)
	 */
	public void siteAdded(Site site) {
		refresh();
	}

	/**
	 * @see ISiteListener#siteRemoved(Site)
	 */
	public void siteRemoved(Site site) {
		refresh();
	}
	
	protected void refresh() {
		viewer.refresh();
	}
}