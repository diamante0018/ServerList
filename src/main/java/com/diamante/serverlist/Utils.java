/*
 * Copyright (C) 2022 Diamante
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.diamante.serverlist;

import java.net.InetAddress;

import java.net.UnknownHostException;

import java.nio.BufferOverflowException;
import java.nio.ReadOnlyBufferException;

import java.nio.ByteBuffer;

/**
 *
 * @author Diamante
 */
public class Utils {

    public static final int BUFFER_SIZE = 512;

    public static final int PACKET_MIN_LEN = 8;
    public static final int PACKET_CLIENT_LEN = 8;
    public static final int PACKET_SERVERT_LEN = 10;

    // (Warning: Remember to take into account endianness)
    // 2023 Update: They magic was changed, client and server were switched (HELP & THEM)
    public static final int OLD_SERVER_MAGIC = 1212501072;
    public static final int NEW_SERVER_MAGIC = 1414022477;

    public static final int OLD_CLIENT_MAGIC = 1414022477;
    public static final int NEW_CLIENT_MAGIC = 1212501072;

    public static boolean isServerMagic(int magic) {
        return magic == OLD_SERVER_MAGIC;
    }

    public static boolean isClientMagic(int magic) {
        return magic == OLD_CLIENT_MAGIC;
    }

    /**
     * Flips the array around
     *
     * @param in array of in. Length must be multiple of 4
     */
    public static void swapByteArray(byte[] in) {
        assert in.length % 4 == 0;

        for (var i = 0; i < in.length; i += 4) {
            // swap 0 and 3
            byte tmp = in[i];
            in[i] = in[i + 3];
            in[i + 3] = tmp;
            // swap 1 and 2
            byte tmp2 = in[i + 1];
            in[i + 1] = in[i + 2];
            in[i + 2] = tmp2;
        }
    }

    /**
     *
     * @param in raw bytes in LE order
     * @return integer in BE
     */
    public static int longSwap(byte[] in) {
        assert in.length == 4;

        var buffer = ByteBuffer.allocate(4);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        try {
            buffer.put(in);
            return buffer.getInt(0);
        }
        catch (BufferOverflowException | ReadOnlyBufferException ex) {
            System.err.println("longSwap: BufferOverflowException or ReadOnlyBufferException");
            return -1;
        }
    }

    /**
     *
     * @param in
     * @return raw bytes in LE order
     */
    public static byte[] longSwap(int in) {
        var buffer = ByteBuffer.allocate(4);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(in);
        return buffer.array();
    }

    /**
     * @param in raw bytes in LE order
     * @return short in BE
     */
    public static short shortSwap(byte[] in) {
        assert in.length == 2;

        var buffer = ByteBuffer.allocate(2);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        try {
            buffer.put(in);
            return buffer.getShort(0);
        }
        catch (BufferOverflowException | ReadOnlyBufferException ex) {
            System.err.println("shortSwap: BufferOverflowException or ReadOnlyBufferException");
            return -1;
        }
    }

    /**
     *
     * @param in
     * @return raw bytes in LE order
     */
    public static byte[] shortSwap(short in) {
        var buffer = ByteBuffer.allocate(2);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(in);
        return buffer.array();
    }

    public static Server stringToServer(String in) {
        try {
            var parts = in.split(":");

            if (parts.length < 2) {
                return null;
            }

            return new Server(InetAddress.getByName(parts[0]), Short.parseShort(parts[1]));
        }
        catch (UnknownHostException ex) {
            return null;
        }
    }
}
