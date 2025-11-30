package main;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.JPanel;

import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.piece;
import piece.Queen;
import piece.Rook;

public class GamePanel extends JPanel implements Runnable{
    public static final int WIDTH = 1200;
    public static final int HEIGHT = 800;
    final int FPS = 60;
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();
    Keyboard keyboard=new Keyboard();

    //Color
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int CURRENT_COLOR = WHITE;

    //ArrayList for pieces
    public static ArrayList<piece> pieces = new ArrayList<>(); //back up for such as undo move
    public static ArrayList<piece> simPieces = new ArrayList<>();
    ArrayList<piece> promotePieces = new ArrayList<>();
    piece aPiece, checkingPiece; //handle the piece that the player is holding
    public static piece castlingPiece;
    

    //BOOLEAN
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver;
    boolean stalemate;

    public GamePanel(){
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        addKeyListener(keyboard);
        requestFocusInWindow();
        setFocusable(true);

        setPieces();
        copyPieces(pieces, simPieces);
    }

    public void launch(){
        gameThread = new Thread(this);
        gameThread.start(); //call the run method
    }

    public void setPieces(){
        //White
        pieces.add(new Pawn(WHITE, 0, 6));
        pieces.add(new Pawn(WHITE, 1, 6));
        pieces.add(new Pawn(WHITE, 2, 6));
        pieces.add(new Pawn(WHITE, 3, 6));
        pieces.add(new Pawn(WHITE, 4, 6));
        pieces.add(new Pawn(WHITE, 5, 6));
        pieces.add(new Pawn(WHITE, 6, 6));
        pieces.add(new Pawn(WHITE, 7, 6));
        pieces.add(new Rook(WHITE, 0, 7));
        pieces.add(new Rook(WHITE, 7, 7));
        pieces.add(new Knight(WHITE, 1, 7));
        pieces.add(new Knight(WHITE, 6, 7));
        pieces.add(new Bishop(WHITE, 2, 7));
        pieces.add(new Bishop(WHITE, 5, 7));
        pieces.add(new Queen(WHITE, 3, 7));
        pieces.add(new King(WHITE, 4, 7));
        //Black
        pieces.add(new Pawn(BLACK, 0, 1));
        pieces.add(new Pawn(BLACK, 1, 1));
        pieces.add(new Pawn(BLACK, 2, 1));
        pieces.add(new Pawn(BLACK, 3, 1));
        pieces.add(new Pawn(BLACK, 4, 1));
        pieces.add(new Pawn(BLACK, 5, 1));
        pieces.add(new Pawn(BLACK, 6, 1));
        pieces.add(new Pawn(BLACK, 7, 1));
        pieces.add(new Rook(BLACK, 0, 0));
        pieces.add(new Rook(BLACK, 7, 0));
        pieces.add(new Knight(BLACK, 1, 0));
        pieces.add(new Knight(BLACK, 6, 0));
        pieces.add(new Bishop(BLACK, 2, 0));
        pieces.add(new Bishop(BLACK, 5, 0));
        pieces.add(new Queen(BLACK, 3, 0));
        pieces.add(new King(BLACK, 4, 0));
    }
    //testing(only)
    //PRESS R TO RESET THE GAME
    public void ResetGame(){
        pieces.clear();
        simPieces.clear();

        // Reset game state
        aPiece = null;
        canMove = false;
        validSquare = false;
        CURRENT_COLOR = WHITE;

        // Set up pieces again
        setPieces();
        copyPieces(pieces, simPieces);

        // Request focus to ensure keyboard input continues to work
        requestFocusInWindow();

        System.out.println("Game Reset!");
    }

    private void copyPieces(ArrayList<piece> source, ArrayList<piece> target){
        target.clear();
        for (int i = 0; i < source.size(); i++){
            target.add(source.get(i));
        }
    }

    @Override
    public void run(){
        //game loop
        double Interval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null){
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime)/Interval;
            lastTime = currentTime;

            if (delta >= 1){
                update();
                repaint();
                delta--;
            }
        }
    }

private void update(){
    // Mouse pressed
    if (promotion == true){
        promoting();
        return;
    }

    if (gameOver) return;

    if (mouse.pressed){
        if (aPiece == null){
            // check if aPiece (active piece) is null or not
            for (piece piece : simPieces){
                // if mouse is currently on an ally piece, allow mouse interaction with them as aPiece
                if (piece.color == CURRENT_COLOR &&
                    piece.col == mouse.x / Board.SQUARE_SIZE &&
                    piece.row == mouse.y / Board.SQUARE_SIZE){
                    aPiece = piece;
                    break;
                }
            }
        } else {
            // if the player is holding a piece, simulate the move
            simulate();
        }
    }

    // Mouse released
    if (!mouse.pressed){
        if (aPiece != null){
            if (validSquare){
                // MOVE CONFIRMED
                copyPieces(simPieces, pieces);
                aPiece.updatePos();
                if (castlingPiece != null){
                    castlingPiece.updatePos();
                }

                // If the move results in a promotion, handle promotion first (player must choose)
                if (canPromote()){
                    promotion = true;
                    // keep aPiece set so promoting() can replace it
                    return;
                }

                // Switch to the opponent (they are now the side to move)
                changePlayer();

                // After switching, check if the side to move is in checkmate
                if (currentlyInCheck() && isCheckmate()){
                    gameOver = true;
                }

                if (isStalemate() && !currentlyInCheck()){
                    stalemate = true;
                }

            } else {
                // invalid move: revert simulation
                copyPieces(pieces, simPieces);
                aPiece.resetPosition();
                aPiece = null; // reset to the original row and col
            }
        }
    }
}

    private void simulate(){
        canMove=false;
        validSquare=false;
        copyPieces(pieces, simPieces);
        
        //fix the bug where if a player pick up a king and not castle, the rook would then be teleport to castled position despite no castle has been made 
        if (castlingPiece != null){
            castlingPiece.col = castlingPiece.preCOL;
            castlingPiece.x = castlingPiece.getX(castlingPiece.col);
            castlingPiece = null;
        }
        //landing position for picked up piece simulation
        aPiece.x = mouse.x - Board.HALF_SQUARE_SIZE;
        aPiece.y = mouse.y - Board.HALF_SQUARE_SIZE;
        aPiece.col = aPiece.getCol(aPiece.x);
        aPiece.row = aPiece.getRow(aPiece.y);
        if(aPiece.canMove(aPiece.col,aPiece.row)){
            canMove=true;
            if(aPiece.hittingP!=null){
                simPieces.remove(aPiece.hittingP.getIndexofpiece());
            }
            checkCastling();

            if (!isIllegal(aPiece) && !currentlyInCheck()){
                validSquare=true;
            }
        }
    }

    public boolean isIllegal(piece king){
        // Always evaluate king safety for the current player's king on simPieces
        piece k = getKing(false); // current player's king
        if (k == null) return false; // safety guard
        for (piece p : simPieces){
            if (p != k && p.color != k.color && p.canMove(k.col, k.row)){
                return true;
            }
        }
        return false;
    }

    private boolean currentlyInCheck(){
        piece king = getKing(false);
        if (king == null) return false; // avoid NPE and treat as not in check
        for (piece piece : simPieces){
            if (piece.color != king.color && piece.canMove(king.col, king.row)){
                return true;
            }
        }
        return false;
    }

    private boolean isKingInCheck(){
        piece king = getKing(true); // opponent's king (used for highlighting/checking)
        if (king == null || aPiece == null) {
            checkingPiece = null;
            return false;
        }
        if (aPiece.canMove(king.col, king.row)){
            checkingPiece = aPiece;
            return true;
        } else {
            checkingPiece = null;
            return false;
        }
    }

    private piece getKing(boolean opponent){
        for (piece piece : simPieces){
            if (piece.type == Type.KING){
                if (opponent && piece.color != CURRENT_COLOR){
                    return piece;
                } else if (!opponent && piece.color == CURRENT_COLOR){
                    return piece;
                }
            }
        }
        return null;
    }


    private boolean isCheckmate() {
        // sync simPieces with the real board
        copyPieces(pieces, simPieces);

        piece king = getKing(false); // current player's king
        if (king == null) return false;

        // if king not in check, not checkmate
        if (!currentlyInCheck()) return false;

        // if king has any legal move, not checkmate
        if (kingLegalMovement(king)) return false;

        // find attackers
        ArrayList<piece> attackers = new ArrayList<>();
        for (piece p : simPieces) {
            if (p.color != king.color && p.canMove(king.col, king.row)) {
                attackers.add(p);
            }
        }

        // if multiple attackers, only king moves can escape (already tested)
        if (attackers.size() > 1) return true;

        // single attacker
        piece attacker = attackers.get(0);

        // check if any friendly piece can capture the attacker
        for (piece p : new ArrayList<>(simPieces)) {
            if (p.color == king.color && p != king) {
                if (p.canMove(attacker.col, attacker.row)) {
                    // temporarily move p to attacker square
                    int oldCol = p.col, oldRow = p.row;
                    p.col = attacker.col; p.row = attacker.row;
                    boolean stillInCheck = currentlyInCheck();
                    // restore
                    p.col = oldCol; p.row = oldRow;
                    if (!stillInCheck) return false;
                }
            }
        }

        // check if any piece can block the attack (only for sliding attackers)
        int colDiff = attacker.col - king.col;
        int rowDiff = attacker.row - king.row;
        int stepCol = Integer.signum(colDiff);
        int stepRow = Integer.signum(rowDiff);

        // only straight or diagonal lines
        if (colDiff == 0 || rowDiff == 0 || Math.abs(colDiff) == Math.abs(rowDiff)) {
            int c = king.col + stepCol;
            int r = king.row + stepRow;
            while (c != attacker.col || r != attacker.row) {
                for (piece p : new ArrayList<>(simPieces)) {
                    if (p.color == king.color && p != king) {
                        if (p.canMove(c, r)) {
                            int oldCol = p.col, oldRow = p.row;
                            p.col = c; p.row = r;
                            boolean stillInCheck = currentlyInCheck();
                            p.col = oldCol; p.row = oldRow;
                            if (!stillInCheck) return false;
                        }
                    }
                }
                c += stepCol;
                r += stepRow;
            }
        }

        // no escape found -> checkmate
        return true;
    }

    private boolean isStalemate() {
        // sync simPieces with the real board
        copyPieces(pieces, simPieces);

        // if side to move is in check, not stalemate
        if (currentlyInCheck()) return false;

        // try every piece of the side to move
        for (piece p : simPieces) {
            if (p.color != CURRENT_COLOR) continue;

            int originalCol = p.col;
            int originalRow = p.row;

            // try every destination square
            for (int c = 0; c < 8; c++) {
                for (int r = 0; r < 8; r++) {
                    if (!p.canMove(c, r)) continue;

                    // temporarily move
                    p.col = c;
                    p.row = r;

                    // if king is safe after this move, then not stalemate
                    if (!isIllegal(p) && !currentlyInCheck()) {
                        // reset and return
                        p.col = originalCol;
                        p.row = originalRow;
                        return false;
                    }

                    // reset before next trial
                    p.col = originalCol;
                    p.row = originalRow;
                }
            }
        }

        // no legal moves and not in check -> stalemate
        return true;
    }
    

    private boolean kingLegalMovement(piece king){
        if (isValidMove(king, -1, -1)) {return true;}
        if (isValidMove(king, 0, -1)) {return true;}
        if (isValidMove(king, -1, 0)) {return true;}
        if (isValidMove(king, 0, 1)) {return true;}
        if (isValidMove(king, 1, 0)) {return true;}
        if (isValidMove(king, 1, -1)) {return true;}
        if (isValidMove(king, -1, 1)) {return true;}
        if (isValidMove(king, 1, 1)) {return true;}

        return false;
    }

    private boolean isValidMove(piece king, int colPlus, int rowPlus){
        boolean isValidMove = false;

        king.col += colPlus;
        king.row += rowPlus;

        if (king.canMove(king.col, king.row)){
            if (king.hittingP != null){
                simPieces.remove(king.hittingP.getIndexofpiece());
            }
            if (!isIllegal(king)){
                isValidMove = true;
            }
        }
        king.resetPosition();
        copyPieces(pieces, simPieces);

        return isValidMove;
    }

    public void checkCastling(){
        if (castlingPiece != null){
            if (castlingPiece.col == 0){
                castlingPiece.col += 3;
            } else if (castlingPiece.col == 7){
                castlingPiece.col -=2;
            }
            castlingPiece.x =castlingPiece.getX(castlingPiece.col);
        }
    }

    public void changePlayer(){

        if (CURRENT_COLOR == WHITE){
            CURRENT_COLOR = BLACK;
            //reset twoStepped(ahead) status
            for (piece piece: pieces){
                if (piece.col == BLACK){
                    piece.twoStepped = false;
                }
            }

        } else {
            CURRENT_COLOR = WHITE;
            for (piece piece: pieces){
                if (piece.col == WHITE){
                    piece.twoStepped = false;
                }
            }
        }
        aPiece = null;
    }

    public boolean canPromote(){
        if (aPiece.type == Type.PAWN){
            if (CURRENT_COLOR == WHITE && aPiece.row ==0 || CURRENT_COLOR == BLACK && aPiece.row == 7){
                promotePieces.clear();
                promotePieces.add(new Knight(CURRENT_COLOR, 9, 3));
                promotePieces.add(new Rook(CURRENT_COLOR, 9, 4)); 
                promotePieces.add(new Bishop(CURRENT_COLOR, 10, 3));
                promotePieces.add(new Queen(CURRENT_COLOR, 10, 4));
                return true;
            }
        }
        return false;
    }

    private void promoting(){
        if (mouse.pressed){
            for (piece piece : promotePieces){
                if (piece.col == mouse.x/board.SQUARE_SIZE && piece.row == mouse.y/board.SQUARE_SIZE){
                    switch (piece.type){
                        case ROOK: simPieces.add(new Rook(CURRENT_COLOR, aPiece.col, aPiece.row)); break;
                        case KNIGHT: simPieces.add(new Knight(CURRENT_COLOR, aPiece.col, aPiece.row)); break;
                        case BISHOP: simPieces.add(new Bishop(CURRENT_COLOR, aPiece.col, aPiece.row)); break;
                        case QUEEN: simPieces.add(new Queen(CURRENT_COLOR, aPiece.col, aPiece.row)); break;
                        default: break;
                    }
                    simPieces.remove(aPiece.getIndexofpiece());
                    copyPieces(simPieces, pieces);
                    aPiece = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        //Chess board
        board.draw(g2);

        //Chess pieces
        for (piece p : simPieces) {
            p.draw(g2);
        }

        if (aPiece != null){
            if(canMove) {
                if (isIllegal(aPiece) || currentlyInCheck()){
                    g2.setColor(Color.RED);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(aPiece.col * Board.SQUARE_SIZE, aPiece.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                } else {
                    g2.setColor(Color.BLUE);
                    //change opacity for the target square
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(aPiece.col * Board.SQUARE_SIZE, aPiece.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    // reset alpha otherwise other things will be half transparent too
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
            //Draw the aPiece in the end so it won't be hidden by the board or the colored square
            aPiece.draw(g2);
            }
        }
        // status panel
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Aptos", Font.PLAIN, 42));
        g2.setColor(Color.white);
        if (promotion){
            g2.drawString("Promote to: ", 890, 250);
            for (piece piece : promotePieces){
                    g2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
            }
        } 
        else if (!gameOver && !stalemate){
            if (CURRENT_COLOR == WHITE){
                g2.drawString("White's turn", 888, 600);
            } 
            else {
                g2.drawString("Black's turn", 888, 200);
            }
        }
        if (gameOver){
            String a = "";
            if (CURRENT_COLOR == WHITE){
                a = "BLACK WINS";
            }
            else {
                a = "WHITE WINS";
            }
            g2.setFont(new Font("Arial", Font.PLAIN, 60));
            g2.setColor(Color.yellow);
            g2.drawString(a, 820, 420);
        }
        if (stalemate){
            g2.setFont(new Font("Arial", Font.PLAIN, 60));
            g2.setColor(Color.yellow);
            g2.drawString("STALEMATE", 820, 420);
        }
    }
}