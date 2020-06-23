package snaykuu.gameLogic

interface Game {
    fun getCurrentState(): GameState
    fun getMetadata(): Metadata
    fun getGameResult(): GameResult
    fun tick(): Game
    fun state(): State
}

enum class State {
    NotStarted,
    Playing,
    Finished
}