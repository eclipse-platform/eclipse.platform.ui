package org.eclipse.update.internal.ui.manager;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.ui.model.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.core.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Image;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class SiteView extends BaseTreeView implements IUpdateModelChangedListener {
	private Action newAction;
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
	model.addSiteModelChangedListener(this);
	siteImage = UpdateUIPluginImages.DESC_SITE_OBJ.createImage();
}

public void dispose() {
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	model.removeSiteModelChangedListener(this);	
	siteImage.dispose();
	super.dispose();
}

public void initProviders() {
	viewer.setContentProvider(new SiteProvider());
	viewer.setLabelProvider(new SiteLabelProvider());
	viewer.setInput(UpdateUIPlugin.getDefault().getUpdateModel());
}

public void makeActions() {
	newAction = new Action() {
		public void run() {
			performNew();
		}
	};
	newAction.setText("New Site...");
}

public void fillActionBars() {
	IActionBars bars = getViewSite().getActionBars();
}

public void fillContextMenu(IMenuManager manager) {
	manager.add(newAction);
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

private IFeature [] getSiteFeatures(SiteBookmark bookmark) {
	return new IFeature[0];
}

public void objectAdded(Object parent, Object child) {
	if (child instanceof SiteBookmark) {
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		viewer.add(model, child);
	}
}

public void objectRemoved(Object parent, Object child) {
}

public void objectChanged(Object object) {
}

}
