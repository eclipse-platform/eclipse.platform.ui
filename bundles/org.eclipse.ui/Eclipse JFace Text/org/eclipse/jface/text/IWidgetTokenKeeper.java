package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * A widget token keeper may require a widget token from an
 * <code>IWidgetTokenOwner</code> and release the token
 * to the owner after usage. An widget token owner may request
 * the token from the token keeper. The keeper may deny that.
 * 
 * @since 2.0
 */ 
public interface IWidgetTokenKeeper {
	
	/**
	 * Requests the widget token from this token keeper.
	 * Returns  <code>true</code> if the token is released
	 * by this token keeper. Note, the keeper must not call 
	 * <code>releaseWidgetToken(IWidgetTokenKeeper)</code>
	 * explicitly.
	 * 
	 * @param owner the token owner
	 * @return <code>true</code> if token has been released
	 * 	<code>false</code> otherwise
	 */
	boolean requestWidgetToken(IWidgetTokenOwner owner);
}
