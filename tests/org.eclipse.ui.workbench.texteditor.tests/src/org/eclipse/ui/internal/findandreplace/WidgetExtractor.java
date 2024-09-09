/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vector Informatik GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.ui.internal.findandreplace.overlay.HistoryTextWrapper;

public final class WidgetExtractor {

	private final Composite rootContainer;

	private final String idDataKey;

	public WidgetExtractor(String idDataKey, Composite container) {
		this.idDataKey= idDataKey;
		this.rootContainer= container;
	}

	public HistoryTextWrapper findHistoryTextWrapper(String id) {
		return findWidget(rootContainer, HistoryTextWrapper.class, id);
	}

	public Combo findCombo(String id) {
		return findWidget(rootContainer, Combo.class, id);
	}

	public Button findButton(String id) {
		return findWidget(rootContainer, Button.class, id);
	}

	public ToolItem findToolItem(String id) {
		return findWidget(rootContainer, ToolItem.class, id);
	}

	private <T extends Widget> T findWidget(Composite container, Class<T> type, String id) {
		List<T> widgets= findWidgets(container, type, id);
		assertFalse("more than one matching widget found for id '" + id + "':" + widgets, widgets.size() > 1);
		return widgets.isEmpty() ? null : widgets.get(0);
	}

	private <T extends Widget> List<T> findWidgets(Composite container, Class<T> type, String id) {
		List<Widget> children= new ArrayList<>();
		children.addAll(List.of(container.getChildren()));
		if (container instanceof ToolBar toolbar) {
			children.addAll(List.of(toolbar.getItems()));
		}
		List<T> result= new ArrayList<>();
		for (Widget child : children) {
			if (type.isInstance(child)) {
				if (id.equals(child.getData(idDataKey))) {
					result.add(type.cast(child));
				}
			}
			if (child instanceof Composite compositeChild) {
				result.addAll(findWidgets(compositeChild, type, id));
			}
		}
		return result;
	}

}
