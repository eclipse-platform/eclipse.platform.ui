package org.eclipse.update.internal.ui.search;

import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;
import java.util.Map;

public interface ISearchCategory {
	public String getId();
	public void setId(String id);
/**
 * Returns an array of queries that need to be executed
 * when search of this category is initiated.
 */
	public ISearchQuery [] getQueries();
/**
 * Creates control that contains widgets used to configure
 * searches of this category.
 */
	public void createControl(Composite parent, FormWidgetFactory factory);
	
	public Control getControl();
/**
 * Returns a textual representation of the current search. This
 * text will be used in the UI 
 */
	public String getCurrentSearch();
	
	public void load(Map settings);
	public void store(Map settings);
}