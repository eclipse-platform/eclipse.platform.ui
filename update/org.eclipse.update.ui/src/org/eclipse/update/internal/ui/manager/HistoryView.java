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
private Action propertiesAction;
private IUpdateModelChangedListener modelListener;
private static final String KEY_RESTORE = "HistoryView.Popup.restore";


class HistoryProvider extends DefaultContentProvider 
						implements ITreeContentProvider {
	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		if (parent instanceof ILocalSite) {
			return ((ILocalSite)parent).getConfigurationHistory();
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
		return getChildren(input);
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
			String version = feature.getIdentifier().getVersion().toString();
			return feature.getLabel() + " "+version;
		}
		return super.getText(obj);
	}
	public Image getImage(Object obj) {
		if (obj instanceof IFeature)
		   return featureImage;
		if (obj instanceof IConfigurationSite)
		   return siteImage;
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

private IInstallConfiguration getSelectedConfiguration() {
	ISelection selection = viewer.getSelection();

	if (selection instanceof IStructuredSelection &&
			!selection.isEmpty()) {
		IStructuredSelection ssel = (IStructuredSelection)selection;
		if (ssel.size()==1) {
			Object obj = ssel.getFirstElement();
			if (obj instanceof IInstallConfiguration)
			   return (IInstallConfiguration)obj;
		}
	}
	return null;
}

protected void makeActions() {
	super.makeActions();
	revertAction = new Action() {
		public void run() {
			IInstallConfiguration target = getSelectedConfiguration();
			if (target!=null)
			   RevertSection.performRevert(target);
		}
	};
	revertAction.setText(UpdateUIPlugin.getResourceString(KEY_RESTORE));
	propertiesAction = new PropertyDialogAction(UpdateUIPlugin.getActiveWorkbenchShell(), viewer);
}

protected void fillContextMenu(IMenuManager manager) {
	IInstallConfiguration config = getSelectedConfiguration();
	if (config!=null && !config.isCurrent()) {
		manager.add(revertAction);
		manager.add(new Separator());
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
