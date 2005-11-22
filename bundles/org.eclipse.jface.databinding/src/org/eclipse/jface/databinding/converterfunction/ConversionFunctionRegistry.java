/*
 * Copyright (C) 2005 db4objects Inc.  http://www.db4o.com
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 */
package org.eclipse.jface.databinding.converterfunction;

import java.util.Date;
import java.util.HashMap;

import org.eclipse.jface.databinding.converterfunctions.ConvertDate2String;
import org.eclipse.jface.databinding.converterfunctions.ConvertObject2String;
import org.eclipse.jface.databinding.converterfunctions.ConvertString2Boolean;
import org.eclipse.jface.databinding.converterfunctions.ConvertString2Byte;
import org.eclipse.jface.databinding.converterfunctions.ConvertString2Character;
import org.eclipse.jface.databinding.converterfunctions.ConvertString2Date;
import org.eclipse.jface.databinding.converterfunctions.ConvertString2Double;
import org.eclipse.jface.databinding.converterfunctions.ConvertString2Float;
import org.eclipse.jface.databinding.converterfunctions.ConvertString2Integer;
import org.eclipse.jface.databinding.converterfunctions.ConvertString2Long;
import org.eclipse.jface.databinding.converterfunctions.ConvertString2Object;
import org.eclipse.jface.databinding.converterfunctions.ConvertString2Short;
import org.eclipse.jface.databinding.converterfunctions.TheIdentityFunction;
import org.eclipse.jface.databinding.converterfunctions.TheNullStringFunction;
import org.eclipse.jface.databinding.converterfunctions.ToStringConverter;


/**
 * ConversionFunctionRegistry.  The place where all converters can be found.
 *
 * @author djo
 */
public class ConversionFunctionRegistry {
	private static HashMap converterFunctions;
    
    /*
     * Returns the set of converters to convert from a specified source class
     */
    private static HashMap getSourceClassConverters(Class sourceClass) {
        HashMap result = (HashMap) converterFunctions.get(sourceClass);
        
        if (result == null) {
            result = new HashMap();
            converterFunctions.put(sourceClass, result);
        }
        
        return result;
    }
    
    /**
     * Associate a particular converter with a particular pair of classes.
     * 
     * @param sourceClass The type to convert from
     * @param destClass The type to convert to
     * @param conversionFunction The IConversionFunction
     */
    public static void associate(Class sourceClass, Class destClass, IConversionFunction conversionFunction) {
        HashMap sourceClassConverters = getSourceClassConverters(sourceClass);
        sourceClassConverters.put(destClass, conversionFunction);
    }
    
    /**
     * Return an IConversionFunction for a specific class2class conversion.
     * 
     * @param sourceClass The source class
     * @param destClass The destination class
     * @return An appropriate IConversionFunction or TheNullConverter.NULL on error
     */
    public static IConversionFunction get(Class sourceClass, Class destClass) {
        if (sourceClass.equals(destClass))
            return TheIdentityFunction.IDENTITY;
        
        HashMap sourceClassConverters = (HashMap) converterFunctions.get(sourceClass);
        
        if (sourceClassConverters == null) {
            System.err.println("No converters for pair (" + sourceClass + ", " + destClass + ") have been registered"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return TheNullStringFunction.NULL;
        }
        
        IConversionFunction result = (IConversionFunction) sourceClassConverters.get(destClass);
        
        if (result == null) {
        	System.err.println("No converters for pair (" + sourceClass + ", " + destClass + ") have been registered"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return TheNullStringFunction.NULL;
        }
        
        return result;
    }
    
    /**
     * Returns if there is a converter registered for a specific class2class
     * conversion.
     *  
     * @param sourceClass
     * @param destClass
     * @return true if a converter is registered; false otherwise.
     */
    public static boolean canConvert(Class sourceClass, Class destClass) {
        if (sourceClass.equals(destClass)) {
            return true;
        }
        
        HashMap sourceClassConverters = (HashMap) converterFunctions.get(sourceClass);
        
        if (sourceClassConverters == null) {
            return false;
        }
        
        IConversionFunction result = (IConversionFunction) sourceClassConverters.get(destClass);
        
        if (result == null) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Returns if the pair (sourceClass, destClass) can be converted in both directions.
     * 
     * @param sourceClass the source Class 
     * @param destClass the destination Class
     * @return true if the pair can be converted in both directions; false otherwise.
     */
    public static boolean canConvertPair(Class sourceClass, Class destClass) {
    	if (canConvert(sourceClass, destClass) && canConvert(destClass, sourceClass)) {
    		return true;
    	}
    	return false;
    }
    
    /*
     * Register converters for built-in Java types
     */
    static {
        converterFunctions = new HashMap();
        
        // Boxing/unboxing
        associate(Object.class, String.class, new ConvertObject2String());
        associate(String.class, Object.class, new ConvertString2Object());
        
        associate(Character.TYPE, Character.class, TheIdentityFunction.IDENTITY);
        associate(Character.class, Character.TYPE, TheIdentityFunction.IDENTITY);
        
        associate(Boolean.TYPE, Boolean.class, TheIdentityFunction.IDENTITY);
        associate(Boolean.class, Boolean.TYPE, TheIdentityFunction.IDENTITY);
        
        associate(Integer.TYPE, Integer.class, TheIdentityFunction.IDENTITY);
        associate(Integer.class, Integer.TYPE, TheIdentityFunction.IDENTITY);
        
        associate(Byte.TYPE, Byte.class, TheIdentityFunction.IDENTITY);
        associate(Byte.class, Byte.TYPE, TheIdentityFunction.IDENTITY);
        
        associate(Short.TYPE, Short.class, TheIdentityFunction.IDENTITY);
        associate(Short.class, Short.TYPE, TheIdentityFunction.IDENTITY);
        
        associate(Long.TYPE, Long.class, TheIdentityFunction.IDENTITY);
        associate(Long.class, Long.TYPE, TheIdentityFunction.IDENTITY);
        
        associate(Float.TYPE, Float.class, TheIdentityFunction.IDENTITY);
        associate(Float.class, Float.TYPE, TheIdentityFunction.IDENTITY);
        
        // String to/from primitive
        associate(Character.TYPE, String.class, ToStringConverter.TOSTRINGFUNCTION);
        associate(String.class, Character.TYPE, new ConvertString2Character());

        associate(Boolean.TYPE, String.class, new ToStringConverter());
        associate(String.class, Boolean.TYPE, new ConvertString2Boolean());
        
        associate(Integer.TYPE, String.class, ToStringConverter.TOSTRINGFUNCTION);
        associate(String.class, Integer.TYPE, new ConvertString2Integer());
        
        associate(Byte.TYPE, String.class, ToStringConverter.TOSTRINGFUNCTION);
        associate(String.class, Byte.TYPE, new ConvertString2Byte());
        
        associate(Short.TYPE, String.class, ToStringConverter.TOSTRINGFUNCTION);
        associate(String.class, Short.TYPE, new ConvertString2Short());
        
        associate(Long.TYPE, String.class, ToStringConverter.TOSTRINGFUNCTION);
        associate(String.class, Long.TYPE, new ConvertString2Long());
        
        associate(Float.TYPE, String.class, ToStringConverter.TOSTRINGFUNCTION);
        associate(String.class, Float.TYPE, new ConvertString2Float());
        
        associate(Double.TYPE, String.class, ToStringConverter.TOSTRINGFUNCTION);
        associate(String.class, Double.TYPE, new ConvertString2Double());
        
        // String to/from boxed types
        associate(Boolean.class, String.class, ToStringConverter.TOSTRINGFUNCTION);
        associate(String.class, Boolean.class, new ConvertString2Boolean());
        
        associate(Integer.class, String.class, ToStringConverter.TOSTRINGFUNCTION);
        associate(String.class, Integer.class, new ConvertString2Integer());
        
        associate(Byte.class, String.class, ToStringConverter.TOSTRINGFUNCTION);
        associate(String.class, Byte.class, new ConvertString2Byte());
        
        associate(Short.class, String.class, ToStringConverter.TOSTRINGFUNCTION);
        associate(String.class, Short.class, new ConvertString2Short());
        
        associate(Long.class, String.class, ToStringConverter.TOSTRINGFUNCTION);
        associate(String.class, Long.class, new ConvertString2Long());
        
        associate(Float.class, String.class, ToStringConverter.TOSTRINGFUNCTION);
        associate(String.class, Float.class, new ConvertString2Float());
        
        associate(Double.class, String.class, ToStringConverter.TOSTRINGFUNCTION);
        associate(String.class, Double.class, new ConvertString2Double());
        
        associate(Date.class, String.class, new ConvertDate2String());
        associate(String.class, Date.class, new ConvertString2Date());
    }
}


