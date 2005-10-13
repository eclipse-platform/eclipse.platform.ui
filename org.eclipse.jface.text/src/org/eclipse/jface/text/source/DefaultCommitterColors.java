/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;


/**
 * Default implementation, assigns random colors to revisions based on committer id.
 * <p>
 * XXX This API is provisional and may change any time during the development of eclipse 3.2.
 * </p>
 * 
 * @since 3.2
 */
final class DefaultCommitterColors implements ICommitterColors {
	private Map fColors= new HashMap();
	
	DefaultCommitterColors() {
	}
	
	/*
	 * @see org.eclipse.jface.text.source.ICommitterColors#getCommitterRGB(org.eclipse.jface.text.source.AnnotateRulerColumn.Revision)
	 */
	public RGB getCommitterRGB(AnnotateRevision revision) {
		RGB rgb= (RGB) fColors.get(revision.getCommitterId());
		if (rgb == null) {
			rgb= computeRGB(revision.getCommitterId().hashCode());
			fColors.put(revision.getCommitterId(), rgb);
		}
		return rgb;
	}
	
	private RGB computeRGB(int i) {
		float hue= computeHue(Math.abs(i));
		RGB rgb= convertHSV(hue, 0.8f, 0.8f); // 0.8 to get a little less brilliant colors
		return rgb;
	}
	
	private float computeHue(int i) {
		int base= 3;
		int l= i < base ? 0 : (int) Math.floor(Math.log(i / base) / Math.log(2));
		int m= ((int) Math.pow(2, l)) * base;
		int j= i < base ? i : i - m;
		float offset= i < base ? 0 : (float) (180 / base / Math.pow(2, l));
		float delta= i < base ? 120 : 2*offset;
		float hue= (offset + j*delta) % 360;
		return hue;
	}
	
	private RGB convertHSV(float h, float s, float v) {
		/* from http://www.cs.rit.edu/~ncs/color/t_convert.html */
		int i;
		float f, p, q, t;
		if (s == 0)
			return newRGB(v, v, v); // achromatic (grey)
		h /= 60; // sector 0 to 5
		i= (int) Math.floor(h);
		f= h - i; // factorial part of h
		p= v * (1 - s);
		q= v * (1 - s * f);
		t= v * (1 - s * (1 - f));
		switch (i) {
			case 0:
				return newRGB(v, t, p);
			case 1:
				return newRGB(q, v, p);
			case 2:
				return newRGB(p, v, t);
			case 3:
				return newRGB(p, q, v);
			case 4:
				return newRGB(t, p, v);
			case 5:
				return newRGB(v, p, q);
		}
		
		throw new IllegalArgumentException();
	}
	
	private RGB newRGB(float r, float g, float b) {
		return new RGB((int) (r * 255), (int) (g * 255), (int) (b * 255));
	}
}
