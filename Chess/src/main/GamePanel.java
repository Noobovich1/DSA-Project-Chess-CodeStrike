package main;

import engine.ChessAI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import piece.*;

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

    // --- GAME DATA ---
    public static piece[][] board = new piece[8][8];
    public static ArrayList<piece> pieces = new ArrayList<>();
    private piece whiteKing, blackKing;
    public static ArrayList<piece> capturedWhite = new ArrayList<>();
    public static ArrayList<piece> capturedBlack = new ArrayList<>();

    // --- DRAW / ENDGAME DETECTION ---
    private Map<String, Integer> repetitionMap = new HashMap<>();
    public static piece activePiece = null;
    public static piece promoPiece = null;
    
    private ArrayList<Point> legalMoves = new ArrayList<>();
    private boolean promotion = false;
    
    // Game Over States
    public boolean gameOver = false;     // Checkmate
    public boolean isStalemate = false;  // Stalemate
    public boolean isDraw = false;       // Repetition or Insufficient Material
    public String endReasonText = "";    // Text to display on screen

    public int gamenotover=0;
    
    // --- STATES ---
    public static final int TITLE_STATE = 0;
    public static final int PLAY_STATE = 1;
    public static final int AI_SELECTOR_STATE = 2; 
    public int gameState = TITLE_STATE;

    // --- ASSETS ---
    BufferedImage background;
    BufferedImage imgChiBao, imgGia, imgTung; 
    
    // --- UI RECTANGLES ---
    Rectangle btnPvP = new Rectangle(400, 360, 400, 80);
    Rectangle btnPvE = new Rectangle(400, 460, 400, 80);
    Rectangle btnBot1 = new Rectangle(150, 200, 250, 300);
    Rectangle btnBot2 = new Rectangle(475, 200, 250, 300);
    Rectangle btnBot3 = new Rectangle(800, 200, 250, 300);
    Rectangle btnColorWhite = new Rectangle(400, 550, 150, 60);
    Rectangle btnColorBlack = new Rectangle(650, 550, 150, 60);
    Rectangle btnStartGame = new Rectangle(450, 650, 300, 80);
    Rectangle btnBack = new Rectangle(50, 50, 100, 50);

    private Sound sound = new Sound();

    // --- AI CONFIGURATION ---
    public ChessAI ai;
    public boolean playAgainstAI = false;
    private boolean aiThinking = false;
    private volatile int gameId = 0;

    private int selectedBotDepth = 3;
    private String selectedBotName = "Gia";
    private BufferedImage selectedBotImage;
    private int playerChosenColor = WHITE;

    public GamePanel() {
        setBackground(Color.BLACK);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        addKeyListener(keyboard);
        setFocusable(true);
        requestFocusInWindow();

        loadImages();
        
        ai = new ChessAI(this);
        selectedBotImage = imgGia; 
    }

    private void loadImages() {
        try {
            background = ImageIO.read(getClass().getResourceAsStream("/backgroundImage/chess_background.png"));
            try { imgChiBao = ImageIO.read(getClass().getResourceAsStream("/botImage/chibao.png")); } catch(Exception e){}
            try { imgGia = ImageIO.read(getClass().getResourceAsStream("/botImage/gia.png")); } catch(Exception e){}
            try { imgTung = ImageIO.read(getClass().getResourceAsStream("/botImage/tungai.png")); } catch(Exception e){}
        } catch (Exception e) {
            System.out.println("Background loading failed: " + e.getMessage());
        }
    }

    public void launch() {
        gameThread = new Thread(this);
        gameThread.start();
        gameSE(); 
    }

    public void ResetGame() {
        gameState = TITLE_STATE;
        activePiece = null;
        promoPiece = null;
        repaint();
    }

    public void startGame() {
        gameId++; 
        board = new piece[8][8];
        pieces.clear();
        capturedBlack.clear();
        capturedWhite.clear();
        
        // Reset Game Over Flags
        gameOver = false;
        isStalemate = false;
        isDraw = false;
        endReasonText = "";
        
        // Reset Repetition History
        repetitionMap.clear();
        
        promotion = false;
        CURRENT_COLOR = WHITE;
        activePiece = null;
        promoPiece = null;
        aiThinking = false;

        // White pieces
        addPiece(new Rook(WHITE, 0,7)); addPiece(new Knight(WHITE, 1,7));
        addPiece(new Bishop(WHITE, 2,7)); addPiece(new Queen(WHITE, 3,7));
        addPiece(new King(WHITE, 4,7)); addPiece(new Bishop(WHITE, 5,7));
        addPiece(new Knight(WHITE, 6,7)); addPiece(new Rook(WHITE, 7,7));
        for (int i = 0; i < 8; i++) addPiece(new Pawn(WHITE, i, 6));

        // Black pieces
        addPiece(new Rook(BLACK, 0,0)); addPiece(new Knight(BLACK, 1,0));
        addPiece(new Bishop(BLACK, 2,0)); addPiece(new Queen(BLACK, 3,0));
        addPiece(new King(BLACK, 4,0)); addPiece(new Bishop(BLACK, 5,0));
        addPiece(new Knight(BLACK, 6,0)); addPiece(new Rook(BLACK, 7,0));
        for (int i = 0; i < 8; i++) addPiece(new Pawn(BLACK, i, 1));

        updateKingCache();
        
        // Add initial state to history
        recordBoardState();

        if (playAgainstAI) {
            ai.setDepth(selectedBotDepth);
            if (playerChosenColor == BLACK) {
                aiTurn();
            }
        }
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
        float scaleX = getWidth() / (float) GAME_WIDTH;
        float scaleY = getHeight() / (float) GAME_HEIGHT;
        int mx = (int)(mouse.x / scaleX);
        int my = (int)(mouse.y / scaleY);

        if (gameState == TITLE_STATE) {
            if (mouse.pressed) {
                if (btnPvP.contains(mx, my)) {
                    playAgainstAI = false;
                    gameState = PLAY_STATE;
                    startGame();
                    mouse.pressed = false;
                } else if (btnPvE.contains(mx, my)) {
                    playAgainstAI = true;
                    gameState = AI_SELECTOR_STATE;
                    mouse.pressed = false;
                }
            }
            return;
        }

        if (gameState == AI_SELECTOR_STATE) {
            if (mouse.pressed) {
                if (btnBot1.contains(mx, my)) {
                    selectedBotName = "Chi Bao (Easy)";
                    selectedBotDepth = 2;
                    selectedBotImage = imgChiBao;
                }
                else if (btnBot2.contains(mx, my)) {
                    selectedBotName = "Gia (Normal)";
                    selectedBotDepth = 3;
                    selectedBotImage = imgGia;
                }
                else if (btnBot3.contains(mx, my)) {
                    selectedBotName = "Mr. Tung (Hard)";
                    selectedBotDepth = 5;
                    selectedBotImage = imgTung;
                }
                else if (btnColorWhite.contains(mx, my)) playerChosenColor = WHITE;
                else if (btnColorBlack.contains(mx, my)) playerChosenColor = BLACK;
                else if (btnStartGame.contains(mx, my)) {
                    gameState = PLAY_STATE;
                    startGame();
                }
                else if (btnBack.contains(mx, my)) {
                    gameState = TITLE_STATE;
                }
                mouse.pressed = false;
            }
            return;
        }

        if (promotion) { promotionInput(mx, my); return; }
        // Stop updates if game has ended
        if (gameOver || isStalemate || isDraw) return;

        if (playAgainstAI && aiThinking) return;

        int displayCol = mx / Board.SQUARE_SIZE;
        int displayRow = my / Board.SQUARE_SIZE;
        int col = getLogicalCol(displayCol);
        int row = getLogicalRow(displayRow);

        if (mouse.pressed) {
            if (activePiece == null) {
                piece p = getPieceAt(col, row);
                if (p != null && p.color == CURRENT_COLOR) {
                    if (playAgainstAI && p.color != playerChosenColor) return;
                    activePiece = p;
                    legalMoves = p.getLegalMoves();
                }
            }
        } else if (activePiece != null) {
            if (col >= 0 && col < 8 && row >= 0 && row < 8 && legalMoves.contains(new Point(col, row))) {
                executeMove(activePiece.col, activePiece.row, col, row);
            } else {
                activePiece.resetPosition();
                illegalSE();
            }
            activePiece = null;
            legalMoves.clear();
        }
    }
    
    private int getDisplayCol(int col) { return (playAgainstAI && playerChosenColor == BLACK) ? 7 - col : col; }
    private int getDisplayRow(int row) { return (playAgainstAI && playerChosenColor == BLACK) ? 7 - row : row; }
    private int getLogicalCol(int displayCol) { return (playAgainstAI && playerChosenColor == BLACK) ? 7 - displayCol : displayCol; }
    private int getLogicalRow(int displayRow) { return (playAgainstAI && playerChosenColor == BLACK) ? 7 - displayRow : displayRow; }

    private void executeMove(int fromCol, int fromRow, int toCol, int toRow) {
        piece p = board[fromCol][fromRow];
        piece captured = board[toCol][toRow];

        if (captured != null) {
            pieces.remove(captured);
            if(captured.color == WHITE) capturedWhite.add(captured);
            if(captured.color == BLACK) capturedBlack.add(captured);
            capSE();
            repetitionMap.clear(); 
        } else {
            moveSE();
        }

        if (p.type == Type.PAWN) repetitionMap.clear();

        for (piece pc : pieces) pc.twoStepped = false;

        board[fromCol][fromRow] = null;
        board[toCol][toRow] = p;
        p.col = toCol; p.row = toRow;
        p.updatePos();

        if (p instanceof King && Math.abs(toCol - fromCol) == 2) {
            int rookCol = toCol > fromCol ? 7 : 0;
            int rookNew = toCol > fromCol ? 5 : 3;
            piece rook = board[rookCol][fromRow];
            board[rookCol][fromRow] = null;
            board[rookNew][fromRow] = rook;
            rook.col = rookNew;
            rook.updatePos();
        }

        if (p instanceof Pawn && captured == null && toCol != fromCol) {
            int captureRow = p.color == WHITE ? toRow + 1 : toRow - 1;
            captured = board[toCol][captureRow];
            if (captured != null) {
                board[toCol][captureRow] = null;
                pieces.remove(captured);
            }
        }

        if (p instanceof Pawn && (toRow == 0 || toRow == 7)) {
            if (playAgainstAI && CURRENT_COLOR != playerChosenColor) {
                pieces.remove(p);
                piece queen = new Queen(CURRENT_COLOR, toCol, toRow);
                board[toCol][toRow] = queen;
                pieces.add(queen);
                promoSE();
                finishTurn();
                return;
            } else {
                promotion = true;
                promoSE();
                promoPiece = p;
                return;
            }
        }
        finishTurn();
    }
    
    private void finishTurn() {
        // 1. Record Board State for Repetition
        recordBoardState();
        
        // 2. Switch Color
        CURRENT_COLOR = 1 - CURRENT_COLOR;
        updateKingCache();

        // 3. Check Mate / Stalemate
        // --- FIX: Use explicit if-else instead of ternary operator to avoid VerifyError ---
        piece king;
        if (CURRENT_COLOR == WHITE) {
            king = whiteKing;
        } else {
            king = blackKing;
        }
        
        boolean inCheck = king != null && king.isAttacked();
        boolean hasLegalMoves = false;
        
        for (piece pc : pieces) {
            if (pc.color == CURRENT_COLOR && !pc.getLegalMoves().isEmpty()) {
                hasLegalMoves = true;
                break;
            }
        }

        if (!hasLegalMoves) {
            if (inCheck) {
                gameOver = true;
                endReasonText = (CURRENT_COLOR == WHITE ? "BLACK" : "WHITE") + " WINS";
            } else {
                isStalemate = true;
                endReasonText = "STALEMATE";
            }
        }
        
        // 4. Check Repetition
        String currentId = generateBoardId();
        if (repetitionMap.getOrDefault(currentId, 0) >= 3) {
            isDraw = true;
            endReasonText = "DRAW BY REPETITION";
        }
        
        // 5. Check Insufficient Material
        if (isInsufficientMaterial()) {
            isDraw = true;
            endReasonText = "INSUFFICIENT MATERIAL";
        }

        // 6. Continue AI if game not over
        if (playAgainstAI && !gameOver && !isStalemate && !isDraw) {
            if (CURRENT_COLOR != playerChosenColor) {
                aiTurn();
            }
        }
    }
    
    public String generateBoardId() {
        StringBuilder sb = new StringBuilder();
        for(int r=0; r<8; r++){
            for(int c=0; c<8; c++){
                piece p = board[c][r];
                if(p == null) sb.append("-");
                else {
                    sb.append(p.type.toString().charAt(0));
                    sb.append(p.color);
                }
            }
        }
        return sb.toString();
    }
    
    private void recordBoardState() {
        String id = generateBoardId();
        repetitionMap.put(id, repetitionMap.getOrDefault(id, 0) + 1);
        
        if (ai != null) {
            ai.setHistory(new ArrayList<>(repetitionMap.keySet()));
        }
    }
    
    private boolean isInsufficientMaterial() {
        if (pieces.size() == 2) return true; // King vs King
        
        if (pieces.size() == 3) {
            for (piece p : pieces) {
                if (p.type == Type.KNIGHT || p.type == Type.BISHOP) return true; // K+N vs K or K+B vs K
            }
        }
        return false;
    }

    private void aiTurn() {
        if (aiThinking) return;
        aiThinking = true;
        int currentGameId = gameId; 
        
        new Thread(() -> {
            try {
                Thread.sleep(500); 
                if (gameId != currentGameId) return;

                ChessAI.Move aiMove = ai.getBestMove(CURRENT_COLOR);
                
                if (aiMove != null) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        if (gameId == currentGameId && gameState == PLAY_STATE) {
                            executeAIMove(aiMove);
                        }
                        aiThinking = false;
                    });
                } else {
                    aiThinking = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                aiThinking = false;
            }
        }).start();
    }

    private void executeAIMove(ChessAI.Move aiMove) {
        executeMove(aiMove.fromCol, aiMove.fromRow, aiMove.toCol, aiMove.toRow);
    }

    private void promotionInput(int mx, int my) {
        if (!mouse.pressed) return;
        int col = mx / Board.SQUARE_SIZE;
        int row = my / Board.SQUARE_SIZE;

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
            finishTurn(); 
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
    
    private void promoSE(){ sound.setFile(sound.PROMOTE); sound.play(); }
    private void moveSE(){ sound.setFile(sound.MOVE); sound.play(); }
    private void capSE(){ sound.setFile(sound.CAPTURE); sound.play(); }
    private void gameSE(){ sound.setFile(sound.GAME_END); sound.play(); }
    private void illegalSE(){
        if(gamenotover!=3){
        sound.setFile(Sound.ILLEGAL);
        sound.play();
        gamenotover++;}
        else{
            sound.setFile(Sound.MAGIC);
            sound.play();
            gamenotover=0;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        float scaleX = getWidth() / (float) GAME_WIDTH;
        float scaleY = getHeight() / (float) GAME_HEIGHT;
        g2.scale(scaleX, scaleY);

        int mx = (int)(mouse.x / scaleX);
        int my = (int)(mouse.y / scaleY);

        if (gameState == TITLE_STATE) {
            drawTitleScreen(g2, mx, my);
            return;
        }

        if (gameState == AI_SELECTOR_STATE) {
            drawAISelectionScreen(g2, mx, my);
            return;
        }

        boardDrawer.draw(g2);

        piece currentKing;
        if (CURRENT_COLOR == WHITE) {
            currentKing = whiteKing;
        } else {
            currentKing = blackKing;
        }

        if (currentKing != null && currentKing.isAttacked()) {
            int highlightCol = getDisplayCol(currentKing.col);
            int highlightRow = getDisplayRow(currentKing.row);
            g2.setColor(new Color(255, 0, 0, 100));
            g2.fillRect(highlightCol * 100, highlightRow * 100, 100, 100);
        }

        drawPieces(g2);
        drawLegalMoves(g2, mx, my);
        drawSidebar(g2);
        drawGameOverUI(g2);
    }
    
    private void drawPieces(Graphics2D g2) {
        synchronized(pieces) {
            for (piece p : pieces) {
                if (p == activePiece) continue; 
                int drawCol = getDisplayCol(p.col);
                int drawRow = getDisplayRow(p.row);
                int savedX = p.x; int savedY = p.y;
                p.x = drawCol * Board.SQUARE_SIZE;
                p.y = drawRow * Board.SQUARE_SIZE;
                p.draw(g2);
                p.x = savedX; p.y = savedY;
            }
        }
    }
    
    private void drawLegalMoves(Graphics2D g2, int mx, int my) {
        if (activePiece != null) {
            g2.setColor(new Color(100, 255, 100, 180));
            for (Point pt : legalMoves) {
                int drawCol = getDisplayCol(pt.x);
                int drawRow = getDisplayRow(pt.y);
                g2.fillOval(drawCol * 100 + 38, drawRow * 100 + 38, 24, 24);
            }
            if (activePiece.image != null) {
                g2.drawImage(activePiece.image, mx - 50, my - 50, 100, 100, null);
            }
        }
    }
    
    private void drawGameOverUI(Graphics2D g2) {
        if (gameOver || isStalemate || isDraw) {
            String mainText = "";
            
            g2.setFont(new Font("Arial", Font.BOLD, 80));
            if (gameOver) {
                g2.setColor(Color.YELLOW.darker());
                mainText = endReasonText;
            } else if (isStalemate) {
                g2.setColor(Color.LIGHT_GRAY);
                mainText = "STALEMATE";
            } else if (isDraw) {
                g2.setColor(Color.LIGHT_GRAY);
                mainText = "DRAW";
            }

            FontMetrics metrics = g2.getFontMetrics();
            int x = (800 - metrics.stringWidth(mainText)) / 2;
            int y = 420;
            
            g2.setColor(new Color(0,0,0,150));
            g2.fillRect(0, 340, 800, 200);
            
            if (gameOver) g2.setColor(Color.YELLOW);
            else g2.setColor(Color.WHITE);
            g2.drawString(mainText, x, y);
            
            if (isDraw && !endReasonText.equals("DRAW")) {
                g2.setFont(new Font("Arial", Font.BOLD, 40));
                metrics = g2.getFontMetrics();
                String subText = "(" + endReasonText + ")";
                int subX = (800 - metrics.stringWidth(subText)) / 2;
                g2.drawString(subText, subX, y + 50);
            }
            
            g2.setFont(new Font("Arial", Font.BOLD, 30));
            g2.setColor(Color.WHITE);
            String restartText = "Press 'R' to Return to Menu";
            metrics = g2.getFontMetrics();
            int rX = (800 - metrics.stringWidth(restartText)) / 2;
            g2.drawString(restartText, rX, y + 100);
        }
    }

    private void drawTitleScreen(Graphics2D g2, int mx, int my) {
        if (background != null) g2.drawImage(background, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
        
        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Monospaced", Font.BOLD, 90));
        String title = "Chess Code Strike";
        int w = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, GAME_WIDTH/2 - w/2, 200);

        g2.setColor(btnPvP.contains(mx, my) ? Color.CYAN : Color.WHITE);
        g2.fill(btnPvP);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        g2.drawString("Human vs Human", btnPvP.x + 20, btnPvP.y + 55);
        
        g2.setColor(btnPvE.contains(mx, my) ? Color.CYAN : Color.WHITE);
        g2.fill(btnPvE);
        g2.setColor(Color.BLACK);
        g2.drawString("Human vs AI", btnPvE.x + 75, btnPvE.y + 55);
    }

    private void drawAISelectionScreen(Graphics2D g2, int mx, int my) {
        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 50));
        g2.drawString("Choose Opponent", 400, 100);

        drawBotCard(g2, btnBot1, "Chi Bao", "Easy (Depth 2)", imgChiBao, selectedBotDepth == 2, mx, my);
        drawBotCard(g2, btnBot2, "Gia", "Normal (Depth 3)", imgGia, selectedBotDepth == 3, mx, my);
        drawBotCard(g2, btnBot3, "Mr. Tung", "Hard (Depth 5)", imgTung, selectedBotDepth == 5, mx, my);

        g2.setColor(Color.WHITE);
        g2.drawString("Choose Your Color:", 400, 530);

        g2.setColor(playerChosenColor == WHITE ? Color.GREEN : Color.GRAY);
        if (btnColorWhite.contains(mx, my)) g2.setColor(Color.CYAN);
        g2.fill(btnColorWhite);
        g2.setColor(Color.BLACK);
        g2.drawString("WHITE", btnColorWhite.x + 20, btnColorWhite.y + 45);

        g2.setColor(playerChosenColor == BLACK ? Color.GREEN : Color.GRAY);
        if (btnColorBlack.contains(mx, my)) g2.setColor(Color.CYAN);
        g2.fill(btnColorBlack);
        g2.setColor(Color.WHITE);
        g2.drawString("BLACK", btnColorBlack.x + 20, btnColorBlack.y + 45);

        g2.setColor(btnStartGame.contains(mx, my) ? Color.CYAN : Color.MAGENTA);
        g2.fill(btnStartGame);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 50));
        g2.drawString("START", btnStartGame.x + 70, btnStartGame.y + 60);

        g2.setColor(Color.LIGHT_GRAY);
        g2.fill(btnBack);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.drawString("Back", btnBack.x + 25, btnBack.y + 30);
    }

    private void drawBotCard(Graphics2D g2, Rectangle rect, String name, String diff, BufferedImage img, boolean selected, int mx, int my) {
        if (selected) g2.setColor(Color.GREEN);
        else if (rect.contains(mx, my)) g2.setColor(Color.YELLOW);
        else g2.setColor(Color.GRAY);

        g2.fill(rect);

        if (img != null) {
            g2.drawImage(img, rect.x + 25, rect.y + 20, 200, 200, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(rect.x + 25, rect.y + 20, 200, 200);
            g2.setColor(Color.WHITE);
            g2.drawString("?", rect.x + 100, rect.y + 120);
        }

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 25));
        g2.drawString(name, rect.x + 20, rect.y + 250);
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.drawString(diff, rect.x + 20, rect.y + 280);
    }

    private void drawSidebar(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 36));

        if (promotion) {
            g2.drawString("Promote to:", 880, 200);
            new Knight(CURRENT_COLOR,9,3).draw(g2);
            new Rook(CURRENT_COLOR,9,4).draw(g2);
            new Bishop(CURRENT_COLOR,10,3).draw(g2);
            new Queen(CURRENT_COLOR,10,4).draw(g2);
        } else if (!gameOver && !isStalemate && !isDraw) {
            g2.drawString(CURRENT_COLOR == WHITE ? "White's turn" : "Black's turn", 870, 80);

            if (playAgainstAI) {
                g2.setColor(Color.LIGHT_GRAY);
                g2.setFont(new Font("Arial", Font.PLAIN, 24));
                g2.drawString("Opponent:", 850, 350);

                if (selectedBotImage != null) {
                    g2.drawImage(selectedBotImage, 850, 370, 150, 150, null);
                }
                g2.setColor(Color.YELLOW);
                g2.setFont(new Font("Arial", Font.BOLD, 28));
                g2.drawString(selectedBotName, 850, 560);

                if (aiThinking) {
                    g2.setColor(Color.RED);
                    g2.setFont(new Font("Monospaced", Font.ITALIC, 20));
                    g2.drawString("Thinking...", 850, 600);
                }
            } else {
                g2.drawString("PvP Mode", 870, 400);
            }

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
    }
}