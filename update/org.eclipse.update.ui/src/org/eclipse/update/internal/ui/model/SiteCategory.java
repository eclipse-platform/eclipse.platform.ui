package org.eclipse.update.internal.ui.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.update.core.ICategory;
import org.eclipse.update.core.IFeatureReference;
import org.eclipse.update.core.IURLEntry;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

public class SiteCategory extends UIModelObject {
private static final String KEY_OTHER_LABEL= "SiteCategory.other.label";
private static final String KEY_OTHER_DESCRIPTION= "SiteCategory.other.description";

	Vector children;
	private ICategory category;
	private String name;
	private boolean touched;
	private int featureCount;
	private boolean canceled;
	
	class OtherCategory implements ICategory {
		IURLEntry entry;
		public OtherCategory() {
			entry = new IURLEntry () {
				public String getAnnotation() {
					return UpdateUIPlugin.getResourceString(KEY_OTHER_DESCRIPTION);
				}
				public URL getURL() {
					return null;
				}
				public Object getAdapter(Class clazz) {
					return null;
				}
				public int getType() {
					return IURLEntry.UPDATE_SITE;
				}				
			};
		}
		public String getName() {
			return SiteCategory.this.getName();
		}
		public String getLabel() {
			return SiteCategory.this.getName();
		}
		public IURLEntry getDescription() {
			return entry;
		}
		public Object getAdapter(Class clazz) {
			return null;
		}
	}
	
	public SiteCategory(String name, ICategory category) {
		if (category==null) {
		   this.name = UpdateUIPlugin.getResourceString(KEY_OTHER_LABEL);
		   this.category = new OtherCategory();
		}
		else {
			this.name = name;
			this.category = category;
		}
		children = new Vector();
	}
	
	public boolean isOtherCategory() {
		return category instanceof OtherCategory;
	}
	
	public Object [] getChildren() {
		return canceled?new Object[0]:children.toArray();
	}
	
	public int getChildCount() {
		return canceled?0:children.size();
	}
	
	public String getName() {
		return name;
	}
	public String getFullName() {
		return category.getName();
	}
	
	public String toString() {
		return category.getLabel();
	}
	
	public ICategory getCategory() {
		return category;
	}
	
	void add(Object child) {
		if (child instanceof IFeatureAdapter)
			featureCount++;
		children.add(child);
	}
	
	public void touchFeatures(IRunnableContext context) {
		if (children.size()==0 || touched || featureCount==0) return;
		
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				monitor.beginTask("Downloading: ", featureCount);
				for (int i=0; i<children.size(); i++) {
					Object child = children.get(i);
					if (monitor.isCanceled())
						break;
					if (child instanceof IFeatureAdapter) {
						IFeatureAdapter adapter = (IFeatureAdapter)child;
						monitor.subTask(adapter.getFastLabel());
						try {
							adapter.getFeature(null);
						}
						catch (CoreException e) {
						}
						finally {
							monitor.worked(1);
						}
					}
				}
				monitor.done();
			}
		};
		
		try {
			context.run(true, true, op);
			touched = true;
		}
		catch (InterruptedException e) {
			canceled = true;
		}
		catch (InvocationTargetException e) {
		}
	}
	
	public void addFeaturesTo(Vector flatList) {
		for (int i=0; i<children.size(); i++) {
			Object child = children.get(i);
			if (child instanceof FeatureReferenceAdapter) {
				FeatureReferenceAdapter cfeature = (FeatureReferenceAdapter)child;
				// Don't add duplicates - there may be the same
				// feature present in several categories
				if (findFeature(flatList, cfeature.getFeatureReference())==null) {
					flatList.add(child);
				}
			}
			else if (child instanceof SiteCategory) {
				((SiteCategory)child).addFeaturesTo(flatList);
			}
		}
	}
	
	private FeatureReferenceAdapter findFeature(Vector flatList, IFeatureReference featureRef) {
		for (int i=0; i<flatList.size(); i++) {
			FeatureReferenceAdapter cfeature = (FeatureReferenceAdapter)flatList.get(i);
			if (cfeature.getFeatureReference().equals(featureRef))
			   return cfeature;
		}
		return null;
	}
}