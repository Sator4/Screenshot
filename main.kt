import javafx.application.Application
import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.control.Button
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.scene.transform.Scale
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.awt.*
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.lang.Math.min
import java.lang.Math.round
import java.util.*
import javax.imageio.ImageIO
import kotlin.concurrent.schedule
import kotlin.math.*


val windowScale = 0.75
val imageScale = 0.6
val screenHeight = 1080.0
val screenWidth = 1920.0
//val screenshotXOffset = screenWidth * windowScale * 0.12


class Screenshot : Application() {

    ////////  переменные

    var sel_x: Double = 0.0
    var sel_y: Double = 0.0
    var last_x: Double = 1920.0
    var last_y: Double = 1080.0

    var brushColor = Color.TRANSPARENT

    var selecting = false
    var erasing = false

    var currentStackPaneScale = Scale(imageScale, imageScale)

    var saveAsSaveAs = false

    override fun start(primaryStage: Stage) {
        ////////  кнопки
        val menuBar = MenuBar()
        val menuMenu = Menu("Файл")
        val menuOpen = MenuItem("Открыть")
        val menuSave = MenuItem("Сохранить")
        val menuSaveAs = MenuItem("Сохранить как")
        val menuClose = MenuItem("Закрыть изображение (очистить экран)")


        menuBar.menus.add(menuMenu)
        menuMenu.items.add(menuOpen)
        menuMenu.items.add(menuSave)
        menuMenu.items.add(menuSaveAs)
        menuMenu.items.add(menuClose)


        val takeScreenshot = Button("Сделать скриншот")

        val slider = Slider(0.0, 10.0, 0.0)
        slider.isShowTickLabels = true
        slider.isShowTickMarks = true
        val sliderLabel = TextFlow(Text("Задержка, с"))
        val minimizeCheck = CheckBox()
        val minimizeLabel = TextFlow(Text("Свернуть перед скриншотом"))

        val selectCheck = CheckBox()
        val selectLabel = TextFlow(Text("Обрезать:"))
        val selectCut = Button("Обрезать")


        val brushSize = Slider(1.0, 100.0, 10.0)
        val brushSizeLabel = TextFlow(Text("Размер кисти:"))

        val eraserButton = Button("Ластик")
        val blackButton = Button("  ")
        blackButton.background = Background(BackgroundFill(Color.BLACK, null, null))
        val greyButton = Button("  ")
        greyButton.background = Background(BackgroundFill(Color.GREY, null, null))
        val whiteButton = Button("  ")
        whiteButton.background = Background(BackgroundFill(Color.WHITE, null, null))
        val redButton = Button("  ")
        redButton.background = Background(BackgroundFill(Color.RED, null, null))
        val orangeButton = Button("  ")
        orangeButton.background = Background(BackgroundFill(Color.ORANGE, null, null))
        val yellowButton = Button("  ")
        yellowButton.background = Background(BackgroundFill(Color.YELLOW, null, null))
        val greenButton = Button("  ")
        greenButton.background = Background(BackgroundFill(Color.rgb(0, 255, 0), null, null))
        val blueButton = Button("  ")
        blueButton.background = Background(BackgroundFill(Color.BLUE, null, null))
        val darkBlueButton = Button("  ")
        darkBlueButton.background = Background(BackgroundFill(Color.DARKBLUE, null, null))
        val violetButton = Button("  ")
        violetButton.background = Background(BackgroundFill(Color.rgb(170, 0, 170), null, null))


        val plusButton = Button("+")
        val minusButton = Button("-")
        val zeroButton = Button("0")


        val canvasImage = Canvas()
        canvasImage.height = screenHeight * windowScale / imageScale
        canvasImage.width = screenWidth * windowScale / imageScale

        val canvasDrawing = Canvas()
        canvasDrawing.height = screenHeight * windowScale / imageScale
        canvasDrawing.width = screenWidth * windowScale / imageScale

        val canvasSelecting = Canvas()
        canvasSelecting.height = screenHeight * windowScale / imageScale
        canvasSelecting.width = screenWidth * windowScale / imageScale




        val globalBox = VBox(10.0)
        val buttonBox = HBox(10.0)
        val delayBox = VBox(5.0)
        val minimizeBox = VBox(5.0)
        val selectBox = VBox(5.0)
        val brushSizeBox = VBox(5.0)
        val colorsBox = HBox(2.0)

        val stackPane = StackPane()

        buttonBox.alignment = Pos.TOP_CENTER
        delayBox.alignment = Pos.TOP_CENTER
        minimizeBox.alignment = Pos.TOP_CENTER
        selectBox.alignment = Pos.TOP_CENTER
        brushSizeBox.alignment = Pos.TOP_CENTER
        colorsBox.alignment = Pos.TOP_CENTER


        globalBox.children.add(menuBar)
        globalBox.children.add(buttonBox)
        globalBox.children.add(stackPane)

        buttonBox.children.add(takeScreenshot)
        buttonBox.children.add(delayBox)
        buttonBox.children.add(minimizeBox)
        buttonBox.children.add(selectBox)
        buttonBox.children.add(selectCut)
        buttonBox.children.add(brushSizeBox)
        buttonBox.children.add(colorsBox)
        buttonBox.children.add(plusButton)
        buttonBox.children.add(minusButton)
        buttonBox.children.add(zeroButton)

        delayBox.children.add(sliderLabel)
        delayBox.children.add(slider)

        minimizeBox.children.add(minimizeLabel)
        minimizeBox.children.add(minimizeCheck)

        selectBox.children.add(selectLabel)
        selectBox.children.add(selectCheck)

        brushSizeBox.children.add(brushSizeLabel)
        brushSizeBox.children.add(brushSize)

        colorsBox.children.add(eraserButton)
        colorsBox.children.add(blackButton)
        colorsBox.children.add(greyButton)
        colorsBox.children.add(whiteButton)
        colorsBox.children.add(redButton)
        colorsBox.children.add(orangeButton)
        colorsBox.children.add(yellowButton)
        colorsBox.children.add(greenButton)
        colorsBox.children.add(blueButton)
        colorsBox.children.add(darkBlueButton)
        colorsBox.children.add(violetButton)

        stackPane.children.add(canvasImage)
        stackPane.children.add(canvasDrawing)
        stackPane.children.add(canvasSelecting)




        //создаем окно
        val scene = Scene(globalBox, screenWidth * windowScale, screenHeight * windowScale)

        primaryStage.title = "Command"
        primaryStage.scene = scene
        primaryStage.show()


        selectCheck.onAction = EventHandler {  // стереть canvasSelecting при убирании галочки
//            println("selecting = $selecting")
            if (!selectCheck.isSelected){
                canvasSelecting.graphicsContext2D.clearRect(0.0, 0.0, canvasSelecting.width, canvasSelecting.height)
                selecting = false
                sel_x = 0.0
                sel_y = 0.0
                last_x = screenWidth
                last_y = screenHeight
            }
        }



        takeScreenshot.onAction = EventHandler {    // сделать скриншот
            canvasImage.height = screenHeight * windowScale / imageScale
            canvasImage.width = screenWidth * windowScale / imageScale
            canvasDrawing.height = screenHeight * windowScale / imageScale
            canvasDrawing.width = screenWidth * windowScale / imageScale
            canvasSelecting.height = screenHeight * windowScale / imageScale
            canvasSelecting.width = screenWidth * windowScale / imageScale


            var snapDelay = round(slider.value * 10000) * 0.1
            if (minimizeCheck.isSelected){
                snapDelay += 200
                primaryStage.isIconified = true
            }

            Timer().schedule(snapDelay.toLong()) {
                Platform.runLater{
//                    println("Delay = $snapDelay ms")
                    saveAsPng()
                    primaryStage.isIconified = false
                    val fileName = getFileName()

                    val image = Image("file:\\$fileName")
                    currentStackPaneScale = Scale(imageScale, imageScale)
                    stackPane.transforms.setAll(currentStackPaneScale)

                    canvasImage.graphicsContext2D.drawImage(image, 0.0, 0.0)
                    canvasDrawing.graphicsContext2D.clearRect(0.0, 0.0, canvasDrawing.width, canvasDrawing.height)
                    canvasSelecting.graphicsContext2D.clearRect(0.0, 0.0, canvasSelecting.width, canvasSelecting.height)
                }
            }
        }

        stackPane.onMousePressed = EventHandler<MouseEvent> { event ->   // нажатие на канвасы
            if (selectCheck.isSelected){
                selecting = true
                sel_x = event.x
                sel_y = event.y
            }
            else if (!erasing){
                canvasDrawing.graphicsContext2D.fill = brushColor
                canvasDrawing.graphicsContext2D.fillOval(event.x - brushSize.value/2, event.y - brushSize.value/2,
                    brushSize.value, brushSize.value)
            }
            else {
                canvasDrawing.graphicsContext2D.clearRect(event.x - brushSize.value/2, event.y - brushSize.value/2,
                    brushSize.value, brushSize.value)
            }
        }

        stackPane.onMouseDragged = EventHandler<MouseEvent> { event ->  // перемещение зажатой лкм по канвасам
            if (selecting){
                canvasSelecting.graphicsContext2D.clearRect(0.0, 0.0, canvasSelecting.width, canvasSelecting.height)
                canvasSelecting.graphicsContext2D.strokeRect(min(sel_x, event.x), min(sel_y, event.y),
                    abs(sel_x - event.x), abs(sel_y - event.y))
                last_x = event.x
                last_y = event.y
            }
            else if (!erasing){
                canvasDrawing.graphicsContext2D.fill = brushColor
                canvasDrawing.graphicsContext2D.fillOval(event.x - brushSize.value/2, event.y - brushSize.value/2,
                    brushSize.value, brushSize.value)
            }
            else {
                canvasDrawing.graphicsContext2D.clearRect(event.x - brushSize.value/2, event.y - brushSize.value/2,
                    brushSize.value, brushSize.value)
            }
        }


        selectCut.onAction = EventHandler {   // нажатие на вырезать
            if (selecting){
                val params = SnapshotParameters()
                params.fill = Color.TRANSPARENT
                val image1 = canvasImage.snapshot(params, null)
                val image2 = canvasDrawing.snapshot(params, null)
                val croppedImage = WritableImage(image1.pixelReader, min(sel_x, last_x).toInt(), min(sel_y, last_y).toInt(),
                    abs(sel_x - last_x).toInt(), abs(sel_y - last_y).toInt())
                val croppedDrawing = WritableImage(image2.pixelReader, min(sel_x, last_x).toInt(), min(sel_y, last_y).toInt(),
                    abs(sel_x - last_x).toInt(), abs(sel_y - last_y).toInt())

                canvasImage.graphicsContext2D.clearRect(0.0, 0.0, canvasImage.width, canvasImage.height)
                canvasImage.graphicsContext2D.drawImage(croppedImage, 0.0, 0.0)
                canvasDrawing.graphicsContext2D.clearRect(0.0, 0.0, canvasDrawing.width, canvasDrawing.height)
                canvasDrawing.graphicsContext2D.drawImage(croppedDrawing, 0.0, 0.0)
                canvasSelecting.graphicsContext2D.clearRect(0.0, 0.0, canvasSelecting.width, canvasSelecting.height)
            }
        }


        eraserButton.onAction = EventHandler {
            erasing = true
        }
        blackButton.onAction = EventHandler {
            brushColor = Color.BLACK
            erasing = false
        }
        greyButton.onAction = EventHandler {
            brushColor = Color.GREY
            erasing = false
        }
        whiteButton.onAction = EventHandler {
            brushColor = Color.WHITE
            erasing = false
        }
        redButton.onAction = EventHandler {
            brushColor = Color.RED
            erasing = false
        }
        orangeButton.onAction = EventHandler {
            brushColor = Color.ORANGE
            erasing = false
        }
        yellowButton.onAction = EventHandler {
            brushColor = Color.YELLOW
            erasing = false
        }
        greenButton.onAction = EventHandler {
            brushColor = Color.rgb(0, 255, 0)
            erasing = false
        }
        blueButton.onAction = EventHandler {
            brushColor = Color.BLUE
            erasing = false
        }
        darkBlueButton.onAction = EventHandler {
            brushColor = Color.DARKBLUE
            erasing = false
        }
        violetButton.onAction = EventHandler {
            brushColor = Color.rgb(170, 0, 170)
            erasing = false
        }


        plusButton.onAction = EventHandler {
            currentStackPaneScale.x *= 1.25
            currentStackPaneScale.y *= 1.25
            stackPane.transforms.setAll(currentStackPaneScale)
//            println(currentStackPaneScale)
        }
        minusButton.onAction = EventHandler {
            currentStackPaneScale.x /= 1.25
            currentStackPaneScale.y /= 1.25
            stackPane.transforms.setAll(currentStackPaneScale)
//            println(currentStackPaneScale)
        }
        zeroButton.onAction = EventHandler {
            currentStackPaneScale = Scale(imageScale, imageScale)
            stackPane.transforms.setAll(currentStackPaneScale)
        }


        menuSave.onAction = EventHandler {    // сохранить (ctrl+s)
            saveAsSaveAs = false
            saveImage(primaryStage, canvasDrawing, canvasImage)
        }

        menuSaveAs.onAction = EventHandler {    // сохранить как
            saveAsSaveAs = true
            saveImage(primaryStage, canvasDrawing, canvasImage)
        }

        menuOpen.onAction = EventHandler {       // открыть изображение
            val fileName = openImage(primaryStage)

            val image = Image("file:\\$fileName")

            if (image.width > canvasImage.width){
                canvasImage.width = image.width
                canvasDrawing.width = image.width
                canvasSelecting.width = image.width

            }
            if (image.height > canvasImage.height){
                canvasImage.height = image.height
                canvasDrawing.height = image.height
                canvasSelecting.height = image.height
            }

            canvasImage.graphicsContext2D.drawImage(image, 0.0, 0.0)
            canvasDrawing.graphicsContext2D.clearRect(0.0, 0.0, canvasDrawing.width, canvasDrawing.height)
            canvasSelecting.graphicsContext2D.clearRect(0.0, 0.0, canvasSelecting.width, canvasSelecting.height)
        }

        menuClose.onAction = EventHandler {     // закрыть изображение
            canvasImage.height = screenHeight * windowScale / imageScale
            canvasImage.width = screenWidth * windowScale / imageScale
            canvasDrawing.height = screenHeight * windowScale / imageScale
            canvasDrawing.width = screenWidth * windowScale / imageScale
            canvasSelecting.height = screenHeight * windowScale / imageScale
            canvasSelecting.width = screenWidth * windowScale / imageScale

            canvasImage.graphicsContext2D.clearRect(0.0, 0.0, canvasImage.width, canvasImage.height)
            canvasDrawing.graphicsContext2D.clearRect(0.0, 0.0, canvasDrawing.width, canvasDrawing.height)
            canvasSelecting.graphicsContext2D.clearRect(0.0, 0.0, canvasSelecting.width, canvasSelecting.height)
        }
    }

    fun saveImage(primaryStage: Stage, canvasDrawing: Canvas, canvasImage: Canvas){
        var file: File? = null
        if (saveAsSaveAs){
            val fileChooser = FileChooser()
            val imageFilter = FileChooser.ExtensionFilter("Image Files", "*.png")
            fileChooser.extensionFilters.add(imageFilter)
            var path = getFileName()
            while (path[path.length - 1] != '\\'){
                path = path.substring(0, path.length - 1)
            }
            fileChooser.initialDirectory = File(path)
            file = fileChooser.showSaveDialog(primaryStage)
        }
        val params = SnapshotParameters()
        params.fill = Color.TRANSPARENT
        val image1 = canvasImage.snapshot(params, null)
        val image2 = canvasDrawing.snapshot(params, null)

        var x = 0
        var y = 0
        var width = 1920
        var height = 1080
        if (selecting){
            x = min(sel_x, last_x).toInt()
            y = min(sel_y, last_y).toInt()
            width = abs(sel_x - last_x).toInt()
            height = abs(sel_y - last_y).toInt()
        }

        val croppedImage = WritableImage(image1.pixelReader, x, y,width, height)
        val croppedDrawing = WritableImage(image2.pixelReader, x, y,width, height)

        val image3 = Canvas()
        image3.width = abs(sel_x - last_x)
        image3.height = abs(sel_y - last_y)
        image3.graphicsContext2D.drawImage(croppedImage, 0.0, 0.0)
        image3.graphicsContext2D.drawImage(croppedDrawing, 0.0, 0.0)
        val image4 = image3.snapshot(params, null)
        val bi: BufferedImage = SwingFXUtils.fromFXImage(image4, null)

        if (saveAsSaveAs){
            ImageIO.write(bi, "png", file)
            setFileName(file.toString())
        }
        else {
            ImageIO.write(bi, "png", File(getFileName()))
        }

    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Screenshot::class.java)
        }
    }
}


fun saveAsPng() {
    try {
        val robot = Robot()
        val fileName = getFileName()
        if (fileName == ""){
            println("Path reading error")
            return
        }

        val screenRect = Rectangle(Toolkit.getDefaultToolkit().getScreenSize())
        val screenFullImage = robot.createScreenCapture(screenRect)
        ImageIO.write(screenFullImage, "png", File(fileName))
//        println("Done")
    } catch (ex: IOException) {
        print(ex)
    }
}

fun openImage(primaryStage: Stage): String {
    try {
        var path = getFileName()
        while (path[path.length - 1] != '\\'){
            path = path.substring(0, path.length - 1)
        }
        val imageFilter = FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png")
        val fileChooser = FileChooser()
        val file: File?
        fileChooser.extensionFilters.add(imageFilter)
        fileChooser.initialDirectory = File(path)
        file = fileChooser.showOpenDialog(primaryStage)
        return file.toString()

    } catch (e: Exception){
        return ""
    }
}

fun getFileName(): String{
    try {
        val bufferedReader: BufferedReader = File("path.txt").bufferedReader()
        val path = bufferedReader.use { it.readText() }
        return path
    } catch (e: Exception){
        return ""
    }
}

fun setFileName(newPath: String){
    try {
        val bufferedWriter: BufferedWriter = File("path.txt").bufferedWriter()
        val path = bufferedWriter.use { it.write(newPath) }
    } catch (e: Exception){
        return
    }
}