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
		if (imports==null) imports = new ArrayList();
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
	private boolean contains(IFeature feature, IImport iimport) {
		IPluginEntry [] entries = feature.getPluginEntries();
		VersionedIdentifier importId = iimport.getVersionedIdentifier();
		for (int i=0; i<entries.length; i++) {
			IPluginEntry entry = entries[i];
			VersionedIdentifier entryId = entry.getVersionedIdentifier();
			if (entryId.equals(importId))
				return true;
		}
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
		String key = "imports";
		String value = (String)map.get(key);
		imports = new ArrayList();
		if (value!=null)
		decodeImports(value, imports);
		
	}
	public void store(Map map) {
		String value = encodeImports(imports);
		map.put("imports", value);
	}
	public static String encodeImports(ArrayList imports) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<imports.size(); i++) {
			IImport iimport = (IImport)imports.get(i);
			String entry = iimport.getVersionedIdentifier().toString();
			if (i>0) buf.append(":");
			buf.append(entry);
		}
		return buf.toString();
	}
	public static void decodeImports(String text, ArrayList result) {
		StringTokenizer stok = new StringTokenizer(text, ":");
		while (stok.hasMoreTokens()) {
			String token = stok.nextToken();
			int uloc = token.lastIndexOf('_');
			String id = token.substring(0, uloc);
			String version = token.substring(uloc+1);
			Import iimport = new Import();
			iimport.setPluginIdentifier(id);
			iimport.setPluginVersion(version);
			result.add(iimport);
		}
	}
}