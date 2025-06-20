# 📚 LiterAlura

**LiterAlura** es un catálogo interactivo de libros desarrollado en Java y Spring Boot, que permite consultar libros desde la API pública de Gutendex y guardarlos en una base de datos PostgreSQL.

## ⚙️ Tecnologías usadas
- Java 17
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- API Gutendex

## 🎯 Funcionalidades de la aplicación
1. Buscar libro por título y guardarlo en la base de datos
2. Listar libros registrados
3. Listar autores registrados
4. Listar autores vivos en un año específico
5. Listar libros por idioma

## 🎯 Recursos utilizados
- Spring Initializer (start.spring.io) para crear el proyecto Maven con Java 17.
- Dependencias principales: Spring Data JPA y PostgreSQL Driver.
- API Gutendex (gutendex.com) basada en el Proyecto Gutenberg.

## 🚀 Cómo ejecutar el proyecto
1. Clona el repositorio
2. Asegúrate de tener PostgreSQL instalado y corriendo.
3. Crea una base de datos llamada literalura.
4. Edita application.properties y reemplaza con tus propias credenciales de acceso a PostgreSQL:
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_CONTRASEÑA
