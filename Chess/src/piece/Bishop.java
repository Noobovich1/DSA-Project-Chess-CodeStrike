
package piece;

import main.GamePanel;

public class Bishop extends piece {

    public Bishop(int color, int col, int row) {
        super(color, col, row);
        
        if (color == GamePanel.WHITE){
            image = getImage("/pieceImage/wbishop");
        }else {
            image = getImage("/pieceImage/bbishop");
        }
    }
}
