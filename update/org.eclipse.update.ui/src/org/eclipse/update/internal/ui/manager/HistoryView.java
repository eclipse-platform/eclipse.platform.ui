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
import java.util.*;
import org.eclipse.swt.graphics.*;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class HistoryView extends BaseTreeView {
private Image featureImage;
private Image siteImage;

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
			return getSiteFeatures(csite.getSite());
		}
		return new Object[0];
	}
	
	private Object[] getSiteFeatures(ISite site) {
		try {
			IFeatureReference [] refs = site.getFeatureReferences();
			Object [] result = new Object[refs.length];
			for (int i=0; i<refs.length; i++) {
				result[i] = refs[i].getFeature();
			}
			return result;
		}
		catch (CoreException e) {
			UpdateUIPlugin.logException(e);
			return new Object[0];
		}
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
		return null;
	}
}

public HistoryView() {
	featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
	siteImage = UpdateUIPluginImages.DESC_SITE_OBJ.createImage();
}

public void initProviders() {
	viewer.setContentProvider(new HistoryProvider());
	viewer.setLabelProvider(new HistoryLabelProvider());
	try {
		viewer.setInput(SiteManager.getLocalSite());
	}
	catch (CoreException e) {
		UpdateUIPlugin.logException(e);
	}
}

protected void partControlCreated() {
}

public void dispose() {
	featureImage.dispose();
	siteImage.dispose();
	
	super.dispose();
}

}
