package piece;

import main.*;

public class Queen extends piece {
    public Queen(int color, int col, int row) {
        super(color, col, row);
        type = Type.QUEEN;
        image = getImage(color == GamePanel.WHITE ? "/pieceImage/wqueen" : "/pieceImage/bqueen");
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if (!isWithinBoard(targetCol, targetRow) || isSameSquare(targetCol, targetRow)) return false;

        int dc = targetCol - col;
        int dr = targetRow - row;

        // Straight
        if (targetCol == col || targetRow == row) {
            return isPathClearStraight(targetCol, targetRow) && isValidSquare(targetCol, targetRow);
        }

        // Not Straight nor horsey
        if (Math.abs(dc) == Math.abs(dr)) {
            return isPathClearDiagonal(targetCol, targetRow) && isValidSquare(targetCol, targetRow);
        }

        return false;
    }
}