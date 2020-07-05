package snaykuu.gameLogic

import com.moandjiezana.toml.Toml
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

data class GameSettings @JvmOverloads constructor(
    override val boardWidth: Int = DEFAULT_BOARD_WIDTH,
    override val boardHeight: Int = DEFAULT_BOARD_HEIGHT,
    override val maximumThinkingTime: Int = DEFAULT_THINKING_TIME,
    override val growthFrequency: Int = DEFAULT_SNAKE_GROWTH_FREQUENCY,
    override val fruitFrequency: Int = DEFAULT_FRUIT_SPAWN_FREQUENCY,
    override val fruitGoal: Int = DEFAULT_FRUIT_GOAL,
    val gameSpeed: Int = DEFAULT_GAME_SPEED,
    val pixelsPerSquare: Int = DEFAULT_PIXELS_PER_SQUARE
): Metadata {
    init {
        require(boardWidth >= MIN_SIZE) { "Width must be at least $MIN_SIZE (including walls)" }
        require(boardHeight >= MIN_SIZE) { "Height must be at least $MIN_SIZE (including walls)" }
        require(maximumThinkingTime > 0) { "Maximum thinking time must be at least 1 ms" }
        require(growthFrequency > 0) { "Snake growth frequency must be at least 1" }
        require(fruitFrequency > 0) { "Fruit spawn frequency must be at least 1" }
        require(fruitGoal > 0) { "Fruit goal must be at least 1" }
        require(gameSpeed >= maximumThinkingTime) { "Game speed must be equal to or larger than maximum thinking time" }
        require(pixelsPerSquare >= 5) { "Pixels per square must be at least 5" }
    }

    companion object {
        fun load(): GameSettings = fromConfigFile() ?: GameSettings()

        fun fromConfigFile(): GameSettings? {
            val configFile: Path = settingsFile()
            val default = GameSettings()
            val data: Toml = parseConfigFile(configFile) ?: return null

            return default.copy(
                boardWidth = data.getInt("board.width", default.boardWidth),
                boardHeight = data.getInt("board.height", default.boardHeight),
                pixelsPerSquare = data.getInt("board.pixelsPerSquare", default.pixelsPerSquare),
                maximumThinkingTime = data.getInt("game.maxThinkingTime", default.maximumThinkingTime),
                gameSpeed = data.getInt("game.speed", default.gameSpeed),
                growthFrequency = data.getInt("game.growthFrequency", default.growthFrequency),
                fruitFrequency = data.getInt("game.fruitFrequency", default.fruitFrequency),
                fruitGoal = data.getInt("game.fruitGoal", default.fruitGoal)
            )
        }

        private fun parseConfigFile(config: Path): Toml? {
            val file: File = config.toFile()
            if(!file.exists()) {
                return null
            }
            return try {
                Toml().read(file)
            } catch(e: Exception) {
                System.err.println(e.message)
                null
            }
        }

        /**
         * Resolve the location of the configuration file for saved
         * GameSettings.
         * Windows: $AppData/snaykuu/config.toml
         * Mac OS: $HOME/Library/Preferences/snaykuu/config.toml
         * Linux/BSD/Other: $HOME/.config/snaykuu/config.toml
         */
        private fun settingsFile(): Path {
            val fallBack: Path =
                Paths.get("config.toml")
            val fileSubLocation = "/snaykuu/config.toml"
            val configDir: Path = when(Platform()) {
                Platform.Windows -> Paths.get(System.getenv("AppData"))
                Platform.Mac -> Paths.get(System.getProperty("user.home"), "Library/Preferences")
                Platform.BSD, Platform.Linux -> Paths.get(System.getProperty("user.home"), ".config")
                Platform.Other -> Paths.get(".")
            }

            return when {
                !configDir.toFile().exists() -> {
                    System.err.println("Config directory does not exist: $configDir")
                    fallBack
                }
                !configDir.toFile().canWrite() -> {
                    System.err.println("Cannot write to config directory: $configDir")
                    fallBack
                }
                !configDir.toFile().isDirectory -> {
                    System.err.println("Config path is not a directory: $configDir")
                    fallBack
                }
                else -> configDir.resolve(fileSubLocation)
            }
        }

//        private fun pathFrom(vararg parts: String, separator: Char = File.separatorChar): Path {
//            val s: String = separator.toString()
//            val fullPath: String = parts.map { it.removeSuffix(s) }.joinToString(separator = s) { it }
//            Paths.get(fullPath, s)
//            return File(fullPath).toPath()
//        }

        private const val MIN_SIZE: Int = 3
        private const val DEFAULT_BOARD_WIDTH: Int = 20
        private const val DEFAULT_BOARD_HEIGHT: Int = 20
        private const val DEFAULT_PIXELS_PER_SQUARE: Int = 25
        private const val DEFAULT_FRUIT_GOAL: Int = 5
        private const val DEFAULT_FRUIT_SPAWN_FREQUENCY: Int = 10
        private const val DEFAULT_SNAKE_GROWTH_FREQUENCY: Int = 5
        private const val DEFAULT_THINKING_TIME: Int = 100
        private const val DEFAULT_GAME_SPEED: Int = 300
    }
}

private enum class Platform(val symbol: String) {
    Windows("win"),
    Mac("mac"),
    Linux("linux"),
    BSD("bsd"),
    Other("###");

    companion object {
        operator fun invoke(): Platform {
            val os: String = System.getProperty("os.name")
            return values().firstOrNull { os.contains(it.symbol, ignoreCase = true) } ?: Other
        }
    }
}

private fun Toml.getInt(key: String, defaultValue: Number): Int =
    this.getLong(key, defaultValue.toLong()).toInt()