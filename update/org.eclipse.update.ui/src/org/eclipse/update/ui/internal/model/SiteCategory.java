package org.eclipse.update.ui.internal.model;

import java.util.*;
import org.eclipse.update.core.*;
import org.eclipse.core.runtime.CoreException;

public class SiteCategory {
	Vector children;
	private ICategory category;
	private String name;
	
	public SiteCategory(String name, ICategory category) {
		this.category = category;
		this.name = name;
		children = new Vector();
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
			if (child instanceof CategorizedFeature) {
				CategorizedFeature cf = (CategorizedFeature)child;
				cf.getFeature();
			}
		}
	}
}