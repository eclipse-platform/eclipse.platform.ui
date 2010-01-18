/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;

/**
 * <code>CellData</code> is the layout data object associated with 
 * <code>CellLayout</code>. You can attach a CellData object to a
 * control by using the <code>setLayoutData</code> method. CellData
 * objects are optional. If you do not attach any layout data to a control,
 * it will behave just like attaching a CellData created using its default
 * constructor.
 * 
 * @since 3.0
 **/
public final class CellData {

    /**
     * hintType flag (value = 0) indicating that the control's computeSize method should be used
     * to determine the control size. If modifierType is set to NONE, then the widthHint
     * and heightHint fields will be ignored.
     */
    public final static int NONE = 0;

    /**
     * hintType flag (value = 1) indicating that the widthHint and heightHint should be used
     * as the control's size instead of the result of computeSize
     * <p>
     * This flag is useful for list boxes, text boxes, tree controls, and other controls
     * whose contents can change dynamically. For example, create a tree control and set
     * its width and height hints to the default size for that control. This will cause
     * the hints to be used instead of the preferred size of the tree control.</p>
     */
    public final static int OVERRIDE = 1;

    /**
     * hintType(value = 2) indicating that the width of the control should be no less than
     * widthHint (if provided) and the height of the control should be no less
     * than heightHint (if provided).
     * <p>
     * This flag is useful for buttons. For example, set the width and height hints to
     * the default button size. This will use the default button size unless the button
     * label is too large to fit on the button.
     * </p>  
     */
    public final static int MINIMUM = 2;

    /**
     * hintType flag (value = 3) indicating that the width of the control should be no more than
     * widthHint (if provided) and the height of the control should be no more
     * than heightHint (if provided). 
     * <p>
     * This flag is useful for wrapping text. For example, set heightHint to SWT.DEFAULT
     * and set widthHint to the desired number of pixels after which text should wrap. This
     * will cause the text to wrap after the given number of pixels, but will not allocate
     * extra space in the column if the text widget does not fill an entire line.
     * </p> 
     */
    public final static int MAXIMUM = 3;

    /**
     * This flag controls how the width and height hints are to be treated. See the constants
     * above. 
     */
    public int hintType = OVERRIDE;

    /**
     * Width hint. This modifies the width of the control, in pixels. If set to SWT.DEFAULT,
     * this dimension will not be constrained. Depending on the value of modifierType,
     * this may be a minimum size, a maximum size, or simply replace the preferred control
     * size.
     */
    public int widthHint = SWT.DEFAULT;

    /**
     * Height hint. This modifies the height of the control, in pixels. If set to SWT.DEFAULT,
     * this dimension will not be constrained. Depending on the value of modifierType,
     * this will be a minimum size, a maximum size, or a replacement for the control's preferred
     * size. 
     */
    public int heightHint = SWT.DEFAULT;

    /**
     * Number of rows spanned by this cell (default = 1)
     */
    public int verticalSpan = 1;

    /**
     * Number of columns spanned by this cell (default = 1)
     */
    public int horizontalSpan = 1;

    /**
     * Horizontal alignment of the control within the cell. May be one
     * of SWT.LEFT, SWT.RIGHT, SWT.CENTER, or SWT.NORMAL. SWT.NORMAL indicates
     * that the control should be made as wide as the cell.
     */
    public int horizontalAlignment = SWT.FILL;

    /**
     * Vertical alignment of the control within the cell. May be one of
     * SWT.TOP, SWT.BOTTOM, SWT.CENTER, or SWT.NORMAL. SWT.NORMAL indicates
     * that the control should be made as wide as the cell.
     */
    public int verticalAlignment = SWT.FILL;

    /**
     * Horizontal indentation (pixels). Positive values move the control 
     * to the right, negative to the left.
     */
    public int horizontalIndent = 0;

    /**
     * Vertical indentation (pixels). Positive values move the control
     * down, negative values move the control up.
     */
    public int verticalIndent = 0;

    /**
     * Constructs a CellData with default properties
     */
    public CellData() {
        // Use the default values for all fields.
    }

    /**
     * Creates a new CellData that with properties that are as close as possible to
     * the given GridData. This is used for converting GridLayouts into CellLayouts.
     * 
     * @param data
     */
    public CellData(GridData data) {
        verticalSpan = data.verticalSpan;
        horizontalSpan = data.horizontalSpan;

        switch (data.horizontalAlignment) {
        case GridData.BEGINNING:
            horizontalAlignment = SWT.LEFT;
            break;
        case GridData.CENTER:
            horizontalAlignment = SWT.CENTER;
            break;
        case GridData.END:
            horizontalAlignment = SWT.RIGHT;
            break;
        case GridData.FILL:
            horizontalAlignment = SWT.FILL;
            break;
        }

        switch (data.verticalAlignment) {
        case GridData.BEGINNING:
            verticalAlignment = SWT.LEFT;
            break;
        case GridData.CENTER:
            verticalAlignment = SWT.CENTER;
            break;
        case GridData.END:
            verticalAlignment = SWT.RIGHT;
            break;
        case GridData.FILL:
            verticalAlignment = SWT.FILL;
            break;
        }

        widthHint = data.widthHint;
        heightHint = data.heightHint;
        horizontalIndent = data.horizontalIndent;
        hintType = OVERRIDE;
    }

    /**
     * Copies the given CellData
     * 
     * @param newData
     */
    public CellData(CellData newData) {
        hintType = newData.hintType;
        widthHint = newData.widthHint;
        heightHint = newData.heightHint;
        horizontalAlignment = newData.horizontalAlignment;
        verticalAlignment = newData.verticalAlignment;
        horizontalSpan = newData.horizontalSpan;
        verticalSpan = newData.verticalSpan;
    }

    /**
     * Sets the size hint for this control. This is used to modify the control's 
     * preferred size. If one dimension should remain unmodified, that hint can be
     * set to SWT.DEFAULT. Using a size hint of CellData.MINIMUM ensures that the preferred
     * control size is larger than the hint. Using a size hint of CellData.MAXIMUM ensures
     * that the preferred size is smaller than the hint. Using a size hint of CellData.OVERRIDE
     * ensures that the preferred size is always equal to the hint. 
     * 
     * @param hintType one of CellData.MINIMUM, CellData.MAXIMUM, or CellData.OVERRIDE
     * @param hint size hint (in pixels). If either dimension is set to SWT.DEFAULT, the
     * hint will not affect that dimension 
     * @return this
     */
    public CellData setHint(int hintType, Point hint) {
        return setHint(hintType, hint.x, hint.y);
    }

    /**
     * Sets the size hint for this control. This is used to modify the control's 
     * preferred size. If one dimension should remain unmodified, that hint can be
     * set to SWT.DEFAULT. Using a size hint of CellData.MINIMUM ensures that the preferred
     * control size is larger than the hint. Using a size hint of CellData.MAXIMUM ensures
     * that the preferred size is smaller than the hint. Using a size hint of CellData.OVERRIDE
     * ensures that the preferred size is always equal to the hint. If both hints are equal
     * to SWT.DEFAULT, then the control's preferred size is unmodified.
     * 
     * @param hintType one of CellData.MINIMUM, CellData.MAXIMUM, or CellData.OVERRIDE
     * @param horizontal horizontal hint (pixels). A value of SWT.DEFAULT will leave the result
     * of the control's computeSize method unmodified.
     * @param vertical vertical hint (pixels). A value of SWT.DEFAULT will leave the result of 
     * the control's computeSize method unmodified.
     * @return this
     */
    public CellData setHint(int hintType, int horizontal, int vertical) {
        this.hintType = hintType;
        this.heightHint = vertical;
        this.widthHint = horizontal;

        return this;
    }

    /**
     * Sets the alignment for this control
     * 
     * @param horizontalAlignment one of SWT.LEFT, SWT.RIGHT, SWT.FILL, or SWT.CENTER
     * @param verticalAlignment one of SWT.TOP, SWT.BOTTOM, SWT.FILL, or SWT.CENTER
     * @return this
     */
    public CellData align(int horizontalAlignment, int verticalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        this.verticalAlignment = verticalAlignment;

        return this;
    }

    /**
     * Sets the number of rows and columns spanned by this control.
     * 
     * @param horizontalSpan number of columns spanned by the control (> 0)
     * @param verticalSpan number of rows spanned by the control (> 0)
     * @return this
     */
    public CellData span(int horizontalSpan, int verticalSpan) {
        this.horizontalSpan = horizontalSpan;
        this.verticalSpan = verticalSpan;

        return this;
    }

    /**
     * Sets the indentation for this control. The indentation is added to
     * the control's position within the cell. For example, indentation of
     * (10,4) will move the control right by 10 pixels and down by 4 pixels.
     * 
     * @param indent indentation (pixels)
     * @return this
     */
    public CellData indent(Point indent) {
        return this.indent(indent.x, indent.y);
    }

    /**
     * Sets the indentation for this cell
     * 
     * @param horizontalIndent distance (pixels) to move the control to the right
     * @param verticalIndent distance (pixels) to move the control down
     * @return this
     */
    public CellData indent(int horizontalIndent, int verticalIndent) {
        this.horizontalIndent = horizontalIndent;
        this.verticalIndent = verticalIndent;

        return this;
    }

    /**
     * Returns the preferred size of the given control, given the known dimensions of
     * its cell.
     *  
     * @param toCompute the control whose size is to be computed
     * @param cellWidth width of the cell, in pixels (or SWT.DEFAULT if unknown)
     * @param cellHeight height of the cell, in pixels (or SWT.DEFAULT if unknown)
     * @return the preferred size of the given control, in pixels
     */
    public Point computeSize(SizeCache toCompute, int cellWidth, int cellHeight) {

        int absHorizontalIndent = Math.abs(horizontalIndent);
        int absVerticalIndent = Math.abs(verticalIndent);

        // If we're going to indent, subtract off the space that will be required for indentation from
        // the available space
        if (cellWidth != SWT.DEFAULT) {
            cellWidth -= absHorizontalIndent;
        }

        if (cellHeight != SWT.DEFAULT) {
            cellHeight -= absVerticalIndent;
        }

        int controlWidth = horizontalAlignment == SWT.FILL ? cellWidth
                : SWT.DEFAULT;
        int controlHeight = verticalAlignment == SWT.FILL ? cellHeight
                : SWT.DEFAULT;

        // Note: this could be optimized further. If we're using a MAXIMUM hint and 
        // non-FILL alignment, we could simply call computeMaximumBoundedSize using the
        // minimum of the cell size and the hint as the boundary -- basically, rather
        // than applying two limits for the hint and the cell boundary, we can do it in
        // one step and reduce the size computations by half (for this specific case).
        Point controlSize = computeControlSize(toCompute, controlWidth,
                controlHeight);

        if (cellWidth != SWT.DEFAULT && controlSize.x > cellWidth) {
            controlSize = computeControlSize(toCompute, cellWidth,
                    controlHeight);
            if (cellHeight != SWT.DEFAULT && controlSize.y > cellHeight) {
                controlSize.y = cellHeight;
            }
        } else if (cellHeight != SWT.DEFAULT && controlSize.y > cellHeight) {
            controlSize = computeControlSize(toCompute, controlWidth,
                    cellHeight);
            if (cellWidth != SWT.DEFAULT && controlSize.x > cellWidth) {
                controlSize.x = cellWidth;
            }
        }

        // If we're going to indent, add the indentation to the required space 
        controlSize.x += absHorizontalIndent;
        controlSize.y += absVerticalIndent;

        return controlSize;
    }

    /**
     * Arranges the given control within the given rectangle using the
     * criteria described by this CellData.
     * 
     * @param control 
     * @param cellBounds
     * @since 3.0
     */
    public void positionControl(SizeCache cache, Rectangle cellBounds) {

        int startx = cellBounds.x;
        int starty = cellBounds.y;
        int availableWidth = cellBounds.width - horizontalIndent;
        int availableHeight = cellBounds.height - verticalIndent;

        Point size = computeSize(cache, availableWidth, availableHeight);

        // Horizontal justification
        switch (horizontalAlignment) {
        case SWT.RIGHT:
            startx = cellBounds.x + availableWidth - size.x;
            break;
        case SWT.CENTER:
            startx = cellBounds.x + (availableWidth - size.x) / 2;
            break;
        }

        // Vertical justification
        switch (verticalAlignment) {
        case SWT.BOTTOM:
            starty = cellBounds.y + availableHeight - size.y;
            break;
        case SWT.CENTER:
            starty = cellBounds.y + (availableHeight - size.y) / 2;
            break;
        }

        // Position the control
        cache.getControl().setBounds(startx + horizontalIndent,
                starty + verticalIndent, size.x, size.y);
    }

    /**
     * Returns the preferred size of the given control in this cell, given one or both
     * known dimensions of the control. This differs from computeSize, which takes known
     * dimensions of the <b>cell</b> as arguments.
     * 
     * @param toCompute
     * @param controlWidth
     * @param controlHeight
     * @return
     * @since 3.0
     */
    private Point computeControlSize(SizeCache toCompute, int controlWidth,
            int controlHeight) {
        switch (hintType) {
        case OVERRIDE:
            return computeOverrideSize(toCompute, controlWidth, controlHeight,
                    widthHint, heightHint);
        case MINIMUM:
            return computeMinimumBoundedSize(toCompute, controlWidth,
                    controlHeight, widthHint, heightHint);
        case MAXIMUM:
            return computeMaximumBoundedSize(toCompute, controlWidth,
                    controlHeight, widthHint, heightHint);
        }

        return computeRawSize(toCompute, controlWidth, controlHeight);
    }

    /** 
     * Computes the size of the control, given its outer dimensions. This should be used in
     * place of calling Control.computeSize, since Control.computeSize takes control-specific
     * inner dimensions as hints.
     *
     * @param toCompute Control whose size will be computed
     * @param controlWidth width of the control (pixels or SWT.DEFAULT if unknown)
     * @param controlHeight height of the control (pixels or SWT.DEFAULT if unknown)
     * @return preferred dimensions of the control
     */
    private static Point computeRawSize(SizeCache toCompute, int controlWidth,
            int controlHeight) {
        if (controlWidth != SWT.DEFAULT && controlHeight != SWT.DEFAULT) {
            return new Point(controlWidth, controlHeight);
        }

        // Known bug: we pass the OUTER dimension of the control into computeSize, even though
        // SWT expects a control-specific inner dimension as width and height hints. Currently,
        // SWT does not provide any means to convert outer dimensions into inner dimensions.
        // Fortunately, the outer and inner dimensions tend to be quite close so we
        // pass in the outer dimension and adjust the result if it differs from one of the
        // hints. This may cause incorrect text wrapping in rare cases, and should be fixed
        // once SWT provides a way to convert the outer dimension of a control into a valid
        // width or height hint for Control.computeSize. Note that the distinction between outer
        // and inner dimensions is undocumented in SWT, and most examples also contain this
        // bug.
        Point result = toCompute.computeSize(controlWidth, controlHeight);

        // Hack: If the result of computeSize differs from the width or height-hints, adjust it.
        // See above. Don't remove this hack until SWT provides some way to pass correct width
        // and height hints into computeSize. Once this happens, these conditions should always
        // return false and the hack will have no effect.
        if (controlWidth != SWT.DEFAULT) {
            result.x = controlWidth;
        } else if (controlHeight != SWT.DEFAULT) {
            result.y = controlHeight;
        }

        return result;
    }

    /**
     * Computes the preferred size of the control. Optionally, one or both dimensions
     * may be fixed to a given size.
     * 
     * @param control object that can compute the size of the control of interest
     * @param wHint known width (or SWT.DEFAULT if the width needs to be computed) 
     * @param hHint known height (or SWT.DEFAULT if the height needs to be computed) 
     * @param overrideW width that should always be returned by the control, 
     * or SWT.DEFAULT if the width is not being constrained
     * @param overrideH height that should always be returned by the control, 
     * or SWT.DEFAULT if the height is not being constrained
     * @return
     */
    private static Point computeOverrideSize(SizeCache control, int wHint,
            int hHint, int overrideW, int overrideH) {
        int resultWidth = overrideW;
        int resultHeight = overrideH;

        if (wHint != SWT.DEFAULT) {
            resultWidth = wHint;
        }

        if (hHint != SWT.DEFAULT) {
            resultHeight = hHint;
        }

        if (resultWidth == SWT.DEFAULT || resultHeight == SWT.DEFAULT) {
            Point result = computeRawSize(control, resultWidth, resultHeight);

            return result;
        }

        return new Point(resultWidth, resultHeight);
    }

    /**
     * Computes the size for the control, optionally bounding the size in the x and
     * y directions. The various hints are used to determine which dimensions are
     * already known and which dimensions need to be computed.
     * 
     * @param control The control whose size should be computed
     * @param wHint known width (or SWT.DEFAULT if the width needs to be computed) 
     * @param hHint known height (or SWT.DEFAULT if the height needs to be computed) 
     * @param boundedWidth maximum width for the control (or SWT.DEFAULT if the width is unbounded)
     * @param boundedHeight maximum height for the control (or SWT.DEFAULT if the height is unbounded)
     * @return the preferred size of the control, given that it cannot exceed the given bounds 
     */
    private static Point computeMaximumBoundedSize(SizeCache control,
            int wHint, int hHint, int boundedWidth, int boundedHeight) {
        Point controlSize = computeRawSize(control, wHint, hHint);

        if (wHint == SWT.DEFAULT && boundedWidth != SWT.DEFAULT
                && controlSize.x > boundedWidth) {
            return computeMaximumBoundedSize(control, boundedWidth, hHint,
                    boundedWidth, boundedHeight);
        }

        if (hHint == SWT.DEFAULT && boundedHeight != SWT.DEFAULT
                && controlSize.y > boundedHeight) {
            return computeMaximumBoundedSize(control, wHint, boundedHeight,
                    boundedWidth, boundedHeight);
        }

        return controlSize;
    }

    private static Point computeMinimumBoundedSize(SizeCache control,
            int wHint, int hHint, int minimumWidth, int minimumHeight) {

        Point controlSize = computeRawSize(control, wHint, hHint);

        if (minimumWidth != SWT.DEFAULT && wHint == SWT.DEFAULT
                && controlSize.x < minimumWidth) {
            return computeMinimumBoundedSize(control, minimumWidth, hHint,
                    minimumWidth, minimumHeight);
        }

        if (minimumHeight != SWT.DEFAULT && hHint == SWT.DEFAULT
                && controlSize.y < minimumHeight) {
            return computeMinimumBoundedSize(control, wHint, minimumHeight,
                    minimumWidth, minimumHeight);
        }

        return controlSize;
    }

}
