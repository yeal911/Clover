<img src="https://github.com/victorpj98/Clover/blob/master/Clover.png">

# Clover 
## Tu asistente personal para todos tus tratamientos 💊

<a href="https://www.youtube.com/watch?v=p8Nrj9qe8tU&feature=youtu.be&ab_channel=V%C3%ADctorP%C3%A9rez
" target="_blank"><img src="https://yt-embed.herokuapp.com/embed?v=p8Nrj9qe8tU" 
alt="IMAGE ALT TEXT HERE" border="10" /></a>

## Descripción
Clover es un asistente de seguimiento de tratamientos orientado a personas que necesitan llevar un control de su medicación, y es la aplicación con la que me presento al concurso de programación de apps basadas en HMS Core "Huawei Student Developers Spain".

## Experiencia de usuario
### Accesibilidad
La aplicación emplea **texto grande** donde es posible. A su vez, **la interfaz es minimalista** y con botones de gran tamaño para facilitar su comprensión.

Cuando el usuario utiliza por primera vez la aplicación, se muestra un **tutorial** que lo guía por las pantallas principales de la aplicación:

<img src="https://github.com/victorpj98/Clover/blob/master/tutorial1.jpg" width="480" height="800">
<img src="https://github.com/victorpj98/Clover/blob/master/tutorial2.jpg" width="480" height="800">


## Módulos HMS utilizados
En total, se han utilizado **7 módulos de HUAWEI Mobile Services**:

- Awareness Kit
- ML Kit
- Analytics Kit
- Ads Kit
- Map Kit
- Sites Kit
- Location Kit

### Awareness Kit 
Detecta el momento del día y el tiempo que hace para personalizar la interfaz de usuario.

#### Por horas (TimeBarriers)
- **Día**
<img src="https://github.com/victorpj98/Clover/blob/master/dia.jpg" width="480" height="800">


- **Atardecer**
<img src="https://github.com/victorpj98/Clover/blob/master/tarde.jpg" width="480" height="800">


- **Noche**
<img src="https://github.com/victorpj98/Clover/blob/master/noche.jpg" width="480" height="800">

#### Por tiempo meteorológico
- **Despejado**
- **Lluvia**
<img src="https://github.com/victorpj98/Clover/blob/master/lluvia.jpg" width="480" height="800">

- **Nieve**
<img src="https://github.com/victorpj98/Clover/blob/master/nieve.jpg" width="480" height="800">

*Importante: funcionalidad sujeta a la disponibilidad del servicio para cada usuario.*

### ML Kit
El usuario puede dar nombre a sus tratamientos utilizando fotos en las que aparezca el texto que quiera introducir; por ejemplo, nombres de medicamentos difíciles de escribir. El módulo de visión por computador es capaz de leer texto en cajas de medicamentos, revistas e incluso algunas notas escritas a mano.

<img src="https://github.com/victorpj98/Clover/blob/master/ML1.jpg" width="480" height="800">
<img src="https://github.com/victorpj98/Clover/blob/master/ML2.jpg" width="480" height="800">

Los análisis se realizan **en el teléfono**, por lo que el **funcionamiento sin conexión** de esta funcionalidad está **garantizado**.

### Analytics Kit
Permite llevar un control del tiempo meteorológico de la zona de los usuarios de la aplicación de manera completamente anónima. El propósito de esta recogida de datos es realizar un estudio estadístico que correlacione el país de procedencia, su climatología y la propensión de sus habitantes a contrer enfermedades que requieran de tratamientos específicos. Por razones de provacidad, ningún elemento referente a la naturaleza de dichos tratamientos es recogido.

### Ads Kit
Monetiza la aplicación mediante la inclusión de pequeños bloques de anuncios. Como esta versión es de prueba, solo se ha incluido uno. En la versión definitiva (disponible próximamente en AppGallery y otras tiendas), se emplearán diversos tipos de anuncios a lo largo de la aplicación. El sufijo 'huawei' del nombre del paquete hace referencia a la intención del desarrollador de habilitar las **compras en la aplicación** para que el usuario pueda habilitar espacios extra para guardar sus tratamientos por una pequeña cantidad (por determinar).

### Maps Kit, Location Kit y Sites Kit
Combinados, permiten al usuario encontrar su farmacia más cercana:

<img src="https://github.com/victorpj98/Clover/blob/master/sin_tratamientos.jpg" width="480" height="800">
<img src="https://github.com/victorpj98/Clover/blob/master/farmacias.jpg" width="480" height="800">

*Importante: funcionalidad sujeta a la configuración y disponibilidad de la ubicación de cada usuario.*

## Manejo de datos
La aplicación utiliza un archivo JSON en un directorio privado de la app para gestionar toda la información del usuario. Este archivo se carga y se modifica conforme el usuario ejecuta una acción:

<img src="https://github.com/victorpj98/Clover/blob/master/Diagrama.png">


## Bugs conocidos
Una aplicación no puede desarrollarse de forma íntegra en tan poco tiempo, y por eso la versión que se muestra aquí es la primera de un proyecto que planeo continuar al terminar el torneo. Algunas de las mejoras que tengo pensadas son:

- Incluir el nombre del usuario en la notificación.
- Rehacer el sistema de control de alarmas para permitir editarlas y eliminarlas.
- Convertir el método ````setWeather``` a estático para evitar repetirlo.
- Actualización del cielo desde el Intent de la actividad anterior para evitar cambios bruscos.
- Opción de modo sin conexión (mediante JSON) y basado en la nube para poder gestionar múltiples entradas de datos sin sobrecargar el peso de la app.
- Servicios extra usando HMS: Nearby para informar de las farmacias cercanas cuando te acerques a ellas, etc.
- Plan de monetización.

## Si has leído hasta aquí...
Quiero darte las gracias personalmente y, si lo lees en diciembre de 2020, aprovechar para desearte unas felices fiestas. 🎄
