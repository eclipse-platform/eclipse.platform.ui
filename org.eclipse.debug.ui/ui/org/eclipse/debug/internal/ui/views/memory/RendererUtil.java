/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import java.math.BigInteger;


/**
 * Util functions for data conversions
 */
public class RendererUtil {
	
	public static final int LITTLE_ENDIAN = 0;
	public static final int BIG_ENDIAN = 1;

	/**
	 * Pad byte array with zero's with the byte array's length
	 * is shorter that what's expected the conversion functions.
	 * @param array
	 * @param size
	 * @param endianess
	 * @return an array of bytes
	 */
	protected static byte[] fillArray(byte[] array, int size, int endianess)
	{
		if (endianess == RendererUtil.LITTLE_ENDIAN)
		{
			byte[] temp = new byte[size];
			
			for (int i=0; i<array.length; i++)
			{
				temp[i] = array[i];
			}
			
			// fill up the rest of the array
			for (int i=array.length; i<size; i++)
			{
				temp[i] = 0;
			}
			
			array = temp;
			return array;
		}
        byte[] temp = new byte[size];
        
        for (int i=0; i<size - array.length; i++)
        {
        	temp[i] = 0;
        }
        
        int j=0;
        // fill up the rest of the array
        for (int i=size - array.length; i<size; i++)
        {
        	temp[i] = array[j];
        	j++;
        }
        
        array = temp;
        return array;
	}
	
	static public BigInteger convertByteArrayToUnsignedLong(byte[] array, int endianess)
	{
		if (array.length < 8)
		{
			array = fillArray(array, 8, endianess);
		}
		
		BigInteger value = new BigInteger("0"); //$NON-NLS-1$
		if (endianess == RendererUtil.LITTLE_ENDIAN)
		{
			for (int i=0; i< 8; i++)
			{
				byte[] temp = new byte[1];
				temp[0] = array[i];
				BigInteger b = new BigInteger(temp);
				b = b.and(new BigInteger("ff", 16)); //$NON-NLS-1$
				b = b.shiftLeft(i*8);
				value = value.or(b);
			}			
		}
		else
		{	
			for (int i=0; i< 8; i++)
			{
				byte[] temp = new byte[1];
				temp[0] = array[i];
				BigInteger b = new BigInteger(temp);
				b = b.and(new BigInteger("ff", 16)); //$NON-NLS-1$
				b = b.shiftLeft((7-i)*8);
				value = value.or(b);
			}
		}
		return value;
	}
	
	/**
	 * Convert byte array to long.
	 * @param array
	 * @param endianess
	 * @return result of the conversion in long
	 */
	static public long convertByteArrayToLong(byte[] array, int endianess)
	{	
		if (array.length < 8)
		{
			array = fillArray(array, 8, endianess);
		}
		
		if (endianess == RendererUtil.LITTLE_ENDIAN)
		{	
			long value = 0;
			for (int i = 0; i < 8; i++) {
				long b = array[i];
				b &= 0xff;
				value |= (b << (i * 8));
			}
			return value;
		}
        long value = 0;
        for (int i=0; i< 8; i++)
        {
        	long b = array[i];
        	b &= 0xff;
        	value |= (b<<((7-i)*8));
        }
        
        return value;
	}
	
	static public BigInteger convertByteArrayToBigInteger(byte[] array, int endianess)
	{	
		if (array.length < 16)
		{
			array = fillArray(array, 16, endianess);
		}
		
		if (endianess == RendererUtil.LITTLE_ENDIAN)
		{	
			// reverse bytes
			byte[] holder = new byte[16];
			int j=15;
			for (int i=0; i<16; i++, j--)
			{	
				holder[i] = array[j];
			}
			
			// create BigInteger
			BigInteger value = new BigInteger(holder);
			return value;
		}
        BigInteger value = new BigInteger(array);
        return value;
	}
	
	static public BigInteger convertByteArrayToUnsignedBigInteger(byte[] array, int endianess)
	{
		if (array.length < 16)
		{
			array = fillArray(array, 16, endianess);
		}
		
		BigInteger value = new BigInteger("0"); //$NON-NLS-1$
		if (endianess == RendererUtil.LITTLE_ENDIAN)
		{
			for (int i=0; i< 16; i++)
			{
				byte[] temp = new byte[1];
				temp[0] = array[i];
				BigInteger b = new BigInteger(temp);
				b = b.and(new BigInteger("ff", 16)); //$NON-NLS-1$
				b = b.shiftLeft(i*8);
				value = value.or(b);
			}			
		}
		else
		{	
			for (int i=0; i< 16; i++)
			{
				byte[] temp = new byte[1];
				temp[0] = array[i];
				BigInteger b = new BigInteger(temp);
				b = b.and(new BigInteger("ff", 16)); //$NON-NLS-1$
				b = b.shiftLeft((15-i)*8);
				value = value.or(b);
			}
		}
		return value;	
	}
	
	/**
	 * Convert byte array to integer.
	 * @param array
	 * @param endianess
	 * @return result of the conversion in int
	 */
	static public int convertByteArrayToInt(byte[] array, int endianess)
	{	
		if (array.length < 4)
		{
			array = fillArray(array, 4, endianess);
		}
		
		if (endianess == RendererUtil.LITTLE_ENDIAN)
		{
			int value = 0;
			for (int i = 0; i < 4; i++) {
				int b = array[i];
				b &= 0xff;
				value |= (b << (i * 8));
			}
			return value;
		}
        int value = 0;
        for (int i=0; i< 4; i++)
        {
        	int b = array[i];
        	b &= 0xff;
        	value |= (b<<((3-i)*8));
        }
        
        return value;
	}
	
	/**
	 * Convert byte array to short.
	 * @param array
	 * @param endianess
	 * @return result of teh conversion in short
	 */
	static public short convertByteArrayToShort(byte[] array, int endianess)
	{	
		if (array.length < 2)
		{
			array = fillArray(array, 2, endianess);
		}
		
		if (endianess == RendererUtil.LITTLE_ENDIAN)
		{
			short value = 0;
			for (int i = 0; i < 2; i++) {
				short b = array[i];
				b &= 0xff;
				value |= (b << (i * 8));
			}
			return value;
		}
        short value = 0;
        for (int i=0; i< 2; i++)
        {
        	short b = array[i];
        	b &= 0xff;
        	value |= (b<<((1-i)*8));
        }
        return value;
	}
	
	/**
	 * Convert big integer to byte array.
	 * @param i
	 * @param endianess
	 * @return result of the conversion in raw byte array
	 */
	static public byte[] convertBigIntegerToByteArray(BigInteger i, int endianess)
	{
		byte buf[]=new byte[16];

		if (endianess == RendererUtil.LITTLE_ENDIAN)
		{
			for (int j=0; j<16; j++)
			{
				BigInteger x = i.shiftRight(j*8);
				buf[j] = x.byteValue();
			}
			return buf;
		}
        for (int j=15; j>=0; j--)
        {
        	BigInteger x = i.shiftRight((15-j)*8);
        	buf[j] = x.byteValue();
        }
        return buf;		
	}
	
	/**
	 * Convert big integer to byte array.
	 * @param i
	 * @param endianess
	 * @return result of the conversion in raw byte array
	 */
	static public byte[] convertUnsignedBigIntegerToByteArray(BigInteger i, int endianess)
	{
		byte buf[]=new byte[32];

		if (endianess == RendererUtil.LITTLE_ENDIAN)
		{
			for (int j=0; j<32; j++)
			{
				BigInteger x = i.shiftRight(j*8);
				buf[j] = x.byteValue();
			}
			return buf;
		}
        for (int j=31; j>=0; j--)
        {
        	BigInteger x = i.shiftRight((31-j)*8);
        	buf[j] = x.byteValue();
        }
        return buf;		
	}
	
	/**
	 * Convert long to byte array.
	 * @param i
	 * @param endianess
	 * @return result of the conversion in raw byte array
	 */
	static public byte[] convertLongToByteArray(long i, int endianess)
	{
		byte buf[]=new byte[8];

		if (endianess == RendererUtil.LITTLE_ENDIAN)
		{
			for (int j=0; j<8; j++)
			{
				buf[j] = new Long(i>>j*8).byteValue();
			}
			return buf;
		}
        for (int j=7; j>=0; j--)
        {
        	buf[j] = new Long(i>>(7-j)*8).byteValue();
        }
        return buf;
	}
	
	/**
	 * Convert integer to byte array.
	 * @param i
	 * @param endianess
	 * @return result of the conversion in raw byte array
	 */
	static public byte[] convertIntToByteArray(int i, int endianess)
	{
		byte buf[]=new byte[4];

		if (endianess == RendererUtil.LITTLE_ENDIAN)
		{
			for (int j=0; j<4; j++)
			{
				buf[j] = new Integer(i>>j*8).byteValue();
			}
			return buf;
		}
        for (int j=3; j>=0; j--)
        {
        	buf[j] = new Integer(i>>(3-j)*8).byteValue();
        }
        return buf;
	}
	
	/**
	 * Convert short to byte array.
	 * @param i
	 * @param endianess
	 * @return result of the conversion in raw byte array
	 */
	static public byte[] convertShortToByteArray(short i, int endianess)
	{
		byte buf[]=new byte[2];

		if (endianess == RendererUtil.LITTLE_ENDIAN)
		{
			for (short j=0; j<2; j++)
			{
				buf[j] = new Integer(i>>j*8).byteValue();
			}
			return buf;
		}
        for (short j=1; j>=0; j--)
        {
        	buf[j] = new Integer(i>>(1-j)*8).byteValue();
        }
        return buf;
	} 	
}
