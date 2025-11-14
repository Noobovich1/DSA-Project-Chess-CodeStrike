package piece;

import main.GamePanel;

public class King extends piece {

    public King(int color, int col, int row) {
        super(color, col, row);
        
        if (color == GamePanel.WHITE){
            image = getImage("/pieceImage/wking");
        }else {
            image = getImage("/pieceImage/bking");
        }
    }
    public boolean canMove(int targetCol,int targetRow){
        if(isWithinboard(targetCol,targetRow)){
            if(Math.abs(targetCol-preCOL)+Math.abs(targetRow-preROW)==1 ||
                    Math.abs((targetCol-preCOL)*(targetRow-preROW))==1){
                if(isvalidSquare(targetCol,targetRow)) {
                    return true;
                }
            }
        }
        return false;
    }
}
