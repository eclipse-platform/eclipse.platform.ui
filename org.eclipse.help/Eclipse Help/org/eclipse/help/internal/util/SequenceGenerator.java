package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * Generates incremental numbers for different contribution ids
 */
public class SequenceGenerator {
	protected static final SequenceGenerator instance = new SequenceGenerator();
	protected long sequence = 0;
	/**
	 * SequenceGenerator constructor comment.
	 */
	protected SequenceGenerator() {
		super();
	}
	/**
	 * @return com.ibm.itp.help.util.SequenceGenerator
	 */
	public static SequenceGenerator getDefaultGenerator() {
		return instance;
	}
	/**
	 * @return com.ibm.itp.help.util.SequenceGenerator
	 */
	public static SequenceGenerator getNewGenerator() {
		return new SequenceGenerator();
	}
	/**
	 * @return long
	 */
	public synchronized long getNext() {
		return ++sequence;
	}
	/**
	 * @return long
	 */
	public synchronized static long next() {
		return ++instance.sequence;
	}
}
