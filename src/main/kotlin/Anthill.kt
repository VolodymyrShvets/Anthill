import java.io.File
import kotlin.random.Random

class Anthill(
    private val width: Int,
    private val height: Int,
    private val stonePercentage: Float,
    private val continueMovingPercentage: Float,
    private val addNewAntPercentage: Float
) {
    private val hill: Array<Array<String>> = Array(height) { Array(width) { Ground.GROUND.strVal } }
    private var ants: MutableList<Ant> = mutableListOf()

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

    fun printAnthillIntoFile(file: File) {
        file.printWriter().use { out ->
            for (row in hill) {
                for (element in row) {
                    out.append(element)
                }
                out.append("\n")
            }
        }
    }

    private fun digEntry() {
        if (hill[0][width / 2] != Ground.STONE.strVal) {
            hill[0][width / 2] = Ground.SPACE.strVal
            ants.add(Ant(Pair(0, width / 2)))
        }
    }

    private fun placeStones() {
        val totalArea = width * height
        var remainingArea = (totalArea * stonePercentage).toInt()
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

    private fun placeSmallStone(x: Int, y: Int, isHorizontal: Boolean) { // TODO refactor stone placement
        hill[y][x] = Ground.STONE.strVal
        if (isHorizontal) {
            if (x + 1 < width)
                hill[y][x + 1] = Ground.STONE.strVal
        } else {
            if (y + 1 < height)
                hill[y + 1][x] = Ground.STONE.strVal
        }
    }

    private fun placeMediumStone(x: Int, y: Int) {
        hill[y][x] = Ground.STONE.strVal
        if (x + 1 < width)
            hill[y][x + 1] = Ground.STONE.strVal
        if (y + 1 < height)
            hill[y + 1][x] = Ground.STONE.strVal
        if (x + 1 < width && y + 1 < height)
            hill[y + 1][x + 1] = Ground.STONE.strVal
    }

    private fun placeBigStone(x: Int, y: Int, isHorizontal: Boolean) {
        hill[y][x] = Ground.STONE.strVal
        if (isHorizontal) {
            if (x + 1 < width)
                hill[y][x + 1] = Ground.STONE.strVal
            if (x + 2 < width)
                hill[y][x + 2] = Ground.STONE.strVal
            if (y + 1 < height) {
                hill[y + 1][x] = Ground.STONE.strVal
                if (x + 1 < width)
                    hill[y + 1][x + 1] = Ground.STONE.strVal
                if (x + 2 < width)
                    hill[y + 1][x + 2] = Ground.STONE.strVal
            }
        } else {
            if (y + 1 < height)
                hill[y + 1][x] = Ground.STONE.strVal
            if (y + 2 < height)
                hill[y + 2][x] = Ground.STONE.strVal
            if (x + 1 < width) {
                hill[y][x + 1] = Ground.STONE.strVal
                if (y + 1 < height)
                    hill[y + 1][x + 1] = Ground.STONE.strVal
                if (y + 2 < height)
                    hill[y + 2][x + 1] = Ground.STONE.strVal
            }
        }
    }

    fun startDigging() {
        placeStones()
        digEntry()

        if (ants.isEmpty())
            return

        while (canAntsMove()) {
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
                            ant.apply { canMove = false }
                        }
                        if (addNewAntOrNo < addNewAntPercentage) {
                            //println("$addNewAntOrNo -- adding new ant for ${ant.currentStep.first} ${ant.currentStep.second}")
                            ant.apply { step = 1 }
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
                    ant.apply { canMove = false }
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

                hill[ant.currentStep.first][ant.currentStep.second] = Ground.SPACE.strVal

                ant.step++
            }
            ants.addAll(newAnts)
            //printAnthill()
        }
    }

    private fun canAntsMove(): Boolean {
        for (ant in ants)
            if (ant.canMove)
                return true
        return false
    }

    private fun getAvailableDirectionsForNextStep(ant: Ant): List<Directions> {
        val availableDirections = mutableListOf<Directions>()
        val (currY, currX) = ant.currentStep

        // check for move LEFT
        if (currX - 1 >= 0)
            if (hill[currY][currX - 1] != Ground.SPACE.strVal
                && hill[currY][currX - 1] != Ground.STONE.strVal
                && !haveSpaceOnTop(currY, currX - 1)
                && !haveSpaceOnLeft(currY, currX - 1)
                && !haveSpaceOnBottom(currY, currX - 1)
            )
                availableDirections.add(Directions.LEFT)

        // check for move DOWN
        if (currY + 1 < height)
            if (hill[currY + 1][currX] != Ground.SPACE.strVal
                && hill[currY + 1][currX] != Ground.STONE.strVal
                && !haveSpaceOnRight(currY + 1, currX)
                && !haveSpaceOnLeft(currY + 1, currX)
                && !haveSpaceOnBottom(currY + 1, currX)
            )
                availableDirections.add(Directions.DOWN)

        // check for move RIGHT
        if (currX + 1 < width)
            if (hill[currY][currX + 1] != Ground.SPACE.strVal
                && hill[currY][currX + 1] != Ground.STONE.strVal
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
            if (hill[y - 1][x] == Ground.SPACE.strVal) {
//                println("=================================")
//                println("space on top for y=${y} x=$x")
                return true
            }
        return false
    }

    private fun haveSpaceOnLeft(y: Int, x: Int): Boolean {
        if (x - 1 >= 0)
            if (hill[y][x - 1] == Ground.SPACE.strVal) {
//                println("=================================")
//                println("space on left for y=$y x=${x}")
                return true
            }
        return false
    }

    private fun haveSpaceOnRight(y: Int, x: Int): Boolean {
        if (x + 1 < width)
            if (hill[y][x + 1] == Ground.SPACE.strVal) {
//                println("=================================")
//                println("space on left for y=$y x=${x}")
                return true
            }
        return false
    }

    private fun haveSpaceOnBottom(y: Int, x: Int): Boolean {
        if (y + 1 < height)
            if (hill[y + 1][x] == Ground.SPACE.strVal) {
//                println("=================================")
//                println("space on down for y=${y} x=$x")
                return true
            }
        return false
    }

    enum class Ground
        (val strVal: String) {
        GROUND("▒▒▒"),
        STONE("███"),
        SPACE("   ")
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
