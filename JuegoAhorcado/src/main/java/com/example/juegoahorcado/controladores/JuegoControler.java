package com.example.juegoahorcado.controladores;

import com.example.juegoahorcado.entidades.Palabra;
import com.example.juegoahorcado.servicios.JuegoService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class JuegoControler {
    private final JuegoService servicio;
    private final HttpSession session;

    @GetMapping("/juego/prueba")
    public String juegoInicio() {
        if (servicio.primeraPartida(session)) {
            servicio.reiniciarListaPalabras();
            // Es la primera partida, inicializar y mostrar la página de juego
            servicio.primeraPalabra(session);
            return "paginaJuego";
        } else {
            // No es la primera partida, limpiar listas y redirigir a la elección de dificultad
            servicio.listaLetrasAcertada().clear();
            servicio.listaLetrasFallada().clear();
            servicio.primeraPalabra(session);
            return "redirect:/elegir/facil";
        }
    }


    @GetMapping("/elegir/facil")
    public String palabraFacil(Model model) {
        // Recuperar atributos de la sesión
        String otraPista = (String) session.getAttribute("nuevaPista");
        String palabraEnmascarada = (String) session.getAttribute("palabraEnmascarada");
        String palabraOriginal = (String) session.getAttribute("palabraOriginal");
        List<String> listadoPalabras = (List<String>) session.getAttribute("listadoPalabras");

        // Logear información
        log.info("Listado de palabras: " + listadoPalabras);

        // Agregar atributos al modelo
        model.addAttribute("palabrasAcertadas", listadoPalabras);
        model.addAttribute("nuevaPista", otraPista);
        model.addAttribute("palabraOriginal", palabraOriginal);

        // Lógica para determinar la palabra enmascarada
        Integer intentos = (Integer) session.getAttribute("intentos");
        if (intentos != null && intentos > 5) {
            model.addAttribute("palabraEnmascarada", palabraOriginal);

        } else {
            model.addAttribute("palabraEnmascarada", palabraEnmascarada);
        }

        // Obtener la URL de la imagen y agregar al modelo
        String urlImagen = servicio.intentosImagen(session);
        model.addAttribute("urlImagen", urlImagen);

        // Agregar lista de letras al modelo
        ArrayList<String> listaLetras = servicio.letras();
        model.addAttribute("Abecedario", listaLetras);

        // Determinar si mostrar el botón
        boolean mostrarBoton = servicio.palabraIgual(session);
        model.addAttribute("mostrarBoton", mostrarBoton);

        // Si se debe mostrar el botón, actualizar la lista de palabras
        if (mostrarBoton) {
            List<String> listaPalabra = servicio.listaPalabras(palabraOriginal);
            session.setAttribute("listadoPalabras", listaPalabra);
            log.info("Listado de palabras actualizado: " + listaPalabra);
        }

        // Agregar listas de letras falladas y acertadas al modelo
        List<Character> listaLetrasFallada = servicio.listaLetrasFallada();
        List<Character> listaLetrasAcertada = servicio.listaLetrasAcertada();
        model.addAttribute("listaLetrasFalladas", listaLetrasFallada);
        model.addAttribute("listaLetrasAcertadas", listaLetrasAcertada);

        // Devolver la vista
        return "paginaPalabra";
    }


    @PostMapping("/revelarLetra")
    public String revelarLetra(@RequestParam char letra) {
        // Establecer la letra en la sesión
        session.setAttribute("letra", letra);

        // Obtener la palabra enmascarada de la sesión y revelar la letra
        String palabraEnmascaradaComparacion = (String) session.getAttribute("palabraEnmascarada");
        String palabraEnmascarada = servicio.revelarLetra(session, letra);

        // Actualizar el número de intentos si la palabra enmascarada cambia
        Integer intentos = (Integer) session.getAttribute("intentos");
        if (palabraEnmascarada.equals(palabraEnmascaradaComparacion)) {
            if (intentos == null) {
                intentos = 0;
            }
            intentos++;
            session.setAttribute("intentos", intentos);
            log.info("Número de intentos: " + intentos);
        }

        // Ajustar el número de intentos si excede el límite
        if (intentos > 6) {
            intentos--;
            session.setAttribute("intentos", intentos);
            log.info("Número de intentos ajustado: " + intentos);
        }

        // Actualizar la palabra enmascarada en la sesión
        session.setAttribute("palabraEnmascarada", palabraEnmascarada);

        // Redirigir a la página donde se muestra la palabra enmascarada actualizada
        return "redirect:/elegir/facil";
    }


    @GetMapping("/añadir/palabra")
    public String crearPalabra(Model model) {
        model.addAttribute("palabra", new Palabra());
        return "crearPalabra";
    }

    @PostMapping("/añadir/palabra/submit")
    public String añadirPalabra(@Valid @ModelAttribute("palabra") Palabra nuevaPalabra, BindingResult result) {
        if (result.hasErrors()) {
            return "crearPalabra"; // Devolver a la plantilla Thymeleaf con el formulario
        }

        // Procesar la nueva palabra
        servicio.palabrasCreadas(nuevaPalabra);

        // Configurar atributos de sesión con la nueva palabra
        String palabraConEspacios = servicio.construirPalabraConEspacios(nuevaPalabra).toUpperCase();
        session.setAttribute("palabraOriginal", palabraConEspacios);

        String palabraOculta = servicio.ocultarPalabra(nuevaPalabra).trim();
        session.setAttribute("palabraEnmascarada", palabraOculta);

        session.setAttribute("nuevaPista", nuevaPalabra.getPista());

        // Redirigir a la página donde se elige la dificultad
        return "redirect:/elegir/facil";
    }
}
