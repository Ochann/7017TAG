package games.catan.gui;

import core.components.BoardNodeWithEdges;
import core.components.Edge;
import games.catan.CatanConstants;
import games.catan.CatanGameState;
import games.catan.CatanParameters;
import games.catan.components.CatanTile;
import games.catan.components.Building;

import javax.swing.*;
import java.awt.*;

import static games.catan.CatanConstants.HEX_SIDES;

public class CatanBoardView extends JComponent {
    CatanGameState gs;
    CatanParameters params;

    private double tileRadius;

    public CatanBoardView(CatanGameState gs, int width, int height){
        this.gs = gs;
        this.params = (CatanParameters) gs.getGameParameters();
        this.tileRadius = 40;
        setPreferredSize(new Dimension(width, height));
//        updateTileSize();
    }

    private void updateTileSize(){
        // updates the tile width and height and keep it proportional
        // todo work out the correct size here
        this.tileRadius = 30; //(double)this.height / CatanConstants.BOARD_SIZE;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        super.paintComponent(g);
        drawBoard(g2);
    }

    private void drawBoard(Graphics2D g) {
        // Draw board
        CatanTile[][] board = gs.getBoard();

        int midX = board.length/2;
        int midY = board[0].length/2;
        CatanTile midTile = new CatanTile(midX, midY);


        for (CatanTile[] tiles : board) {
            for (CatanTile tile : tiles) {
                // mid_x should be the same as the distance
                if (midTile.getDistanceToTile(tile) >= midX + 1) {
                    continue;
                }
                Point centreCoords = tile.getCentreCoords(tileRadius);

                g.setColor(tileColourLookup(tile));
                Polygon tileHex = tile.getHexagon(tileRadius);
                g.fillPolygon(tileHex);
                g.setColor(Color.BLACK);
                g.drawPolygon(tileHex);

                if (tile.hasRobber()) {
                    drawRobber(g, centreCoords);
                }

                String type = "" + tile.getTileType();
                String number = "" + tile.getNumber();
                g.drawString(type, centreCoords.x - 20, centreCoords.y);
                if (!number.equals("0"))
//                    g.drawString((tile.x + " " + tile.y), (int) tile.x_coord, (int) tile.y_coord + 20);
                    g.drawString(number, centreCoords.x, centreCoords.y + 20);
            }
        }

        // Draw roads top of board
        for (CatanTile[] catanTiles : board) {
            for (CatanTile tile : catanTiles) {
                // draw roads
                Edge[] roads = gs.getRoads(tile);
                for (int i = 0; i < roads.length; i++) {
                    if (roads[i] != null && roads[i].getOwnerId() != -1)
                        drawRoad(g, i, tile.getEdgeCoords(i, tileRadius), CatanConstants.PlayerColors[roads[i].getOwnerId()]);
                    // todo useful for showing road IDs on the GUI
//                        g.setFont(new Font("TimeRoman", Font.PLAIN, 10));
//                        g.setColor(Color.BLACK);
//                        Point[] location = tile.getEdgeCoords(i, tileRadius);
//                        g.drawLine(location[0].x, location[0].y, location[1].x, location[1].y);
//                        g.drawString(i + "", ((location[0].x + location[1].x) / 2), ((location[0].y + location[1].y) / 2));
                }
            }
        }

        // Finally draw settlements
        for (CatanTile[] catanTiles : board) {
            for (CatanTile tile : catanTiles) {
                // draw settlements
                Building[] settlements = gs.getBuildings(tile);
                for (int i = 0; i < settlements.length; i++) {
//                    g.drawString("" + settlements[i].hashCode(), tile.getVerticesCoords(i).x, tile.getVerticesCoords(i).y);
                    if (settlements[i].getOwnerId() != -1) {
                        drawSettlement(g, i, tile.getVerticesCoords(i, tileRadius), CatanConstants.PlayerColors[settlements[i].getOwnerId()], settlements[i].getBuildingType());
                    }
                    // todo lines below are useful for debugging as they display settlement IDs
//                    g.setFont(new Font("TimeRoman", Font.PLAIN, 20));
//                    g.setColor(Color.GRAY);
//                    g.drawString(settlements[i].getID() + "", tile.getVerticesCoords(i).x, tile.getVerticesCoords(i).y);

                    if (settlements[i].getHarbour() != null) {
                        CatanParameters.Resource type = settlements[i].getHarbour();
                        drawHarbour(g, tile.getVerticesCoords(i, tileRadius), type);
                    }
                }

                // lines below render cube coordinates and distances from middle
//                String s = Arrays.toString(tile.to_cube(tile));
//                String mid = Arrays.toString(tile.to_cube(board[3][3]));
//                String dist = "" + tile.distance(board[3][3]);
//                g.drawString(s, (int)tile.x_coord - 20, (int)tile.y_coord - 20);
//                g.drawString(mid, (int)tile.x_coord - 20, (int)tile.y_coord);
//                g.setColor(Color.ORANGE);
//                g.drawString(dist, (int)tile.x_coord - 20, (int)tile.y_coord + 20);


            }
        }
    }

    private Color tileColourLookup(CatanTile tile){
        if (tile.getTileType() == CatanTile.TileType.DESERT){
            return Color.YELLOW;
        } else if (tile.getTileType() == CatanTile.TileType.SEA){
            return new Color(51, 204, 255);//Color.BLUE;
        }else if (tile.getTileType() == CatanTile.TileType.FIELDS){
            return new Color(255, 255, 153); //Color.LIGHT_GRAY;
        }else if (tile.getTileType() == CatanTile.TileType.FOREST){
            return new Color(0, 102, 0); //Color.RED;
        }else if (tile.getTileType() == CatanTile.TileType.MOUNTAINS){
            return Color.DARK_GRAY;
        }else if (tile.getTileType() == CatanTile.TileType.PASTURE){
            return new Color(102, 255, 102); //Color.GREEN;
        } else if (tile.getTileType() == CatanTile.TileType.HILLS){
            return new Color(102, 102, 102); //Color.ORANGE;
        } else{
            return Color.WHITE;
        }
    }

    public void drawRobber(Graphics2D g, Point point){
        int RADIUS = 10;
        g.setColor(Color.BLACK);
        g.fillOval(point.x, point.y, RADIUS, RADIUS);
    }

    public void drawHarbor(Graphics2D g, CatanTile tile){
        // todo rotate text? should be clear what harbor it is
        //  rotation below does not work as expected
        // todo draws a black road to represent the harbor for now
        for (int i = 0; i < HEX_SIDES; i++){
            Building b = gs.getBuilding(tile, i);
            if (b.getHarbour() != null){
                Color color = Color.BLACK;
                g.setColor(color);
                Stroke stroke = g.getStroke();
                g.setStroke(new BasicStroke(8));
                Point[] location = tile.getEdgeCoords(i, tileRadius);
                g.drawLine(location[0].x, location[0].y, location[1].x, location[1].y);
                g.setStroke(stroke);
//                AffineTransform original = g.getTransform();
//                g.rotate(Math.toRadians(-60));
                Point centreCoords = tile.getCentreCoords(tileRadius);
                g.drawString(b.getHarbour().name(), centreCoords.x, centreCoords.y+10);
//                g.setTransform(original);
                break;
            }
        }
    }

    public void drawHarbour(Graphics2D g, Point point, CatanParameters.Resource type) {
        int radius = 10;
        g.setColor(Color.BLACK);
        g.fillOval(point.x, point.y, radius, radius);
        int exchangeRate = params.harbour_exchange_rate;
        if (type == CatanParameters.Resource.WILD) exchangeRate = params.harbour_wild_exchange_rate;
        g.drawString(type.name() + " " + exchangeRate + ":1", point.x, point.y+10);
    }

    public void drawRoad(Graphics2D g, int edge, Point[] location, Color color){
        g.setColor(color);
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke(5));
        g.drawLine(location[0].x, location[0].y, location[1].x, location[1].y);
        g.setStroke(stroke);

//        g.drawString("" + edge, location[0].x + 5, location[0].y);

    }

    public void drawSettlement(Graphics2D g, int vertex, Point point, Color color, Building.Type type){

        /* point is the centre of the hexagon
        *  / \  settl.  / \___  city
        * |   |         |    |
        * -----         ------
        * */
        int RADIUS = 10;
        // Create a polygon to contain x,y coordinates
        Polygon settlement = new Polygon();
        settlement.addPoint(point.x - RADIUS/2, point.y-RADIUS/2);
        settlement.addPoint(point.x, point.y - RADIUS);
        settlement.addPoint(point.x + RADIUS/2, point.y-RADIUS/2);
        if (type == Building.Type.City){
            settlement.addPoint(point.x + RADIUS, point.y - RADIUS/2);
            settlement.addPoint(point.x + RADIUS, point.y + RADIUS/2);
        }
        settlement.addPoint(point.x + RADIUS/2, point.y+RADIUS/2);
        settlement.addPoint(point.x - RADIUS/2, point.y+RADIUS/2);

        g.setColor(color);
        g.fillPolygon(settlement);
        g.setColor(Color.BLACK);
        g.drawPolygon(settlement);

//        g.drawString("" + vertex, point.x, point.y);
    }
}
