/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.testing.ContributionInfoMessages;
import org.eclipse.ui.testing.ContributionInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * @since 3.6
 */
public class SpyHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		Shell shell = HandlerUtil.getActiveShell(event);
		if (shell != null) {
			Control control = shell.getDisplay().getFocusControl();
			if (control != null) {
				showTooltip(control);
			}
		}
		return null;
	}

	protected void showTooltip(Control control) {
		ContributionInfo contributionInfo = null;
		Point offset = new Point(0, 0);
		while (control != null) {
			if (control instanceof Table) {
				Table table = (Table) control;
				if (table.getSelectionCount() == 1) {
					TableItem tableItem = table.getSelection()[0];
					contributionInfo = getContributionInfo(tableItem.getData(),
							ContributionInfoMessages.ContributionInfo_TableItem);
					if (contributionInfo != null) {
						Rectangle bounds = tableItem.getBounds();
						offset = new Point(bounds.x, bounds.y);
						break;
					}
				}
			} else if (control instanceof Tree) {
				Tree tree = (Tree) control;
				if (tree.getSelectionCount() == 1) {
					TreeItem treeItem = tree.getSelection()[0];
					contributionInfo = getContributionInfo(treeItem.getData(),
							ContributionInfoMessages.ContributionInfo_TreeItem);
					if (contributionInfo != null) {
						Rectangle bounds = treeItem.getBounds();
						offset = new Point(bounds.x, bounds.y);
						break;
					}
				}
			}
			String optionalElementType;

			// "force" a contribution info if we are at a shell
			if (control instanceof Shell)
				optionalElementType = ContributionInfoMessages.ContributionInfo_Window;
			else
				optionalElementType = null;

			contributionInfo = getContributionInfo(control.getData(), optionalElementType);

			if (contributionInfo != null) {
				break;
			}
			control = control.getParent();
		}
		if (contributionInfo == null) {
			return;
		}
		doShowTooltip(control, offset, contributionInfo);
	}

	protected void doShowTooltip(Control control, Point offset, final ContributionInfo contributionInfo) {
		ToolTip toolTip = new ToolTip(control, ToolTip.NO_RECREATE, true) {

			@Override
			protected Composite createToolTipContentArea(Event event, Composite parent) {
				// Create the content area
				Composite composite = new Composite(parent, SWT.NONE);
				Color fg = parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND);
				Color bg = parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
				composite.setForeground(fg);
				composite.setBackground(bg);
				Text text = new Text(composite, SWT.READ_ONLY);
				text.setForeground(fg);
				text.setBackground(bg);
				String info = NLS.bind(ContributionInfoMessages.ContributionInfo_ContributedBy,
						contributionInfo.getElementType(), contributionInfo.getBundleId());
				text.setText(info);
				GridLayoutFactory.fillDefaults().margins(2, 2).generateLayout(composite);
				return composite;
			}
		};
		toolTip.setHideOnMouseDown(false);
		toolTip.setHideDelay(3000);
		toolTip.show(offset);
	}

	/**
	 * Returns a ContributionInfo for the given data object (typically, a data
	 * object associated with a widget). If the data object is a ContributionInfo,
	 * return it. If it can be adapted to ContributionInfo, return the result of the
	 * adaptation. Otherwise, if a non-null 'optionalElementType' string is given,
	 * return a contribution info based on it and the bundle that contains the given
	 * object's class.
	 */
	ContributionInfo getContributionInfo(Object data, String optionalElementType) {
		if (data instanceof ContributionInfo) {
			return (ContributionInfo) data;
		}
		ContributionInfo result = Adapters.adapt(data, ContributionInfo.class);
		if (optionalElementType != null && result == null && data != null) {
			Bundle bundle = FrameworkUtil.getBundle(data.getClass());
			if (bundle != null) {
				result = new ContributionInfo(bundle.getSymbolicName(), optionalElementType, null);
			}
		}
		return result;
	}
}