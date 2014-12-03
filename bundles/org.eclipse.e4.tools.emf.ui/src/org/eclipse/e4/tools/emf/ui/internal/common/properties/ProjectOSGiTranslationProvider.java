package org.eclipse.e4.tools.emf.ui.internal.common.properties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.tools.services.impl.ResourceBundleHelper;
import org.eclipse.e4.tools.services.impl.ResourceBundleTranslationProvider;
import org.osgi.framework.Constants;

public class ProjectOSGiTranslationProvider extends ResourceBundleTranslationProvider {

	public static final String META_INF_DIRECTORY_NAME = "META-INF"; //$NON-NLS-1$
	public static final String MANIFEST_DEFAULT_PATH = "META-INF/MANIFEST.MF"; //$NON-NLS-1$

	/**
	 * The {@link IProject} this translation provider is connected to
	 */
	private IProject project;
	/**
	 * The manifest header identifying the base name of the bundle's
	 * localization entries.
	 */
	private String basename;
	/**
	 * The Locale to use for translations.
	 */
	private Locale locale;

	/**
	 * @param project
	 *            The {@link IProject} this translation provider should be
	 *            connected to.
	 * @param locale
	 *            The initial {@link Locale} for which this translation provider
	 *            should be created.
	 */
	// TODO change parameter to Locale instead of String once we break e4 tools
	// compatibility with Luna
	public ProjectOSGiTranslationProvider(IProject project, String locale) {
		// create the translation provider with no initial ResourceBundle as we
		// need to calculate it first
		super(null);

		this.project = project;
		this.project.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {

			@Override
			public void resourceChanged(IResourceChangeEvent event) {
				if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
					try {
						event.getDelta().accept(new IResourceDeltaVisitor() {

							@Override
							public boolean visit(IResourceDelta delta) throws CoreException {
								return ProjectOSGiTranslationProvider.this.visit(delta);
							}
						});
					} catch (final CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		setLocale(locale, false);

		final IFile f = this.project.getFile(MANIFEST_DEFAULT_PATH);
		if (f.exists()) {
			handleManifestChange(f);
		} else {
			basename = Constants.BUNDLE_LOCALIZATION_DEFAULT_BASENAME;
		}
	}

	// TODO remove once we break e4 tools
	// compatibility with Luna
	@Inject
	void setLocale(@Named(TranslationService.LOCALE) String locale, @Optional Boolean performUpdate) {
		try {
			this.locale = locale == null ? Locale.getDefault() : ResourceBundleHelper.toLocale(locale);
		} catch (final Exception e) {
			this.locale = Locale.getDefault();
		}

		if (performUpdate == null || performUpdate) {
			updateResourceBundle();
		}
	}

	@Inject
	void setLocale(@Named(TranslationService.LOCALE) Locale locale, @Optional Boolean performUpdate) {
		this.locale = locale == null ? Locale.getDefault() : locale;

		if (performUpdate == null || performUpdate) {
			updateResourceBundle();
		}
	}

	/**
	 *
	 * @param delta
	 *            The resource delta that represents the changes in the state of
	 *            a resource tree between two discrete points in time.
	 * @return <code>true</code> if the resource delta's children should be
	 *         visited; <code>false</code> if they should be skipped.
	 */
	boolean visit(IResourceDelta delta) {
		if (delta.getResource() instanceof IWorkspaceRoot) {
			return true;
		} else if (delta.getResource().equals(project)) {
			return true;
		} else if (delta.getResource().getProjectRelativePath().toString().equals(META_INF_DIRECTORY_NAME)) {
			return true;
		} else if (delta.getResource().getProjectRelativePath().toString().equals(MANIFEST_DEFAULT_PATH)) {
			handleManifestChange((IFile) delta.getResource());
			return false;
		} else if (delta.getResource() instanceof IFile) {
			final String filename = ((IFile) delta.getResource()).getName();
			// extract base bundle name out of local basename
			final String fileBaseName = basename.substring(basename.lastIndexOf("/") + 1, basename.length()); //$NON-NLS-1$
			if (filename.startsWith(fileBaseName)) {
				updateResourceBundle();
				return false;
			}
		}

		if (delta.getResource().getProjectRelativePath().toString().equals(basename)) {
			updateResourceBundle();
			return false;
		}

		final String[] p = basename.split("/"); //$NON-NLS-1$
		int i = 0;
		String path = ""; //$NON-NLS-1$
		do {
			path += p[i];
			if (delta.getResource().getProjectRelativePath().toString().equals(path)) {
				return true;
			}
			path += "/"; //$NON-NLS-1$
		} while (++i < p.length);

		return false;
	}

	/**
	 * Will check if the manifest header identifying the base name of the
	 * bundle's localization entries has changed and if so it will update the
	 * underlying {@link ResourceBundle} and clear the caches.
	 *
	 * @param file
	 *            The reference to the manifest file of the current project.
	 */
	private void handleManifestChange(IFile file) {
		try {
			final String newValue = extractBasenameFromManifest(file);

			if (!newValue.equals(basename)) {
				basename = newValue;
				if (basename != null) {
					updateResourceBundle();
				}
			}

		} catch (final CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Extracts the manifest header identifying the base name of the bundle's
	 * localization entries.
	 *
	 * @param file
	 *            The reference to the manifest file of the current project.
	 * @return The manifest header identifying the base name of the bundle's
	 *         localization entries.
	 * @throws CoreException
	 *             If loading the contents of the given {@link IFile} fails
	 * @throws IOException
	 *             If reading out of the given file fails.
	 *
	 * @see IFile#getContents()
	 */
	public static String extractBasenameFromManifest(IFile file) throws CoreException, IOException {
		final InputStream in = file.getContents();
		final BufferedReader r = new BufferedReader(new InputStreamReader(in));
		String line;
		String newValue = Constants.BUNDLE_LOCALIZATION_DEFAULT_BASENAME;
		while ((line = r.readLine()) != null) {
			if (line.startsWith(Constants.BUNDLE_LOCALIZATION)) {
				newValue = line.substring(Constants.BUNDLE_LOCALIZATION.length() + 1).trim();
				break;
			}
		}

		r.close();
		return newValue;
	}

	/**
	 * Reloads the underlying ResourceBundle.
	 */
	protected void updateResourceBundle() {
		setResourceBundle(ResourceBundleHelper.getEquinoxResourceBundle(basename, locale,
			new ProjectResourceBundleControl(true), new ProjectResourceBundleControl(false)));
	}

	/**
	 * Specialization of {@link Control} which loads the {@link ResourceBundle} by using file structures of a project
	 * instead of using a classloader.
	 *
	 * @author Dirk Fauth
	 */
	class ProjectResourceBundleControl extends ResourceBundle.Control {

		/**
		 * Flag to determine whether the default locale should be used as
		 * fallback locale in case there is no {@link ResourceBundle} found for
		 * the specified locale.
		 */
		private final boolean useFallback;

		/**
		 * @param useFallback
		 *            <code>true</code> if the default locale should be used as
		 *            fallback locale in the search path or <code>false</code> if there should be no fallback.
		 */
		ProjectResourceBundleControl(boolean useFallback) {
			this.useFallback = useFallback;
		}

		@Override
		public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
			boolean reload) throws IllegalAccessException, InstantiationException, IOException {

			final String bundleName = toBundleName(baseName, locale);
			ResourceBundle bundle = null;
			if (format.equals("java.properties")) { //$NON-NLS-1$
				final String resourceName = toResourceName(bundleName, "properties"); //$NON-NLS-1$
				InputStream stream = null;
				try {
					stream = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
						@Override
						public InputStream run() throws IOException {
							return getResourceAsStream(resourceName);
						}
					});
				} catch (final PrivilegedActionException e) {
					throw (IOException) e.getException();
				}
				if (stream != null) {
					try {
						bundle = new PropertyResourceBundle(stream);
					} finally {
						stream.close();
					}
				}
			} else {
				throw new IllegalArgumentException("unknown format: " + format); //$NON-NLS-1$
			}
			return bundle;
		}

		/**
		 * Loads the properties file by using the {@link IProject} of the {@link ProjectOSGiTranslationProvider}.
		 *
		 * @param name
		 * @return The {@link InputStream} to the properties file to load
		 */
		protected InputStream getResourceAsStream(String name) {
			final IFile f = project.getFile(name);
			try {
				if (f.exists()) {
					return f.getContents();
				}
				return null;
			} catch (final CoreException e) {
				return null;
			}
		}

		@Override
		public List<String> getFormats(String baseName) {
			return FORMAT_PROPERTIES;
		}

		@Override
		public Locale getFallbackLocale(String baseName, Locale locale) {
			return useFallback ? super.getFallbackLocale(baseName, locale) : null;
		}

		// this implementation simply doesn't cache the values in the
		// ResourceBundle. If we recognize performance issues in the
		// Application Model Editor because of this we should consider
		// returning 0 here and overriding needsReload() with the information
		// which bundle needs to be reloaded
		@Override
		public long getTimeToLive(String baseName, Locale locale) {
			return TTL_DONT_CACHE;
		}
	}
}
