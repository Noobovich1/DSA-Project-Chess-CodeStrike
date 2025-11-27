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
            if(isWithinboard(targetCol,targetRow)&& (!isSameSquare(targetCol, targetRow))){
                    if(targetCol==preCOL || targetRow==preROW){
                        if(isvalidSquare(targetCol,targetRow)&&!pieceOntheStraightline(targetCol,targetRow)){
                            return true;
                        }
                    }

            }
            return false;
        }
    }