package org.eclipse.update.internal.ui.search;

import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.forms.ActivityConstraints;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.model.ISiteAdapter;
import org.eclipse.update.internal.ui.parts.Sorter;
import org.eclipse.update.internal.ui.preferences.MainPreferencePage;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

public class UpdatesSearchCategory extends SearchCategory {
	private static final String KEY_CURRENT_SEARCH =
		"UpdatesSearchCategory.currentSearch";

	class Hit {
		IFeature candidate;
		IFeatureReference ref;
		public Hit(IFeature candidate, IFeatureReference ref) {
			this.candidate = candidate;
			this.ref = ref;
		}

		public PendingChange getJob() {
			try {
				IFeature feature = ref.getFeature();
				return new PendingChange(candidate, feature);
			} catch (CoreException e) {
				return null;
			}
		}
	}

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

	class HitSorter extends Sorter {
		public boolean compare(Object left, Object right) {
			Hit hit1 = (Hit) left;
			Hit hit2 = (Hit) right;
			try {
				VersionedIdentifier hv1 = hit1.ref.getVersionedIdentifier();
				VersionedIdentifier hv2 = hit2.ref.getVersionedIdentifier();
				return isNewerVersion(hv2, hv1);
			} catch (CoreException e) {
				return false;
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
		public IFeature[] getMatchingFeatures(
			ISite site,
			IProgressMonitor monitor) {
			ArrayList hits = new ArrayList();
			IFeatureReference[] refs = site.getFeatureReferences();
			monitor.beginTask("", refs.length + 1);
			for (int i = 0; i < refs.length; i++) {
				IFeatureReference ref = refs[i];
				try {
					if (isNewerVersion(candidate.getVersionedIdentifier(),
						ref.getVersionedIdentifier())) {
						hits.add(new Hit(candidate, ref));
					}
				} catch (CoreException e) {
				}
				monitor.worked(1);
			}
			IFeature[] result;
			if (hits.size() == 0)
				result = new IFeature[0];
			else {
				IFeature topHit = getFirstValid(hits);
				if (topHit == null)
					result = new IFeature[0];
				else
					result = new IFeature[] { topHit };
			}
			monitor.worked(1);
			monitor.done();
			return result;
		}
	}

	private ArrayList candidates;

	public UpdatesSearchCategory() {
	}

	private IFeature getFirstValid(ArrayList hits) {
		Object[] array = hits.toArray();
		HitSorter sorter = new HitSorter();
		sorter.sortInPlace(array);
		for (int i = 0; i < array.length; i++) {
			Hit hit = (Hit) array[i];
			PendingChange job = hit.getJob();
			if (job == null)
				continue;
			IStatus status = ActivityConstraints.validatePendingChange(job);
			if (status == null)
				return job.getFeature();
		}
		// no valid hits
		return null;
	}

	public void initialize() {
		candidates = new ArrayList();
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration config = localSite.getCurrentConfiguration();
			IConfiguredSite[] isites = config.getConfiguredSites();
			for (int i = 0; i < isites.length; i++) {
				IFeatureReference[] refs = isites[i].getConfiguredFeatures();
				for (int j = 0; j < refs.length; j++) {
					IFeatureReference ref = refs[j];
					candidates.add(ref.getFeature());
				}
			}
			filterIncludedFeatures(candidates);

		} catch (CoreException e) {
			UpdateUIPlugin.logException(e, false);
		}
	}

	private void filterIncludedFeatures(ArrayList candidates)
		throws CoreException {
		IFeature[] array =
			(IFeature[]) candidates.toArray(new IFeature[candidates.size()]);
		// filter out included features so that only top-level features remain on the list
		for (int i = 0; i < array.length; i++) {
			IFeature feature = array[i];
			IFeatureReference[] included =
				feature.getIncludedFeatureReferences();
			for (int j = 0; j < included.length; j++) {
				IFeatureReference fref = included[j];
				IFeature ifeature = fref.getFeature();
				int index = candidates.indexOf(ifeature);
				if (index != -1)
					candidates.remove(index);
			}
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

	private boolean isNewerVersion(
		VersionedIdentifier fvi,
		VersionedIdentifier cvi) {
		if (!fvi.getIdentifier().equals(cvi.getIdentifier()))
			return false;
		PluginVersionIdentifier fv = fvi.getVersion();
		PluginVersionIdentifier cv = cvi.getVersion();
		String mode = MainPreferencePage.getUpdateVersionsMode();
		boolean greater = cv.isGreaterThan(fv);
		if (!greater)
			return false;
		if (mode.equals(MainPreferencePage.EQUIVALENT_VALUE))
			return cv.isEquivalentTo(fv);
		else if (mode.equals(MainPreferencePage.COMPATIBLE_VALUE))
			return cv.isCompatibleWith(fv);
		else
			return false;
	}

	public void createControl(Composite parent, FormWidgetFactory factory) {
		Composite container = factory.createComposite(parent);
		setControl(container);
	}
	public void load(Map map, boolean editable) {
	}
	public void store(Map map) {
	}
}