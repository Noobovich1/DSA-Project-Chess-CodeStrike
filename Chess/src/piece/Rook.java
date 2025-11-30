package piece;

import main.GamePanel;
import main.Type;

public class Rook extends piece {

    public Rook(int color, int col, int row) {
        super(color, col, row);
        type = Type.ROOK;
        
        if (color == GamePanel.WHITE){
            image = getImage("/pieceImage/wrook");
        } else {
            image = getImage("/pieceImage/brook");
        }
    }

    public boolean canMove(int targetCol, int targetRow){
        if(isWithinboard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false){
            if(targetCol==preCOL || targetRow==preROW){
                if(isvalidSquare(targetCol, targetRow) && pieceIsOnStraightLine(targetCol, targetRow) == false){
                    return true;
                }
            }
        }
        return false;
    }
}
