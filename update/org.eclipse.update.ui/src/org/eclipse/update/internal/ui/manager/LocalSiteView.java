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
import org.eclipse.update.core.ICategory;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IImport;
import org.eclipse.update.core.IInfo;
import org.eclipse.update.core.IInstallConfiguration;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.ISiteChangedListener;
import org.eclipse.update.core.VersionedIdentifier;
import java.util.*;
import org.eclipse.swt.graphics.*;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class LocalSiteView extends BaseTreeView {
	
class FolderObject {
	String label;
	
	public FolderObject(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	public String toString() {
		return getLabel();
	}
}

FolderObject myEclipse = new FolderObject("Installed Features");
FolderObject updates = new FolderObject("Available Updates");
Image eclipseImage;
Image updatesImage;
Image featureImage;
	
class LocalSiteProvider extends DefaultContentProvider 
						implements ITreeContentProvider {
	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		if (parent instanceof UpdateModel) {
			return new Object [] { myEclipse, updates };
		}
		if (parent == myEclipse) {
			return openLocalSite();
		}
		if (parent == updates) {
			return checkForUpdates();
		}
		return new Object[0];
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
		if (parent instanceof FolderObject) return true;
		return false;
	}

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object input) {
		return getChildren(input);
	}
}

class LocalSiteLabelProvider extends LabelProvider {
	public Image getImage(Object obj) {
		if (obj.equals(myEclipse))
		   return eclipseImage;
		if (obj.equals(updates))
		   return updatesImage;
		if (obj instanceof IFeature)
		   return featureImage;
		return null;
	}
}

public LocalSiteView() {
	eclipseImage = UpdateUIPluginImages.DESC_ECLIPSE_OBJ.createImage();
	updatesImage = UpdateUIPluginImages.DESC_UPDATES_OBJ.createImage();
	featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
}

public void initProviders() {
	viewer.setContentProvider(new LocalSiteProvider());
	viewer.setInput(UpdateUIPlugin.getDefault().getUpdateModel());
	viewer.setLabelProvider(new LocalSiteLabelProvider());
}

private Object [] openLocalSite() {
	return new Object[0];
}

private Object [] checkForUpdates() {
	BusyIndicator.showWhile(viewer.getTree().getDisplay(),
					new Runnable() {
		public void run() {
			try {
			   Thread.currentThread().sleep(2000);
			}
			catch (InterruptedException e) {
			}
		}
	});
	return new Object[0];
}

}
