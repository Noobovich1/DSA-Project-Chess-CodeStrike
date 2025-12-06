package piece;

import main.*;

public class Bishop extends piece {
    public Bishop(int color, int col, int row) {
        super(color, col, row);
        type = Type.BISHOP;
        image = getImage(color == GamePanel.WHITE ? "/pieceImage/wbishop" : "/pieceImage/bbishop");
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if (!isWithinBoard(targetCol, targetRow) || isSameSquare(targetCol, targetRow)) return false;

        int dc = Math.abs(targetCol - col);
        int dr = Math.abs(targetRow - row);

        if (dc == dr) {
            return isPathClearDiagonal(targetCol, targetRow) && isValidSquare(targetCol, targetRow);
        }

        return false;
    }
}