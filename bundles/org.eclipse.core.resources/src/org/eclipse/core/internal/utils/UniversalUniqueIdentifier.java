/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.utils;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.GregorianCalendar;
import java.util.Random;
import org.eclipse.core.runtime.Assert;
import org.eclipse.osgi.util.NLS;

public class UniversalUniqueIdentifier implements java.io.Serializable {

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/* INSTANCE FIELDS =============================================== */

	private byte[] fBits = new byte[BYTES_SIZE];

	/* NON-FINAL PRIVATE STATIC FIELDS =============================== */

	private static BigInteger fgPreviousClockValue;
	private static int fgClockAdjustment = 0;
	private static int fgClockSequence = -1;
	private static byte[] nodeAddress;

	static {
		nodeAddress = computeNodeAddress();
	}

	/* PRIVATE STATIC FINAL FIELDS =================================== */

	private static Random fgRandomNumberGenerator = new Random();

	/* PUBLIC STATIC FINAL FIELDS ==================================== */

	public static final int BYTES_SIZE = 16;
	public static final byte[] UNDEFINED_UUID_BYTES = new byte[16];
	public static final int MAX_CLOCK_SEQUENCE = 0x4000;
	public static final int MAX_CLOCK_ADJUSTMENT = 0x7FFF;
	public static final int TIME_FIELD_START = 0;
	public static final int TIME_FIELD_STOP = 6;
	public static final int TIME_HIGH_AND_VERSION = 7;
	public static final int CLOCK_SEQUENCE_HIGH_AND_RESERVED = 8;
	public static final int CLOCK_SEQUENCE_LOW = 9;
	public static final int NODE_ADDRESS_START = 10;
	public static final int NODE_ADDRESS_STOP = 13;
	public static final int HOST_ADDRESS_BYTE_SIZE = 4;
	public static final int NODE_ADDRESS_BYTE_SIZE = 6;

	public static final int BYTE_MASK = 0xFF;

	public static final int MOST_SIGINIFICANT_BIT_MASK = 0x80;

	public static final int MOST_SIGNIFICANT_TWO_BITS_MASK = 0xC0;

	public static final int MOST_SIGNIFICANT_THREE_BITS_MASK = 0xE0;

	public static final int HIGH_NIBBLE_MASK = 0xF0;

	public static final int LOW_NIBBLE_MASK = 0x0F;

	public static final int SHIFT_NIBBLE = 4;

	public static final int ShiftByte = 8;
	public static final int PrintStringSize = 32;
//	public static final int[] PrintStringDashPositions = new int[] {8, 13, 18, 23};
//	public static final char[] ValidPrintStringCharacters = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', '-'};

	/**
	 UniversalUniqueIdentifier default constructor returns a
	 new instance that has been initialized to a unique value.
	 */
	public UniversalUniqueIdentifier() {
		this.setVersion(1);
		this.setVariant(1);
		this.setTimeValues();
		this.setNode(getNodeAddress());
	}

	/**
	 Constructor that accepts the bytes to use for the instance.&nbsp;&nbsp; The format
	 of the byte array is compatible with the <code>toBytes()</code> method.

	 <p>The constructor returns the undefined uuid if the byte array is invalid.

	 @see #toBytes()
	 @see #BYTES_SIZE
	 */
	public UniversalUniqueIdentifier(byte[] byteValue) {
		fBits = new byte[BYTES_SIZE];
		if (byteValue.length >= BYTES_SIZE)
			System.arraycopy(byteValue, 0, fBits, 0, BYTES_SIZE);
	}

	/**
	 Construct an instance whose internal representation is defined by the given string.  
	 The format of the string is that of the <code>toString()</code> instance method.

	 <p>It is useful to compare the <code>fromHex(String)</code> method in the
	 Java class <code>HexConverter</code>.

	 @see       #toString()
	 */
	public UniversalUniqueIdentifier(String string) {
		// Check to ensure it is a String of the right length.
		// do not use Assert to avoid having to call Policy.bind ahead of time
		if (string.length() != PrintStringSize)
			Assert.isTrue(false, NLS.bind(Messages.utils_wrongLength, string));

		char[] newChars = string.toCharArray();

		// Convert to uppercase.
		for (int i = 0; i < newChars.length; i++)
			newChars[i] = Character.toUpperCase(newChars[i]);

		// LoadUp a new instance.
		for (int i = 0; i < BYTES_SIZE; i++) {
			int characterOffset = i * 2;
			int hi = Character.digit(newChars[characterOffset], 16);
			int lo = Character.digit(newChars[characterOffset + 1], 16);
			fBits[i] = new Integer((hi * 16 + lo)).byteValue();
		}
	}

	private void appendByteString(StringBuffer buffer, byte value) {
		String hexString;

		if (value < 0)
			hexString = Integer.toHexString(256 + value);
		else
			hexString = Integer.toHexString(value);
		if (hexString.length() == 1)
			buffer.append("0"); //$NON-NLS-1$
		buffer.append(hexString);
	}

	private static BigInteger clockValueNow() {
		GregorianCalendar now = new GregorianCalendar();
		BigInteger nowMillis = BigInteger.valueOf(now.getTime().getTime());
		BigInteger baseMillis = BigInteger.valueOf(now.getGregorianChange().getTime());

		return (nowMillis.subtract(baseMillis).multiply(BigInteger.valueOf(10000L)));
	}

	/**
	 Simply increases the visibility of <code>Object</code>'s clone.
	 Otherwise, no new behaviour.
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			Assert.isTrue(false, Messages.utils_clone);
			return null;
		}
	}

	public static int compareTime(byte[] fBits1, byte[] fBits2) {
		for (int i = TIME_FIELD_STOP; i >= 0; i--) 
			if (fBits1[i] != fBits2[i])
				return (0xFF & fBits1[i]) - (0xFF & fBits2[i]);		
		return 0;
	}		

	/**
	 * Answers the node address attempting to mask the IP
	 * address of this machine.
	 * 
	 * @return byte[] the node address
	 */
	private static byte[] computeNodeAddress() {

		byte[] address = new byte[NODE_ADDRESS_BYTE_SIZE];

		// Seed the secure randomizer with some oft-varying inputs
		int thread = Thread.currentThread().hashCode();
		long time = System.currentTimeMillis();
		int objectId = System.identityHashCode(new String());
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);
		byte[] ipAddress = getIPAddress();

		try {
			if (ipAddress != null)
				out.write(ipAddress);
			out.write(thread);
			out.writeLong(time);
			out.write(objectId);
			out.close();
		} catch (IOException exc) {
			//ignore the failure, we're just trying to come up with a random seed
		}
		byte[] rand = byteOut.toByteArray();

		SecureRandom randomizer = new SecureRandom(rand);
		randomizer.nextBytes(address);

		// set the MSB of the first octet to 1 to distinguish from IEEE node addresses
		address[0] = (byte) (address[0] | (byte) 0x80);

		return address;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof UniversalUniqueIdentifier))
			return false;

		byte[] other = ((UniversalUniqueIdentifier) obj).fBits;
		if (fBits == other)
			return true;
		if (fBits.length != other.length)
			return false;
		for (int i = 0; i < fBits.length; i++) {
			if (fBits[i] != other[i])
				return false;
		}
		return true;
	}

	/**
	 Answers the IP address of the local machine using the
	 Java API class <code>InetAddress</code>.

	 @return byte[] the network address in network order
	 @see    java.net.InetAddress#getLocalHost()
	 @see    java.net.InetAddress#getAddress()
	 */
	protected static byte[] getIPAddress() {
		try {
			return InetAddress.getLocalHost().getAddress();
		} catch (UnknownHostException e) {
			//valid for this to be thrown be a machine with no IP connection
			//It is VERY important NOT to throw this exception
			return null;
		}
	}

	public byte[] getNode() {
		byte[] nodeValue = new byte[HOST_ADDRESS_BYTE_SIZE];

		System.arraycopy(fBits, NODE_ADDRESS_START, nodeValue, 0, HOST_ADDRESS_BYTE_SIZE);
		return nodeValue;
	}

	private static byte[] getNodeAddress() {
		return nodeAddress;
	}

	public int getVariant() {
		byte flags = fBits[CLOCK_SEQUENCE_HIGH_AND_RESERVED];

		if ((flags & MOST_SIGINIFICANT_BIT_MASK) == 0) {
			// HP/Apollo NCS 1.x and DEC RPC Version 1.
			return (0);
		}

		if ((flags & MOST_SIGNIFICANT_TWO_BITS_MASK) == MOST_SIGINIFICANT_BIT_MASK) {
			// HP/DEC OSF 'DEC UID Architecture Functional Specification Version X1.0.4' from NCS 2.0.
			return (1);
		}

		if ((flags & MOST_SIGNIFICANT_THREE_BITS_MASK) == MOST_SIGNIFICANT_TWO_BITS_MASK) {
			// Microsoft GUID
			return (2);
		}

		if ((flags & MOST_SIGNIFICANT_THREE_BITS_MASK) == MOST_SIGNIFICANT_THREE_BITS_MASK) {
			// reserved
			return (3);
		}

		// unknown
		return (-1);
	}

	public int hashCode() {
		return fBits[0] + fBits[3] + fBits[7] + fBits[11] + fBits[15];
	}

	/**
	 * Tests to see if the receiver is anonymous.
	 * <p>
	 * If the receiver was constrcted from a string or bytes it
	 * may not really be anonymous.<p>
	 *
	 * @return boolean true if the receiver is anonymous
	 */
	public boolean isAnonymous() {
		return isUndefined() || ((getNode()[0] & MOST_SIGINIFICANT_BIT_MASK) == MOST_SIGINIFICANT_BIT_MASK);
	}

	public boolean isUndefined() {
		return this.equals(UniversalUniqueIdentifier.newUndefined());
	}

	/**
	 Returns a new instance of <code>UniversalUniqueIdentifier</code>
	 that represents the single undefined state.

	 <p>The undefined <code>UniversalUniqueIdentifier</code> is used
	 to represent cases where a unique identifier is not required.

	 @return the undefined instance of <code>UniversalUniqueIdentifier</code>
	 */
	public static UniversalUniqueIdentifier newUndefined() {
		return new UniversalUniqueIdentifier(UNDEFINED_UUID_BYTES);
	}

	private static int nextClockSequence() {

		if (fgClockSequence == -1)
			fgClockSequence = (int) (fgRandomNumberGenerator.nextDouble() * MAX_CLOCK_SEQUENCE);

		fgClockSequence = (fgClockSequence + 1) % MAX_CLOCK_SEQUENCE;

		return fgClockSequence;
	}

	private static BigInteger nextTimestamp() {

		BigInteger timestamp = clockValueNow();
		int timestampComparison;

		timestampComparison = timestamp.compareTo(fgPreviousClockValue);

		if (timestampComparison == 0) {
			if (fgClockAdjustment == MAX_CLOCK_ADJUSTMENT) {
				while (timestamp.compareTo(fgPreviousClockValue) == 0)
					timestamp = clockValueNow();
				timestamp = nextTimestamp();
			} else
				fgClockAdjustment++;
		} else {
			fgClockAdjustment = 0;

			if (timestampComparison < 0)
				nextClockSequence();
		}

		return timestamp;
	}

	private void setClockSequence(int clockSeq) {
		int clockSeqHigh = (clockSeq >>> ShiftByte) & LOW_NIBBLE_MASK;
		int reserved = fBits[CLOCK_SEQUENCE_HIGH_AND_RESERVED] & HIGH_NIBBLE_MASK;

		fBits[CLOCK_SEQUENCE_HIGH_AND_RESERVED] = (byte) (reserved | clockSeqHigh);
		fBits[CLOCK_SEQUENCE_LOW] = (byte) (clockSeq & BYTE_MASK);
	}

	protected void setNode(byte[] bytes) {

		for (int index = 0; index < NODE_ADDRESS_BYTE_SIZE; index++)
			fBits[index + NODE_ADDRESS_START] = bytes[index];
	}

	private void setTimestamp(BigInteger timestamp) {
		BigInteger value = timestamp;
		BigInteger bigByte = BigInteger.valueOf(256L);
		BigInteger[] results;
		int version;
		int timeHigh;

		for (int index = TIME_FIELD_START; index < TIME_FIELD_STOP; index++) {
			results = value.divideAndRemainder(bigByte);
			value = results[0];
			fBits[index] = (byte) results[1].intValue();
		}
		version = fBits[TIME_HIGH_AND_VERSION] & HIGH_NIBBLE_MASK;
		timeHigh = value.intValue() & LOW_NIBBLE_MASK;
		fBits[TIME_HIGH_AND_VERSION] = (byte) (timeHigh | version);
	}

	protected synchronized void setTimeValues() {
		this.setTimestamp(timestamp());
		this.setClockSequence(fgClockSequence);
	}

	protected int setVariant(int variantIdentifier) {
		int clockSeqHigh = fBits[CLOCK_SEQUENCE_HIGH_AND_RESERVED] & LOW_NIBBLE_MASK;
		int variant = variantIdentifier & LOW_NIBBLE_MASK;

		fBits[CLOCK_SEQUENCE_HIGH_AND_RESERVED] = (byte) ((variant << SHIFT_NIBBLE) | clockSeqHigh);
		return (variant);
	}

	protected void setVersion(int versionIdentifier) {
		int timeHigh = fBits[TIME_HIGH_AND_VERSION] & LOW_NIBBLE_MASK;
		int version = versionIdentifier & LOW_NIBBLE_MASK;

		fBits[TIME_HIGH_AND_VERSION] = (byte) (timeHigh | (version << SHIFT_NIBBLE));
	}

	private static BigInteger timestamp() {
		BigInteger timestamp;

		if (fgPreviousClockValue == null) {
			fgClockAdjustment = 0;
			nextClockSequence();
			timestamp = clockValueNow();
		} else
			timestamp = nextTimestamp();

		fgPreviousClockValue = timestamp;
		return fgClockAdjustment == 0 ? timestamp : timestamp.add(BigInteger.valueOf(fgClockAdjustment));
	}

	/** 
	 This representation is compatible with the (byte[]) constructor.

	 @see #UniversalUniqueIdentifier(byte[])
	 */
	public byte[] toBytes() {
		byte[] result = new byte[fBits.length];

		System.arraycopy(fBits, 0, result, 0, fBits.length);
		return result;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < fBits.length; i++)
			appendByteString(buffer, fBits[i]);
		return buffer.toString();
	}

	public String toStringAsBytes() {
		String result = "{"; //$NON-NLS-1$

		for (int i = 0; i < fBits.length; i++) {
			result += fBits[i];
			if (i < fBits.length + 1)
				result += ","; //$NON-NLS-1$
		}
		return result + "}"; //$NON-NLS-1$
	}
}
