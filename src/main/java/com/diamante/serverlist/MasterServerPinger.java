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

    private static final String MASTER = "mw3.totalkillaz.ovh";
    private static final int MASTER_PORT = 27017;

    private static final int CLIENT_VERSION = 17039893;

    private Socket clientSocket;

    public void pingMaster() {
        try {
            clientSocket = new Socket(MASTER, MASTER_PORT);
        }
        catch (IOException ex) {
            System.err.println("IOException: Failed to open a socket");
            return;
        }

        try {
            var output = clientSocket.getOutputStream();
            var data = new byte[8];

            var magicLE = Utils.longSwap(Utils.NEW_CLIENT_MAGIC);
            var versionLE = Utils.longSwap(CLIENT_VERSION);

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
            var input = clientSocket.getInputStream();
            var bytes = new byte[0x1000 * 0x6 + 0x4];

            int count;
            while ((count = input.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
        }
        catch (IOException ex) {
            System.err.println("IOException: Failed to read from a socket");
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
