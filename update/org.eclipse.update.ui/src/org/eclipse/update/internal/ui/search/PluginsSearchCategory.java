package org.eclipse.update.internal.ui.search;

import java.util.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.model.ISiteAdapter;
import org.eclipse.update.internal.ui.parts.DefaultContentProvider;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

public class PluginsSearchCategory extends SearchCategory {
	private static final String KEY_NEW = "PluginSearchCategory.new";
	private ArrayList imports;
	private TableViewer tableViewer;
	private Button newButton;

	class ImportContentProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {
		public Object[] getElements(Object parent) {
			if (imports == null)
				return new Object[0];
			return imports.toArray();
		}
	}

	class ImportLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof IImport) {
				IImport iimport = (IImport) obj;
				return iimport.getVersionedIdentifier().toString();
			}
			return obj.toString();
		}
	}

	public PluginsSearchCategory() {
	}

	public void initialize() {
		if (imports == null)
			imports = new ArrayList();
	}
	public ISearchQuery[] getQueries() {
		initialize();
		ISearchQuery query = new ISearchQuery() {
			public ISiteAdapter getSearchSite() {
				return null;
			}
			public boolean matches(IFeature feature) {
				for (int i = 0; i < imports.size(); i++) {
					IImport iimport = (IImport) imports.get(i);
					if (!contains(feature, iimport))
						return false;
				}
				return true;
			}
		};
		return new ISearchQuery[] { query };
	}
	private boolean contains(IFeature feature, IImport iimport) {
		IPluginEntry[] entries = feature.getPluginEntries();
		VersionedIdentifier importId = iimport.getVersionedIdentifier();
		for (int i = 0; i < entries.length; i++) {
			IPluginEntry entry = entries[i];
			VersionedIdentifier entryId = entry.getVersionedIdentifier();
			if (entryId.equals(importId))
				return true;
		}
		return false;
	}

	public String getCurrentSearch() {
		return encodeImports(imports);
	}
	public void createControl(Composite parent, FormWidgetFactory factory) {
		Composite container = factory.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		container.setLayout(layout);
		tableViewer = new TableViewer(container, SWT.V_SCROLL | SWT.H_SCROLL);
		tableViewer.setContentProvider(new ImportContentProvider());
		tableViewer.setLabelProvider(new ImportLabelProvider());
		tableViewer.setInput(this);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		tableViewer.getControl().setLayoutData(gd);
		newButton =
			factory.createButton(
				container,
				UpdateUIPlugin.getResourceString(KEY_NEW),
				SWT.PUSH);
		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
			}
		});
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		newButton.setLayoutData(gd);
		factory.paintBordersFor(container);
		setControl(container);
	}
	public void load(Map map, boolean editable) {
		String key = "imports";
		String value = (String) map.get(key);
		imports = new ArrayList();
		if (value != null)
			decodeImports(value, imports);
		tableViewer.refresh();
		newButton.setEnabled(editable);
	}
	public void store(Map map) {
		String value = encodeImports(imports);
		map.put("imports", value);
	}
	public static String encodeImports(ArrayList imports) {
		if (imports == null)
			return "";
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < imports.size(); i++) {
			IImport iimport = (IImport) imports.get(i);
			String entry = iimport.getVersionedIdentifier().toString();
			if (i > 0)
				buf.append(",");
			buf.append(entry);
		}
		return buf.toString();
	}
	public static void decodeImports(String text, ArrayList result) {
		StringTokenizer stok = new StringTokenizer(text, ",");
		while (stok.hasMoreTokens()) {
			String token = stok.nextToken();
			int uloc = token.lastIndexOf('_');
			String id = token.substring(0, uloc);
			String version = token.substring(uloc + 1);
			Import iimport = new Import();
			iimport.setPluginIdentifier(id);
			iimport.setPluginVersion(version);
			result.add(iimport);
		}
	}
}