package org.eclipse.ui.internal;

import org.eclipse.swt.graphics.Point;

public class Shape {
	int[] data;
	int used = 0;
	
	public Shape(int[] data) {
		this.data = data;
		this.used = data.length;
	}
	
	public Shape(int size) {
		data = new int[size * 2];
	}
	
	public Shape() {
		this(8);
	}
	
	public void add(IntAffineMatrix transform, Shape toAppend) {
		int idx = 0;
		while(idx < toAppend.used) {
			int x = toAppend.data[idx++];
			int y = toAppend.data[idx++];
			
			add(transform.getx(x,y), transform.gety(x, y));
		}
	}
	
	public void add(Shape toAppend) {
		int idx = 0;
		while(idx < toAppend.used) {
			int x = toAppend.data[idx++];
			int y = toAppend.data[idx++];
			
			add(x, y);
		}
	}
	
	public Shape reverse() {
		int[] result = new int[used];
		
		int idx = 0;
		int srcIdx = used - 2;
		while (idx < used) {
			result[idx++] = data[srcIdx];
			result[idx++] = data[srcIdx + 1];
			srcIdx -= 2;
		}
		
		return new Shape(result);
	}
	
	public void add(int x, int y) {
		if (used >= data.length - 1) {
			resizeArray(Math.max(data.length * 2, 8));
		}
		
		data[used++] = x;
		data[used++] = y;
	}
	
	public void add(Point toAdd) {
		add(toAdd.x, toAdd.y);
	}
	
	public Point[] asPointArray() {
		Point[] result = new Point[used / 2];
		int idx = 0;
		for (int i = 0; i < result.length; i++) {
			int x = data[idx++];
			int y = data[idx++];
			
			result[i] = new Point(x, y);
		}
		
		return result;
	}
	
	private void resizeArray(int newSize) {
		int[] newData = new int[newSize];
		System.arraycopy(data, 0, newData, 0, used);
		data = newData;
	}
	
	public int[] getData() {
		if (used < data.length) {
			resizeArray(used);
		}
		
		return data;
	}
}
