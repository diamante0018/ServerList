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

        try {
            out.close();
        }
        catch (IOException ex) {
            System.err.println("readReplyFromMaster: IOException in out.close()");
        }
    }
}
