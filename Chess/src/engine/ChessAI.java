package engine;

import main.*;
import piece.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class ChessAI {
    private GamePanel gp;
    private int engineColor;
    
    // Piece values
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int KING_VALUE = 20000;
    
    private int searchDepth = 4;
    private HashSet<String> gameHistoryToCheck = new HashSet<>();
    
    // Tables (Left unchanged for brevity, assume they exist as in original)
    private static final int[][] PAWN_TABLE = {
        {0,  0,  0,  0,  0,  0,  0,  0},
        {50, 50, 50, 50, 50, 50, 50, 50},
        {10, 10, 20, 30, 30, 20, 10, 10},
        {5,  5, 10, 25, 25, 10,  5,  5},
        {0,  0,  0, 20, 20,  0,  0,  0},
        {5, -5,-10,  0,  0,-10, -5,  5},
        {5, 10, 10,-20,-20, 10, 10,  5},
        {0,  0,  0,  0,  0,  0,  0,  0}
    };

    private static final int[][] KNIGHT_TABLE = {
        {-50,-40,-30,-30,-30,-30,-40,-50},
        {-40,-20,  0,  0,  0,  0,-20,-40},
        {-30,  0, 10, 15, 15, 10,  0,-30},
        {-30,  5, 15, 20, 20, 15,  5,-30},
        {-30,  0, 15, 20, 20, 15,  0,-30},
        {-30,  5, 10, 15, 15, 10,  5,-30},
        {-40,-20,  0,  5,  5,  0,-20,-40},
        {-50,-40,-30,-30,-30,-30,-40,-50}
    };

    private static final int[][] BISHOP_TABLE = {
        {-20,-10,-10,-10,-10,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5, 10, 10,  5,  0,-10},
        {-10,  5,  5, 10, 10,  5,  5,-10},
        {-10,  0, 10, 10, 10, 10,  0,-10},
        {-10, 10, 10, 10, 10, 10, 10,-10},
        {-10,  5,  0,  0,  0,  0,  5,-10},
        {-20,-10,-10,-10,-10,-10,-10,-20}
    };

    private static final int[][] ROOK_TABLE = {
        {0,  0,  0,  0,  0,  0,  0,  0},
        {5, 10, 10, 10, 10, 10, 10,  5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        {-5,  0,  0,  0,  0,  0,  0, -5},
        {0,  0,  0,  5,  5,  0,  0,  0}
    };

    private static final int[][] QUEEN_TABLE = {
        {-20,-10,-10, -5, -5,-10,-10,-20},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  5,  5,  5,  5,  0,-10},
        {-5,   0,  5,  5,  5,  5,  0, -5},
        {-5,   0,  5,  5,  5,  5,  0, -5},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-10,  0,  0,  0,  0,  0,  0,-10},
        {-20,-10,-10, -5, -5,-10,-10,-20}
    };

    private static final int[][] KING_MID_TABLE = {
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-30,-40,-40,-50,-50,-40,-40,-30},
        {-20,-30,-30,-40,-40,-30,-30,-20},
        {-10,-20,-20,-20,-20,-20,-20,-10},
        {20, 20,  0,  0,  0,  0, 20, 20},
        {20, 40, 10,  0,  0, 10, 40, 20}
    };

    private static final int[][] KING_END_TABLE = {
        {-50,-40,-30,-20,-20,-30,-40,-50},
        {-30,-20,-10,  0,  0,-10,-20,-30},
        {-30,-10, 20, 30, 30, 20,-10,-30},
        {-30,-10, 30, 40, 40, 30,-10,-30},
        {-30,-10, 30, 40, 40, 30,-10,-30},
        {-30,-10, 20, 30, 30, 20,-10,-30},
        {-30,-30,  0,  0,  0,  0,-30,-30},
        {-50,-30,-30,-30,-30,-30,-30,-50}
    };

    public ChessAI(GamePanel gp) {
        this.gp = gp;
    }
    
    public void setDepth(int depth) {
        this.searchDepth = depth;
    }

    public void setHistory(ArrayList<String> history) {
        this.gameHistoryToCheck = new HashSet<>(history);
    }
    
    public Move getBestMove(int aiColor) {
        this.engineColor = aiColor;
        MoveScore result = minimax(searchDepth, aiColor, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
        return result.move;
    }

    private MoveScore minimax(int depth, int color, int alpha, int beta, boolean maximizing) {
        if (depth == 0) {
            int qScore = quiescenceSearch(alpha, beta, color);
            return new MoveScore(null, qScore);
        }
        
        ArrayList<Move> moves = getAllLegalMoves(color);
        
        if (moves.isEmpty()) {
            piece king = findKing(color);
            if (king != null && king.isAttacked()) {
                return new MoveScore(null, maximizing ? -100000 - depth : 100000 + depth);
            }
            return new MoveScore(null, 0); 
        }
        
        orderMoves(moves);
        Move bestMove = moves.get(0);
        
        if (maximizing) {
            int maxScore = Integer.MIN_VALUE;
            for (Move move : moves) {
                BoardState state = makeMove(move);
                
                // --- FIX: REPETITION DETECTION ---
                // If we are at the root level (depth == searchDepth), check if this move 
                // creates a board state seen before.
                boolean isRepetition = false;
                if (depth == searchDepth) {
                     String currentBoardId = gp.generateBoardId();
                     if (gameHistoryToCheck.contains(currentBoardId)) {
                         isRepetition = true;
                     }
                }
                
                int score;
                if (isRepetition) {
                    // Penalize repetition heavily to force progress, but not as bad as losing
                    score = -5000; 
                } else {
                    score = minimax(depth - 1, 1 - color, alpha, beta, false).score;
                }

                undoMove(state);
                
                if (score > maxScore) {
                    maxScore = score;
                    bestMove = move;
                }
                alpha = Math.max(alpha, score);
                if (beta <= alpha) break;
            }
            return new MoveScore(bestMove, maxScore);
        } else {
            int minScore = Integer.MAX_VALUE;
            for (Move move : moves) {
                BoardState state = makeMove(move);
                int score = minimax(depth - 1, 1 - color, alpha, beta, true).score;
                undoMove(state);
                
                if (score < minScore) {
                    minScore = score;
                    bestMove = move;
                }
                beta = Math.min(beta, score);
                if (beta <= alpha) break;
            }
            return new MoveScore(bestMove, minScore);
        }
    }

    private int quiescenceSearch(int alpha, int beta, int color) {
        int standPat = evaluateBoard(); 
        
        if (color == engineColor) { 
             if (standPat >= beta) return beta;
             if (standPat > alpha) alpha = standPat;
        } else { 
             if (standPat <= alpha) return alpha;
             if (standPat < beta) beta = standPat;
        }
        
        ArrayList<Move> moves = getAllLegalMoves(color);
        ArrayList<Move> captures = new ArrayList<>();
        for(Move m : moves) {
            if(GamePanel.board[m.toCol][m.toRow] != null) {
                captures.add(m);
            }
        }
        
        orderMoves(captures);
        
        if (color == engineColor) { 
            for (Move move : captures) {
                BoardState state = makeMove(move);
                int score = quiescenceSearch(alpha, beta, 1 - color);
                undoMove(state);
                
                if (score >= beta) return beta;
                if (score > alpha) alpha = score;
            }
            return alpha;
        } else { 
            for (Move move : captures) {
                BoardState state = makeMove(move);
                int score = quiescenceSearch(alpha, beta, 1 - color);
                undoMove(state);
                
                if (score <= alpha) return alpha;
                if (score < beta) beta = score;
            }
            return beta;
        }
    }
    
    private int evaluateBoard() {
        int whiteScore = 0;
        int blackScore = 0;
        boolean isEndgame = GamePanel.pieces.size() < 12;

        for (piece p : GamePanel.pieces) {
            int material = getPieceValue(p.type);
            int position = getPositionValue(p, isEndgame);
            
            if (p.color == GamePanel.WHITE) {
                whiteScore += (material + position);
            } else {
                blackScore += (material + position);
            }
        }
        
        int score = whiteScore - blackScore;
        if (engineColor == GamePanel.BLACK) return -score;
        return score;
    }
    
    private int getPieceValue(Type type) {
        switch (type) {
            case PAWN: return PAWN_VALUE;
            case KNIGHT: return KNIGHT_VALUE;
            case BISHOP: return BISHOP_VALUE;
            case ROOK: return ROOK_VALUE;
            case QUEEN: return QUEEN_VALUE;
            case KING: return KING_VALUE;
            default: return 0;
        }
    }
    
    private int getPositionValue(piece p, boolean isEndgame) {
        int row = p.row;
        int col = p.col;
        int tableRow = (p.color == GamePanel.WHITE) ? row : 7 - row;
        switch (p.type) {
            case PAWN: return PAWN_TABLE[tableRow][col];
            case KNIGHT: return KNIGHT_TABLE[tableRow][col];
            case BISHOP: return BISHOP_TABLE[tableRow][col];
            case ROOK: return ROOK_TABLE[tableRow][col];
            case QUEEN: return QUEEN_TABLE[tableRow][col];
            case KING: return isEndgame ? KING_END_TABLE[tableRow][col] : KING_MID_TABLE[tableRow][col];
            default: return 0;
        }
    }
    
    private void orderMoves(ArrayList<Move> moves) {
        Collections.sort(moves, new Comparator<Move>() {
            @Override
            public int compare(Move m1, Move m2) {
                int score1 = 0;
                int score2 = 0;
                piece cap1 = GamePanel.board[m1.toCol][m1.toRow];
                piece cap2 = GamePanel.board[m2.toCol][m2.toRow];
                if (cap1 != null) score1 = 10 * getPieceValue(cap1.type) - getPieceValue(m1.piece.type);
                if (cap2 != null) score2 = 10 * getPieceValue(cap2.type) - getPieceValue(m2.piece.type);
                if (m1.piece.type == Type.PAWN && (m1.toRow == 0 || m1.toRow == 7)) score1 += 800;
                if (m2.piece.type == Type.PAWN && (m2.toRow == 0 || m2.toRow == 7)) score2 += 800;
                return score2 - score1;
            }
        });
    }
    
    private BoardState makeMove(Move move) {
        BoardState state = new BoardState();
        state.piece = move.piece;
        state.fromCol = move.fromCol;
        state.fromRow = move.fromRow;
        state.toCol = move.toCol;
        state.toRow = move.toRow;
        state.capturedPiece = GamePanel.board[move.toCol][move.toRow];
        state.moved = move.piece.moved;
        state.twoStepped = move.piece.twoStepped;
        
        GamePanel.board[move.fromCol][move.fromRow] = null;
        GamePanel.board[move.toCol][move.toRow] = move.piece;
        move.piece.col = move.toCol;
        move.piece.row = move.toRow;
        move.piece.moved = true;
        
        // --- FIX: PROMOTION SIMULATION ---
        // If pawn hits end, momentarily change it to Queen for evaluation
        if (move.piece.type == Type.PAWN && (move.toRow == 0 || move.toRow == 7)) {
            state.promoted = true;
            move.piece.type = Type.QUEEN;
        }
        
        // Handle Castling in simulation
        if (move.piece.type == Type.KING && Math.abs(move.toCol - move.fromCol) == 2) {
            state.castled = true;
            if (move.toCol > move.fromCol) { // Kingside
                state.rook = GamePanel.board[7][move.fromRow];
                state.rookFromCol = 7;
                state.rookToCol = 5;
            } else { // Queenside
                state.rook = GamePanel.board[0][move.fromRow];
                state.rookFromCol = 0;
                state.rookToCol = 3;
            }
            if (state.rook != null) {
                GamePanel.board[state.rookFromCol][move.fromRow] = null;
                GamePanel.board[state.rookToCol][move.fromRow] = state.rook;
                state.rook.col = state.rookToCol;
            }
        }
        
        if (state.capturedPiece != null) GamePanel.pieces.remove(state.capturedPiece);
        return state;
    }
    
    private void undoMove(BoardState state) {
        state.piece.col = state.fromCol;
        state.piece.row = state.fromRow;
        state.piece.moved = state.moved;
        state.piece.twoStepped = state.twoStepped;
        
        // --- FIX: REVERT PROMOTION ---
        if (state.promoted) {
            state.piece.type = Type.PAWN;
        }
        
        GamePanel.board[state.fromCol][state.fromRow] = state.piece;
        GamePanel.board[state.toCol][state.toRow] = state.capturedPiece;
        
        if (state.castled && state.rook != null) {
            GamePanel.board[state.rookToCol][state.fromRow] = null;
            GamePanel.board[state.rookFromCol][state.fromRow] = state.rook;
            state.rook.col = state.rookFromCol;
        }
        
        if (state.capturedPiece != null) GamePanel.pieces.add(state.capturedPiece);
    }
    
    private ArrayList<Move> getAllLegalMoves(int color) {
        ArrayList<Move> moves = new ArrayList<>();
        // Clone the list to avoid ConcurrentModificationException during simulation
        for (piece p : new ArrayList<>(GamePanel.pieces)) {
            if (p.color != color) continue;
            ArrayList<Point> legalMoves = p.getLegalMoves();
            for (Point pt : legalMoves) moves.add(new Move(p, p.col, p.row, pt.x, pt.y));
        }
        return moves;
    }
    
    private piece findKing(int color) {
        for (piece p : GamePanel.pieces) {
            if (p.type == Type.KING && p.color == color) return p;
        }
        return null;
    }
    
    public static class Move {
        public piece piece;
        public int fromCol, fromRow, toCol, toRow;
        public Move(piece piece, int fromCol, int fromRow, int toCol, int toRow) {
            this.piece = piece;
            this.fromCol = fromCol;
            this.fromRow = fromRow;
            this.toCol = toCol;
            this.toRow = toRow;
        }
    }
    
    private static class MoveScore {
        Move move;
        int score;
        MoveScore(Move move, int score) {
            this.move = move;
            this.score = score;
        }
    }
    
    private static class BoardState {
        piece piece;
        int fromCol, fromRow, toCol, toRow;
        piece capturedPiece;
        boolean moved, twoStepped;
        boolean castled = false;
        piece rook;
        int rookFromCol, rookToCol;
        // Added for promotion fix
        boolean promoted = false;
    }
}