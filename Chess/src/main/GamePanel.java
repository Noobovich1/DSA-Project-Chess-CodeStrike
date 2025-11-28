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

    //Color
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int CURRENT_COLOR = WHITE;

    //ArrayList for pieces
    public static ArrayList<piece> pieces = new ArrayList<>(); //back up for such as undo move
    public static ArrayList<piece> simPieces = new ArrayList<>();
    piece aPiece; //handle the piece that the player is holding
    public static piece castlingPiece;

    //BOOLEAN
    boolean canMove;
    boolean vaildSquare;

    public GamePanel(){
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

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
        if (mouse.pressed){
            if (aPiece == null){
                //check if aPiece (active piece) is null or not
                for (piece piece : simPieces){
                    //if mouse is currently on an ally piece, allow mouse interaction with them as aPiece
                    if (piece.color == CURRENT_COLOR &&
                        piece.col == mouse.x/Board.SQUARE_SIZE &&
                        piece.row == mouse.y/Board.SQUARE_SIZE){
                    aPiece = piece;
                    }
                }
            } else {
            // if the player holding a piece, simulate the move
            simulate();
            }
        }

        //Mouse released
        if (mouse.pressed == false){
            if (aPiece != null){
                if(vaildSquare){
                    //MOVE CONFIRMED
                    copyPieces(simPieces,pieces);
                    aPiece.updatePos();
                    if (castlingPiece != null){
                        castlingPiece.updatePos();
                    }

                    changePlayer();
                }
                else {
                    copyPieces(pieces, simPieces);
                    aPiece.resetPosition();
                    aPiece = null;// resset to the original row and col
                }

            }
        }
    }

    private void simulate(){
        canMove=false;
        vaildSquare=false;
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
            vaildSquare=true;
        }
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
        // status panel
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Aptos", Font.PLAIN, 42));
        g2.setColor(Color.white);
        if (CURRENT_COLOR == WHITE){
            g2.drawString("White's turn", 880, 600);
        } else {
            g2.drawString("Black's turn", 880, 200);
        }
    }
}
