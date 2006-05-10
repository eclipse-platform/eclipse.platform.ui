/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh;

class ClientPacket extends Packet {
	byte[] packet;
public ClientPacket(int type, byte[] data, Cipher cipher) {
	packetLength = data == null ? 5 : data.length + 5;
	paddingLength = 8 - (packetLength % 8);
	packetType = type;
	packet = new byte[4 + paddingLength + packetLength];

	int packetOff = 0;
	Misc.writeInt(packetLength, packet, packetOff);
	packetOff += 4;

	if (cipher == null) {
		for (int i = 0; i < paddingLength; i++) {
			packet[packetOff++] = 0;
		}
	} else {
		Misc.random(packet, packetOff, paddingLength, false);
		packetOff += paddingLength;
	}

	packet[packetOff++] = (byte) packetType;

	if (data != null) {
		for (int i = 0; i < data.length; ++i) {
			packet[packetOff++] = data[i];
		}
	}

	long crc = Misc.crc32(packet, 4, packet.length - 8, 0);
	Misc.writeInt((int) crc, packet, packetOff);
	packetOff += 4;

	if (cipher != null) {
		cipher.encipher(packet, 4, packet, 4, packet.length - 4);
	}
}
public byte[] getBytes() {
	return packet;
}
}
