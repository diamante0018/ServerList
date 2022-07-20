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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Diamante
 */
public class ServerList {

    private final Set<Server> serverList;

    public ServerList() {
        serverList = Collections.synchronizedSet(new HashSet<>());
    }

    public boolean isServerRegistered(Server server) {
        synchronized (serverList) {
            var it = serverList.iterator(); // Must be in the synchronized block
            while (it.hasNext()) {
                var other = it.next();
                if (server.equals(other)) {
                    // Update the time so we don't accidentally remove the server
                    other.updateTime();
                    return true;
                }
            }
        }

        return false;
    }

    public void addServer(Server server) {

        if (!isServerRegistered(server)) {
            serverList.add(server);
        }

        System.out.println(String.format("addServer: Tried to add server %s", server.toString()));
    }

    public void removeInactive() {
        synchronized (serverList) {
            var it = serverList.iterator();
            var time = System.currentTimeMillis() / 1000L;

            while (it.hasNext()) {
                var server = it.next();
                if (time - server.getTime() > 60) {
                    System.out.println(String.format("Removing server %s because of inactivity", server.getAddress().toString()));
                    it.remove();
                }
            }
        }
    }

    /**
     * The first 4 bytes will contain the numbers of servers we are going to
     * send in LE Then we have 4 bytes for the IP address in LE Finally 2 bytes
     * for the net_port in LE. Repeat for each server
     *
     * @param version the version of the client
     * @return the raw bytes to send to the client
     */
    public byte[] createResponse(int version) {
        var builder = new ByteArrayOutputStream();

        // We need to swap everything to LE
        var sizeBE = serverList.size();
        var sizeLE = Utils.longSwap(sizeBE);

        try {
            builder.write(sizeLE);
        }
        catch (IOException ex) {
            System.err.println("createResponse: IOException in builder.write(sizeLE)");
        }

        synchronized (serverList) {
            var it = serverList.iterator(); // Must be in the synchronized block
            while (it.hasNext()) {
                var server = it.next();
                // Let's make sure we send the client only servers on the same version
                if (server.getVersion() == version) {
                    try {
                        // Let's flip the bytes of this one too
                        var ipBE = server.getAddress().getAddress();
                        Utils.swapByteArray(ipBE);

                        // And the port too of course
                        var portBE = server.getNetPort();
                        var portLE = Utils.shortSwap(portBE);

                        builder.write(ipBE);
                        builder.write(portLE);
                    }
                    catch (IOException ex) {
                        System.err.println("createResponse: IOException while writing server data");
                    }
                }
            }
        }

        // I forgot why I do this
        var byteBuffer = ByteBuffer.allocate(builder.size());
        byteBuffer.clear();
        byteBuffer.put(builder.toByteArray());
        byteBuffer.flip();

        try {
            builder.close();
        }
        catch (IOException ex) {
            System.err.println("createResponse: IOException in builder.close()");
        }

        return byteBuffer.array();
    }

    public void dumpOnlineServers() {
        if (!Main.running.get()) {
            return;
        }

        var thread = new Thread(new ClientEmulator(serverList));
        thread.start();
    }
}
