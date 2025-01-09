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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;

/**
 *
 * @author Diamante
 */
public class InfoDumper {

    public static void dumpServerResponse(Server server, byte[] data) {
        assert data.length == ClientEmulator.SERVER_INFO_SIZE;

        var magicLE = new byte[4];

        var playersLE = new byte[4];
        var maxPlayersLE = new byte[4];

        var rawDataLE = new byte[2048];

        System.arraycopy(data, 0, magicLE, 0, 4);
        System.arraycopy(data, 8, playersLE, 0, 4);
        System.arraycopy(data, 12, maxPlayersLE, 0, 4);

        System.arraycopy(data, 81, rawDataLE, 0, 2048);

        var playersBE = Utils.longSwap(playersLE);
        var maxPlayersBE = Utils.longSwap(maxPlayersLE);

        System.out.println(String.format("dumpServerResponse: Players %d:%d", playersBE, maxPlayersBE));

        String infoString = new String(rawDataLE, StandardCharsets.UTF_8);
        System.out.println(infoString);

        // Save to JSON for easier inspection
        var magicBE = Utils.longSwap(magicLE);

        var obj = new JSONObject();
        obj.put("server", server.toString());
        obj.put("magic", magicBE);
        obj.put("players", playersBE);
        obj.put("sv_maxClients", maxPlayersBE);
        
        saveJSONFile(String.format("stats_%d.json", Math.abs(server.hashCode())), obj);
    }

    public static void saveJSONFile(String fileName, JSONObject obj) {
        try {
            var writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(obj.toJSONString());
            writer.close();
        }
        catch (IOException ex) {
            System.err.println("saveJSONFile: IOException while writing a JSON file");
        }
    }
}
