/*
 * Created on Apr 18, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.ui.wizards;

import java.net.URL;
import java.util.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardDialog;
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

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UnifiedSitePage extends BannerPage implements ISearchProvider {
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
		public Object[] getChildren(Object parentElement) {
			return new Object [0];
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
			return false;
		}
	}

	class TreeLabelProvider
		extends LabelProvider {
		/**
		* @see ITableLabelProvider#getColumnImage(Object, int)
		*/
		public Image getImage(Object obj) {
			if (obj instanceof SiteCandidate)
				return UpdateUI.getDefault().getLabelProvider().get(
					UpdateUIImages.DESC_SITE_OBJ);
			return null;
		}

		/**
		 * @see ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getText(Object obj) {
			if (obj instanceof SiteCandidate) {
				SiteCandidate csite = (SiteCandidate) obj;
				return csite.getLabel();
			}
			return "";
		}
	}

	class ModelListener implements IUpdateModelChangedListener {
		/* (non-Javadoc)
		* @see org.eclipse.update.internal.ui.model.IUpdateModelChangedListener#objectChanged(java.lang.Object, java.lang.String)
		*/
		public void objectChanged(Object object, String property) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.internal.ui.model.IUpdateModelChangedListener#objectsAdded(java.lang.Object, java.lang.Object[])
		 */
		public void objectsAdded(Object parent, Object[] children) {
			if (parent==null && children[0] instanceof SiteBookmark) {
				treeViewer.refresh();
				checkItems();
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.internal.ui.model.IUpdateModelChangedListener#objectsRemoved(java.lang.Object, java.lang.Object[])
		 */
		public void objectsRemoved(Object parent, Object[] children) {
			if (parent==null && children[0] instanceof SiteBookmark) {
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
	private SearchRunner searchRunner;
	private UnifiedSearchCategory category;
	private UnifiedSearchObject search;
	private ModelListener modelListener;

	/**
	 * @param name
	 */
	public UnifiedSitePage(SearchRunner searchRunner) {
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
		UpdateUI
			.getDefault()
			.getUpdateModel()
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
		treeViewer = new CheckboxTreeViewer(
				parent,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		Tree tree = treeViewer.getTree();
		tree.setLayoutData(gd);
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.setLabelProvider(new TreeLabelProvider());
		treeViewer.setSorter(new ViewerSorter() {});
		treeViewer.setInput(UpdateUI.getDefault().getUpdateModel());
		initializeItems();
		treeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				handleSiteChecked(
					(SiteCandidate) e.getElement(),
					e.getChecked());
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
		TreeItem [] items = treeViewer.getTree().getItems();
		ArrayList checked = new ArrayList();
		for (int i=0; i<items.length; i++) {
			SiteCandidate cand = (SiteCandidate)items[i].getData();
			SiteBookmark bookmark = cand.getBookmark();
			if (bookmark.isSelected())
				checked.add(cand);
		}
		treeViewer.setCheckedElements(checked.toArray());
	}

	private void handleAddSite() {
		NewSiteBookmarkWizardPage page = new NewSiteBookmarkWizardPage(null);
		NewWizard wizard =
			new NewWizard(page, UpdateUIImages.DESC_NEW_BOOKMARK);
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
		updateSearchObject();
	}

	private void handleSelectionChanged(IStructuredSelection ssel) {
		boolean canDelete = false;

		if (ssel.size() > 0) {
			canDelete = true;
			for (Iterator iter = ssel.iterator(); iter.hasNext();) {
				SiteCandidate item = (SiteCandidate) iter.next();
				if (item.isReadOnly())
					canDelete = false;
			}
		}
		removeButton.setEnabled(canDelete);
	}

	private void updateSearchObject() {
		Object[] checked = treeViewer.getCheckedElements();
		SiteBookmark[] bookmarks = new SiteBookmark[checked.length];
		for (int i = 0; i < checked.length; i++) {
			SiteCandidate cand = (SiteCandidate) checked[i];
			bookmarks[i] = cand.getBookmark();
		}
		search.setSelectedBookmarks(bookmarks);
		searchRunner.setNewSearchNeeded(true);
		setPageComplete(checked.length>0);
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
}
