package org.eclipse.update.internal.ui.search;

import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.model.ISiteAdapter;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

public class UpdatesSearchCategory extends SearchCategory {
	private static final String KEY_CURRENT_SEARCH = "UpdatesSearchCategory.currentSearch";

	class SiteAdapter implements ISiteAdapter {
		IURLEntry entry;
		SiteAdapter(IURLEntry entry) {
			this.entry = entry;
		}
		public URL getURL() {
			return entry.getURL();
		}
		public String getLabel() {
			String label = entry.getAnnotation();
			if (label == null || label.length() == 0)
				label = getURL().toString();
			return label;
		}
		public ISite getSite() {
			try {
				return SiteManager.getSite(getURL());
			} catch (CoreException e) {
				return null;
			}
		}
	}

	class UpdateQuery implements ISearchQuery {
		IFeature candidate;
		ISiteAdapter adapter;

		public UpdateQuery(IFeature candidate) {
			this.candidate = candidate;
			IURLEntry entry = candidate.getUpdateSiteEntry();
			if (entry != null && entry.getURL() != null)
				adapter = new SiteAdapter(entry);
		}
		public ISiteAdapter getSearchSite() {
			return adapter;
		}
		public boolean matches(IFeature feature) {
			return isNewerVersion(candidate, feature);
		}
	}

	private ArrayList candidates;

	public UpdatesSearchCategory() {
	}

	public void initialize() {
		candidates = new ArrayList();
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration config = localSite.getCurrentConfiguration();
			IConfiguredSite[] isites = config.getConfiguredSites();
			for (int i = 0; i < isites.length; i++) {
				ISite isite = isites[i].getSite();
				IFeatureReference[] refs = isite.getFeatureReferences();
				for (int j = 0; j < refs.length; j++) {
					IFeatureReference ref = refs[j];
					candidates.add(ref.getFeature());
				}
			}
		} catch (CoreException e) {
			UpdateUIPlugin.logException(e, false);
		}
	}
	public ISearchQuery[] getQueries() {
		initialize();
		ISearchQuery[] queries = new ISearchQuery[candidates.size()];
		for (int i = 0; i < candidates.size(); i++) {
			queries[i] = new UpdateQuery((IFeature) candidates.get(i));
		}
		return queries;
	}

	public String getCurrentSearch() {
		return UpdateUIPlugin.getResourceString(KEY_CURRENT_SEARCH);
	}

	private boolean isNewerVersion(IFeature feature, IFeature candidate) {
		VersionedIdentifier fvi = feature.getVersionedIdentifier();
		VersionedIdentifier cvi = candidate.getVersionedIdentifier();
		Version fv = fvi.getVersion();
		Version cv = cvi.getVersion();
		return cv.compare(fv) > 0;
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