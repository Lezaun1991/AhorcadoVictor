package com.example.juegoahorcado.servicios;

import com.example.juegoahorcado.entidades.Palabra;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class JuegoService {
    private List<Palabra> listaPalabrasFaciles = new ArrayList<>();
    private List<String> listaPalabrasAcertadas = new ArrayList<>();

    private List<Character> letraFallada = new ArrayList<>();
    private List<Character> letraAcertada = new ArrayList<>();
    private List<Character> palabraCreada = new ArrayList<>();

    // Obtener una palabra al azar de la lista de palabras fáciles
    public Palabra obtenerPalabraAlAzarFacil() {
        listaPalabrasFaciles();
        Random rand = new Random();
        int indiceAleatorio = rand.nextInt(this.listaPalabrasFaciles.size());
        int contador = 0;
        for (Palabra palabra : this.listaPalabrasFaciles) {
            palabra.getNombre();
            if (contador == indiceAleatorio) {
                return palabra;
            }
            contador++;
        }
        return null; // En caso de que algo falle
    }

    // Ocultar la palabra proporcionada con guiones bajos
    public String ocultarPalabra(Palabra palabra) {
        StringBuilder palabraEnmascarada = new StringBuilder();
        for (int i = 0; i < palabra.getNombre().length(); i++) {
            palabraEnmascarada.append("_" + " ");
        }
        return palabraEnmascarada.toString();
    }
    /**
     * Revela la letra en la palabra enmascarada y actualiza las listas de letras acertadas y falladas.
     *
     * @param session La sesión HTTP que contiene la información del juego.
     * @param letra   La letra que se intenta revelar en la palabra.
     * @return Una cadena representando la palabra enmascarada actualizada después de revelar la letra.
     */
    public String revelarLetra(HttpSession session, char letra) {
        // Obtener la palabra original y enmascarada de la sesión
        String palabraOriginal = (String) session.getAttribute("palabraOriginal");
        String palabraEnmascarada = (String) session.getAttribute("palabraEnmascarada");

        // Crear un StringBuilder a partir de la palabra enmascarada actual
        StringBuilder palabraRevelada = new StringBuilder(palabraEnmascarada);

        // Bandera para indicar si la letra fue encontrada en la palabra original
        boolean letraEncontrada = false;

        // Iterar sobre la palabra original para revelar la letra
        for (int i = 0; i < palabraOriginal.length(); i++) {
            if (palabraOriginal.charAt(i) == letra) {
                palabraRevelada.setCharAt(i, letra);
                letraEncontrada = true;
            }
        }

        // Si la letra fue encontrada y no ha sido acertada previamente, agregarla a la lista de acertadas
        if (letraEncontrada && !this.letraAcertada.contains(letra)) {
            this.letraAcertada.add(letra);
        }

        // Si la letra no fue encontrada, agregarla a la lista de falladas
        if (!letraEncontrada) {
            this.letraFallada.add(letra);
        }

        // Devolver la palabra enmascarada actualizada como una cadena
        return palabraRevelada.toString();
    }


    // Obtener la lista de letras falladas
    public List<Character> listaLetrasFallada() {
        return this.letraFallada;
    }

    // Obtener la lista de letras acertadas
    public List<Character> listaLetrasAcertada() {
        return this.letraAcertada;
    }

    // Obtener la lista de palabras acertadas
    public List<String> listaPalabras(String palabra) {
        this.listaPalabrasAcertadas.add(palabra);
        return listaPalabrasAcertadas;
    }

    public void reiniciarListaPalabras() {
        this.listaPalabrasAcertadas.clear();
    }

    // Inicializar la primera palabra para una nueva partida
    public void primeraPalabra(HttpSession session) {
        Integer intentos = 0;
        session.setAttribute("intentos", intentos);
        Palabra palabraInicial = obtenerPalabraAlAzarFacil();
        String palabraEnmascarada = ocultarPalabra(palabraInicial).trim();
        String nuevaPalabra = construirPalabraConEspacios(palabraInicial);
        String nuevaPista = obtenerPista(palabraInicial);
        session.setAttribute("palabraEnmascarada", palabraEnmascarada);
        session.setAttribute("nuevaPista", nuevaPista);
        session.setAttribute("palabraOriginal", nuevaPalabra.toUpperCase());
    }

    // Obtener la pista asociada a una palabra
    public String obtenerPista(Palabra palabra) {
        return palabra.getPista();
    }

    // Construir una representación de la palabra con espacios
    public String construirPalabraConEspacios(Palabra palabra) {
        StringBuilder nuevaPalabraBuilder = new StringBuilder();
        for (int i = 0; i < palabra.getNombre().length(); i++) {
            char caracterActual = palabra.getNombre().charAt(i);
            nuevaPalabraBuilder.append(caracterActual);

            // Agrega un espacio en blanco si no es el último carácter
            if (i < palabra.getNombre().length() - 1) {
                nuevaPalabraBuilder.append(' ');
            }
        }
        return nuevaPalabraBuilder.toString();
    }

    // Comprobar si la palabra enmascarada es igual a la original
    public Boolean palabraIgual(HttpSession session) {
        String palabraOriginal = (String) session.getAttribute("palabraOriginal");
        String palabraEnmascarada = (String) session.getAttribute("palabraEnmascarada");
        if (palabraOriginal!=null && palabraOriginal.equals(palabraEnmascarada)) {
            primeraPalabra(session);
            return true;
        }
        return false;
    }

    // Obtener la URL de la imagen basada en el número de intentos
    public String intentosImagen(HttpSession session) {
        Integer intentos = (Integer) session.getAttribute("intentos");
        // Si es la primera vez que juegas, inicializa los intentos a 0
        if (intentos == null) {
            intentos = 0;
        }
        // Lógica para determinar la imagen actual basada en el número de intentos
        String imagenActual = "/img/" + intentos + ".jpg";
        // Incrementar el número de intentos en la sesión
        session.setAttribute("intentos", intentos);
        return imagenActual;
    }

    // Comprobar si es la primera partida
    public boolean primeraPartida(HttpSession session) {
        String palabraEnmascarada = (String) session.getAttribute("palabraEnmascarada");
        String palabraOriginal = (String) session.getAttribute("palabraOriginal");
        return palabraEnmascarada == null && palabraOriginal == null;
    }

    // Obtener una lista de letras del abecedario
    public ArrayList<String> letras() {
        return new ArrayList<>(List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
                "M", "N", "Ñ", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"));
    }

    // Agregar una palabra a la lista de palabras creadas
    public List<Palabra> palabrasCreadas(Palabra palabra) {
        List<Palabra> listaCreadas = new ArrayList<>();
        listaCreadas.add(palabra);
        return listaCreadas;
    }

    // Inicializar la lista de palabras fáciles
    public List<Palabra> listaPalabrasFaciles(){
        listaPalabrasFaciles.add(new Palabra("casa", " Donde vives y encuentras refugio"));
        listaPalabrasFaciles.add(new Palabra("perro","El mejor amigo del hombre"));
        listaPalabrasFaciles.add(new Palabra("gato","Un felino doméstico independiente"));
        listaPalabrasFaciles.add(new Palabra("sol","Fuente de luz y calor"));
        listaPalabrasFaciles.add(new Palabra("flor","Es aromática, colorida y hay muchas"));
        listaPalabrasFaciles.add(new Palabra("mesa","Superficie plana con patas,con muchas utilidades"));
        listaPalabrasFaciles.add(new Palabra("cielo","cambia a lo largo del dia"));
        listaPalabrasFaciles.add(new Palabra("mar", " Gran extensión de agua salada"));
        listaPalabrasFaciles.add(new Palabra("piedra","Material sólido formado por minerales."));
        listaPalabrasFaciles.add(new Palabra("amigo","Persona cercana con la que compartes momentos especiales"));
        listaPalabrasFaciles.add(new Palabra("rojo","Color vibrante que representa la pasión"));
        listaPalabrasFaciles.add(new Palabra("hoja","Parte plana y delgada de una planta"));
        listaPalabrasFaciles.add(new Palabra("lago","Cuerpo de agua rodeado de tierra"));
        listaPalabrasFaciles.add(new Palabra("llave","Se usa para poder entrar a algun sitio"));
        listaPalabrasFaciles.add(new Palabra("nieve","hubo un monton en la filomena"));
        listaPalabrasFaciles.add(new Palabra("familia","todos tenemos una, algunos mas numerosos que otros"));
        listaPalabrasFaciles.add(new Palabra("luz","Es muy util en la oscuridad"));
        listaPalabrasFaciles.add(new Palabra("pan","hay muchas variedades y esta muy rico"));
        listaPalabrasFaciles.add(new Palabra("rio","En españa tenemos unos cuantos, hay muchos peces"));
        listaPalabrasFaciles.add(new Palabra("puerta","Hay que tener cuidado de no dar un golpe fuerte al salir"));
        return listaPalabrasFaciles;
    }
}