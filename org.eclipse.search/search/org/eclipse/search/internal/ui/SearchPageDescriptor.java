package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.StringConverter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageScoreComputer;
import org.eclipse.search.ui.ISearchResultViewEntry;

/**
 * Proxy that represents a search page.
 */
class SearchPageDescriptor implements Comparable {

	public final static String PAGE_TAG= "page";
	private final static String ID_ATTRIBUTE= "id";
	private final static String ICON_ATTRIBUTE= "icon";
	private final static String CLASS_ATTRIBUTE= "class";
	private final static String LABEL_ATTRIBUTE= "label";
	private final static String SIZE_ATTRIBUTE= "sizeHint";
	private final static String TAB_POSITION_ATTRIBUTE= "tabPosition";
	private final static String EXTENSIONS_ATTRIBUTE= "extensions";
	
	public final static Point UNKNOWN_SIZE= new Point(SWT.DEFAULT, SWT.DEFAULT);
	
	private IConfigurationElement fElement;
	
	private static class ExtensionScorePair {
		public String extension;
		public int score;
		public ExtensionScorePair(String extension, int score) {
			this.extension= extension;
			this.score= score;
		}
	}
	private List fExtensionScorePairs;
	private int fWildcardScore= ISearchPageScoreComputer.UNKNOWN;
	

	
	/**
	 * Creates a new search page node with the given configuration element.
	 */
	public SearchPageDescriptor(IConfigurationElement element) {
		fElement= element;
	}

	/**
	 * Creates a new search page from this node.
	 */
	public ISearchPage createObject() {
		ISearchPage result= null;
		try {
			result= (ISearchPage)fElement.createExecutableExtension(CLASS_ATTRIBUTE);
		} catch (CoreException ex) {
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.createSearchPage.");
		} catch (ClassCastException ex) {
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.createSearchPage.");
			return null;
		}
		if (result != null) {
			result.setTitle(getLabel());
		}
		return result;
	}
	
	//---- XML Attribute accessors ---------------------------------------------
	
	/**
	 * Returns the page's id.
	 */
	public String getId() {
		return fElement.getAttribute(ID_ATTRIBUTE);
	}
	 
	/**
	 * Returns the page's image
	 */
	public ImageDescriptor getImage() {
		String imageName= fElement.getAttribute(ICON_ATTRIBUTE);
		if (imageName == null)
			return null;
		URL url;
		try {
			url= new URL(fElement.getDeclaringExtension().getDeclaringPluginDescriptor().getInstallURL(), imageName);
		} catch (java.net.MalformedURLException ex) {
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.createSearchPage.");
			return null;
		}
		return ImageDescriptor.createFromURL(url);
	}

	/**
	 * Returns the page's label.
	 */
	public String getLabel() {
		return fElement.getAttribute(LABEL_ATTRIBUTE);
	}
	
	/**
	 * Returns the page's preferred size
	 */
	public Point getPreferredSize() {
		return StringConverter.asPoint(
			fElement.getAttribute(SIZE_ATTRIBUTE), UNKNOWN_SIZE);
	}
	/**
	 * Returns the page's tab position relative to the other tabs.
	 * @return	the tab position or <code>Integer.MAX_VALUE</code> if not defined in
	 			the plugins.xml file
	 *
	 */
	public int getTabPosition() {
		int position= Integer.MAX_VALUE;
		String str= fElement.getAttribute(TAB_POSITION_ATTRIBUTE);
		if (str != null)
			try {
				position= Integer.parseInt(str);
		} catch (NumberFormatException ex) {
			ExceptionHandler.handle(ex, SearchPlugin.getResourceBundle(), "Search.Error.createSearchPage.");
			// position is Integer.MAX_VALUE;
		}
		return position;
	}

	/* 
	 * Implements a method from IComparable 
	 */ 
	public int compareTo(Object o) {
		int myPos= getTabPosition();
		int objsPos= ((SearchPageDescriptor)o).getTabPosition();
		if (myPos == Integer.MAX_VALUE && objsPos == Integer.MAX_VALUE || myPos == objsPos)
			return getLabel().compareTo(((SearchPageDescriptor)o).getLabel());
		else
			return myPos - objsPos;
	}
	
	//---- Suitability tests ---------------------------------------------------
	
	/**
	 * Returns the score for this page with the given input element.
	 */
	public int computeScore(Object element) {
		if (element instanceof IFile) {
			String extension= ((IFile)element).getFileExtension();
			if (extension != null)
				return getScoreForFileExtension(extension);
		} else if (element instanceof IAdaptable) {
			ISearchPageScoreComputer tester= 
				(ISearchPageScoreComputer)((IAdaptable)element).getAdapter(ISearchPageScoreComputer.class);
			if (tester != null)
				return tester.computeScore(getId(), element);	
		} else if (element instanceof ISearchResultViewEntry) {
			ISearchResultViewEntry entry= (ISearchResultViewEntry)element;
			return computeScore(entry.getSelectedMarker());
		}
		if (fWildcardScore != ISearchPageScoreComputer.UNKNOWN)
			return fWildcardScore;
			
		return ISearchPageScoreComputer.LOWEST;
	}
	
	private int getScoreForFileExtension(String extension) {
		if (fExtensionScorePairs == null)
			readExtensionScorePairs();
			
		int size= fExtensionScorePairs.size();
		for (int i= 0; i < size; i++) {
			ExtensionScorePair p= (ExtensionScorePair)fExtensionScorePairs.get(i);
			if (extension.equals(p.extension))
				return p.score;
		}
		if (fWildcardScore != ISearchPageScoreComputer.UNKNOWN)
			return fWildcardScore;
			
		return ISearchPageScoreComputer.LOWEST;	
	}
	
	private void readExtensionScorePairs() {
		fExtensionScorePairs= new ArrayList(3);
		String content= fElement.getAttribute(EXTENSIONS_ATTRIBUTE);
		StringTokenizer tokenizer= new StringTokenizer(content, ",");
		while (tokenizer.hasMoreElements()) {
			String token= tokenizer.nextToken().trim();
			int pos= token.indexOf(':');
			if (pos != -1) {
				String extension= token.substring(0, pos);
				int score= StringConverter.asInt(token.substring(pos+1), ISearchPageScoreComputer.UNKNOWN);
				if (extension.equals("*")) {
					fWildcardScore= score;
				} else {
					fExtensionScorePairs.add(new ExtensionScorePair(extension, score));
				}	
			}
		}
	}
}