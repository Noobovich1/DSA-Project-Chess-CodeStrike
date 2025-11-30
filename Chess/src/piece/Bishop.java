
package piece;

import main.GamePanel;
import main.Type;

public class Bishop extends piece {

    public Bishop(int color, int col, int row) {
        super(color, col, row);
        type = Type.BISHOP;
        
        if (color == GamePanel.WHITE){
            image = getImage("/pieceImage/wbishop");
        }else {
            image = getImage("/pieceImage/bbishop");
        }
    }

    public boolean canMove(int targetCol, int targetRow){
        if(isWithinboard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false){
            if(Math.abs(targetCol - preCOL) == Math.abs(targetRow - preROW) && pieceIsOnDiagonalLine(targetCol, targetRow) == false){
                if(isvalidSquare(targetCol, targetRow)){
                    return true;
                }
            }
        }
        return false;
    }
}
