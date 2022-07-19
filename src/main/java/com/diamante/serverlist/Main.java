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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Diamante
 */
public class Main {

    public static final AtomicBoolean running = new AtomicBoolean(true);

    private final MasterServer server;

    public Main() {
        server = new MasterServer();
    }

    public MasterServer getServer() {
        return server;
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Running shutdown hook");
                running.compareAndSet(true, false);
            }
        });

        var main = new Main();

        while (running.get() && main.getServer().isValid()) {
            main.getServer().await();
        }

        main.getServer().stop();
    }
}
