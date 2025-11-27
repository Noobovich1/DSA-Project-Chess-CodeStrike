
package piece;

import main.GamePanel;

public class Bishop extends piece {

    public Bishop(int color, int col, int row) {
        super(color, col, row);
        
        if (color == GamePanel.WHITE){
            image = getImage("/pieceImage/wbishop");
        }else {
            image = getImage("/pieceImage/bbishop");
        }
    }
    @Override
    public boolean canMove(int targetCol,int targetRow){
        if(isWithinboard(targetCol,targetRow)&&!isSameSquare(targetCol,targetRow)){
            if((Math.abs(targetCol-preCOL)==Math.abs(targetRow-preROW) )){
                if((isvalidSquare(targetCol,targetRow))&&pieceOnDiagonalmovement(targetCol,targetRow)==false){
                    return true;
                }
            }
        }
        return false;
    }
}
