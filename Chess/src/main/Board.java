package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.*;

public class Board{
    final int MAX_ROW = 8;
    final int MAX_COL = 8;
    public static final int SQUARE_SIZE = 100;
    public static final int HALF_SQUARE_SIZE = SQUARE_SIZE/2;

    private BufferedImage lightTile;
    private BufferedImage darkTile;

    private final int[][] tileMap = {
            {1, 0, 1, 0, 1, 0, 1, 0},
            {0, 1, 0, 1, 0, 1, 0, 1},
            {1, 0, 1, 0, 1, 0, 1, 0},
            {0, 1, 0, 1, 0, 1, 0, 1},
            {1, 0, 1, 0, 1, 0, 1, 0},
            {0, 1, 0, 1, 0, 1, 0, 1},
            {1, 0, 1, 0, 1, 0, 1, 0},
            {0, 1, 0, 1, 0, 1, 0, 1}
    };

    public Board(){
        loadTileImages();
    }

    private void loadTileImages(){
        try{
            BufferedImage rawLight = ImageIO.read(getClass().getResourceAsStream("/boardTiles/wtile.png"));
            BufferedImage rawDark = ImageIO.read(getClass().getResourceAsStream("/boardTiles/btile.png"));

            lightTile = scaleImage(rawLight, SQUARE_SIZE, SQUARE_SIZE);
            darkTile = scaleImage(rawDark, SQUARE_SIZE, SQUARE_SIZE);
        } catch (IOException e){
            System.err.println("Failed to load tile images!");
            e.printStackTrace();
        } catch (IllegalArgumentException e){
            System.err.println("Tile images not found in /boardTiles/!");
            e.printStackTrace();
        }
    }

    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, original.getType());
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        return scaled;
    }

    public void draw(Graphics2D g2){
        for(int row = 0; row < MAX_ROW; row++){
            for(int col = 0; col < MAX_COL; col++){
                //read the tile index from our map
                int tileType = tileMap[row][col];

                //draw the right tile
                if(tileType == 0){
                    g2.drawImage(lightTile, col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE, null);
                } else{
                    g2.drawImage(darkTile, col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE, null);
                }
            }
        }
    }

    /*public void draw(Graphics2D g2){
        int c = 0;
        for (int row = 0; row < MAX_ROW; row++){
            for (int col = 0; col < MAX_COL; col++){
            if (c == 0){
                g2.setColor(new Color(238, 238, 210));
                c = 1;
            } else {
                g2.setColor(new Color(118, 150, 86));
                c = 0;
            }
                g2.fillRect(col*SQUARE_SIZE , row*SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
            if (c == 0){
                c = 1;
            } else{
                c = 0;
            }
        }
    }*/
}