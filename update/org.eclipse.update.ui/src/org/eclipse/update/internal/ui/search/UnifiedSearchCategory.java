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
			ISite site,
			IProgressMonitor monitor) {
			IFeatureReference [] refs = site.getFeatureReferences();

			monitor.beginTask("", refs.length);
			ArrayList result = new ArrayList();

			for (int i=0; i<refs.length; i++) {
				IFeatureReference ref = refs[i];
				if (monitor.isCanceled())
					break;
				try {
					IFeature feature = ref.getFeature(null);
					result.add(feature);
					monitor.subTask(feature.getLabel());
				}
				catch (CoreException e) {
				}
				finally {
					monitor.worked(1);
				}
			}
			return (IFeature[])result.toArray(new IFeature[result.size()]);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.internal.ui.search.ISearchQuery#getSearchSite()
		 */
		public ISiteAdapter getSearchSite() {
			return null;
		}
	}
	
	public UnifiedSearchCategory () {
		query = new UnifiedQuery();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.search.ISearchCategory#getQueries()
	 */
	public ISearchQuery[] getQueries() {
		return new ISearchQuery[] {query};
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
