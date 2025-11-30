package piece;

import main.GamePanel;
import main.Type;

public class Knight extends piece {

    public Knight(int color, int col, int row) {
        super(color, col, row);
        type = Type.KNIGHT;
        
        if (color == GamePanel.WHITE){
            image = getImage("/pieceImage/wknight");
        }else {
            image = getImage("/pieceImage/bknight");
        }
    }

    public boolean canMove(int targetCol, int targetRow){
        if(isWithinboard(targetCol, targetRow)){
            if((Math.abs(targetCol-preCOL) * Math.abs(targetRow - preROW)) == 2){
                if(isvalidSquare(targetCol, targetRow)){
                    return true;
                }
            }
        }
        return false;
    }
    
}
