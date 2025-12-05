// src/main/GamePanel.java
package main;

import piece.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {
    public static final int GAME_WIDTH = 1200;
    public static final int GAME_HEIGHT = 800;
    final int FPS = 60;
    Thread gameThread;
    Board boardDrawer = new Board();
    Mouse mouse = new Mouse();
    Keyboard keyboard = new Keyboard();

    public static final int WHITE = 0, BLACK = 1;
    public int CURRENT_COLOR = WHITE;

    public static piece[][] board = new piece[8][8];
    public static ArrayList<piece> pieces = new ArrayList<>();
    private piece whiteKing, blackKing;
    public static ArrayList<piece> capturedWhite = new ArrayList<>();
    public static ArrayList<piece> capturedBlack = new ArrayList<>();

    public static piece activePiece = null;
    public static piece promoPiece = null; // <--- NEW: Stores the pawn during promotion
    
    private ArrayList<Point> legalMoves = new ArrayList<>();
    private boolean promotion = false;
    public boolean gameOver = false, stalemate = false;

    public static final int TITLE_STATE = 0, PLAY_STATE = 1;
    public int gameState = TITLE_STATE;
    BufferedImage background;
    Rectangle playButton = new Rectangle(500, 360, 200, 80);

    public GamePanel() {
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setBackground(Color.BLACK);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        addKeyListener(keyboard);
        setFocusable(true);
        requestFocusInWindow();

        try {
            background = ImageIO.read(getClass().getResourceAsStream("/backgroundImage/chess_background.png"));
        } catch (IOException ignored) {}

        setupNewGame();
    }

    public void launch() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void ResetGame() {
        setupNewGame();
        gameState = TITLE_STATE;
        repaint();
    }

    public void setupNewGame() {
        board = new piece[8][8];
        pieces.clear();
        gameOver = stalemate = promotion = false;
        CURRENT_COLOR = WHITE;
        activePiece = null;
        promoPiece = null;

        // White
        addPiece(new Rook(WHITE, 0,7)); addPiece(new Knight(WHITE, 1,7));
        addPiece(new Bishop(WHITE, 2,7)); addPiece(new Queen(WHITE, 3,7));
        addPiece(new King(WHITE, 4,7)); addPiece(new Bishop(WHITE, 5,7));
        addPiece(new Knight(WHITE, 6,7)); addPiece(new Rook(WHITE, 7,7));
        for (int i = 0; i < 8; i++) addPiece(new Pawn(WHITE, i, 6));

        // Black
        addPiece(new Rook(BLACK, 0,0)); addPiece(new Knight(BLACK, 1,0));
        addPiece(new Bishop(BLACK, 2,0)); addPiece(new Queen(BLACK, 3,0));
        addPiece(new King(BLACK, 4,0)); addPiece(new Bishop(BLACK, 5,0));
        addPiece(new Knight(BLACK, 6,0)); addPiece(new Rook(BLACK, 7,0));
        for (int i = 0; i < 8; i++) addPiece(new Pawn(BLACK, i, 1));

        updateKingCache();
    }

    private void addPiece(piece p) {
        pieces.add(p);
        board[p.col][p.row] = p;
        if (p instanceof King) {
            if (p.color == WHITE) whiteKing = p;
            else blackKing = p;
        }
    }

    @Override
    public void run() {
        double interval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long last = System.nanoTime();

        while (gameThread != null) {
            long now = System.nanoTime();
            delta += (now - last) / interval;
            last = now;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        if (gameState == TITLE_STATE) {
            if (mouse.pressed && playButton.contains(mouse.x, mouse.y)) {
                gameState = PLAY_STATE;
            }
            return;
        }

        if (promotion) { promotionInput(); return; }
        if (gameOver || stalemate) return;

        int col = mouse.x / Board.SQUARE_SIZE;
        int row = mouse.y / Board.SQUARE_SIZE;

        if (mouse.pressed) {
            if (activePiece == null) {
                piece p = getPieceAt(col, row);
                if (p != null && p.color == CURRENT_COLOR) {
                    activePiece = p;
                    legalMoves = p.getLegalMoves();
                }
            } else {
                activePiece.x = mouse.x - Board.HALF_SQUARE_SIZE;
                activePiece.y = mouse.y - Board.HALF_SQUARE_SIZE;
            }
        } else if (activePiece != null) {
            if (col < 8 && row < 8 && legalMoves.contains(new Point(col, row))) {
                executeMove(activePiece.col, activePiece.row, col, row);
            } else {
                activePiece.resetPosition();
            }
            activePiece = null;
            legalMoves.clear();
        }
    }

    private void executeMove(int fromCol, int fromRow, int toCol, int toRow) {
        piece p = board[fromCol][fromRow];
        piece captured = board[toCol][toRow];

        if (captured != null) {
            pieces.remove(captured);
            if(captured.color == WHITE) capturedWhite.add(captured);
            if(captured.color == BLACK) capturedBlack.add(captured);
        }

        // Reset En Passant flag for all pieces
        for (piece pc : pieces) {
            pc.twoStepped = false;
        }

        board[fromCol][fromRow] = null;
        board[toCol][toRow] = p;
        p.col = toCol; p.row = toRow;
        p.updatePos();

        // Castling
        if (p instanceof King && Math.abs(toCol - fromCol) == 2) {
            int rookCol = toCol > fromCol ? 7 : 0;
            int rookNew = toCol > fromCol ? 5 : 3;
            piece rook = board[rookCol][fromRow];
            board[rookCol][fromRow] = null;
            board[rookNew][fromRow] = rook;
            rook.col = rookNew;
            rook.updatePos();
        }

        // En Passant
        if (p instanceof Pawn && captured == null && toCol != fromCol) {
            int captureRow = p.color == WHITE ? toRow + 1 : toRow - 1;
            captured = board[toCol][captureRow];
            if (captured != null) {
                board[toCol][captureRow] = null;
                pieces.remove(captured);
            }
        }

        // Promotion Logic
        if (p instanceof Pawn && (toRow == 0 || toRow == 7)) {
            promotion = true;
            promoPiece = p; //Store the pawn so we can access it in promotionInput()
            return;         //Return early! Don't finish turn yet.
        }

        finishTurn();
    }
    
    private void finishTurn() {
        CURRENT_COLOR = 1 - CURRENT_COLOR;
        updateKingCache();

        piece king = null;
        if (CURRENT_COLOR == WHITE) king = whiteKing;
        else king = blackKing;

        boolean inCheck = king != null && king.isAttacked();
        boolean noMoves = true;
        for (piece pc : pieces) {
            if (pc.color == CURRENT_COLOR && !pc.getLegalMoves().isEmpty()) {
                noMoves = false;
                break;
            }
        }

        if (inCheck && noMoves) gameOver = true;
        else if (noMoves) stalemate = true;
    }

    private void promotionInput() {
        if (!mouse.pressed) return;
        int col = mouse.x / Board.SQUARE_SIZE;
        int row = mouse.y / Board.SQUARE_SIZE;

        // We use promoPiece now, NOT activePiece (which is null)
        piece newPiece = null;
        if (col == 9 && row == 3) newPiece = new Knight(CURRENT_COLOR, promoPiece.col, promoPiece.row);
        else if (col == 9 && row == 4) newPiece = new Rook(CURRENT_COLOR, promoPiece.col, promoPiece.row);
        else if (col == 10 && row == 3) newPiece = new Bishop(CURRENT_COLOR, promoPiece.col, promoPiece.row);
        else if (col == 10 && row == 4) newPiece = new Queen(CURRENT_COLOR, promoPiece.col, promoPiece.row);

        if (newPiece != null) {
            pieces.remove(promoPiece);
            board[promoPiece.col][promoPiece.row] = newPiece;
            pieces.add(newPiece);
            
            promotion = false;
            promoPiece = null;
            
            finishTurn(); // <--- Now we check for checkmate caused by the new Queen
        }
    }

    private piece getPieceAt(int col, int row) {
        if (col < 0 || col >= 8 || row < 0 || row >= 8) return null;
        return board[col][row];
    }

    private void updateKingCache() {
        whiteKing = blackKing = null;
        for (piece p : pieces) {
            if (p instanceof King) {
                if (p.color == WHITE) whiteKing = p;
                else blackKing = p;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (gameState == TITLE_STATE) {
            if (background != null) g2.drawImage(background, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
            g2.setColor(Color.GREEN);
            g2.setFont(new Font("Monospaced", Font.BOLD, 90));
            String title = "Chess Code Strike";
            int w = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, GAME_WIDTH/2 - w/2, 200);

            g2.setColor(mouse.pressed && playButton.contains(mouse.x, mouse.y) ? Color.CYAN : Color.MAGENTA);
            g2.fill(playButton);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Monospaced", Font.BOLD, 40));
            g2.drawString("PLAY", 565, 410);
            return;
        }

        boardDrawer.draw(g2);

        piece currentKing = null;
        if (CURRENT_COLOR == WHITE) currentKing = whiteKing;
        else currentKing = blackKing;
        
        if (currentKing != null && currentKing.isAttacked()) {
            g2.setColor(new Color(255, 0, 0, 100));
            g2.fillRect(currentKing.col * 100, currentKing.row * 100, 100, 100);
        }

        for (piece p : pieces) p.draw(g2);

        if (activePiece != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
            activePiece.draw(g2);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

            g2.setColor(new Color(100, 255, 100, 180));
            for (Point pt : legalMoves) {
                g2.fillOval(pt.x * 100 + 38, pt.y * 100 + 38, 24, 24);
            }
        }

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        if (promotion) {
            g2.drawString("Promote to:", 880, 200);
            new Knight(CURRENT_COLOR,9,3).draw(g2);
            new Rook(CURRENT_COLOR,9,4).draw(g2);
            new Bishop(CURRENT_COLOR,10,3).draw(g2);
            new Queen(CURRENT_COLOR,10,4).draw(g2);
        } else if (!gameOver && !stalemate) {
            g2.drawString(CURRENT_COLOR == WHITE ? "White's turn" : "Black's turn", 870, 95);
            int x = 840;
            int y = 640;
            int scale = 45;
            for(piece p : capturedBlack){
                g2.drawImage(p.image, x, y, scale, scale, null);
                x += 40;
                if(x > 1100) {x = 840; y += 40;}
            }
            x = 840;
            y = 100;
            for(piece p : capturedWhite){
                g2.drawImage(p.image, x, y, scale, scale, null);
                x += 40;
                if(x > 1100) {x = 840; y += 40;}
            }
        }
        if (gameOver) {
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 80));
            g2.drawString(CURRENT_COLOR == WHITE ? "BLACK WINS" : "WHITE WINS", 700, 420);
        }
        if (stalemate) {
            g2.setColor(Color.YELLOW);
            g2.drawString("STALEMATE", 820, 420);
        }
    }
}