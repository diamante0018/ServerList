/*
 * Copyright (C) 2025 Diamante
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Diamante
 */
public class MasterServerPinger {

    private static final String OFFICIAL_MASTER = "mw3.totalkillaz.ovh";
    private static final String LAN_MASTER = "127.0.0.1";
    private static final int MASTER_PORT = 27017;

    private Socket clientSocket;

    public void pingMaster() {
        try {
            clientSocket = new Socket(OFFICIAL_MASTER, MASTER_PORT);
        }
        catch (IOException ex) {
            System.err.println("IOException: Failed to open a socket");
            return;
        }

        try {
            var output = clientSocket.getOutputStream();
            var data = new byte[8];

            var magicLE = Utils.longSwap(Utils.NEW_CLIENT_MAGIC);
            var versionLE = Utils.longSwap(Utils.CLIENT_VERSION);

            System.arraycopy(magicLE, 0, data, 0, 4);
            System.arraycopy(versionLE, 0, data, 4, 4);

            output.write(data);
        }
        catch (IOException ex) {
            System.err.println("IOException: Failed to write to a socket");
        }
    }

    public void readReplyFromMaster() {
        var out = new ByteArrayOutputStream();

        try {
            System.out.println("readReplyFromMaster: awaiting reply from master server");
            var input = clientSocket.getInputStream();
            System.out.println("readReplyFromMaster: finished waiting for a reply from master server");
            var bytes = new byte[0x1000 * 0x6 + 0x4];

            int count = input.read(bytes);
            out.write(bytes, 0, count);

            System.out.println("readReplyFromMaster: finished reading bytes from socket");
        }
        catch (IOException ex) {
            System.err.println("IOException: Failed to read from a socket");
        }

        if (out.size() == 0) {
            System.out.println("readReplyFromMaster: got no reply");

            try {
                out.close();
            }
            catch (IOException ex) {
                System.err.println("readReplyFromMaster: IOException in out.close()");
            }

            return;
        }

        var bytes = out.toByteArray();

        var serverCountLE = new byte[4];

        System.arraycopy(bytes, 0, serverCountLE, 0, 4);

        var serverCountBE = Utils.longSwap(serverCountLE);

        System.out.println(String.format("readReplyFromMaster: got %d servers", serverCountBE));

        var root = new JSONObject();
        var serverArray = new JSONArray();

        // Process server data
        for (int i = 4; i < bytes.length; i += 6) {
            if (i + 6 > bytes.length) {
                System.err.println("readReplyFromMaster: Incomplete server data detected");
                break;
            }

            byte[] ipBytesLE = new byte[4];
            System.arraycopy(bytes, i, ipBytesLE, 0, 4);
            var ipBytesBE = Utils.bytesToInt(ipBytesLE);

            var ipAddress = Utils.bytesToIP(ipBytesBE);

            var portBytesLE = new byte[2];
            System.arraycopy(bytes, i + 4, portBytesLE, 0, 2);
            var port = Utils.shortSwap(portBytesLE);

            System.out.println(String.format("Server: %s:%d", ipAddress, port));

            var serverObject = new JSONObject();
            serverObject.put("IP", ipAddress);
            serverObject.put("port", port);

            serverArray.add(serverObject);
        }

        root.put("totalServers", serverCountBE);
        root.put("servers", serverArray);

        Utils.saveJSONFile(String.format("server_dump_%d.json", System.currentTimeMillis() / 1000L), root);

        try {
            out.close();
        }
        catch (IOException ex) {
            System.err.println("readReplyFromMaster: IOException in out.close()");
        }
    }
}
