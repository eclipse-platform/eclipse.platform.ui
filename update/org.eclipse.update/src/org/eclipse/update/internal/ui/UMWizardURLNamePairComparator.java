package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Comparator;import org.eclipse.core.internal.boot.update.URLNamePair;
public class UMWizardURLNamePairComparator implements Comparator {
	/**
	 * UpdateManagerTreeItemComparator constructor comment.
	 */
	public UMWizardURLNamePairComparator() {
		super();
	}
	/**
	 * Compares its two arguments for order.  Returns a negative integer,
	 * zero, or a positive integer as the first argument is less than, equal
	 * to, or greater than the second.<p>
	 *
	 * The implementor must ensure that <tt>sgn(compare(x, y)) ==
	 * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
	 * implies that <tt>compare(x, y)</tt> must throw an exception if and only
	 * if <tt>compare(y, x)</tt> throws an exception.)<p>
	 *
	 * The implementor must also ensure that the relation is transitive:
	 * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
	 * <tt>compare(x, z)&gt;0</tt>.<p>
	 *
	 * Finally, the implementer must ensure that <tt>compare(x, y)==0</tt>
	 * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
	 * <tt>z</tt>.<p>
	 *
	 * It is generally the case, but <i>not</i> strictly required that 
	 * <tt>(compare(x, y)==0) == (x.equals(y))</tt>.  Generally speaking,
	 * any comparator that violates this condition should clearly indicate
	 * this fact.  The recommended language is "Note: this comparator
	 * imposes orderings that are inconsistent with equals."
	 * 
	 * @return a negative integer, zero, or a positive integer as the
	 *         first argument is less than, equal to, or greater than the
	 *         second. 
	 * @throws ClassCastException if the arguments' types prevent them from
	 *         being compared by this Comparator.
	 */
	public int compare(java.lang.Object o1, java.lang.Object o2) {

		if (o1 instanceof URLNamePair && o2 instanceof URLNamePair) {
			if (((URLNamePair) o1)._getURL() != null && ((URLNamePair) o2)._getURL() != null) {
				return ((URLNamePair) o1)._getURL().compareTo(((URLNamePair) o2)._getURL());
			}
		}

		return 0;
	}
}