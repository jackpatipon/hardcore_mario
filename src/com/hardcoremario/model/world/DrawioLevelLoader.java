package com.hardcoremario.model.world;

import com.hardcoremario.model.entity.Guard;
import com.hardcoremario.model.entity.Player;
import com.hardcoremario.model.item.HealthPack;
import com.hardcoremario.util.Constants;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Parses draw.io XML files to dynamically load stages, platforms, enemies, items, and hazards.
 * Demonstrates XML Parsing, File I/O, and Level Generation in OOP.
 */
public class DrawioLevelLoader {

    public static class LevelData {
        public double minX = 0.0;
        public double minY = 0.0;
        public double maxX = 3600.0;
        public double maxY = 1400.0;
        public double width = 3600.0;
        public double height = 1400.0;
        public double playerSpawnX = 520.0;
        public double playerSpawnY = 1060.0;
        public final List<Tile> tiles = new ArrayList<>();
        public final List<Guard> enemies = new ArrayList<>();
        public final List<HealthPack> items = new ArrayList<>();
    }

    public static File findStageFile(int stage) {
        String[] possiblePaths = {
            "assets/levels/hardcore_mario_stage" + stage + ".drawio.xml",
            "assets/levels/stage" + stage + ".drawio.xml",
            "hardcore_mario_stage" + stage + ".drawio.xml",
            "stage" + stage + ".drawio.xml",
            "hardcore_mario_stage" + stage + ".xml"
        };
        for (String p : possiblePaths) {
            File f = new File(p);
            if (f.exists()) return f;
        }
        return null;
    }

    public static LevelData loadLevel(String xmlFilePath) {
        LevelData data = new LevelData();
        File file = new File(xmlFilePath);

        if (!file.exists()) {
            // Also check under assets/levels/
            File subFile = new File("assets/levels/" + xmlFilePath);
            if (subFile.exists()) {
                file = subFile;
            } else {
                File subFile2 = new File("assets/levels/" + file.getName());
                if (subFile2.exists()) {
                    file = subFile2;
                } else {
                    System.err.println("Warning: Level XML file not found at " + xmlFilePath + ". Using embedded layout.");
                    return buildHardcodedStage1Fallback();
                }
            }
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Disable external DTDs for safety
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList cellNodes = doc.getElementsByTagName("mxCell");
            final double SCALE = 2.0; // Draw.io 20px grid -> 40px in-game tile size

            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;

            for (int i = 0; i < cellNodes.getLength(); i++) {
                Element cell = (Element) cellNodes.item(i);
                String value = cell.getAttribute("value").trim().toLowerCase();
                String style = cell.getAttribute("style");

                // Skip connections / edges
                if ("1".equals(cell.getAttribute("edge"))) {
                    continue;
                }

                NodeList geoList = cell.getElementsByTagName("mxGeometry");
                if (geoList.getLength() == 0) continue;

                Element geo = (Element) geoList.item(0);
                String xStr = geo.getAttribute("x");
                String yStr = geo.getAttribute("y");
                String wStr = geo.getAttribute("width");
                String hStr = geo.getAttribute("height");

                // In draw.io XML, attributes x and y default to 0.0 when omitted
                if (xStr.isEmpty() && yStr.isEmpty() && wStr.isEmpty() && hStr.isEmpty()) continue;

                double rawX = xStr.isEmpty() ? 0.0 : Double.parseDouble(xStr);
                double rawY = yStr.isEmpty() ? 0.0 : Double.parseDouble(yStr);
                double rawW = wStr.isEmpty() ? 20.0 : Double.parseDouble(wStr);
                double rawH = hStr.isEmpty() ? 20.0 : Double.parseDouble(hStr);

                // Ignore legend elements on the far left (legend icons and text have rawX + rawW < 50)
                if (rawX + rawW < 50) continue;

                // Clamp level elements that start to the left of 0 (e.g. ground at x=-100)
                if (rawX < 0) {
                    rawW += rawX;
                    rawX = 0;
                }

                double gameX = rawX * SCALE;
                double gameY = rawY * SCALE;
                double gameW = rawW * SCALE;
                double gameH = rawH * SCALE;

                Color fillColor = parseFillColor(style);

                // Classify cell by value or style
                if (value.equals("player")) {
                    data.playerSpawnX = gameX;
                    data.playerSpawnY = gameY;
                    minX = Math.min(minX, gameX);
                    minY = Math.min(minY, gameY);
                    maxX = Math.max(maxX, gameX + 40.0);
                    maxY = Math.max(maxY, gameY + 60.0);
                } else if (value.equals("enemy")) {
                    data.enemies.add(new Guard(gameX, gameY, 0));
                    minX = Math.min(minX, gameX);
                    minY = Math.min(minY, gameY);
                    maxX = Math.max(maxX, gameX + gameW);
                    maxY = Math.max(maxY, gameY + gameH);
                } else if (value.equals("exit") || style.contains("shape=loopLimit")) {
                    data.tiles.add(new Tile(gameX, gameY, (int) gameW, (int) gameH, Tile.TileType.EXIT));
                    minX = Math.min(minX, gameX);
                    minY = Math.min(minY, gameY);
                    maxX = Math.max(maxX, gameX + gameW);
                    maxY = Math.max(maxY, gameY + gameH);
                } else if (style.contains("shape=cross")) {
                    // Item: Health Pack (44x44 for crisp high visibility)
                    int itemW = Math.max((int) Math.round(gameW > 0 ? gameW : 44), 44);
                    int itemH = Math.max((int) Math.round(gameH > 0 ? gameH : 44), 44);
                    HealthPack pack = new HealthPack(gameX, gameY, itemW, itemH);
                    if (style.contains("decor") || value.contains("decor") || style.contains("locked=1") || style.contains("movable=0")) {
                        pack.setFloating(true);
                    }
                    data.items.add(pack);
                    minX = Math.min(minX, gameX);
                    minY = Math.min(minY, gameY);
                    maxX = Math.max(maxX, gameX + itemW);
                    maxY = Math.max(maxY, gameY + itemH);
                } else if (style.contains("triangle") || style.contains("rotation=-90")) {
                    // Hazard: Spikes (Instant death)
                    boolean isFlipped = style.contains("flipV=1") || style.contains("flipH=1") || style.contains("rotation=90");
                    Tile spikeTile = new Tile(gameX, gameY, (int) gameW, (int) gameH, Tile.TileType.SPIKES);
                    spikeTile.setPointingDown(isFlipped);
                    data.tiles.add(spikeTile);
                    minX = Math.min(minX, gameX);
                    minY = Math.min(minY, gameY);
                    maxX = Math.max(maxX, gameX + gameW);
                    maxY = Math.max(maxY, gameY + gameH);
                } else if (style.contains("fillColor=light-dark(#0050EF,#FF8000)") || style.contains("#FF8000")) {
                    // Breakable block (Shootable by player only)
                    data.tiles.add(new Tile(gameX, gameY, (int) gameW, (int) gameH, Tile.TileType.BREAKABLE_BLOCK));
                    minX = Math.min(minX, gameX);
                    minY = Math.min(minY, gameY);
                    maxX = Math.max(maxX, gameX + gameW);
                    maxY = Math.max(maxY, gameY + gameH);
                } else if (style.contains("fillColor=#0050ef")) {
                    // Indestructible solid blue block
                    data.tiles.add(new Tile(gameX, gameY, (int) gameW, (int) gameH, Tile.TileType.SOLID_BLOCK));
                    minX = Math.min(minX, gameX);
                    minY = Math.min(minY, gameY);
                    maxX = Math.max(maxX, gameX + gameW);
                    maxY = Math.max(maxY, gameY + gameH);
                } else if (rawW > 100 && rawY >= 500) {
                    // Solid ground floor (uses custom color if specified in draw.io)
                    data.tiles.add(new Tile(gameX, gameY, (int) gameW, (int) gameH, Tile.TileType.GROUND, fillColor));
                    minX = Math.min(minX, gameX);
                    minY = Math.min(minY, gameY);
                    maxX = Math.max(maxX, gameX + gameW);
                    maxY = Math.max(maxY, gameY + gameH);
                } else if (rawW > 0 && rawH > 0 && !style.contains("text")) {
                    // Other solid block
                    data.tiles.add(new Tile(gameX, gameY, (int) gameW, (int) gameH, Tile.TileType.SOLID_BLOCK, fillColor));
                    minX = Math.min(minX, gameX);
                    minY = Math.min(minY, gameY);
                    maxX = Math.max(maxX, gameX + gameW);
                    maxY = Math.max(maxY, gameY + gameH);
                }
            }


            // Determine item floating vs grounded physics:
            // 1. If multiple items are vertically aligned in the same column (like decorative pixel-art patterns),
            //    they must float (otherwise gravity would make them fall and merge on top of each other).
            for (HealthPack item : data.items) {
                for (HealthPack other : data.items) {
                    if (item == other) continue;
                    if (Math.abs(item.getX() - other.getX()) < 15) {
                        item.setFloating(true);
                        other.setFloating(true);
                    }
                }
            }

            // 2. For isolated items: items placed directly on a block rest on it and will fall if the block breaks.
            //    Items placed floating in mid-air float in place.
            for (HealthPack item : data.items) {
                if (item.isFloating()) continue;
                boolean hasFloorBelow = false;
                double itemBottom = item.getY() + item.getHeight();
                for (Tile tile : data.tiles) {
                    if (!tile.isSolid()) continue;
                    // Check horizontal alignment with tile
                    if (tile.getX() <= item.getCenterX() && item.getCenterX() <= tile.getX() + tile.getWidth()) {
                        // Check if tile top is directly underneath item bottom (within 16px)
                        if (tile.getY() >= itemBottom - 4 && tile.getY() - itemBottom <= 16) {
                            hasFloorBelow = true;
                            break;
                        }
                    }
                }
                if (!hasFloorBelow) {
                    item.setFloating(true);
                }
            }

            // Set world bounds with padding
            if (minX == Double.MAX_VALUE) {
                minX = 0;
                minY = 0;
                maxX = 3400;
                maxY = 1400;
            }

            double boundMinX = Math.min(0.0, minX);
            double boundMinY = Math.min(0.0, minY < 0 ? minY - 260.0 : 0.0);
            double boundMaxX = Math.max(3400.0, maxX + 200.0);
            double boundMaxY = Math.max(1400.0, maxY + 100.0);

            data.minX = boundMinX;
            data.minY = boundMinY;
            data.maxX = boundMaxX;
            data.maxY = boundMaxY;
            data.width = boundMaxX - boundMinX;
            data.height = boundMaxY - boundMinY;

            System.out.println("Loaded Level from " + xmlFilePath + ": " +
                               data.tiles.size() + " tiles, " +
                               data.enemies.size() + " enemies, " +
                               data.items.size() + " items.");
            return data;

        } catch (Exception e) {
            System.err.println("Error parsing XML: " + e.getMessage());
            e.printStackTrace();
            return buildHardcodedStage1Fallback();
        }
    }

    private static Color parseFillColor(String style) {
        if (style == null) return null;
        int idx = style.indexOf("fillColor=");
        if (idx == -1) return null;
        String sub = style.substring(idx + 10);
        int semi = sub.indexOf(";");
        if (semi != -1) sub = sub.substring(0, semi);

        if (sub.startsWith("light-dark(") && sub.endsWith(")")) {
            String inside = sub.substring(11, sub.length() - 1);
            String[] parts = inside.split(",");
            if (parts.length >= 2) {
                sub = parts[1].trim(); // Dark mode color or 2nd color
            } else if (parts.length == 1) {
                sub = parts[0].trim();
            }
        }
        try {
            if (sub.startsWith("#")) {
                return Color.decode(sub);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static LevelData buildHardcodedStage1Fallback() {
        LevelData data = new LevelData();
        final double S = 2.0;

        // Ground floor: x=20..2360, y=560
        data.tiles.add(new Tile(20 * S, 560 * S, 2340 * S, 120 * S, Tile.TileType.GROUND));

        // Player spawn: x=260, y=530
        data.playerSpawnX = 260 * S;
        data.playerSpawnY = 500 * S;

        // Platform 1 before pit: x=360..460, y=520, 540
        for (int x = 360; x <= 460; x += 20) {
            data.tiles.add(new Tile(x * S, 520 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
            data.tiles.add(new Tile(x * S, 540 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
        }

        // Pit 1 Spikes: x=480..580, y=540
        for (int x = 480; x <= 580; x += 20) {
            data.tiles.add(new Tile(x * S, 540 * S, 20 * S, 20 * S, Tile.TileType.SPIKES));
        }

        // Breakable bridge above Pit 1: x=480..540, y=400
        for (int x = 480; x <= 540; x += 20) {
            data.tiles.add(new Tile(x * S, 400 * S, 20 * S, 20 * S, Tile.TileType.BREAKABLE_BLOCK));
        }
        // Enemy on breakable bridge: x=480, y=370
        data.enemies.add(new Guard(480 * S, 350 * S, 0));

        // Platform 2 after pit: x=600..700, y=520, 540
        for (int x = 600; x <= 700; x += 20) {
            data.tiles.add(new Tile(x * S, 520 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
            data.tiles.add(new Tile(x * S, 540 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
        }

        // Stepping blocks: x=740, 760, y=480 (adjusted in XML from 460 to 480)
        data.tiles.add(new Tile(740 * S, 480 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
        data.tiles.add(new Tile(760 * S, 480 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));

        // Wall 1: x=840, y=360..540 (y=420, 440 are breakable!)
        for (int y = 360; y <= 540; y += 20) {
            if (y == 420 || y == 440) {
                data.tiles.add(new Tile(840 * S, y * S, 20 * S, 20 * S, Tile.TileType.BREAKABLE_BLOCK));
            } else {
                data.tiles.add(new Tile(840 * S, y * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
            }
        }
        // Enemy on top of Wall 1: x=840, y=330
        data.enemies.add(new Guard(840 * S, 310 * S, 0));

        // Pit 2 Spikes: x=860..1160, y=540
        for (int x = 860; x <= 1160; x += 20) {
            data.tiles.add(new Tile(x * S, 540 * S, 20 * S, 20 * S, Tile.TileType.SPIKES));
        }

        // Stepping stones across Pit 2:
        data.tiles.add(new Tile(920 * S, 480 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
        data.tiles.add(new Tile(1000 * S, 460 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
        data.enemies.add(new Guard(1000 * S, 420 * S, 0));
        data.tiles.add(new Tile(1040 * S, 520 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
        data.tiles.add(new Tile(1080 * S, 380 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
        data.enemies.add(new Guard(1080 * S, 340 * S, 0));
        data.tiles.add(new Tile(1160 * S, 520 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));

        // Pit 3 Spikes: x=1180..1460, y=540
        for (int x = 1180; x <= 1460; x += 20) {
            data.tiles.add(new Tile(x * S, 540 * S, 20 * S, 20 * S, Tile.TileType.SPIKES));
        }

        // Stepping stones & enemies across Pit 3:
        data.tiles.add(new Tile(1200 * S, 380 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
        data.enemies.add(new Guard(1200 * S, 340 * S, 0));
        data.tiles.add(new Tile(1240 * S, 500 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
        data.tiles.add(new Tile(1280 * S, 420 * S, 20 * S, 20 * S, Tile.TileType.BREAKABLE_BLOCK));
        data.items.add(new HealthPack(1285 * S, 400 * S)); // Item on breakable block
        data.tiles.add(new Tile(1340 * S, 500 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
        data.tiles.add(new Tile(1400 * S, 460 * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
        data.enemies.add(new Guard(1400 * S, 420 * S, 0));

        // Wall 2: x=1480, y=360..540 (y=400, 420 are breakable!)
        for (int y = 360; y <= 540; y += 20) {
            if (y == 400 || y == 420) {
                data.tiles.add(new Tile(1480 * S, y * S, 20 * S, 20 * S, Tile.TileType.BREAKABLE_BLOCK));
            } else {
                data.tiles.add(new Tile(1480 * S, y * S, 20 * S, 20 * S, Tile.TileType.SOLID_BLOCK));
            }
        }
        data.enemies.add(new Guard(1480 * S, 320 * S, 0));

        // Exit Portal: x=1500, y=390
        data.tiles.add(new Tile(1500 * S, 390 * S, (int) (62.5 * S), (int) (50 * S), Tile.TileType.EXIT));

        data.minX = 0.0;
        data.minY = 0.0;
        data.maxX = 3400.0;
        data.maxY = 1400.0;
        data.width = 3400.0;
        data.height = 1400.0;
        return data;
    }
}
