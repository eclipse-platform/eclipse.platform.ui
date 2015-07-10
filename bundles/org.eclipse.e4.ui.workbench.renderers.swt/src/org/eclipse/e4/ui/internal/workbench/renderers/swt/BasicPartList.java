/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.internal.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class BasicPartList extends AbstractTableInformationControl {

	private class BasicStackListLabelProvider extends ColumnLabelProvider {

		private Font boldFont;

		public BasicStackListLabelProvider() {
			Font font = Display.getDefault().getSystemFont();
			FontData[] fontDatas = font.getFontData();
			for (FontData fontData : fontDatas) {
				fontData.setStyle(fontData.getStyle() | SWT.BOLD);
			}
			boldFont = new Font(Display.getDefault(), fontDatas);
		}

		@Override
		public Font getFont(Object element) {
			if (element instanceof MPart) {
				MPart part = (MPart) element;
				CTabItem item = renderer.findItemForPart(part);
				if (item != null && !item.isShowing()) {
					return boldFont;
				}
			}
			return super.getFont(element);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof MDirtyable
					&& ((MDirtyable) element).isDirty()) {
				return "*" + ((MUILabel) element).getLocalizedLabel(); //$NON-NLS-1$
			}
			return ((MUILabel) element).getLocalizedLabel();
		}

		@Override
		public Image getImage(Object element) {
			return renderer.getImage((MUILabel) element);
		}

		@Override
		public String getToolTipText(Object element) {
			return renderer.getToolTip((MUILabel) element);
		}

		@Override
		public boolean useNativeToolTip(Object object) {
			return true;
		}

		@Override
		public void dispose() {
			boldFont.dispose();
		}
	}

	private MElementContainer<?> input;

	private EPartService partService;

	private StackRenderer renderer;

	// private ISaveHandler saveHandler;

	public BasicPartList(Shell parent, int shellStyle, int treeStyler,
			EPartService partService, MElementContainer<?> input,
			StackRenderer renderer, boolean mru) {
		super(parent, shellStyle, treeStyler);
		this.partService = partService;
		this.input = input;
		this.renderer = renderer;
		// this.saveHandler = saveHandler;
		if (mru && getTableViewer() != null) {
			getTableViewer().setComparator(new ViewerComparator() {
				@Override
				public int category(Object element) {
					if (element instanceof MPart) {
						MPart part = (MPart) element;
						CTabItem item = BasicPartList.this.renderer.findItemForPart(part);
						if (item != null && !item.isShowing()) {
							return -1;
						}
					}
					return 0;
				}
			});
		}
	}

	@Override
	protected TableViewer createTableViewer(Composite parent, int style) {
		Table table = new Table(parent, SWT.SINGLE | (style & ~SWT.MULTI));
		table.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false,
				false));
		TableViewer tableViewer = new TableViewer(table);
		tableViewer.addFilter(new NamePatternFilter());
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setLabelProvider(new BasicStackListLabelProvider());

		ColumnViewerToolTipSupport.enableFor(tableViewer);
		return tableViewer;
	}

	private List<Object> getInput() {
		List<Object> list = new ArrayList<Object>();
		for (MUIElement element : input.getChildren()) {
			if (element instanceof MPlaceholder) {
				if (!element.isToBeRendered() || !element.isVisible()) {
					continue;
				}

				element = ((MPlaceholder) element).getRef();
			}

			if (element.isToBeRendered() && element.isVisible()
					&& element instanceof MPart) {
				list.add(element);
			}
		}
		return list;
	}

	public void setInput() {
		getTableViewer().setInput(getInput());
		selectFirstMatch();
	}

	@Override
	protected void gotoSelectedElement() {
		Object selectedElement = getSelectedElement();

		// close the shell
		dispose();

		if (selectedElement instanceof MPart) {
			partService.activate((MPart) selectedElement);
		}
	}

	@Override
	protected boolean deleteSelectedElements() {
		Object selectedElement = getSelectedElement();
		if (selectedElement != null) {
			if (partService.savePart((MPart) selectedElement, true))
				partService.hidePart((MPart) selectedElement);

			if (getShell() == null) {
				// Bug 421170: Contract says to return true if there are no
				// elements left. In this case, there is no shell left because
				// we popped a save dialog and auto-closed the list. Ergo, there
				// are no elements left.
				return true;
			}
			if (getInput().isEmpty()) {
				getShell().dispose();
				return true;
			}

			// Remove part from viewer model
			@SuppressWarnings("unchecked")
			List<Object> viewerInput = (List<Object>) getTableViewer()
					.getInput();
			viewerInput.remove(selectedElement);

		}
		return false;

	}
}
