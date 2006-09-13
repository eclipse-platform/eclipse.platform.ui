/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;

import org.eclipse.jface.util.SafeRunnable;

import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.rulers.IColumnSupport;
import org.eclipse.ui.texteditor.rulers.IContributedRulerColumn;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;
import org.eclipse.ui.texteditor.rulers.RulerColumnRegistry;

/**
 * Captures the vertical and overview ruler support of an {@link ITextEditor}.
 * <p>
 * <em>This API is provisional and may change any time before the 3.3 API freeze.</em>
 * </p>
 * 
 * @since 3.3
 */
public class ColumnSupport implements IColumnSupport {
	private final AbstractTextEditor fEditor;
	private final RulerColumnRegistry fRegistry;

	/**
	 * Creates a new column support for the given editor. Only the editor itself should normally
	 * create such an instance.
	 * 
	 * @param editor the editor
	 * @param registry the contribution registry to refer to
	 */
	public ColumnSupport(AbstractTextEditor editor, RulerColumnRegistry registry) {
		Assert.isLegal(editor != null);
		Assert.isLegal(registry != null);
		fEditor= editor;
		fRegistry= registry;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.IColumnSupport#setColumnVisible(java.lang.String, boolean)
	 */
	public final void setColumnVisible(RulerColumnDescriptor descriptor, boolean visible) {
		Assert.isLegal(descriptor != null);

		final CompositeRuler ruler= getRuler();
		if (ruler == null)
			return;

		if (!isColumnSupported(descriptor))
			visible= false;

		if (isColumnVisible(descriptor)) {
			if (!visible)
				removeColumn(ruler, descriptor);
		} else {
			if (visible)
				addColumn(ruler, descriptor);
		}
	}
	
	private void addColumn(final CompositeRuler ruler, final RulerColumnDescriptor descriptor) {
		
		final int idx= computeIndex(ruler, descriptor);
		
		SafeRunnable runnable= new SafeRunnable() {
			public void run() throws Exception {
				IContributedRulerColumn column= descriptor.createColumn(fEditor);
				initializeColumn(column);
				ruler.addDecorator(idx, column);
			}
		};
		SafeRunner.run(runnable);
	}

	/**
	 * Hook to let subclasses initialize a newly created column.
	 * 
	 * @param column the created column
	 */
	protected void initializeColumn(IContributedRulerColumn column) {
	}

	private void removeColumn(final CompositeRuler ruler, final RulerColumnDescriptor descriptor) {
		final IContributedRulerColumn target= getVisibleColumn(ruler, descriptor);
		if (target != null) {
			SafeRunnable runnable= new SafeRunnable() {
				public void run() throws Exception {
					ruler.removeDecorator(target);
					descriptor.disposeColumn(target);
				}
			};
			SafeRunner.run(runnable);
		}
	}

	/**
	 * Returns the currently visible column matching <code>id</code>, <code>null</code> if
	 * none.
	 * 
	 * @param ruler the composite ruler to scan
	 * @param descriptor the descriptor of the column of interest
	 * @return the matching column or <code>null</code>
	 */
	private IContributedRulerColumn getVisibleColumn(CompositeRuler ruler, RulerColumnDescriptor descriptor) {
		for (Iterator it= ruler.getDecoratorIterator(); it.hasNext();) {
			IVerticalRulerColumn column= (IVerticalRulerColumn)it.next();
			if (column instanceof IContributedRulerColumn) {
				IContributedRulerColumn rulerColumn= (IContributedRulerColumn)column;
				RulerColumnDescriptor rcd= rulerColumn.getDescriptor();
				if (descriptor.equals(rcd))
					return rulerColumn;
			}
		}
		return null;
	}

	/**
	 * Computes the insertion index for a column contribution into the currently visible columns.
	 * 
	 * @param ruler the composite ruler into which to insert the column
	 * @param descriptor the descriptor to compute the index for
	 * @return the insertion index for a new column
	 */
	private int computeIndex(CompositeRuler ruler, RulerColumnDescriptor descriptor) {
		int index= 0;
		List all= fRegistry.getColumnDescriptors();
		int newPos= all.indexOf(descriptor);
		for (Iterator it= ruler.getDecoratorIterator(); it.hasNext();) {
			IVerticalRulerColumn column= (IVerticalRulerColumn) it.next();
			if (column instanceof IContributedRulerColumn) {
				RulerColumnDescriptor rcd= ((IContributedRulerColumn)column).getDescriptor();
				if (rcd != null && all.indexOf(rcd) > newPos)
					break;
			} else if ("org.eclipse.jface.text.source.projection.ProjectionRulerColumn".equals(column.getClass().getName())) { //$NON-NLS-1$
				// projection column is always the rightmost column
				break;
			}
			index++;
		}
		return index;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IColumnSupport#isColumnVisible(java.lang.String)
	 */
	public final boolean isColumnVisible(RulerColumnDescriptor descriptor) {
		Assert.isLegal(descriptor != null);
		CompositeRuler ruler= getRuler();
		return ruler != null && getVisibleColumn(ruler, descriptor) != null;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.IColumnSupport#isColumnSupported(java.lang.String)
	 */
	public final boolean isColumnSupported(RulerColumnDescriptor descriptor) {
		Assert.isLegal(descriptor != null);
		if (getRuler() == null)
			return false;
		
		if (descriptor == null)
			return false;
		
		return descriptor.matchesEditor(fEditor);
	}
	
	/**
	 * Returns the editor's vertical ruler, if it is a {@link CompositeRuler}, <code>null</code>
	 * otherwise.
	 * 
	 * @return the editor's {@link CompositeRuler} or <code>null</code>
	 */
	private CompositeRuler getRuler() {
		Object ruler= fEditor.getAdapter(IVerticalRulerInfo.class);
		if (ruler instanceof CompositeRuler)
			return (CompositeRuler) ruler;
		return null;
	}

	/**
	 * Removes all currently showing rulers.
	 */
	public void dispose() {
		CompositeRuler ruler= getRuler();
		if (ruler == null)
			return;

		List toRemove= new ArrayList(10);
		for (Iterator it= ruler.getDecoratorIterator(); it.hasNext();) {
			IVerticalRulerColumn column= (IVerticalRulerColumn) it.next();
			if (column instanceof IContributedRulerColumn) {
				RulerColumnDescriptor rcd= ((IContributedRulerColumn)column).getDescriptor();
				if (rcd != null)
					toRemove.add(rcd);
			}
		}
		for (Iterator it= toRemove.iterator(); it.hasNext();) {
			RulerColumnDescriptor rcd= (RulerColumnDescriptor) it.next();
			removeColumn(ruler, rcd);
		}
	}
}
