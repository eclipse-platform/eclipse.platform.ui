package org.eclipse.ui.views.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.List;
/**
 * A category in a PropertySheet used to group <code>IPropertySheetEntry</code>
 * entries so they are displayed together.
 */
/*package*/ class PropertySheetCategory {
	private String categoryName;
	private List entries = new ArrayList();
	private boolean shouldAutoExpand = true;
/**
 * Create a PropertySheet category with name.
 */
public PropertySheetCategory(String name) {
	categoryName = name;
}
/**
 * Add an <code>IPropertySheetEntry</code> to the list
 * of entries in this category. 
 */
public void addEntry(IPropertySheetEntry entry) {
	entries.add(entry);
}
/**
 * Return the category name.
 */
public String getCategoryName() {
	return categoryName;
}
/**
 * Returns <code>true</code> if this category should be automatically 
 * expanded. The default value is <code>true</code>.
 * 
 * @return <code>true</code> if this category should be automatically 
 * expanded, <code>false</code> otherwise
 */
public boolean getAutoExpand() {
	return shouldAutoExpand;
}
/**
 * Sets if this category should be automatically 
 * expanded.
 */
public void setAutoExpand(boolean autoExpand) {
	shouldAutoExpand = autoExpand;
}
/**
 * Returns the entries in this category.
 *
 * @return the entries in this category
 */
public IPropertySheetEntry[] getChildEntries() {
	return (IPropertySheetEntry[])entries.toArray(new IPropertySheetEntry[entries.size()]);
}
/**
 * Removes all of the entries in this category.
 * Doing so allows us to reuse this category entry.
 */
public void removeAllEntries() {
	entries = new ArrayList();
}
}
