package org.eclipse.update.internal.ui;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * @author Wassim Melhem
 */
public class WebBookmarksDialog extends Dialog {
	
	class ModelListener implements IUpdateModelChangedListener {
		public void objectChanged(Object object, String property) {
			viewer.refresh();
		}

		public void objectsAdded(Object parent, Object[] children) {
			viewer.refresh();
		}

		public void objectsRemoved(Object parent, Object[] children) {
			viewer.refresh();
		}
	}
	
	class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			UpdateModel model = UpdateUI.getDefault().getUpdateModel();
			Object[] bookmarks = model.getBookmarkLeafs();
			Object[] sitesToVisit = discoveryFolder.getChildren(discoveryFolder);
			Object[] all = new Object[bookmarks.length + sitesToVisit.length];
			System.arraycopy(bookmarks, 0, all, 0, bookmarks.length);
			System.arraycopy(sitesToVisit, 0, all, bookmarks.length, sitesToVisit.length);
			return all;
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}
	}
	
	class WebSiteLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			UpdateLabelProvider provider = UpdateUI.getDefault().getLabelProvider();
			if (element instanceof BookmarkFolder) 
				return provider.get(UpdateUIImages.DESC_BFOLDER_OBJ,0);
			if (element instanceof SiteBookmark)
				return provider.get(UpdateUIImages.DESC_WEB_SITE_OBJ,0);
			return null;
		}
		public String getText(Object element) {
			return super.getText(element);
		}
		
	}
	
	private Button addButton;
	private Button editButton;
	private Button removeButton;
	private ModelListener modelListener;
	private TableViewer viewer;
	private DiscoveryFolder discoveryFolder = new DiscoveryFolder();

	public WebBookmarksDialog(Shell parentShell) {
		super(parentShell);
		UpdateUI.getDefault().getLabelProvider().connect(this);
		modelListener = new ModelListener();
		UpdateUI.getDefault().getUpdateModel().addUpdateModelChangedListener(
			modelListener);
	}

	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 10;
		container.setLayout(layout);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(container, SWT.NONE);
		label.setText(UpdateUI.getString("WebBookmarksDialog.label")); //$NON-NLS-1$
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		
		Table table = new Table(container, SWT.BORDER | SWT.V_SCROLL);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		viewer = new TableViewer(table);		
		viewer.setContentProvider(new ContentProvider());
		viewer.setLabelProvider(new WebSiteLabelProvider());
		viewer.setInput(UpdateUI.getDefault().getUpdateModel());
		
		viewer.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof SiteBookmark)
					return ((SiteBookmark)element).isWebBookmark();
				return true;
			}
		});
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				IStructuredSelection ssel =
					(IStructuredSelection) e.getSelection();
				if (ssel.size() == 0) 
					return;				
				SiteBookmark bookmark = (SiteBookmark)ssel.getFirstElement();
				Object parent = bookmark.getParent(bookmark);
				editButton.setEnabled(parent == null || !parent.equals(discoveryFolder));
				removeButton.setEnabled(parent == null || !parent.equals(discoveryFolder));
			}
		});

		createButtons(container);
		Dialog.applyDialogFont(container);
		return container;
	}
	
	private void createButtons(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		

		addButton = new Button(container, SWT.PUSH);
		addButton.setText(UpdateUI.getString("WebBookmarksDialog.add")); //$NON-NLS-1$
		addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				NewWebSiteDialog dialog = new NewWebSiteDialog(getShell());
				dialog.create();
				dialog.getShell().setText(UpdateUI.getString("WebBookmarksDialog.new")); //$NON-NLS-1$
				dialog.open();
			}
		});
				
		editButton = new Button(container, SWT.PUSH);
		editButton.setText(UpdateUI.getString("WebBookmarksDialog.edit")); //$NON-NLS-1$
		editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection ssel =
					(IStructuredSelection) viewer.getSelection();
				SiteBookmark bookmark = (SiteBookmark) ssel.getFirstElement();		
				EditSiteDialog dialog = new EditSiteDialog(getShell(), bookmark);
				dialog.create();
				dialog.getShell().setText(UpdateUI.getString("WebBookmarksDialog.editTitle")); //$NON-NLS-1$
				dialog.open();
			}
		});
		editButton.setEnabled(false);
		
		removeButton = new Button(container, SWT.PUSH);
		removeButton.setText(UpdateUI.getString("WebBookmarksDialog.remove")); //$NON-NLS-1$
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection ssel =
					(IStructuredSelection) viewer.getSelection();
				SiteBookmark bookmark = (SiteBookmark) ssel.getFirstElement();
				UpdateUI.getDefault().getUpdateModel().removeBookmark(bookmark);
			}
		});
		removeButton.setEnabled(false);
		
		
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(
			parent,
			IDialogConstants.CANCEL_ID,
			IDialogConstants.CANCEL_LABEL,
			false);
	}
	
	protected void okPressed() {
		super.okPressed();
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		UpdateUI.getDefault().getUpdateModel().removeUpdateModelChangedListener(
			modelListener);
	}

	protected void cancelPressed() {
		UpdateUI.getDefault().getUpdateModel().reset();
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		UpdateUI.getDefault().getUpdateModel().removeUpdateModelChangedListener(
			modelListener);
		super.cancelPressed();
	}
	
	

}
