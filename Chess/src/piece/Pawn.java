package piece;

import main.GamePanel;
import main.Type;

public class Pawn extends piece {

    public Pawn(int color, int col, int row) {
        super(color, col, row);
        type = Type.PAWN;
        initImage(color);
    }

    private void initImage(int color) {
        if (color == GamePanel.WHITE){
            image = getImage("/pieceImage/wpawn");
        }else {
            image = getImage("/pieceImage/bpawn");
        }
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if (!isWithinboard(targetCol, targetRow) || isSameSquare(targetCol, targetRow)) {
            return false;
        }

        int moveValue = (color == GamePanel.WHITE) ? -1 : 1;

        // Reset hittingP at the start
        hittingP = null;

        // 1-square forward move
        if (targetCol == preCOL && targetRow == preROW + moveValue) {
            if (getHittingP(targetCol, targetRow) == null) {
                return true;
            }
        }

        // 2-square forward move
        
        if (targetCol == preCOL && targetRow == preROW + moveValue * 2 && !moved) {
            if (getHittingP(targetCol, targetRow) == null &&
                pieceIsOnStraightLine(targetCol, targetRow) == false) {
                return true;
            }
        }

        // Diagonal moves (normal capture + en passant combined)
       
        if (Math.abs(targetCol - preCOL) == 1 && targetRow == preROW + moveValue) {
            
            // Normal capture - check target square first
            piece target = getHittingP(targetCol, targetRow);
            if (target != null && target.color != color) {
                hittingP = target;
                return true;
            }
            
            // En Passant - check square beside us
            
            for (piece p : GamePanel.simPieces) {
                if (p.col == targetCol && 
                    p.row == preROW && 
                    p.twoStepped && 
                    p.color != this.color) {  
                    hittingP = p;
                    return true;
                }
            }
        }

        return false;
    }
}