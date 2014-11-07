/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.examples.chart;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class Chart extends Canvas {
	private static int TREND_CAPTION_OFFSET = 25;
	
	private static final String DEFAULT_LAST_TREND_NAME = "The rest";
	
	private RGB[] chartGradient = {new RGB(255, 0, 0), new RGB(0, 255, 0)};
	
	private RGB fontColor = new RGB(0, 0, 0);
	
	private List<Trend> trends = new ArrayList<Trend>();
	
	
	public Chart(Composite parent, int style) {
		super(parent, style);		
		
		addPaintListener(new PaintListener() {
		      public void paintControl(PaintEvent e) {
		    	  int lastTrendStart = 0;
		    	  List<Trend> trends = Chart.this.trends;
		    	  
		    	  int sumOfPercens = sumTrendPercents();
		    	  if (sumOfPercens < 100) {
		    		  trends = new ArrayList<Trend> (trends);
		    		  trends.add(new Trend(DEFAULT_LAST_TREND_NAME, 100 - sumOfPercens));
		    	  }
		        
		    	  for (int i = 0; i < trends.size(); i++) {
		        	Trend trend = trends.get(i);
		        	Color color = new Color(e.display, getNextColorForGradient(i, trends.size() - 1)); 
		        			
		        	drawTrend(e.gc, trend.toString(), color, TREND_CAPTION_OFFSET, (i + 1) * TREND_CAPTION_OFFSET,
		        			lastTrendStart, percentToArc(trend.getPercent()));		       
		        	lastTrendStart += percentToArc(trend.getPercent());
		        	color.dispose();
		    	  }
		      }
		});
	}
	
	public void addTrend(Trend trend) {
		validateTrend(trend);
		trends.add(trend);
	}
	
	public void setChartGradient(RGB[] chartGradient) {
		this.chartGradient = chartGradient;
	}
	
	public void setFontColor(RGB fontColor) {
		this.fontColor = fontColor;
	}
	
	private void validateTrend(Trend trend) {
		if (trend.getName() == null || trend.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("Invalid name of trend");
		}
		if (trend.getPercent() < 0 || trend.getPercent() > 100) {
			throw new IllegalArgumentException("Invalid percent of trend");
		}
		if (sumTrendPercents() + trend.getPercent() > 100) {
			throw new IllegalArgumentException("Sum of all trend percents is greated than 100%");
		}
	}
	
	private int sumTrendPercents() {
		int result = 0;
		for (Trend trend: trends) {
			result += trend.getPercent();
		}
		return result;
	}
	
	private void drawTrend(GC gc, String name, Color color, int x, int y, int trendStart, int trendEnd) {
		Color fontColor = new Color(gc.getDevice(), this.fontColor);
		int fontHeight = gc.getFontMetrics().getHeight();
		gc.setBackground(color); 
		gc.setForeground(fontColor);
		gc.fillRectangle(x, y, fontHeight, fontHeight);
		gc.drawRectangle(x, y, fontHeight, fontHeight);
		
		gc.setBackground(getParent().getBackground());
		gc.drawText(name, x + fontHeight * 2, y);
	
		gc.setBackground(color);
		Rectangle rect = getBounds();
		Point chartSize = new Point((int) (rect.height * 0.8), (int) (rect.height * 0.8));
		gc.fillArc((rect.width - chartSize.x) / 2, (rect.height - chartSize.y) / 2, chartSize.x, chartSize.x, trendStart, trendEnd); 
		gc.drawOval((rect.width - chartSize.x) / 2, (rect.height - chartSize.y) / 2, chartSize.x - 1, chartSize.x - 1);
		
		fontColor.dispose();
	}
	
	private int percentToArc(int percent) {
		return (int) (360 * (percent / 100.0));
	}
	
	private RGB getNextColorForGradient(int trendIndex, int trendCount) {
		int redDelta = Math.round((chartGradient[1].red - chartGradient[0].red) / (float) trendCount);
		int greenDelta = Math.round((chartGradient[1].green - chartGradient[0].green) / (float) trendCount);		
		int blueDelta = Math.round((chartGradient[1].blue - chartGradient[0].blue) / (float) trendCount); 
				
		int red = chartGradient[0].red + redDelta * trendIndex;
		int blue = chartGradient[0].green + greenDelta * trendIndex;
		int green = chartGradient[0].blue + blueDelta * trendIndex; 
		
		return new RGB(Math.max(Math.min(red, 255), 0), Math.max(Math.min(blue, 255), 0), Math.max(Math.min(green, 255), 0));
	}
	
	public static class Trend {
		private String name;
		
		private int percent;
		
		public Trend(String name, int percent) {
			this.name = name;
			this.percent = percent;
		}
		
		public String getName() {
			return name;
		}
		
		public int getPercent() {
			return percent;
		}
		
		public String toString() {
			return String.format("%s - %d%%", name, percent);
		}
	}
}
