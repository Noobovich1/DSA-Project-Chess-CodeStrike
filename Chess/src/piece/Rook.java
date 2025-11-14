package piece;

import main.GamePanel;

public class Rook extends piece {

    public Rook(int color, int col, int row) {
        super(color, col, row);
        
        if (color == GamePanel.WHITE){
            image = getImage("/pieceImage/wrook");
        }else {
            image = getImage("/pieceImage/brook");
        }
    }
}
