package org.eclipse.team.internal.ui.target;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.text.html.parser.TagElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.IRemoteTargetResource;
import org.eclipse.team.core.target.ISiteListener;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

public class SiteExplorerView extends ViewPart implements ISiteListener {

	public static final String VIEW_ID = "org.eclipse.team.ui.target.SiteExplorerView"; //$NON-NLS-1$

	// The tree viewer
	private TableViewer tableViewer;
	private TreeViewer treeViewer;
	
	// The root
	private SiteRootsElement root;
	
	// The view's actions
	private Action addSiteAction;

	/**
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite p) {
		SashForm sash = new SashForm(p, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		root = new SiteRootsElement(TargetManager.getSites(), RemoteResourceElement.SHOW_FOLDERS);
		
		treeViewer = new TreeViewer(sash, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.setContentProvider(new SiteLazyContentProvider());
		treeViewer.setLabelProvider(new WorkbenchLabelProvider());

		treeViewer.setSorter(new SiteViewSorter());
		treeViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.F5) {
					refresh();
				}
			}
		});
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateFileTable();
			}
		});
		
		treeViewer.setInput(root);

		Table table = new Table(sash, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		TableLayout tlayout = new TableLayout();
		
		TableColumn tableColumn = new TableColumn(table, SWT.NULL);
		tableColumn.setText("Name");
		tableColumn = new TableColumn(table, SWT.NULL);
		tableColumn.setText("Size");
		tableColumn.setAlignment(SWT.RIGHT);
		tableColumn = new TableColumn(table, SWT.NULL);
		tableColumn.setText("Modified");
		tableColumn = new TableColumn(table, SWT.NULL);
		tableColumn.setText("URL");
		ColumnLayoutData cLayout = new ColumnPixelData(21);
		tlayout.addColumnData(cLayout);
		cLayout = new ColumnPixelData(20);
		tlayout.addColumnData(cLayout);
		cLayout = new ColumnWeightData(100, true);
		tlayout.addColumnData(cLayout);
		cLayout = new ColumnPixelData(100);
		tlayout.addColumnData(cLayout);
		table.setLayout(tlayout);
		table.setHeaderVisible(true);

		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new SiteLazyContentProvider());
		tableViewer.setLabelProvider(new SiteExplorerViewLabelProvider());
		
		TargetManager.addSiteListener(this);
		initalizeToolbarActions();
	}

	private IRemoteTargetResource getSelectedRemoteFolder() {
		IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();		
		if (!selection.isEmpty()) {
			final List filesSelection = new ArrayList();
			Iterator it = selection.iterator();
			while(it.hasNext()) {
				Object o = it.next();
				if(o instanceof RemoteResourceElement) {
					return ((RemoteResourceElement)o).getRemoteResource();
				} else if(o instanceof SiteElement) {
					try {
						return ((SiteElement)o).getSite().getRemoteResource();
					} catch (TeamException e) {
						return null;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Method updateFileTable.
	 */
	private void updateFileTable() {
		final IRemoteTargetResource remoteFolder = getSelectedRemoteFolder();
		final Set tags = new HashSet();
		if(remoteFolder != null) {
			tableViewer.setInput(new RemoteResourceElement(remoteFolder, RemoteResourceElement.SHOW_FILES));
		}
	}

	private void initalizeToolbarActions() {
		final Shell shell = tableViewer.getTable().getShell();
		// Create actions
		
		// Refresh (toolbar)
		addSiteAction = new Action(Policy.bind("SiteExplorerViewaddSiteAction"), TeamImages.getImageDescriptor(ISharedImages.IMG_SITE_ELEMENT)) { //$NON-NLS-1$
			public void run() {
				ConfigureTargetWizard wizard = new ConfigureTargetWizard();
				wizard.init(null, null);
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.open();
			}
		};
		addSiteAction.setToolTipText(Policy.bind("SiteExplorerViewaddSiteActionTooltip")); //$NON-NLS-1$

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
		root = new SiteRootsElement(TargetManager.getSites(), RemoteResourceElement.SHOW_FOLDERS);
		treeViewer.setInput(root);
		treeViewer.refresh();
		tableViewer.refresh();		
	}
}