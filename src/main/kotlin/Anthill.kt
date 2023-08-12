import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random

class Anthill(
    private val width: Int,
    private val height: Int,
    private val stonePercentage: Float,
    private val continueMovingPercentage: Float,
    private val addNewAntPercentage: Float
) {
    private val hill: Array<Array<Ground>> = Array(height) { Array(width) { Ground.GROUND } }
    private var ants: MutableList<Ant> = mutableListOf()

    fun printAnthill() {
        print("   ")
        for (i in 0..<width)
            print(" %3d".format(i))
        println()
        for ((j, row) in hill.withIndex()) {
            print("%3d".format(j))
            for (element in row) {
                print(" ${element.strVal}")
            }
            println()
        }
//        for (row in hill) {
//            for (element in row) {
//                print("$element")
//            }
//            println()
//        }
    }

    fun printAnthillIntoFile(fileName: String = "anthill.txt") {
        val file = File(fileName)
        file.printWriter().use { out ->
            for (row in hill) {
                for (element in row) {
                    out.append(element.strVal)
                }
                out.append("\n")
            }
        }
    }

    private fun digEntry() {
        if (hill[0][width / 2] != Ground.STONE) {
            hill[0][width / 2] = Ground.SPACE
            ants.add(Ant(Pair(0, width / 2)))
        }
    }

    private fun placeStones() {
        val totalArea = width * height
        var remainingArea = (totalArea * stonePercentage).toInt()
        //println("total are: $totalArea, stone area: $remainingArea")

        while (remainingArea > 0) {
            val stone = StoneType.entries.toTypedArray().random()
            var stoneWidth = stone.width
            var stoneHeight = stone.height

            remainingArea -= (stoneWidth * stoneHeight)
            //println("adding ${stone.name}, remaining area: $remainingArea")
            val x = Random.nextInt(width)
            val y = Random.nextInt(height)

            if (Random.nextBoolean())
                stoneWidth = stoneHeight.also { stoneHeight = stoneWidth }

            for (yS in y..<(y + stoneWidth))
                for (xS in x..<(x + stoneHeight))
                    if (yS < width && xS < height)
                        hill[yS][xS] = Ground.STONE
        }

        //println("Stones List: $stonesList")
    }

    fun startDigging() {
        placeStones()
        digEntry()

        if (ants.isEmpty())
            return

        while (ants.any { it.canMove }) {
            val newAnts: MutableList<Ant> = mutableListOf()

            for (ant in ants) {
                //println("ants count: ${ants.size}")
                if (ant.canMove)
                    if (ant.step > 10) {
                        val random = Random(System.currentTimeMillis())
                        val stopOrNo = random.nextDouble()
                        val addNewAntOrNo = random.nextDouble()

                        if (stopOrNo > continueMovingPercentage) {
                            //println("$stopOrNo -- end of life for ant ${ant.currentStep.first} ${ant.currentStep.second}")
                            ant.canMove = false
                        }
                        if (addNewAntOrNo < addNewAntPercentage) {
                            //println("$addNewAntOrNo -- adding new ant for ${ant.currentStep.first} ${ant.currentStep.second}")
                            ant.step = 1
                            newAnts.add(
                                Ant(Pair(ant.currentStep.first, ant.currentStep.second))
                            )
                            //println("new ants count: ${ants.size}")
                        }
                    }

                val availableDirections = getAvailableDirectionsForNextStep(ant)
                //println("for ${ant.currentStep.first}, ${ant.currentStep.second} from:$availableDirections")

                if (availableDirections.isEmpty()) {
                    //println("no moves for ant ${ant.currentStep.first} ${ant.currentStep.second}")
                    ant.canMove = false
                    continue
                }

                val randomDirection = availableDirections.random()
                //println(" choose direction: $randomDirection")

                val (oldY, oldX) = ant.currentStep

                ant.currentStep = when (randomDirection) {
                    Directions.LEFT -> {
                        Pair(oldY, oldX - 1)
                    }

                    Directions.DOWN -> {
                        Pair(oldY + 1, oldX)
                    }

                    Directions.RIGHT -> {
                        Pair(oldY, oldX + 1)
                    }
                }

                hill[ant.currentStep.first][ant.currentStep.second] = Ground.SPACE

                ant.step++
            }
            ants.addAll(newAnts)
            //printAnthill()
        }
    }

    private fun getAvailableDirectionsForNextStep(ant: Ant): List<Directions> {
        val availableDirections = mutableListOf<Directions>()
        val (currY, currX) = ant.currentStep

        // check for move LEFT
        if (currX - 1 >= 0)
            if (hill[currY][currX - 1] != Ground.SPACE
                && hill[currY][currX - 1] != Ground.STONE
                && !haveSpaceOnTop(currY, currX - 1)
                && !haveSpaceOnLeft(currY, currX - 1)
                && !haveSpaceOnBottom(currY, currX - 1)
            )
                availableDirections.add(Directions.LEFT)

        // check for move DOWN
        if (currY + 1 < height)
            if (hill[currY + 1][currX] != Ground.SPACE
                && hill[currY + 1][currX] != Ground.STONE
                && !haveSpaceOnRight(currY + 1, currX)
                && !haveSpaceOnLeft(currY + 1, currX)
                && !haveSpaceOnBottom(currY + 1, currX)
            )
                availableDirections.add(Directions.DOWN)

        // check for move RIGHT
        if (currX + 1 < width)
            if (hill[currY][currX + 1] != Ground.SPACE
                && hill[currY][currX + 1] != Ground.STONE
                && !haveSpaceOnTop(currY, currX + 1)
                && !haveSpaceOnRight(currY, currX + 1)
                && !haveSpaceOnBottom(currY, currX + 1)
            )
                availableDirections.add(Directions.RIGHT)

        //println(availableDirections)
        return availableDirections
    }

    private fun haveSpaceOnTop(y: Int, x: Int): Boolean {
        if (y - 1 >= 0)
            if (hill[y - 1][x] == Ground.SPACE) {
//                println("=================================")
//                println("space on top for y=${y} x=$x")
                return true
            }
        return false
    }

    private fun haveSpaceOnLeft(y: Int, x: Int): Boolean {
        if (x - 1 >= 0)
            if (hill[y][x - 1] == Ground.SPACE) {
//                println("=================================")
//                println("space on left for y=$y x=${x}")
                return true
            }
        return false
    }

    private fun haveSpaceOnRight(y: Int, x: Int): Boolean {
        if (x + 1 < width)
            if (hill[y][x + 1] == Ground.SPACE) {
//                println("=================================")
//                println("space on left for y=$y x=${x}")
                return true
            }
        return false
    }

    private fun haveSpaceOnBottom(y: Int, x: Int): Boolean {
        if (y + 1 < height)
            if (hill[y + 1][x] == Ground.SPACE) {
//                println("=================================")
//                println("space on down for y=${y} x=$x")
                return true
            }
        return false
    }

    enum class Ground
        (val strVal: String, val colorVal: Color) {
        GROUND("▒▒▒", Color.GREEN),
        STONE("███", Color.DARK_GRAY),
        SPACE("   ", Color.WHITE)
    }

    enum class Directions {
        LEFT,
        DOWN,
        RIGHT
    }

    enum class StoneType
        (val width: Int, val height: Int) {
        SMALL(2, 1),  // 2x1
        MEDIUM(2, 2), // 2x2
        BIG(2, 3);     // 2x3
    }
}
