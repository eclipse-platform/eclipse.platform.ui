/*
 * Created on Apr 18, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.ui.wizards;

import java.net.URL;
import java.util.ArrayList;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.model.SiteBookmark;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.search.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SitePage extends BannerPage implements ISearchProvider {
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
		public void setSelected(boolean selected) {
			bookmark.setSelected(selected);
		}
	}

	class TableContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {
		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object parent) {
			return computeElements();
		}
		
		private Object[] computeElements() {
			UpdateModel model = UpdateUI.getDefault().getUpdateModel();
			Object [] bookmarks = model.getBookmarkLeafs();
			Object [] sitesToVisit = discoveryFolder.getChildren(discoveryFolder);
			ArrayList candidates = new ArrayList();
			createCandidates(sitesToVisit, candidates, false);
			createCandidates(bookmarks, candidates, true);
			return candidates.toArray();
		}

		private void createCandidates(Object[] bookmarks, ArrayList list, boolean readOnly) {
			for (int i=0; i<bookmarks.length; i++) {
				SiteBookmark bookmark = (SiteBookmark)bookmarks[i];
				list.add(new SiteCandidate(bookmark, readOnly));
			}
		}
	}

	class TableLabelProvider
		extends LabelProvider
		implements ITableLabelProvider {
		/**
		* @see ITableLabelProvider#getColumnImage(Object, int)
		*/
		public Image getColumnImage(Object obj, int col) {
			if (obj instanceof SiteCandidate)
				return UpdateUI
					.getDefault()
					.getLabelProvider().get(UpdateUIImages.DESC_SITE_OBJ);
			return null;
		}

		/**
		 * @see ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getColumnText(Object obj, int col) {
			if (obj instanceof SiteCandidate && col == 0) {
				SiteCandidate csite = (SiteCandidate) obj;
				return csite.getLabel();
			}
			return null;
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
			tableViewer.refresh();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.internal.ui.model.IUpdateModelChangedListener#objectsRemoved(java.lang.Object, java.lang.Object[])
		 */
		public void objectsRemoved(Object parent, Object[] children) {
			tableViewer.refresh();
		}
	}
	
	
	private DiscoveryFolder discoveryFolder;
	private CheckboxTableViewer tableViewer;
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
	public SitePage(SearchRunner searchRunner) {
		super("SitePage");
		UpdateUI.getDefault().getLabelProvider().connect(this);
		discoveryFolder = new DiscoveryFolder();
		this.searchRunner = searchRunner;
		category = new UnifiedSearchCategory();
		search = new UnifiedSearchObject();
		search.setModel(UpdateUI.getDefault().getUpdateModel());
		modelListener = new ModelListener();
		UpdateUI.getDefault().getUpdateModel().addUpdateModelChangedListener(modelListener);
	}
	
	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		UpdateUI.getDefault().getUpdateModel().removeUpdateModelChangedListener(modelListener);
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
		label.setText("&Sites to search:");
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		createTableViewer(client);
		Composite buttonContainer = new Composite(client, SWT.NULL);
		gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		layout = new GridLayout();
		buttonContainer.setLayout(layout);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
	
		addSiteButton = new Button(buttonContainer, SWT.PUSH);
		addSiteButton.setText("Add &Update Site...");
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		addSiteButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(addSiteButton);
		addSiteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddSite();
			}
		});
		
		addLocalButton = new Button(buttonContainer, SWT.PUSH);
		addLocalButton.setText("Add &Local Site...");
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		addLocalButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(addLocalButton);
		addLocalButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddLocal();
			}
		});
		
		removeButton = new Button(buttonContainer, SWT.PUSH);
		removeButton.setText("&Remove");
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		removeButton.setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(removeButton);
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRemove();
			}
		});

		return client;
	}

	private void createTableViewer(Composite parent) {
		tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		Table table = tableViewer.getTable();
		table.setLayoutData(gd);
		tableViewer.setContentProvider(new TableContentProvider());
		tableViewer.setLabelProvider(new TableLabelProvider());
		tableViewer.setInput(UpdateUI.getDefault().getUpdateModel());
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				handleSiteChecked((SiteCandidate)e.getElement(), e.getChecked());
			}
		});
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
	}

	private void handleSiteChecked(SiteCandidate cand, boolean checked) {
		cand.setSelected(checked);
		updateSearchObject();
	}
	
	private void updateSearchObject() {
		Object [] checked = tableViewer.getCheckedElements();
		SiteBookmark [] bookmarks = new SiteBookmark[checked.length];
		for (int i=0; i<checked.length; i++) {
			SiteCandidate cand = (SiteCandidate)checked[i];
			bookmarks[i] = cand.getBookmark();
		}
		search.setSelectedBookmarks(bookmarks);
		searchRunner.setNewSearchNeeded(true);
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
