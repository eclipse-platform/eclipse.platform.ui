package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;

import java.io.InputStream;
import java.net.URL;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.custom.*;
import org.eclipse.update.core.*;
import org.eclipse.jface.action.Action;
import org.eclipse.update.core.IInstallConfiguration;
import java.util.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.action.*;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.*;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class HistoryView extends BaseTreeView implements ILocalSiteChangedListener{
private Image configImage;
private Image featureImage;
private Image siteImage;
private Image currentConfigImage;
private Action revertAction;
private Action preserveAction;
private Action removePreservedAction;
private Action propertiesAction;
private IUpdateModelChangedListener modelListener;
private SavedFolder savedFolder;
private static final String KEY_RESTORE = "HistoryView.Popup.restore";
private static final String KEY_PRESERVE= "HistoryView.Popup.preserve";
private static final String KEY_REMOVE_PRESERVED = "HistoryView.Popup.removePreserved";
private static final String KEY_SAVED_FOLDER = "HistoryView.savedFolder";

class SavedFolder {
	private String label;
	private Image image;
	
	public SavedFolder() {
		label = UpdateUIPlugin.getResourceString(KEY_SAVED_FOLDER);
		String imageKey = ISharedImages.IMG_OBJ_FOLDER;
		image = PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
	
	public Image getImage() {
		return image;
	}
		
	public String toString() {
		return label;
	}
	
	public Object [] getChildren() {
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			return makeChildren(localSite.getPreservedConfigurations());
		}
		catch (CoreException e) {
			return new Object [0];
		}
	}
	
	private Object [] makeChildren(IInstallConfiguration [] preserved) {
		Object [] children = new Object[preserved.length];
		for (int i=0; i<preserved.length; i++) {
			children[i] = new PreservedConfiguration(preserved[i]);
		}
		return children;
	}
}


class HistoryProvider extends DefaultContentProvider 
						implements ITreeContentProvider {
	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		if (parent instanceof SavedFolder) {
			return ((SavedFolder)parent).getChildren();
		}
		if (parent instanceof PreservedConfiguration) {
			// resolve the adapter
			parent = ((PreservedConfiguration)parent).getConfiguration();
		}
		if (parent instanceof IInstallConfiguration) {
			return ((IInstallConfiguration)parent).getConfigurationSites();
		}
		if (parent instanceof IConfigurationSite) {
			IConfigurationSite csite = (IConfigurationSite)parent;
			return getConfiguredFeatures(csite);
		}
		return new Object[0];
	}
	
	private Object[] getConfiguredFeatures(IConfigurationSite csite) {
		IFeatureReference [] refs = csite.getConfiguredFeatures();
		ISite site = csite.getSite();
		Object [] result = new Object[refs.length];
		for (int i=0; i<refs.length; i++) {
			try {
				result[i] = refs[i].getFeature();
			}
			catch (CoreException e) {
				result[i] = new MissingFeature(site, refs[i].getURL());
			}
		}
		return result;
	}


	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object child) {
		return null;
	}

	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object parent) {
		return getChildren(parent).length>0;
	}

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object input) {
		if (input instanceof ILocalSite) {
			Object [] history = ((ILocalSite)input).getConfigurationHistory();
			Object [] result = new Object[1+history.length];
			result[0] = savedFolder;
			for (int i=1; i<=history.length; i++) {
				result [i] = history[i-1];
			}
			return result;
		}
		return new Object[0];
	}
}

class HistoryLabelProvider extends LabelProvider {
	public String getText(Object obj) {
		if (obj instanceof IInstallConfiguration) {
			IInstallConfiguration config = (IInstallConfiguration)obj;
			return config.getLabel();
		}
		if (obj instanceof IConfigurationSite) {
			IConfigurationSite csite = (IConfigurationSite)obj;
			ISite site = csite.getSite();
			return site.getURL().toString();
		}
		if (obj instanceof IFeature) {
			IFeature feature = (IFeature)obj;
			String version = feature.getVersionIdentifier().getVersion().toString();
			return feature.getLabel() + " "+version;
		}
		return super.getText(obj);
	}
	public Image getImage(Object obj) {
		if (obj instanceof SavedFolder)
		   return ((SavedFolder)obj).getImage();
		if (obj instanceof IFeature)
		   return featureImage;
		if (obj instanceof IConfigurationSite)
		   return siteImage;
		if (obj instanceof PreservedConfiguration) {
			obj = ((PreservedConfiguration)obj).getConfiguration();
		}
		if (obj instanceof IInstallConfiguration) {
			IInstallConfiguration config = (IInstallConfiguration)obj;
			if (config.isCurrent()) return currentConfigImage;
			return configImage;
		}
		return null;
	}
}

public HistoryView() {
	featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
	siteImage = UpdateUIPluginImages.DESC_SITE_OBJ.createImage();
	configImage = UpdateUIPluginImages.DESC_CONFIG_OBJ.createImage();
	ImageDescriptor cdesc = new OverlayIcon(UpdateUIPluginImages.DESC_CONFIG_OBJ,
					new ImageDescriptor [][] {{}, {UpdateUIPluginImages.DESC_CURRENT_CO}});
	currentConfigImage = cdesc.createImage();
	savedFolder = new SavedFolder();
}

public void initProviders() {
	viewer.setContentProvider(new HistoryProvider());
	viewer.setLabelProvider(new HistoryLabelProvider());
	try {
		ILocalSite localSite = SiteManager.getLocalSite();
		viewer.setInput(localSite);
		localSite.addLocalSiteChangedListener(this);
	}
	catch (CoreException e) {
		UpdateUIPlugin.logException(e);
	}
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	modelListener = new IUpdateModelChangedListener () {
		public void objectAdded(Object parent, Object child) {
		}
		public void objectRemoved(Object parent, Object child) {
		}
		public void objectChanged(Object obj, String property) {
			viewer.update(obj, null);
		}
	};
	model.addUpdateModelChangedListener(modelListener);
}

protected void partControlCreated() {
}

public void dispose() {
	featureImage.dispose();
	siteImage.dispose();
	configImage.dispose();
	currentConfigImage.dispose();
	
	try {
		ILocalSite localSite = SiteManager.getLocalSite();
		localSite.removeLocalSiteChangedListener(this);
	}
	catch (CoreException e) {
		UpdateUIPlugin.logException(e);
	}
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	model.removeUpdateModelChangedListener(modelListener);
	super.dispose();
}

private IInstallConfiguration getSelectedConfiguration(boolean onlyPreserved) {
	ISelection selection = viewer.getSelection();

	if (selection instanceof IStructuredSelection &&
			!selection.isEmpty()) {
		IStructuredSelection ssel = (IStructuredSelection)selection;
		if (ssel.size()==1) {
			Object obj = ssel.getFirstElement();
			if (!onlyPreserved && obj instanceof IInstallConfiguration)
			   return (IInstallConfiguration)obj;
			if (obj instanceof PreservedConfiguration)
			    return ((PreservedConfiguration)obj).getConfiguration();
		}
	}
	return null;
}

protected void makeActions() {
	super.makeActions();
	revertAction = new Action() {
		public void run() {
			IInstallConfiguration target = getSelectedConfiguration(false);
			if (target!=null)
			   RevertSection.performRevert(target);
		}
	};
	revertAction.setText(UpdateUIPlugin.getResourceString(KEY_RESTORE));
	preserveAction = new Action() {
		public void run() {
			IInstallConfiguration target = getSelectedConfiguration(false);
			if (target == null) return;
			try {
				ILocalSite localSite = SiteManager.getLocalSite();
				localSite.addToPreservedConfigurations(target);
				localSite.save();
				viewer.refresh(savedFolder);
			}
			catch (CoreException e) {
				UpdateUIPlugin.logException(e);
			}
		}
	};
	preserveAction.setText(UpdateUIPlugin.getResourceString(KEY_PRESERVE));
	removePreservedAction = new Action() {
		public void run() {
			IInstallConfiguration target = getSelectedConfiguration(true);
			if (target==null) return;
			if (isPreserved(target)==false) return;
			try {
				ILocalSite localSite = SiteManager.getLocalSite();
				// what API to call to remove??
				// localSite.??
				// localSite.save();
				// viewer.remove(target);
			}
			catch (CoreException e) {
				UpdateUIPlugin.logException(e);
			}
		}
	};
	removePreservedAction.setText(UpdateUIPlugin.getResourceString(KEY_REMOVE_PRESERVED));
	propertiesAction = new PropertyDialogAction(UpdateUIPlugin.getActiveWorkbenchShell(), viewer);
}

private boolean isPreserved(IInstallConfiguration config) {
	try {
		ILocalSite localSite = SiteManager.getLocalSite();
		IInstallConfiguration [] preservedConfigs = 
				localSite.getPreservedConfigurations();
		for (int i=0; i<preservedConfigs.length; i++) {
			if (preservedConfigs[i].equals(config)) return true;
		}
		return false;
	}
	catch (CoreException e) {
		return false;
	}
}

protected void fillContextMenu(IMenuManager manager) {
	IInstallConfiguration config = getSelectedConfiguration(false);
	if (config!=null && !config.isCurrent()) {
		manager.add(revertAction);
		manager.add(new Separator());
	}
	if (config!=null && !isPreserved(config)) {
		manager.add(preserveAction);
	}
	config = getSelectedConfiguration(true);
	if (config!=null) {
		manager.add(removePreservedAction);
	}
	super.fillContextMenu(manager);
	manager.add(propertiesAction);
}

public void currentInstallConfigurationChanged(IInstallConfiguration configuration) {
	viewer.refresh();
}

public void installConfigurationRemoved(IInstallConfiguration configuration) {
}

}
