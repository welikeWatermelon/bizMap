import { useRef, useCallback, useState } from 'react';
import { importLibrary, setOptions } from '@googlemaps/js-api-loader';

let optionsSet = false;

export default function useMap() {
  const mapRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const markersRef = useRef([]);
  const infoWindowRef = useRef(null);
  const [loaded, setLoaded] = useState(false);

  const initMap = useCallback(async (element, options = {}) => {
    if (!optionsSet) {
      const key = import.meta.env.VITE_GOOGLE_MAPS_KEY;
      setOptions({ key });
      optionsSet = true;
    }

    const { Map, InfoWindow } = await importLibrary('maps');

    const map = new Map(element, {
      center: options.center || { lat: 37.5665, lng: 126.978 },
      zoom: options.zoom || 12,
    });

    mapInstanceRef.current = map;
    infoWindowRef.current = new InfoWindow();
    setLoaded(true);
    return map;
  }, []);

  const addMarkers = useCallback(async (pins, onClick) => {
    clearMarkers();
    if (!mapInstanceRef.current) return;

    const { Marker } = await importLibrary('marker');

    pins.forEach((pin) => {
      const marker = new Marker({
        position: { lat: pin.latitude, lng: pin.longitude },
        map: mapInstanceRef.current,
        title: pin.name,
      });

      if (onClick) {
        marker.addListener('click', () => {
          onClick(pin, marker);
        });
      }

      markersRef.current.push(marker);
    });
  }, []);

  const showInfoWindow = useCallback((content, marker) => {
    if (infoWindowRef.current && mapInstanceRef.current) {
      infoWindowRef.current.setContent(content);
      infoWindowRef.current.open(mapInstanceRef.current, marker);
    }
  }, []);

  const clearMarkers = useCallback(() => {
    markersRef.current.forEach((m) => m.setMap(null));
    markersRef.current = [];
  }, []);

  const getCenter = useCallback(() => {
    if (mapInstanceRef.current) {
      const center = mapInstanceRef.current.getCenter();
      return { lat: center.lat(), lng: center.lng() };
    }
    return null;
  }, []);

  return { mapRef, mapInstance: mapInstanceRef, loaded, initMap, addMarkers, showInfoWindow, clearMarkers, getCenter };
}
