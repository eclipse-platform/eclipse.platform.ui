/*
 * Created on Apr 18, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.ui.search;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.model.ISiteAdapter;
import org.eclipse.update.internal.ui.model.SiteBookmark;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UnifiedSearchCategory extends SearchCategory {
	private UnifiedQuery query;

	class UnifiedQuery implements ISearchQuery {
		public IFeature[] getMatchingFeatures(
			ISiteAdapter adapter,
			ISite site,
			IProgressMonitor monitor) {
			ISiteFeatureReference[] refs = site.getFeatureReferences();
			HashSet ignores = new HashSet();
			if (adapter instanceof SiteBookmark) {
				SiteBookmark bookmark = (SiteBookmark) adapter;
				String[] ignoredCategories = bookmark.getIgnoredCategories();
				if (ignoredCategories != null) {
					for (int i = 0; i < ignoredCategories.length; i++) {
						ignores.add(ignoredCategories[i]);
					}
				}
			}

			monitor.beginTask("", refs.length);
			ArrayList result = new ArrayList();

			for (int i = 0; i < refs.length; i++) {
				ISiteFeatureReference ref = refs[i];
				boolean skipFeature = false;
				if (monitor.isCanceled())
					break;
				if (ignores.size() > 0) {
					ICategory[] categories = ref.getCategories();

					for (int j = 0; j < categories.length; j++) {
						ICategory category = categories[j];
						if (ignores.contains(category.getName())) {
							skipFeature = true;
							break;
						}
					}
				}
				try {
					if (!skipFeature) {
						IFeature feature = ref.getFeature(null);
						result.add(feature);
						monitor.subTask(feature.getLabel());
					}
				} catch (CoreException e) {
				} finally {
					monitor.worked(1);
				}
			}
			return (IFeature[]) result.toArray(new IFeature[result.size()]);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.internal.ui.search.ISearchQuery#getSearchSite()
		 */
		public ISiteAdapter getSearchSite() {
			return null;
		}
	}

	public UnifiedSearchCategory() {
		query = new UnifiedQuery();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.search.ISearchCategory#getQueries()
	 */
	public ISearchQuery[] getQueries() {
		return new ISearchQuery[] { query };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.search.ISearchCategory#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.update.ui.forms.internal.FormWidgetFactory)
	 */
	public void createControl(Composite parent, FormWidgetFactory factory) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.search.ISearchCategory#getCurrentSearch()
	 */
	public String getCurrentSearch() {
		return "New features";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.search.ISearchCategory#load(java.util.Map, boolean)
	 */
	public void load(Map settings, boolean editable) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.search.ISearchCategory#store(java.util.Map)
	 */
	public void store(Map settings) {
	}
}
