package piece;

import main.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public abstract class piece {
    public BufferedImage image;
    public int x, y;
    public int col, row, preCOL, preROW;
    public int color;
    public boolean moved = false, twoStepped = false;
    public Type type;

    public piece(int color, int col, int row) {
        this.color = color;
        this.col = col;
        this.row = row;
        x = col * Board.SQUARE_SIZE;
        y = row * Board.SQUARE_SIZE;
        preCOL = col;
        preROW = row;
    }

    public BufferedImage getImage(String path) {
        try {
            return ImageIO.read(getClass().getResourceAsStream(path + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updatePos() {
        if (type == Type.PAWN && Math.abs(row - preROW) == 2) twoStepped = true;
        x = col * Board.SQUARE_SIZE;
        y = row * Board.SQUARE_SIZE;
        preCOL = col;
        preROW = row;
        moved = true;
    }

    public void resetPosition() {
        col = preCOL;
        row = preROW;
        x = col * Board.SQUARE_SIZE;
        y = row * Board.SQUARE_SIZE;
    }

    public boolean isWithinBoard(int col, int row) {
        return col >= 0 && col < 8 && row >= 0 && row < 8;
    }

    public boolean isValidSquare(int targetCol, int targetRow) {
        piece p = GamePanel.board[targetCol][targetRow];
        return p == null || p.color != this.color;
    }

    public boolean isSameSquare(int col, int row) {
        return col == this.col && row == this.row;
    }

    public boolean isPathClearStraight(int targetCol, int targetRow) {
        int dx = Integer.signum(targetCol - col);
        int dy = Integer.signum(targetRow - row);
        int x = col + dx;
        int y = row + dy;
        while (x != targetCol || y != targetRow) {
            if (GamePanel.board[x][y] != null) return false;
            x += dx;
            y += dy;
        }
        return true;
    }

    public boolean isPathClearDiagonal(int targetCol, int targetRow) {
        int dx = Integer.signum(targetCol - col);
        int dy = Integer.signum(targetRow - row);
        int x = col + dx;
        int y = row + dy;
        while (x != targetCol || y != targetRow) {
            if (GamePanel.board[x][y] != null) return false;
            x += dx;
            y += dy;
        }
        return true;
    }

    public ArrayList<Point> getLegalMoves() {
        ArrayList<Point> moves = new ArrayList<>();
        for (int c = 0; c < 8; c++) {
            for (int r = 0; r < 8; r++) {
                if (canMove(c, r) && !leavesKingInCheck(c, r)) {
                    moves.add(new Point(c, r));
                }
            }
        }
        return moves;
    }

    private boolean leavesKingInCheck(int toCol, int toRow) {
        piece[][] b = GamePanel.board;
        piece captured = b[toCol][toRow];
        
        //simulate the move on the board array
        b[col][row] = null;
        b[toCol][toRow] = this;
        
        int oldC = col; 
        int oldR = row;
        col = toCol; 
        row = toRow;

        // Find our King
        piece king = null;
        // Accessing GamePanel.pieces here needs synchronization too if called from UI
        synchronized(GamePanel.pieces) {
            for (piece p : GamePanel.pieces) {
                if (p.type == Type.KING && p.color == this.color) {
                    king = p;
                    break;
                }
            }
        }

        boolean check = false;
        if (king != null) {
            check = king.isAttacked(captured);
        }

        //Restore the board
        b[toCol][toRow] = captured;
        col = oldC; 
        row = oldR;
        b[col][row] = this;

        return check;
    }

    public boolean isAttacked() {
        return isAttacked(null);
    }

    // --- FIXED: ADDED SYNCHRONIZATION TO PREVENT CRASH ---
    public boolean isAttacked(piece ignoredPiece) {
        synchronized(GamePanel.pieces) {
            for (piece p : GamePanel.pieces) {
                if (p == ignoredPiece) continue; 
                if (p.color != this.color && p.canMove(this.col, this.row)) {
                    return true;
                }
            }
        }
        return false;
    }

    public abstract boolean canMove(int targetCol, int targetRow);

    public void draw(Graphics2D g2) {
        g2.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
    }
}