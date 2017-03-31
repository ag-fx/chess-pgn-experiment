package com.loloof64.chess_core.pieces

import com.loloof64.chess_core.game.*

abstract class ChessPiece(open val whitePlayer: Boolean) {
    companion object {
        fun fenToPiece(pieceFen: Char): ChessPiece? {
            return when(pieceFen){
                'P' -> Pawn(true)
                'p' -> Pawn(false)
                'N' -> Knight(true)
                'n' -> Knight(false)
                'B' -> Bishop(true)
                'b' -> Bishop(false)
                'R' -> Rook(true)
                'r' -> Rook(false)
                'Q' -> Queen(true)
                'q' -> Queen(false)
                'K' -> King(true)
                'k' -> King(false)
                else -> null
            }
        }

        fun thereIsBlockadeBetweenStartAndTarget(deltaFile: Int, deltaRank: Int, game: ChessGame, startSquare: Pair<Int, Int>): Boolean {
            val squaresBetweenTargetAndStart = arrayListOf<Pair<Int, Int>>()
            val deltaFileSign = if (deltaFile == 0) 0 else deltaFile / Math.abs(deltaFile)
            val deltaRankSign = if (deltaRank == 0) 0 else deltaRank / Math.abs(deltaRank)
            val numSquares = if (deltaFileSign == 0) Math.abs(deltaRank) - 1 else Math.abs(deltaFile) - 1
            (1..numSquares).forEach {
                squaresBetweenTargetAndStart.add(
                        Pair(startSquare.first + it * deltaRankSign, startSquare.second + it * deltaFileSign))
            }
            if (squaresBetweenTargetAndStart.any {
                game.board[it.first, it.second] != null
            }) return true
            return false
        }
    }

    abstract fun toFEN() : Char

    // With pseudo-legal moves, we still can leave our own king in chess
    abstract fun isValidPseudoLegalMove(game: ChessGame, startSquare: Pair<Int, Int>,
                                        endSquare: Pair<Int, Int>): Boolean
}

data class Pawn(override val whitePlayer: Boolean) : ChessPiece(whitePlayer){
    override fun isValidPseudoLegalMove(game: ChessGame, startSquare: Pair<Int, Int>,
                                        endSquare: Pair<Int, Int>): Boolean {
        val deltaFile = endSquare.second - startSquare.second
        val deltaRank = endSquare.first - startSquare.first

        val isValidTwoCellsJumpAsWhite = (game.info.whiteTurn && deltaRank == 2 && startSquare.first == ChessBoard.RANK_2
                                        && game.board[ChessBoard.RANK_3, startSquare.second] == null
                                        && game.board[ChessBoard.RANK_4, startSquare.second] == null)
        val isValidTwoCellsJumpAsBlack = (!game.info.whiteTurn && deltaRank == -2 && startSquare.first == ChessBoard.RANK_7
                                        && game.board[ChessBoard.RANK_6, startSquare.second] == null
                                        && game.board[ChessBoard.RANK_5, startSquare.second] == null)

        val isValidForwardMoveAsWhite = (game.info.whiteTurn && deltaRank == 1 && deltaFile == 0
                                        && game.board[startSquare.first+1, startSquare.second] == null)

        val isValidForwardMoveAsBlack = (!game.info.whiteTurn && deltaRank == -1 && deltaFile == 0
                                        && game.board[startSquare.first-1, startSquare.second] == null)

        val isValidCaptureMoveAsWhite = (game.info.whiteTurn && deltaRank == 1 && Math.abs(deltaFile) == 1
                && game.board[endSquare.first, endSquare.second] != null
                && !game.board[endSquare.first, endSquare.second]!!.whitePlayer)
        val isValidCaptureMoveAsBlack = (!game.info.whiteTurn && deltaRank == -1 && Math.abs(deltaFile) == 1
                && game.board[endSquare.first, endSquare.second] != null
                && game.board[endSquare.first, endSquare.second]!!.whitePlayer)

        val ownerPlayer = whitePlayer == game.info.whiteTurn
        val followValidLine = isValidTwoCellsJumpAsWhite || isValidTwoCellsJumpAsBlack
                || isValidForwardMoveAsWhite || isValidForwardMoveAsBlack
                || isValidCaptureMoveAsWhite || isValidCaptureMoveAsBlack

        return followValidLine && ownerPlayer
    }

    override fun toFEN(): Char {
        return if (whitePlayer) 'P' else 'p'
    }


}
data class Knight(override val whitePlayer: Boolean) : ChessPiece(whitePlayer){
    override fun isValidPseudoLegalMove(game: ChessGame, startSquare: Pair<Int, Int>, endSquare: Pair<Int, Int>): Boolean {
        val deltaFile = endSquare.second - startSquare.second
        val deltaRank = endSquare.first - startSquare.first

        val absDeltaFile = Math.abs(deltaFile)
        val absDeltaRank = Math.abs(deltaRank)

        val pieceAtEndCell = game.board[endSquare.first, endSquare.second]
        val endSquarePieceIsEnemy = pieceAtEndCell?.whitePlayer != whitePlayer
        val followValidLine = (absDeltaFile == 1 && absDeltaRank == 2) || (absDeltaFile == 2 && absDeltaRank == 1)

        val ownerPlayer = whitePlayer == game.info.whiteTurn

        return followValidLine && endSquarePieceIsEnemy && ownerPlayer
    }

    override fun toFEN(): Char {
        return if (whitePlayer) 'N' else 'n'
    }
}
data class Bishop(override val whitePlayer: Boolean) : ChessPiece(whitePlayer){
    override fun isValidPseudoLegalMove(game: ChessGame, startSquare: Pair<Int, Int>, endSquare: Pair<Int, Int>): Boolean {
        val deltaFile = endSquare.second - startSquare.second
        val deltaRank = endSquare.first - startSquare.first

        val absDeltaFile = Math.abs(deltaFile)
        val absDeltaRank = Math.abs(deltaRank)

        val pieceAtEndCell = game.board[endSquare.first, endSquare.second]
        val endSquarePieceIsEnemy = pieceAtEndCell?.whitePlayer != whitePlayer
        val followValidLine = absDeltaFile == absDeltaRank

        val ownerPlayer = whitePlayer == game.info.whiteTurn

        if (thereIsBlockadeBetweenStartAndTarget(deltaFile, deltaRank, game, startSquare)) return false

        return followValidLine && endSquarePieceIsEnemy && ownerPlayer
    }

    override fun toFEN(): Char {
        return if (whitePlayer) 'B' else 'b'
    }
}
data class Rook(override val whitePlayer: Boolean) : ChessPiece(whitePlayer){
    override fun isValidPseudoLegalMove(game: ChessGame, startSquare: Pair<Int, Int>, endSquare: Pair<Int, Int>): Boolean {
        val deltaFile = endSquare.second - startSquare.second
        val deltaRank = endSquare.first - startSquare.first

        val absDeltaFile = Math.abs(deltaFile)
        val absDeltaRank = Math.abs(deltaRank)

        val pieceAtEndCell = game.board[endSquare.first, endSquare.second]
        val endSquarePieceIsEnemy = pieceAtEndCell?.whitePlayer != whitePlayer
        val followValidLine = (absDeltaFile == 0 && absDeltaRank > 0) || (absDeltaFile > 0 && absDeltaRank == 0)

        if (thereIsBlockadeBetweenStartAndTarget(deltaFile, deltaRank, game, startSquare)) return false

        val ownerPlayer = whitePlayer == game.info.whiteTurn

        return followValidLine && endSquarePieceIsEnemy && ownerPlayer
    }

    override fun toFEN(): Char {
        return if (whitePlayer) 'R' else 'r'
    }
}
data class Queen(override val whitePlayer: Boolean) : ChessPiece(whitePlayer){
    override fun isValidPseudoLegalMove(game: ChessGame, startSquare: Pair<Int, Int>, endSquare: Pair<Int, Int>): Boolean {
        val deltaFile = endSquare.second - startSquare.second
        val deltaRank = endSquare.first - startSquare.first

        val absDeltaFile = Math.abs(deltaFile)
        val absDeltaRank = Math.abs(deltaRank)

        val pieceAtEndCell = game.board[endSquare.first, endSquare.second]
        val endSquarePieceIsEnemy = pieceAtEndCell?.whitePlayer != whitePlayer
        val followValidLine = (absDeltaFile == 0 && absDeltaRank > 0) || (absDeltaFile > 0 && absDeltaRank == 0) ||
                absDeltaFile == absDeltaRank

        if (thereIsBlockadeBetweenStartAndTarget(deltaFile, deltaRank, game, startSquare)) return false

        val ownerPlayer = whitePlayer == game.info.whiteTurn

        return followValidLine && endSquarePieceIsEnemy && ownerPlayer
    }

    override fun toFEN(): Char {
        return if (whitePlayer) 'Q' else 'q'
    }
}
data class King(override val whitePlayer: Boolean) : ChessPiece(whitePlayer){
    override fun isValidPseudoLegalMove(game: ChessGame, startSquare: Pair<Int, Int>, endSquare: Pair<Int, Int>): Boolean {
        val deltaFile = endSquare.second - startSquare.second
        val deltaRank = endSquare.first - startSquare.first

        val absDeltaFile = Math.abs(deltaFile)
        val absDeltaRank = Math.abs(deltaRank)

        val pieceAtEndCell = game.board[endSquare.first, endSquare.second]
        val followValidLine = absDeltaFile == 1 || absDeltaRank == 1
        val endPieceIsEnemy = pieceAtEndCell?.whitePlayer != whitePlayer

        val isLegalKingSideCastleAsWhite = game.info.whiteTurn
                && WhiteKingSideCastle in game.info.castles
                && deltaFile == 2 && deltaRank == 0
                && startSquare == Pair(ChessBoard.RANK_1, ChessBoard.FILE_E)
                && game.board[ChessBoard.RANK_1, ChessBoard.FILE_H] == Rook(whitePlayer = true)
                && game.board[ChessBoard.RANK_1, ChessBoard.FILE_F] == null
                && game.board[ChessBoard.RANK_1, ChessBoard.FILE_G] == null

        val isLegalQueenSideCastleAsWhite = game.info.whiteTurn
                && WhiteQueenSideCastle in game.info.castles
                && deltaFile == -2 && deltaRank == 0
                && startSquare == Pair(ChessBoard.RANK_1, ChessBoard.FILE_E)
                && game.board[ChessBoard.RANK_1, ChessBoard.FILE_A] == Rook(whitePlayer = true)
                && game.board[ChessBoard.RANK_1, ChessBoard.FILE_D] == null
                && game.board[ChessBoard.RANK_1, ChessBoard.FILE_C] == null
                && game.board[ChessBoard.RANK_1, ChessBoard.FILE_B] == null

        val isLegalKingSideCastleAsBlack = !game.info.whiteTurn
                && BlackKingSideCastle in game.info.castles
                && deltaFile == 2 && deltaRank == 0
                && startSquare == Pair(ChessBoard.RANK_8, ChessBoard.FILE_E)
                && game.board[ChessBoard.RANK_8, ChessBoard.FILE_H] == Rook(whitePlayer = false)
                && game.board[ChessBoard.RANK_8, ChessBoard.FILE_F] == null
                && game.board[ChessBoard.RANK_8, ChessBoard.FILE_G] == null

        val isLegalQueenSideCastleAsBlack = !game.info.whiteTurn
                && BlackQueenSideCastle in game.info.castles
                && deltaFile == -2 && deltaRank == 0
                && startSquare == Pair(ChessBoard.RANK_8, ChessBoard.FILE_E)
                && game.board[ChessBoard.RANK_8, ChessBoard.FILE_A] == Rook(whitePlayer = false)
                && game.board[ChessBoard.RANK_8, ChessBoard.FILE_D] == null
                && game.board[ChessBoard.RANK_8, ChessBoard.FILE_C] == null
                && game.board[ChessBoard.RANK_8, ChessBoard.FILE_B] == null

        val isLegalCastle = isLegalKingSideCastleAsWhite || isLegalQueenSideCastleAsWhite
                            || isLegalKingSideCastleAsBlack || isLegalQueenSideCastleAsBlack

        val ownerPlayer = whitePlayer == game.info.whiteTurn

        return ((followValidLine && endPieceIsEnemy) || isLegalCastle) && ownerPlayer
    }

    override fun toFEN(): Char {
        return if (whitePlayer) 'K' else 'k'
    }
}
