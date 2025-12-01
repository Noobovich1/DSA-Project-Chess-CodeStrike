package piece;

import main.GamePanel;
import main.Type;

public class Pawn extends piece {

    public Pawn(int color, int col, int row) {
        super(color, col, row);
        type = Type.PAWN;

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
            // 2 square movement
            if (targetCol == preCOL && targetRow == preROW + moveValue*2 && hittingP == null && moved == false 
                && pieceIsOnStraightLine(targetCol, targetRow) == false){
                twoStepped = true;
                return true;
            }
            // Capture
            if (Math.abs(targetCol-preCOL) == 1 && targetRow == preROW + moveValue && hittingP != null && hittingP.color != color){
                return true;
            }
            //En Passant  (holy hell)
            if (Math.abs(targetCol-preCOL) == 1 && targetRow == preROW + moveValue){
                for (piece piece : GamePanel.simPieces){
                    if (piece.col == targetCol && piece.row == preROW && piece.twoStepped == true){
                        hittingP = piece;
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
