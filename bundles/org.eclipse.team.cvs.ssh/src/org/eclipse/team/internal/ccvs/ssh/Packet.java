package org.eclipse.team.internal.ccvs.ssh;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
class Packet {
	protected int packetLength;
	protected int paddingLength;
	protected int packetType;
public int getType() {
	return packetType;
}
}
