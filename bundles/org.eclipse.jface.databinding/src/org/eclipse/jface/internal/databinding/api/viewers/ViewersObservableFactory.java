/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.api.viewers;

import org.eclipse.jface.internal.databinding.api.IDataBindingContext;
import org.eclipse.jface.internal.databinding.api.IObservableFactory;
import org.eclipse.jface.internal.databinding.api.observable.IObservable;
import org.eclipse.jface.viewers.AbstractListViewer;

/**
 * A factory that supports binding to JFace viewers. This factory supports the
 * following description objects:
 * <ul>
 * <li>{@link AbstractListViewer} - denotes the viewer's collection of elements</li>
 * <li>{@link TableViewerDescription} - TODO describe</li>
 * <li>org.eclipse.jface.databinding.PropertyDescription - depending on the
 * property description's object and property ID:
 * <ul>
 * <li>object instanceof StructuredViewer, property ID is
 * ViewersProperties.SINGLE_SELECTION - denoting the viewer's (single) selection</li>
 * <li>object instanceof TableViewer, property ID is ViewersProperties.CONTENT</li>
 * <li>object instanceof AbstractListViewer, property ID is
 * ViewersProperties.CONTENT</li>
 * </ul>
 * </li>
 * </ul>
 * TODO complete the list
 * 
 * @since 1.0
 * 
 */
final public class ViewersObservableFactory implements IObservableFactory {

	private int updateTime;

	/**
	 * Create a factory that can create udatables for JFace viewers
	 */
	public ViewersObservableFactory() {
	}

	/**
	 * @param updateTime.
	 *            Update policy of IDataBindingContext.TIME_EARLY or TIME_LATE.
	 *            This is only a hint that some editable viewers may support
	 */
	public ViewersObservableFactory(int updateTime) {
		this.updateTime = updateTime;
	}

	public IObservable createObservable(IDataBindingContext bindingContext,
			Object description) {
//		if (description instanceof Property) {
//			Object object = ((Property) description).getObject();
//			Object attribute = ((Property) description).getPropertyID();
//			if (object instanceof StructuredViewer
//					&& ViewersProperties.SINGLE_SELECTION.equals(attribute)) {
//				return new StructuredViewerObservableValue(
//						(StructuredViewer) object, (String) attribute);
//			}
//			if (object instanceof AbstractListViewer
//					&& ViewersProperties.SINGLE_SELECTION.equals(attribute))
//				return new StructuredViewerObservableValue(
//						(AbstractListViewer) object, (String) attribute);
//			else if (object instanceof AbstractListViewer
//					&& ViewersProperties.CONTENT.equals(attribute))
//				return new ObservableCollectionViewer(
//						(AbstractListViewer) object);
//			if (object instanceof TableViewer
//					&& ViewersProperties.CONTENT.equals(attribute)) {
//				// return new TableViewerObservableCollection((TableViewer)
//				// object);
//				return new TableViewerObservableTable((TableViewer) object);
//			}
//			if (object instanceof TreeViewer
//					&& ViewersProperties.CONTENT.equals(attribute)) {
//				return new TreeViewerObservableTree((TreeViewer) object);
//			}
//		}
//		if (description instanceof AbstractListViewer) {
//			// binding to a Viewer directly implies binding to its
//			// content
//			return new ObservableCollectionViewer(
//					(AbstractListViewer) description);
//		} else if (description instanceof TableViewerDescription) {
//			return new TableViewerObservableCollectionExtended(
//					(TableViewerDescription) description, bindingContext,
//					updateTime);
//		} else if (description instanceof TreeViewerDescription) {
//			return new TreeViewerObservableTreeExtended(
//					(TreeViewerDescription) description, bindingContext);
//		}
		return null;
	}
}
