package org.eclipse.update.internal.ui.properties;

import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.search.SearchObject;

public class SearchObjectPropertyPage extends NamedObjectPropertyPage {

	/**
	 * Constructor for SearchObjectPropertyPage.
	 */
	public SearchObjectPropertyPage() {
		super();
	}
	
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		SearchObject searchObject = (SearchObject)getElement();
		objectName.setEditable(!searchObject.isCategoryFixed());
		return control;
	}
}
