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
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.core.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Image;
import java.util.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.dialogs.*;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class SiteView extends BaseTreeView implements IUpdateModelChangedListener {
	private Action propertiesAction;
	private Action newAction;
	private Action deleteAction;
	private Image siteImage;
	private Image featureImage;
	private Action refreshAction;
	private SelectionChangedListener selectionListener;
	
class SelectionChangedListener implements ISelectionChangedListener {
	public void selectionChanged(SelectionChangedEvent event) {
		updateForSelection((IStructuredSelection)event.getSelection());
	}
}
	
class SiteProvider extends DefaultContentProvider
					implements ITreeContentProvider {

	public Object[] getChildren(Object parent) {
		if (parent instanceof UpdateModel) {
			UpdateModel model = (UpdateModel)parent;
			return model.getBookmarks();
		}
		if (parent instanceof SiteBookmark) {
			return getSiteCatalog((SiteBookmark)parent);
		}
		if (parent instanceof SiteCategory) {
			final SiteCategory category = (SiteCategory)parent;
			BusyIndicator.showWhile(viewer.getTree().getDisplay(),
									new Runnable() {
				public void run() {
					try {
						category.touchFeatures();
					}
					catch (CoreException e) {
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
		if (parent instanceof SiteCategory) {
			return ((SiteCategory)parent).getChildCount()>0;
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
			IFeature feature = (IFeature)obj;
			return feature.getLabel();
		}
		if (obj instanceof CategorizedFeature) {
			try {
				IFeature feature = ((CategorizedFeature)obj).getFeature();
				String version = feature.getIdentifier().getVersion().toString();
				return feature.getLabel()+" "+version;
			}
			catch (CoreException e) {
				UpdateUIPlugin.logException(e);
			}
		}
		return super.getText(obj);
	}
	public Image getImage(Object obj) {
		if (obj instanceof SiteBookmark) {
			return siteImage;
		}
		if (obj instanceof SiteCategory) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		}
		if (obj instanceof IFeature || obj instanceof CategorizedFeature) {
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
	selectionListener = new SelectionChangedListener();
}

public void dispose() {
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	model.removeUpdateModelChangedListener(this);	
	siteImage.dispose();
	featureImage.dispose();
	super.dispose();
}

public void initProviders() {
	viewer.setContentProvider(new SiteProvider());
	viewer.setLabelProvider(new SiteLabelProvider());
	viewer.setInput(UpdateUIPlugin.getDefault().getUpdateModel());
}

public void makeActions() {
	propertiesAction = new PropertyDialogAction(UpdateUIPlugin.getActiveWorkbenchShell(), viewer);
	newAction = new Action() {
		public void run() {
			performNew();
		}
	};
	newAction.setText("New Site...");
	deleteAction = new Action() {
		public void run() {
			performDelete();
		}
	};
	deleteAction.setText("Delete");
	
	refreshAction = new Action() {
		public void run() {
			performRefresh();
		}
	};
	refreshAction.setText("Refresh");
	viewer.addSelectionChangedListener(selectionListener);
}

private void updateForSelection(IStructuredSelection selection) {
	refreshAction.setEnabled(selection.size()==1);
}

public void fillActionBars() {
	IActionBars bars = getViewSite().getActionBars();
}

public void fillContextMenu(IMenuManager manager) {
	IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
	Object obj = sel.getFirstElement();
	manager.add(newAction);
	if (obj instanceof SiteBookmark) {
		manager.add(refreshAction);
		manager.add(deleteAction);
	}
	manager.add(new Separator());
	manager.add(propertiesAction);
}

private void performNew() {
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	Shell shell = UpdateUIPlugin.getActiveWorkbenchShell();
	NewSiteDialog dialog = new NewSiteDialog(shell);
	dialog.create();
	dialog.getShell().setSize(300, 150);
	if (dialog.open()==NewSiteDialog.OK) {
		SiteBookmark site = dialog.getNewSite();
		model.addBookmark(site);
	}
}

private void performDelete() {
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	ISelection selection = viewer.getSelection();
	if (selection instanceof IStructuredSelection) {
		IStructuredSelection ssel = (IStructuredSelection)selection;
		for (Iterator iter=ssel.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof SiteBookmark) {
				model.removeBookmark((SiteBookmark)obj);
			}
		}
	}
}

private void performRefresh() {
	IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
	Object obj = sel.getFirstElement();
	if (obj!=null && obj instanceof SiteBookmark) {
		final SiteBookmark bookmark = (SiteBookmark)obj;
		BusyIndicator.showWhile(viewer.getTree().getDisplay(), new Runnable() {
			public void run() {
				try {
					bookmark.connect();
					viewer.refresh(bookmark);
				}
				catch (CoreException e) {
					System.out.println(e);
				}
			}
		});
	}
}

class CatalogBag {
	Object [] catalog;
}

private Object [] getSiteCatalog(final SiteBookmark bookmark) {
	if (!bookmark.isSiteConnected()) {
		final CatalogBag bag = new CatalogBag();
		BusyIndicator.showWhile(viewer.getTree().getDisplay(), new Runnable() {
			public void run() {
				try {
					bookmark.connect();
					bag.catalog = bookmark.getCatalog();
				}
				catch (CoreException e) {
				}
			}
		});
		if (bag.catalog!=null) return bag.catalog;
	}
	if (bookmark.getSite()!=null) {
		return bookmark.getCatalog();
	}
	return new Object[0];
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
	if (object instanceof SiteBookmark &&
		property.equals(SiteBookmark.P_NAME))
		viewer.update(object, null);
}

}
