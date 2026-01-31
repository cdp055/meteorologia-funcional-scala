# 📊 Proyecto Meteorología – Scala + FS2

## Descripción
Este proyecto fue desarrollado en **Scala** aplicando **Programación Funcional y Reactiva**.  
El objetivo es leer un archivo **CSV** con datos meteorológicos y calcular estadísticas utilizando **FS2 Streams** y **Cats Effect**.

El programa procesa información de estaciones meteorológicas y obtiene:

- **Coeficiente de desbalance de viento**  
- **Momento absoluto de orden 3 de la precipitación**

---

## Tecnologías Utilizadas
- Scala 3  
- FS2 (Functional Streams)  
- Cats Effect  
- sbt  
- IntelliJ IDEA  

---

---

## Datos de Ejemplo (CSV)

El archivo `meteorologia.csv` contiene los siguientes campos separados por `;`:

| Estación | Tipo   | Precipitación | Viento |
|----------|--------|---------------|--------|
| E001     | costa  | 125.30        | 45.20  |
| E002     | sierra | 280.50        | 32.80  |
| E003     | costa  | 142.80        | 48.60  |
| E004     | sierra | 265.40        | 35.90  |
| E005     | costa  | 138.60        | 42.70  |

## Funcionalidades

### 1. Lectura de CSV
Se utiliza **FS2** para leer el archivo como un *stream*, decodificar texto UTF-8 y transformar cada línea en un objeto Scala.

### 2. Modelado de Datos
Se usa un `case class` para representar cada registro:

```scala
case class Meteo(
  estacion: String,
  tipo: String,
  precipitacion: Double,
  viento: Double
)
 ``` 


## 3. Cálculos Estadísticos

### Coeficiente de Desbalance de Viento
**Fórmula:**

\[
CD = \frac{x_{max} - \mu}{\mu - x_{min}}
\]

Donde:  
- \(\mu\) = promedio de viento  
- \(x_{max}\) = valor máximo  
- \(x_{min}\) = valor mínimo  

---

### Momento Absoluto de Orden 3 (Precipitación)
**Fórmula:**

\[
\delta_3 = \frac{1}{N} \sum |x_i - \mu|^3
\]

Donde:  
- \(x_i\) = cada valor de precipitación  
- \(\mu\) = promedio  
- \(N\) = número total de registros  
- \(|x|\) = valor absoluto

## 📤 Ejemplo de Salida

El programa mostrará en consola:

```text
Registros leídos: 5
Coeficiente de desbalance de viento: 0.9175
Momento absoluto orden 3 precipitación: 334883.6481
 ```
##  Código completo

```scala
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

 ``` 
<img width="1912" height="1019" alt="image" src="https://github.com/user-attachments/assets/d9c90e9b-c832-42ac-83bd-3094110d8fd0" />

##  Resultado

<img width="577" height="169" alt="image" src="https://github.com/user-attachments/assets/d5206920-3178-4ad9-913b-adeba349ab3b" />

