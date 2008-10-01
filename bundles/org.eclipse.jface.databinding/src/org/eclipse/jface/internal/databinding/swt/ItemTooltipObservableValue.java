/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * @since 3.5
 * 
 */
public class ItemTooltipObservableValue extends AbstractSWTObservableValue {

	private final Item item;

	/**
	 * @param item
	 */
	public ItemTooltipObservableValue(Item item) {
		super(item);
		this.item = item;
	}
	
	/**
	 * @param realm
	 * @param item
	 */
	public ItemTooltipObservableValue(Realm realm, Item item) {
		super(realm, item);
		this.item = item;
	}

	public void doSetValue(final Object value) {
		String oldValue = (String) doGetValue();

		String newValue = value == null ? "" : value.toString(); //$NON-NLS-1$
		if (item instanceof CTabItem) {
			((CTabItem)item).setToolTipText(newValue);
		}
		else if (item instanceof TabItem) {
			((TabItem)item).setToolTipText(newValue);
		}
		else if (item instanceof TableColumn) {
			((TableColumn)item).setToolTipText(newValue);
		}
		else if (item instanceof ToolItem) {
			((ToolItem)item).setToolTipText(newValue);
		}
		else if (item instanceof TrayItem) {
			((TrayItem)item).setToolTipText(newValue);
		}
		else if (item instanceof TreeColumn) {
			((TreeColumn)item).setToolTipText(newValue);
		}
		
		if (!newValue.equals(oldValue)) {
			fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		}
	}

	public Object doGetValue() {
		if (item instanceof CTabItem) {
			return ((CTabItem)item).getToolTipText();
		}
		else if (item instanceof TabItem) {
			return ((TabItem)item).getToolTipText();
		}
		else if (item instanceof TableColumn) {
			return ((TableColumn)item).getToolTipText();
		}
		else if (item instanceof ToolItem) {
			return ((ToolItem)item).getToolTipText();
		}
		else if (item instanceof TrayItem) {
			return ((TrayItem)item).getToolTipText();
		}
		else if (item instanceof TreeColumn) {
			return ((TreeColumn)item).getToolTipText();
		}
		
		return null;
	}

	public Object getValueType() {
		return String.class;
	}

}
