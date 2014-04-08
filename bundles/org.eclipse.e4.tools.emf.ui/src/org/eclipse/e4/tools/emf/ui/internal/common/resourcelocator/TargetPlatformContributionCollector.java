/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 424730
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.tools.emf.ui.common.FilterEx;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.ContributionData;
import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider;
import org.eclipse.e4.tools.emf.ui.common.ResourceSearchScope;
import org.eclipse.e4.tools.emf.ui.internal.common.ClassContributionCollector;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.FilteredContributionDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.TargetPlatformHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * A contribution collector encompassing the current target platform.<br />
 * Uses FilterEx for bundle, package, and location filtering.<br />
 * This implementation finds resources based on file names, not by parsing file
 * contents.
 *
 * @author Steven Spungin
 *
 */
public abstract class TargetPlatformContributionCollector extends ClassContributionCollector {

	ArrayList<Entry> cacheEntry = new ArrayList<Entry>();
	HashSet<String> cacheBundleId = new HashSet<String>();
	HashSet<String> cachePackage = new HashSet<String>();
	HashSet<String> cacheLocation = new HashSet<String>();
	private Pattern patternFile;
	protected String cacheName;

	static class Entry {
		String name;
		String path;
		String installLocation;
		String relativePath;
		String bundleSymName;
		String pakage;
	}

	@SuppressWarnings("unused")
	private TargetPlatformContributionCollector() {
	}

	protected TargetPlatformContributionCollector(String cacheName) {

		this.cacheName = cacheName;
		patternFile = getFilePattern();

		addContributor(new IClassContributionProvider() {

			@Override
			public void findContribution(Filter filter, ContributionResultHandler handler) {

				Pattern patternName = Pattern.compile(filter.namePattern, Pattern.CASE_INSENSITIVE);

				reloadCache(false);

				int maxResults = filter.maxResults;
				if (maxResults == 0) {
					maxResults = 100;
				}

				int found = 0;
				boolean more = false;
				for (Entry e : cacheEntry) {
					// Check for FilterEx filters
					if (filter instanceof FilterEx) {
						FilterEx filterEx = (FilterEx) filter;
						if (E.notEmpty(filterEx.getBundles())) {
							if (!filterEx.getBundles().contains(e.bundleSymName)) {
								continue;
							}
						}
						if (E.notEmpty(filterEx.getPackages())) {
							if (!filterEx.getPackages().contains(e.pakage)) {
								continue;
							}
						}
						if (E.notEmpty(filterEx.getLocations())) {
							boolean locationFound = false;
							for (String location : filterEx.getLocations()) {
								if (e.installLocation.startsWith(location)) {
									locationFound = true;
									break;
								}
							}
							if (!locationFound) {
								continue;
							}
						}
						if (filterEx.isIncludeNonBundles() == false) {
							if (e.bundleSymName == null) {
								continue;
							}
						}
						if (filterEx.getSearchScope().contains(ResourceSearchScope.WORKSPACE)) {
							if (filter.project != null) {
								IWorkspace workspace = filter.project.getWorkspace();
								boolean fnd = false;
								for (IProject project : workspace.getRoot().getProjects()) {
									// String path =
									// project.getLocationURI().getPath();
									String path = project.getName();
									if (e.installLocation.contains(path)) {
										fnd = true;
										break;
									}
								}
								if (!fnd) {
									continue;
								}
							}
						}
					}
					Matcher m = patternName.matcher(e.name);
					if (m.find()) {
						found++;
						if (found > maxResults) {
							more = true;
							handler.moreResults(-1, filter);
							break;
						} else {
							handler.result(makeData(e));
						}
					}

				}
				if (!more) {
					handler.moreResults(0, filter);
				}
			}
		});

		addModelElementContributor(new IModelElementProvider() {

			@Override
			public void getModelElements(Filter filter, ModelResultHandler handler) {
				// TODO Auto-generated method stub
			}

			@Override
			public void clearCache() {
				cacheEntry.clear();
				cacheBundleId.clear();
				cachePackage.clear();
				cacheLocation.clear();
				outputDirectories.clear();
			}
		});
	}

	protected ContributionData makeData(Entry e) {
		// If class is in a java project, strip the source directory
		// String path = e.path;// .replace("/", ".") + e.name;
		// path = stripSourceDirectory(path, e.installLocation);
		IPath ip = Path.fromOSString(e.path);
		ip = ip.addTrailingSeparator().makeRelative();
		ip = ip.append(e.name);
		String className = ip.toOSString().replace('/', '.');
		ContributionData data = new ContributionData(e.bundleSymName, className, "Java", e.installLocation); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		data.installLocation = e.installLocation;
		data.resourceRelativePath = e.relativePath;
		return data;
	}

	/**
	 *
	 * @return A copy of the bundle IDs in the cache.
	 */
	public Collection<String> getBundleIds() {
		reloadCache(false);
		return new ArrayList<String>(cacheBundleId);
	}

	/**
	 *
	 * @return A copy of the bundle IDs in the cache.
	 */
	public Collection<String> getPackages() {
		reloadCache(false);
		return new ArrayList<String>(cachePackage);
	}

	/**
	 *
	 * @return A copy of the bundle IDs in the cache.
	 */
	public Collection<String> getLocations() {
		reloadCache(false);
		return new ArrayList<String>(cacheLocation);
	}

	/**
	 * Ensures the cache is loaded. By default it is loaded on first access, and
	 * kept static until forced to reloaded.
	 *
	 * @param force
	 *            true to force reload the cache
	 */
	private void reloadCache(boolean force) {
		if (cacheEntry.isEmpty() || force) {
			cacheEntry.clear();
			cacheBundleId.clear();
			cachePackage.clear();
			cacheLocation.clear();
			outputDirectories.clear();

			ProgressMonitorDialog dlg = new ProgressMonitorDialog(Display.getDefault().getActiveShell()) {

				@Override
				protected Control createContents(Composite parent) {
					// TODO odd this is not a bean.
					Composite ret = (Composite) super.createContents(parent);
					Label label = new Label(ret, SWT.NONE);
					label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
					label.setText(Messages.TargetPlatformContributionCollector_pleaseWait);

					return ret;
				}
			};
			try {
				dlg.run(true, true, new IRunnableWithProgress() {

					@Override
					public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

						// load workspace projects
						for (final IProject pj : PDECore.getWorkspace().getRoot().getProjects()) {
							if (monitor.isCanceled()) {
								break;
							}
							String rootDirectory = pj.getLocation().toOSString();
							TargetPlatformContributionCollector.this.visit(monitor, FilteredContributionDialog.getBundle(rootDirectory), rootDirectory, new File(rootDirectory));
						}

						// load target platform bundles
						IPluginModelBase[] models = TargetPlatformHelper.getPDEState().getTargetModels();
						monitor.beginTask(Messages.TargetPlatformContributionCollector_updatingTargetPlatformCache + cacheName + ")", models.length); //$NON-NLS-2$
						for (IPluginModelBase pluginModelBase : models) {
							monitor.subTask(pluginModelBase.getPluginBase().getId());
							monitor.worked(1);
							if (monitor.isCanceled()) {
								break;
							}

							IPluginBase pluginBase = pluginModelBase.getPluginBase();
							if (pluginBase == null) {
								// bundle = getBundle(new File())
								continue;
							}
							URL url;
							try {
								String installLocation = pluginModelBase.getInstallLocation();
								if (installLocation.endsWith(".jar")) { //$NON-NLS-1$
									url = new URL("file://" + installLocation); //$NON-NLS-1$
									ZipInputStream zis = new ZipInputStream(url.openStream());
									while (true) {
										ZipEntry entry = zis.getNextEntry();
										if (entry == null) {
											break;
										} else {
											String name2 = entry.getName();
											if (shouldIgnore(name2)) {
												continue;
											}
											Matcher m = patternFile.matcher(name2);
											if (m.matches()) {
												Entry e = new Entry();
												e.installLocation = installLocation;
												cacheLocation.add(installLocation);
												e.name = m.group(2);
												e.path = m.group(1);
												if (e.path != null) {
													e.pakage = e.path.replace("/", "."); //$NON-NLS-1$ //$NON-NLS-2$
													if (e.pakage.startsWith(".")) { //$NON-NLS-1$
														e.pakage = e.pakage.substring(1);
													}
													if (e.pakage.endsWith(".")) { //$NON-NLS-1$
														e.pakage = e.pakage.substring(0, e.pakage.length() - 1);
													}
												} else {
													e.pakage = ""; //$NON-NLS-1$
												}
												cachePackage.add(e.pakage);

												e.bundleSymName = pluginBase.getId();
												if (e.path == null) {
													e.path = ""; //$NON-NLS-1$
												}
												cacheEntry.add(e);
												cacheBundleId.add(pluginBase.getId());

												//
												// System.out.println(group
												// + " -> "
												// +
												// m.group(2));
											}
										}
									}
								} else {
									// not a jar file
									String bundle = getBundle(new File(installLocation));
									if (bundle != null) {
										visit(monitor, bundle, installLocation, new File(installLocation));
									}
								}
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						monitor.done();
					}
				});
			} catch (InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	// @Refactor
	static public String getBundle(File file) {
		if (file.isDirectory() == false) {
			return null;
		}

		File f = new File(file, "META-INF/MANIFEST.MF"); //$NON-NLS-1$

		if (f.exists() && f.isFile()) {
			BufferedReader r = null;
			try {
				InputStream s = new FileInputStream(f);
				r = new BufferedReader(new InputStreamReader(s));
				String line;
				while ((line = r.readLine()) != null) {
					if (line.startsWith("Bundle-SymbolicName:")) { //$NON-NLS-1$
						int start = line.indexOf(':');
						int end = line.indexOf(';');
						if (end == -1) {
							end = line.length();
						}
						return line.substring(start + 1, end).trim();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (r != null) {
					try {
						r.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return null;
	}

	protected void visit(IProgressMonitor monitor, String bundleName, String installLocation, File file) {
		for (File fChild : file.listFiles()) {
			if (fChild.isDirectory()) {
				visit(monitor, bundleName, installLocation, fChild);
			} else {
				String name2 = fChild.getAbsolutePath().substring(installLocation.length() + 1);
				name2 = stripOutputDirectory(name2, installLocation);
				if (shouldIgnore(name2)) {
					continue;
				}
				Matcher m = patternFile.matcher(name2);
				if (m.matches()) {
					Entry e = new Entry();
					e.installLocation = installLocation;
					cacheLocation.add(installLocation);
					e.name = m.group(2);
					if (e.name.contains("$")) { //$NON-NLS-1$
						continue;
					}
					e.path = m.group(1);
					if (e.path != null) {
						e.pakage = e.path.replace("/", "."); //$NON-NLS-1$ //$NON-NLS-2$
						if (e.pakage.startsWith(".")) { //$NON-NLS-1$
							e.pakage = e.pakage.substring(1);
						}
						if (e.pakage.endsWith(".")) { //$NON-NLS-1$
							e.pakage = e.pakage.substring(0, e.pakage.length() - 1);
						}
					} else {
						e.pakage = ""; //$NON-NLS-1$
					}
					if (e.path == null) {
						e.path = ""; //$NON-NLS-1$
					}
					e.relativePath = Path.fromOSString(file.getAbsolutePath().replace(e.installLocation, "")).makeRelative().toOSString(); //$NON-NLS-1$

					e.bundleSymName = bundleName;
					// TODO we need project to strip source paths.
					// e.pakage = e.pakage.replaceAll("^bin.", "");
					cachePackage.add(e.pakage);
					cacheEntry.add(e);
					if (bundleName != null) {
						cacheBundleId.add(bundleName);
					}
				}
			}
		}

	}

	static private String stripOutputDirectory(String path, String installLocation) {
		if (installLocation.matches(".*\\.jar")) { //$NON-NLS-1$
			return path;
		}
		for (String sourceDirectory : getOutputDirectories(installLocation)) {
			if (path.startsWith(sourceDirectory)) {
				path = path.substring(sourceDirectory.length());
				break;
			}
		}
		return path;
	}

	/**
	 * A cache of the output directories for install locations (if install
	 * location has a classpath file with appropriate output entries)
	 */
	static private HashMap<String, List<String>> outputDirectories = new HashMap<String, List<String>>();

	// Returns the Eclipse output directories for an install location. The
	// directories are relative to the install location.
	// <classpathentry kind="output" path="bin"/>
	static private List<String> getOutputDirectories(String installLocation) {
		List<String> ret = outputDirectories.get(installLocation);
		if (ret == null) {
			ret = new ArrayList<String>();
			outputDirectories.put(installLocation, ret);
			try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(installLocation + File.separator + ".classpath")); //$NON-NLS-1$
				XPath xp = XPathFactory.newInstance().newXPath();
				NodeList list = (NodeList) xp.evaluate("//classpathentry[@kind='output']/@path", doc, XPathConstants.NODESET); //$NON-NLS-1$
				for (int i = 0; i < list.getLength(); i++) {
					String value = list.item(i).getNodeValue();
					ret.add(value);
				}
			} catch (Exception e) {
			}
		}
		return ret;
	}

	protected boolean shouldIgnore(String name) {
		return false;
	}

	protected abstract Pattern getFilePattern();

}
