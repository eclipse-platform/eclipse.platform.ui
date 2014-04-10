/*******************************************************************************
 * Copyright (c) 2009, 2014 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - initial API and implementation
 *     Brian de Alwis (MTI) - Performance tweaks (Bug 430829)
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom;

import java.util.ArrayList;
import java.util.Collections;
import org.eclipse.e4.ui.css.core.dom.ArrayNodeList;
import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.dom.ChildVisibilityAwareElement;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.internal.css.swt.ICTabRendering;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link CSSStylableElement} implementation which wrap SWT {@link CTabFolder}.
 *
 */
public class CTabFolderElement extends CompositeElement implements ChildVisibilityAwareElement {
	private final static String BACKGROUND_SET_BY_TAB_RENDERER = "bgSetByTabRenderer"; //$NON-NLS-1$

	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			applyStyles(getWidget(), true);
		}

	};

	public CTabFolderElement(CTabFolder tabFolder, CSSEngine engine) {
		super(tabFolder, engine);
	}

	@Override
	public void initialize() {
		super.initialize();
		((CTabFolder) getControl()).addSelectionListener(selectionListener);
	}

	@Override
	public void dispose() {
		CTabFolder ctf = (CTabFolder) getControl();
		if (ctf != null && !ctf.isDisposed()) {
			ctf.removeSelectionListener(selectionListener);
		}
		super.dispose();
	}

	/**
	 * Compute static pseudo instances.
	 *
	 */
	@Override
	protected void computeStaticPseudoInstances() {
		super.computeStaticPseudoInstances();
		// it's CTabFolder. Set selected as static pseudo instance.
		// because this widget define methods
		// CTabFolder#setSelectionBackground (Color color)
		// which set background Color when a CTabItem is selected.
		super.addStaticPseudoInstance("selected");

	}

	@Override
	public Node item(int index) {
		Widget widget = getWidget();
		// retrieve the child control or child item depending on the
		// index
		CTabFolder folder = (CTabFolder) widget;
		int length = folder.getChildren().length;
		if (index >= length) {
			Widget w = folder.getItem(index - length);
			return getElement(w);
		} else {
			Widget w = folder.getChildren()[index];
			return getElement(w);
		}

	}

	@Override
	public int getLength() {
		Widget widget = getWidget();
		int childCount = 0;
		if (widget instanceof Composite) {
			childCount = ((Composite) widget).getChildren().length;

			if (widget instanceof CTabFolder) {
				// if it's a CTabFolder, include the child items in the count
				childCount += ((CTabFolder) widget).getItemCount();
			}
		}
		return childCount;
	}

	@Override
	public void reset() {
		CTabFolder folder = (CTabFolder) getWidget();
		folder.setSelectionBackground((Color) null);
		folder.setSelectionForeground((Color) null);
		folder.setSelectionBackground((Image) null);

		folder.setBackground(null, null);
		resetChildrenBackground(folder);

		if (folder.getRenderer() instanceof ICTabRendering) {
			ICTabRendering renderer = (ICTabRendering) folder
					.getRenderer();
			folder.setRenderer(null);
			renderer.setSelectedTabFill(null);
			renderer.setTabOutline(null);
			renderer.setInnerKeyline(null);
			renderer.setOuterKeyline(null);
			renderer.setShadowColor(null);
		}
		super.reset();
	}

	private void resetChildrenBackground(Composite composite) {
		for (Control control : composite.getChildren()) {
			resetChildBackground(control);
			if (control instanceof Composite) {
				resetChildrenBackground((Composite) control);
			}
		}
	}

	private void resetChildBackground(Control control) {
		Color backgroundSetByRenderer = (Color) control
				.getData(BACKGROUND_SET_BY_TAB_RENDERER);
		if (backgroundSetByRenderer != null) {
			if (control.getBackground() == backgroundSetByRenderer) {
				control.setBackground(null);
			}
			control.setData(BACKGROUND_SET_BY_TAB_RENDERER, null);
		}
	}

	public static void setBackgroundOverriddenDuringRenderering(
			Composite composite, Color background) {
		composite.setBackground(background);
		composite.setData(BACKGROUND_SET_BY_TAB_RENDERER, background);

		for (Control control : composite.getChildren()) {
			if (!CompositeElement.hasBackgroundOverriddenByCSS(control)) {
				control.setBackground(background);
				control.setData(BACKGROUND_SET_BY_TAB_RENDERER, background);
			}
		}
	}

	@Override
	public NodeList getVisibleChildNodes() {
		// CTabFolder#getChildren() exposes the "tab controls" (the toolbars and
		// the top-right area), as well as the composites used to host the
		// CTabItem contents. We need to expose both the CTabItems but
		// just the composite of the active CTabItem
		CTabFolder folder = (CTabFolder) getWidget();
		ArrayList<Widget> visible = new ArrayList<Widget>();

		if (folder.getTopRight() != null) {
			visible.add(folder.getTopRight());
		}
		Collections.addAll(visible, folder.getItems());
		int selected = folder.getSelectionIndex();
		// if (selected < 0 && folder.getItemCount() > 0) {
		// selected = 0;
		// }
		if (selected >= 0) {
			CTabItem item = folder.getItem(selected);
			// If item.getControl() is not yet set, we pretend it doesn't exist
			if (item.getControl() != null) {
				visible.add(item.getControl());
			}
		}
		return new ArrayNodeList(visible, engine);
	}
}

