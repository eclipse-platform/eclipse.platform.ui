package org.eclipse.update.internal.ui.search;

import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;
import java.util.Map;

public abstract class SearchCategory implements ISearchCategory {
	private Control control;
	private String id;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Control getControl() {
		return control;
	}
	
	public void setControl(Control control) {
		this.control = control;
	}
	protected String getString(String key, Map map) {
		Object obj = map.get(key);
		if (obj!=null) return obj.toString();
		return "";
	}
	protected boolean getBoolean(String key, Map map) {
		return getString(key, map).equals("true");
	}
}
