# SignupUsers

## ¿Qué es SignupUsers 2?

SignupUsers 2 es una Spring Boot Application, esto es, una aplicación web desarrollada en lenguaje Java con [Spring Framework](https://spring.io/projects/spring-framework) 
con un servidor de aplicaciones integrado por el contenedor generado por Spring.

## ¿Qué librerías utiliza?

En detalle, esta aplicación depende de las siguientes librerías:

* **Hibernate**: Librería con implementación de la especificación JPA (Java Persistance API), requerida para trabajar con bases de datos sobre un contenedor.
* **Spring Core 2.5.14**: Requerida para los demás componentes de Spring usados en esta aplicación
* **Spring Data JPA 2.5.14**: Properciona un contenedor para trabajar con JPA, además de código complementario.
* **Spring Boot 2.5.14**: Requerida para desarrollar microservicios web.
* **Spring Web 2.5.14**: Requerido por Spring Boot para código complementario.
* **Spring Test 2.5.14**: Requerido para pruebas unitarias en aplicaciones Java con Spring.
* **H2 Database**: Motor de base de datos que puede ser implementado separadamente o como parte de una aplicación.
* **Flyway**: Librería para cargar migraciones a bases de datos en uno o varios scripts.
* **JUnit**: Librería para implementación de pruebas unitarias.
* **Mockito**: Librería complementaria que se usa en combinación con JUnit para generar objetos "dummy" para simular la ejecución de un método.
* **Hamcrest 2.2**: Otra librería complementaria para pruebas unitarias que se usa en conjunto con Mockito.
* **ModelMapper**: Librería para una fácil conversión de un objeto a otro de una misma clase u otra similar en términos de atributos.
* **Lombok**: Librería para generar interna y automáticamente código fuente.
* **jjwt 0.12.6**: Librería para lectura y generación de JWT. 

La aplicación además, utiliza Gradle 7.4 para la descarga de las dependencias.

## Instalación

Para instalar esta aplicación, se requiere descargar e instalar los siguientes programas: 

* [Git](https://git-scm.com/downloads) (use la versión correspondiente a su sistema operativo)
* [Gradle 7.4 o inferior](https://gradle.org/releases/#7.4)
* [OpenJDK 11](https://jdk.java.net/archive/) _Nota: OpenJDK se puede sustituir por [Oracle JDK](https://www.oracle.com/java/technologies/downloads/archive/)_

Una vez instalados, los programas, siga los siguientes pasos:

1. Agregue la variable JAVA_HOME a las variables de entorno de su sistema operativo con valor igual al directorio en el que fue instalado
   el JDK descargado.
2. _Opcional_: Agregue el directorio bin que está dentro de su instalación de Gradle al final de la variable de entorno PATH si no desea ingresar la
   ruta absoluta del ejecutable de Gradle.
3. _Opcional_: Agregue el directorio bin que está dentro de la instalación de la versión escogida de JDK al final de la variable de entorno PATH si
   no desea ingresar la ruta absoluta del ejecutable de java.
4. Abra una ventana de línea de comandos (terminal para UNIX, Símbolo de sistema para Windows)
5. En línea de comandos, diríjase a la carpeta deseada (usando md y/o cd en el caso de Windows o mkdir y/o cd en el caso de UNIX)
6. Estando en la carpeta deseada, ejecute ```git clone --branch master https://github.com/mrcoar/SignupUsers.git```
7. Entre al directorio descargado con ```cd SignupUsers```
8. Ejecute ```gradlew.bat build``` (Windows) o ```./gradlew.sh build``` en UNIX para descargar las librerías que SignupUsers necesita para funcionar
   _Nota: Si usted está usando UNIX (Linux, Mac, etc.), asegúrese de que gradlew.sh tenga permisos de ejecución para el usuario_
9. Para ejecutar la aplicación, se tiene dos alternativas:
   * Ejecutar ```gradlew.bat bootRun``` (Windows) o ```./gradlew.sh bootRun``` en UNIX
   * Ejecutar ```cd build\libs``` (Windows) o ```cd build/libs``` (UNIX) y ejecutar ```java -jar SignupUsers-0.0.1-SNAPSHOT.jar```
     _Nota: Esto asume que se usted siguió el paso 3. Si no lo hizo, anteponga a la palabra java la ruta absoluta a ese archivo antes de ejecutar el comando_

En caso de éxito, Spring creará el contenedor para la aplicación web y la base de datos, H2 levantará la base de datos interna y Flyway se encargará de ejecutar los scripts sql
para crear las tablas en la base de datos.

## Ejecución

Para poder probar los RESTFul implementados en esta aplicación, utilice [Postman](https://www.postman.com/) u algún otro cliente apropiado de Web Service REST.
Las URL para ejecutar los RESTFUL son:
* localhost:8080/sign-up
* localhost:8080/login

En la siguiente sección se explicará en qué consiste cada URL:

## Uso

Esta aplicación define Un RESTFul (es decir, webservice REST). Para una mayor ilustración, refiérase a los archivos 
.PNG incluidos en el directorio raíz de SignupUsers 2:

### /sign-up 

Método HTTP PUT para insertar un usuario y sus números telefónicos, si es que tiene.
Debe tener un cuerpo en formato JSON (Javascript Object Notation) conteniendo los siguientes campos:
* name: 
    El nombre del usuario. Máximo 100 caracteres. Obligatorio
* email: 
    Correo electrónico del usuario. Debe respetar el formato impuesto por el [RFC-5322](https://www.rfc-editor.org/info/rfc5322), es decir,
    ```<nombre>@<dominio>.<extension>```. Obligatorio
* password: 
    Contraseña del usuario. Debe tener entre 8 y 12 caracteres de largo, debe tener exactamente una letra mayúscula, exactamente dos dígitos
    que no tienen que ser consecutivos y el resto de los caracteres deben ser letas minúsculas. Campo obligatorio
* phones:
    Un arreglo con uno o más números telefónicos. Cada elemento en el arreglo debe tener la siguiente información:
   * phoneNumber: El número telefónico propiamente tal. Número entero positivo. Obligatorio
   * cityCode: El código correspondiente a la ciudad a la que pertenece el número. Obligatorio
   * countryCode: Código numérico representando al país de origen
  
En caso de éxito, se obtendrá, junto al status HTTP 201 ("Created"), un texto en formato JSON conteniendo los campos ingresados por usted más los siguientes:

   * id: Identificador asignado automáticamente al usuario en formato [UUID](https://es.wikipedia.org/wiki/Identificador_%C3%BAnico_universal)
   * created: Fecha y hora en que el usuario fue creado
   * lastLogin: Fecha última autentificación. Al momento de la creación del usuario, este campo tendrá el mismo valor que created
   * isActive: Bandera booleana para indicar si el usuario está activo (```true```) o inactivo (```false```)
   * token: Un token de autentificación generado en formato JWT (JSON Web Token), definido por el [RFC-7519](https://www.rfc-editor.org/info/rfc7519)

  Revise más abajo los casos de error.

## Escenarios de error

En caso de un error al invocar a /sign-up o a login, la salida será un texto en formato JSON el siguiente campo:

* detail: Detalle del error en inglés.

Además, se obtendrá un status HTTP del orden de 400 cuyo valor exacto dependerá del error obtenido.

La siguiente lista muestra los posibles errores que se obtendrán al invocar a cualquiera de los dos RESTful definidos en esta aplicación.
Excepto donde se indique, para cada código de error se obtendrá un código HTTP 400 ("Bad Request")

1. Email no ingresado al invocar a /sign-up
2. Contraseña no ingresada al invocar a /sign-up
3. Formato inválido para la contraseña al invocar a /sign-up
4. Usuario ya existente en la base de datos al invocar a /sign-up. Además, se obtiene un status HTTP 403 ("Forbidden")
5. Formato inválido para el email al invocar a /sign-up.

También se pueden obtener errores con código negativo y status HTTP 500 ("Internal Server Error"). Para estos caso, sírvase
comunicarse con el desarrollador de este programa para hacer las correcciones pertinentes.

## Troubleshooting

En caso de que ejecute gradlew.bat o gradlew.sh y obtiene un mensaje de error indicando que no se encontró una clase de Gradle
(ClassNotFoundException), asumiendo que usted instaló Gradle 7.4 y agregó el directorio bin de esa instalación en la variable
de entorno PATH (en caso contrario, ingrese la ruta absoluta al archivo gradle.bat o gradle.sh), ejecute lo siguiente desde 
el directorio raíz de SignupUsers 2:

```gradle wrapper```

Esto generará una librería con el wrapper de Gradle de acuerdo a esa versión, la cual será utilizada en subsecuentes ejecuciones
de gradlew.
