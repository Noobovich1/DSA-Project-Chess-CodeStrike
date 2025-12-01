package piece;

import main.GamePanel;
import main.Type;

public class Rook extends piece {

    public Rook(int color, int col, int row) {
        super(color, col, row);
        type = Type.ROOK;
        
        if (color == GamePanel.WHITE){
            image = getImage("/pieceImage/wrook");
        }else {
            image = getImage("/pieceImage/brook");
        }
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if (!isWithinboard(targetCol, targetRow) || isSameSquare(targetCol, targetRow)) {
            return false;
        }

        // Rook moves only in straight lines
        if (targetCol == preCOL || targetRow == preROW) {
            // Check if path is clear and destination is valid
            if (!pieceIsOnStraightLine(targetCol, targetRow) && isvalidSquare(targetCol, targetRow)) {
                return true;
            }
        }

        return false;
    }
}
