package org.eclipse.text.quicksearch.internal.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.content.IContentType;

class ExtensionsRegistry<T> {
	private final static String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	private final static String EXTENSIONS_ATTRIBUTE = "extensions"; //$NON-NLS-1$
	private final static String CONTENT_TYPE_ID_ATTRIBUTE = "contentTypeId"; //$NON-NLS-1$

	private HashMap<String, T> fIdMap;	// maps ids to data
	private HashMap<String, List<T>> fExtensionMap;	// multimap: maps extensions to list of data
	private HashMap<IContentType, List<T>> fContentTypeBindings; // multimap: maps content type bindings to list of data

	void register(IConfigurationElement element, T data) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		if (id != null) {
			if (fIdMap == null)
				fIdMap = new HashMap<>();
			fIdMap.put(id, data);
		}

		String types = element.getAttribute(EXTENSIONS_ATTRIBUTE);
		if (types != null) {
			if (fExtensionMap == null)
				fExtensionMap = new HashMap<>();
			StringTokenizer tokenizer = new StringTokenizer(types, ","); //$NON-NLS-1$
			while (tokenizer.hasMoreElements()) {
				String extension = tokenizer.nextToken().trim();
				List<T> l = fExtensionMap.get(normalizeCase(extension));
				if (l == null)
					fExtensionMap.put(normalizeCase(extension),	l = new ArrayList<>());
				l.add(data);
			}
		}
	}

	void createBinding(IConfigurationElement element, String idAttributeName) {
		String type = element.getAttribute(CONTENT_TYPE_ID_ATTRIBUTE);
		String id = element.getAttribute(idAttributeName);
		if (id == null)
			QuickSearchActivator.log(QuickSearchActivator.getFormattedString("QuickSearchActivator.targetIdAttributeMissing", idAttributeName)); //$NON-NLS-1$
		if (type != null && id != null && fIdMap != null) {
			T o = fIdMap.get(id);
			if (o != null) {
				IContentType ct = QuickSearchActivator.getDefault().getContentType(type);
				if (ct != null) {
					if (fContentTypeBindings == null)
						fContentTypeBindings = new HashMap<>();
					List<T> l = fContentTypeBindings.get(ct);
					if (l == null)
						fContentTypeBindings.put(ct, l = new ArrayList<>());
					l.add(o);
				} else {
					QuickSearchActivator.log(QuickSearchActivator.getFormattedString("QuickSearchActivator.contentTypeNotFound", type)); //$NON-NLS-1$
				}
			} else {
				QuickSearchActivator.log(QuickSearchActivator.getFormattedString("QuickSearchActivator.targetNotFound", id)); //$NON-NLS-1$
			}
		}
	}

	T search(IContentType type) {
		List<T> list = searchAll(type);
		return list != null ? list.get(0) : null;
	}

	List<T> searchAll(IContentType type) {
		if (fContentTypeBindings != null) {
			for (; type != null; type = type.getBaseType()) {
				List<T> data = fContentTypeBindings.get(type);
				if (data != null)
					return data;
			}
		}
		return null;
	}

	T search(String extension) {
		List<T> list = searchAll(extension);
		return list != null ? list.get(0) : null;
	}

	List<T> searchAll(String extension) {
		if (fExtensionMap != null)
			return fExtensionMap.get(normalizeCase(extension));
		return null;
	}

	Collection<T> getAll() {
		return fIdMap == null ? Collections.emptySet() : fIdMap.values();
	}

	private static String normalizeCase(String s) {
		return s == null ? null : s.toUpperCase();
	}
}