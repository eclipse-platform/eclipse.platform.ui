package org.eclipse.update.internal.ui.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.update.core.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import java.net.URL;
import org.eclipse.core.runtime.PlatformObject;

public class SiteCategory extends PlatformObject {
private static final String KEY_OTHER_LABEL= "SiteCategory.other.label";
private static final String KEY_OTHER_DESCRIPTION= "SiteCategory.other.description";

	Vector children;
	private ICategory category;
	private String name;
	
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
		return children.toArray();
	}
	
	public int getChildCount() {
		return children.size();
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
		children.add(child);
	}
	
	public void touchFeatures() throws CoreException {
		for (int i=0; i<children.size(); i++) {
			Object child = children.get(i);
			if (child instanceof FeatureReferenceAdapter) {
				FeatureReferenceAdapter cf = (FeatureReferenceAdapter)child;
				cf.getFeature();
			}
			else if (child instanceof SiteCategory) {
				((SiteCategory)child).touchFeatures();
			}
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