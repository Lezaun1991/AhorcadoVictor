// En tu página HTML o plantilla Thymeleaf, incluye el siguiente script

    // Verifica si se ha alcanzado el límite
   //Puedes establecer un valor predeterminado si es necesario/* Obtén el valor de la variable desde el modelo o el backend */;

    if (intentos > 5) {
    // Deshabilita la interacción con la página

    // Muestra tu botón o mensaje
    // Puedes personalizar esto según tus necesidades
    let botonReinicio = document.createElement("button");
    botonReinicio.id = "";
    const boton = document.getElementById("botonJavaScript");
    botonReinicio.innerHTML = "Has Perdido, volver a jugar";
    botonReinicio.classList.add("botonDerrota");
    boton.appendChild(botonReinicio);

    // Asigna una función al clic del botón
    botonReinicio.onclick = function() {
    // Redirige a la página de inicio o realiza las acciones necesarias
    window.location.href = "/juego/prueba";
};
        console.log("Papitooo")
        document.body.style.pointerEvents = 'none';
        botonReinicio.style.pointerEvents = 'auto';
}

