package org.eclipse.update.internal.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.search.*;

public class UnifiedSitePage extends BannerPage2 implements ISearchProvider2 {

	class TreeContentProvider
		extends DefaultContentProvider
		implements ITreeContentProvider {

		public Object[] getElements(Object parent) {
			UpdateModel model = UpdateUI.getDefault().getUpdateModel();
			Object[] bookmarks = model.getBookmarkLeafs();
			Object[] sitesToVisit = discoveryFolder.getChildren(discoveryFolder);
			Object[] all = new Object[bookmarks.length + sitesToVisit.length];
			System.arraycopy(bookmarks, 0, all, 0, bookmarks.length);
			System.arraycopy(sitesToVisit, 0, all, bookmarks.length, sitesToVisit.length);
			return all;
		}

		public Object[] getChildren(final Object parent) {
			if (parent instanceof SiteBookmark) {
				final SiteBookmark bookmark = (SiteBookmark) parent;
				final Object[] children =
					getSiteCatalogWithIndicator(bookmark, !bookmark.isSiteConnected());
				treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (children.length > 0)
							handleSiteExpanded(bookmark, children);
					}
				});
				return children;
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return (element instanceof SiteBookmark);
		}

	}

	class TreeLabelProvider extends LabelProvider {

		public Image getImage(Object obj) {
			if (obj instanceof SiteBookmark)
				return UpdateUI.getDefault().getLabelProvider().get(
					UpdateUIImages.DESC_SITE_OBJ);
			if (obj instanceof SiteCategory)
				return UpdateUI.getDefault().getLabelProvider().get(
					UpdateUIImages.DESC_CATEGORY_OBJ);
			return super.getImage(obj);
		}

		public String getText(Object obj) {
			if (obj instanceof SiteBookmark) {
				return ((SiteBookmark) obj).getLabel();
			}
			return super.getText(obj);
		}
	}

	class ModelListener implements IUpdateModelChangedListener {
		public void objectChanged(Object object, String property) {
		}

		public void objectsAdded(Object parent, Object[] children) {
			treeViewer.refresh();
			checkItems();
		}

		public void objectsRemoved(Object parent, Object[] children) {
			treeViewer.refresh();
			checkItems();
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
		UpdateUI.getDefault().getUpdateModel().addUpdateModelChangedListener(
			modelListener);
	}

	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		UpdateUI.getDefault().getUpdateModel().removeUpdateModelChangedListener(
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
		buttonContainer.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttonContainer.setLayout(layout);

		addSiteButton = new Button(buttonContainer, SWT.PUSH);
		addSiteButton.setText("Add &Update Site...");
		addSiteButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		SWTUtil.setButtonDimensionHint(addSiteButton);
		addSiteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddSite();
			}
		});

		addLocalButton = new Button(buttonContainer, SWT.PUSH);
		addLocalButton.setText("Add &Local Site...");
		addLocalButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		SWTUtil.setButtonDimensionHint(addLocalButton);
		addLocalButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddLocal();
			}
		});

		removeButton = new Button(buttonContainer, SWT.PUSH);
		removeButton.setText("&Remove");
		removeButton.setEnabled(false);
		removeButton.setLayoutData(
			new GridData(GridData.HORIZONTAL_ALIGN_FILL));
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
			new CheckboxTreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.setInput(UpdateUI.getDefault().getUpdateModel());

		initializeItems();

		treeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				Object element = e.getElement();
				if (element instanceof SiteBookmark)
					handleSiteChecked((SiteBookmark) element, e.getChecked());
				else if (element instanceof SiteCategory) {
					handleCategoryChecked((SiteCategory) element, e.getChecked());
				}
			}
		});

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
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
		for (int i = 0; i < items.length; i++) {
			SiteBookmark bookmark = (SiteBookmark) items[i].getData();
			treeViewer.setChecked(bookmark, bookmark.isSelected());
		}
	}

	private void handleAddSite() {
		NewSiteDialog dialog = new NewSiteDialog(UpdateUI.getActiveWorkbenchShell());
		dialog.create();
		dialog.getShell().setText("New Update Site");
		dialog.open();
	}

	private void handleAddLocal() {
	}

	private void handleRemove() {
		BusyIndicator.showWhile(treeViewer.getControl().getDisplay(), new Runnable() {
			public void run() {
				UpdateModel updateModel = UpdateUI.getDefault().getUpdateModel();
				IStructuredSelection ssel =
					(IStructuredSelection) treeViewer.getSelection();
				for (Iterator iter = ssel.iterator(); iter.hasNext();) {
					SiteBookmark bookmark = (SiteBookmark) iter.next();
					if (!bookmark.isReadOnly()) {
						updateModel.removeBookmark(bookmark);
					}
				}
			}
		});
	}

	private void handleSiteChecked(SiteBookmark bookmark, boolean checked) {
		bookmark.setSelected(checked);
		updateBookmarkGrayState(bookmark, checked);
		updateSearchObject();
	}

	private void updateBookmarkGrayState(SiteBookmark bookmark, boolean checked) {
		boolean grayed = checked && bookmark.getIgnoredCategories() != null;
		if (treeViewer.getGrayed(bookmark) != grayed)
			treeViewer.setGrayed(bookmark, grayed);
	}

	private void handleSiteExpanded(SiteBookmark bookmark, Object[] cats) {
		String[] ignored = bookmark.getIgnoredCategories();
		HashSet imap = null;

		if (ignored != null) {
			imap = new HashSet();
			for (int i = 0; i < ignored.length; i++) {
				imap.add(ignored[i]);
			}
		}

		for (int i = 0; i < cats.length; i++) {
			if (cats[i] instanceof SiteCategory) {
				SiteCategory category = (SiteCategory) cats[i];
				boolean checked = true;
				if (imap != null) {
					String cname = category.getFullName();
					checked = !imap.contains(cname);
				}
				treeViewer.setChecked(category, checked);
			}
		}
		updateBookmarkGrayState(bookmark, treeViewer.getChecked(bookmark));
	}

	private void handleCategoryChecked(SiteCategory category, boolean checked) {
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
		} else if (!array.contains(category.getFullName()))
			array.add(category.getFullName());


		bookmark.setIgnoredCategories(
			array.size() == 0
				? null
				: (String[]) array.toArray(new String[array.size()]));
		searchRunner.setNewSearchNeeded(true);
		updateBookmarkGrayState(bookmark, treeViewer.getChecked(bookmark));
	}

	private void handleSelectionChanged(IStructuredSelection ssel) {
		boolean canDelete = false;

		if (ssel.size() > 0) {
			canDelete = true;
			for (Iterator iter = ssel.iterator(); iter.hasNext();) {
				Object item = iter.next();
				if (item instanceof SiteBookmark) {
					if (((SiteBookmark) item).isReadOnly()) {
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
			if (checked[i] instanceof SiteBookmark) {
				barray.add((SiteBookmark) checked[i]);
			}
		}
		SiteBookmark[] bookmarks =
			(SiteBookmark[]) barray.toArray(new SiteBookmark[barray.size()]);
		search.setSelectedBookmarks(bookmarks);
		searchRunner.setNewSearchNeeded(true);
		setPageComplete(bookmarks.length > 0);
	}

	public ISearchCategory getCategory() {
		return category;
	}

	public SearchObject getSearch() {
		return search;
	}

	public void setVisible(boolean value) {
		super.setVisible(value);
		if (value)
			searchRunner.setSearchProvider(this);
	}

	class CatalogBag {
		Object[] catalog;
	}

	private Object[] getSiteCatalogWithIndicator(
		final SiteBookmark bookmark,
		final boolean connect) {
		final CatalogBag bag = new CatalogBag();

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					monitor.beginTask(UpdateUI.getString("UpdatesView.connecting"), 3);
					monitor.worked(1);

					if (connect)
						bookmark.connect(new SubProgressMonitor(monitor, 1));
					else
						monitor.worked(1);
					bag.catalog =
						bookmark.getCatalog(true, new SubProgressMonitor(monitor, 1));
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

		return (bag.catalog == null) ? new Object[0] : bag.catalog;
	}
}
