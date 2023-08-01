import java.io.File

fun main() {
    val anthillWidth = 43
    val anthillHeight = 38

    val fileName = "anthill.txt"
    val file = File(fileName)

    val anthill = Anthill(
        width = anthillWidth,
        height = anthillHeight,
        stonePercentage = 0.15f,
        continueMovingPercentage = 0.8f,
        addNewAntPercentage = 0.6f)

    anthill.startDigging()
    anthill.printAnthill()
    anthill.printAnthillIntoFile(file)

//    for (i in 1..50) {
//        println("Test $i:")
//        val anthill = Anthill(
//            width = anthillWidth,
//            height = anthillHeight,
//            stonePercentage = 0.15f,
//            continueMovingPercentage = 0.8f,
//            addNewAntPercentage = 0.6f
//        )
//
//        anthill.startDigging()
//        anthill.printAnthill()
//        anthill.printAnthillIntoFile(file)
//    }
}
