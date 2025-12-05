// src/piece/Pawn.java
package piece;

import main.*;

public class Pawn extends piece {
    public Pawn(int color, int col, int row) {
        super(color, col, row);
        type = Type.PAWN;
        image = getImage(color == GamePanel.WHITE ? "/pieceImage/wpawn" : "/pieceImage/bpawn");
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if (!isWithinBoard(targetCol, targetRow)) return false;

        int direction = color == GamePanel.WHITE ? -1 : 1;
        
        // 1. Standard Move (1 square forward)
        if (targetCol == col && targetRow == row + direction && GamePanel.board[targetCol][targetRow] == null) {
            return true;
        }

        // 2. Double Move (2 squares forward from start)
        if (!moved && targetCol == col && targetRow == row + 2*direction 
            && GamePanel.board[col][row + direction] == null 
            && GamePanel.board[targetCol][targetRow] == null) {
            return true;
        }

        // 3. Diagonal Capture & En Passant
        if (Math.abs(targetCol - col) == 1 && targetRow == row + direction) {
            piece target = GamePanel.board[targetCol][targetRow];
            
            // Normal diagonal capture
            if (target != null && target.color != color) {
                return true;
            }

            // En Passant Logic
            // The square we move TO is empty...
            if (target == null) {
                // ...but there is an enemy pawn adjacent to us (at targetCol, but on our same row)
                piece passingPawn = GamePanel.board[targetCol][row];
                
                if (passingPawn != null && passingPawn instanceof Pawn && passingPawn.color != color) {
                    // And that pawn just moved 2 steps in the previous turn
                    if (passingPawn.twoStepped) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}