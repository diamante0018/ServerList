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

import java.io.IOException;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.Socket;

import java.net.UnknownHostException;

/**
 *
 * @author Diamante
 */
public class ServerEmulator {

    private static final String OFFICIAL_MASTER = "mw3.totalkillaz.ovh";
    
    private Socket socket;

    private int currentPort;

    private boolean valid;

    public ServerEmulator() {
        try {
            var ip = InetAddress.getByName(OFFICIAL_MASTER);
            socket = new Socket(ip, MasterServer.PORT);
            currentPort = 0;
            valid = true;
        }
        catch (UnknownHostException ex) {
            valid = false;
            System.err.println("ServerEmulator: UnknownHostException in InetAddress.getByName()");
        }
        catch (IOException ex) {
            valid = false;
            System.err.println("ServerEmulator: IOException in new Socket()");
        }
    }

    public void pingMasterServer(int port, OutputStream out) {
        var request = new byte[Utils.PACKET_SERVERT_LEN];

        var magicLE = Utils.longSwap(Utils.OLD_SERVER_MAGIC);
        var versionLE = Utils.longSwap(Utils.CLIENT_VERSION);
        var portLE = Utils.longSwap(port);

        System.arraycopy(magicLE, 0, request, 0, 4);
        System.arraycopy(versionLE, 0, request, 4, 4);
        // Write only two bytes for the port
        System.arraycopy(portLE, 0, request, 8, 2);

        try {
            out.write(request);
        }
        catch (IOException ex) {
            System.err.println("pingMasterServer: IOException in out.write()");
            setValid(false);
        }
    }

    public void pingLoop() {
        while (Main.running.get() && isValid()) {
            var port = currentPort;
            currentPort = (currentPort + 1) % 65535;

            try {
                var out = socket.getOutputStream();
                pingMasterServer(port, out);
            }
            catch (IOException ex) {
                System.err.println("pingLoop: IOException in socket.getOutputStream()");
            }

            try {
                Thread.sleep(80);
            }
            catch (InterruptedException ex) {
                setValid(false);
            }
        }

        stop();
    }

    public void stop() {
        // Can happen if multiple instances are launched
        if (socket == null || socket.isClosed()) {
            return;
        }

        try {
            socket.close();
        }
        catch (IOException ex) {
            System.err.println("stop: IOException in socket.close()");
        }
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
