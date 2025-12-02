package piece;

import main.GamePanel;
import main.Type;

public class Queen extends piece {

    public Queen(int color, int col, int row) {
        super(color, col, row);
        type = Type.QUEEN;
        initImage(color);
    }

    private void initImage(int color) {
        if (color == GamePanel.WHITE){
            image = getImage("/pieceImage/wqueen");
        }else {
            image = getImage("/pieceImage/bqueen");
        }
    }

    // queen is the combination of rook and bishop cuh
    @Override
    public boolean canMove(int targetCol, int targetRow){
        if(isWithinboard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false){
            // straight line movement
            if(targetCol == preCOL || targetRow == preROW){
                if(isvalidSquare(targetCol, targetRow) && pieceIsOnStraightLine(targetCol, targetRow) == false){
                    return true;
                }
            }

            // diagonal movement
            if(Math.abs(targetCol - preCOL) == Math.abs(targetRow - preROW)){
                if(isvalidSquare(targetCol, targetRow) && pieceIsOnDiagonalLine(targetCol, targetRow) == false){
                    return true;
                }
            }
        }
        return false;
    }
}
