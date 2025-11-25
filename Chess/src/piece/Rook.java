package piece;

import main.GamePanel;

public class Rook extends piece {

    public Rook(int color, int col, int row) {
        super(color, col, row);

        if (color == GamePanel.WHITE) {
            image = getImage("/pieceImage/wrook");
        } else {
            image = getImage("/pieceImage/brook");
        }
    }
        public boolean canMove(int targetCol,int targetRow){
            if(isWithinboard(targetCol,targetRow)){
                if(isvalidSquare(targetCol,targetRow)) {
                    if((Math.abs(targetCol-preCOL)*Math.abs(targetRow-preROW)==0)&&(pieceOntheStraightline(targetCol,targetRow)==false)){
                        return true;
                    }
                }
            }
            return false;
        }
}