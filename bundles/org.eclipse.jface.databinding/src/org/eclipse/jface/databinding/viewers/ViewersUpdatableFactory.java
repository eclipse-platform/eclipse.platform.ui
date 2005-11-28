/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.databinding.viewers;

import java.util.Map;

import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.IUpdatableFactory;
import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.databinding.internal.viewers.StructuredViewerUpdatableValue;
import org.eclipse.jface.databinding.internal.viewers.TableViewerUpdatableCollection;
import org.eclipse.jface.databinding.internal.viewers.TableViewerUpdatableCollectionExtended;
import org.eclipse.jface.databinding.internal.viewers.TreeViewerUpdatableTree;
import org.eclipse.jface.databinding.internal.viewers.TreeViewerUpdatableTreeExtended;
import org.eclipse.jface.databinding.internal.viewers.UpdatableCollectionViewer;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * A factory that supports binding to JFace viewers. This
 * factory supports the following description objects:
 * <ul>
 * <li>{@link AbstractListViewer} - denotes the viewer's collection of elements</li>
 * <li>{@link TableViewerDescription} - TODO describe</li>
 * <li>org.eclipse.jface.databinding.PropertyDescription - depending on the
 * property description's object and property ID:
 * <ul>
 * <li>object instanceof StructuredViewer, property ID is ViewersProperties.SINGLE_SELECTION - denoting the
 * viewer's (single) selection</li>
 * <li>object instanceof TableViewer, property ID is ViewersProperties.CONTENT</li>
 * <li>object instanceof AbstractListViewer, property ID is ViewersProperties.CONTENT</li>
 * </ul>
 * </li>
 * </ul>
 * TODO complete the list
 * @since 3.2
 *
 */
final public class ViewersUpdatableFactory implements IUpdatableFactory {

	public IUpdatable createUpdatable(Map properties, Object description,
			IDataBindingContext bindingContext) {
		if (description instanceof Property) {
			Object object = ((Property) description).getObject();
			Object attribute = ((Property) description)
					.getPropertyID();
			if (object instanceof StructuredViewer
					&& ViewersProperties.SINGLE_SELECTION.equals(attribute)) {
				return new StructuredViewerUpdatableValue(
						(StructuredViewer) object, (String) attribute);
			}
			if (object instanceof AbstractListViewer
					&& ViewersProperties.SINGLE_SELECTION.equals(attribute))
				return new StructuredViewerUpdatableValue(
						(AbstractListViewer) object, (String) attribute);
			else if (object instanceof AbstractListViewer
					&& ViewersProperties.CONTENT.equals(attribute))
				return new UpdatableCollectionViewer(
						(AbstractListViewer) object);
			if (object instanceof TableViewer
					&& ViewersProperties.CONTENT.equals(attribute)) {
				return new TableViewerUpdatableCollection((TableViewer) object);
			}
			if (object instanceof TreeViewer
					&& ViewersProperties.CONTENT.equals(attribute)) {
				return new TreeViewerUpdatableTree((TreeViewer) object);
			}
		}
		if (description instanceof AbstractListViewer) {
			// binding to a Viewer directly implies binding to its
			// content
			return new UpdatableCollectionViewer(
					(AbstractListViewer) description);
		} else if (description instanceof TableViewerDescription) {
			return new TableViewerUpdatableCollectionExtended(
					(TableViewerDescription) description, bindingContext);
		} else if (description instanceof TreeViewerDescription) {
			return new TreeViewerUpdatableTreeExtended(
				(TreeViewerDescription) description, bindingContext);
		}
		return null;
	}
}