package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * A widget token must be aquired in order to display
 * overlay information because multiple overlay information
 * displays may hide each other. This interface is intended to become
 * part of <code>ITextViewer</code>.
 * 
 * @since 2.0
 */ 
public interface IWidgetTokenOwner {
	
	/**
	 * Requests the widget token from this token owner. Returns 
	 * <code>true</code> if the token has been aquired or is
	 * already owned by the requester. This method is non-blocking.
	 * 
	 * @param requester the token requester
	 * @return <code>true</code> if requester aquires the token,
	 * 	<code>false</code> otherwise
	 */
	boolean requestWidgetToken(IWidgetTokenKeeper requester);
	
	/**
	 * The given token keeper releases the token to this
	 * token owner. If the token has previously not been held
	 * by the given token keeper, nothing happens. This
	 * method is non-blocking.
	 * 
	 * @param tokenKeeper the token keeper
	 */
	void releaseWidgetToken(IWidgetTokenKeeper tokenKeeper);
}
