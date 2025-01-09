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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import org.apache.commons.cli.ParseException;

/**
 *
 * @author Diamante
 */
public class Main {

    private enum Mode {
        Emulator, Master, MasterPing, ServerPing, Bad;
    }

    private Mode mode;

    public static final AtomicBoolean running = new AtomicBoolean(true);

    private MasterServer server;

    public Main() {
        mode = Mode.Bad;
    }

    private Mode getMode() {
        return mode;
    }

    private void setMode(Mode mode) {
        this.mode = mode;
    }

    public MasterServer getServer() {
        return server;
    }

    private void createMasterServer() {
        server = new MasterServer();
    }

    private Options createOptions() {
        var options = new Options();

        var master = new Option("master", "master server mode");
        var emulator = new Option("emulator", "client emulator mode");
        var masterPing = new Option("master_ping", "ping the master server");
        var serverPing = new Option("server_ping", "ping the master server as a server");

        var ping = Option.builder("ping")
                .argName("IP:Port")
                .hasArg()
                .desc("Server to ping")
                .build();

        options.addOption(master);
        options.addOption(emulator);
        options.addOption(masterPing);
        options.addOption(serverPing);
        options.addOption(ping);

        return options;
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
        var options = main.createOptions();
        var ip = new String();

        var parser = new DefaultParser();
        try {
            var line = parser.parse(options, args);
            if (line.hasOption("master")) {
                main.setMode(Mode.Master);
            } else if (line.hasOption("emulator")) {
                main.setMode(Mode.Emulator);
            } else if (line.hasOption("master_ping")) {
                main.setMode(Mode.MasterPing);
            } else if (line.hasOption("server_ping")) {
                main.setMode(Mode.ServerPing);
            }

            if (line.hasOption("ping")) {
                ip = line.getOptionValue("ping");
            }
        }
        catch (ParseException exp) {
            System.err.println("Parsing failed. Reason: " + exp.getMessage());
        }

        if (main.getMode() == Mode.Master) {
            main.createMasterServer();
            System.out.println("Master Server startup");
            while (running.get() && main.getServer().isValid()) {
                main.getServer().await();
            }
            main.getServer().stop();
        } else if (main.getMode() == Mode.Emulator) {
            var emulator = new ClientEmulator();
            emulator.pingSingleServer(ip);
        } else if (main.getMode() == Mode.MasterPing) {
            var ping = new MasterServerPinger();
            ping.pingMaster();
            ping.readReplyFromMaster();
        } else if (main.getMode() == Mode.ServerPing) {
             var ping = new ServerEmulator();
             ping.pingLoop();
        }

        System.out.println("Normal shutdown");
    }
}
