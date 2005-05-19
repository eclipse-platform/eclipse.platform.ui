package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

public final class IntAffineMatrix {
    
    public static final IntAffineMatrix IDENTITY = new IntAffineMatrix(1, 0, 0, 0, 1, 0);
    public static final IntAffineMatrix ROT_90 = new IntAffineMatrix(0, -1, 0, 1, 0, 0);
    public static final IntAffineMatrix ROT_180 = new IntAffineMatrix(-1, 0, 0, 0, -1, 0);
    public static final IntAffineMatrix ROT_270 = new IntAffineMatrix(0, 1, 0, -1, 0, 0);
    public static final IntAffineMatrix FLIP_XAXIS = new IntAffineMatrix(1, 0, 0, 0, -1, 0);
    public static final IntAffineMatrix FLIP_YAXIS = new IntAffineMatrix(-1, 0, 0, 0, 1, 0);
    
    int m11;
    int m12;
    int m13;
    int m21;
    int m22;
    int m23;
    
    public IntAffineMatrix() {
        this(1, 0, 0, 0, 1, 0);
    }
    
    public IntAffineMatrix(int _m11, int _m12, int _m13, int _m21, int _m22, int _m23) {
        m11 = _m11;
        m12 = _m12;
        m13 = _m13;
        m21 = _m21;
        m22 = _m22;
        m23 = _m23;
    }
    
    public IntAffineMatrix(IntAffineMatrix toCopy) {
        this(toCopy.m11, toCopy.m12, toCopy.m13, toCopy.m21, toCopy.m22, toCopy.m23);
    }
    
    public IntAffineMatrix inverse() {
        int det = det();
        
        return new IntAffineMatrix(
                m22 / det,  -m12 / det, (m12 * m23 - m22 * m13) / det, 
                -m21 / det, m11 / det, (m21 * m13 - m11 * m23) / det); 
    }
    
    /**
     * Returns a rotation matrix that would make a vector pointing directly up
     * point in the given direction.
     * 
     * @param swtDirectionConstant
     * @return a rotation matrix that would make a vector pointing directly up
     * point in the given direction
     */
    public static IntAffineMatrix getRotation(int swtDirectionConstant) {
    	switch(swtDirectionConstant) {
    	case SWT.RIGHT: return ROT_90;
    	case SWT.BOTTOM: return ROT_180;
    	case SWT.LEFT: return ROT_270;
    	}
    	
    	return IDENTITY;
    }
    
    public int det() {
        return m11 * m22 - m12 * m21;
    }
    
    public static IntAffineMatrix translation(int x, int y) {
        return new IntAffineMatrix(1, 0, x, 0, 1, y);
    }
 
    public static IntAffineMatrix translation(Point vector) {
        return new IntAffineMatrix(1, 0, vector.x, 0, 1, vector.y);
    }
    
    /**
     * Returns a new matrix formed by applying the argument transformation followed
     * by the receiver. The receiver and the argument are unmodified.
     * 
     * @param matrix matrix to transform
     * @return a new transformed matrix
     */
    public IntAffineMatrix multiply(IntAffineMatrix m) {
        IntAffineMatrix result = new IntAffineMatrix(
                m11 * m.m11 + m12 * m.m21,
                m11 * m.m12 + m12 * m.m22,
                m11 * m.m13 + m12 * m.m23 + m13,
                m21 * m.m11 + m22 * m.m21,
                m21 * m.m12 + m22 * m.m22,
                m21 * m.m13 + m22 * m.m23 + m23	
        );
        return result;
    }
    
//    /**
//     * Applies the reciever's transformation to the given matrix. For example, if the
//     * argument is a translation and the reciever is a rotation about the origin,
//     * the argument will become a translation followed by a rotation about the origin.
//     *
//     * @param m matrix to be modified
//     */
//    public void transform (IntAffineMatrix m) {
//        m.m11 = m11 * m.m11 + m12 * m.m21;
//        m.m12 = m11 * m.m12 + m12 * m.m22;
//        m.m13 = m11 * m.m13 + m12 * m.m23 + m13;
//        m.m21 = m21 * m.m11 + m22 * m.m21;
//        m.m22 = m21 * m.m12 + m22 * m.m22;
//        m.m23 = m21 * m.m13 + m22 * m.m23 + m23;
//    }
    
    /**
     * Copies and transforms a set of points from the source shape to the dest
     * 
     * @param source source shape (alternating x and y values, starting with an x value)
     * @param sourcePos index of the first point in the source array to transform (the value
     * 3 indicates the 3rd x, y pair in the array, which is at position 6)
     * @param dest destination shape (alternating x and y values, starting with an x value). A
     * portion of this array will be overwritten.
     * @param destPos first point to overwrite in the dest array (each x, y pair is considered
     * 1 position)
     * @param length number of points to copy
     */
    public void transform(int[] source, int sourcePos, int[] dest, int destPos, int length) {
        int nextSrc = sourcePos * 2;
        int nextDest = destPos * 2;
        for(int i = 0; i < length; i++) {
            int sourcex = source[nextSrc++];
            int sourcey = source[nextSrc++];
            
            dest[nextDest++] = getx(sourcex, sourcey);
            dest[nextDest++] = gety(sourcex, sourcey);
        }
    }

	/**
	 * @param sourcex
	 * @param sourcey
	 * @return
	 */
	public int gety(int sourcex, int sourcey) {
		return sourcex * m21 + sourcey * m22 + m23;
	}

	/**
	 * @param sourcex
	 * @param sourcey
	 * @return
	 */
	public int getx(int sourcex, int sourcey) {
		return sourcex * m11 + sourcey * m12 + m13;
	}
    
    public Shape transform(Shape toTransform) {
    	int[] newArray = new int[toTransform.used];
    	transform(toTransform.data, 0, newArray, 0, toTransform.used / 2);
    	return new Shape(newArray);
    }
    
    /**
     * Transforms a point, returning the newly transformed point. The original
     * point is unmodified.
     * 
     * @param toTransform point to transform
     * @return trantsformed point
     */
    public Point multiply(Point toTransform) {
        return new Point(toTransform.x * m11 + toTransform.y * m12 + m13,
                toTransform.x * m21 + toTransform.y * m22 + m23);
    }
}
