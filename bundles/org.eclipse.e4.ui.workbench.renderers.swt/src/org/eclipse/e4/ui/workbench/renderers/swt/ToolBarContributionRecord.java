/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.action.ToolBarManager;

public class ToolBarContributionRecord {
	MToolBar toolbarModel;
	MToolBarContribution toolbarContribution;
	ArrayList<MToolBarElement> generatedElements = new ArrayList<MToolBarElement>();
	HashSet<MToolBarElement> sharedElements = new HashSet<MToolBarElement>();
	ToolBarManagerRenderer renderer;
	boolean isVisible = true;

	public ToolBarContributionRecord(MToolBar model,
			MToolBarContribution contribution, ToolBarManagerRenderer renderer) {
		this.toolbarModel = model;
		this.toolbarContribution = contribution;
		this.renderer = renderer;
	}

	public ToolBarManager getManagerForModel() {
		return renderer.getManager(toolbarModel);
	}

	/**
	 * @param context
	 */
	public void updateVisibility(IEclipseContext context) {
		ExpressionContext exprContext = new ExpressionContext(context);
		updateIsVisible(exprContext);
		HashSet<ToolBarContributionRecord> recentlyUpdated = new HashSet<ToolBarContributionRecord>();
		recentlyUpdated.add(this);
		boolean changed = false;
		for (MToolBarElement item : generatedElements) {
			boolean currentVisibility = computeVisibility(recentlyUpdated,
					item, exprContext);
			if (item.isVisible() != currentVisibility) {
				item.setVisible(currentVisibility);
				changed = true;
			}
		}
		for (MToolBarElement item : sharedElements) {
			boolean currentVisibility = computeVisibility(recentlyUpdated,
					item, exprContext);
			if (item.isVisible() != currentVisibility) {
				item.setVisible(currentVisibility);
				changed = true;
			}
		}

		if (changed) {
			getManagerForModel().markDirty();
		}
	}

	public void updateIsVisible(ExpressionContext exprContext) {
		isVisible = ContributionsAnalyzer.isVisible(toolbarContribution,
				exprContext);
	}

	public boolean computeVisibility(
			HashSet<ToolBarContributionRecord> recentlyUpdated,
			MToolBarElement item, ExpressionContext exprContext) {
		boolean currentVisibility = isVisible;
		if (item instanceof MToolBarSeparator) {
			ArrayList<ToolBarContributionRecord> list = renderer.getList(item);
			if (list != null) {
				Iterator<ToolBarContributionRecord> cr = list.iterator();
				while (!currentVisibility && cr.hasNext()) {
					ToolBarContributionRecord rec = cr.next();
					if (!recentlyUpdated.contains(rec)) {
						rec.updateIsVisible(exprContext);
						recentlyUpdated.add(rec);
					}
					currentVisibility |= rec.isVisible;
				}
			}
		}
		if (currentVisibility
				&& item.getVisibleWhen() instanceof MCoreExpression) {
			boolean val = ContributionsAnalyzer.isVisible(
					(MCoreExpression) item.getVisibleWhen(), exprContext);
			currentVisibility = val;
		}
		return currentVisibility;
	}

	public boolean anyVisibleWhen() {
		if (toolbarContribution.getVisibleWhen() != null) {
			return true;
		}
		for (MToolBarElement child : toolbarContribution.getChildren()) {
			if (child.getVisibleWhen() != null) {
				return true;
			}
		}
		return false;
	}

	public boolean mergeIntoModel() {
		int idx = getIndex(toolbarModel,
				toolbarContribution.getPositionInParent());
		if (idx == -1) {
			return false;
		}

		for (MToolBarElement item : toolbarContribution.getChildren()) {
			MToolBarElement copy = (MToolBarElement) EcoreUtil
					.copy((EObject) item);
			// if a visibleWhen clause is defined, the item should not be
			// visible until the clause has been evaluated and returned 'true'
			copy.setVisible(!anyVisibleWhen());
			if (copy instanceof MToolBarSeparator) {
				MToolBarSeparator shared = findExistingSeparator(copy
						.getElementId());
				if (shared == null) {
					shared = (MToolBarSeparator) copy;
					renderer.linkElementToContributionRecord(copy, this);
					toolbarModel.getChildren().add(idx++, copy);
				} else {
					copy = shared;
				}
				sharedElements.add(shared);
			} else {
				generatedElements.add(copy);
				renderer.linkElementToContributionRecord(copy, this);
				toolbarModel.getChildren().add(idx++, copy);
			}
			if (copy instanceof MToolBarSeparator) {
				ArrayList<ToolBarContributionRecord> array = renderer
						.getList(copy);
				array.add(this);
			}
		}
		return true;
	}

	MToolBarSeparator findExistingSeparator(String id) {
		if (id == null) {
			return null;
		}
		for (MToolBarElement item : toolbarModel.getChildren()) {
			if (item instanceof MToolBarSeparator
					&& id.equals(item.getElementId())) {
				return (MToolBarSeparator) item;
			}
		}
		return null;
	}

	public void dispose() {
		for (MToolBarElement copy : generatedElements) {
			toolbarModel.getChildren().remove(copy);
		}
		for (MToolBarElement shared : sharedElements) {
			ArrayList<ToolBarContributionRecord> array = renderer
					.getList(shared);
			array.remove(this);
			if (array.isEmpty()) {
				toolbarModel.getChildren().remove(shared);
			}
		}
	}

	private static int getIndex(MElementContainer<?> model,
			String positionInParent) {
		String id = null;
		String modifier = null;
		if (positionInParent != null && positionInParent.length() > 0) {
			String[] array = positionInParent.split("="); //$NON-NLS-1$
			modifier = array[0];
			id = array[1];
		}
		if (id == null) {
			return model.getChildren().size();
		}

		int idx = 0;
		int size = model.getChildren().size();
		while (idx < size) {
			if (id.equals(model.getChildren().get(idx).getElementId())) {
				if ("after".equals(modifier)) { //$NON-NLS-1$
					idx++;
				}
				return idx;
			}
			idx++;
		}
		return id.equals("additions") ? model.getChildren().size() : -1; //$NON-NLS-1$
	}
}