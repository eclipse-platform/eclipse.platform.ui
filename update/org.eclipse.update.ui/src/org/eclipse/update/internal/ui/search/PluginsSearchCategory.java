package org.eclipse.update.internal.ui.search;

import java.util.*;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.model.ISiteAdapter;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

public class PluginsSearchCategory extends SearchCategory {
	private ArrayList imports;

	public PluginsSearchCategory() {
	}

	public void initialize() {
		imports = new ArrayList();
	}
	public ISearchQuery [] getQueries() {
		initialize();
		ISearchQuery query = new ISearchQuery() {
			public ISiteAdapter getSearchSite() {
				return null;
			}
			public boolean matches(IFeature feature) {
				for (int i = 0; i < imports.size(); i++) {
					IImport iimport = (IImport)imports.get(i);
					if (!contains(feature, iimport))
						return false;
				}
				return true;
			}
		};
		return new ISearchQuery [] { query };
	}
	private boolean contains(IFeature feauture, IImport iimport) {
		return false;
	}
	
	public String getCurrentSearch() {
		return "Required Plug-ins";
	}
	public void createControl(Composite parent, FormWidgetFactory factory) {
		Composite container = factory.createComposite(parent);
		setControl(container);
	}
	public void load(Map map) {
	}
	public void store(Map map) {
	}
}