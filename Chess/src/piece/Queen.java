package piece;

import main.GamePanel;
import main.Type;

public class Queen extends piece {

    public Queen(int color, int col, int row) {
        super(color, col, row);
        type = Type.QUEEN;
        
        if (color == GamePanel.WHITE){
            image = getImage("/pieceImage/wqueen");
        }else {
            image = getImage("/pieceImage/bqueen");
        }
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        //vertical & Horizontal
        if(targetCol==preCOL || targetRow==preROW){
            if(isvalidSquare(targetCol,targetRow)&& pieceOntheStraightline(targetCol,targetRow)==false){
                return true;
            }
        }
        //DiagonalMovemment
        if(Math.abs(targetCol-preCOL)==Math.abs(targetRow-preROW)){
            if(isvalidSquare(targetCol,targetRow)&& pieceOnDiagonalmovement(targetCol,targetRow)==false){
                return true;
            }
        }
        return false;
    }
}
