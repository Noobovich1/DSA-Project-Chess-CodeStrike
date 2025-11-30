package piece;

import main.GamePanel;
import main.Type;

public class King extends piece {

    public King(int color, int col, int row) {
        super(color, col, row);
        type = Type.KING;
        
        if (color == GamePanel.WHITE){
            image = getImage("/pieceImage/wking");
        }else {
            image = getImage("/pieceImage/bking");
        }
    }
    public boolean canMove(int targetCol,int targetRow){
        //yesking
        if(isWithinboard(targetCol,targetRow)){
            if(Math.abs(targetCol-preCOL)+Math.abs(targetRow-preROW)==1 ||
                    Math.abs((targetCol-preCOL)*(targetRow-preROW))==1){
                if(isvalidSquare(targetCol,targetRow)) {
                    return true;
                }
            }
        }
        //check castling
        if (moved == false){
            //short castling O-O
            if (targetCol == preCOL + 2 && targetRow == preROW && pieceIsOnStraightLine(targetCol, targetRow) == false){
                //we scan the piece 3 square to the right side of the starting king position (the rook) to check if it moved or not
                for (piece piece : GamePanel.simPieces){
                    if (piece.col == preCOL + 3 && piece.row == preROW && piece.moved == false){
                        GamePanel.castlingPiece = piece;
                        return true;
                    }
                }
            }
            //long castling O-O-O
            if (targetCol == preCOL - 2 && targetRow == preROW && pieceIsOnStraightLine(targetCol, targetRow) == false){
                piece p[] = new piece[2];
                for (piece piece : GamePanel.simPieces){
                    if (piece.col == preCOL - 3 && piece.row == targetRow){
                        p[0] = piece;
                    }
                    if (piece.col == preCOL - 4 && piece.row == targetRow){
                        p[1] = piece;
                    }
                    if (p[0] == null && p[1] != null && p[1].moved == false){
                        GamePanel.castlingPiece = p[1];
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
