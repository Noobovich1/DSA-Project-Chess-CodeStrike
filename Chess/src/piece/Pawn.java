package piece;

import main.GamePanel;

public class Pawn extends piece {

    public Pawn(int color, int col, int row) {
        super(color, col, row);
        
        if (color == GamePanel.WHITE){
            image = getImage("/pieceImage/wpawn");
        }else {
            image = getImage("/pieceImage/bpawn");
        }
    }
}
