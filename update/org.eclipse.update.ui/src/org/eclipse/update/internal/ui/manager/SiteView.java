package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.security.AuthorizationDatabase;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Image;

import java.net.Authenticator;
import java.util.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class SiteView
	extends BaseTreeView
	implements IUpdateModelChangedListener {
	private static final String KEY_NEW_SITE = "SiteView.Popup.newSite";
	private static final String KEY_NEW_LOCAL_SITE = "SiteView.Popup.newLocalSite";
	private static final String KEY_DELETE = "SiteView.Popup.delete";
	private static final String KEY_REFRESH = "SiteView.Popup.refresh";
	private static final String KEY_FILTER_FILES = "SiteView.menu.showFiles";
	private static final String KEY_SHOW_CATEGORIES =
		"SiteView.menu.showCategories";
	private Action propertiesAction;
	private Action newAction;
	private Action newLocalAction;
	private Action deleteAction;
	private Action fileFilterAction;
	private Action showCategoriesAction;
	private Image siteImage;
	private Image featureImage;
	private Image computerImage;
	private Action refreshAction;
	private SelectionChangedListener selectionListener;
	private Hashtable fileImages = new Hashtable();
	private FileFilter fileFilter = new FileFilter();

	class FileFilter extends ViewerFilter {
		public boolean select(Viewer viewer, Object parent, Object child) {
			if (child instanceof MyComputerFile) {
				return false;
			}
			return true;
		}
	}

	class SelectionChangedListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			updateForSelection((IStructuredSelection) event.getSelection());
		}
	}

	class SiteProvider
		extends DefaultContentProvider
		implements ITreeContentProvider {

		public Object[] getChildren(Object parent) {
			if (parent instanceof UpdateModel) {
				return getBookmarks((UpdateModel) parent);
			}
			if (parent instanceof SiteBookmark) {
				return getSiteCatalog((SiteBookmark) parent);
			}
			if (parent instanceof MyComputer) {
				return ((MyComputer) parent).getChildren(parent);
			}
			if (parent instanceof MyComputerDirectory) {
				return ((MyComputerDirectory) parent).getChildren(parent);
			}
			if (parent instanceof SiteCategory) {
				final SiteCategory category = (SiteCategory) parent;
				BusyIndicator.showWhile(viewer.getTree().getDisplay(), new Runnable() {
					public void run() {
						try {
							category.touchFeatures();
						} catch (CoreException e) {
							UpdateUIPlugin.logException(e);
						}
					}
				});
				return category.getChildren();
			}
			return new Object[0];
		}

		public Object getParent(Object child) {
			return null;
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof SiteBookmark)
				return true;
			if (parent instanceof MyComputer) {
				return true;
			}
			if (parent instanceof MyComputerDirectory) {
				return ((MyComputerDirectory) parent).hasChildren(parent);
			}
			if (parent instanceof SiteCategory) {
				return ((SiteCategory) parent).getChildCount() > 0;
			}
			return false;
		}

		public Object[] getElements(Object obj) {
			return getChildren(obj);
		}
	}

	class SiteLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof IFeature) {
				IFeature feature = (IFeature) obj;
				return feature.getLabel();
			}
			if (obj instanceof FeatureReferenceAdapter) {
				try {
					IFeature feature = ((FeatureReferenceAdapter) obj).getFeature();
					VersionedIdentifier versionedIdentifier=(feature!=null)?feature.getVersionedIdentifier():null;
					String version = "";
					if (versionedIdentifier!=null) version = versionedIdentifier.getVersion().toString();
					String label = (feature!=null)?feature.getLabel():"";
					return label + " " + version;
				} catch (CoreException e) {
					UpdateUIPlugin.logException(e);
				}
			}
			return super.getText(obj);
		}
		public Image getImage(Object obj) {
			if (obj instanceof SiteBookmark) {
				return siteImage;
			}
			if (obj instanceof MyComputer) {
				return computerImage;
			}
			if (obj instanceof MyComputerDirectory) {
				return ((MyComputerDirectory) obj).getImage(obj);
			}
			if (obj instanceof MyComputerFile) {
				ImageDescriptor desc = ((MyComputerFile) obj).getImageDescriptor(obj);
				Image image = (Image) fileImages.get(desc);
				if (image == null) {
					image = desc.createImage();
					fileImages.put(desc, image);
				}
				return image;
			}
			if (obj instanceof SiteCategory) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_FOLDER);
			}
			if (obj instanceof IFeature || obj instanceof FeatureReferenceAdapter) {
				return featureImage;
			}
			return super.getImage(obj);
		}
	}

	/**
	 * The constructor.
	 */
	public SiteView() {
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		model.addUpdateModelChangedListener(this);
		siteImage = UpdateUIPluginImages.DESC_SITE_OBJ.createImage();
		featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
		computerImage = UpdateUIPluginImages.DESC_COMPUTER_OBJ.createImage();
		selectionListener = new SelectionChangedListener();
	}

	public void dispose() {
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		model.removeUpdateModelChangedListener(this);
		siteImage.dispose();
		featureImage.dispose();
		computerImage.dispose();
		for (Enumeration enum = fileImages.elements(); enum.hasMoreElements();) {
			((Image) enum.nextElement()).dispose();
		}
		super.dispose();
	}

	public void initProviders() {
		viewer.setContentProvider(new SiteProvider());
		viewer.setLabelProvider(new SiteLabelProvider());
		viewer.setInput(UpdateUIPlugin.getDefault().getUpdateModel());
	}

	public void makeActions() {
		super.makeActions();
		propertiesAction =
			new PropertyDialogAction(UpdateUIPlugin.getActiveWorkbenchShell(), viewer);
		newAction = new Action() {
			public void run() {
				performNew();
			}
		};
		newAction.setText(UpdateUIPlugin.getResourceString(KEY_NEW_SITE));

		newLocalAction = new Action() {
			public void run() {
				performNewLocal();
			}
		};
		newLocalAction.setText(UpdateUIPlugin.getResourceString(KEY_NEW_LOCAL_SITE));

		deleteAction = new Action() {
			public void run() {
				performDelete();
			}
		};
		deleteAction.setText(UpdateUIPlugin.getResourceString(KEY_DELETE));

		refreshAction = new Action() {
			public void run() {
				performRefresh();
			}
		};
		refreshAction.setText(UpdateUIPlugin.getResourceString(KEY_REFRESH));

		fileFilterAction = new Action() {
			public void run() {
				if (fileFilterAction.isChecked()) {
					viewer.removeFilter(fileFilter);
				} else
					viewer.addFilter(fileFilter);
			}
		};
		fileFilterAction.setText(UpdateUIPlugin.getResourceString(KEY_FILTER_FILES));
		fileFilterAction.setChecked(false);

		viewer.addFilter(fileFilter);

		showCategoriesAction = new Action() {
			public void run() {
				showCategories(!showCategoriesAction.isChecked());
			}
		};
		showCategoriesAction.setText(
			UpdateUIPlugin.getResourceString(KEY_SHOW_CATEGORIES));
		showCategoriesAction.setChecked(true);

		viewer.addSelectionChangedListener(selectionListener);
	}

	private void updateForSelection(IStructuredSelection selection) {
		refreshAction.setEnabled(selection.size() == 1);
	}

	public void fillActionBars(IActionBars bars) {
		IMenuManager menuManager = bars.getMenuManager();
		menuManager.add(fileFilterAction);
		menuManager.add(new Separator());
		menuManager.add(showCategoriesAction);
	}

	public void fillContextMenu(IMenuManager manager) {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		Object obj = sel.getFirstElement();

		manager.add(refreshAction);
		manager.add(new Separator());
		manager.add(newAction);
		if (obj instanceof SiteBookmark) {
			SiteBookmark site = (SiteBookmark)obj;
			if (site.getType()==SiteBookmark.LOCAL)
				manager.add(newLocalAction);
			else
				manager.add(deleteAction);
		}
		manager.add(new Separator());
		super.fillContextMenu(manager);
		if (obj instanceof SiteBookmark)
			manager.add(propertiesAction);
	}

	private void performNew() {
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		Shell shell = UpdateUIPlugin.getActiveWorkbenchShell();
		NewSiteDialog dialog = new NewSiteDialog(shell);
		dialog.create();
		dialog.getShell().setText(
			UpdateUIPlugin.getResourceString(NewSiteDialog.KEY_TITLE));
		dialog.getShell().setSize(400, 150);
		if (dialog.open() == NewSiteDialog.OK) {
			SiteBookmark site = dialog.getNewSite();
			model.addBookmark(site);
		}
	}

	private void performNewLocal() {
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			Object obj = ssel.getFirstElement();
			if (obj instanceof SiteBookmark) {
				SiteBookmark bookmark = (SiteBookmark) obj;
				if (bookmark.getType()==SiteBookmark.LOCAL) {
					UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
					Shell shell = UpdateUIPlugin.getActiveWorkbenchShell();
					NewSiteDialog dialog = new NewSiteDialog(shell, bookmark);
					dialog.create();
					dialog.getShell().setText(
							UpdateUIPlugin.getResourceString(NewSiteDialog.KEY_TITLE));
					dialog.getShell().setSize(400, 150);
					if (dialog.open() == NewSiteDialog.OK) {
						SiteBookmark site = dialog.getNewSite();
						model.addBookmark(site);
					}
				}
			}
		}
	}

	private void performDelete() {
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			for (Iterator iter = ssel.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (obj instanceof SiteBookmark) {
					model.removeBookmark((SiteBookmark) obj);
				}
			}
		}
	}

	private void performRefresh() {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		final Object obj = sel.getFirstElement();

		if (obj != null) {
			BusyIndicator.showWhile(viewer.getTree().getDisplay(), new Runnable() {
				public void run() {
					try {
						// reinitialize the authenticator 
						AuthorizationDatabase auth = UpdateUIPlugin.getDefault().getDatabase();
						if (auth!=null) auth.reset();							
						if (obj instanceof SiteBookmark)
							 ((SiteBookmark) obj).connect();
						viewer.refresh(obj);
					} catch (CoreException e) {
						UpdateUIPlugin.logException(e);
					}
				}
			});
		}
	}

	private Object[] getBookmarks(UpdateModel model) {
		SiteBookmark[] bookmarks = model.getBookmarks();
		Object[] array = new Object[1 + bookmarks.length];
		array[0] = new MyComputer();
		for (int i = 1; i < array.length; i++) {
			array[i] = bookmarks[i - 1];
		}
		return array;
	}

	class CatalogBag {
		Object[] catalog;
	}

	private Object[] getSiteCatalog(final SiteBookmark bookmark) {
		if (!bookmark.isSiteConnected()) {
			final CatalogBag bag = new CatalogBag();
			BusyIndicator.showWhile(viewer.getTree().getDisplay(), new Runnable() {
				public void run() {
					try {
						bookmark.connect();
						bag.catalog = bookmark.getCatalog(showCategoriesAction.isChecked());
					} catch (CoreException e) {
						// FIXME
					}
				}
			});
			if (bag.catalog != null)
				return bag.catalog;
		}
		if (bookmark.getSite() != null) {
			return bookmark.getCatalog(showCategoriesAction.isChecked());
		}
		return new Object[0];
	}

	private void showCategories(boolean show) {
		Object[] expanded = viewer.getExpandedElements();
		for (int i = 0; i < expanded.length; i++) {
			if (expanded[i] instanceof SiteBookmark) {
				viewer.refresh(expanded[i]);
			}
		}
	}

	public void objectAdded(Object parent, Object child) {
		if (child instanceof SiteBookmark) {
			UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
			viewer.add(model, child);
			viewer.setSelection(new StructuredSelection(child));
		}
	}

	public void objectRemoved(Object parent, Object child) {
		if (child instanceof SiteBookmark) {
			viewer.remove(child);
		}
	}

	public void objectChanged(Object object, String property) {
		if (object instanceof SiteBookmark && property.equals(SiteBookmark.P_NAME))
			viewer.update(object, null);
			viewer.setSelection(viewer.getSelection());
	}
	
	public void selectSiteBookmark(SiteBookmark bookmark) {
		viewer.setSelection(new StructuredSelection(bookmark), true);
	}

}