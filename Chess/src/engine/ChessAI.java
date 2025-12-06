package engine;

import main.*;
import piece.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ChessAI {
    private GamePanel gp;
    
    // Piece values
    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 900;
    private static final int KING_VALUE = 20000;
    
    // Search depth
    private int searchDepth = 3;
    
    // Position value tables
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
    
    public ChessAI(GamePanel gp) {
        this.gp = gp;
    }
    
    public void setDepth(int depth) {
        this.searchDepth = Math.max(1, Math.min(depth, 5));
    }
    
    // Main entry point
    public Move getBestMove(int aiColor) {
        long startTime = System.currentTimeMillis();
        
        MoveScore result = minimax(searchDepth, aiColor, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
        
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("AI searched depth " + searchDepth + " in " + elapsed + "ms");
        if (result.move != null) {
            System.out.println("Best move: " + result.move.piece.type + " from (" + 
                             result.move.fromCol + "," + result.move.fromRow + ") to (" + 
                             result.move.toCol + "," + result.move.toRow + ") - Score: " + result.score);
        }
        
        return result.move;
    }
    
    // Minimax with alpha-beta pruning
    private MoveScore minimax(int depth, int color, int alpha, int beta, boolean maximizing) {
        if (depth == 0) {
            return new MoveScore(null, evaluateBoard(color));
        }
        
        ArrayList<Move> moves = getAllLegalMoves(color);
        
        if (moves.isEmpty()) {
            piece king = findKing(color);
            if (king != null && king.isAttacked()) {
                return new MoveScore(null, maximizing ? -100000 : 100000);
            }
            return new MoveScore(null, 0); // Stalemate
        }
        
        orderMoves(moves);
        Move bestMove = moves.get(0);
        
        if (maximizing) {
            int maxScore = Integer.MIN_VALUE;
            
            for (Move move : moves) {
                BoardState state = makeMove(move);
                int score = minimax(depth - 1, 1 - color, alpha, beta, false).score;
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
    
    // Evaluate board position
    private int evaluateBoard(int aiColor) {
        int score = 0;
        
        for (piece p : GamePanel.pieces) {
            int pieceValue = getPieceValue(p.type);
            int positionValue = getPositionValue(p);
            int totalValue = pieceValue + positionValue;
            
            if (p.color == aiColor) {
                score += totalValue;
            } else {
                score -= totalValue;
            }
        }
        
        score += evaluateMobility(aiColor);
        score += evaluateKingSafety(aiColor);
        
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
    
    private int getPositionValue(piece p) {
        int row = p.row;
        int col = p.col;
        
        if (p.color == GamePanel.BLACK) {
            row = 7 - row;
        }
        
        switch (p.type) {
            case PAWN:
                return PAWN_TABLE[row][col];
            case KNIGHT:
                return KNIGHT_TABLE[row][col];
            case BISHOP:
                return (int)((3 - Math.abs(3.5 - row)) * 5 + (3 - Math.abs(3.5 - col)) * 5);
            case ROOK:
                return (row == 1 || row == 6) ? 20 : 0;
            case QUEEN:
                return (int)((3 - Math.abs(3.5 - row)) * 3 + (3 - Math.abs(3.5 - col)) * 3);
            case KING:
                int pieceCount = GamePanel.pieces.size();
                if (pieceCount < 10) {
                    return (int)((3 - Math.abs(3.5 - row)) * 5 + (3 - Math.abs(3.5 - col)) * 5);
                } else {
                    return (int)(-(Math.abs(3.5 - row)) * 5 - (Math.abs(3.5 - col)) * 5);
                }
            default:
                return 0;
        }
    }
    
    private int evaluateMobility(int color) {
        int aiMoves = getAllLegalMoves(color).size();
        int opponentMoves = getAllLegalMoves(1 - color).size();
        return (aiMoves - opponentMoves) * 2;
    }
    
    private int evaluateKingSafety(int color) {
        piece king = findKing(color);
        if (king == null) return 0;
        
        int safety = 0;
        int pawnShield = 0;
        
        if (color == GamePanel.WHITE) {
            for (int c = king.col - 1; c <= king.col + 1; c++) {
                if (c < 0 || c > 7) continue;
                if (king.row - 1 < 0) continue;
                piece p = GamePanel.board[c][king.row - 1];
                if (p != null && p.type == Type.PAWN && p.color == color) {
                    pawnShield++;
                }
            }
        } else {
            for (int c = king.col - 1; c <= king.col + 1; c++) {
                if (c < 0 || c > 7) continue;
                if (king.row + 1 > 7) continue;
                piece p = GamePanel.board[c][king.row + 1];
                if (p != null && p.type == Type.PAWN && p.color == color) {
                    pawnShield++;
                }
            }
        }
        
        safety += pawnShield * 10;
        return safety;
    }
    
    private void orderMoves(ArrayList<Move> moves) {
        Collections.sort(moves, new Comparator<Move>() {
            @Override
            public int compare(Move m1, Move m2) {
                int score1 = getMoveOrderScore(m1);
                int score2 = getMoveOrderScore(m2);
                return score2 - score1;
            }
        });
    }
    
    private int getMoveOrderScore(Move move) {
        int score = 0;
        
        piece captured = GamePanel.board[move.toCol][move.toRow];
        if (captured != null) {
            score += 1000 + getPieceValue(captured.type);
        }
        
        int centerDist = (int)(Math.abs(move.toCol - 3.5) + Math.abs(move.toRow - 3.5));
        score += (7 - centerDist) * 10;
        
        return score;
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
        
        // Execute move on board array
        GamePanel.board[move.fromCol][move.fromRow] = null;
        GamePanel.board[move.toCol][move.toRow] = move.piece;
        move.piece.col = move.toCol;
        move.piece.row = move.toRow;
        move.piece.moved = true;
        
        // Remove captured piece from pieces list
        if (state.capturedPiece != null) {
            GamePanel.pieces.remove(state.capturedPiece);
        }
        
        return state;
    }
    
    private void undoMove(BoardState state) {
        // Restore piece position
        state.piece.col = state.fromCol;
        state.piece.row = state.fromRow;
        state.piece.moved = state.moved;
        state.piece.twoStepped = state.twoStepped;
        
        // Restore board array
        GamePanel.board[state.fromCol][state.fromRow] = state.piece;
        GamePanel.board[state.toCol][state.toRow] = state.capturedPiece;
        
        // Restore captured piece to pieces list
        if (state.capturedPiece != null) {
            GamePanel.pieces.add(state.capturedPiece);
        }
    }
    
    private ArrayList<Move> getAllLegalMoves(int color) {
        ArrayList<Move> moves = new ArrayList<>();
        
        for (piece p : new ArrayList<>(GamePanel.pieces)) {
            if (p.color != color) continue;
            
            ArrayList<Point> legalMoves = p.getLegalMoves();
            for (Point pt : legalMoves) {
                moves.add(new Move(p, p.col, p.row, pt.x, pt.y));
            }
        }
        
        return moves;
    }
    
    private piece findKing(int color) {
        for (piece p : GamePanel.pieces) {
            if (p.type == Type.KING && p.color == color) {
                return p;
            }
        }
        return null;
    }
    
    // Inner classes
    public static class Move {
        public piece piece;
        public int fromCol, fromRow;
        public int toCol, toRow;
        
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
        int fromCol, fromRow;
        int toCol, toRow;
        piece capturedPiece;
        boolean moved;
        boolean twoStepped;
    }
}