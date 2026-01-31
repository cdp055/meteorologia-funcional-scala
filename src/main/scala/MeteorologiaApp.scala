import cats.effect.{IO, IOApp}
import fs2.*
import fs2.io.file.{Files, Path}
import fs2.text.*

object MeteorologiaApp extends IOApp.Simple {

  // ===== RUTA CSV =====
  val ruta = Path("src/main/resources/data/meteorologia.csv")

  // ===== MODELO =====
  case class Meteo(
                    estacion: String,
                    tipo: String,
                    precipitacion: Double,
                    viento: Double
                  )

  // ===== LECTURA CSV =====
  def leerCSV: IO[List[Meteo]] =
    Files[IO]
      .readAll(ruta)
      .through(utf8.decode)
      .through(lines)
      .drop(1)
      .filter(_.nonEmpty)
      .map(_.trim)
      .map { linea =>
        val c = linea.split(";")
        Meteo(
          c(0),
          c(1),
          c(2).toDouble,
          c(3).toDouble
        )
      }
      .compile
      .toList

  // ===== CÁLCULOS =====
  def calcular(datos: List[Meteo]): IO[Unit] = IO {

    val vientos = datos.map(_.viento)
    val precipitaciones = datos.map(_.precipitacion)

    val N = vientos.size.toDouble
    val mediaViento = vientos.sum / N
    val xMax = vientos.max
    val xMin = vientos.min

    val CD = (xMax - mediaViento) / (mediaViento - xMin)

    val mediaPrecip = precipitaciones.sum / precipitaciones.size
    val delta3 =
      precipitaciones
        .map(x => math.pow(math.abs(x - mediaPrecip), 3))
        .sum / precipitaciones.size

    println(f"Coeficiente de desbalance de viento: $CD%.4f")
    println(f"Momento absoluto orden 3 precipitación: $delta3%.4f")
  }

  // ===== MAIN =====
  override def run: IO[Unit] =
    for {
      datos <- leerCSV
      _ <- IO.println(s"Registros leídos: ${datos.length}")
      _ <- calcular(datos)
    } yield ()
}
