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
    }
    public void resetPosition(){
        col=preCOL;
        row=preROW;
        x=getX(col);
        y=getY(row);
    }
    public piece getHittingP(int targetCol,int targetRow){
        for(piece piece: GamePanel.sPieces){
            if(piece.col==targetCol && piece.row==targetRow && piece!=this){
                return piece;
            }
        }
        return null;
    }
    //FUNCTION ONLY USE FOR REWRITE PURPOSE ONLY DO NOT TOUCH OR I WILL TOUCH YOU
    public  boolean canMove(int targetCol, int targetRow){
        return false;
    }
    //CHECK IF IT IN THE BOARD
    public boolean isWithinboard(int targetCol,int targetRow){
        if((targetCol>=0 && targetCol<=7)&&(targetRow>=0 && targetRow<=7) ){
            return true;
        }
        return false;
    }
    //CHECK THE PIECE ON THE SAME SQUARE
    public boolean isSameSquare(int targetCol,int targetRow){
        if(targetCol==preCOL && targetRow==preROW){
            return true;
        }
        return false;
    }
    // CHECK IF PIECE IS ON STRAIGHT LINE
    public boolean pieceOntheStraightline(int targetCol,int targetRow){
        //when the piece moving the left
        for(int c= preCOL-1;c>targetCol;c--){
            for(piece piece: GamePanel.sPieces){
                if(piece.col==c && piece.row==targetRow){
                    hittingP=piece;
                    return true;
                }
            }
        }
        //when the piece mobing right
        for(int c= preCOL+1;c<targetCol;c++){
            for(piece piece: GamePanel.sPieces){
                if(piece.col==c && piece.row==targetRow){
                    hittingP=piece;
                    return true;
                }
            }
        }
        //when the piece moving down
        for(int r= preROW+1;r<targetRow;r++){
            for(piece piece: GamePanel.sPieces){
                if(piece.row==r && piece.col==targetCol){
                    hittingP=piece;
                    return true;
                }
            }
        }
        //when the pig fly(moving up)
        for(int r= preROW-1;r>targetRow;r--){
            for(piece piece: GamePanel.sPieces){
                if(piece.row==r && piece.col==targetCol){
                    hittingP=piece;
                    return true;
                }
            }
        }
        return false;
    }
    //CHECK FOR THE DIAGONAL MOVEMENT
    public boolean pieceOnDiagonalmovement(int targetCol,int targertRow){
        if(targertRow < preROW){
            //UPLEFT
            for(int c=preCOL-1;c>targetCol;c--){
                int diff=Math.abs(c-preCOL);
                for(piece piece:GamePanel.sPieces){
                    if(piece.col==c && piece.row==preROW-diff){
                        hittingP=piece;
                        return true;
                    }
                }
            }
            //UP RIGHT
            for(int c=preCOL+1;c<targetCol;c++) {
                int diff = Math.abs(c - preCOL);
                for (piece piece : GamePanel.sPieces) {
                    if (piece.col == c && piece.row == preROW + diff) {
                        hittingP = piece;
                        return true;
                    }
                }
            }
        }
        if(targertRow>preROW){
            //DOWNLEFT
            for(int c=preCOL-1;c>targetCol;c--){
                int diff=Math.abs(c-preCOL);
                    for(piece piece:GamePanel.sPieces){
                        if(piece.col==c && piece.row==preROW+diff){
                            hittingP= piece;
                            return true;
                        }
                    }

            }
            //DOWNRIGHT
            for(int c=preCOL+1;c<targetCol;c++){
                int diff=Math.abs(c-preCOL);
                    for(piece piece:GamePanel.sPieces){
                        if(piece.col==c && piece.row==preROW+diff){
                            hittingP= piece;
                            return true;
                        }
                    }

            }
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
        for(int i=0;i<GamePanel.sPieces.size();i++){
            if(GamePanel.sPieces.get(i)==this){
                return i;
            }
        }
        return 0;
    }

    public void draw(Graphics2D g2){
        g2.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
    }
}
