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

import java.util.Objects;

import java.net.InetAddress;

/**
 *
 * @author Diamante
 */
public class Server {

    private InetAddress address;

    private Short netPort;

    private Long time;

    private Integer version;

    public Server(InetAddress address, Short netPort, Integer version) {
        this.address = address;
        this.netPort = netPort;
        this.version = version;

        this.time = System.currentTimeMillis() / 1000L;
    }

    public Server(InetAddress address, Short netPort) {
        this.address = address;
        this.netPort = netPort;

        this.time = System.currentTimeMillis() / 1000L;
    }

    public InetAddress getAddress() {
        return address;
    }

    public short getNetPort() {
        return netPort;
    }

    public long getTime() {
        return time;
    }

    public int getVersion() {
        return version;
    }

    public void updateTime() {
        this.time = System.currentTimeMillis() / 1000L;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Server other = (Server) obj;
        if (!Objects.equals(this.address, other.address)) {
            return false;
        }

        if (!Objects.equals(this.netPort, other.netPort)) {
            return false;
        }

        if (!Objects.equals(this.version, other.version)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.address);
        hash = 71 * hash + Objects.hashCode(this.netPort);
        hash = 71 * hash + Objects.hashCode(this.version);
        hash = 71 * hash + Objects.hashCode(this.time);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("%s:%d", this.getAddress().toString(), this.netPort);
    }
}
