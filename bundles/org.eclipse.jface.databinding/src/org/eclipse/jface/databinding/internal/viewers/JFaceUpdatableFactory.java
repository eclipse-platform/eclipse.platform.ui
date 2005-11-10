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

package org.eclipse.jface.databinding.internal.viewers;

import java.util.Map;

import org.eclipse.jface.databinding.DataBinding;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.IUpdatableFactory;
import org.eclipse.jface.databinding.IValidationContext;
import org.eclipse.jface.databinding.PropertyDescription;
import org.eclipse.jface.databinding.TableViewerDescription;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;

/**
 * @since 3.2
 *
 */
public class JFaceUpdatableFactory implements IUpdatableFactory {

	public IUpdatable createUpdatable(Map properties, Object description,
			IValidationContext validationContext) {
		if (description instanceof PropertyDescription) {
			Object object = ((PropertyDescription) description).getObject();
			Object attribute = ((PropertyDescription) description)
					.getPropertyID();
			if (object instanceof StructuredViewer
					&& DataBinding.SELECTION.equals(attribute)) {
				return new StructuredViewerUpdatableValue(
						(StructuredViewer) object, (String) attribute);
			}
			if (object instanceof AbstractListViewer
					&& DataBinding.SELECTION.equals(attribute))
				return new StructuredViewerUpdatableValue(
						(AbstractListViewer) object, (String) attribute);
			else if (object instanceof AbstractListViewer
					&& DataBinding.CONTENT.equals(attribute))
				return new UpdatableCollectionViewer(
						(AbstractListViewer) object);
			if (object instanceof TableViewer
					&& DataBinding.CONTENT.equals(attribute)) {
				return new TableViewerUpdatableCollection((TableViewer) object);
			}
		}
		if (description instanceof AbstractListViewer) {
			// binding to a Viewer directly implies binding to its
			// content
			return new UpdatableCollectionViewer(
					(AbstractListViewer) description);
		} else if (description instanceof TableViewerDescription) {
			return new TableViewerUpdatableCollectionExtended(
					(TableViewerDescription) description, validationContext);
		}
		return null;
	}
}