/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;

/**
 * This interface defines a visual component which may serve text viewers as an overview annotation
 * presentation area. This means, presentation of annotations is independent from the actual view
 * port of the text viewer. The annotations of the viewer's whole document are visible in the
 * overview ruler.
 * <p>
 * This interfaces embodies three contracts:
 * <ul>
 * <li>The overview ruler retrieves the annotations it presents from an annotation model.
 * <li>The ruler is a visual component which must be integrated in a hierarchy of SWT controls.
 * <li>The ruler provides interested clients with mapping and interaction information. This covers
 * the mapping between coordinates of the ruler's control and line numbers based on the connected
 * text viewer's document (<code>IVerticalRulerInfo</code>).
 * </ul>
 * </p>
 * <p>
 * In order to provide backward compatibility for clients of <code>IOverviewRuler</code>, extension
 * interfaces are used as a means of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link org.eclipse.jface.text.source.IOverviewRulerExtension} since version 3.8
 * allowing the ruler to set whether to use saturated colors.</li>
 * </ul>
 * </p>
 * <p>
 * Clients may implement this interface or use the default implementation provided by
 * <code>OverviewRuler</code>.
 * </p>
 * 
 * @see org.eclipse.jface.text.ITextViewer
 * @see org.eclipse.jface.text.source.IOverviewRulerExtension
 * @since 2.1
 */
public interface IOverviewRuler extends IVerticalRuler {

	/**
	 * Returns whether there is an annotation an the given vertical coordinate. This
	 * method takes the compression factor of the overview ruler into account.
	 *
	 * @param y the y-coordinate
	 * @return <code>true</code> if there is an annotation, <code>false</code> otherwise
	 */
	boolean hasAnnotation(int y);

	/**
	 * Returns the height of the visual presentation of an annotation in this
	 * overview ruler. Assumes that all annotations are represented using the
	 * same height.
	 *
	 * @return the visual height of an annotation
	 */
	int getAnnotationHeight();

	/**
	 * Sets the color for the given annotation type in this overview ruler.
	 *
	 * @param annotationType the annotation type
	 * @param color the color
	 */
	void setAnnotationTypeColor(Object annotationType, Color color);

	/**
	 * Sets the drawing layer for the given annotation type in this overview ruler.
	 *
	 * @param annotationType the annotation type
	 * @param layer the drawing layer
	 */
	void setAnnotationTypeLayer(Object annotationType, int layer);

	/**
	 * Adds the given annotation type to this overview ruler. Starting with this
	 * call, annotations of the given type are shown in the overview ruler.
	 *
	 * @param annotationType the annotation type
	 */
	void addAnnotationType(Object annotationType);

	/**
	 * Removes the given annotation type from this overview ruler. Annotations
	 * of the given type are no longer shown in the overview ruler.
	 *
	 * @param annotationType the annotation type
	 */
	void removeAnnotationType(Object annotationType);

	/**
	 * Adds the given annotation type to the header of this ruler. Starting with
	 * this call, the presence of annotations is tracked and the header is drawn
	 * in the configured color.
	 *
	 * @param annotationType the annotation type to be tracked
	 */
	void addHeaderAnnotationType(Object annotationType);

	/**
	 * Removes the given annotation type from the header of this ruler. The
	 * presence of annotations of the given type is no longer tracked and the
	 * header is drawn in the default color, depending on the other configured
	 * configured annotation types.
	 *
	 * @param annotationType the annotation type to be removed
	 */
	void removeHeaderAnnotationType(Object annotationType);

	/**
	 * Returns this rulers header control. This is the little area between the
	 * top of the text widget and the top of this overview ruler.
	 *
	 * @return the header control of this overview ruler.
 	 */
	Control getHeaderControl();
}
