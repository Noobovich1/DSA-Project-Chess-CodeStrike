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

        // --- CASTLING LOGIC FIX ---
        if (!moved && targetRow == row) {
            // King Side Castling (Target col 6)
            if (targetCol == 6) {
                piece rook = GamePanel.board[7][row];
                if (rook instanceof Rook && !rook.moved && GamePanel.board[5][row] == null && GamePanel.board[6][row] == null) {
                    return true;
                }
            }
            // Queen Side Castling (Target col 2)
            else if (targetCol == 2) {
                piece rook = GamePanel.board[0][row];
                // MUST check col 1, 2, and 3 (b, c, d)
                if (rook instanceof Rook && !rook.moved && 
                    GamePanel.board[1][row] == null && 
                    GamePanel.board[2][row] == null && 
                    GamePanel.board[3][row] == null) {
                    return true;
                }
            }
        }

        return false;
    }
}