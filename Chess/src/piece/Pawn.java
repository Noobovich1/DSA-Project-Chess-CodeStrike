package piece;

import main.GamePanel;

public class Pawn extends piece {

    public Pawn(int color, int col, int row) {
        super(color, col, row);
        
        if (color == GamePanel.WHITE){
            image = getImage("/pieceImage/wpawn");
        }else {
            image = getImage("/pieceImage/bpawn");
        }
    }
    public boolean canMove(int targetCol,int targetRow){
        if(isWithinboard(targetCol,targetRow) && isSameSquare(targetCol,targetRow)==false){
            int moveValue;
            if(color==GamePanel.WHITE){
                moveValue=-1;
            }
            //BLACK
            else {
                moveValue=1;
            }
            //check the hitting P
            hittingP=getHittingP(targetCol,targetRow);
            //1 square movement
            if(targetCol==preCOL && targetRow==preROW+moveValue  && hittingP==null ){
                return true;
            }
        }
        return false;
    }
}
