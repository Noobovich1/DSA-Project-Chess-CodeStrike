package piece;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import main.Board;
import main.GamePanel;

public class piece {
    
    public BufferedImage image;
    public int x,y;
    public int col, row, preCOL, preROW;
    public int color;
    public piece hittingP;
    public boolean moved, twoStepped;

    public piece(int color, int col, int row){
        this.color = color;
        this.row = row;
        this.col = col;
        x = getX(col);
        y = getY(row);
        preCOL = col;
        preROW = row;
    }

    public BufferedImage getImage(String imagePath){
        BufferedImage image = null;
        try {
            image = ImageIO.read(getClass().getResourceAsStream(imagePath+".png"));
        }catch(IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public int getX(int col){
        return col*Board.SQUARE_SIZE;
    }

    public int getY(int row){
        return row*Board.SQUARE_SIZE;
    }

    //detect the centre of the piece coordinate
    public int getCol(int x){
        return (x + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    public int getRow(int y){
        return (y + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    public void updatePos(){
        x = getX(col);
        y = getY(row);
        preCOL = getCol(x);
        preROW = getRow(y);
        moved = true;
    }
    public void resetPosition(){
        col=preCOL;
        row=preROW;
        x=getX(col);
        y=getY(row);
    }
    public piece getHittingP(int targetCol,int targetRow){
        for(piece piece: GamePanel.simPieces){
            if(piece.col==targetCol && piece.row==targetRow && piece!=this){
                return piece;
            }
        }
        return null;
    }
    //FUNCTION ONLY USE FOR REWRITE PURPOSE ONLY DO NOT TOUCH OR I WILL TOUCH YOU
    public boolean canMove(int targetCol, int targetRow){
        return false;
    }
    //CHECK IF IT IN THE BOARD
    public boolean isWithinboard(int targetCol,int targetRow){
        if((targetCol>=0 && targetCol<=7)&&(targetRow>=0 && targetRow<=7) ){
            return true;
        }
        return false;
    }
    // CHECK THE SQUARE IS OCCUPIED OR NOT
    public boolean  isvalidSquare(int targetCol,int targetRow){
        hittingP=getHittingP(targetCol,targetRow);
        if(hittingP==null){
            return  true;
        }
        else{
            if(hittingP.color!=this.color){
                return true;
            }
            else{
                hittingP=null;
            }
        }
        return false;
    }
    public int getIndexofpiece(){
        for(int i = 0; i<GamePanel.simPieces.size(); i++){
            if(GamePanel.simPieces.get(i)==this){
                return i;
            }
        }
        return 0;
    }

    public void draw(Graphics2D g2){
        g2.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
    }
}
