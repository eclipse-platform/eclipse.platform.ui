package org.eclipse.update.internal.ui.parts;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.ui.forms.internal.*;
import java.util.Hashtable;

public class UpdateWebForm extends WebForm implements IUpdateForm {
	private IUpdateFormPage page;
	private Hashtable settingsTable;
	
	public UpdateWebForm(IUpdateFormPage page) {
		this.page = page;
	}
	
	public IUpdateFormPage getPage() {
		return page;
	}
	
	protected Object getSettings(Object input) {
		if (settingsTable==null) return null;
		return settingsTable.get(input);
	}

	protected void setSettings(Object input, Object settings) {
		if (settingsTable==null) settingsTable = new Hashtable();
		settingsTable.put(input, settings);
	}
}

