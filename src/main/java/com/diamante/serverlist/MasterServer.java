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

import java.net.Socket;
import java.net.ServerSocket;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import java.util.Arrays;

import java.io.IOException;

/**
 *
 * @author Diamante
 */
public class MasterServer {

    public static final int PORT = 27017;

    private ServerSocket socket;

    private boolean valid;

    private final ServerList serverList;

    public MasterServer() {
        serverList = new ServerList();

        try {
            socket = new ServerSocket(PORT);
            valid = true;
        } catch (IOException ex) {
            System.err.println(String.format("Socket creation on port %d failed", PORT));
            valid = false;
        }
    }

    private void handlePacket(Socket from, ByteArrayOutputStream packetData) {

        if (packetData.size() < Utils.PACKET_MIN_LEN) {
            System.out.println(String.format("handlePacket: packetData.size() is less than %d bytes", Utils.PACKET_MIN_LEN));
            return;
        }

        var blob = packetData.toByteArray();

        var magicLE = Arrays.copyOfRange(blob, 0, 4);
        var magicBE = Utils.longSwap(magicLE);

        var versionLE = Arrays.copyOfRange(blob, 4, 8);
        var versionBE = Utils.longSwap(versionLE);

        if (Utils.isServerMagic(magicBE)) {
            System.out.println("handlePacket: magic is of type server");

            if (packetData.size() < 10) {
                System.out.println("handlePacket: server packet is less than 10 bytes");
                return;
            }

            var portLE = Arrays.copyOfRange(blob, 8, 10);
            var portBE = Utils.shortSwap(portLE);
            System.out.println(String.format("handlePacket: server %s has net_port %d", from.getInetAddress(), portBE));

            var server = new Server(from.getInetAddress(), portBE, versionBE);
            serverList.addServer(server);

        } else if (Utils.isClientMagic(magicBE)) {
            System.out.println("handlePacket: magic is of type client");

            serverList.removeInactive();
            var toSend = serverList.createResponse(versionBE);

            try {
                var out = new DataOutputStream(from.getOutputStream());
                out.write(toSend);

                // Clean things up
                out.close();
            } catch (IOException ex) {
                System.err.println("handlePacket: IOException in DataOutputStream(from.getOutputStream())");
            }
        } else {
            System.out.println("handlePacket: magic is not recognized");
        }
    }

    public void await() {
        Socket worker;
        InputStream in;

        try {
            worker = socket.accept();
            System.out.println("Accepted a connection");
        } catch (IOException ex) {
            System.err.println("await: IOException in socket.accept()");
            return;
        }

        try {
            in = worker.getInputStream();
        } catch (IOException ex) {
            System.err.println("await: IOException in worker.getInputStream()");
            return;
        }

        var bytes = new byte[Utils.BUFFER_SIZE];
        var out = new ByteArrayOutputStream();

        int count;

        try {
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);

                // The client seems to cause this loop to never end
                // We cut connection after PACKET_MIN_LEN is read
                // Server does not cause problems
                if (count >= Utils.PACKET_MIN_LEN) {
                    break;
                }
            }
        } catch (IOException ex) {
            System.err.println("await: IOException in in.read(bytes)");
            return;
        }

        System.out.println(String.format("await: received %d", out.size()));
        handlePacket(worker, out);

        // Clean things up
        try {
            worker.close();
            out.close();
            in.close();
        } catch (IOException ex) {
            System.err.println("await: IOException while cleaning up");
        }
    }

    public void stop() {
        // Can happen if multiple instances are launched
        if (socket == null || socket.isClosed()) {
            return;
        }

        try {
            socket.close();
        } catch (IOException ex) {
            System.err.println("stop: IOException in socket.close()");
        }
    }

    public boolean isValid() {
        return valid;
    }
}
