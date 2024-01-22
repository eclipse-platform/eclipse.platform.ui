/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import java.util.Objects;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

/**
 * Abstract base implementation of all workbench views.
 * <p>
 * This class should be subclassed by clients wishing to define new views. The
 * name of the subclass should be given as the <code>"class"</code> attribute in
 * a <code>view</code> extension contributed to the workbench's view extension
 * point (named <code>"org.eclipse.ui.views"</code>). For example, the plug-in's
 * XML markup might contain:
 * </p>
 *
 * <pre>
 * &lt;extension point="org.eclipse.ui.views"&gt;
 *      &lt;view id="com.example.myplugin.view"
 *         name="My View"
 *         class="com.example.myplugin.MyView"
 *         icon="images/eview.gif"
 *      /&gt;
 * &lt;/extension&gt;
 * </pre>
 * <p>
 * where <code>com.example.myplugin.MyView</code> is the name of the
 * <code>ViewPart</code> subclass.
 * </p>
 * <p>
 * Subclasses must implement the following methods:
 * </p>
 * <ul>
 * <li><code>createPartControl</code> - to create the view's controls</li>
 * <li><code>setFocus</code> - to accept focus</li>
 * </ul>
 * <p>
 * Subclasses may extend or reimplement the following methods as required:
 * </p>
 * <ul>
 * <li><code>setInitializationData</code> - extend to provide additional
 * initialization when view extension is instantiated</li>
 * <li><code>init(IWorkbenchPartSite)</code> - extend to provide additional
 * initialization when view is assigned its site</li>
 * <li><code>dispose</code> - extend to provide additional cleanup</li>
 * <li><code>getAdapter</code> - reimplement to make their view adaptable</li>
 * </ul>
 */
public abstract class ViewPart extends WorkbenchPart implements IViewPart {

	/**
	 * Listens to PROP_TITLE property changes in this object until the first call to
	 * setContentDescription. Used for compatibility with old parts that call
	 * setTitle or overload getTitle instead of using setContentDescription.
	 */
	private IPropertyListener compatibilityTitleListener = (source, propId) -> {
		if (propId == IWorkbenchPartConstants.PROP_TITLE) {
			setDefaultContentDescription();
		}
	};

	/**
	 * Creates a new view.
	 */
	protected ViewPart() {
		super();

		addPropertyListener(compatibilityTitleListener);
	}

	@Override
	public IViewSite getViewSite() {
		return (IViewSite) getSite();
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		setSite(site);

		setDefaultContentDescription();
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		/*
		 * Initializes this view with the given view site. A memento is passed to the
		 * view which contains a snapshot of the views state from a previous session.
		 * Where possible, the view should try to recreate that state within the part
		 * controls. <p> This implementation will ignore the memento and initialize the
		 * view in a fresh state. Subclasses may override the implementation to perform
		 * any state restoration as needed.
		 */
		init(site);
	}

	@Override
	public void saveState(IMemento memento) {
		// do nothing
	}

	@Override
	protected void setPartName(String partName) {
		if (compatibilityTitleListener != null) {
			removePropertyListener(compatibilityTitleListener);
			compatibilityTitleListener = null;
		}

		super.setPartName(partName);
	}

	@Override
	protected void setContentDescription(String description) {
		if (compatibilityTitleListener != null) {
			removePropertyListener(compatibilityTitleListener);
			compatibilityTitleListener = null;
		}

		super.setContentDescription(description);
	}

	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		super.setInitializationData(cfig, propertyName, data);

		setDefaultContentDescription();
	}

	private void setDefaultContentDescription() {
		if (compatibilityTitleListener == null) {
			return;
		}

		String partName = getPartName();
		String title = getTitle();

		if (Objects.equals(partName, title)) {
			internalSetContentDescription(""); //$NON-NLS-1$
		} else {
			internalSetContentDescription(title);
		}
	}

	@Override
	protected final void checkSite(IWorkbenchPartSite site) {
		super.checkSite(site);
		Assert.isTrue(site instanceof IViewSite, "The site for a view must be an IViewSite"); //$NON-NLS-1$
	}
}
