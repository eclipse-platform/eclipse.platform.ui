package org.eclipse.update.internal.ui.manager;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.jface.viewers.*;
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
	
class SiteProvider extends DefaultContentProvider
					implements ITreeContentProvider {

	public Object[] getChildren(Object parent) {
		if (parent instanceof UpdateModel) {
			UpdateModel model = (UpdateModel)parent;
			return model.getBookmarks();
		}
		if (parent instanceof SiteBookmark) {
			return getSiteFeatures((SiteBookmark)parent);
		}
		return new Object[0];
	}

	public Object getParent(Object child) {
		return null;
	}
	public boolean hasChildren(Object parent) {
		if (parent instanceof SiteBookmark)
		   return true;
		return false;
	}
	public Object[] getElements(Object obj) {
		return getChildren(obj);
	}
}	

class SiteLabelProvider extends LabelProvider {
	public Image getImage(Object obj) {
		if (obj instanceof SiteBookmark) {
			return siteImage;
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
}

public void dispose() {
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	model.removeUpdateModelChangedListener(this);	
	siteImage.dispose();
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
}

public void fillActionBars() {
	IActionBars bars = getViewSite().getActionBars();
}

public void fillContextMenu(IMenuManager manager) {
	manager.add(newAction);
	manager.add(deleteAction);
	manager.add(new Separator());
	manager.add(propertiesAction);
	ISelection selection = viewer.getSelection();
	deleteAction.setEnabled(!selection.isEmpty());
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

private IFeature [] getSiteFeatures(final SiteBookmark bookmark) {
	if (!bookmark.isSiteConnected()) {
		BusyIndicator.showWhile(viewer.getTree().getDisplay(), new Runnable() {
			public void run() {
				try {
					bookmark.connect(null);
				}
				catch (CoreException e) {
				}
			}
		});
	}
	if (bookmark.getSite()!=null) {
		return bookmark.getSite().getFeatures();
	}
	return new IFeature[0];
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
