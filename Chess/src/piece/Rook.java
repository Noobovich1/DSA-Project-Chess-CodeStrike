package piece;

import main.*;

public class Rook extends piece {
    public Rook(int color, int col, int row) {
        super(color, col, row);
        type = Type.ROOK;
        image = getImage(color == GamePanel.WHITE ? "/pieceImage/wrook" : "/pieceImage/brook");
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if (!isWithinBoard(targetCol, targetRow) || isSameSquare(targetCol, targetRow)) return false;

        if (targetCol == col || targetRow == row) {
            return isPathClearStraight(targetCol, targetRow) && isValidSquare(targetCol, targetRow);
        }

        return false;
    }
}