import kotlin.random.Random

class Anthill(
    private val width: Int,
    private val height: Int,
    private val steps: Int
) {
    private val hill: Array<Array<String>> = Array(height) { Array(width) { GROUND } }
    private var currentStep: Pair<Int, Int> = Pair(-1, -1)
    private var hasMoves = true
    private val random = Random(System.currentTimeMillis())

    fun printAnthill() {
        print("   ")
        for (i in 0..<width)
            print(" %3d".format(i))
        println()
        for ((j, row) in hill.withIndex()) {
            print("%3d".format(j))
            for (element in row) {
                print(" $element")
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

    private fun digEntry() {
        if (hill[0][width / 2] != STONE) {
            hill[0][width / 2] = SPACE
            currentStep = Pair(0, width / 2)
        }
    }

    private fun placeStones() {
        val totalArea = width * height
        var remainingArea = (totalArea * 0.15).toInt()
        //println("total are: $totalArea, stone area: $remainingArea")

        while (remainingArea > 0) {
            val stone = StoneType.entries.toTypedArray().random()
            remainingArea -= stone.area
            //println("adding ${stone.name}, remaining area: $remainingArea")
            val x = Random.nextInt(width)
            val y = Random.nextInt(height)

            when (stone) {
                StoneType.SMALL -> {
                    placeSmallStone(x, y, Random.nextBoolean())
                }

                StoneType.MEDIUM -> {
                    placeMediumStone(x, y)
                }

                StoneType.BIG -> {
                    placeBigStone(x, y, Random.nextBoolean())
                }
            }
        }

        //println("Stones List: $stonesList")
    }

    private fun placeSmallStone(x: Int, y: Int, isHorizontal: Boolean) {
        hill[y][x] = STONE
        if (isHorizontal)
            if (x + 1 < width)
                hill[y][x + 1] = STONE
        if (!isHorizontal)
            if (y + 1 < height)
                hill[y + 1][x] = STONE
    }

    private fun placeMediumStone(x: Int, y: Int) {
        hill[y][x] = STONE
        if (x + 1 < width)
            hill[y][x + 1] = STONE
        if (y + 1 < height)
            hill[y + 1][x] = STONE
        if (x + 1 < width && y + 1 < height)
            hill[y + 1][x + 1] = STONE
    }

    private fun placeBigStone(x: Int, y: Int, isHorizontal: Boolean) {
        hill[y][x] = STONE
        if (isHorizontal) {
            if (x + 1 < width)
                hill[y][x + 1] = STONE
            if (x + 2 < width)
                hill[y][x + 2] = STONE
            if (y + 1 < height) {
                hill[y + 1][x] = STONE
                if (x + 1 < width)
                    hill[y + 1][x + 1] = STONE
                if (x + 2 < width)
                    hill[y + 1][x + 2] = STONE
            }
        } else {
            if (y + 1 < height)
                hill[y + 1][x] = STONE
            if (y + 2 < height)
                hill[y + 2][x] = STONE
            if (x + 1 < width) {
                hill[y][x + 1] = STONE
                if (y + 1 < height)
                    hill[y + 1][x + 1] = STONE
                if (y + 2 < height)
                    hill[y + 2][x + 1] = STONE
            }
        }
    }

    fun startDigging() {
        placeStones()
        digEntry()

        if (currentStep.first == -1 || currentStep.second == -1)
            return

        var step = 1

        while (hasMoves) {
//            if (step >= steps) {
//                hasMoves = false
//                break
//            }

            val randomNumber = random.nextDouble()
            if (step > 10 && randomNumber > 0.8) {
                break
            }

            //print("current step: (${currentStep.first};${currentStep.second})")

            val availableDirections = getAvailableDirectionsForNextStep()

            if (availableDirections.isEmpty())
                break

            val randomDirection = availableDirections.random()
            //print(" from:$availableDirections")
            //println(" choose direction: $randomDirection")

            val (oldY, oldX) = currentStep

            currentStep = when (randomDirection) {
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

            hill[currentStep.first][currentStep.second] = SPACE

            step++

            //printAnthill()
        }
    }

    private fun getAvailableDirectionsForNextStep(): List<Directions> {
        var availableDirections = mutableListOf<Directions>()
        val (currY, currX) = currentStep

        // check for move LEFT
        if (currX - 1 >= 0)
            if (hill[currY][currX - 1] != SPACE
                && hill[currY][currX - 1] != STONE
                && !haveSpaceOnTop(currY, currX - 1)
                && !haveSpaceOnLeft(currY, currX - 1)
                && !haveSpaceOnBottom(currY, currX - 1)
            )
                availableDirections.add(Directions.LEFT)

        // check for move DOWN
        if (currY + 1 < height)
            if (hill[currY + 1][currX] != SPACE
                && hill[currY + 1][currX] != STONE
                && !haveSpaceOnRight(currY + 1, currX)
                && !haveSpaceOnLeft(currY + 1, currX)
                && !haveSpaceOnBottom(currY + 1, currX)
            )
                availableDirections.add(Directions.DOWN)

        // check for move RIGHT
        if (currX + 1 < width)
            if (hill[currY][currX + 1] != SPACE
                && hill[currY][currX + 1] != STONE
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
            if (hill[y - 1][x] == SPACE) {
//                println("=================================")
//                println("space on top for y=${y} x=$x")
                return true
            }
        return false
    }

    private fun haveSpaceOnLeft(y: Int, x: Int): Boolean {
        if (x - 1 >= 0)
            if (hill[y][x - 1] == SPACE) {
//                println("=================================")
//                println("space on left for y=$y x=${x}")
                return true
            }
        return false
    }

    private fun haveSpaceOnRight(y: Int, x: Int): Boolean {
        if (x + 1 < width)
            if (hill[y][x + 1] == SPACE) {
//                println("=================================")
//                println("space on left for y=$y x=${x}")
                return true
            }
        return false
    }

    private fun haveSpaceOnBottom(y: Int, x: Int): Boolean {
        if (y + 1 < height)
            if (hill[y + 1][x] == SPACE) {
//                println("=================================")
//                println("space on down for y=${y} x=$x")
                return true
            }
        return false
    }

    companion object {
        const val GROUND = "▒▒▒"
        const val STONE = "███"
        const val SPACE = "   "
    }

    enum class Directions {
        LEFT,
        DOWN,
        RIGHT
    }

    enum class StoneType
        (val area: Int) {
        SMALL(2),  // 2x1
        MEDIUM(4), // 2x2
        BIG(6)     // 2x3
    }
}