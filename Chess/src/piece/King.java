package piece;

import main.*;

public class King extends piece {
    public King(int color, int col, int row) {
        super(color, col, row);
        type = Type.KING;
        image = getImage(color == GamePanel.WHITE ? "/pieceImage/wking" : "/pieceImage/bking");
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if (!isWithinBoard(targetCol, targetRow) || isSameSquare(targetCol, targetRow)) return false;

        int dc = Math.abs(targetCol - col);
        int dr = Math.abs(targetRow - row);

        // Normal king move
        if (dc <= 1 && dr <= 1) {
            return isValidSquare(targetCol, targetRow);
        }

        // Castling
        if (!moved && targetRow == row && (targetCol == 2 || targetCol == 6)) {
            int rookCol = targetCol == 6 ? 7 : 0;
            piece rook = GamePanel.board[rookCol][row];
            if (rook instanceof Rook && !rook.moved) {
                // Check path is clear
                int step = targetCol > col ? 1 : -1;
                for (int c = col + step; c != targetCol; c += step) {
                    if (GamePanel.board[c][row] != null) return false;
                }
                if (GamePanel.board[targetCol][row] == null) {
                    return true;
                }
            }
        }

        return false;
    }
}