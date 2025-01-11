/*
 * Copyright (C) 2023 Diamante
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.nio.BufferOverflowException;
import java.nio.ReadOnlyBufferException;
import java.nio.ByteBuffer;

import org.json.simple.JSONObject;

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
    // 2023 Update: They magic was changed
    public static final int OLD_SERVER_MAGIC = 1212501072; // HELP

    public static final int OLD_CLIENT_MAGIC = 1414022477; // THEM
    public static final int NEW_CLIENT_MAGIC = 1129268293;

    public static final int CLIENT_VERSION = 17039893;

    public static boolean isServerMagic(int magic) {
        return magic == OLD_SERVER_MAGIC;
    }

    public static boolean isClientMagic(int magic) {
        return magic == OLD_CLIENT_MAGIC;
    }

    public static int bytesToInt(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("Array must contain exactly 4 bytes.");
        }

        // Combine the bytes into an int (assuming the bytes are in Big-Endian order)
        int result = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16)
                | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);

        return result;
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

    public static String bytesToIP(int in) {
        String ipAddress = String.format(
                "%d.%d.%d.%d",
                (in & 0xff),
                (in >> 8 & 0xff),
                (in >> 16 & 0xff),
                (in >> 24 & 0xff)
        );

        return ipAddress;
    }

    public static void saveJSONFile(String fileName, JSONObject obj) {
        try {
            var writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(obj.toJSONString());
            writer.close();
        }
        catch (IOException ex) {
            System.err.println("saveJSONFile: IOException while writing a JSON file");
        }
    }
}
