package main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Queen;
import piece.Rook;
import piece.piece;

public class GamePanel extends JPanel implements Runnable{
    public static final int GAME_WIDTH = 1200;
    public static final int GAME_HEIGHT = 800;
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
    public static ArrayList<piece> capturedWhite = new ArrayList<>();
    public static ArrayList<piece> capturedBlack = new ArrayList<>();
    public static piece castlingPiece;
    private ArrayList<Point> legalMoves = new ArrayList<>();
    private boolean kingInCheck = false;
    private piece checkedKing = null;
    ArrayList<piece> promotePieces = new ArrayList<>();
    piece aPiece, checkingPiece; //handle the piece that the player is holding

    //BOOLEAN
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver;
    boolean stalemate;

    //Menu panel nha mấy bro
    public static final int TITLE_STATE = 0;
    public static final int PLAY_STATE = 1;
    public static int gameState = TITLE_STATE;
    BufferedImage background;
    Rectangle playButton;
    boolean mousePressedOverButton = false;

    public GamePanel(){
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        addKeyListener(keyboard);
        requestFocusInWindow();
        setFocusable(true);

        //load background image
        try {
            background = ImageIO.read(getClass().getResourceAsStream("/backgroundImage/chess_background.png"));
        } catch (IOException e){
            System.err.println("Failed to load background image: " + e.getMessage());
        }
        //load nút play nha mấy bro
        int buttonWidth = 200;
        int buttonHeight = 80;
        playButton = new Rectangle((GAME_WIDTH - buttonWidth) / 2, (GAME_HEIGHT - buttonHeight) / 2, buttonWidth, buttonHeight);

        setPieces();
        copyPieces(pieces, simPieces);
    }

    public void launch(){
        gameThread = new Thread(this);
        gameThread.start(); //call the run method
    }

    public final void setPieces(){
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
        capturedWhite.clear();
        capturedBlack.clear();

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
    //Menu state update
        if(gameState == TITLE_STATE) {
            // Check if mouse is clicking the play button
            if (mouse.pressed && playButton.contains(mouse.x, mouse.y)) {
                mousePressedOverButton = true;
            } else {
                if (mousePressedOverButton) {
                    gameState = PLAY_STATE;
                    mousePressedOverButton = false;
                }
            }
            return; // Stop here, don't run the game logic yet
        }
    //Play state update
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

                    //check if a piece was captured during this confirmed move
                    if(aPiece.hittingP != null){
                       
                        if(aPiece.hittingP.color == WHITE){
                            capturedWhite.add(aPiece.hittingP);
                        } else {
                            capturedBlack.add(aPiece.hittingP);
                        }
                    }
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
                    updateCheckStatus();

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
        copyPieces(pieces, simPieces);
        canMove=false;
        computeLegalMoves();
        validSquare=false;

        
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

        if(aPiece.canMove(aPiece.col, aPiece.row)){
            canMove = true;
    
            // If canMove() found a piece to capture, remove it from simulation
            if(aPiece.hittingP != null){
                simPieces.remove(aPiece.hittingP.getIndexofpiece()); 
            }
    
            checkCastling();
    
            if (!isIllegal(aPiece) && !currentlyInCheck()){
                validSquare = true;
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

    private boolean updateCheckStatus() {
        // Ensure simPieces reflects the current board
        copyPieces(pieces, simPieces);

        // Find the king for the side to move
        piece king = getKing(false); // false -> current player's king
        checkedKing = null;
        checkingPiece = null;
        kingInCheck = false;

        if (king == null) return false;

        // Scan all opponent pieces to see if any can move to the king
        for (piece p : simPieces) {
            if (p.color != king.color) {
                // Use p.canMove on the simulated board
                if (p.canMove(king.col, king.row)) {
                    kingInCheck = true;
                    checkedKing = king;
                    checkingPiece = p;
                    return true;
                }
            }
        }
        return false;
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
        return isValidMove(king, -1, -1) || isValidMove(king, 0, -1) || 
               isValidMove(king, -1, 0) || isValidMove(king, 0, 1) || 
               isValidMove(king, 1, 0) || isValidMove(king, 1, -1) || 
               isValidMove(king, -1, 1) || isValidMove(king, 1, 1);
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
            for (piece p: pieces){
                if (p.color == WHITE && p.type == Type.PAWN){
                    p.twoStepped = false;
                }
            }

        } else {
            CURRENT_COLOR = WHITE;
            for (piece p: pieces){
                if (p.color == BLACK && p.type == Type.PAWN){
                    p.twoStepped = false;
                }
            }
        }
        aPiece = null;
    }

    ///find piece for thing, avoiding mutating the actual piece list
    private piece findSimPieceFor(piece original){
        for (piece p : simPieces) {
            if (p.type == original.type && p.color == original.color
                && p.preCOL == original.preCOL && p.preROW == original.preROW) {
                return p;
            }
        }
        return null;
    }

    private void computeLegalMoves() {
        legalMoves.clear();
        if (aPiece == null) return;

        // Ensure simPieces starts as a copy of the real board
        copyPieces(pieces, simPieces);

        // Find the sim-piece that corresponds to the currently picked piece
        piece simPicked = findSimPieceFor(aPiece);
        if (simPicked == null) return;

        int origCol = simPicked.col;
        int origRow = simPicked.row;

        // Try every square on the board
        for (int c = 0; c < 8; c++) {
            for (int r = 0; r < 8; r++) {
                // skip same square
                if (c == origCol && r == origRow) continue;

                // Reset simPieces to the real board for each trial
                copyPieces(pieces, simPieces);
                simPicked = findSimPieceFor(aPiece);
                if (simPicked == null) continue;

                // Quick check: does the piece think it can move there?
                if (!simPicked.canMove(c, r)) continue;

                // If this move captures a piece, remove that piece from simPieces
                piece captured = simPicked.hittingP;
                if (captured != null) {
                    simPieces.remove(captured.getIndexofpiece());
                }

                // Temporarily move the simPicked piece
                int oldCol = simPicked.col;
                int oldRow = simPicked.row;
                simPicked.col = c;
                simPicked.row = r;

                // If castling or special moves require extra handling, simulate them here
                // (e.g., set castlingPiece positions if your code uses it)

                // Now check king safety on the simulated board
                boolean illegal = isIllegal(simPicked); // uses simPieces internally
                boolean inCheck = currentlyInCheck();   // also uses simPieces

                if (!illegal && !inCheck) {
                    legalMoves.add(new Point(c, r));
                }

                // restore not strictly necessary because we copy simPieces each iteration
                simPicked.col = oldCol;
                simPicked.row = oldRow;
            }
        }

        // restore simPieces to the real board
        copyPieces(pieces, simPieces);
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
                if (piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE){
                    switch (piece.type) {
                        case ROOK -> simPieces.add(new Rook(CURRENT_COLOR, aPiece.col, aPiece.row));
                        case KNIGHT -> simPieces.add(new Knight(CURRENT_COLOR, aPiece.col, aPiece.row));
                        case BISHOP -> simPieces.add(new Bishop(CURRENT_COLOR, aPiece.col, aPiece.row));
                        case QUEEN -> simPieces.add(new Queen(CURRENT_COLOR, aPiece.col, aPiece.row));
                        default -> {}
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

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        //Check state
        if(gameState == TITLE_STATE){
            drawMenu(g2);
        } else {
            //Chess board
            board.draw(g2);

            //checked king
            if (kingInCheck && checkedKing != null) {
                int kc = checkedKing.col;
                int kr = checkedKing.row;
                g2.setColor(new Color(255, 0, 0, 120)); // semi-transparent red
                g2.fillRect(kc * Board.SQUARE_SIZE, kr * Board.SQUARE_SIZE, Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                // reset composite if you changed it elsewhere
            }

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

        if (aPiece != null && !legalMoves.isEmpty()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
            g2.setColor(new Color(120, 120, 120, 200)); // grey with alpha via color
            int circleSize = 24; // diameter of the circle
            for (Point p : legalMoves) {
                int centerX = p.x * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE;
                int centerY = p.y * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE;
                int x = centerX - circleSize / 2;
                int y = centerY - circleSize / 2;
                g2.fillOval(x, y, circleSize, circleSize);
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
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
                    g2.drawString("Black's turn", 888, 290);
                }
                //this is for drawing captured pieces bruv
                int x = 840;
                int y = 620;
                int scale = 45;
                for(piece p : capturedWhite){
                    g2.drawImage(p.image, x, y, scale, scale, null);
                    x += 40;
                    if(x > 1100) { x = 840; y += 40; }
                    }
                //aye twin, this is for white captured pieces
                x = 840;
                y = 100;
                for(piece p : capturedBlack){
                    g2.drawImage(p.image, x, y, scale, scale, null);
                    x += 40;
                    if(x > 1100) { x = 840; y += 40; }
                }
            }
            if (gameOver){
                String a;
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
    // Menu drawing method
    public void drawMenu(Graphics2D g2) {
        // Draw Background 
        if (background != null) {
            g2.drawImage(background, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
        } else {
            // Fallback if image not found
            g2.setColor(new Color(50, 50, 50));
            g2.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        }

        // Draw Title
        g2.setFont(new Font("Monospaced", Font.BOLD, 90));
        String title = "Chess Code Strike";
        // Center the text
        int textWidth = g2.getFontMetrics().stringWidth(title);
        int x = GAME_WIDTH/2 - textWidth/2;
        int y = GAME_HEIGHT/4;
        
        // Shadow effect
        g2.setColor(Color.GREEN);
        g2.drawString(title, x+3, y+3);
        g2.setColor(Color.GREEN);
        g2.drawString(title, x, y);

        // Draw Play Button
        g2.setFont(new Font("Monospaced", Font.BOLD, 40));
        g2.setColor(mousePressedOverButton ? new Color(100, 255, 100) : new Color(255, 102, 255));
        g2.fill(playButton);
        
        // Button Text
        g2.setColor(Color.BLACK);
        String btnText = "PLAY";
        int btnTextWidth = g2.getFontMetrics().stringWidth(btnText);
        g2.drawString(btnText, playButton.x + (playButton.width - btnTextWidth)/2, playButton.y + 45);
    }
}