package piece;

import main.*;

public class Knight extends piece {
    public Knight(int color, int col, int row) {
        super(color, col, row);
        type = Type.KNIGHT;
        image = getImage(color == GamePanel.WHITE ? "/pieceImage/wknight" : "/pieceImage/bknight");
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if (!isWithinBoard(targetCol, targetRow)) return false;

        int dc = Math.abs(targetCol - col);
        int dr = Math.abs(targetRow - row);

        if ((dc == 2 && dr == 1) || (dc == 1 && dr == 2)) {
            return isValidSquare(targetCol, targetRow);
        }

        return false;
    }
}