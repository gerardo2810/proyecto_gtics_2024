// Puntos del mapa

const locations = [
    { lat: -12.067713, lng: -77.067505 },
    { lat: -12.067739299285414, lng: -77.07048533833166 },
    { lat: -12.067991537715104, lng: -77.07550755645634 },
    { lat : -12.068533248724986, lng: -77.07808532986617},
    { lat: -12.069724275588285, lng: -77.07804924250952}
]


  // Inicializar mapa
  function initMap() {
    const map = new google.maps.Map(document.getElementById("map"), {
      zoom: 16,
      center: locations[0],
    });

    // Crear marcador
    const marker = new google.maps.Marker({
      position: locations[0],
      map: map,
      icon: {
        url: "../img/moto_icon.png", // Ruta de la imagen de la moto
        scaledSize: new google.maps.Size(55, 50) // Tamaño personalizado (ancho, alto)
      } // Ruta de la imagen de la moto
    });

    // Mover el marcador a través de los puntos

    let i = 0;
    const interval = setInterval(() => {
    i++;
    if (i >= locations.length) {
        clearInterval(interval); // Detener el intervalo cuando llega a la tercera posición
        return;
    }
    marker.setPosition(locations[i]);
    }, 2000); // Cambia de ubicación cada 3 segundos

  }
  