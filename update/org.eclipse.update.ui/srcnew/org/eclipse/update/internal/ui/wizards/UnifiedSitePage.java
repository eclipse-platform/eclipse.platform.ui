/*
 * Created on Apr 18, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.search.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UnifiedSitePage extends BannerPage2 implements ISearchProvider2 {
	static class SiteCandidate {
		private SiteBookmark bookmark;
		private boolean readOnly;

		public SiteCandidate(SiteBookmark bookmark, boolean readOnly) {
			this.bookmark = bookmark;
			this.readOnly = readOnly;
		}
		public SiteCandidate(SiteBookmark bookmark) {
			this(bookmark, false);
		}
		public URL getURL() {
			return bookmark.getURL();
		}
		public SiteBookmark getBookmark() {
			return bookmark;
		}
		public String getLabel() {
			return bookmark.getLabel();
		}
		public boolean isSelected() {
			return bookmark.isSelected();
		}
		public boolean isReadOnly() {
			return readOnly;
		}
		public void setSelected(boolean selected) {
			bookmark.setSelected(selected);
		}
		public String toString() {
			return getLabel();
		}
	}

	class TreeContentProvider
		extends DefaultContentProvider
		implements ITreeContentProvider {
		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object parent) {
			return computeElements();
		}

		private Object[] computeElements() {
			UpdateModel model = UpdateUI.getDefault().getUpdateModel();
			Object[] bookmarks = model.getBookmarkLeafs();
			Object[] sitesToVisit =
				discoveryFolder.getChildren(discoveryFolder);
			ArrayList candidates = new ArrayList();
			createCandidates(sitesToVisit, candidates, true);
			createCandidates(bookmarks, candidates, false);
			return candidates.toArray();
		}

		private void createCandidates(
			Object[] bookmarks,
			ArrayList list,
			boolean readOnly) {
			for (int i = 0; i < bookmarks.length; i++) {
				SiteBookmark bookmark = (SiteBookmark) bookmarks[i];
				list.add(new SiteCandidate(bookmark, readOnly));
			}
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(final Object parent) {
			if (parent instanceof SiteCandidate) {
				final Object [] children = getSiteCatalog((SiteCandidate)parent);
				treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						handleSiteExpanded((SiteCandidate)parent, children);
					}
				});
				return children;
			}
			/*
			if (parent instanceof SiteCategory) {
				final SiteCategory category = (SiteCategory) parent;
				category.touchFeatures(getContainer());
				return category.getChildren();
			}
			*/
			return new Object[0];
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
			if (element instanceof SiteCandidate) {
				return true;
			}
			/*
			if (element instanceof SiteCategory) {
				return ((SiteCategory)element).getChildCount()>0;
			}
			*/
			return false;
		}
	}

	class TreeLabelProvider extends LabelProvider {
		/**
		* @see ITableLabelProvider#getColumnImage(Object, int)
		*/
		public Image getImage(Object obj) {
			if (obj instanceof SiteCandidate)
				return UpdateUI.getDefault().getLabelProvider().get(
					UpdateUIImages.DESC_SITE_OBJ);
			if (obj instanceof SiteCategory)
				return UpdateUI.getDefault().getLabelProvider().get(
					UpdateUIImages.DESC_CATEGORY_OBJ);
			return super.getImage(obj);
		}

		/**
		 * @see ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getText(Object obj) {
			if (obj instanceof SiteCandidate) {
				SiteCandidate csite = (SiteCandidate) obj;
				return csite.getLabel();
			}
			return super.getText(obj);
		}
	}

	class ModelListener implements org.eclipse.update.internal.operations.IUpdateModelChangedListener {
		/* (non-Javadoc)
		* @see org.eclipse.update.internal.ui.model.IUpdateModelChangedListener#objectChanged(java.lang.Object, java.lang.String)
		*/
		public void objectChanged(Object object, String property) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.internal.ui.model.IUpdateModelChangedListener#objectsAdded(java.lang.Object, java.lang.Object[])
		 */
		public void objectsAdded(Object parent, Object[] children) {
			if (parent == null && children[0] instanceof SiteBookmark) {
				treeViewer.refresh();
				checkItems();
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.internal.ui.model.IUpdateModelChangedListener#objectsRemoved(java.lang.Object, java.lang.Object[])
		 */
		public void objectsRemoved(Object parent, Object[] children) {
			if (parent == null && children[0] instanceof SiteBookmark) {
				treeViewer.refresh();
				checkItems();
			}
		}
	}

	private DiscoveryFolder discoveryFolder;
	private CheckboxTreeViewer treeViewer;
	private Button addSiteButton;
	private Button addLocalButton;
	private Button removeButton;
	private SearchRunner2 searchRunner;
	private UnifiedSearchCategory category;
	private UnifiedSearchObject search;
	private ModelListener modelListener;

	/**
	 * @param name
	 */
	public UnifiedSitePage(SearchRunner2 searchRunner) {
		super("SitePage");
		setTitle("Update sites to visit");
		setDescription("Select update sites to visit while looking for new features.");
		UpdateUI.getDefault().getLabelProvider().connect(this);
		discoveryFolder = new DiscoveryFolder();
		this.searchRunner = searchRunner;
		category = new UnifiedSearchCategory();
		search = new UnifiedSearchObject();
		search.setModel(UpdateUI.getDefault().getUpdateModel());
		modelListener = new ModelListener();
		UpdateManager.getOperationsManager().addUpdateModelChangedListener(
			modelListener);
	}

	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		UpdateManager.getOperationsManager()
			.removeUpdateModelChangedListener(
			modelListener);
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.wizards.BannerPage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		client.setLayout(layout);
		Label label = new Label(client, SWT.NULL);
		label.setText("&Sites to include in search:");
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		createTreeViewer(client);
		Composite buttonContainer = new Composite(client, SWT.NULL);
		gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		layout = new GridLayout();
		buttonContainer.setLayout(layout);
		layout.marginWidth = 0;
		layout.marginHeight = 0;

		addSiteButton = new Button(buttonContainer, SWT.PUSH);
		addSiteButton.setText("Add &Update Site...");
		gd =
			new GridData(
				GridData.VERTICAL_ALIGN_BEGINNING
					| GridData.HORIZONTAL_ALIGN_FILL);
		addSiteButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(addSiteButton);
		addSiteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddSite();
			}
		});

		addLocalButton = new Button(buttonContainer, SWT.PUSH);
		addLocalButton.setText("Add &Local Site...");
		gd =
			new GridData(
				GridData.VERTICAL_ALIGN_BEGINNING
					| GridData.HORIZONTAL_ALIGN_FILL);
		addLocalButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(addLocalButton);
		addLocalButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddLocal();
			}
		});

		removeButton = new Button(buttonContainer, SWT.PUSH);
		removeButton.setText("&Remove");
		removeButton.setEnabled(false);
		gd =
			new GridData(
				GridData.VERTICAL_ALIGN_BEGINNING
					| GridData.HORIZONTAL_ALIGN_FILL);
		removeButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(removeButton);
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRemove();
			}
		});

		return client;
	}

	private void createTreeViewer(Composite parent) {
		treeViewer =
			new CheckboxTreeViewer(
				parent,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		Tree tree = treeViewer.getTree();
		tree.setLayoutData(gd);
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.setSorter(new ViewerSorter() {
		});
		treeViewer.setInput(UpdateUI.getDefault().getUpdateModel());
		initializeItems();
		treeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				Object element = e.getElement();
				if (element instanceof SiteCandidate)
					handleSiteChecked((SiteCandidate) element, e.getChecked());
				else if (element instanceof SiteCategory) {
					handleCategoryChecked((SiteCategory)element, e.getChecked());
				}
			}
		});
		treeViewer
			.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				handleSelectionChanged((IStructuredSelection) e.getSelection());
			}
		});
	}

	private void initializeItems() {
		checkItems();
		updateSearchObject();
	}

	private void checkItems() {
		TreeItem[] items = treeViewer.getTree().getItems();
		ArrayList checked = new ArrayList();
		for (int i = 0; i < items.length; i++) {
			SiteCandidate cand = (SiteCandidate) items[i].getData();
			SiteBookmark bookmark = cand.getBookmark();
			if (bookmark.isSelected())
				checked.add(cand);
		}
		treeViewer.setCheckedElements(checked.toArray());
	}

	private void handleAddSite() {
		NewSiteBookmarkWizardPage2 page = new NewSiteBookmarkWizardPage2(null);
		NewWizard2 wizard =
			new NewWizard2(page, UpdateUIImages.DESC_NEW_BOOKMARK);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setText("New Site");
		dialog.open();
	}

	private void handleAddLocal() {
	}

	private void handleRemove() {
		BusyIndicator
			.showWhile(treeViewer.getControl().getDisplay(), new Runnable() {
			public void run() {
				UpdateModel updateModel =
					UpdateUI.getDefault().getUpdateModel();
				IStructuredSelection ssel =
					(IStructuredSelection) treeViewer.getSelection();
				for (Iterator iter = ssel.iterator(); iter.hasNext();) {
					SiteCandidate item = (SiteCandidate) iter.next();
					if (item.isReadOnly())
						continue;
					updateModel.removeBookmark(item.getBookmark());
				}
			}
		});
	}

	private void handleSiteChecked(SiteCandidate cand, boolean checked) {
		cand.setSelected(checked);
		updateCandidateGrayState(cand, checked);
		updateSearchObject();
	}
	
	private void updateCandidateGrayState(SiteCandidate cand, boolean checked) {
		boolean grayed = false;
		SiteBookmark bookmark = cand.getBookmark();
		if (checked && bookmark.getIgnoredCategories()!=null)
			grayed = true;
		if (treeViewer.getGrayed(cand)!=grayed)
			treeViewer.setGrayed(cand, grayed);
	}
	
	private void handleSiteExpanded(SiteCandidate cand, Object [] cats) {
		SiteBookmark bookmark = cand.getBookmark();
		String [] ignored = bookmark.getIgnoredCategories();
		HashSet imap = null;
		
		if (ignored!=null) {
			imap = new HashSet();
			for (int i=0; i<ignored.length; i++) {
				imap.add(ignored[i]);
			}
		}
		
		for (int i=0; i<cats.length; i++) {
			if (cats[i] instanceof SiteCategory) {
				SiteCategory category = (SiteCategory)cats[i];
				boolean checked=true;
				if (imap!=null) {
					String cname = category.getFullName();
					checked = !imap.contains(cname);
				}
				treeViewer.setChecked(category, checked);
			}
		}
		updateCandidateGrayState(cand, treeViewer.getChecked(cand));
	}

	private void handleCategoryChecked(
		SiteCategory category,
		boolean checked) {
		SiteBookmark bookmark = category.getBookmark();
		String[] ignored = bookmark.getIgnoredCategories();
		ArrayList array = new ArrayList();
		if (ignored != null) {
			for (int i = 0; i < ignored.length; i++) {
				array.add(ignored[i]);
			}
		}
		if (checked) {
			if (array.contains(category.getFullName()))
				array.remove(category.getFullName());
		}
		else
			if (!array.contains(category.getFullName()))
				array.add(category.getFullName());
		
		String [] newIgnored = null;
		if (array.size()>0) 
			newIgnored = (String[])array.toArray(new String[array.size()]);

		bookmark.setIgnoredCategories(newIgnored);
		searchRunner.setNewSearchNeeded(true);
		SiteCandidate cand = getCandidate(bookmark);
		if (cand!=null)
			updateCandidateGrayState(cand, treeViewer.getChecked(cand));
	}
	
	private SiteCandidate getCandidate(SiteBookmark bookmark) {
		TreeItem [] items = treeViewer.getTree().getItems();
		// FIXME: it is lame to go to the tree widget to
		// get the root elements.
		for (int i=0; i<items.length; i++) {
			TreeItem item = items[i];
			SiteCandidate cand = (SiteCandidate)item.getData();
			SiteBookmark cbookmark = cand.getBookmark();
			if (cbookmark==bookmark)
				return cand;
		}
		return null;
	}

	private void handleSelectionChanged(IStructuredSelection ssel) {
		boolean canDelete = false;

		if (ssel.size() > 0) {
			canDelete = true;
			for (Iterator iter = ssel.iterator(); iter.hasNext();) {
				Object item = iter.next();
				if (item instanceof SiteCandidate) {
					SiteCandidate csite = (SiteCandidate) item;
					if (csite.isReadOnly()) {
						canDelete = false;
						break;
					}
				}
				if (item instanceof SiteCategory) {
					canDelete = false;
					break;
				}
			}
		}
		removeButton.setEnabled(canDelete);
	}

	private void updateSearchObject() {
		Object[] checked = treeViewer.getCheckedElements();
		ArrayList barray = new ArrayList();
		
		for (int i = 0; i < checked.length; i++) {
			if (checked[i] instanceof SiteCandidate) {
				SiteCandidate cand = (SiteCandidate) checked[i];
				barray.add(cand.getBookmark());
			}
		}
		SiteBookmark [] bookmarks = (SiteBookmark[])barray.toArray(new SiteBookmark[barray.size()]);
		search.setSelectedBookmarks(bookmarks);
		searchRunner.setNewSearchNeeded(true);
		setPageComplete(bookmarks.length > 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.wizards.ISearchProvider#getCategory()
	 */
	public ISearchCategory getCategory() {
		return category;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.wizards.ISearchProvider#getSearch()
	 */
	public SearchObject getSearch() {
		return search;
	}

	public void setVisible(boolean value) {
		super.setVisible(value);
		if (value)
			searchRunner.setSearchProvider(this);
	}
	private Object[] getSiteCatalog(SiteCandidate candidate) {
		final SiteBookmark bookmark = candidate.getBookmark();
		if (bookmark.isWebBookmark())
			return new Object[0];
		Object[] result =
			getSiteCatalogWithIndicator(bookmark, !bookmark.isSiteConnected());
		if (result != null)
			return result;
		else
			return new Object[0];
	}

	class CatalogBag {
		Object[] catalog;
	}

	private Object[] getSiteCatalogWithIndicator(
		final SiteBookmark bookmark,
		final boolean connect) {
		final CatalogBag bag = new CatalogBag();

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
				throws InvocationTargetException {
				try {
					monitor.beginTask(
						UpdateUI.getString("UpdatesView.connecting"),
						3);
					monitor.worked(1);

					if (connect)
						bookmark.connect(new SubProgressMonitor(monitor, 1));
					else
						monitor.worked(1);
					bag.catalog =
						bookmark.getCatalog(
							true,
							new SubProgressMonitor(monitor, 1));
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException e) {
			UpdateUI.logException(e);
		} catch (InterruptedException e) {
		}
		return bag.catalog;
	}
}
