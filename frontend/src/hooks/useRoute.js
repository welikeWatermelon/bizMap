import { useState, useCallback } from 'react';

const GOOGLE_MAPS_BASE = 'https://www.google.com/maps/dir/?api=1';

function getGoogleMapsUrl(storeAddress, travelMode) {
  const params = new URLSearchParams({
    destination: storeAddress,
    travelmode: travelMode,
  });
  return `${GOOGLE_MAPS_BASE}&${params.toString()}`;
}

export default function useRoute() {
  const [selectedStore, setSelectedStore] = useState(null);
  const [urls, setUrls] = useState(null);

  const selectStore = useCallback((storeName, address) => {
    setSelectedStore({ storeName, address });
    setUrls({
      driving: getGoogleMapsUrl(address, 'driving'),
      walking: getGoogleMapsUrl(address, 'walking'),
    });
  }, []);

  const clearRoute = useCallback(() => {
    setSelectedStore(null);
    setUrls(null);
  }, []);

  return { selectedStore, urls, selectStore, clearRoute };
}
