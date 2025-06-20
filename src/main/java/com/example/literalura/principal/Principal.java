package com.example.literalura.principal;

import com.example.literalura.model.*;
import com.example.literalura.model.DatosLibros;
import com.example.literalura.repository.AutorRepository;
import com.example.literalura.repository.LibroRepository;
import com.example.literalura.service.ConsumoAPI;
import com.example.literalura.service.ConvierteDatos;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

@Component
public class Principal {

    private static final String URL_BASE = "https://gutendex.com/books/";

    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final ConvierteDatos conversor = new ConvierteDatos();
    private final Scanner teclado = new Scanner(System.in);

    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu() {
        var json = consumoAPI.obtenerDatos(URL_BASE);
        System.out.println(json);
        var datos = conversor.obtenerDatos(json, Datos.class);
        System.out.println("\nMostrar los datos: ");
        System.out.println(datos);
        System.out.println("-----------------------------------------------------------------------");

        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n======= MENÚ LITERALURA ========");
            System.out.println("1. Buscar libro por título");
            System.out.println("2. Mostrar libros registrados");
            System.out.println("3. Mostrar autores registrados");
            System.out.println("4. Mostrar autores vivos en un año específico");
            System.out.println("5. Mostrar libros por idioma");
            System.out.println("0. Salir");
            System.out.println("-----------------------------------------------------------------------");
            System.out.print("\nSeleccione una opción: ");

            try {
                opcion = teclado.nextInt();
                teclado.nextLine(); // Limpiar buffer

                switch (opcion) {
                    case 1 -> buscarYGuardarLibro();
                    case 2 -> mostrarLibros();
                    case 3 -> mostrarAutores();
                    case 4 -> mostrarAutoresVivos();
                    case 5 -> mostrarLibrosPorIdioma();
                    case 0 -> System.out.println("\nGracias por usar LiterAlura.");
                    default -> System.out.println("\nOpción no válida.");
                }
            } catch (InputMismatchException e) {
                System.out.println("\nEntrada inválida.");
                teclado.nextLine();
            }
        }
    }

    private void buscarYGuardarLibro() {
        System.out.print("Ingrese el título del libro: ");
        String titulo = teclado.nextLine();
        String urlBusqueda = URL_BASE + "?search=" + titulo.replace(" ", "+");

        String json = consumoAPI.obtenerDatos(urlBusqueda);
        Datos datos = conversor.obtenerDatos(json, Datos.class);

        if (datos.resultados().isEmpty()) {
            System.out.println("❌ Libro no encontrado.");
            return;
        }

        DatosLibros libroJson = datos.resultados().get(0);
        String tituloLibro = libroJson.titulo();

        if (libroRepository.existsByTitulo(tituloLibro)) {
            System.out.println("⚠️ El libro ya está registrado.");
            return;
        }

        // Obtener autor desde JSON
        DatosAutor autorJson = libroJson.autor().isEmpty() ? null : libroJson.autor().get(0);
        Autor autor = null;

        if (autorJson != null) {
            String nombre = autorJson.nombre();
            Integer nacimiento = parsearEntero(autorJson.fechaDeNacimiento());
            Integer fallecimiento = parsearEntero(autorJson.fechaDeFallecimiento());

            if (autorRepository.existsByNombre(nombre)) {
                autor = autorRepository.findByNombre(nombre);
            } else {
                autor = new Autor(nombre, nacimiento, fallecimiento);
                autorRepository.save(autor);
            }
        }

        // Crear y guardar libro
        String idioma = libroJson.idiomas().isEmpty() ? "desconocido" : libroJson.idiomas().get(0);
        Double descargas = libroJson.numeroDeDescargas() == null ? 0.0 : libroJson.numeroDeDescargas();

        Libro libro = new Libro(tituloLibro, idioma, descargas, autor);
        libroRepository.save(libro);

        System.out.println("✅ Libro guardado correctamente.");
    }

    private void mostrarLibros() {
        var libros = libroRepository.findAll();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados.");
            return;
        }

        System.out.println("\n📚 Libros en base de datos:");
        for (Libro libro : libros) {
            System.out.println("Título: " + libro.getTitulo());
            System.out.println("Idioma: " + libro.getIdioma());
            System.out.println("Descargas: " + libro.getNumeroDeDescargas());
            System.out.println("Autor: " + (libro.getAutor() != null ? libro.getAutor().getNombre() : "Desconocido"));
            System.out.println("----------------------------------------------------------------");
        }
    }

    private void mostrarAutores() {
        var autores = autorRepository.findAll();
        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados.");
            return;
        }

        System.out.println("\nAutores registrados:");
        for (Autor autor : autores) {
            System.out.println("Nombre: " + autor.getNombre());
            System.out.println("Nacimiento: " + autor.getFechaDeNacimiento());
            System.out.println("Fallecimiento: " + autor.getFechaDeFallecimiento());
            System.out.println("--------------------------");
        }
    }

    private void mostrarAutoresVivos() {
        System.out.print("Ingrese un año para ver autores vivos: ");
        int anio = teclado.nextInt();
        teclado.nextLine();

        List<Autor> autores = autorRepository.findAll();
        boolean encontrado = false;

        for (Autor autor : autores) {
            Integer nacimiento = autor.getFechaDeNacimiento();
            Integer fallecimiento = autor.getFechaDeFallecimiento();

            if (nacimiento != null && fallecimiento != null && nacimiento <= anio && fallecimiento >= anio) {
                System.out.println("👴 Autor vivo en " + anio + ": " + autor.getNombre());
                encontrado = true;
            }
        }

        if (!encontrado) {
            System.out.println("No se encontraron autores vivos en ese año.");
        }
    }

    private void mostrarLibrosPorIdioma() {
        System.out.print("Ingrese el código del idioma (ej. 'en', 'es', 'fr'): ");
        String idioma = teclado.nextLine();

        List<Libro> libros = libroRepository.findByIdioma(idioma);

        if (libros.isEmpty()) {
            System.out.println("No se encontraron libros en el idioma especificado.");
        } else {
            System.out.println("\n📚 Libros en idioma '" + idioma + "':");
            for (Libro libro : libros) {
                System.out.println("- " + libro.getTitulo());
            }
        }
    }

    private Integer parsearEntero(String valor) {
        try {
            return valor == null ? null : Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }


}
