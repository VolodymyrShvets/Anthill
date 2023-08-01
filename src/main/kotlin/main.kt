fun main() {
    val anthillWidth = 21
    val anthillHeight = 20
    val steps = 30

//    val anthill = Anthill(anthillWidth, anthillHeight, steps)
//
//    //anthill.printAnthill()
//    anthill.startDigging()
    //anthill.printAnthill()

    for (i in 1..50) {
        println("Test $i:")
        val anthill = Anthill(anthillWidth, anthillHeight, steps)

        anthill.startDigging()
        anthill.printAnthill()
    }
}
