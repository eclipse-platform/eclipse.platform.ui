package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * This class loads translatable strings from a resource bundle properties file.
 */
import java.util.ResourceBundle;
import java.util.MissingResourceException;

public class BootUpdateManagerStrings {
	private static ResourceBundle _resourceBundle = null;
/**
 * Obtains a NL translated string from a resource bundle
 */
public static String getString( String strKey ) 
{
	String strTranslated = strKey;

	// Obtain the set of translated strings
	//-------------------------------------
	if( _resourceBundle == null )
	{
		try
		{
			_resourceBundle = ResourceBundle.getBundle( "org.eclipse.core.internal.boot.update.BootUpdateManagerStrings" );
		}
		catch( MissingResourceException ex )
		{
			_resourceBundle = null;
		}
	}

	// Obtain the translated string
	//-----------------------------
	if( _resourceBundle != null )
	{
		try
		{
			strTranslated = _resourceBundle.getString( strKey );
		}
		catch( MissingResourceException ex )
		{
			strTranslated = strKey;
		}
	}

	return strTranslated;
}
}
