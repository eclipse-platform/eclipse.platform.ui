package org.eclipse.ui.internal.registry;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.core.runtime.*;
import java.util.*;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @see ICategory
 */
public class Category implements ICategory {
	private String id;
	private String name;
	private String [] parentCategoryPath;
	private ArrayList views;
	private IConfigurationElement config;
	public static final String ATT_ID="id";
	public static final String ATT_CATEGORY="parentCategory";
	public static final String ATT_NAME="name";
/**
 * A constructor.
 */
public Category(String id, String name) {
	this.id = id;
	this.name = name;
}
/**
 * A constructor.
 */
public Category(IConfigurationElement aConfig) throws CoreException {
	config = aConfig;
	id = config.getAttribute(ATT_ID);
	name = config.getAttribute(ATT_NAME);
	if (id == null || name == null) {
		throw new CoreException(new Status(IStatus.ERROR, 
			WorkbenchPlugin.PI_WORKBENCH,
			0,
			"Invalid extension: " + id, 
			null));
	}
}
/**
 * Adds a view to this category.
 */
public void addView(IViewDescriptor desc) {
	if (views == null)
		views = new ArrayList(3);
	views.add(desc);
}
/**
 * Returns unique category id.
 */
public String getID() {
	return id;
}
/**
 * Returns category name.
 */
public String getLabel() {
	return name;
}
/**
 * Returns tokens that represent a path from the top
 * to this category, or null if not defined.
 */
public String[] getParentCategoryPath() {
	if (parentCategoryPath != null) 
		return parentCategoryPath;
	if (config != null) {
		String category = config.getAttribute(ATT_CATEGORY);
		if (category==null) return null;
		StringTokenizer stok = new StringTokenizer(category, "/");
		parentCategoryPath = new String [stok.countTokens()];
		for (int i=0; stok.hasMoreTokens(); i++) {
			parentCategoryPath[i]=stok.nextToken();
		}
	}
	return parentCategoryPath;
	

}
/**
 * Returns the root category for this.
 */
public String getRootPath() {
	String result = id;
	String [] parentPath = getParentCategoryPath();
	if (parentPath != null && parentPath.length > 0)
		result = parentPath[0];
	return result;
}
/**
 * Returns the views for this category.
 * May be null.
 */
public ArrayList getViews() {
	return views;
}
}
