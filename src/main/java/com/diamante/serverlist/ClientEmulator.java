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

import java.lang.management.ManagementFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;

import java.util.Set;

/**
 * The purpose of this module is to ping the servers on the server list.
 *
 * @author Diamante
 */
public class ClientEmulator implements Runnable {

    public static final int SERVER_QUERY = 1347374924;

    public static final int SERVER_INFO_SIZE = 2129;

    private final Set<Server> servers;

    private static final int SOCKET_TIMEOUT = 4000;
    private DatagramSocket socket;

    public ClientEmulator(Set<Server> servers) {
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(SOCKET_TIMEOUT);
        }
        catch (SocketException ex) {
            System.err.println("ClientEmulator: SocketException while creating new DatagramSocket");
            System.exit(-1);
        }

        this.servers = servers;
    }

    public ClientEmulator() {
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(SOCKET_TIMEOUT);
        }
        catch (SocketException ex) {
            System.err.println("ClientEmulator: SocketException while creating new DatagramSocket");
            System.exit(-1);
        }

        this.servers = null;
    }

    public void pingSingleServer(String ip) {
        var to = Utils.stringToServer(ip);
        if (to != null) {
            handleServer(to);
        }
    }

    private void sendDatagramPacket(DatagramPacket packet) {
        try {
            socket.send(packet);
        }
        catch (IOException | IllegalArgumentException | IllegalBlockingModeException ex) {
            // Socket will timeout
            System.err.println("sendDatagramPacket: exception while sending a packet");
        }
    }

    private DatagramPacket receiveDatagramPacket() {
        var buffer = new byte[SERVER_INFO_SIZE];
        var packet = new DatagramPacket(buffer, buffer.length);

        try {
            socket.receive(packet);
            System.out.println(String.format("receiveDatagramPacket: Server %s:%d returned a packet", packet.getAddress().toString(), packet.getPort()));
            return packet;
        }
        catch (SocketException ex) {
            System.err.println("receiveDatagramPacket: SocketException");
            return null;
        }
        catch (IllegalBlockingModeException ex) {
            System.err.println("receiveDatagramPacket: IllegalBlockingModeException");
            return null;
        }
        catch (SocketTimeoutException ex) {
            System.err.println("receiveDatagramPacket: SocketTimeoutException");
            return null;
        }
        catch (IOException ex) {
            System.err.println("receiveDatagramPacket: IOException");
            return null;
        }
    }

    private byte[] generateClientPing() {
        // Use this as tick
        var jvmUpTime = (Long) ManagementFactory.getRuntimeMXBean().getUptime();

        var tickLE = Utils.longSwap(jvmUpTime.intValue());
        var magicLE = Utils.longSwap(SERVER_QUERY);

        var data = new byte[8];

        System.arraycopy(magicLE, 0, data, 0, 4);
        System.arraycopy(tickLE, 0, data, 4, 4);

        return data;
    }

    private void sendPingToServer(Server server) {
        var data = generateClientPing();
        // We use the net_port like a client would.
        var packet = new DatagramPacket(data, data.length, server.getAddress(), server.getNetPort());
        sendDatagramPacket(packet);
    }

    private void handleServer(Server server) {
        sendPingToServer(server);
        var response = receiveDatagramPacket();
        if (response == null) {
            return;
        }

        var rawData = response.getData();
        InfoDumper.dumpServerResponse(server, rawData);
    }

    @Override
    public void run() {
        synchronized (servers) {
            var it = servers.iterator();
            while (it.hasNext()) {
                var server = it.next();
                handleServer(server);
            }
        }
    }
}
