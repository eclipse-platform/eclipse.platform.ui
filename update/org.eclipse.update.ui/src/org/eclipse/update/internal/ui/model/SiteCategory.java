/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.model;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;

public class SiteCategory extends UIModelObject {

	Vector children;
	private ICategory category;
	private String name;
	private boolean touched;
	private int featureCount;
	private boolean canceled;
	private SiteBookmark bookmark;

	class OtherCategory implements ICategory {
		IURLEntry entry;
		public OtherCategory() {
			entry = new IURLEntry() {
				public String getAnnotation() {
					return UpdateUI.getString("SiteCategory.other.description"); //$NON-NLS-1$
				}
				public URL getURL() {
					return null;
				}
				public Object getAdapter(Class clazz) {
					return null;
				}
				public int getType() {
					return IURLEntry.UPDATE_SITE;
				}
			};
		}
		public String getName() {
			return SiteCategory.this.getName();
		}
		public String getLabel() {
			return SiteCategory.this.getName();
		}
		public IURLEntry getDescription() {
			return entry;
		}
		public Object getAdapter(Class clazz) {
			return null;
		}
	}

	public SiteCategory(SiteBookmark bookmark, String name, ICategory category) {
		this.bookmark = bookmark;
		if (category == null) {
			this.name = UpdateUI.getString("SiteCategory.other.label"); //$NON-NLS-1$
			this.category = new OtherCategory();
		} else {
			this.name = name;
			this.category = category;
		}
		children = new Vector();
	}
	
	public SiteBookmark getBookmark() {
		return bookmark;
	}

	public boolean isOtherCategory() {
		return category instanceof OtherCategory;
	}

	public Object[] getChildren() {
		return canceled ? new Object[0] : children.toArray();
	}

	public int getChildCount() {
		return canceled ? 0 : children.size();
	}

	public String getName() {
		return name;
	}
	public String getFullName() {
		return category.getName();
	}

	public String toString() {
		return category.getLabel();
	}

	public ICategory getCategory() {
		return category;
	}

	void add(Object child) {
		if (child instanceof IFeatureAdapter)
			featureCount++;
		children.add(child);
	}

	public void touchFeatures(IRunnableContext context) {
		if (children.size() == 0 || touched || featureCount == 0)
			return;

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				monitor.beginTask(
					UpdateUI.getString("SiteBookmark.downloading"), //$NON-NLS-1$
					featureCount);
				for (int i = 0; i < children.size(); i++) {
					Object child = children.get(i);
					if (monitor.isCanceled())
						break;
					if (child instanceof IFeatureAdapter) {
						IFeatureAdapter adapter = (IFeatureAdapter) child;
						monitor.subTask(adapter.getFastLabel());
						try {
							adapter.getFeature(
								new SubProgressMonitor(monitor, 1));
						} catch (CoreException e) {
						}
					}
				}
				monitor.done();
			}
		};

		try {
			context.run(true, true, op);
			touched = true;
		} catch (InterruptedException e) {
			canceled = true;
		} catch (InvocationTargetException e) {
		}
	}

	public void addFeaturesTo(Vector flatList) {
		for (int i = 0; i < children.size(); i++) {
			Object child = children.get(i);
			if (child instanceof FeatureReferenceAdapter) {
				FeatureReferenceAdapter cfeature =
					(FeatureReferenceAdapter) child;
				// Don't add duplicates - there may be the same
				// feature present in several categories
				if (findFeature(flatList, cfeature.getFeatureReference())
					== null) {
					flatList.add(child);
				}
			} else if (child instanceof SiteCategory) {
				((SiteCategory) child).addFeaturesTo(flatList);
			}
		}
	}

	private FeatureReferenceAdapter findFeature(
		Vector flatList,
		IFeatureReference featureRef) {
		for (int i = 0; i < flatList.size(); i++) {
			FeatureReferenceAdapter cfeature =
				(FeatureReferenceAdapter) flatList.get(i);
			if (cfeature.getFeatureReference().equals(featureRef))
				return cfeature;
		}
		return null;
	}
}
